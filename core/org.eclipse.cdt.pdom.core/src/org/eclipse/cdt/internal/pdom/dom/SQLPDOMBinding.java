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
import org.eclipse.cdt.internal.pdom.dom.c.SQLPDOMCVariable;
import org.eclipse.cdt.internal.pdom.dom.cpp.SQLPDOMCPPVariable;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public class SQLPDOMBinding implements IBinding {

	private int id;
	private int nameId;
	private char[] name;
	
	public static final int B_UNKNOWN = 0;
	public static final int B_CVARIABLE = 1;
	public static final int B_CPPVARIABLE = 2;
	
	public int getBindingType() {
		return B_UNKNOWN; 
	}
	
	public static SQLPDOMBinding create(SQLPDOM pdom, IASTName name) throws CoreException {
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
					return new SQLPDOMCPPVariable(pdom, name, (ICPPVariable)binding);
				} else {
					return new SQLPDOMCVariable(pdom, name, (IVariable)binding);
				}
			} else {
				return new SQLPDOMBinding(pdom, name); // TODO 
			}
		}
	}
	
	public static SQLPDOMBinding create(int id, int type, int nameId, char[] name) {
		switch (type) {
		case B_CVARIABLE:
			return new SQLPDOMCVariable(id, nameId, name);
		case B_CPPVARIABLE:
			return new SQLPDOMCPPVariable(id, nameId, name);
		default:
			return new SQLPDOMBinding(id, nameId, name);
		}
	}
	
	public SQLPDOMBinding(SQLPDOM pdom, IASTName name) throws CoreException {
		nameId = pdom.getStringId(new String(name.toCharArray()), true);
		this.name = name.toCharArray(); 
		pdom.addBinding(this);
	}
	
	public SQLPDOMBinding(int id, int nameId, char[] name) {
		this.id = id;
		this.nameId = nameId;
		this.name = name;
	}
	
	public int getNameId() {
		return nameId;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return new String(name);
	}

	public char[] getNameCharArray() {
		return name;
	}

	public IScope getScope() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public int getId() {
		return id;
	}
	
}
