/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.markdown;

import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class FtlNodeRenderer implements NodeRenderer {
    final private String superscriptStyleHtmlOpen;
    final private String superscriptStyleHtmlClose;

    public FtlNodeRenderer(DataHolder options) {
        superscriptStyleHtmlOpen = FtlExtension.SUPERSCRIPT_STYLE_HTML_OPEN.get(options);
        superscriptStyleHtmlClose = FtlExtension.SUPERSCRIPT_STYLE_HTML_CLOSE.get(options);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(FtlNode.class, this::render));
        return set;
    }

    private void render(FtlNode node, NodeRendererContext context, HtmlWriter html) {
        if (superscriptStyleHtmlOpen == null || superscriptStyleHtmlClose == null) {
            if (context.getHtmlOptions().sourcePositionParagraphLines) {
                html.withAttr().tag("ftl");
            } else {
                html.srcPos(node.getText()).withAttr().tag("ftl");
            }
            context.renderChildren(node);
            html.tag("/ftl");
        } else {
            html.raw(superscriptStyleHtmlOpen);
            context.renderChildren(node);
            html.raw(superscriptStyleHtmlClose);
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new FtlNodeRenderer(options);
        }
    }
}
