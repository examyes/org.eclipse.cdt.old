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
package org.eclipse.cdt.internal.pdom.dom;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public abstract class SQLPDOMBinding implements IBinding {

	protected int id;
	protected int nameId;
	
	public static final int B_CVARIABLE = 1;
	public abstract int getBindingType(); 
	
	public static SQLPDOMBinding create(SQLPDOM pdom, IASTName name) throws CoreException {
		SQLPDOMBinding pdomBinding;
		
		IBinding binding = name.resolveBinding();
		if (binding instanceof IProblemBinding) {
			// Problem, no binding for you
			return null;
		} else if (binding instanceof SQLPDOMBinding) {
			// Ah, the binding is already in the PDOM, we can simply return it
			return (SQLPDOMBinding)binding;
		} else {
			// It's a DOM binding, need to create the PDOM version of it
			if (binding instanceof IVariable) {
				if (binding instanceof ICPPVariable) {
					pdomBinding = null; // TODO 
				} else {
					pdomBinding = new SQLPDOMCVariable(pdom, (IVariable)binding);
				}
			} else {
				pdomBinding = null; // TODO 
			}
		}

		pdomBinding.setId(pdom);
		return pdomBinding;
	}
	
	private void setId(SQLPDOM pdom) throws CoreException {
		id = pdom.getBindingId(nameId, getBindingType());
	}

	public int getNameId() {
		return nameId;
	}
	
	public String getName() {
		throw new SQLPDOMNotImplementedError();
	}

	public char[] getNameCharArray() {
		throw new SQLPDOMNotImplementedError();
	}

	public IScope getScope() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public int getId() {
		return id;
	}
	
}
