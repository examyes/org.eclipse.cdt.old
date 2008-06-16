package org.eclipse.cdt.msw.debug.core.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.msw.debug.dbgeng.DebugCreateProcessOptions;
import org.eclipse.cdt.msw.debug.dbgeng.DebugObjectFactory;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugClient;
import org.junit.Test;


public class BasicDebugTests extends TestCase {
	
	@Test
	public void testClientCreate() throws Exception {
		IDebugClient client = DebugObjectFactory.createClient();
		client.createProcess(0, "notepad", DebugCreateProcessOptions.DEBUG_PROCESS);
	}

}
