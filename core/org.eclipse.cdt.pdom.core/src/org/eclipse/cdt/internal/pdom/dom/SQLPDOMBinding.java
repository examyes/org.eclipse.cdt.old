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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;
import org.eclipse.cdt.internal.pdom.dom.c.SQLPDOMCVariable;
import org.eclipse.cdt.internal.pdom.dom.cpp.SQLPDOMCPPClassType;
import org.eclipse.cdt.internal.pdom.dom.cpp.SQLPDOMCPPField;
import org.eclipse.cdt.internal.pdom.dom.cpp.SQLPDOMCPPVariable;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public class SQLPDOMBinding implements IBinding {

	private int id;
	private int scopeId;
	private int nameId;
	private char[] name;
	
	public static final int B_UNKNOWN = 0;
	public static final int B_CVARIABLE = 1;
	public static final int B_CPPVARIABLE = 2;
	public static final int B_CPPCLASSTYPE = 3;
	public static final int B_CPPFIELD = 4;
	
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
				// The order here is important since these all extend eachother
				if (binding instanceof ICPPField) {
					return new SQLPDOMCPPField(pdom, name, (ICPPField)binding);
				} else if (binding instanceof ICPPVariable) {
					return new SQLPDOMCPPVariable(pdom, name, (ICPPVariable)binding);
				} else {
					return new SQLPDOMCVariable(pdom, name, (IVariable)binding);
				}
			} else if (binding instanceof ICPPClassType) {
				return new SQLPDOMCPPClassType(pdom, name, (ICPPClassType)binding);
			} else {
				return new SQLPDOMBinding(pdom, name, binding); 
			}
		}
	}
	
	public static SQLPDOMBinding create(int id, int scopeId, int type, int nameId, char[] name) {
		switch (type) {
		case B_CVARIABLE:
			return new SQLPDOMCVariable(id, scopeId, nameId, name);
		case B_CPPVARIABLE:
			return new SQLPDOMCPPVariable(id, scopeId, nameId, name);
		case B_CPPCLASSTYPE:
			return new SQLPDOMCPPClassType(id, scopeId, nameId, name);
		case B_CPPFIELD:
			return new SQLPDOMCPPField(id, scopeId, nameId, name);
		default:
			return new SQLPDOMBinding(id, scopeId, nameId, name);
		}
	}
	
	public SQLPDOMBinding(SQLPDOM pdom, IASTName name, IBinding binding) throws CoreException {
		nameId = pdom.getStringId(new String(name.toCharArray()), true);
		this.name = name.toCharArray();
		
		try {
			IScope scope = binding.getScope();
			IASTName scopeName = scope.getScopeName();
			if (scopeName != null) {
				IBinding scopeBinding = scope.getScopeName().getBinding();
				if (scopeBinding instanceof SQLPDOMBinding)
					scopeId = ((SQLPDOMBinding)scopeBinding).getId();
			}
		} catch (DOMException e) {
		}

		pdom.addBinding(this);
	}
	
	public SQLPDOMBinding(int id, int scopeId, int nameId, char[] name) {
		this.id = id;
		this.scopeId = scopeId;
		this.nameId = nameId;
		this.name = name;
	}
	
	public int getNameId() {
		return nameId;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getScopeId() {
		return scopeId;
	}
	
	protected void setScopeId(int scopeId) {
		this.scopeId = scopeId;
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
