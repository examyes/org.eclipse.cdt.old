package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLStackFrame.java, eclipse, eclipse-dev, 20011128
// Version 1.23 (last modified 11/28/01 15:58:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.*;
import com.ibm.debug.model.MonitoredExpression;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IDebugElement;
import com.ibm.debug.internal.picl.SourceLocation;
import com.ibm.debug.epdc.EPDC;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Instances of this class represent a single stack frame in the PICL debug model
 */
public class PICLStackFrame extends PICLDebugElement implements IStackFrame, StackFrameEventListener, LocalMonitoredExpressionsEventListener {

	// Resource String keys
	private static final String PREFIX= "picl_stack_frame.";
	private static final String LABEL= PREFIX + "label.";
	private static final String STACK_FRAME= LABEL + "stack_frame";
	private static final String LINE= LABEL + "line";
	private static final String ERROR= PREFIX + "error.";
	private static final String DROP_TO_FRAME= ERROR + "drop_to_frame";
	private static final String STATE= PREFIX + "state.";
	private static final String UNKNOWN= LABEL + "unknown";

	/**
	 * The IBM debug model stack frame this PICL element is a proxy for.
	 */
	protected StackFrame fStackFrame;

	/**
	 * The view information (source view, disassembly view, etc.) associated
	 * with this stack frame.
	 * @see com.ibm.debug.model.ViewInformation
	 */
	protected ViewInformation fViewInformation;
	/**
	 * The current source location for this stack frame.
	 */
	protected SourceLocation fSourceLocation= new SourceLocation();
	/**
	 * The current ViewFile for this stack frame.
	 */
	protected ViewFile fViewFile= null;

	/**
	 * The current engine supplied view (e.g. dissasembly view, mixed view)
	 * content and location for this stack frame.
	 */
	private EngineSuppliedViewEditorInput fEngineSuppliedViewEI;

	/**
	 * Flag indicating if locals are monitored
	 */
	private boolean fMonitoringLocals = false;
	private LocalMonitoredExpressions fLocalMonitor	= null;


	/**
	 * Creates the PICL proxy for this stack frame
	 * with the <code>IThread</code> parent.
	 */
	public PICLStackFrame(IThread parent, StackFrame stackFrame) {
		super(parent, IDebugElement.STACK_FRAME);
		fStackFrame= stackFrame;
		fStackFrame.addEventListener(this);
		fEngineSuppliedViewEI = new EngineSuppliedViewEditorInput(this);
//		LocalMonitoredExpressions monitor= getLocalExpressionsMonitor();
//		if (monitor != null) {
//			monitor.addEventListener(this);
//			MonitoredExpression[] monitoredExpressions= monitor.getLocalMonitoredExpressionsArray();
//			if (monitoredExpressions != null) {
//				for (int j= 0; j < monitoredExpressions.length; j++) {
//					if (monitoredExpressions[j] != null) {
//						//add the variables to the stack frame that already exist
//						this.addChild(new PICLVariable(this, monitoredExpressions[j]));
//					}
//				}
//			}
//		}
	}

	/**
	 * Stack frames can not be resumed.  Resuming a stack frame
	 * really means resuming the stack frame's parent thread.
	 */
	public boolean canResume() {
		return ((IThread) getParent()).canResume();
	}

	/**
	 * Stack frames can not be suspended.  Suspending a stack frame
	 * really means suspending the stack frame's parent thread.
	 */
	public boolean canSuspend() {
		return ((IThread) getParent()).canSuspend();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepInto() {
		return getThread().canStepInto();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepOver() {
		return getThread().canStepOver();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepReturn() {
		return getThread().canStepReturn();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepDebug() {
		return ((PICLThread)getThread()).canStepDebug();
	}

	/**
	 * Determines the name of the source file and the current location
	 * for this stack frame
	 */
	protected void determineSourceFileNameAndLocation() throws DebugException {

		if (haveDoneCleanup()) {
			return;
		}
		try {
			Location location= getLocation(getViewInformation());
			if (location != null) {
				fSourceLocation.setLineNumber(location.lineNumber());
				if (fSourceLocation.getPath() == null) { //undetermined
					ViewFile viewFile= location.file();
					fSourceLocation.setPath(new Path(viewFile.baseFileName().trim()));
					String sourceFileName= fSourceLocation.getPath().lastSegment();
				}
			}
		} catch (IOException e) {
//			throw new DebugException(e);
		}
	}

	/**
	 * Returns the local expressions monitor for this stack frame.
	 */
	protected LocalMonitoredExpressions getLocalExpressionsMonitor() {
		return ((PICLThread) getParent()).getLocalExpressionsMonitor();
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() {
		if (haveDoneCleanup()) {
			return PICLUtils.getResourceString(STATE + "terminated");
		}
		StringBuffer buffer= new StringBuffer();
		String[] columns= fStackFrame.columns();
		if ((columns != null) && (columns.length > 0)) {
			buffer.append(columns[2]);
			buffer.append("::");
			buffer.append(columns[1]);
			return buffer.toString();
		} else {
			return PICLUtils.getResourceString(STACK_FRAME);
		}
	}

	/**
	 * @see IDebugElement
	 */
	public String getLabel(boolean qualified) {
		if (haveDoneCleanup()) {
			return "";
		}
		StringBuffer buffer= new StringBuffer();
		String[] columns= fStackFrame.columns();
		
		//build as much of a reasonable label as possible
		if ((columns != null) && (columns.length > 1)) {
			String functionName = columns[1];
			if (functionName == null)
				functionName = PICLUtils.getResourceString(UNKNOWN);
			if (!qualified) {
				int index= functionName.lastIndexOf(':');
				if (index != -1) {
					functionName= functionName.substring(index + 1);
				}
			}
			buffer.append(functionName);

			Location loc = getLocation(getViewInformation())			;

			if (loc != null) {
				int line= loc.lineNumber();
				buffer.append(": " + PICLUtils.getResourceString(LINE)+ " " + line);
			}
		} else {
			buffer.append(PICLUtils.getResourceString(STACK_FRAME));
		}
		return buffer.toString();
	}


	protected Location getLocation(ViewInformation view) {
		PICLThread pt = (PICLThread)getParent();
		PICLStackFrame topStackFrame = (PICLStackFrame)pt.getTopStackFrame();
		try {
			if (topStackFrame.equals(this))
				return ((PICLThread)getParent()).getLocation(view);
			else
				return fStackFrame.getCurrentLocation(view);
		} catch(Exception e) {
			return null;   // not sure what should be returned here
		}
	}




	/**
	 * Returns the name of the stack frame
	 * with out the parameters
	 */
	protected String getStackFrameName() {
		if (haveDoneCleanup()) {
			return "";
		}
		String name;
		String[] columns= fStackFrame.columns();
		if ((columns != null) && (columns.length > 0)) {
			name= columns[2];
			//remove the parameters
			int index= name.indexOf('(');
			if (index != -1) {
				name= name.substring(0, index);
			}
			return name;
		} else {
			return PICLUtils.getResourceString(STACK_FRAME);
		}
	}

	/**
	 * Returns the name of the object for the stack frame.
	 * In the case of Java for example, this is the name of the class.
	 * May return <code>null</code>.
	 */
	protected String getObjectName() {
		if (haveDoneCleanup()) {
			return null;
		}
		String[] columns= fStackFrame.columns();
		if ((columns != null) && (columns.length > 1)) {
			return columns[1];
		} else {
			return null;
		}
	}

	/**
	 * @see IStackFrame
	 */
	public SourceLocation getSourceLocation() {
		try {
			determineSourceFileNameAndLocation();
		} catch (DebugException de) {
			logError(de);
		}
		return fSourceLocation;
	}

	/**
	 * @see IDebugElement
	 */
	public IStackFrame getStackFrame() {
		return this;
	}

	/**
	 * Returns the <code>ViewInformation</code> object currently used to
	 * get the view for this frame
	 */
	public ViewInformation getViewInformation() {
		if (fViewInformation == null) {
			PICLDebugTarget pdt = (PICLDebugTarget) getDebugTarget();
			ViewInformation preferredView = pdt.getPreferredView();
			// Source is the default view unless user specified something different
			short preferredKind = EPDC.View_Class_Source;
			if (preferredView != null)
				preferredKind = preferredView.kind();

			// If the preferred view is not available,
			// the second choice will be the source view if available,
			// else the first available view.
			ViewInformation secondChoice = null;
			View views[] = getSupportedViews();

			int viewCount = (views == null ? 0 : views.length);
		 	for (int j = 0; j < viewCount; j++)
			{
				if (views[j] == null)  continue;
				ViewInformation viewVI = views[j].viewInformation();
				if (viewVI != null)
				{
					short kind = viewVI.kind();
					if (preferredKind != EPDC.View_Class_Unk && preferredKind == kind )
					{
						fViewInformation = viewVI;
						return fViewInformation;
					}

					if (kind == EPDC.View_Class_Source)
					{
						secondChoice = viewVI;
					}
					if (secondChoice == null)
						secondChoice = viewVI;
				}
			}
			// No match found for preferred view, so fall back to second choice.
			fViewInformation = secondChoice;
		}
		return fViewInformation;
	}

	/**
	 * Returns the first <code>ViewInformation</code> from the list of
	 * supported <code>ViewInformation</code> for the current
	 * <code>DebugEngine</code> that is valid for this frame
	 */
	ViewInformation findSupportedViewInformation() {

		ViewInformation engineViews[] = getDebugEngine().supportedViews();
		// Check each view to see if it is available for the current part
		int viewCount = (engineViews == null ? 0 : engineViews.length);
		int i = 0;  //loop counter
		for (i = 0; i < viewCount; i++)	{
			if (engineViews[i] == null) continue;
			ViewInformation vi = engineViews[i];
			Location loc = getLocation(vi);
			if (loc != null) {
				// we have found a view that is supported by this part
				return vi;
			}
		}
		return null;
	}

	/**
	 * @see IStep
	 */
	public boolean isStepping() {
		return ((PICLThread) getParent()).isStepping();
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean isSuspended() {
		return ((PICLThread) getParent()).isSuspended();
	}

	/**
	 * @see com.ibm.debug.model.LocalMonitoredExpressionsEventListener
	 */
	public void localExpressionsMonitorEnded(LocalMonitoredExpressionsEndedEvent event) {
    	PICLUtils.logEvent("local expressions monitor ended",this);

		removeAllChildren();
		fMonitoringLocals = false;
	}

	/**
	 * @see LocalMonitoredExpressionsEventListener
	 */
	public void monitoredExpressionAdded(MonitoredExpressionAddedEvent event) {
    	PICLUtils.logEvent("monitored expression added",this);

		addChild(new PICLVariable(this, event.getMonitoredExpression()));
	}

	/**
	 * Stack frames can not be resumed.  Resuming a stack frame
	 * really means resuming the stack frame's parent thread.
	 */
	public void resume() throws DebugException {
		((IThread) getParent()).resume();
	}

	/**
	 * Sets the <code>ViewInformation</code> object used to
	 * get the view for this frame
	 */
	public void setViewInformation(ViewInformation viewInformation, boolean openNewEditorInput) {
		if (fViewInformation != viewInformation) {
			fViewInformation= viewInformation;
			
			if (openNewEditorInput) {
				// create a new editor input (and editor) to display new content
				fEngineSuppliedViewEI = new EngineSuppliedViewEditorInput(this);
			} else {
				// re-use the current editor input 
				fEngineSuppliedViewEI.setStackFrame(this);
			}
		}
	}

	/**
	 * @see StackFrameEventListener#stackFrameChanged(StackFrameChangedEvent)
	 */
	public void stackFrameChanged(StackFrameChangedEvent event) {
    	PICLUtils.logEvent("stack frame changed",this);
 	
    	if (event.getSource() instanceof StackFrame) {
    		StackFrame stack =  (StackFrame)event.getSource();
    		ViewInformation viewInfo = getViewInformation();
    		try { 
    			Location loc = stack.getCurrentLocation(viewInfo);
    			if ((loc != null) && (loc.file() != null) && (loc.file() != fViewFile)) {
    				if (fViewFile == null) {
    					// Initial load of ViewFile generates a stackFrameChanged event,
    					// so if fViewFile is null, do not throw away this editorinput
	    				setViewInformation(null, false);
    				} else {
						// Clean up cached information
	    				setViewInformation(null, true);
    				}
					fViewFile = loc.file();
    			}
    		} catch(IOException ioe) {}
    	}
    	
    	fSourceLocation= new SourceLocation();
		fireChangeEvent();
	}

	/**
	 * @see StackFrameEventListener#stackFrameEnded(StackFrameChangedEvent)
	 */
	public void stackFrameEnded(StackFrameEndedEvent event) {
    	PICLUtils.logEvent("stack frame ended",this);

		// tell the thread (parent) to remove this stackframe.
		((PICLThread)getParent()).removeChild(this);
	}

	/**
	 * @see IStep
	 */
	public void stepInto() throws DebugException {
		getThread().stepInto();
	}

	/**
	 * @see IStep
	 */
	public void stepOver() throws DebugException {
		getThread().stepOver();
	}

	/**
	 * @see IStep
	 */
	public void stepReturn() throws DebugException {
		getThread().stepReturn();
	}

	/**
	 * @see IStep
	 */
	public void stepDebug() throws DebugException {
		((PICLThread)getThread()).stepDebug();
	}

	/**
	 * Stack frames can not be suspended.  Suspending a stack frame
	 * really means suspending the stack frame's parent thread.
	 */
	public void suspend() throws DebugException {
		((IThread) getParent()).suspend();
	}

	/**
	 * @see IDropToFrame
	 */
	public boolean supportsDropToFrame() {
		return false;
	}

	/**
	 * @see IDropToFrame
	 */
	public void dropToFrame() throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.NOT_SUPPORTED, PICLUtils.getResourceString(DROP_TO_FRAME), null));
	}

	/**
	 * @see PICLDebugElement
	 */
	 protected void doCleanupDetails() {
		fStackFrame.removeEventListener(this);
		if (fLocalMonitor != null)
			fLocalMonitor.removeEventListener(this);
		fStackFrame= null;
		fViewInformation = null;
		fSourceLocation = null;
		fLocalMonitor = null;
	 }

	public IPath getSourceFile() {
		SourceLocation sourceLocation = getSourceLocation();
		if (sourceLocation != null) {
			IPath path = sourceLocation.getPath();
			return path;
		}
		return null;
	}

	public int getLineNumber() {
		ViewInformation viewInfo = getViewInformation();
		if (viewInfo.isSourceView()) {
			SourceLocation sourceLocation = getSourceLocation();
			if (sourceLocation != null) {
				int lineNum = sourceLocation.getLineNumber();
				if (lineNum > 0)
					return lineNum;
			}
			// Maybe wasn't really a source view
			EngineSuppliedViewEditorInput engineView = getEngineSuppliedViewEI();
			if (engineView != null) {
				return engineView.getLineNumber();
			}
			return 0;
		} else if (viewInfo.isDisassemblyView() ||
				   viewInfo.isMixedView()       ||
				   viewInfo.isListingView()) {
			EngineSuppliedViewEditorInput engineView = getEngineSuppliedViewEI();
			if (engineView != null) {
				return engineView.getLineNumber();
			}
		}

		return 0;

	}

	/**
	 * Returns the views that are supported by the part associated with this
	 * PICLStackFrame
	 */
	public View [] getSupportedViews() {
		// NOTE: Do not call getViewInformation() here because it may
		// be calling this function to get an initial value.
		// If fViewInformation is not set yet then just get any supported
		// ViewInformation to pass to getLocation() to get the complete list
		ViewInformation viewInfo = fViewInformation;
		if (viewInfo == null)
			viewInfo = findSupportedViewInformation();
		if (viewInfo == null)
			return null;

		Location loc = getLocation(viewInfo);
		if (loc != null) {
			ViewFile vFile = loc.file();
			if (vFile != null) {
				View view = vFile.view();
				if (view != null) {
					Part part = view.part();
					if (part != null)
						return part.views();
				}
			}
		}
		return null;
	}


	public View getCorrespondingView(ViewInformation viewInfo) {

		if (viewInfo == null)
			return null;
		Location loc = getLocation(viewInfo);
		if (loc == null)
			return null;

		ViewFile vfile = loc.file();
		if (vfile == null)
			return null;

		return vfile.view();
	}


	/**
	 * Gets the fEngineSuppliedViewEI
	 * @return Returns a EngineSuppliedViewEditorInput
	 */
	public EngineSuppliedViewEditorInput getEngineSuppliedViewEI() {
		if (fEngineSuppliedViewEI.isUndetermined()) {
			try {
				fEngineSuppliedViewEI.getStorage();
			} catch (CoreException ce) {
				return null;
			}
		}
		if (fEngineSuppliedViewEI.isUndetermined())
        	return null;
		return fEngineSuppliedViewEI;
	}

	/**
	 * Sets the fEngineSuppliedViewEI
	 * @param engineView The EngineSuppliedViewEditorInput to set
	 */
	public void setEngineSuppliedViewEI(EngineSuppliedViewEditorInput engineView) {
		this.fEngineSuppliedViewEI = engineView;
	}
	/**
	 * @see PICLDebugElement#hasChildren()
	 */
	public boolean hasChildren() {
		if (fMonitoringLocals)
			return super.hasChildren();
		else
			return true;
	}

	/**
	 * @see PICLDebugElement#getChildren()
	 */
	public IDebugElement[] getChildren() throws DebugException {
		if (!fMonitoringLocals) {
			fLocalMonitor = ((PICLThread)getParent()).getLocalMonitoredExpressions(this);
			if (fLocalMonitor == null)  // locals monitor failed
				return super.getChildren();
			else
				fMonitoringLocals = true;
			fLocalMonitor.addEventListener(this);
			// call thread to start monitoring.   Then add the results to this stackframe
			MonitoredExpression monitoredExpressions[] = fLocalMonitor.getLocalMonitoredExpressionsArray();
			// now add them as children to this stackframe
			if (monitoredExpressions != null) {
				for (int j= 0; j < monitoredExpressions.length; j++) {
					if (monitoredExpressions[j] != null) {
						//add the variables to the stack frame that already exist
						addChild(new PICLVariable(this, monitoredExpressions[j]));
					}

				}
			}

		}


		return super.getChildren();

	}

	/**
	 * @see PICLDebugElement#getChildrenNoExpand()
	 * Don't request locals from the engine just return whatever children we happen to have
	 */
	public IDebugElement[] getChildrenNoExpand() throws DebugException {
		return super.getChildren();
	}


	/**
	 * @see PICLDebugElement#getChildrenAsList()
	 */
	public List getChildrenAsList() {
		return super.getChildrenAsList();
	}

	/**
	 * Returns the relative number that this stackframe is from the top
	 */
	public int getStackNumber() {
		return 0;   // for now always return 0 which means the top of the stack
					// in the future this should return the entry number of this
					// stackframe relative to the top.
	}

	/**
	 * Reset anything in the local variables that uses changed flags
	 */

	public void resetChanged() {
		// loop through all of the variables and reset the changed flags

		if (!hasChildren())
			return;

		IDebugElement vars[] = null;

		try {
			vars = getChildrenNoExpand();
		} catch(DebugException de) {
			return;
		}
		for (int i = 0;i < vars.length; i++)
			((PICLVariable)vars[i]).resetChanged();

	}


}

