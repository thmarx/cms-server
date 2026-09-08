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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Persistent inverted index for one source site. All reference directions share a commit. */
final class LuceneUsageStore implements AutoCloseable {
    private static final String VERSION = "1";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final Gson JSON = new Gson();
    private final FSDirectory directory;
    private final IndexWriter writer;
    private DirectoryReader reader;
    private boolean dirty;
    private List<UsageProblem> siteProblems;

    LuceneUsageStore(Path siteRoot) throws IOException {
        directory = FSDirectory.open(siteRoot.resolve("data/usage/index"));
        IndexWriter openedWriter = null;
        try {
            boolean recreate = false;
            if (DirectoryReader.indexExists(directory)) {
                try (var existing = DirectoryReader.open(directory)) {
                    recreate = !VERSION.equals(existing.getIndexCommit().getUserData().get("usage-format"));
                }
            }
            openedWriter = new IndexWriter(directory, new IndexWriterConfig(new KeywordAnalyzer())
                    .setOpenMode(recreate ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND));
            writer = openedWriter;
            reader = DirectoryReader.open(writer);
            var settings = new HashMap<String, String>();
            writer.getLiveCommitData().forEach(entry -> settings.put(entry.getKey(), entry.getValue()));
            String problems = settings.get("site-problems");
            siteProblems = problems == null ? List.of() : JSON.fromJson(problems, new TypeToken<List<UsageProblem>>() {}.getType());
            dirty = !VERSION.equals(settings.get("usage-format"));
        } catch (Exception ex) {
            if (openedWriter != null) openedWriter.close();
            directory.close();
            throw ex;
        }
    }

    synchronized List<PersistedUsageSource> sources() throws IOException {
        var result = new ArrayList<PersistedUsageSource>();
        for (var doc : search(new MatchAllDocsQuery())) {
            UsageResource resource = JSON.fromJson(doc.get("resource"), UsageResource.class);
            Map<String, Object> metadata = new Yaml(new SafeConstructor(new LoaderOptions())).load(doc.get("metadata"));
            List<UsageReference> references = JSON.fromJson(doc.get("references"), new TypeToken<List<UsageReference>>() {}.getType());
            List<UsageProblem> problems = JSON.fromJson(doc.get("problems"), new TypeToken<List<UsageProblem>>() {}.getType());
            List<UsageProblem> extractionProblems = JSON.fromJson(doc.get("extraction-problems"), new TypeToken<List<UsageProblem>>() {}.getType());
            result.add(new PersistedUsageSource(new UsageDocument(resource, doc.get("public-path"), metadata, references),
                    doc.get("file-stamp"), doc.get("schema"), usages(doc), extractionProblems, problems));
        }
        return List.copyOf(result);
    }

    synchronized void update(PersistedUsageSource source) throws IOException {
        var value = source.document();
        var document = new Document();
        document.add(new StringField(SOURCE, key(value.resource()), Field.Store.NO));
        source.usages().stream().map(Usage::target).distinct().forEach(target ->
                document.add(new StringField(TARGET, key(target), Field.Store.NO)));
        document.add(new StoredField("resource", JSON.toJson(value.resource())));
        if (value.publicPath() != null) document.add(new StoredField("public-path", value.publicPath()));
        // YAML preserves date/number metadata types required by collection route templates.
        document.add(new StoredField("metadata", new Yaml().dump(value.metadata())));
        document.add(new StoredField("references", JSON.toJson(value.references())));
        document.add(new StoredField("usages", JSON.toJson(source.usages())));
        document.add(new StoredField("problems", JSON.toJson(source.problems())));
        document.add(new StoredField("extraction-problems", JSON.toJson(source.extractionProblems())));
        document.add(new StoredField("file-stamp", source.fileStamp()));
        document.add(new StoredField("schema", source.schema()));
        writer.updateDocument(new Term(SOURCE, key(value.resource())), document);
        dirty = true;
    }

    synchronized void delete(UsageResource source) throws IOException {
        writer.deleteDocuments(new Term(SOURCE, key(source)));
        dirty = true;
    }

    synchronized List<Usage> incoming(UsageResource target) throws IOException {
        return search(new TermQuery(new Term(TARGET, key(target)))).stream().flatMap(doc -> usages(doc).stream())
                .filter(usage -> usage.target().equals(target)).toList();
    }

    synchronized List<Usage> outgoing(UsageResource source) throws IOException {
        return search(new TermQuery(new Term(SOURCE, key(source)))).stream().flatMap(doc -> usages(doc).stream()).toList();
    }

    synchronized List<UsageProblem> siteProblems() { return siteProblems; }

    synchronized void commit(List<UsageProblem> problems) throws IOException {
        var nextProblems = List.copyOf(problems);
        if (!siteProblems.equals(nextProblems)) dirty = true;
        if (!dirty) return;
        writer.setLiveCommitData(Map.of("usage-format", VERSION, "site-problems", JSON.toJson(nextProblems)).entrySet());
        writer.commit();
        var next = DirectoryReader.openIfChanged(reader, writer);
        if (next != null) {
            var previous = reader;
            reader = next;
            previous.close();
        }
        siteProblems = nextProblems;
        dirty = false;
    }

    private List<Document> search(Query query) throws IOException {
        var searcher = new IndexSearcher(reader);
        var stored = searcher.storedFields();
        var result = new ArrayList<Document>();
        ScoreDoc after = null;
        while (true) {
            var page = searcher.searchAfter(after, query, 256);
            for (var hit : page.scoreDocs) result.add(stored.document(hit.doc));
            if (page.scoreDocs.length < 256) break;
            after = page.scoreDocs[page.scoreDocs.length - 1];
        }
        return result;
    }

    private static List<Usage> usages(Document document) {
        return JSON.fromJson(document.get("usages"), new TypeToken<List<Usage>>() {}.getType());
    }

    private static String key(UsageResource resource) { return JSON.toJson(resource); }

    @Override
    public synchronized void close() throws IOException {
        try {
            commit(siteProblems);
        } finally {
            try { reader.close(); }
            finally {
                try { writer.close(); }
                finally { directory.close(); }
            }
        }
    }
}
