package org.eclipse.cdt.windows.debug.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.windows.debug.core.DebugCreateProcessOptions;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugClient;
import org.eclipse.cdt.windows.debug.core.IDebugControl;
import org.eclipse.core.runtime.Path;

public class BasicTests extends TestCase {

	public void testDebugClient() throws Exception {
		IDebugClient debugClient = IDebugClient.create();
		IDebugControl debugControl = new IDebugControl();
		assertFalse(HRESULT.FAILED(debugClient.createControl(debugControl)));
		// Register callbacks
		assertFalse(HRESULT.FAILED(debugClient.setEventCallbacks(new TestEventCallbacks())));
		// Create Process
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);
		
		String dir = Activator.getDefault().getFileNameInPlugin(new Path("resources"));
		String app = dir + "/testApp.exe";
		assertNotNull(dir);
		assertFalse(HRESULT.FAILED(debugClient.createProcess2(0, app, options, dir, null)));
		// Event loop
		while (true) {
			int hr = debugControl.waitForEvent(0, IDebugControl.INFINITE);
			if (hr == HRESULT.E_UNEXPECTED)
				break;
			assertFalse(HRESULT.FAILED(hr));
		}
	}
	
}
