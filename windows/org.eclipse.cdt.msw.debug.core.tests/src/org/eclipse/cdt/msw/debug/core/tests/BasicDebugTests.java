package org.eclipse.cdt.msw.debug.core.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.msw.debug.dbgeng.DebugCreateProcessOptions;
import org.eclipse.cdt.msw.debug.dbgeng.DebugEvent;
import org.eclipse.cdt.msw.debug.dbgeng.DebugObjectFactory;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStatus;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugClient;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugControl;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugEventCallbacks;
import org.junit.Test;


public class BasicDebugTests extends TestCase {
	
	@Test
	public void testClientCreate() throws Exception {
		IDebugClient client = DebugObjectFactory.createClient();
		client.createProcess(0, "notepad", DebugCreateProcessOptions.DEBUG_PROCESS);
		client.setEventCallbacks(new IDebugEventCallbacks() {
			@Override
			protected int getInterestMask() {
				return DebugEvent.CREATE_PROCESS;
			}
		});
		
		IDebugControl control = DebugObjectFactory.createControl();
//		control.setExecutionStatus(DebugStatus.GO);
		while (true) {
			try {
				control.waitForEvent(0, IDebugControl.INFINITE);
			} catch (HRESULTException e) {
				if (e.getHRESULT() == HRESULTException.E_UNEXPECTED)
					break;
				else
					throw e;
			}
			control.setExecutionStatus(DebugStatus.GO);
		}
	}

}
