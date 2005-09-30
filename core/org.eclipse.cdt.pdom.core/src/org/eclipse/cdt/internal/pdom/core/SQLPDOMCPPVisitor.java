/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.core;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMCPPVisitor extends CPPASTVisitor {

	private final SQLPDOM pdom;
	
	public SQLPDOMCPPVisitor(SQLPDOM pdom) {
		super();
		this.pdom = pdom;
		shouldVisitNames = true;
	}

	public int visit(IASTName name) {
		IBinding binding = name.resolveBinding();
		if (binding instanceof IVariable) {
			pdom.addVariable(name, (IVariable)binding);
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

}
