/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.doclava;

import com.google.doclava.parser.JavaLexer;
import com.google.doclava.parser.JavaParser;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.debug.ParseTreeBuilder;
import org.antlr.runtime.tree.ParseTree;

public class Doclava2 {
    public static void main(String args[]) throws Exception {
        InfoBuilder infoBuilder = new InfoBuilder();

        JavaLexer lex = new JavaLexer(new ANTLRFileStream(args[0], "UTF8"));
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // create the ParseTreeBuilder to build a parse tree
        // much easier to parse than ASTs
        ParseTreeBuilder builder = new ParseTreeBuilder("compilationUnit");
        JavaParser g = new JavaParser(tokens, builder);

        try {
             g.compilationUnit();
        } catch (RecognitionException e) {
            e.printStackTrace();
        }

        ParseTree tree = builder.getTree();
        lex = null;
        tokens = null;
        builder = null;
        g = null;

        infoBuilder.parseFile(tree);

        infoBuilder.printStuff();
    }
}