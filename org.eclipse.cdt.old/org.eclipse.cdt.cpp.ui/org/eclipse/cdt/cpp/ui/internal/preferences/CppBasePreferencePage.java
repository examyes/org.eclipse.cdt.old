package com.ibm.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CppBasePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage 
{
    public CppBasePreferencePage() 
    {
	super(GRID);
    }

    public static void initDefaults(IPreferenceStore store) 
    {
    }

    protected void createFieldEditors() 
    {
    }
    
    public void init(IWorkbench workbench) 
    {
    }
	
}
