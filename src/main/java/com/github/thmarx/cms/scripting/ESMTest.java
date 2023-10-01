/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.scripting;

import java.io.IOException;
import org.graalvm.polyglot.*;

public class ESMTest {

    public static void main(String[] args) {
         System.out.println("Hello Java!");
         
         String src = "import {Foo} from 'C:\\\\Other\\\\foo.mjs';" +
                 "const foo = new Foo();" +
                 "console.log('Hello JavaScript!');" +
                 "console.log('Square of 42 is: ' + foo.square(42));";
         
         Engine engine = Engine.newBuilder()
                 .option("engine.WarnInterpreterOnly", "false")
                 .build();
         
         Context ctx = Context.newBuilder("js")
                 .engine(engine)
                 //.allowHostAccess(HostAccess.ALL) /*not needed for this example */
                 .allowHostClassLookup(className -> true)
                 .allowIO(true)
                 .build();
         
         try {
            Source source =  Source.newBuilder("js", src, "script.mjs").build();
            ctx.eval(source);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         
    }

}
