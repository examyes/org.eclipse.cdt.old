package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/EditBreakpointAction.java, eclipse, eclipse-dev, 20011128
// Version 1.8 (last modified 11/28/01 16:00:23)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.BreakpointsView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.ui.IWorkbenchPart;
import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.dialogs.AddressBPWizard;
import com.ibm.debug.internal.pdt.ui.dialogs.EntryBPWizard;
import com.ibm.debug.internal.pdt.ui.dialogs.LineBPWizard;
import com.ibm.debug.internal.pdt.ui.dialogs.LoadBPWizard;
import com.ibm.debug.internal.pdt.ui.dialogs.WatchBPWizard;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;

public class EditBreakpointAction extends AbstractOpenWizardWorkbenchAction implements IViewActionDelegate,  ISelectionListener{
	IAction myAction;
	IWorkbenchPage p;
	//keep track of last selection so that we can check for termination before running the action
	PICLDebugTarget lastSelectedTarget;   
	
	/**
	 * Constructor for AddEntryBreakpointAction
	 */
	public EditBreakpointAction(IWorkbench workbench, String label, Class[] acceptedTypes) {
		super(workbench, label, acceptedTypes, false);
	}
	
	/**
	 * Constructor for AddEntryBreakpointAction
	 */
	public EditBreakpointAction(){
		super();		
		init(PICLDebugPlugin.getActiveWorkbenchWindow());	
	}
		
	/**
	 * @see AbstractOpenWizardAction#createWizard()
	 */
	protected Wizard createWizard() {
		//Need to double check that target has not terminated. A selectionChanged event is not
		//generated in this case, so it's possible the action should have been disabled.
		if(lastSelectedTarget == null || lastSelectedTarget.isTerminated())
		{
			//send selection changed event so the other breakpoint actions will change their states as well
			IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			if (p == null) 	return null;
			BreakpointsView view= (BreakpointsView) p.findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
			if(view==null)	return null;	
			
			view.getViewer().setSelection(view.getViewer().getSelection());
			return null;
		}
			
		if (p == null) 	
			p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p==null)
			return null;
		BreakpointsView view= (BreakpointsView) p.findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		if (view != null) 
		{
			ISelection currentSelection =  view.getViewer().getSelection();
			if(currentSelection == null)
				return null;
			if (currentSelection instanceof IStructuredSelection)
			{ //multiple items selected	
				Object[] selections = ((IStructuredSelection)currentSelection).toArray();			
				for (int i = 0; i < selections.length; i++)	
				{
					if(selections[i] == null)
						continue;
					if(selections[i] instanceof IMarker)
						return openWizard((IMarker) selections[i]);
				}
			}
			else if (currentSelection instanceof IMarker)
				return openWizard((IMarker) currentSelection);
		}
		return null;
	}
	
	private Wizard openWizard(IMarker breakpoint)
	{
		String breakpointType = null;

        // Get the type of the breakpoint (specified in the plugin.xml for this debugger)
        // this type will determine how it should be handled.

        try {
            breakpointType = breakpoint.getType();
        } catch(CoreException e) {
  //          throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,"(TBT)Error getting breakpoint type",e));
  				return null;
        }

        // Determine breakpoint type and bring up corresponding dialog for editing
        if (breakpointType.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT)) 
            return new LineBPWizard(breakpoint);
        else if (breakpointType.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT)) 
	       	return new EntryBPWizard(breakpoint);
	    else if (breakpointType.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT)) 
		    return new WatchBPWizard(breakpoint);
		else if (breakpointType.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT)) 
			return new LoadBPWizard(breakpoint);
		else if (breakpointType.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT)) 
			return new AddressBPWizard(breakpoint);
				         
				        
		// bad breakpoint type
//		throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,"(TBT)Unsupported breakpoint type",null));
		return null;
	}
	
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart arg0) {
		//want to be notified when picl stack frame selected so action can be enabled.
		DebugUIPlugin.getDefault().addSelectionListener(this); 		
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 * Processes selection changed events from breakpoint view only.
	 */
	public void selectionChanged(IAction action, ISelection currentSelection) {
		if(myAction == null)
			myAction=action;		
		
		if(currentSelection == null || currentSelection.isEmpty())
		{
			myAction.setEnabled(false);	
			return;
		}
		if(!debugTargetSupportsEditing())
		{
			myAction.setEnabled(false);
			return;
		}
		
		if (currentSelection instanceof IStructuredSelection)
			{ //multiple items selected	
				Object[] selections = ((IStructuredSelection)currentSelection).toArray();			
								
				int found = 0;				
				for (int i = 0; i < selections.length; i++)	
				{
					if(selections[i] == null)
						continue;
					if(selections[i] instanceof IMarker)
					{	try{
							String type = ((IMarker)selections[i]).getType();
						
							if( type.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT) ||
								type.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT) ||
								type.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT) ||
								type.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT) ||
								type.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT))									
									found++;
						}catch(Exception e){}
					}					
				}
				if(found != 0 && found == selections.length)  //supported by all selected items
					myAction.setEnabled(true);
			}
			else if (currentSelection instanceof IMarker)
			{	try{
					String type = ((IMarker) currentSelection).getType();
				
					if( type.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT) ||
						type.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT) ||
						type.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT) ||
						type.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT) ||
						type.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT)) 					
							myAction.setEnabled(true);
				}catch(Exception e){}
						
			}
			
		
	}
	
	private boolean debugTargetSupportsEditing()
	{
		//try to save a lot of processing by using last selected debug target
		if(lastSelectedTarget != null)
		{ 
			if(!lastSelectedTarget.isTerminated())
				return true;
			else
				return false;
		}	
		p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if(p==null) return false;
		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if(view==null)	return false;
	
		ISelection sel = view.getViewer().getSelection();
					
		Object element = null;		
		if (sel instanceof IStructuredSelection)
		{
			
			IStructuredSelection selection = (IStructuredSelection) sel;
			if(!selection.isEmpty() && selection.size() == 1)
			{
				element = selection.getFirstElement();
				if (element == null)
					return false;
			}
			else 			
				return false;
				
		} else
		  element = sel;
		
		//check item is a stack frame, process, or thread for live debug target	that supports editing breakpoints	  	
		if (element instanceof PICLDebugElement 
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget())!=null 
			&& !((IDebugElement)element).getDebugTarget().isTerminated() 
			&& ((IDebugElement)element).getDebugTarget().isSuspended()
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget()).getBreakpointCapabilities()!=null
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget()).getBreakpointCapabilities().breakpointModifySupported()) 
		{				
				lastSelectedTarget = (PICLDebugTarget)((IDebugElement)element).getDebugTarget();
				return true;
		}
		else if (element instanceof ILaunch) //launcher
		{
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
			if (dt!=null && dt instanceof PICLDebugTarget 
				&& !dt.isTerminated() && dt.isSuspended()
				&& ((PICLDebugTarget)dt).getBreakpointCapabilities()!=null
				&& ((PICLDebugTarget)dt).getBreakpointCapabilities().breakpointModifySupported())
			{
					lastSelectedTarget = (PICLDebugTarget)dt;
					return true;					
			}
			else return false;
		}	
		else return false;
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		
		if (! (part instanceof DebugView))
			return;
		
		lastSelectedTarget = null;		
		boolean flag = false;
		
		Object element = null;	
		
		if(sel == null)
		{
			myAction.setEnabled(false);
			return;	
		}
		
		if (sel instanceof IStructuredSelection)
		{			
			IStructuredSelection selection = (IStructuredSelection) sel;
			if(!selection.isEmpty() && selection.size() == 1)
			{
				element = selection.getFirstElement();
				if (element == null)
				{
					myAction.setEnabled(false);
					return;
				}
			}
			else 			
			{
				myAction.setEnabled(false);
				return;
			}
				
		} else
		  element = sel;
		
		//check item is a stack frame, process, or thread for live debug target	that supports editing breakpoints	  	
		if (element instanceof PICLDebugElement && !((IDebugElement)element).getDebugTarget().isTerminated() 
			&& ((IDebugElement)element).getDebugTarget().isSuspended()
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget())!=null 
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget()).getBreakpointCapabilities()!=null
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget()).getBreakpointCapabilities().breakpointModifySupported()) 
		{
			flag = true;
			lastSelectedTarget = (PICLDebugTarget)((IDebugElement)element).getDebugTarget();	
		}
		else if (element instanceof ILaunch) //launcher
		{
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
		 	
		 		 		
			if ((dt instanceof PICLDebugTarget) && !dt.isTerminated() && dt.isSuspended()
				&& ((PICLDebugTarget)dt).getBreakpointCapabilities()!=null
				&& ((PICLDebugTarget)dt).getBreakpointCapabilities().breakpointModifySupported())
			{
				 	flag = true;
				 	lastSelectedTarget = (PICLDebugTarget) dt;		
			}
			else myAction.setEnabled(false);
		}	
		else myAction.setEnabled(false);
		
		//acceptable selection in debug view. Now lets make sure breakpoint view has "good" selection.
		if (flag)
		{
			if (p == null) 	
				p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			
			if (p==null)
			{
				myAction.setEnabled(false);
				return;				
			}
			//findView() will throw null pointer if perspective still null when perspective opened
			BreakpointsView view;
			try{
				view= (BreakpointsView) p.findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);			
			}catch(Exception e){
				myAction.setEnabled(false);
				return;			
			}
			if (view != null) 
			{
				ISelection currentSelection =  view.getViewer().getSelection();
				if(currentSelection == null)
				{
					myAction.setEnabled(false);		
					return;
				}
				if (currentSelection instanceof IStructuredSelection)
				{ //multiple items selected	
					Object[] selections = ((IStructuredSelection)currentSelection).toArray();			
					int found = 0;				
					for (int i = 0; i < selections.length; i++)	
					{
						if(selections[i] == null)
							continue;
						if(selections[i] instanceof IMarker)
						{	try{
								String type = ((IMarker)selections[i]).getType();
							
								if( type.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT) ||
									type.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT) ||
									type.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT) ||
									type.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT) ||
									type.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT))									
										found++;
							}catch(Exception e){}
						}					
					}
					if(found == selections.length)  //supported by all selected items
						myAction.setEnabled(true);
				}
				else if (currentSelection instanceof IMarker)
				{	try{
						String type = ((IMarker) currentSelection).getType();
					
						if( type.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT) ||
							type.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT) ||
							type.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT) ||
							type.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT) ||
							type.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT)) 					
								myAction.setEnabled(true);
						}catch(Exception e){}
						
				}
			}
		}
		else myAction.setEnabled(false);
			
	}

}

