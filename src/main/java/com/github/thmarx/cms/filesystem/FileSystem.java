/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.utils.PathUtil;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
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

	private void addOrUpdateMetaData(Path file) throws IOException {
		Map<String, Object> fileMeta = contentParser.parseMeta(file);
		
		var uri = PathUtil.toUri(file, contentBase);
		
		metaData.add(new MetaData.Node(uri, fileMeta));
	}

	public void init() throws IOException {

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
				if (FileEvent.Type.DELETED.equals(item.type())) {
					
					var uri = PathUtil.toUri(item.file().toPath(), contentBase);
					
					metaData.remove(uri);
				} else {
					try {
						addOrUpdateMetaData(item.file().toPath());
					} catch (IOException ex) {
						Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
					}
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
	
	private void reInitFolder (final Path folder) throws IOException {
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
	}
}
