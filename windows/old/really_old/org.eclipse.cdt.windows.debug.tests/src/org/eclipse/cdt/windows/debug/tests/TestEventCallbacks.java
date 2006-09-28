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

package org.eclipse.cdt.windows.debug.tests;

import org.eclipse.cdt.windows.debug.core.HRESULTFailure;
import org.eclipse.cdt.windows.debug.core.IDebugEventCallbacks;

/**
 * @author Doug Schaefer
 *
 */
public class TestEventCallbacks extends IDebugEventCallbacks {

	@Override
	protected int getInterestMask() throws HRESULTFailure {
		return DEBUG_EVENT_CREATE_PROCESS
			 | DEBUG_EVENT_EXIT_PROCESS
			 | DEBUG_EVENT_CREATE_THREAD
			 | DEBUG_EVENT_EXIT_THREAD;
	}

	@Override
	protected int createProcess(long imageFileHandle, long handle, long baseOffset, int moduleSize, String moduleName, String imageName, int checkSum, int timeDateStamp, long initialThreadHandle, long threadDataOffset, long startOffset) {
		return DEBUG_STATUS_NO_CHANGE;
	}
	
	@Override
	protected int exitProcess(int exitCode) {
		return DEBUG_STATUS_NO_CHANGE;
	}
	
	@Override
	protected int createThread(long handle, long dataOffset, long startOffset) {
		return DEBUG_STATUS_NO_CHANGE;
	}

	@Override
	protected int exitThread(int exitCode) {
		return DEBUG_STATUS_NO_CHANGE;
	}
	
}
