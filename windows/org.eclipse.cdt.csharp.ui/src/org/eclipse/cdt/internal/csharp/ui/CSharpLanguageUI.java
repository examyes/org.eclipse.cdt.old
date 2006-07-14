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

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ILanguageUI;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * @author Doug Schaefer
 *
 */
public class CSharpLanguageUI extends PlatformObject implements ILanguageUI {

	private static CSharpLanguageUI myDefault;
	
	private CSharpCodeScanner codeScanner;
	
	public CSharpLanguageUI() {
		codeScanner = new CSharpCodeScanner(
				CUIPlugin.getDefault().getTextTools().getColorManager(),
				CUIPlugin.getDefault().getPreferenceStore());
	}
	
	public static CSharpLanguageUI getDefault() {
		if (myDefault == null)
			myDefault = new CSharpLanguageUI();
		return myDefault;
	}
	
	public RuleBasedScanner getCodeScanner() {
		return getDefault().codeScanner;
	}
	
}
