package org.eclipse.cdt.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
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

public class ParserPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private ParseBehaviourControl _parseBehaviourControl;
    
    public ParserPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	Composite control = new Composite(parent, SWT.NONE);

	_parseBehaviourControl = new ParseBehaviourControl(control, SWT.NONE);

	performDefaults();
	
	control.setLayout(new GridLayout());

	return control;
    }	
    
    public void performDefaults() 
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList autoParse = plugin.readProperty("AutoParse");
	if (autoParse.isEmpty())
	    {
		_parseBehaviourControl.setAutoParseSelection(false);
	    }
	else
	    {
		String preference = (String)autoParse.get(0);
		if (preference.equals("Yes"))
		    {
			_parseBehaviourControl.setAutoParseSelection(true);
		    }
		else
		    {
			_parseBehaviourControl.setAutoParseSelection(false);
		    }
	    }

	ArrayList autoPersist = plugin.readProperty("AutoPersist");
	if (autoPersist.isEmpty())
	    {
		_parseBehaviourControl.setAutoPersistSelection(false);
	    }
	else
	    {
		String preference = (String)autoPersist.get(0);
		if (preference.equals("Yes"))
		    {
			_parseBehaviourControl.setAutoPersistSelection(true);
		    }
		else
		    {
			_parseBehaviourControl.setAutoPersistSelection(false);
		    }
	    }


    }

    public boolean performOk()
    {
	// auto parse
	ArrayList autoParse = new ArrayList();
	if (_parseBehaviourControl.getAutoParseSelection())
	    {
		autoParse.add("Yes");		
	    }
	else
	    {
		autoParse.add("No");		
	    }

	CppPlugin plugin      = CppPlugin.getDefault();
	plugin.writeProperty("AutoParse", autoParse);	


	// auto persist
	ArrayList autoPersist = new ArrayList();
	if (_parseBehaviourControl.getAutoPersistSelection())
	    {
		autoPersist.add("Yes");		
	    }
	else
	    {
		autoPersist.add("No");		
	    }

	plugin.writeProperty("AutoPersist", autoPersist);	

	return true;
    }

}
