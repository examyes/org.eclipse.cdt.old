package org.eclipse.cdt.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.widgets.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

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
import org.eclipse.swt.graphics.*;

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

import java.util.ArrayList;

public class PathsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private PathWorkbook _control;
    public PathsPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	_control = new PathWorkbook(parent, SWT.NONE);
	_control.setLayout(new FillLayout());
	
	performDefaults();
	return _control;
    }	
    
    public void performDefaults() 
    {
	CppPlugin plugin = CppPlugin.getDefault();
	ArrayList includePath        = plugin.readProperty("DefaultIncludePath");
	ArrayList externalSourcePath = plugin.readProperty("DefaultExternalSourcePath");
	ArrayList libraries          = plugin.readProperty("DefaultLibraries");

	_control.setIncludePath(includePath);
	_control.setExternalSourcePath(externalSourcePath);
	_control.setLibraries(libraries);
    }

    public boolean performOk()
    {
	ArrayList includePath        = _control.getIncludePath();
	ArrayList externalSourcePath = _control.getExternalSourcePath();
	ArrayList libraries          = _control.getLibraries();

        CppPlugin.writeProperty("DefaultIncludePath", includePath);
        CppPlugin.writeProperty("DefaultExternalSourcePath", externalSourcePath);
        CppPlugin.writeProperty("DefaultLibraries", libraries);
	return true;
    }
}
