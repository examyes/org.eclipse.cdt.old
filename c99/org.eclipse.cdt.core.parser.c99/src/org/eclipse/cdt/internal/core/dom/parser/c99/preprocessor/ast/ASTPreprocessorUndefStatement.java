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
package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.ast;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class ASTPreprocessorUndefStatement extends ASTNode implements
		IASTPreprocessorUndefStatement {

	private IASTName macroName;

	public IASTName getMacroName() {
		return macroName;
	}

	public void setMacroName(IASTName macroName) {
		this.macroName = macroName;
	}
}
