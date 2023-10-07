/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import java.io.File;

/**
 *
 * @author t.marx
 */
public record FileEvent(File file, Type type) {

	public enum Type {
		CREATED,
		MODIFIED,
		DELETED
	}
}
