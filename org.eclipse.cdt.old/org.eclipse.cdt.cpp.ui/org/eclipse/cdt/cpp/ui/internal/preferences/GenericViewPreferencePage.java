package org.eclipse.cdt.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.*;
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

public class GenericViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private ViewPreferenceControl _control;
    public GenericViewPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	_control = new ViewPreferenceControl(parent, SWT.NONE);
	_control.setLayout(new FillLayout());

	performDefaults();	
	return _control;
    }	
    
    public void readColour(String property, RGB colour, boolean background)
    {
	CppPlugin plugin = CppPlugin.getDefault();

	// background
	ArrayList colours = plugin.readProperty(property);
	if (colours.size() == 3)
	  {	    
	      String rStr = (String)colours.get(0);
	      String gStr = (String)colours.get(1);
	      String bStr = (String)colours.get(2);
	      
	      colour.red = new Integer(rStr).intValue();
	      colour.green = new Integer(gStr).intValue();
	      colour.blue = new Integer(bStr).intValue();

	      if (background)
		  {
		      _control.setBackground(colour);
		  }
	      else
		  {
		      _control.setForeground(colour);
		  }
	  }
	else
	  {
	      if (background)
		  {
		      _control.setBackground(colour);
		  }
	      else
		  {
		      _control.setForeground(colour);
		  }
	  }		
    }

    public void writeColour(String property, RGB colour)
    {
	CppPlugin plugin = CppPlugin.getDefault();
	int r = colour.red;
	int g = colour.green;
	int b = colour.blue;
	
	ArrayList colours = new ArrayList(3);
	colours.add("" + r);
	colours.add("" + g);
	colours.add("" + b);

	plugin.writeProperty(property, colours);
    }

    public void readFont()
    {
	String property = new String("ViewFont");

	CppPlugin plugin = CppPlugin.getDefault();
	ArrayList fontArray = plugin.readProperty(property);
	if (fontArray.size() > 0)
	    {
		String fontStr = (String)fontArray.get(0);
		fontStr = fontStr.replace(',', '|');
		_control.setFont(fontStr);
	    }
    }

    public void writeFont()
    {
	String property = new String("ViewFont");

	CppPlugin plugin = CppPlugin.getDefault();

	ArrayList fontArray = new ArrayList();

	FontData font = _control.getFontData();
	if (font != null)
	    {
		String fontStr = font.toString();
		fontStr = fontStr.replace('|', ',');
		fontArray.add(fontStr);
		
		plugin.writeProperty(property, fontArray);
	    }
    }

    public void performDefaults() 
    {
	readColour("ViewBackground", _control.getColour1(), true);
	readColour("ViewForeground", _control.getColour2(), false);
	readFont();
    }

    public boolean performOk()
    {
	writeColour("ViewBackground", _control.getColour1());
	writeColour("ViewForeground", _control.getColour2());
	writeFont();

	CppPlugin plugin = CppPlugin.getDefault();
	CppProjectNotifier notifier = plugin.getModelInterface().getProjectNotifier();
	notifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.VIEW_CHANGE, null));

	return true;
    }

}
