package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/AddExceptionDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.13 (last modified 11/28/01 15:58:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Arrays;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.util.CheckedList;
import com.ibm.debug.internal.pdt.ui.util.StatusInfo;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLModelPresentation;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.DebuggeeException;
import com.ibm.debug.model.PersistentRestorableObjects;
import com.ibm.debug.model.SaveRestoreFlags;



/**
 * Lots of stuff commented out because pending API change in 
 * Debugger
 */
public class AddExceptionDialog extends Dialog{
	
	private static final String DIALOG_SETTINGS= "AddExceptionDialog";	
	protected final static String PREFIX= "AddExceptionDialog";		
	
	private CheckedList exceptionList;
	private DebuggeeException[] exceptionArray;
	private Button checkAllButton;
	private Button uncheckAllButton;
	private Button restoreDefaultsButton;
	private Button setDefaultsButton;
	private SelectionListener selectionListener;
	
	IDebugTarget target;
	PersistentRestorableObjects restoreObject;	 
	
	/**
	 * Constructor for AddExceptionDialog
	 */
	public AddExceptionDialog(Shell parentShell) {
		super(parentShell);
		setDefaultImage(PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_CLCL_ADD_COMPILED_EXCEPTION));
		
	}
	
	/* (non-Javadoc)
 	 * Method declared in Window.
 	 * Caller can pass in PICLDebugPlugin.getActiveWorkbenchShell() as the shell.
 	 */
	protected void configureShell(Shell shell) 
	{ 
		super.configureShell(shell);
		shell.setText(PICLUtils.getResourceString(PREFIX+".title"));
	}
	
	/**
	 * Does engine of selected target support this dialog?
	 */
	public boolean checkEngineSupport()
	{	
		target = PICLDebugPlugin.determineCurrentDebugTarget();
		//FCT bit already checked when enabling action, so no need to check again as it doesn't change
		if(target instanceof PICLDebugTarget && !target.isTerminated() && !target.isDisconnected() && target.isSuspended())
		{
		/*	restoreObject = new PersistentRestorableObjects(((PICLDebugTarget)target).getDebuggeeProcess(), SaveRestoreFlags.EXCEPTION_FILTERS,"testingExceptions");
			try{
				restoreObject.restore(true); //restore synchronously				
			}catch(Exception e){}*/
			exceptionArray = ((PICLDebugTarget)target).getSupportedExceptions();		
		}
		else
		{
			StatusInfo status = new StatusInfo();
			status.setError(PICLUtils.getResourceString(PREFIX+".unsupported"));
			ErrorDialog.openError(getShell(), PICLUtils.getResourceString("ErrorDialog.error"),null, status);
			return false;
		}	
		if(exceptionArray == null)
		{
			StatusInfo status = new StatusInfo();
			status.setError(PICLUtils.getResourceString(PREFIX+".unsupported"));
			ErrorDialog.openError(getShell(), PICLUtils.getResourceString("ErrorDialog.error"),null, status);
			return false;
		}			
		return true;
	}
		
	
	public void create() {
		super.create();
		exceptionList.selectFilterText();
		exceptionList.setFocus();
	}
	protected Control createDialogArea(Composite ancestor) {		
		Composite parent = new Composite(ancestor, SWT.NULL);
		
				
		Label l= new Label(parent, SWT.NULL);
		l.setLayoutData(new GridData());
		l.setText(PICLUtils.getResourceString(PREFIX+".label"));
		
		exceptionList= new CheckedList(parent, SWT.BORDER |SWT.CHECK, 				
				new PICLModelPresentation(),true);		
		
		GridLayout layout= new GridLayout();
		parent.setLayout(layout);		
		
		GridData gd= new GridData(GridData.FILL_BOTH);
		//gd.horizontalIndent= convertWidthInCharsToPixels(4);
		gd.widthHint= convertWidthInCharsToPixels(65);
		gd.heightHint= convertHeightInCharsToPixels(20);
		exceptionList.setLayoutData(gd);	
		
		initializeExceptionList();
		
		Composite buttonParent = new Composite(parent, SWT.NULL);
		
		layout= new GridLayout();		
		layout.numColumns = 3;		
		buttonParent.setLayout(layout);
		
		checkAllButton = new Button(buttonParent, SWT.PUSH);
		checkAllButton.setLayoutData(new GridData());
		checkAllButton.setText(PICLUtils.getResourceString(PREFIX+".checkAllButton"));
		checkAllButton.addSelectionListener(getSelectionListener());
		
		uncheckAllButton = new Button(buttonParent, SWT.PUSH);
		uncheckAllButton.setLayoutData(new GridData());
		uncheckAllButton.setText(PICLUtils.getResourceString(PREFIX+".uncheckAllButton"));
		uncheckAllButton.addSelectionListener(getSelectionListener());
		
		restoreDefaultsButton = new Button(buttonParent, SWT.PUSH);
		restoreDefaultsButton.setText(PICLUtils.getResourceString(PREFIX+".restoreDefaultsButton"));
		restoreDefaultsButton.addSelectionListener(getSelectionListener());
		
	/*	setDefaultsButton = new Button(parent, SWT.CHECK);
		setDefaultsButton.setText(PICLUtils.getResourceString(PREFIX+".setDefaultsCheckBox"));
		setDefaultsButton.setSelection(true);*/
	//	setDefaultsButton.setEnabled(false);
		
		WorkbenchHelp.setHelp(getShell(),  new Object[] {PICLUtils.getHelpResourceString("AddExceptionDialog")});
	//	WorkbenchHelp.setHelp(exceptionList.getShell(), new Object[] { PICLUtils.getHelpResourceString("AddExceptionDialog")});		
		
		return parent;
	}
	
	/**
 	  * Creates a selection listener.
 	  */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == checkAllButton) {
					exceptionList.checkAll();
				} else if (widget == uncheckAllButton) {
					exceptionList.uncheckAll();
				} else if (widget == restoreDefaultsButton) {
					restoreDefaultsPressed();
				}
			}
		};
	}

	/**
	 *  Returns this field editor's selection listener.
	 *  The listener is created if nessessary.
	 *  @return the selection listener
	 */ 
	private SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}
	
		
	private void restoreDefaultsPressed()
	{
		boolean[] doCheck = new boolean[exceptionArray.length];		
		for (int i=0;  i < exceptionArray.length; i++)
			//ask engine for default
			doCheck[i] = exceptionArray[i].defaultStateIsEnabled();
		
		exceptionList.resetChecks(doCheck);	
		exceptionList.setFocus();
	}	
	
	private boolean initializeExceptionList() {	
		boolean[] doCheck = new boolean[exceptionArray.length];		
		for (int i=0;  i < exceptionArray.length; i++)
			//ask engine for what is currently checked and check it		
			doCheck[i] = exceptionArray[i].isEnabled();		
		//pass the array to the table for selection	
		exceptionList.setElements(java.util.Arrays.asList(exceptionArray), true, doCheck); 	
		
		exceptionList.setFilter("*", true);		
		return true;
	}
	
	protected void okPressed() {
		
	//	IBreakpointManager mgr= DebugPlugin.getDefault().getBreakpointManager();
		//get list of selected exceptions - may be multiple selections
	//	List selections = exceptionList.getSelection();		
		
	//	DebuggeeException choice;
		
		//set exception breakpoint for each selected
	//	for(int i=0; i < selections.size(); i++)
	//	{
	//		choice = (DebuggeeException) selections.get(i);				
			/*try {
				IMarker marker = PICLDebugModel.createExceptionBreakpoint(result, caught, uncaught, exceptionKind == AddExceptionDialog.CHECKED_EXCEPTION);
				mgr.addBreakpoint(marker);
			} catch (DebugException exc) {
				//ExceptionHandler.handle(exc, "Add Exception", "An exception occured while adding the breakpoint");
			}*/		
		
	//	}
				
		boolean [] stateArray = exceptionList.getStateArray();
		for (int i=0;  i < exceptionArray.length; i++)
		{
			if(stateArray[i])
				exceptionArray[i].enable();
			else exceptionArray[i].disable();
		}			
		((PICLDebugTarget)target).commitExceptionChanges(true);
		/*((PICLDebugTarget)target).commitExceptionChanges(setDefaultsButton.getSelection());
		if(setDefaultsButton.getSelection())
		{
			try{
				restoreObject.save(true);  //save synchronously
			}catch(Exception e) {}	
		}*/
		super.okPressed();		
	}	
}