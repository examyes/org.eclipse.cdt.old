package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/OverloadedDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:01:11)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Function;



/**
 * A dialog that will show a list and allow the user to pick from the list of choices.
 * This is generic enough to use for any similar situation. It was created for use with overloaded
 * functions.
 */
public class OverloadedDialog extends Dialog{

	private static final String DIALOG_SETTINGS= "OverloadedDialog";
	protected final static String PREFIX= "OverloadedDialog";

	private Function[] functionArray;
	private SelectionListener selectionListener;
	Table list;
	String choice;


	/**
	 * Constructor for AddExceptionDialog
	 */
	public OverloadedDialog(Shell parentShell){//, Function[] functions) {
		super(parentShell);
		//functionArray = functions;
	}


	/* (non-Javadoc)
 	 * Method declared in Window.
 	 * Caller can pass in PICLDebugPlugin.getActiveWorkbenchShell() as the shell.
 	 */
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(PICLUtils.getResourceString(PREFIX+".title"));
		setDefaultImage(getImage(DLG_IMG_QUESTION));
	}


	public void create() {
		super.create();
		list.setFocus();
	}
	protected Control createDialogArea(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NULL);

		Label l= new Label(parent, SWT.NULL);
		l.setText(PICLUtils.getResourceString(PREFIX+".message"));
		l = new Label(parent, SWT.NULL);
		l.setText(PICLUtils.getResourceString(PREFIX+".instruction"));

		list= new Table(parent, SWT.BORDER |SWT.SINGLE);
		list.setLayoutData(new GridData(GridData.FILL_BOTH));
		list.addSelectionListener(listListener);
		list.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				list.removeSelectionListener(listListener);
			}
		});



		GridLayout layout= new GridLayout();
		parent.setLayout(layout);

		initializeFunctionList();

		WorkbenchHelp.setHelp(getShell(),  new Object[] {PICLUtils.getHelpResourceString("OverloadedDialog")});
//		WorkbenchHelp.setHelp(list.getShell(), new Object[] { PICLUtils.getHelpResourceString("MultipleMatchesDialog.list")});

		return parent;
	}


	private void initializeFunctionList() {

		if(functionArray == null) return;

		TableItem tableItem;
		for(int i=0; i< functionArray.length; i++)
		{
			tableItem = new TableItem(list, i);
			tableItem.setText(functionArray[i].getFile().view().part().module().name() +"."+ functionArray[i].getFile().view().part().name() +"."+ functionArray[i].getDemangledName());
		}
		list.setSelection(0);

	}

	protected void okPressed() {

		choice = list.getSelection().toString();


	//	IBreakpointManager mgr= DebugPlugin.getDefault().getBreakpointManager();
		//get list of selected exceptions - may be multiple selections
	//	List selections = functionList.getSelection();

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
	//	boolean [] stateArray = functionList.getStateArray();
	/*	for (int i=0;  i < functionArray.length; i++)
		{
			if(stateArray[i])
				functionArray[i].enable();
			else functionArray[i].disable();
		}*/
	//	((PICLDebugTarget)target).commitExceptionChanges(setDefaultsButton.getSelection());
		super.okPressed();
	}

	public String getChoice()
	{
		return choice;
	}

	/**
	 * Listen for selection events on the table items to keep track of current state.
	 * Also listen for double clicks to toggle the check box state.
	 */
	private SelectionListener listListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent evt) {}

		//double click on item - same as select and hit OK
		public void widgetDefaultSelected(SelectionEvent evt) {
			okPressed();
		}
	};
}

