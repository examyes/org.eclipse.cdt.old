/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.util.HashMap;
import java.util.Map;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.parser.c99.ITokenMap;


/**
 * Maps token kinds from new parsers back to the token kinds defined in C99Parsersym.
 * If this is not done then C99ParserAction will not behave properly.
 * 
 * @author Mike Kucera
 */
public class C99TokenMap implements ITokenMap {

	// LPG token kinds start at 0
	public static int INVALID_KIND = -1;
	
	private int[] kindMap = null; 
	private Map symbolMap = new HashMap();
	private String[] targetSymbols = null;
	
	
	/**
	 * @param toSymbols An array of symbols where the index is the token kind and the
	 * element data is a string representing the token kind. It is expected
	 * to pass the orderedTerminalSymbols field from an LPG generated symbol
	 * file, for example C99Parsersym.orderedTerminalSymbols.
	 */
	public C99TokenMap(String[] toSymbols) {
		targetSymbols = toSymbols;
		
		// If this map is not being used with an extension then it becomes an "identity map".
		if(toSymbols == C99Parsersym.orderedTerminalSymbols)
			return;
		
		kindMap = new int[toSymbols.length];
		
		for(int i = 0; i < C99Parsersym.orderedTerminalSymbols.length; i++) {
			symbolMap.put(C99Parsersym.orderedTerminalSymbols[i], new Integer(i));
		}
		
		for(int i = 0; i < toSymbols.length; i++) {
			Integer kind = (Integer)symbolMap.get(toSymbols[i]);
			kindMap[i] = kind == null ? INVALID_KIND : kind.intValue();
		}
	}
	
	
	public String[] getTargetSymbols() {
		return targetSymbols;
	}
	
	
	/**
	 * Maps a token kind back to the corresponding kind define in the base C99 parser.
	 */
	public int asC99Kind(int kind) {
		if(kindMap == null)
			return kind;
		
		if(kind < 0 || kind >= kindMap.length)
			return INVALID_KIND;
		
		return kindMap[kind];
	}
	
	public int asC99Kind(IToken token) {
		return asC99Kind(token.getKind());
	}
}