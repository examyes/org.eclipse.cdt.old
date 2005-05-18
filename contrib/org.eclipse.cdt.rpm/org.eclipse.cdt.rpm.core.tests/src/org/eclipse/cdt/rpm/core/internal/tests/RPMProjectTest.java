/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.cdt.rpm.core.internal.tests;

import java.io.File;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.rpm.core.IRPMConstants;
import org.eclipse.cdt.rpm.core.IRPMProject;
import org.eclipse.cdt.rpm.core.ISpecFile;
import org.eclipse.cdt.rpm.core.RPMCorePlugin;
import org.eclipse.cdt.rpm.core.RPMExportDelta;
import org.eclipse.cdt.rpm.core.RPMProjectFactory;
import org.eclipse.cdt.rpm.core.RPMProjectNature;
import org.eclipse.cdt.rpm.core.tests.RPMCoreTestsPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;

public class RPMProjectTest extends TestCase {

    IWorkspace workspace;
    IWorkspaceRoot root;
    NullProgressMonitor monitor;
    String pluginRoot;
	
    final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	private final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
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
        return new TestSuite(RPMProjectTest.class);
    }

    public void testImportHelloWorld() throws Exception {
        // Create a project for the test
        IProject testProject = root.getProject("testHelloWorld");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
		
		// Instantiate an RPMProject
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
        
        // Find the test SRPM and install it
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld-2-2.src.rpm"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld-2-2.src.rpm");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        rpmProject.importSourceRPM(foo);
        
        // Make sure the original SRPM got copied into the workspace
        IFile srpm = rpmProject.getConfiguration().getSrpmsFolder().getFile("helloworld-2-2.src.rpm");
        assertTrue(srpm.exists());
		assertNotNull(rpmProject.getProject().getPersistentProperty(new QualifiedName(RPMCorePlugin.ID, 
				IRPMConstants.SRPM_PROPERTY)));
        
        // Make sure everything got installed properly
        IFile spec = rpmProject.getConfiguration().getSpecsFolder().getFile("helloworld.spec");
        assertTrue(spec.exists());
        IFile sourceBall = rpmProject.getConfiguration().getSourcesFolder().getFile("helloworld-2.tar.bz2");
        assertTrue(sourceBall.exists());

        // Make sure we got the spec file
        ISpecFile specFile = rpmProject.getSpecFile();
        assertTrue(specFile != null);
		assertNotNull(rpmProject.getProject().getPersistentProperty(new QualifiedName(RPMCorePlugin.ID,
				IRPMConstants.SPEC_FILE_PROPERTY)));
        
		// Make sure the sources got copied from BUILD to the project root
		IResource[] sources = rpmProject.getConfiguration().getBuildFolder().members();
		// If there is one folder, assume it contains all the sources
		if(sources.length == 1 && sources[0].getType() == IResource.FOLDER) {
			IFolder foo1 = rpmProject.getProject().getFolder(sources[0].getProjectRelativePath());
			sources = foo1.members();
		}
		for(int i=0; i < sources.length; i++) {
			if(sources[i].getType() == IResource.FILE) {
				assertTrue(testProject.getFile(sources[i].getName()).exists());
			}
			else if(sources[i].getType() == IResource.FOLDER) {
				assertTrue(testProject.getFolder(sources[i].getName()).exists());
			}
		}
		
		// Make sure the checksum was stored
		assertNotNull(rpmProject.getProject().getPersistentProperty(new QualifiedName(RPMCorePlugin.ID,
				IRPMConstants.CHECKSUM_PROPERTY)));
		
		// Make sure the RPM nature was added
		assertTrue(rpmProject.getProject().hasNature(RPMProjectNature.RPM_NATURE_ID));
		
        // Clean up
        testProject.delete(true, false, monitor);
    }
    
    public void testBuildPrepHelloWorld() throws Exception {
        // Create a project for the test
        IProject testProject = root.getProject("testBuildPrepHelloWorld");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
		
		// Instantiate an RPMProject
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
        
        // Find the test SRPM, install, and build-prep it
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld-2-2.src.rpm"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld-2-2.src.rpm");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        rpmProject.importSourceRPM(foo);
        rpmProject.buildPrep();
        
        // Make sure we got everything in the build directory
        IFolder builddir = rpmProject.getConfiguration().getBuildFolder();
        IFolder helloworldFolder = builddir.getFolder("helloworld-2");
        assertTrue(helloworldFolder.exists());
        
        // Clean up
        testProject.delete(true, false, monitor);
    }
	
	public void testIsChangedHelloWorld() throws Exception {
		// Create a project for the test
        IProject testProject = root.getProject("testIsChangedHelloWorld");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
		
		// Instantiate an RPMProject
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
        
        // Find the test SRPM and install it
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld-2-2.src.rpm"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld-2-2.src.rpm");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        rpmProject.importSourceRPM(foo);
		assertFalse(rpmProject.isChanged());
		
		testProject.delete(true, false, null);
	}
	
	public void testIsChangedHelloWorld1() throws Exception {
		// Create a project for the test
        IProject testProject = root.getProject("testIsChangedHelloWorld1");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
		
		// Instantiate an RPMProject
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
        
        // Find the test SRPM and install it
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld-2-2.src.rpm"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld-2-2.src.rpm");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        rpmProject.importSourceRPM(foo);
		IFile sourceFile = rpmProject.getProject().getFile("helloworld.cpp");
		StringBufferInputStream foo1 = new StringBufferInputStream("/* */");
		sourceFile.appendContents(foo1, false, false, null);
		assertTrue(rpmProject.isChanged());
		
		testProject.delete(true, false, null);
	}
	
	public void testBuildSourceRPMHelloWorld() throws Exception {
		// Create a project for the test
        IProject testProject = root.getProject("testBuildSourceRPMHelloWorld1");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
		
		// Instantiate an RPMProject
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
        
        // Find the test SRPM and install it
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld-2-2.src.rpm"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld-2-2.src.rpm");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        rpmProject.importSourceRPM(foo);
		RPMExportDelta export = new RPMExportDelta();
		export.setSpecFile(rpmProject.getSpecFile().getFile());
		export.setVersion("2");
		export.setRelease("3");
		rpmProject.buildSourceRPM(export);
		
		IFile foo2 = rpmProject.getConfiguration().getSrpmsFolder().getFile("helloworld-2-3.src.rpm");
		assertTrue(foo2.exists());
		
		testProject.delete(true, false, null);
	}
	
	public void testBuildSourceRPMHelloWorld1() throws Exception {
		// Create a project for the test
        IProject testProject = root.getProject("testBuildSourceRPMHelloWorld");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
		
		// Instantiate an RPMProject
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
        
        // Find the test SRPM and install it
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld-2-2.src.rpm"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld-2-2.src.rpm");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        rpmProject.importSourceRPM(foo);
		IFile sourceFile = rpmProject.getProject().getFile("helloworld.cpp");
		StringBufferInputStream foo1 = new StringBufferInputStream("/* */");
		sourceFile.appendContents(foo1, false, false, null);
		RPMExportDelta export = new RPMExportDelta();
		export.setSpecFile(rpmProject.getSpecFile().getFile());
		export.setVersion("2");
		export.setRelease("4");
		export.setPatchName("myPatchFFFFF.patch");
		Date today = new Date();
		SimpleDateFormat df = new SimpleDateFormat("E MMM dd yyyy"); //$NON-NLS-1$
		export.setChangelogEntry("* " + df.format(today) + "  Foo Bot  <bot@foo.bar>  2-4" + line_sep + 
				"- Made test change" + line_sep);
		rpmProject.buildSourceRPM(export);
		
		// Make sure patch was created
		assertTrue(rpmProject.getConfiguration().getSourcesFolder().getFile(export.getPatchName()).exists());
		
		IFile foo2 = rpmProject.getConfiguration().getSrpmsFolder().getFile("helloworld-2-4.src.rpm");
		assertTrue(foo2.exists());
	}
}
