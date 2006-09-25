package org.eclipse.cdt.windows.debug.tests;

import org.eclipse.cdt.windows.debug.core.DebugCreateProcessOptions;
import org.eclipse.cdt.windows.debug.core.IDebugClient;
import org.eclipse.cdt.windows.debug.core.IDebugControl;

import junit.framework.TestCase;

public class BasicTests extends TestCase {

	public void testDebugClient() throws Exception {
		IDebugClient debugClient = IDebugClient.debugCreate();
		assertNotNull(debugClient);
		IDebugControl debugControl = IDebugControl.debugCreate();
		assertNotNull(debugControl);
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);
		debugClient.createProcess2(0, "C:\\cygwin\\bin\\ls", options, "C:\\cygwin", null);
		while (true)
			debugControl.waitForEvent(0, IDebugControl.INFINITE);
	}
	
}
