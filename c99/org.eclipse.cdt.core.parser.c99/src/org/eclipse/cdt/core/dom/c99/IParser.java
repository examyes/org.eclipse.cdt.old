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
package org.eclipse.cdt.core.dom.c99;


/**
 * Represents a parser that can be used by C99Language.
 * 
 * @author Mike Kucera
 */
public interface IParser extends IPreprocessorTokenCollector {
	
	public IParseResult parse();
}
