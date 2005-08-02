/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import org.eclipse.cdt.debug.win32.core.dbgeng.IDebugOutputCallbacks;

public class WinDbgOutputCallbacks extends IDebugOutputCallbacks {

	public int output(int mask, String text) {
		return 0;
	}

}
