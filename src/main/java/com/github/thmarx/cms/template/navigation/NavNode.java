/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.navigation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 *
 * @author t.marx
 */
@Data
@RequiredArgsConstructor
public class NavNode {
	private final String name;
	private final String path;
	private int depth = 1;
	private boolean current = false;
}
