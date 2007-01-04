/**
 * 
 */
package org.eclipse.cdt.csharp.core.tests.parser;

import junit.framework.TestCase;

/**
 * @author DSchaefer
 *
 */
public class BaseParserTest extends TestCase {

	protected void parseString(String code) throws Exception {
//		CharStream input = new ANTLRStringStream(code);
//		CSharpLexer lexer = new CSharpLexer(input);
//		CommonTokenStream tokens = new CommonTokenStream(lexer);
//		CSharp parser = new CSharp(tokens);
//		parser.compilationUnit();
	}
	
	public void test1() throws Exception {
		String code =
			"using System;\n" +
			"class Hello {\n" +
			"    static void Main() {\n" +
			"        Console.WriteLine(\"hello, world\");\n" +
			"    }\n" +
			"}\n";
		parseString(code);
	}
	
	public void test2() throws Exception {
		String code =
		"class Hello {\n" +
		"    static void Main() {\n" +
		"        Hello ? blah;" +
		"    }\n" +
		"}\n";
		parseString(code);
	}
}
