package com.ibm.debug;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/WorkspaceSourceLocator.java, eclipse, eclipse-dev, 20011129
// Version 1.16 (last modified 11/29/01 14:15:48)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLStackFrame;
import com.ibm.debug.model.ViewInformation;

/**
 * Standard source code locator for source files.
 * <p>
 * This class may be instantiated.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 *
 * @see org.eclipse.debug.core.model.ISourceLocator
 */
public class WorkspaceSourceLocator implements ISourceLocator {

	private IWorkspace fWorkspace = null;
	// Project debug session associated with, if any.  This will be the first place
	// any source files are looked for.
	private IProject   homeProject = null;
	// Path(s) to search outside of the workspace.  This string will be passed to the
	// back-end as part of initialize.
	private String     searchPath = null;
	private String     lastFileName = null; // File name from last search
	private IFile      lastFile = null;     // File found for last search

	/**
	 * Creates a new source locator for the given workspace.
	 *
	 * @param the workspace
	 */
	public WorkspaceSourceLocator(IWorkspace ws) {
		fWorkspace= ws;
	}

	public WorkspaceSourceLocator() {
		fWorkspace= ResourcesPlugin.getWorkspace();
	}

	/**
	 * The <code>WorkspaceSourceLocator</code> implementation of this
	 * <code>ISourceLocator</code> method returns the first <code>IFile</code>
	 * that can be found using the file name supplied by the stack frame.
	 * If no file can be found in the workspace, then the engine is requested to
	 * supply the view, which is returned as an EngineSuppliedViewEditorInput.
	 *
	 * see org.eclipse.debug.core.model.ISourceLocator.
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		Object sourceElement = null;
		if (!(stackFrame instanceof PICLStackFrame))
			return null;

		PICLStackFrame frame= (PICLStackFrame)stackFrame;
		if (frame != null) {
			ViewInformation viewInfo = frame.getViewInformation();

			if (viewInfo == null) {
				//System.out.println("WorkspaceSourceLocator - viewInfo is null.  This is an error ");
				return null;
			}

			if (viewInfo.isSourceView()) {
				IPath path = frame.getSourceFile();
				if (path != null) {
					String filename = path.toOSString();
					IFile sourceFile = findFile(filename);
					if (sourceFile != null)
						return sourceFile;
					else
						// get source view from engine
						sourceElement = frame.getEngineSuppliedViewEI();
				}
			} else if (viewInfo.isDisassemblyView() ||
					   viewInfo.isMixedView()       ||
					   viewInfo.isListingView()) {
				sourceElement = frame.getEngineSuppliedViewEI();
			}
		}

		if (sourceElement == null) {
			// Engine could not generate the requested view,
			// try falling back to disassembly view
			PICLDebugTarget pdt = (PICLDebugTarget) frame.getDebugTarget();
			ViewInformation viewInfo = pdt.getDebugEngine().getDisassemblyViewInformation();
			frame.setViewInformation(viewInfo, false);
			sourceElement = frame.getEngineSuppliedViewEI();
		}

		// What should we do if sourceElement is still empty??
		// Maybe generate a dummy storage with "Source not available"
		return sourceElement;
	}

	/**
	 * Tell this <code>WorkspaceSourceLocator</code> which <code>IProject</code>
	 * the debug session was launched from.  This is the first location source
	 * files are searched for.
	 *
	  */
	public void setHomeProject(IProject project)
	{
		homeProject = project;
	}

	/**
	 * Gets the homeProject
	 * @return Returns a IProject
	 */
	public IProject getHomeProject() {
		return homeProject;
	}

	/**
	 * Search the entire workspace for a file.
	 *
	 */
	public IFile findFile(IWorkspaceRoot root, String fileName) {
		//System.out.println("WorkspaceSourceLocator::findFile 1 - looking for " + fileName);

		IProject projects[] = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IFile result = findFile(projects[i], fileName);
			if (result != null) {
				//System.out.println("WorkspaceSourceLocator::findFile 1 - match found" );
				return result;
			}
		}

		return null;
	}

	/**
	 * Search for a file in a particular project.
	 *
	 */
	public IFile findFile(IContainer root, String fileName) {
		if (root == null)
			return null;
		try {
			IResource resources[] = root.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				String path = resource.getLocation().lastSegment();
				if (path.equalsIgnoreCase(fileName)) {
					return (IFile)resource;
			    }

				if (resource instanceof IContainer) {
					IFile result = findFile((IContainer)resource, fileName);
					if (result != null) {
				 		return result;
					}
				}
			}
		}
		catch (CoreException e)
		{
		}

		return null;
	}

	public IFile findFile(String fileName) {
		IFile file = null;

		if (fileName != null) {
			// If last search was for same file, then just return the last result
			if (fileName.equals(lastFileName) && lastFile != null)
			{
				return lastFile;
			}
			if (homeProject != null)
			{
				file = findFile(homeProject, fileName);
			}
			if (fWorkspace != null && file == null) {
				IWorkspaceRoot root = fWorkspace.getRoot();
				file = findFile(root, fileName);
			}
			// Cache the results
			lastFileName = fileName;
			lastFile = file;
		}

		return file;
	}


	/**
	 * Gets the searchPath
	 * @return Returns a String
	 */
	public String getSearchPath() {
		return searchPath;
	}
	/**
	 * Sets the searchPath
	 * @param searchPath The searchPath to set
	 */
	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}

}
