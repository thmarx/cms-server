/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;
import org.graalvm.polyglot.io.FileSystem;

/**
 *
 * @author t.marx
 */
public class ExtensionFileSystem implements FileSystem {

	private final Path extensionPath;

	public ExtensionFileSystem(final Path extensionPath) {
		this.extensionPath = extensionPath;
	}

	@Override
	public Path parsePath(URI uri) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public Path parsePath(String path) {
		if (path.startsWith("system/")) {
			return Path.of(path);
		}
		return extensionPath.resolve(path);
	}

	@Override
	public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public void delete(Path path) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
		if (path.startsWith("system/")) {
			var localPath = path.toString().replaceAll("\\\\", "/");
			InputStream resourceAsStream = ExtensionFileSystem.class.getResourceAsStream(localPath);
			
			byte[] content = new byte[0];
			if (resourceAsStream != null) {
				content = resourceAsStream.readAllBytes();
			}
			return new SeekableInMemoryByteChannel(content);
		}
		return Files.newByteChannel(path, options, attrs);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public Path toAbsolutePath(Path path) {
		return path.toAbsolutePath();
	}

	@Override
	public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
		return path;
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

}
