package org.eclipse.cdt.cpp.ui.internal.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import java.io.*;
import java.util.*;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.resource.*;
import org.eclipse.core.resources.*;

public class PreventableMessageBox extends MessageDialog implements SelectionListener

{
    static private boolean _showAgain = true;

    private Button _preventButton;

    public PreventableMessageBox(Shell parentShell, String dialogTitle, Image dialogTitleImage, 
				 String dialogMessage, int dialogImageType, 
				 String[] dialogButtonLabels, int defaultIndex) 
    {
	super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, 
	      dialogImageType, dialogButtonLabels, defaultIndex);
    }
    
 /*   protected Control createCustomArea(Composite parent) 
    {
	Composite cnr = new Composite(parent, SWT.NULL);
	_preventButton = new Button(cnr, SWT.CHECK);
	_preventButton.setText("Show this message again");
	_preventButton.setSelection(true);
	_preventButton.addSelectionListener(this);

	cnr.setLayout(new GridLayout());

	GridData gd= new GridData();
	gd.horizontalAlignment= GridData.CENTER;
	gd.grabExcessHorizontalSpace= false;
	gd.verticalAlignment= GridData.CENTER;
	gd.grabExcessVerticalSpace= false;
	
	cnr.setLayoutData(gd);

	return cnr;
    }*/


    public void setShowAgain(boolean show)
    {
	_showAgain = show;

	// auto parse
	ArrayList showMsg = new ArrayList();
	if (show)
	    {
		showMsg.add("Yes");		
	    }
	else
	    {
		showMsg.add("No");		
	    }

	CppPlugin plugin      = CppPlugin.getDefault();
	plugin.writeProperty(getClass().getName(), showMsg);	
    }

    public boolean getShowAgain()
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList showMsg = plugin.readProperty(getClass().getName());
	if (showMsg.isEmpty())
	    {
		_showAgain = true;
		return _showAgain;
	    }
	else
	    {
		String preference = (String)showMsg.get(0);
		if (preference.equals("Yes"))
		    {
			_showAgain = true;
			return _showAgain;
		    }
		else
		    {
			_showAgain = false;
			return _showAgain;
		    }
	    }
    }

    public static boolean openConfirm(Shell parent, String title, String message) 
    {
	if (_showAgain)
	    {
		PreventableMessageBox dialog = new PreventableMessageBox (
									  parent, 
									  title, 
									  null,	// accept the default window icon
									  message, 
									  QUESTION, 
									  new String[] {IDialogConstants.OK_LABEL, 
											IDialogConstants.CANCEL_LABEL}, 
									  0); 	// OK is the default

		boolean result = dialog.open() == 0;
		return result;
	    }
	else
	    {
		return true;
	    }
    }

    public static void openError(Shell parent, String title, String message) 
    {
	if (_showAgain)
	    {
		PreventableMessageBox dialog = new PreventableMessageBox(
									 parent,
									 title, 
									 null,	// accept the default window icon
									 message, 
									 ERROR, 
									 new String[] {IDialogConstants.OK_LABEL}, 
									 0); 	// ok is the default
		dialog.open();
	    }

	return;
    }

    public static void openInformation(
				       Shell parent,
				       String title,
				       String message) 
    {
	if (_showAgain)
	    {
		PreventableMessageBox dialog =
		    new PreventableMessageBox(parent, title, null, // accept the default window icon
					      message, INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
		// ok is the default
		dialog.open();
	    }

	return;
    }
    
    public static boolean openQuestion(Shell parent, String title, String message) 
    {
	if (_showAgain)
	    {
		PreventableMessageBox dialog = new PreventableMessageBox(
									 parent,
									 title, 
									 null,	// accept the default window icon
									 message, 
									 QUESTION, 
									 new String[] {IDialogConstants.YES_LABEL, 
										       IDialogConstants.NO_LABEL}, 
									 0); 	// yes is the default
		boolean result = dialog.open() == 0;
		return result;
	    }
	return true;
    }
    
    public static void openWarning(Shell parent, String title, String message) 
    {
	if (_showAgain)
	    {
		PreventableMessageBox dialog = new PreventableMessageBox(
									 parent,
									 title, 
									 null,	// accept the default window icon
									 message, 
									 WARNING, 
									 new String[] {IDialogConstants.OK_LABEL}, 
									 0); 	// ok is the default
		dialog.open();
	    }
	return;
    }


    public void widgetDefaultSelected(SelectionEvent e)
    {
	widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
	Widget source = e.widget;
	
	if (source == _preventButton)
	    {
		if (!_preventButton.getSelection())
		    {
			setShowAgain(false);
		    }
	    }
    }

    public int open()
    {
	if (getShowAgain())
	    {
		return super.open();
	    }
	else
	    {
		return 0;
	    }
    }
}
