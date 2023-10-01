/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.extensions;

import io.undertow.server.HttpHandler;
import lombok.Data;

/**
 *
 * @author t.marx
 */
public record HttpHandlerExtension (String path, HttpHandler handler) {}
