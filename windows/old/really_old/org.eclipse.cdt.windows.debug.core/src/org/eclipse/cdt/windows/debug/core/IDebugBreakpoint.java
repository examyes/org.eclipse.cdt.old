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

package org.eclipse.cdt.windows.debug.core;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class IDebugBreakpoint {

	@SuppressWarnings("unused")
	private final long p;
	
	// This is only constructed by native code
	@SuppressWarnings("unused")
	private IDebugBreakpoint(long p) throws CoreException {
		this.p = p;
	}
	
	public native int getId(int[] id);
	
	public native int setOffsetExpression(String expression);

	public native int getOffsetExpression(String[] expression);
	
	public static final int DEBUG_BREAKPOINT_GO_ONLY =		0x00000001;
	public static final int DEBUG_BREAKPOINT_DEFERRED =		0x00000002;
	public static final int DEBUG_BREAKPOINT_ENABLED =		0x00000004;
	public static final int DEBUG_BREAKPOINT_ADDER_ONLY =	0x00000008;
	public static final int DEBUG_BREAKPOINT_ONE_SHOT =		0x00000010;
	
	public native int addFlags(int flags);
	
}
