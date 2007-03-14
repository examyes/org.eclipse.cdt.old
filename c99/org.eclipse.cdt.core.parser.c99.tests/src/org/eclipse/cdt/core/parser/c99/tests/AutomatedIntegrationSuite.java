/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.c99.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite extends TestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		
		
		suite.addTestSuite(C99Tests.class); // has some tests that do fail
		suite.addTestSuite(C99PreprocessorTests.class); // should all pass
		
		suite.addTestSuite(C99SpecTests.class); // a couple of failuers
		suite.addTestSuite(C99KnRTests.class); // mostly fail due to ambiguities
		
		// nowhere near working
		//suite.addTestSuite(C99SelectionParseTest.class);
		
		suite.addTestSuite(C99DOMLocationTests.class);
		suite.addTestSuite(C99DOMLocationMacroTests.class);
		suite.addTestSuite(C99DOMLocationInclusionTests.class);
		
		
		//suite.addTest(new FakeTestCaseToPrint());
		
		return suite;
	
	}
	
	private static class FakeTestCaseToPrint extends TestCase {
		public void tearDown() throws Exception {
			super.tearDown();
			System.out.println("Number of test run on C99 parser: " + ParseHelper.testsRun);
		}
		public void testIt() {}
	}
	
}
