package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/DebuggerEditor.java, eclipse, eclipse-dev, 20011128
// Version 1.12 (last modified 11/28/01 15:59:32)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.internal.picl.EngineSuppliedView;
import com.ibm.debug.internal.picl.EngineSuppliedViewEditorInput;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Host;
import com.ibm.debug.model.ViewInformation;
import com.ibm.lpex.alef.LpexTextEditor;
import com.ibm.lpex.core.LpexDocumentLocation;
import com.ibm.lpex.core.LpexParser;
import com.ibm.lpex.core.LpexView;

public class DebuggerEditor extends LpexTextEditor
							implements IDebugEventListener {
	// Max lines of EngineSuppliedView we will show in the editor at any one time
	private static final int MAX_LINES = 300;
	// We only register the DebugEventListener if we are displaying
	// an engine supplied view
	private boolean fIsRegisteredDebugEventListener = false;

	/**
	 * Constructor for DebuggerEditor
	 */
	public DebuggerEditor() {
		super();
		setDocumentProvider(PICLDebugPlugin.getInstance().getDebuggerEditorDocumentProvider());
		setHelpContextId(PICLUtils.getHelpResourceString("DebuggerEditor"));
	}

   /**
    * @see LpexTextEditor#initializeEditor
    */
	protected void initializeEditor() {
		super.initializeEditor();
   }

   /**
 	* Sets or clears the title of this part.
 	*
 	* @param title the title, or <code>null</code> to clear
 	*/
	protected void setTitle(String title) {
		super.setTitle(title);
	}

	/**
	 * @see LpexAbstractTextEditor#setFocus()
	 */
	public void setFocus() {
		super.setFocus();
	}

	public void gotoMarker(IMarker marker) {
		super.gotoMarker(marker);
		LpexView lpexView = getLpexView();
		if (lpexView != null && marker != null) {
			int len = lpexView.queryInt("length");
			lpexView.doCommand("set emphasisLength " + len); // emphasize the line
			lpexView.triggerAction(lpexView.actionId("scrollCenter")); // make it a centerpiece
		}
	}
	/**
	 * @see LpexAbstractTextEditor#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		super.init(site, input);
	}

	/**
	 * @see LpexAbstractTextEditor#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// Force Debugger parser to be used
		LpexView lpexView = getLpexView();
		if (lpexView != null) {
			lpexView.doCommand("set updateProfile.parserClass.debuggerParser com.ibm.debug.internal.pdt.ui.editor.DebuggerParser");
			lpexView.doCommand("set updateProfile.parser debuggerParser");
			lpexView.doCommand("updateProfile");
		}

		IEditorInput input = getEditorInput();
		if (input instanceof EngineSuppliedViewEditorInput) {
			LpexParser parser = lpexView.parser();
			EngineSuppliedViewEditorInput engineInput = (EngineSuppliedViewEditorInput) input;
			if (parser instanceof DebuggerParser) {
				// Tell parser which lines are executable so it can hightlight properly
				((DebuggerParser)parser).setLineIsExecutable(engineInput.getLineIsExecutable());
			}

			// Tell editor how many source file lines are available above and below the lines it currently knows about
			try {
				EngineSuppliedView view = (EngineSuppliedView) engineInput.getStorage();
				int linesBefore =view.getBufferStartLine() - view.getFileStartLine();
				int linesAfter = view.getFileEndLine() - view.getBufferEndLine();
				setDocumentSection(linesBefore, linesAfter);
				//System.out.println("Initially linesBefore=" + lpexView.linesBeforeStart() +
				//				", linesAfter=" + lpexView.linesAfterEnd());
			} catch (CoreException ce)
			{}

			// Register as a debugEvent listener so we will know when to close
			DebugPlugin.getDefault().addDebugEventListener(this);
			fIsRegisteredDebugEventListener = true;
		}
	}

	/**
    * Set our DebuggerParser for non-file IStorageEditorInput views opened by us.
    */
	public void updateProfile()	{
		IEditorInput input = getEditorInput();
		// First check if file, as an IFileEditorInput is also an IStorageEditorInput
		if (input instanceof IFileEditorInput) {
			// set initial double click action to line breakpoint
			setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK,
					 	getAction("AddLineBreakpoint"));
			return;
		}

		LpexView lpexView = getLpexView();

		if (input instanceof EngineSuppliedViewEditorInput) {
			LpexParser parser = lpexView.parser();
			if (parser instanceof DebuggerParser) {
				EngineSuppliedViewEditorInput dei = (EngineSuppliedViewEditorInput) input;
				((DebuggerParser)parser).setLineIsExecutable(dei.getLineIsExecutable());
				lpexView.doCommand("parse all");
			}
			// set initial double click action to either line or address breakpoint
			String breakpointType = breakpointTypeToUse();
			setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK,
					 	getAction(breakpointType));
		}

		super.updateProfile();
	}
	/**
	 * @see LpexAbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		setAction("AddLineBreakpoint",
			new BreakpointRulerAction(PICLUtils.getResourceBundle(),
									  "EditorAddBreakpoint.",
									  getVerticalRuler(),
									  this,
									  IPICLDebugConstants.PICL_LINE_BREAKPOINT));
		setAction("AddAddressBreakpoint",
			new BreakpointRulerAction(PICLUtils.getResourceBundle(),
									  "EditorAddBreakpoint.",
									  getVerticalRuler(),
									  this,
									  IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT));
	}

	/**
	 * @see LpexAbstractTextEditor#rulerContextMenuAboutToShow(IMenuManager)
	 */
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		//Only add/remove breakpoints if clicked on a valid line
		if (getVerticalRuler().getLineOfLastMouseButtonActivity() >= 0) {
			boolean supported = true;
			//Only display one of Line or Address breakpoint action
			String breakpointType = breakpointTypeToUse();
			addAction(menu, breakpointType);
			IAction action = getAction(breakpointType);

			// Check if add breakpoint action should be enabled or not for
			// this editor.  This can only be done if the editor input
			// came from a debug engine.

			IEditorInput input = getEditorInput();
			if (input instanceof EngineSuppliedViewEditorInput) {
				EngineSuppliedViewEditorInput engineViewEI = (EngineSuppliedViewEditorInput) input;
				String bkptType = IPICLDebugConstants.PICL_LINE_BREAKPOINT;
				if (breakpointType == "AddAddressBreakpoint")
					bkptType = IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT;

				PICLDebugTarget target = engineViewEI.getPICLDebugTarget();
				supported = target.supportsBrkptType(bkptType);
				action.setEnabled(supported);
			}

			if (supported)
				setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, action);
			else
				setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, null);
		}
	}

	/**
	 * Determine if we should be using a line breakpoint or address breakpoint base on what is currently
	 * being edited.
	 */
	protected String breakpointTypeToUse() {
		//Assume we want a line breakpoint as this is the most common
		String breakpointType = "AddLineBreakpoint";
		IEditorInput input = getEditorInput();

		if (input instanceof EngineSuppliedViewEditorInput) {
			EngineSuppliedViewEditorInput engineInput = (EngineSuppliedViewEditorInput) input;

			ViewInformation viewInfo = engineInput.getViewInformation();
			short kind = viewInfo.kind();

			// Address breakpoint used for mixed and real disassembly (i.e. not AS400 statement) views.
			// Line breakpoint used for source, listing and statement views.
			if (kind == EPDC.View_Class_Mixed ||
					(kind == EPDC.View_Class_Disasm && engineInput.getEngineHost() != Host.OS400))
				breakpointType = "AddAddressBreakpoint";
		}
		return breakpointType;
	}

	/**
    * This method is called by LPEX when displaying an
    * EngineSuppliedViewEditorInput source file.  It will be called whenever
    * new source lines need to be added at that top or bottom of the document
    * due to the user srcolling / paging up or down in the document.
    * Note that no KeyListener or ScrollbarListener is required in this
    * editor bacause LPEX is taking care of these things for us and calling
    * this method when we need to do something.
    *
    *
    * @param lpexView the LPEX text-widget view
    * @param lineNeeded the first line before the currently-loaded LPEX document
    *        section, or the last line after the currently-loaded LPEX document
    *        section, in the range of lines with which the current document
    *        section must be expanded;
    *        lineNeeded already includes the minimum threshold indicated
    *
    * @return true = restore original position in view
    *
    * @see #LpexAbstractTextEditor.addLines
    * @see #LpexAbstractTextEditor.setDocumentSection
    */
	protected boolean addLines(LpexView lpexView, int neededLine) {

		//System.out.println(" linesBefore=" + lpexView.linesBeforeStart() +
		//					", linesAfter=" + lpexView.linesAfterEnd() +
		//					" REQUEST to add line " + neededLine);

		IEditorInput input = getEditorInput();

		if (!(input instanceof EngineSuppliedViewEditorInput))
			return false;

		try	{
			EngineSuppliedViewEditorInput engineViewEI = (EngineSuppliedViewEditorInput)input;
			EngineSuppliedView engineView = (EngineSuppliedView) engineViewEI.getStorage();
			int bufStart = engineView.getBufferStartLine();
			int bufEnd = engineView.getBufferEndLine();
			int fileStart = engineView.getFileStartLine();
			int fileEnd = engineView.getFileEndLine();
			int newStart = 0;
			int newEnd = 0;

			// Step 1.  Load new lines in EngineSupplied View
			if (neededLine < lpexView.linesBeforeStart()) {
				// Need to add lines to start of document

				if (bufStart > fileStart) {
					// Force start to match file start to avoid scrolling problems
					newStart = fileStart;
					newEnd = bufEnd;
					engineView.showLines(newStart, newEnd);
				} else
					return false;

			} else {
				// Need to add lines to end of document
				if (bufEnd < fileEnd) {
					newEnd = neededLine;
					// Force start to match file start to avoid scrolling problems
					newStart = fileStart;
					engineView.showLines(newStart, newEnd);
				} else
					return false;
			}

			// Step 2. Update contents of editor

			//lpexView.setText(((LpexSourceViewer)getSourceViewer()).getVisibleDocument().get());
			java.io.InputStream stream = engineView.getContents();
			java.io.InputStreamReader reader = new java.io.InputStreamReader(stream);
			lpexView.load(reader);


			// Step 3. Reset the new boundaries of the document section
			int linesBefore = newStart - engineView.getFileStartLine();
			int linesAfter = engineView.getFileEndLine() - newEnd;

			setDocumentSection(linesBefore, linesAfter);
			//System.out.println(" newLinesBefore=" + lpexView.linesBeforeStart() +
			//				", newLinesAfter=" + lpexView.linesAfterEnd());
		} catch (CoreException e)
		{ return false;}

		return true;
	}

	/**
	 * @see IDebugEventListener
	 */
	public void handleDebugEvent(final DebugEvent event) {

		switch (event.getKind()) {
			case DebugEvent.TERMINATE:
				// We only care about termination of PICLDebugTargets
				Object object = event.getSource();
				if (!(object instanceof PICLDebugTarget))
					return;

				// Make sure it is my debug target that terminated
				IEditorInput input = getEditorInput();
				if (input instanceof EngineSuppliedViewEditorInput) {
					EngineSuppliedViewEditorInput engineInput =
						(EngineSuppliedViewEditorInput) input;
					PICLDebugTarget target = engineInput.getPICLDebugTarget();
					if (target != null && target.isTerminated())
						close(false);
				}
				break;
		}
	}


	/**
	 * @see LpexAbstractTextEditor#dispose()
	 */
	public void dispose() {
		if (fIsRegisteredDebugEventListener)
			DebugPlugin.getDefault().removeDebugEventListener(this);
		super.dispose();
	}

	/**
	 * Create an LPEX mark corresponding to the given Eclipse marker.
	 * LPEX marks are maintained in sync with the text during editing.
	 * LPEX marks are created automatically for an IFile / IResource
	 * handled by this text editor.
	 *
	 * <p>The mark is defined inside the currently-loaded LPEX document section,
	 * and assumes that the marker is defined in an IDocument corresponding to
	 * this section.</p>
	 */
	public void addMark(IMarker marker) {
		
		if (!(getEditorInput() instanceof EngineSuppliedViewEditorInput)) {
			super.addMark(marker);
		} else {
			LpexView lpexView = getLpexView();
			if (lpexView == null)
				return;
		
			int linesBeforeStart = lpexView.linesBeforeStart();
			StringBuffer setMarkName = new StringBuffer(64);
			setMarkName.append("set mark.@ALEF.");
			setMarkName.append(String.valueOf(marker.getId()));
			setMarkName.append(' ');
		
			// NB marker's charStart, charEnd are ZERO-based, lineNumber is ONE-based
			int charStart = MarkerUtilities.getCharStart(marker);
			int charEnd = MarkerUtilities.getCharEnd(marker);
		
			// 1.- character mark
			if (charStart >= 0 && charEnd >= 0) {
				int eolLength = getEOL().length();
				// NB offset conversion can only be done for
				// the currently-loaded LPEX document section
				LpexDocumentLocation start = lpexView.documentLocation(charStart, eolLength);
				// bump up start.element to be inside complete doc for "set mark" command
				start.element += linesBeforeStart;
		
				setMarkName.append(start.element);
				setMarkName.append(' ');
				setMarkName.append(start.position);
		
				if (charEnd != charStart) {
					LpexDocumentLocation end = lpexView.documentLocation(charEnd, eolLength);
					end.element += linesBeforeStart; // also bump up for "set mark" command
		
					setMarkName.append(' ');
					setMarkName.append(end.element);
					setMarkName.append(' ');
					setMarkName.append(end.position);
				}
			}
		
			// 2.- element mark
			else {
				int element = lpexView.elementOfLine(MarkerUtilities.getLineNumber(marker));
				if (element <= 0)
					return;
				element += linesBeforeStart; // also bump up for "set mark" command
		
				setMarkName.append("element ");
				setMarkName.append(element);
			}
			lpexView.doCommand(setMarkName.toString());
		}
	}
}


