/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.list;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author t.marx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {
	
	public static final Page EMPTY = new Page(0, 0, 1, Collections.EMPTY_LIST);
	
	private int size;
	private long total;
	private int page;
	
	private List<T> items;
}
