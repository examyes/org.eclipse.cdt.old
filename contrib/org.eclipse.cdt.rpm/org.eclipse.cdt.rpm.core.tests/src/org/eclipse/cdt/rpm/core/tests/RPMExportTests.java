/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.cdt.rpm.core.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.rpm.core.RPMExport;
import org.eclipse.cdt.rpm.core.SRPMImport;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Jeremy Handcock <handcock@redhat.com>
 * 
 * This class tests the RPM plug-in's core SRPM import operation
 */
public class RPMExportTests extends TestCase {
	IWorkspace workspace;
	IWorkspaceRoot root;
	NullProgressMonitor monitor;
	String pluginRoot;
	static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	
	/**
	 * Constructor for RPMExportTests.
	 * @param name
	 */
	public RPMExportTests(String name) {
		super(name);
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		IWorkspaceDescription desc;
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if(workspace == null) {
			fail("Workspace was not setup");
		}
		if(root == null) {
			fail("Workspace root was not setup");
		}
		desc = workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);
	}
	
	public static TestSuite suite() {
		return new TestSuite(RPMExportTests.class);
	}
	
	public void testHelloWorldRPMExport() throws CoreException, IOException {
		/* Create C project to use during the test */
		RPMExportTester("helloworld", "2", "2");
	}
	
	private void RPMExportTester(String name, String version, String release) 
		throws CoreException, IOException {
		
		/* Create C project to use during the test */
		ICProject testProject = CProjectHelper.createCProject(name, "none"); //$NON-NLS-1$ //$NON-NLS-2$
		if(testProject == null) {
			fail("Unable to create project");
		}
		
		URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + 
				file_sep + name+"-"+version+"-"+release+".src.rpm"));
		if (url == null)
			fail("Unable to find resources" + file_sep + "srpms" + file_sep +
				name+"-"+version+"-"+release+".src.rpm");
		
		SRPMImport SRPMImport = new SRPMImport(testProject.getProject().getLocation().
				toOSString(), Platform.asLocalURL(url).getPath());
		SRPMImport.setDoAutoconf(true);
		SRPMImport.setDoPatches(true);
		SRPMImport.run();
		
		/* Export the SRPM */
		RPMExport rpmExport = new RPMExport(testProject.getProject().getLocation().toOSString());
		// set the various properties for an RPM export
		
		rpmExport.setUi_ver_no(version);
		rpmExport.setUi_rel_no(release);
		rpmExport.setUi_spec_file("");
		
		rpmExport.run();
		
		File f = new File(testProject.getProject().getLocation().toOSString());
		FilenameFilter rpms = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".rpm") && !name.endsWith("src.rpm"));
			}
		};
			
		String[] files = f.list(rpms);
		assertTrue("source rpms were not generated", files != null); //$NON-NLS-1$
		
		
		/* Clean up */
		testProject.getProject().delete(true, true, monitor);
	}
	
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
}
