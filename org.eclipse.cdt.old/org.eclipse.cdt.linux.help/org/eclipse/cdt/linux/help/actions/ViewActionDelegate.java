package com.ibm.linux.help.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.linux.help.preferences.*;

import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

public class ViewActionDelegate implements IViewActionDelegate
{
    public IViewPart view;
    public HelpDialogSetting helpDialog;

    public void init(IViewPart view)
    {
	this.view = view;
    }

    public ViewActionDelegate()
    {
	super();
    }
    
    public void run(IAction action)
    {
	helpDialog = new HelpDialogSetting(view.getSite().getShell());
	helpDialog.open();
    }
    
    public void selectionChanged(IAction action, ISelection selection)
    {
    }


}
