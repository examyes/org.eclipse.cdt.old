package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/EngineSuppliedViewEditorInput.java, eclipse, eclipse-dev, 20011129
// Version 1.15 (last modified 11/29/01 14:15:56)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Host;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.View;
import com.ibm.debug.model.ViewFile;
import com.ibm.debug.model.ViewInformation;
import java.io.IOException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import com.ibm.debug.WorkspaceSourceLocator;

/**
 * Used as the editor input for <code>EngineSuppliedView</code> which is the
 * text supplied by a debug engine for a "source view" (e.g. disassembly, mixed)
 */

public class EngineSuppliedViewEditorInput implements IStorageEditorInput {

	private EngineSuppliedView fEngineSuppliedView;
	private PICLStackFrame fStackFrame = null;
	private Location fLocation = null;
	public boolean fUndetermined = true;
	private byte fEngineHost = 0;
	private IProject fProject = null;
	private PICLDebugTarget fPICLDebugTarget = null;

	/**
	 * Constructor for EngineSuppliedViewEditorInput
	 */
	public EngineSuppliedViewEditorInput(EngineSuppliedView engineView, PICLStackFrame frame) {
		super();
		fEngineSuppliedView = engineView;
		fStackFrame = frame;
		fUndetermined = false;

		fEngineHost = fStackFrame.getDebugEngine().host().getPlatformID();
		fPICLDebugTarget = (PICLDebugTarget) fStackFrame.getDebugTarget();
	}

	/**
	 * Constructor for EngineSuppliedViewEditorInput
	 */
	public EngineSuppliedViewEditorInput(PICLStackFrame frame) {
		super();
		fStackFrame = frame;
		fEngineSuppliedView = new EngineSuppliedView(frame);
		fUndetermined = true;
		fEngineHost = fStackFrame.getDebugEngine().host().getPlatformID();
		fPICLDebugTarget = (PICLDebugTarget) fStackFrame.getDebugTarget();
	}

	/**
	 * Constructor for EngineSuppliedViewEditorInput
	 */
	public EngineSuppliedViewEditorInput(Location location, PICLDebugTarget target) {
		super();
		fLocation = location;
		fEngineSuppliedView = new EngineSuppliedView(location);
		fUndetermined = true;

		fPICLDebugTarget = target;
		fEngineHost = target.getDebugEngine().host().getPlatformID();
	}


	/**
	 * @see IStorageEditorInput#getStorage()
	 */
	public IStorage getStorage() throws CoreException {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		if (fUndetermined)
			// Not able to build view properly
			return null;

		return fEngineSuppliedView;
	}

	/**
	 * Default method to fill fEngineSuppliedView with lines
	 */
	public void setEngineSuppliedView() {
		Location loc = getLocation();
		if (loc == null)
			return;

		int lineNum = loc.lineNumber();
		ViewFile vfile = loc.file();
		int startLine = 1;
		int endLine = 1;
		try {
			startLine = vfile.firstLineNumber();
			endLine =  vfile.lastLineNumber();
			fEngineSuppliedView.initEngineSuppliedView(startLine, endLine);

		} catch (IOException ioe) {}

		if (lineNum >  30) {
			startLine = lineNum - 30;
		}
		if (endLine > lineNum + 70) {
			endLine = lineNum + 70;
		}

		setEngineSuppliedView(startLine, endLine);
	}


	/**
	 * fill fEngineSuppliedView with a specific range of lines
	 */
	public void setEngineSuppliedView(int start, int end) {
		if (!(fEngineSuppliedView.showLines(start, end))) {
			return;
		}
		fUndetermined = false;
	}

	/**
	 * set fEngineSuppliedView to some other EngineSuppliedView
	 */
	public void setEngineSuppliedView(EngineSuppliedView newContent) {
		fEngineSuppliedView = newContent;
		fUndetermined = false;
	}


	/**
	 * refresh this editorinput with information from the specified stackframe
	 */
	public void setStackFrame(PICLStackFrame frame) {
		fStackFrame = frame;
		fLocation = fStackFrame.getLocation(fStackFrame.getViewInformation());
		fEngineHost = fStackFrame.getDebugEngine().host().getPlatformID();
		fPICLDebugTarget = (PICLDebugTarget)fStackFrame.getDebugTarget();
		fEngineSuppliedView.setStackFrame(frame);
		fUndetermined = true;	//reload the enginesuppliedview the next time getStorage() is called
	}
	
	/**
	 * @see IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/**
	 * @see IEditorInput#getName()
	 */
	public String getName() {
		if (fEngineSuppliedView == null)
			return PICLUtils.getResourceString("EngineSuppliedView.labelUnknown");
		return fEngineSuppliedView.getName();
	}

	/**
	 * Sets this editor input to its undetermined state.
	 */
	public void setUndetermined() {
		fUndetermined = true;
		fEngineSuppliedView = null;
	}

	public int getLineNumber() {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		return fEngineSuppliedView.getLineNumber();
	}

	public int getBufferStartLine() {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		return fEngineSuppliedView.getBufferStartLine();
	}

	public int getBufferEndLine() {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		return fEngineSuppliedView.getBufferEndLine();
	}

	public int getFileStartLine() {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		return fEngineSuppliedView.getFileStartLine();
	}

	public int getFileEndLine() {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		return fEngineSuppliedView.getFileEndLine();
	}

	/**
	 * Sets the line number for this editor input.
	 */
	public void setLineNumber(int newLineNumber) {
		if (fUndetermined) {
			setEngineSuppliedView();
		}
		fEngineSuppliedView.setLineNumber(newLineNumber);
	}

	/**
	 * Sets this editor input to its undetermined state.
	 */
	public boolean isUndetermined() {
		return fUndetermined;
	}


	/**
	 * @see IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * @see IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return null;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IProject.class)
			return getProject();
		if (adapter == IResource.class)
			return getProject();
		return null;
	}

	public boolean[] getLineIsExecutable() {
		if (fEngineSuppliedView == null)
			return null;
		return fEngineSuppliedView.getLineIsExecutable();
	}


	public IProject getProject() {
		// First choice is to use the project associated with the stack frame
		if (fStackFrame != null) {
			ISourceLocator sourceLocator = fStackFrame.getLaunch().getSourceLocator();
			if (sourceLocator instanceof WorkspaceSourceLocator) {
				WorkspaceSourceLocator wslocator = (WorkspaceSourceLocator) sourceLocator;
				return wslocator.getHomeProject();
			}
		}
		// If we have been told about a project then use it
		return fProject;
	}

	public void setProject(IProject project) {
		fProject = project;
	}

	public int getPrefixLength() {
		// TODO - how do you handle this when there is no stackframe ??
		if (fStackFrame != null) {
			View view = fStackFrame.getCorrespondingView(fStackFrame.getViewInformation());
			if (view != null)
				return view.prefixLength();
		}
		return 0;
	}



	/**
	 * Gets the engineHost
	 * @return Returns a byte
	 */
	public byte getEngineHost() {
		return fEngineHost;
	}

		/**
	 * Return the ViewInformation associated with this EngineSuppliedView
	 */
	public ViewInformation getViewInformation() {
		if (fStackFrame != null)
			return fStackFrame.getViewInformation();
		else if (fLocation != null)
			return fLocation.file().view().viewInformation();

		return null;
	}

	/**
	 * Return the source file name associated with this EngineSuppliedView
	 */
	public String getSourceFile() {
		if (fStackFrame != null) {
			IPath path = fStackFrame.getSourceFile();
			if (path != null)
				return path.toOSString();
		} else if (fLocation != null) {
			try {
				return fLocation.file().baseFileName().trim();
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Find a Location to use from either the stackframe or Location, which ever is available.
	 *
	 */
	private Location getLocation() {
		Location loc = null;
		if (fStackFrame != null) {
			ViewInformation viewInfo = fStackFrame.getViewInformation();
			if (viewInfo == null)
				return null;

			loc = fStackFrame.getLocation(viewInfo);

		} else if (fLocation != null) {
			loc = fLocation;
		}

		return loc;
	}


	/**
	 * Gets the PICLDebugTarget
	 * @return Returns a PICLDebugTarget
	 */
	public PICLDebugTarget getPICLDebugTarget() {
		return fPICLDebugTarget;
	}

}

