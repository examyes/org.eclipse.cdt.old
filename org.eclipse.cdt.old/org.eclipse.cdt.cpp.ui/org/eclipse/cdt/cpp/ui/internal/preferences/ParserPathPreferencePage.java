package org.eclipse.cdt.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.builder.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.*;

public class ParserPathPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private ParsePathControl _pathControl;

    public ParserPathPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;

	_pathControl = new ParsePathControl(parent, SWT.NONE);	
	_pathControl.setLayout(layout);

	performDefaults();
	return _pathControl;
    }	
    
    public void performDefaults() 
    {
	CppPlugin plugin = CppPlugin.getDefault();
	ArrayList paths = plugin.readProperty("DefaultParseIncludePath");
	_pathControl.setPaths(paths);
    }

    public boolean performOk()
    {
        ArrayList paths = _pathControl.getPaths();
        CppPlugin.writeProperty("DefaultParseIncludePath", paths);
	return true;
    }
}
