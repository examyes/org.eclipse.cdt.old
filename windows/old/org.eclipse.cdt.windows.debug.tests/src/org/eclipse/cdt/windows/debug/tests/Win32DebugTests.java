package org.eclipse.cdt.windows.debug.tests;

import org.eclipse.cdt.windows.debug.core.sdk.Win32Debug;

import junit.framework.TestCase;

public class Win32DebugTests extends TestCase {

	public void test1() {
		assertTrue(Win32Debug.WriteProcessMemory(0, 0, null, null));
	}
	
}
