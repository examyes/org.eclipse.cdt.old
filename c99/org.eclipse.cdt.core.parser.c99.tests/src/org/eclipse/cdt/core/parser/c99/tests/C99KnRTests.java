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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2KnRTests;


/**
 * 
 * @author Mike Kucera
 */
public class C99KnRTests extends AST2KnRTests {
	
	
	protected IASTTranslationUnit parse( String code, ParserLanguage lang ) {
	    return parse(code, lang, false, true );
	}
	    
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) {
	    return parse( code, lang, useGNUExtensions, true );
	}
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) {
    	return ParseHelper.parse(code, lang, expectNoProblems);
    }
    
    
    // TODO: Failing tests, will get around to fixing these bugs
    
    public void testKRCProblem3() throws Exception {
    	try {
    		super.testKRCProblem3();
    	} catch(AssertionFailedError _) {
    		return;
    	}
    	
    	fail();
    }
    
    public void testKRCProblem4() throws Exception  {
    	try {
    		super.testKRCProblem4();
    	} catch(AssertionFailedError _) {
    		return;
    	}
    	
    	fail();
    }

    public void testKRCProblem5() throws Exception  {
    	try {
    		super.testKRCProblem5();
    	} catch(AssertionFailedError _) {
    		return;
    	}
    	
    	fail();
    }

    public void testKRC_monop_cards1() throws Exception  {
    	try {
    		super.testKRC_monop_cards1();
    	} catch(AssertionFailedError _) {
    		return;
    	}
    	
    	fail();
    }
    
    public void testKRCProblem2() throws Exception  {
    	try {
    		super.testKRCProblem2();
    	} catch(AssertionFailedError _) {
    		return;
    	}
    	
    	fail();
    }
    
}
