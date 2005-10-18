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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMBinding;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMCPPClassType extends SQLPDOMBinding implements
		ICPPClassType, ICPPClassScope {

	public SQLPDOMCPPClassType(SQLPDOM pdom, IASTName name, ICPPClassType type)
			throws CoreException {
		super(pdom, name, type);
		// TODO - oooh, very sneaky...
		name.setBinding(this);
	}

	public SQLPDOMCPPClassType(int id, int scopeId, int nameId, char[] name) {
		super(id, scopeId, nameId, name);
	}

	public int getBindingType() {
		return B_CPPCLASSTYPE;
	}
	
    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }
    
	public ICPPBase[] getBases() throws DOMException {
		return new ICPPBase[0]; // TODO we should do this sometime
	}

	public IField[] getFields() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public IField findField(String name) throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public ICPPMethod[] getMethods() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public IBinding[] getFriends() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public int getKey() throws DOMException {
		return k_class; // TODO record this for real
	}

	public IScope getCompositeScope() throws DOMException {
		return this;
	}

	public boolean isSameType(IType type) {
		throw new SQLPDOMNotImplementedError();
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

	public ICPPClassType getClassType() {
		return this;
	}

	public ICPPMethod[] getImplicitMethods() {
		throw new SQLPDOMNotImplementedError();
	}

	public void addBinding(IBinding binding) throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public void addName(IASTName name) throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public IBinding[] find(String name) throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public void flushCache() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		SQLPDOM pdom = (SQLPDOM)name.getTranslationUnit().getPDOM();
		return pdom.getBinding(name, getId());
	}

	public IScope getParent() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTNode getPhysicalNode() throws DOMException {
		return null; // TODO there is no physical node
	}

	public IASTName getScopeName() throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isFullyCached() throws DOMException {
		return true;
	}

	public void removeBinding(IBinding binding) throws DOMException {
		throw new SQLPDOMNotImplementedError();
	}

	public void setFullyCached(boolean b) throws DOMException {
	}

}
