/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class FileSystem {

	private final Path hostBaseDirectory;

	public Path resolve(String path) {
		return hostBaseDirectory.resolve(path);
	}
	
	public String loadContent (final Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8);
	}
	
	public List<String> loadLines (final Path file) throws IOException {
		return Files.readAllLines(file, StandardCharsets.UTF_8);
	}
}
