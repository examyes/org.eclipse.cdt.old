package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/BreakpointWizard.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:01:15)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLUtils;

/**
 * Superclass for the breakpoint wizards
 **/

public abstract class BreakpointWizard extends Wizard {

	//The currently selected debug target. May be null.
	protected PICLDebugTarget target;

	protected PICLDebugTarget getSelectedTarget()
	{
		return target;
	}

	protected void findSelectedDebugTarget()
	{
		IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if(page ==null) return;
		DebugView view= (DebugView) page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if(view==null)	return;

		ISelection sel = view.getViewer().getSelection();

		Object element = null;
		if (sel instanceof IStructuredSelection)
		{

			IStructuredSelection selection = (IStructuredSelection) sel;
			if(!selection.isEmpty() && selection.size() == 1)
				element = selection.getFirstElement();
			else
				return;

		}
		else
		  element = sel;

		//check item is a stack frame, process, or thread for live debug target	that supports address breakpoints
		if (element instanceof PICLDebugElement && !((IDebugElement)element).getDebugTarget().isTerminated() )
			target = (PICLDebugTarget)((IDebugElement)element).getDebugTarget();

		else if (element instanceof ILaunch) //launcher
		{
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
			if (dt instanceof PICLDebugTarget && !((PICLDebugTarget)dt).isTerminated())
				target = (PICLDebugTarget)dt;
		}
	}

	//Checks if any of the 3 conditional FCT bits are on
	protected boolean conditionalBPSupported()
	{
		if (target == null)
			return false;
		return (expressionsSupported() || frequencySupported() || threadsSupported());

	}
	//Checks frequency FCT bit
	protected boolean frequencySupported()
	{
		if (target == null)
			return false;
		return target.getBreakpointCapabilities().breakpointFrequencySupported();
	}
	//Checks threads FCT bit
	protected boolean threadsSupported()
	{
		if (target == null)
			return false;
		return target.getBreakpointCapabilities().breakpointThreadsSupported();
	}

	//Checks expression FCT bit
	protected boolean expressionsSupported()
	{
		if (target == null)
			return false;
		return target.getBreakpointCapabilities().conditionalBreakpointsSupported();
	}

	protected boolean deferredSupported()
	{
		if (target == null)
			return false;
		return target.getBreakpointCapabilities().deferredBreakpointsSupported();
	}

	public boolean canFinish()
	{
		//Only first of two pages are mandatory, so if page 1 complete, can finish
		if (getStartingPage().isPageComplete())
			return true;
		return false;
	}

	public static String getPluginIdentifier()
	{
		return PICLUtils.getModelIdentifier();
	}

	/**
	 * @see Wizard#performFinish()
	 * Subclasses must override method.
	 */
	public abstract boolean performFinish();


}

