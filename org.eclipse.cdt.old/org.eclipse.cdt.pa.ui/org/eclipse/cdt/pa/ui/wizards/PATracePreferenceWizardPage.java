package org.eclipse.cdt.pa.ui.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.ui.*;

import org.eclipse.cdt.pa.ui.*;
import org.eclipse.cdt.pa.ui.api.*;


public class PATracePreferenceWizardPage extends WizardPage implements Listener {

   private PAPlugin  _plugin = PAPlugin.getDefault();
   private Button	 _fcRealClockTimeRadio;
   private Button	 _fcCpuTimeRadio;
   private Button	 _fcSingleProcessRadio;
   private Button	 _fcForkRadio;
   private Button 	 _fcThreadRadio;
   
    
   // Constructor
   public PATracePreferenceWizardPage(String pageId) {
	 super(pageId);
	 setPageComplete(true);
   }


   public void createControl(Composite parent) {
	
	Composite composite = new Composite(parent, SWT.NULL);
	
	
	composite.addHelpListener(new HelpListener() {
		public void helpRequested(HelpEvent event)
		{
		 performHelp();
		}
	   });
	
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	PATraceTypeWizardPage traceTypePage = (PATraceTypeWizardPage)getPreviousPage();
	
	// createGprofPreferenceGroup(composite);
	
	createFunctionCheckPreferenceGroup(composite);
	
	setControl(composite);    
   }

    /**
     * Creates a new label with a bold font.
     */
   protected Label createBoldLabel(Composite parent, String text)
   {
	Label label = new Label(parent, SWT.NONE);
	label.setFont(JFaceResources.getBannerFont());
	label.setText(text);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	return label;
   }
   
   
   protected void createGprofPreferenceGroup(Composite parent) {
   
     createBoldLabel(parent, "gprof preferences:");
     
   }
   
   
   protected void createFunctionCheckPreferenceGroup(Composite parent) {
   
    createBoldLabel(parent, "FunctionCheck preferences:");
     
	Composite fcGroup = new Composite(parent, SWT.NULL);
		
	fcGroup.setLayout(new GridLayout());
	fcGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	createFCTimeModeGroup(fcGroup);
	
	createFCProfileModeGroup(fcGroup);
	
   }
   
   
   protected void createFCTimeModeGroup(Composite parent) {
   
    createBoldLabel(parent, "Time mode:");
    
	Composite timeModeGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	timeModeGroup.setLayout(layout);
	timeModeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	_fcRealClockTimeRadio = new Button(timeModeGroup, SWT.RADIO);
	_fcRealClockTimeRadio.setText("Real clock time");
	_fcRealClockTimeRadio.addListener(SWT.Selection, this);
	
	_fcCpuTimeRadio = new Button(timeModeGroup, SWT.RADIO);
	_fcCpuTimeRadio.setText("CPU time");
	_fcCpuTimeRadio.addListener(SWT.Selection, this);
	
	_fcRealClockTimeRadio.setSelection(true);    
    
   }
   
   
   protected void createFCProfileModeGroup(Composite parent) {
   
    createBoldLabel(parent, "Profile mode:");
    
	Composite profileModeGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	profileModeGroup.setLayout(layout);
	profileModeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	_fcSingleProcessRadio = new Button(profileModeGroup, SWT.RADIO);
	_fcSingleProcessRadio.setText("Single process");
	_fcSingleProcessRadio.addListener(SWT.Selection, this);
	
	_fcForkRadio = new Button(profileModeGroup, SWT.RADIO);
	_fcForkRadio.setText("Fork");
	_fcForkRadio.addListener(SWT.Selection, this);

	_fcThreadRadio = new Button(profileModeGroup, SWT.RADIO);
	_fcThreadRadio.setText("Thread");
	_fcThreadRadio.addListener(SWT.Selection, this);
	
	_fcSingleProcessRadio.setSelection(true);
	
   }
   
   
   public void handleEvent(Event ev) {
   
	Widget source = ev.widget;
	
	if (source == _fcRealClockTimeRadio && _fcRealClockTimeRadio.getSelection()) {
	
	}
	else if (source == _fcCpuTimeRadio && _fcCpuTimeRadio.getSelection()) {
	 System.out.println("CPU Time selected");
	}
	else if (source == _fcSingleProcessRadio && _fcSingleProcessRadio.getSelection()) {
	
	}
	else if (source == _fcForkRadio && _fcForkRadio.getSelection()) {
	 System.out.println("Fork selected");
	}
	else if (source == _fcThreadRadio && _fcThreadRadio.getSelection()) {
	
	}
	
   }
    
}
