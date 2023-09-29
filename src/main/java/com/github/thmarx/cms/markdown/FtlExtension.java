/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.NullableDataKey;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author t.marx
 */
public class FtlExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
	 final public static NullableDataKey<String> SUPERSCRIPT_STYLE_HTML_OPEN = new NullableDataKey<>("FTL_HTML_OPEN");
    final public static NullableDataKey<String> SUPERSCRIPT_STYLE_HTML_CLOSE = new NullableDataKey<>("FTL_HTML_CLOSE");
	
	 private FtlExtension() {
    }

    public static FtlExtension create() {
        return new FtlExtension();
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new FtlDelimiterProcessor());
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new FtlNodeRenderer.Factory());
        }
    }
}
