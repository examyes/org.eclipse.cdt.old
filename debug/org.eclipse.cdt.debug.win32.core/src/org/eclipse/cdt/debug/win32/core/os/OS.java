/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.win32.core.os;

/**
 * @author Doug Schaefer
 */
public class OS {

	public static native boolean GetThreadContext(long threadHandle, CONTEXT contextObj);

	public static native boolean SymInitialize(long processHandle,
											   String userSearchPath,
											   boolean invadeProcess);
	
	public static native boolean SymCleanup(long processHandle);

	public static native boolean StackWalk64(int machineType,
											 long processHandle,
											 long threadHandle,
											 STACKFRAME64 stackFrame,
											 CONTEXT context);

	public static native boolean SymFromAddr(long processHandle,
											 long address,
											 long [] displacement, // out value
											 SYMBOL_INFO symbol);

	public static native boolean SymGetLineFromAddr(long processHandle,
													long address,
													int [] displacement, // out value
													IMAGEHLP_LINE64 line);

}
