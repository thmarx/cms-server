package com.condation.cms.content.usage;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.usage.*;
import com.condation.cms.api.ui.elements.ContentTypes;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.content.CollectionRouteTemplate;
import com.condation.cms.core.content.io.ContentFileParser;
import com.google.gson.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Persistent usage index for exactly one site. */
public final class EditorialUsageIndex implements UsageIndex, AutoCloseable {
    private final UsageSite site;
    private final LuceneUsageStore store;
    // Raw extractions are cached for route resolution; incoming/outgoing queries run against Lucene.
    private final Map<UsageResource, UsageDocument> documents = new LinkedHashMap<>();
    private final Map<UsageResource, PersistedUsageSource> persisted = new LinkedHashMap<>();
    private final Map<UsageResource, SourceStamp> stamps = new HashMap<>();
    private final List<UsageProblem> extractionProblems = new ArrayList<>();
    private volatile List<UsageProblem> currentProblems = List.of();
    private record SourceStamp(String file, String schema) {}

    public EditorialUsageIndex(UsageSite site) {
        this.site = Objects.requireNonNull(site);
        try {
            store = new LuceneUsageStore(site.root());
            try {
                for (var source : store.sources()) {
                    var resource = source.document().resource();
                    if (!resource.site().equals(site.id())) {
                        store.delete(resource);
                        continue;
                    }
                    persisted.put(resource, source);
                    documents.put(resource, source.document());
                    stamps.put(resource, new SourceStamp(source.fileStamp(), source.schema()));
                    extractionProblems.addAll(source.extractionProblems());
                }
                var siteProblems = store.siteProblems().stream().filter(problem -> problem.site().equals(site.id())).toList();
                extractionProblems.addAll(siteProblems);
                store.commit(siteProblems);
            } catch (Exception ex) {
                store.close();
                throw ex;
            }
            extractionProblems.add(new UsageProblem(site.id(), "", "Startup reconciliation pending"));
            currentProblems = java.util.stream.Stream.concat(extractionProblems.stream(),
                    persisted.values().stream().flatMap(source -> source.problems().stream())).distinct().toList();
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot open usage index for " + site.id(), ex);
        }
    }

    /** Startup/configuration reconciliation: unchanged files and schemas reuse stored extractions. */
    public synchronized void synchronize() {
        readSite(false);
        publish();
    }

    @Override
    public synchronized void rebuild() {
        readSite(true);
        publish();
    }

    /** Called after the primary metadata index has processed a file or directory change. */
    public synchronized void refresh(Path changed) {
        Path file = changed.toAbsolutePath().normalize();
        var root = sourceRoot(site, file);
        if (root == null) return;
        if (!file.toString().endsWith(".md")) {
            readSite(false);
        } else {
            var resource = resource(site, root, file);
            extractionProblems.removeIf(problem -> problem.path().equals(resource.path()));
            if (!Files.exists(file)) {
                documents.remove(resource);
                stamps.remove(resource);
            } else {
                try {
                    var types = site.contentTypes().get();
                    readFile(site, types, schema(types), root, file, documents);
                } catch (Exception ex) { problem(site, resource.path(), ex); }
            }
        }
        // Target routes may have changed too. Re-resolve stored extractions, only write changed Lucene documents.
        publish();
    }

    private void readSite(boolean force) {
        var oldProblems = extractionProblems.stream().filter(problem -> problem.site().equals(site.id())).toList();
        var replacement = new LinkedHashMap<UsageResource, UsageDocument>();
        try {
            ContentTypes types = site.contentTypes().get();
            String schema = schema(types);
            extractionProblems.removeIf(problem -> problem.site().equals(site.id()));
            for (String folder : List.of("content", "collections")) {
                Path root = site.root().resolve(folder);
                if (!Files.isDirectory(root)) continue;
                try (var files = Files.walk(root)) {
                    for (var file : files.filter(Files::isRegularFile).filter(PathUtil::isContentFile).sorted().toList()) {
                        var resource = resource(site, root, file);
                        if (!localSource(site, resource)) continue;
                        try {
                            var stamp = new SourceStamp(fileStamp(file), schema);
                            if (!force && stamp.equals(stamps.get(resource)) && documents.containsKey(resource)) {
                                replacement.put(resource, documents.get(resource));
                                oldProblems.stream().filter(problem -> problem.path().equals(resource.path())).forEach(extractionProblems::add);
                            } else {
                                readFile(site, types, schema, root, file, replacement);
                            }
                        } catch (Exception ex) {
                            problem(site, resource.path(), ex);
                            // Keep the last good stamp as well, so failed files are retried after restart.
                            if (documents.containsKey(resource)) replacement.put(resource, documents.get(resource));
                        }
                    }
                }
            }
            documents.keySet().removeIf(source -> source.site().equals(site.id()));
            documents.putAll(replacement);
            stamps.keySet().removeIf(source -> source.site().equals(site.id()) && !replacement.containsKey(source));
        } catch (Exception ex) { problem(site, "", ex); }
    }

    private void readFile(UsageSite site, ContentTypes types, String schema, Path root, Path file,
            Map<UsageResource, UsageDocument> destination) throws IOException {
        if (!file.toRealPath().startsWith(root.toRealPath())) throw new IOException("Source is outside its site root");
        var resource = resource(site, root, file);
        if (!localSource(site, resource)) {
            destination.remove(resource);
            stamps.remove(resource);
            return;
        }
        String before = fileStamp(file);
        var parser = new ContentFileParser(file.toString());
        var metadata = parser.getHeader();
        String publicPath = publicPath(site, resource, metadata);
        var references = new UsageExtractor(types, site, extractionProblems).extract(resource, metadata, parser.getContent());
        if (!before.equals(fileStamp(file))) throw new IOException("Source changed during extraction; retry required");
        destination.put(resource, new UsageDocument(resource, publicPath, metadata, references));
        stamps.put(resource, new SourceStamp(before, schema));
    }

    private static boolean localSource(UsageSite site, UsageResource resource) {
        if (resource.kind() != UsageResource.Kind.COLLECTION_ITEM) return true;
        String[] parts = resource.path().split("/");
        return parts.length == 2 && site.collectionSite(parts[0]).equals(site.id());
    }

    private static UsageResource resource(UsageSite site, Path root, Path file) {
        return new UsageResource(site.id(), root.endsWith("collections")
                ? UsageResource.Kind.COLLECTION_ITEM : UsageResource.Kind.CONTENT,
                root.relativize(file).toString().replace('\\', '/'));
    }

    private static String fileStamp(Path file) throws IOException {
        var attributes = Files.readAttributes(file, BasicFileAttributes.class);
        return attributes.lastModifiedTime() + ":" + attributes.size() + ":" + attributes.fileKey();
    }

    private static String schema(ContentTypes types) {
        try {
            // Map iteration order differs between JVMs. Canonicalize object keys before hashing.
            String serialized = canonical(new Gson().toJsonTree(types)).toString();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(("usage-extractor-1\n" + serialized).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }

    private static JsonElement canonical(JsonElement element) {
        if (element.isJsonObject()) {
            var result = new JsonObject();
            element.getAsJsonObject().keySet().stream().sorted().forEach(key ->
                    result.add(key, canonical(element.getAsJsonObject().get(key))));
            return result;
        }
        if (element.isJsonArray()) {
            var result = new JsonArray();
            element.getAsJsonArray().forEach(value -> result.add(canonical(value)));
            return result;
        }
        return element;
    }

    private String publicPath(UsageSite site, UsageResource resource, Map<String, Object> metadata) {
        if (resource.kind() == UsageResource.Kind.CONTENT) {
            String path = resource.path();
            String name = Path.of(path).getFileName().toString();
            if (com.condation.cms.api.utils.SectionUtil.isSectionEntry(name)) {
                path = path.substring(0, path.length() - name.length()) + name.substring(0, name.indexOf('.')) + ".md";
            }
            return site.db().getContent().byPath(path).map(node -> node.url()).orElse(PathUtil.toURL(path));
        }
        if (site.collections() == null) return null;
        String[] parts = resource.path().split("/", 2);
        return site.collections().collection(parts[0]).flatMap(definition -> definition.detailPage()).map(detail -> {
            try { return new CollectionRouteTemplate(detail).render(parts[1].substring(0, parts[1].length() - 3), metadata); }
            catch (IllegalArgumentException ex) { return null; }
        }).orElse(null);
    }

    private void publish() {
        var resolver = new UsageReferenceResolver(site, documents.values());
        var next = new LinkedHashMap<UsageResource, PersistedUsageSource>();
        var problems = new ArrayList<>(extractionProblems);
        for (var document : documents.values()) {
            var usages = new ArrayList<Usage>();
            var current = new UsageDocument(document.resource(), publicPath(site, document.resource(), document.metadata()),
                    document.metadata(), document.references());
            var old = persisted.get(document.resource());
            var previousUsages = old == null ? List.<Usage>of() : old.usages();
            for (var ref : document.references()) {
                try {
                    var target = resolver.resolve(current, ref);
                    if (target.isEmpty()) continue;
                    UsageResource resource = target.get();
                    Usage.TargetStatus status = resolver.status(resource);
                    if (resource.kind() == UsageResource.Kind.UNRESOLVED_URL || status == Usage.TargetStatus.MISSING) {
                        var previous = previousUsages.stream()
                                .filter(usage -> usage.location().equals(ref.location()) && usage.originalReference().equals(ref.value()))
                                .filter(usage -> usage.target().kind() != UsageResource.Kind.UNRESOLVED_URL).findFirst();
                        if (previous.isPresent()) {
                            resource = previous.get().target();
                            status = Usage.TargetStatus.MISSING;
                        }
                    }
                    usages.add(new Usage(document.resource(), resource, ref.location(), ref.origin(), ref.value(),
                            document.title(), document.status(), status));
                } catch (Exception ex) {
                    problems.add(new UsageProblem(site.id(), document.resource().path(),
                            "Cannot resolve " + ref.location() + ": " + ref.value() + " (" + ex.getMessage() + ")"));
                }
            }
            var stamp = stamps.get(document.resource());
            var sourceProblems = problems.stream().filter(problem -> problem.site().equals(site.id())
                    && problem.path().equals(document.resource().path())).toList();
            var sourceExtractionProblems = extractionProblems.stream().filter(problem -> problem.site().equals(site.id())
                    && problem.path().equals(document.resource().path())).toList();
            next.put(document.resource(), new PersistedUsageSource(current, stamp.file(), stamp.schema(),
                    List.copyOf(usages), sourceExtractionProblems, sourceProblems));
        }
        try {
            for (var old : persisted.keySet()) {
                if (!next.containsKey(old)) store.delete(old);
            }
            for (var source : next.values()) {
                if (!source.equals(persisted.get(source.document().resource()))) store.update(source);
            }
            var siteProblems = problems.stream().filter(problem -> problem.path().isEmpty()
                    || next.keySet().stream().noneMatch(key -> key.path().equals(problem.path()))).toList();
            store.commit(siteProblems);
            persisted.clear();
            persisted.putAll(next);
        } catch (IOException ex) {
            problems.add(new UsageProblem(site.id(), "", "Usage index commit failed: " + ex.getMessage()));
        }
        currentProblems = List.copyOf(problems);
    }

    private static Path sourceRoot(UsageSite site, Path file) {
        for (String folder : List.of("content", "collections")) {
            Path root = site.root().resolve(folder);
            if (file.startsWith(root)) return root;
        }
        return null;
    }

    private void problem(UsageSite site, String path, Exception ex) {
        extractionProblems.add(new UsageProblem(site.id(), path, "Extraction failed: " + ex.getMessage()));
    }

    @Override
    public synchronized List<Usage> incoming(UsageResource target) {
        try {
            return withLiveMediaStatus(store.incoming(target));
        } catch (IOException ex) { throw new UncheckedIOException(ex); }
    }

    @Override
    public synchronized List<Usage> outgoing(UsageResource source) {
        if (!source.site().equals(site.id())) return List.of();
        try { return withLiveMediaStatus(store.outgoing(source)); }
        catch (IOException ex) { throw new UncheckedIOException(ex); }
    }

    private List<Usage> withLiveMediaStatus(List<Usage> usages) {
        var resolver = new UsageReferenceResolver(site, documents.values());
        return usages.stream().map(usage -> usage.target().kind() != UsageResource.Kind.MEDIA ? usage :
                new Usage(usage.source(), usage.target(), usage.location(), usage.origin(), usage.originalReference(),
                        usage.sourceTitle(), usage.sourceStatus(), resolver.status(usage.target()))).toList();
    }

    @Override
    public List<UsageProblem> problems() { return currentProblems; }

    @Override
    public synchronized void close() {
        try { store.close(); }
        catch (IOException ex) { throw new UncheckedIOException(ex); }
        documents.clear();
        persisted.clear();
        stamps.clear();
        extractionProblems.clear();
        currentProblems = List.of();
    }
}
