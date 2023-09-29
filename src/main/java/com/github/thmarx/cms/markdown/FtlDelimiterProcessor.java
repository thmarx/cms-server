/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.markdown;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 *
 * @author t.marx
 */
public class FtlDelimiterProcessor implements DelimiterProcessor  {

	@Override
	public char getOpeningCharacter() {
		return '?';
	}

	@Override
	public char getClosingCharacter() {
		return '?';
	}

	@Override
	public int getMinLength() {
		return 1;
	}

	@Override
	public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
		if (opener.length() >= 1 && closer.length() >= 1) {
            return 1;
        } else {
            return 0;
        }
	}

	@Override
	public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
		 // Normal case, wrap nodes between delimiters in strikethrough.
        FtlNode ftlNode = new FtlNode(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));
        opener.moveNodesBetweenDelimitersTo(ftlNode, closer);
	}

	@Override
	public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiter) {
		return null;
	}

	@Override
	public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
		return true;
	}

	@Override
	public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
		return true;
	}

	@Override
	public boolean skipNonOpenerCloser() {
		return false;
	}
	
}
