/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple object that stores macros.
 */
public class MacroEnvironment<TKN> {
	
	private Map<String, Macro<TKN>> env = new HashMap<String, Macro<TKN>>();
	
	public void addMacro(Macro<TKN> macro) {
		env.put(macro.getName(), macro);
	}
	
	public Macro<TKN> get(String macroId) {
		return env.get(macroId);
	}

	public boolean hasMacro(String macroId) {
		return env.containsKey(macroId);
	}

	public void removeMacro(String macroId) {
		env.remove(macroId);
	}
	
	public String toString() {
		return env.toString();
	}
}
