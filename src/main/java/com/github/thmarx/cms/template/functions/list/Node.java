/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.functions.list;

import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */

public record Node (String name, String path, String content, Map<String, Object> meta) {}
