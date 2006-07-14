/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software System - cut & pasted CPPCodeScanner
 *******************************************************************************/
package org.eclipse.cdt.internal.csharp.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.text.AbstractCScanner;
import org.eclipse.cdt.internal.ui.text.CBraceRule;
import org.eclipse.cdt.internal.ui.text.COperatorRule;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.text.NumberRule;
import org.eclipse.cdt.internal.ui.text.PreprocessorRule;
import org.eclipse.cdt.internal.ui.text.util.CWordDetector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * C# code scanner.
 * 
 * @author Doug Schaefer
 */
public final class CSharpCodeScanner extends AbstractCScanner {

	private static String[] constants = { 
		"false",
		"null",
		"this",
		"true"
	};
	
	private static String[] keywords = {
		"abstract",
		"as",
		"base",
		"break",
		"case",
		"catch",
		"checked",
		"class",
		"const",
		"continue",
		"default",
		"delegate",
		"do",
		"else",
		"enum",
		"event",
		"explicit",
		"extern",
		"finally",
		"fixed",
		"for",
		"foreach",
		"goto",
		"if",
		"implicit",
		"in",
		"interface",
		"internal",
		"is",
		"lock",
		"namespace",
		"new",
		"operator",
		"out",
		"override",
		"params",
		"private",
		"protected",
		"public",
		"readonly",
		"ref",
		"return",
		"sealed",
		"short",
		"sizeof",
		"stackalloc",
		"static",
		"struct",
		"switch",
		"throw",
		"try",
		"typeof",
		"unchecked",
		"unsafe",
		"using",
		"virtual",
		"volatile",
		"while"
	};

	private static String[] types = {
		"bool",
		"byte",
		"char",
		"decimal",
		"double",
		"float",
		"int",
		"long",
		"object",
		"sbyte",
		"string",
		"uint",
		"ulong",
		"ushort",
		"void"
	};
	
	private static String[] ppDirectives = {
		"define",
		"error",
		"if",
		"elif",
		"else",
		"endif",
		"endregion",
		"line",
		"pragma",
		"region",
		"undef",
		"warning"
	};
	
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
        ICColorConstants.C_OPERATOR,
        ICColorConstants.C_BRACES,
        ICColorConstants.C_NUMBER,
		ICColorConstants.C_DEFAULT
	};
	
	public CSharpCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}
	
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	protected List createRules() {
				
		List<IRule> rules = new ArrayList<IRule>();		
		Token token;
		
		// Add word rule for keywords, types, and constants.
		token = getToken(ICColorConstants.C_DEFAULT);
		WordRule wordRule= new WordRule(new CWordDetector(), token);
		
		token = getToken(ICColorConstants.C_KEYWORD);
		for (int i = 0; i < keywords.length; ++i)
			wordRule.addWord(keywords[i], token);
		
		token = getToken(ICColorConstants.C_TYPE);
		for (int i = 0; i < types.length; ++i)
			wordRule.addWord(types[i], token);
		
		for (int i = 0; i < constants.length; i++)
			wordRule.addWord(constants[i], token);
		
		rules.add(wordRule);

		token = getToken(ICColorConstants.C_TYPE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), token);
		for (int i = 0; i < ppDirectives.length; ++i)
			preprocessorRule.addWord(ppDirectives[i], token);
		
		rules.add(preprocessorRule);

        token = getToken(ICColorConstants.C_NUMBER);
        NumberRule numberRule = new NumberRule(token);
        rules.add(numberRule);
        
        token = getToken(ICColorConstants.C_OPERATOR);
        COperatorRule opRule = new COperatorRule(token);
        rules.add(opRule);

        token = getToken(ICColorConstants.C_BRACES);
        CBraceRule braceRule = new CBraceRule(token);
        rules.add(braceRule);
        
		setDefaultReturnToken(getToken(ICColorConstants.C_DEFAULT));
		return rules;
	}

	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}
	
}
