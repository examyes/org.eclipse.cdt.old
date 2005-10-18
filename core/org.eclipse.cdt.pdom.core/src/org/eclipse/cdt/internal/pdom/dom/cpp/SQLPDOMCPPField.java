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
package org.eclipse.cdt.internal.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMBinding;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMCPPField extends SQLPDOMBinding implements ICPPField {

	public SQLPDOMCPPField(SQLPDOM pdom, IASTName name, ICPPField field) throws CoreException {
		super(pdom, name, field);
	}

	public SQLPDOMCPPField(int id, int scopeId, int nameId, char[] name) {
		super(id, scopeId, nameId, name);
	}

	public int getBindingType() {
		return B_CPPFIELD;
	}
	
	public IType getType() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isStatic() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isExtern() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isAuto() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isRegister() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public int getVisibility() throws DOMException {
		return v_public; // TODO for real
	}

	public String[] getQualifiedName() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isMutable() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

}
