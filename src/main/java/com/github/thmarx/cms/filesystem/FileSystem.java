/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.ContentParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class FileSystem {

	private final Path hostBaseDirectory;

	@Getter
	private final MetaData metaData = new MetaData();
	
	public Path resolve(String path) {
		return hostBaseDirectory.resolve(path);
	}
	
	public String loadContent (final Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8);
	}
	
	public List<String> loadLines (final Path file) throws IOException {
		return Files.readAllLines(file, StandardCharsets.UTF_8);
	}
	
	
	
	public void buildMetaData () throws IOException {
		Path contentBase = resolve("content/");
		final ContentParser contentParser = new ContentParser(this);
		Files.walkFileTree(contentBase, new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				
				Map<String, Object> fileMeta = contentParser.parseMeta(file);
				var uri = contentBase.relativize(file).toString();
				uri = uri.replaceAll("\\\\", "/");
				metaData.add(new MetaData.Node(uri, fileMeta));
				
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
