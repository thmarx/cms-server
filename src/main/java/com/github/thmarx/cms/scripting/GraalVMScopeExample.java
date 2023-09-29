/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.scripting;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Language;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 *
 * @author t.marx
 */
public class GraalVMScopeExample {
	public static void main(String[] args) {
		
		Engine engine = Engine.create("js");
		
		long before = System.currentTimeMillis();
        try (Context context = Context.newBuilder().engine(engine).build()) {
            // Erstellen eines neuen Scope
            context.eval("js", "console.log('Hallo leute!'); const a = 'hello';");
        }
		long after = System.currentTimeMillis();
		System.out.println( (after-before) + "ms");
		
		before = System.currentTimeMillis();
        try (Context context = Context.newBuilder().engine(engine).build()) {
            // Erstellen eines neuen Scope
            context.eval("js", "console.log('Hallo leute!', (typeof a !== 'undefined' ? 'ja' : 'nein'));");
        }
		after = System.currentTimeMillis();
		System.out.println( (after-before) + "ms");
    }
}
