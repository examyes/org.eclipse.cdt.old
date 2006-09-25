package org.eclipse.cdt.windows.debug.tests;

import org.eclipse.cdt.windows.debug.core.IDebugClient;

import junit.framework.TestCase;

public class BasicTests extends TestCase {

	public void testDebugClient() throws Exception {
		IDebugClient debugClient = IDebugClient.debugCreate();
		assertNotNull(debugClient);
		assertTrue(debugClient.getP() != 0);
	}
	
}
