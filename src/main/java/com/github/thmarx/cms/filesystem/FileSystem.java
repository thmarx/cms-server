/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.utils.PathUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class FileSystem {

	private final Path hostBaseDirectory;

	private RecursiveWatcher watcher;

	final ContentParser contentParser = new ContentParser(this);
	private Path contentBase;

	@Getter
	private final MetaData metaData = new MetaData();

	public Path resolve(String path) {
		return hostBaseDirectory.resolve(path);
	}

	public String loadContent(final Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8);
	}

	public List<String> loadLines(final Path file) throws IOException {
		return Files.readAllLines(file, StandardCharsets.UTF_8);
	}
	
	public List<String> listContent (final Path base, final String start) {
		final Path contentBase = resolve("content/");
		var startPath = base.resolve(start);
		
		return metaData.nodes().entrySet().stream().filter(entry -> {
			var key = entry.getKey();
			
			var ePath = contentBase.resolve(key);
			
			var filename = ePath.toString().replace(startPath.toString(), "");
			
			if (filename.startsWith(File.separator)) {
				filename = filename.substring(1);
			}
			
			return -1 == filename.indexOf(File.separatorChar) && metaData.isVisible(key);
		}).map(entry -> entry.getKey()).collect(Collectors.toList());
	}

	private void addOrUpdateMetaData(Path file) throws IOException {
		Map<String, Object> fileMeta = contentParser.parseMeta(file);

		var uri = PathUtil.toUri(file, contentBase);

		metaData.add(new MetaData.Node(uri, fileMeta));
	}

	public void init() throws IOException {
		log.debug("init filesystem");

		this.contentBase = resolve("content/");
		this.watcher = new RecursiveWatcher(contentBase);
		watcher.getPublisher().subscribe(new Flow.Subscriber<FileEvent>() {
			Flow.Subscription subscription;

			@Override
			public void onSubscribe(Flow.Subscription subscription) {
				this.subscription = subscription;
				this.subscription.request(1);
			}

			@Override
			public void onNext(FileEvent item) {
				try {

					if (item.file().isDirectory()) {
						swapMetaData();
					} else {
						if (FileEvent.Type.DELETED.equals(item.type())) {

							var uri = PathUtil.toUri(item.file().toPath(), contentBase);

							metaData.remove(uri);
						} else {
							addOrUpdateMetaData(item.file().toPath());

						}
					}
				} catch (IOException ex) {
					log.error("", ex);
				}

				this.subscription.request(1);
			}

			@Override
			public void onError(Throwable throwable) {
			}

			@Override
			public void onComplete() {
			}

		});

		reInitFolder(contentBase);

		watcher.start();
	}

	private void swapMetaData() throws IOException {
		log.debug("rebuild metadata");
		metaData.clear();
		reInitFolder(contentBase);
	}

	private void reInitFolder(final Path folder) throws IOException {

		long before = System.currentTimeMillis();
		Files.walkFileTree(folder, new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				addOrUpdateMetaData(file);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});

		long after = System.currentTimeMillis();

		log.debug("loading metadata took " + (after - before) + "ms");
	}
}
