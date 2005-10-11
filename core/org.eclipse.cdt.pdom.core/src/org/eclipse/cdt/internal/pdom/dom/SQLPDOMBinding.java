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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author Doug Schaefer
 */
public class SQLPDOMBinding implements IBinding {

	protected int id;
	
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
