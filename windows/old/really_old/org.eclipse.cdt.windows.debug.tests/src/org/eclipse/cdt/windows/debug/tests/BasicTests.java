package org.eclipse.cdt.windows.debug.tests;

import org.eclipse.cdt.windows.debug.core.DebugCreateProcessOptions;
import org.eclipse.cdt.windows.debug.core.IDebugClient;
import org.eclipse.cdt.windows.debug.core.IDebugControl;

import junit.framework.TestCase;

public class BasicTests extends TestCase {

	public void testDebugClient() throws Exception {
		IDebugClient debugClient = new IDebugClient();
		IDebugControl debugControl = new IDebugControl();
		// Register callbacks
		debugClient.setEventCallbacks(new TestEventCallbacks());
		// Create Process
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);
		debugClient.createProcess2(0, "C:\\cygwin\\bin\\ls", options, "C:\\cygwin", null);
		// Event loop
		while (true)
			debugControl.waitForEvent(0, IDebugControl.INFINITE);
	}
	
}
