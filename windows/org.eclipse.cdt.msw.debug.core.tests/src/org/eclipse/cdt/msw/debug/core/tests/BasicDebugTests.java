package org.eclipse.cdt.msw.debug.core.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.msw.debug.dbgeng.DebugObjectFactory;
import org.junit.Test;


public class BasicDebugTests extends TestCase {
	
	@Test
	public void testClientCreate() throws Exception {
		DebugObjectFactory.createClient();
	}

}
