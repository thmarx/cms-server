/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.freemarker.list;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@Data
@RequiredArgsConstructor
public class Node {
	
	private final String name;
	private final String path;
	private final String content;
	
}
