package org.eclipse.cdt.linux.help.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.preferences.*;

import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

public class HelpPreferencesActionDelegate implements IViewActionDelegate
{
    public IViewPart view;
    public HelpPreferencesDialogSetting helpDialog;

    public void init(IViewPart view)
    {
	this.view = view;
    }

    public HelpPreferencesActionDelegate()
    {
	super();
    }
    
    public void run(IAction action)
    {
	helpDialog = new HelpPreferencesDialogSetting(view.getSite().getShell());
        helpDialog.open();
    }
    
    public void selectionChanged(IAction action, ISelection selection)
    {
    }


}
