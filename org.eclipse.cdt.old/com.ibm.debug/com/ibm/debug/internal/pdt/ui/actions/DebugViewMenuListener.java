package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/DebugViewMenuListener.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLStackFrame;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Host;
import com.ibm.debug.model.View;
import com.ibm.debug.model.ViewInformation;
import java.util.Vector;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class DebugViewMenuListener implements IMenuListener {
	protected final static String PREFIX= "DebugViewMenuListener";
	static private int MAX_VIEWS = 5;
	DebugView debugView = null;
	SwitchViewActionContributionItem acArray[] = null;
	/**
	 * Constructor for DebugViewMenuListener
	 */
	public DebugViewMenuListener(DebugView dv) {
		super();
		debugView = dv;
		acArray = new SwitchViewActionContributionItem[MAX_VIEWS];
		SwitchViewBaseAction action = new SwitchToSourceViewAction();
		action.setText(PICLUtils.getResourceString(PREFIX+".showSource"));
		action.setView(dv);
		acArray[0] = new SwitchViewActionContributionItem(action, EPDC.View_Class_Source);
		action = new SwitchToDisassemblyViewAction();
		action.setText(PICLUtils.getResourceString(PREFIX+".showDisassembly"));
		action.setView(dv);
		acArray[1] = new SwitchViewActionContributionItem(action, EPDC.View_Class_Disasm);
		action = new SwitchToMixedViewAction();
		action.setText(PICLUtils.getResourceString(PREFIX+".showMixed"));
		action.setView(dv);
		acArray[2] = new SwitchViewActionContributionItem(action, EPDC.View_Class_Mixed);
		action = new SwitchToListingViewAction();
		action.setText(PICLUtils.getResourceString(PREFIX+".showListing"));
		action.setView(dv);
		acArray[3] = new SwitchViewActionContributionItem(action, EPDC.View_Class_Listing);
		action = new SwitchToStatementViewAction();
		action.setText(PICLUtils.getResourceString(PREFIX+".showStatement"));
		action.setView(dv);
		acArray[4] = new SwitchViewActionContributionItem(action, EPDC.View_Class_Disasm);
	}

	/**
	 * @see IMenuListener#menuAboutToShow(IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager mm) {
		if (debugView == null)
			return;
		ISelection sel = debugView.getViewer().getSelection();
		Object element = null;
		boolean show = false;

		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection selection = (IStructuredSelection) sel;
			element = selection.getFirstElement();
		} else
		  element = sel;

		// First remove any existing entries from the menu
		// Probably safest to just remove them all, regards of whether we
		// think it is already on the menu or not
		for (int i = 0; i < MAX_VIEWS; i++)
			mm.remove(acArray[i]);

		if (element instanceof PICLStackFrame)
		{
			PICLStackFrame stackFrame = (PICLStackFrame) element;

		 	// First make sure right source views are in menu
		 	PICLDebugTarget pdt = (PICLDebugTarget) stackFrame.getDebugTarget();
		 	ViewInformation engineViews[] = pdt.getDebugEngine().supportedViews();
		 	boolean useStatementView = false;
			if (pdt.getDebugEngine().host().getPlatformID() == Host.OS400)
				useStatementView = true;

			int viewCount = (engineViews == null ? 0 : engineViews.length);
			int i = 0;  //loop counter
			short kind = 0;
			for (i = 0; i < viewCount; i++)	{
				if (engineViews[i] == null) continue;
				ViewInformation vi = engineViews[i];
				if (vi == null) continue;
				kind = vi.kind();
				if (kind == EPDC.View_Class_Source) {
					mm.appendToGroup("ViewSwitching", acArray[0]);
					acArray[0].setEnabled(false);
					acArray[0].setStackFrame(stackFrame);
				}
				else if (kind == EPDC.View_Class_Disasm) {
					int index = 1;
					if (useStatementView)
						index = 4;

					mm.appendToGroup("ViewSwitching", acArray[index]);
					acArray[index].setEnabled(false);
					acArray[index].setStackFrame(stackFrame);
				}
				else if (kind == EPDC.View_Class_Mixed) {
					mm.appendToGroup("ViewSwitching", acArray[2]);
					acArray[2].setEnabled(false);
					acArray[2].setStackFrame(stackFrame);
				}
				else if (kind == EPDC.View_Class_Listing) {
					mm.appendToGroup("ViewSwitching", acArray[3]);
					acArray[3].setEnabled(false);
					acArray[3].setStackFrame(stackFrame);
				}

			}

		 	// Show proper enablement for each menu item
		 	View partViews[] = stackFrame.getSupportedViews();

		 	viewCount = (partViews == null ? 0 : partViews.length);
		 	kind = 0;
		 	for (int j = 0; j < viewCount; j++)
			{
				if (partViews[j] == null)  continue;
				ViewInformation viewVI = partViews[j].viewInformation();
				if (viewVI != null)
				{
					//enable appropriate menu item
					kind = viewVI.kind();
					if (kind == EPDC.View_Class_Source)
						acArray[0].setEnabled(true);
					else if (kind == EPDC.View_Class_Disasm) {
						if (useStatementView)
							acArray[4].setEnabled(true);
						else
							acArray[1].setEnabled(true);
					}
					else if (kind == EPDC.View_Class_Mixed)
						acArray[2].setEnabled(true);
					else if (kind == EPDC.View_Class_Listing)
						acArray[3].setEnabled(true);
				}
			}
		}
	}
}

