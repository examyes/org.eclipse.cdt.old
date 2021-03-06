/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.c99.tests;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2Tests;
import org.eclipse.cdt.internal.core.parser.ParserException;


/**
 * 
 * @author Mike Kucera
 *
 */
public class C99Tests extends AST2Tests {

    public static TestSuite suite() {
    	return suite(C99Tests.class);
    }
    
	public C99Tests(String name) {
		super(name);
	}
	
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	if(lang != ParserLanguage.C)
    		return super.parse(code, lang, useGNUExtensions, expectNoProblems);
    	
    	return ParseHelper.parse(code, getLanguage(), expectNoProblems);
    }
    
    
    protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
    }
    
    
    public void testMultipleHashHash() throws Exception {
    	String code = "#define TWICE(a)  int a##tera; int a##ther; \n TWICE(pan)";
    	parseAndCheckBindings(code, ParserLanguage.C);
    }
    
    
    public void testBug191279() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append(" /**/ \n");
    	sb.append("# define YO 99 /**/ \n");
    	sb.append("# undef YO /**/ ");
    	sb.append(" /* $ */ ");
    	String code = sb.toString();
    	parseAndCheckBindings(code, ParserLanguage.C);
    }
    
    
    public void testBug191324() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("int x$y = 99; \n");
    	sb.append("int $q = 100; \n"); // can use $ as first character in identifier
    	sb.append("#ifndef SS$_INVFILFOROP \n");
    	sb.append("int z; \n");
    	sb.append("#endif \n");
    	String code = sb.toString();
    	parseAndCheckBindings(code, ParserLanguage.C);
    }
    
    public void testBug192009_implicitInt() throws Exception {
    	String code = "main() { int x; }";
    	IASTTranslationUnit tu = parse(code, ParserLanguage.C, false, true);
    	
    	IASTDeclaration[] declarations = tu.getDeclarations();
    	assertEquals(1, declarations.length);
    	
    	IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
    	ICASTSimpleDeclSpecifier declSpec = (ICASTSimpleDeclSpecifier) main.getDeclSpecifier();
    	assertEquals(0, declSpec.getType());
    	
    	
    	assertEquals("main", main.getDeclarator().getName().toString());
    }
    
	

	
	public void testBug93980() { // some wierd gcc extension I think
		try {
			super.testBug93980();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	}
	
	
	public void testBug95866() { // gcc extension
		try {
			super.testBug95866();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	}
	
	public void testBug192165() {
		try {
			super.testBug192165();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	}
	
	
	public void testBug80171() throws Exception {  // implicit int not supported
		try {
			super.testBug80171();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
    }
	
	
	public void testBug196468_emptyArrayInitializer() {  // empty array initializer is a gcc extension
		try {
			super.testBug196468_emptyArrayInitializer();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	}
}
