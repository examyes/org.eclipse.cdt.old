package com.ibm.cpp.ui.internal.preferences;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.wizards.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

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

public class ViewPreferenceControl extends Composite implements Listener
{
    private Button  _browseButton1;
    private RGB     _colour1;

    private Button  _browseButton2;
    private RGB     _colour2;

    private Button  _browseButton3;
    private FontData  _font;
    private List     _canvas;

    public ViewPreferenceControl(Composite cnr, int style) 
    {
	super(cnr, style);

	CppPlugin plugin = CppPlugin.getDefault();

	_colour1 = new RGB(255,255,255);
	_colour2 = new RGB(0,0,0);

	Composite superControl = new Composite(this, SWT.NONE);

	Group control = new Group(superControl, SWT.NONE);
	control.setText(plugin.getLocalizedString("ViewPreferences.View_Settings"));
	
	// background
	Composite background = new Composite(control, SWT.NONE);
	Label label1 = new Label(background, SWT.NONE);
	label1.setText(plugin.getLocalizedString("ViewPreferences.Background_Color"));

	_browseButton1 = new Button(background, SWT.PUSH);
        _browseButton1.setText(plugin.getLocalizedString("ViewPreferences.Change"));
	_browseButton1.addListener(SWT.Selection, this);

	background.setLayout(new GridLayout());	

	// foreground
	Composite foreground = new Composite(control, SWT.NONE);
	Label label2 = new Label(foreground, SWT.NONE);
	label2.setText(plugin.getLocalizedString("ViewPreferences.Text_Color"));

	_browseButton2 = new Button(foreground, SWT.PUSH);
        _browseButton2.setText(plugin.getLocalizedString("ViewPreferences.Change"));
	_browseButton2.addListener(SWT.Selection, this);
	foreground.setLayout(new GridLayout());	

	// font
	Composite viewFont = new Composite(control, SWT.NONE);
	Label label3 = new Label(viewFont, SWT.NONE);
	label3.setText(plugin.getLocalizedString("ViewPreferences.Font"));

	_browseButton3 = new Button(viewFont, SWT.PUSH);
        _browseButton3.setText(plugin.getLocalizedString("ViewPreferences.Change"));
	_browseButton3.addListener(SWT.Selection, this);

       	viewFont.setLayout(new GridLayout());	
	
	control.setLayout(new RowLayout());

	_canvas = new List(superControl, SWT.BORDER);	
	_canvas.add(plugin.getLocalizedString("ViewPreferences.Sample"));
	GridData data1 = new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
	data1.heightHint = 80;
	_canvas.setLayoutData(data1);

	superControl.setLayout(new GridLayout());
    }

    public void setBackground(RGB colour)
    {
	_canvas.setBackground(new Color(_canvas.getDisplay(), colour));
    }
    
    public void setForeground(RGB colour)
    {
	_canvas.setForeground(new Color(_canvas.getDisplay(), colour));
    }

    public void setFont(String fontStr)
    {
	_font = new FontData(fontStr);
	_canvas.setFont(new Font(_canvas.getDisplay(), _font) );			
    }

    public void setFont(Font font)
    {
	_font = font.getFontData()[0];
	_canvas.setFont(font);
    }

    public FontData getFontData()
    {
	return _font;
    }

    public RGB getColour1()
    {
	return _colour1;
    }

    public RGB getColour2()
    {
	return _colour2;
    }

    public void handleEvent(Event e)
    {
       Widget source = e.widget;
       if (source == _browseButton1)
	   {
	       ColorDialog dialog = new ColorDialog(_browseButton1.getShell(), SWT.NONE);
	       dialog.open();
	       RGB rgb = dialog.getRGB();
	       if (rgb != null)
		   {
		       _colour1 = rgb;
		       _canvas.setBackground(new Color(_canvas.getDisplay(), _colour1));
		   }
	   }
       else if (source == _browseButton2)
	   {
	       ColorDialog dialog = new ColorDialog(_browseButton2.getShell(), SWT.NONE);
	       dialog.open();
	       RGB rgb = dialog.getRGB();
	       if (rgb != null)
		   {
		       _colour2 = rgb;
		       _canvas.setForeground(new Color(_canvas.getDisplay(), _colour2));
		   }
	   }
       else if (source == _browseButton3)
	   {
	       FontDialog dialog = new FontDialog(_browseButton3.getShell(), SWT.NONE);
	       FontData data = dialog.open();
	       if (data != null)
		   {
		       _font = data;
		       _canvas.setFont(new Font(_canvas.getDisplay(), _font));
		   }
	   }
    }
}
