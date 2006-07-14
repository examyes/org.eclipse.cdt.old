/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.internal.csharp.ui;

import org.eclipse.cdt.csharp.core.CSharpLanguage;
import org.eclipse.cdt.ui.ILanguageUI;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * @author Doug Schaefer
 *
 */
public class CSharpLanguageUIAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof CSharpLanguage
				&& adapterType == ILanguageUI.class)
			return CSharpLanguageUI.getDefault();
		else
			return null;
	}

	private static Class[] adapterList = new Class[] {
		ILanguageUI.class
	};
	
	public Class[] getAdapterList() {
		return adapterList;
	}

}
