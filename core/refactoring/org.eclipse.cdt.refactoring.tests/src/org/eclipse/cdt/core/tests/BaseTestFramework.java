/*******************************************************************************
 * Copyright (c) 2004-2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     IBM Corporation - initial implementation
 *     Wind River Systems - adapting to the needs of the refactoring tests
 ******************************************************************************/ 

/*
 * Created on Oct 4, 2004
 */
package org.eclipse.cdt.core.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.refactoring.tests.TestsPlugin;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * @author aniefer
 */
abstract public class BaseTestFramework extends TestCase {
    static protected NullProgressMonitor	monitor;
    static protected IWorkspace 			workspace;
    static protected IProject 				project;
    static protected ICProject				cproject;
    
    {
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
			(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
			monitor = new NullProgressMonitor();
			
			workspace = ResourcesPlugin.getWorkspace();
			
	        try {
	            cproject = createCCProject("RegressionTestProject", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
	        
	            project = cproject.getProject();
	            project.setSessionProperty(IndexManager.activationKey, Boolean.FALSE );
	        } catch ( CoreException e ) {
                e.printStackTrace();
	            /*boo*/
	        }
			if (project == null)
				fail("Unable to create project"); //$NON-NLS-1$
	
        }
	}
    
    public void enableIndexing(){
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
            if( project != null )
                try {
                    project.setSessionProperty( IndexManager.activationKey, Boolean.TRUE );
                } catch ( CoreException e ) { //boo
                }
        }
    }
    
    public void disableIndexing(){
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
            if( project != null )
                try {
                    project.setSessionProperty( IndexManager.activationKey, Boolean.FALSE );
                } catch ( CoreException e ) { //boo
                }
        }
    }
    
    public BaseTestFramework()
    {
        super();
    }
    /**
     * @param name
     */
    public BaseTestFramework(String name)
    {
        super(name);
    }
      
    public void cleanupProject() throws Exception {
        try{
	        project.delete( true, false, monitor );
	        project = null;
	    } catch( Throwable e ){
	        /*boo*/
	    }
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
        IResource [] members = project.members();
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cdtproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            try{
                members[i].delete( true, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
  	}
    
    protected IFile importFile(String fileName, String contents ) throws Exception{
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);
		InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
		//Create file input stream
        if( file.exists() )
		    file.setContents( stream, false, false, monitor );
		else
            file.create( stream, true, monitor );
		
		return file;
	}
    
    public ICProject createCCProject(final String projectName, final String binFolderName) throws CoreException {
        return createProject(projectName, binFolderName, true);
    }

    public ICProject createCProject(final String projectName, 
            String binFolderName) throws CoreException {
        return createProject(projectName, binFolderName, false);
    }
    
    private ICProject createProject(final String projectName, String binFolderName, 
            final boolean cc) throws CoreException {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final IProject newProject[] = new IProject[1];
        ws.run(new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                IWorkspaceRoot root = ws.getRoot();
                IProject project = root.getProject(projectName);
                if (!project.exists()) {
                    project.create(null);
                } else {
                    project.refreshLocal(IResource.DEPTH_INFINITE, null);
                }
                if (!project.isOpen()) {
                    project.open(null);
                }
                if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
                    CProjectNature.addCNature(project, null);
                    String projectId = TestsPlugin.getDefault().getBundle().getSymbolicName() + ".TestProject"; //$NON-NLS-1$
                    CCorePlugin.getDefault().mapCProjectOwner(project, projectId, false);
                }
                if (cc && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
                    CCProjectNature.addCCNature(project, null);
                }
                newProject[0] = project;
            }
        }, null);

        return CCorePlugin.getDefault().getCoreModel().create(newProject[0]);
    }
}
