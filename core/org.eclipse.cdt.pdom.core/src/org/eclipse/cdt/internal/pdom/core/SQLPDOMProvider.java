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

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMProvider;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMProvider implements IPDOMProvider {

	private static final QualifiedName pdomProperty
		= new QualifiedName(PDOMCorePlugin.ID, "sqlpdom"); //$NON-NLS-1$
	
	public IPDOM getPDOM(IProject project) {
		try {
			SQLPDOM pdom = (SQLPDOM)project.getSessionProperty(pdomProperty);
			
			if (pdom == null) {
				pdom = new SQLPDOM(project);
				project.setSessionProperty(pdomProperty, pdom);
			}
			
			return pdom;
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
			return null;
		}
	}

}
