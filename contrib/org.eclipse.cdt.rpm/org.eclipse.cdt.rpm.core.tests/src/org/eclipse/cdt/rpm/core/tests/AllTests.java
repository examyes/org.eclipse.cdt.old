/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.cdt.rpm.core.tests;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite{

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.cdt.rpm.core.tests");
		//$JUnit-BEGIN$
		suite.addTest(RPMCoreTestSuite.suite());
		//$JUnit-END$
		return suite;
	}
}
