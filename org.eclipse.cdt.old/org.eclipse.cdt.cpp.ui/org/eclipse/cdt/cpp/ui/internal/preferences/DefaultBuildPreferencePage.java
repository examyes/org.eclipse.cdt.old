package com.ibm.cpp.ui.internal.preferences;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.wizards.*;
import com.ibm.cpp.ui.internal.*;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

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

public class DefaultBuildPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private BuildInvocationEntry _buildInvocationEntry;
    private CppPlugin _plugin;

    public DefaultBuildPreferencePage() 
    {
	_plugin = CppPlugin.getDefault();
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {		
        String labelText = "";
        String defaultInvocation = "";
	String buildLabel = _plugin.getLocalizedString("BuildPreferences.Default_Build_Invocation");
        String cleanLabel = "Clean Invocation";
        _buildInvocationEntry = new BuildInvocationEntry(parent, 
							 buildLabel, "",
							 cleanLabel, "");

	Control buildInvocationEntryControl = _buildInvocationEntry.getControl();

	performDefaults();
	return buildInvocationEntryControl;
    }	
    
    public void performDefaults() 
    {
	// build 
	ArrayList history = _plugin.readProperty("DefaultBuildInvocation");
	if ((history != null) && (history.size() > 0))
	    {
		String defaultStr = (String)history.get(0);
		_buildInvocationEntry.setBuildText(defaultStr);
		_buildInvocationEntry.addBuild(defaultStr, 0);
	    }
	else
	    {
		_buildInvocationEntry.setBuildText("gmake all");
		_buildInvocationEntry.addBuild("gmake all", 0);
	    }

	// clean 
	ArrayList chistory = _plugin.readProperty("DefaultCleanInvocation");
	if ((chistory != null) && (chistory.size() > 0))
	    {
		String defaultStr = (String)chistory.get(0);
		_buildInvocationEntry.setCleanText(defaultStr);
		_buildInvocationEntry.addClean(defaultStr, 0);
	    }
	else
	    {
		_buildInvocationEntry.setCleanText("gmake clean");
		_buildInvocationEntry.addClean("gmake clean", 0);
	    }
    }

    public boolean performOk()
    {
	ArrayList list = new ArrayList(); 
	list.add(new String(_buildInvocationEntry.getBuildText()));
	_plugin.writeProperty("DefaultBuildInvocation", list);

	ArrayList clist = new ArrayList(); 
	clist.add(new String(_buildInvocationEntry.getCleanText()));
	_plugin.writeProperty("DefaultCleanInvocation", clist);
	return true;
    }
}
