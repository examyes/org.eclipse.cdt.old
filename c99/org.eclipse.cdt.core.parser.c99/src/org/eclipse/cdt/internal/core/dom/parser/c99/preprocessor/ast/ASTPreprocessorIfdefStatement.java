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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;

public class ASTPreprocessorIfdefStatement extends ASTPreprocessorConditionalBranch implements
		IASTPreprocessorIfdefStatement {

	public ASTPreprocessorIfdefStatement(boolean taken) {
		super(taken);
	}
}
