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

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;

import org.eclipse.cdt.pa.ui.*;
import org.eclipse.cdt.pa.ui.api.*;


public class PATraceTypeWizardPage extends WizardPage implements Listener, IPATraceListener {

   private PAPlugin	 _plugin;
   private PAModelInterface  _api;
   private Button    _traceFileRadio;
   private Button	 _traceProgramRadio;
   private Button	 _autoFormatRadio;
   private Button	 _gprofFormatRadio;
   private Button	 _fcFormatRadio;
   private Text		 _targetPathField;
   private Button	 _targetPathBrowseButton;
   private Text		 _argumentField;
   
   private int       _traceType;
   private String  	 _traceFormat;
   private String	 _realTraceFormat;
   private String	 _detectedTraceFormat;
   private String	 _argument;
   private DataElement _traceElement;
   
   private static String AUTO_FORMAT  = "auto";
   private static String GPROF_FORMAT = "gprof";
   private static String FC_FORMAT    = "functioncheck";
   
   private static final int SIZING_TEXT_FIELD_WIDTH = 150;
   
   
   // Constructor
   public PATraceTypeWizardPage(String pageId) {
	 
	 super(pageId);
	 
	 _plugin = PAPlugin.getDefault();
	 _api = PAModelInterface.getInstance();
	 _api.getTraceNotifier().addTraceListener(this);
	 _traceType = PAResource.TRACE_FILE;
	 _traceFormat = AUTO_FORMAT;
	 _realTraceFormat = AUTO_FORMAT;
	 _detectedTraceFormat = null;
	 _traceElement = null;
	 _argument = null;
	 setPageComplete(false);
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
	
	createTraceTypeGroup(composite);
	
	createTraceFormatGroup(composite);
	
	createTargetPathGroup(composite);
	
	setControl(composite);    
   }

   
   protected void createTraceTypeGroup(Composite parent) {
   
    Label traceTypeLabel = createBoldLabel(parent, "Trace type:");
    
	Composite traceTypeGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	traceTypeGroup.setLayout(layout);
	traceTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	_traceFileRadio = new Button(traceTypeGroup, SWT.RADIO);
	_traceFileRadio.setText("Trace file");
	_traceFileRadio.addListener(SWT.Selection, this);

	createTextGroup(traceTypeGroup, "Select this option if you want to parse the profile output saved in a text file.");
	
	_traceProgramRadio = new Button(traceTypeGroup, SWT.RADIO);
	_traceProgramRadio.setText("Trace program");
	_traceProgramRadio.addListener(SWT.Selection, this);

	createTextGroup(traceTypeGroup, "Select this option if you want to analyze an executable.");
	
	_traceFileRadio.setSelection(true);
   }
   
   
   private void createTextGroup(Composite parent, String text) {
   
	Composite textGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	textGroup.setLayout(layout);
	textGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Label textLabel = new Label(textGroup, SWT.NONE);
    textLabel.setText(text);
   }
   
   
   protected void createTraceFormatGroup(Composite parent) {
   
    Label traceFormatLabel = createBoldLabel(parent, "Trace format:");
    
	Composite traceFormatGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	traceFormatGroup.setLayout(layout);
	traceFormatGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
	_autoFormatRadio = new Button(traceFormatGroup, SWT.RADIO);
	_autoFormatRadio.setText("Auto detected");
	_autoFormatRadio.addListener(SWT.Selection, this);
	
	_gprofFormatRadio = new Button(traceFormatGroup, SWT.RADIO);
	_gprofFormatRadio.setText("gprof");
	_gprofFormatRadio.addListener(SWT.Selection, this);
	
	_fcFormatRadio = new Button(traceFormatGroup, SWT.RADIO);
	_fcFormatRadio.setText("functioncheck");
	_fcFormatRadio.addListener(SWT.Selection, this);	
   
    _autoFormatRadio.setSelection(true);
   }
   
   
   protected void createTargetPathGroup(Composite parent) {
   
	Composite targetPathGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	targetPathGroup.setLayout(layout);
	targetPathGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Label targetPathLabel = new Label(targetPathGroup, SWT.NONE);
    targetPathLabel.setText("Target Path:");
    
    _targetPathField = new Text(targetPathGroup, SWT.BORDER);
	_targetPathField.addListener(SWT.Modify, this);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	_targetPathField.setLayoutData(data);
	_targetPathField.setEditable(false);
	
	_targetPathBrowseButton = new Button(targetPathGroup, SWT.PUSH);
	_targetPathBrowseButton.setText("Browse...");
	_targetPathBrowseButton.addListener(SWT.Selection, this);
	_targetPathBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	
	new Label(targetPathGroup, SWT.NONE).setText("Arguments:");
	
	_argumentField = new Text(targetPathGroup, SWT.BORDER);
	_argumentField.addListener(SWT.Modify, this);
	
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	data.horizontalSpan = 2;
	_argumentField.setLayoutData(data);
	
	_argumentField.setEditable(false);
	
   }
   
 
   // From IPATraceListener
   public void traceChanged(PATraceEvent event) {
   
     DataElement object = event.getObject();
     DataElement argument = event.getArgument();
     int type = event.getType();
     
     switch (type) {
      
       case PATraceEvent.FORMAT_CHANGED:
       
         String format = object.getValue();
         if (format.equals("invalid trace file")) {
           setErrorMessage("Not a valid trace file: " + argument.getSource());
           _detectedTraceFormat = null;
           setPageComplete(false);
         }
         else if (format.equals("invalid trace program")) {
           setErrorMessage("Not a valid trace program: " + argument.getSource());
           _detectedTraceFormat = null;
           setPageComplete(false);
           _argumentField.setEditable(false);
         }
         else {
           setErrorMessage(null);
           setMessage("Detected trace format: " + format);
           _realTraceFormat = format;
           _detectedTraceFormat = format;
           setPageComplete(true);
           
           if (_traceType == PAResource.TRACE_PROGRAM) {
             _argumentField.setEditable(true);
           }
           
         }         
         break;
         
       default:
         break;
     }
     
   }
    
 
   public int getTraceType() {
     return _traceType;
   }
   
   
   public String getTraceFormat() {
     return _realTraceFormat;
   }
   
   
   public DataElement getTraceTarget() {
     return _traceElement;
   }
   
   
   public boolean finish() {
   
     if (_traceType == PAResource.TRACE_FILE) {
       _api.addTraceFile(_traceElement, _realTraceFormat);
     }
     else {
      _api.addTraceProgram(_traceElement, _realTraceFormat, _argument);
     }
     
     return true;
   }
   
   
   public void dispose()
   {
     _api.getTraceNotifier().removeTraceListener(this);
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
   
   
   public void handleEvent(Event ev) {
   
	Widget source = ev.widget;
	
	if (source == _traceFileRadio && _traceFileRadio.getSelection()) {
	 handleTraceTypeChanged(PAResource.TRACE_FILE);
	}
	else if (source == _traceProgramRadio && _traceProgramRadio.getSelection()) {
	 handleTraceTypeChanged(PAResource.TRACE_PROGRAM);
	}
	else if (source == _autoFormatRadio && _autoFormatRadio.getSelection()) {
	 handleTraceFormatChanged(AUTO_FORMAT);
	}
	else if (source == _gprofFormatRadio && _gprofFormatRadio.getSelection()) {
	 handleTraceFormatChanged(GPROF_FORMAT);
	}
	else if (source == _fcFormatRadio && _fcFormatRadio.getSelection()) {
	 handleTraceFormatChanged(FC_FORMAT);
	}
	else if (source == _targetPathField) {
	 // System.out.println("Text field modified");
	 handleTargetPathChanged();
	}
	else if (source == _targetPathBrowseButton) {
	 handleTargetPathBrowseButtonPressed();
	}
	else if (source == _argumentField) {
	 _argument = _argumentField.getText();
	}
	
   }
   
   
   private void handleTraceTypeChanged(int newTraceType) {
   
    _traceType = newTraceType;
    
    String targetName = _targetPathField.getText();
    if (targetName != null && targetName.trim().length() > 0) {     
      handleTargetPathChanged();
    }
    
   }
   
   
   private boolean match(String detectedFormat, String newFormat) {
   
    if (newFormat.equals(AUTO_FORMAT))
     return false;
    else if (detectedFormat == null)
     return false;
    else if (detectedFormat.indexOf("gprof") >= 0 && newFormat.indexOf("gprof") >= 0)
     return true;
    else
     return detectedFormat.equals(newFormat);
     
   }
   
   private void handleTraceFormatChanged(String newFormat) {
       
    _traceFormat     = newFormat;
    _realTraceFormat = newFormat;

    String targetName = _targetPathField.getText();
    if (targetName != null && targetName.trim().length() > 0) {     
      handleTargetPathChanged();
    }
    
   }
   
   
   protected void handleTargetPathChanged() {
       
    if (_traceType == PAResource.TRACE_FILE) { 
      
      _argumentField.setEditable(false);
      
      if (_traceFormat.equals(AUTO_FORMAT)) {
        _api.queryTraceFileFormat(_traceElement);
        setMessage("Detecting trace format...");
      }
      else {
        setErrorMessage(null);
        setPageComplete(true);
        
        if (!match(_detectedTraceFormat, _traceFormat)) {
         setMessage("Be warned that you might encounter a parse problem if the trace format is not correct");
        
        }
      }
      
    }
    else if (_traceType == PAResource.TRACE_PROGRAM ) {
    
      if (_traceElement.isOfType("binary executable")) {
        if (_traceFormat.equals(AUTO_FORMAT)) {
         _api.queryTraceProgramFormat(_traceElement);
         setMessage("Detecting trace format...");
        }
        else {
         setErrorMessage(null);
         setPageComplete(true);
         _argumentField.setEditable(true);
         
         if (!match(_detectedTraceFormat, _traceFormat)) {
          setMessage("Be warned that you might encounter a parse problem if the trace format is not correct");
         }
         
        }
      }
      else {
        _detectedTraceFormat = null;
        setErrorMessage("Not a platform executable: " + _traceElement.getSource());
        setPageComplete(false);
        _argumentField.setEditable(false);
      }
    }
        
   }
      
   
   protected void handleTargetPathBrowseButtonPressed() {

	ModelInterface api = ModelInterface.getInstance();
	
	String title = null;
	if (_traceType == PAResource.TRACE_FILE) {
	 title = "Select a trace file";
	}
	else {
	 title = "Select a trace program";
	}
	
	ChooseProjectDialog dlg = new ChooseProjectDialog(title, api.findWorkspaceElement());
	dlg.useFilter(true);
	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)  {
	 
	 java.util.List selections = dlg.getSelected();
	 
	 if (selections.size() > 0) {
	 
	  DataElement selected = (DataElement)selections.get(0);
	  if (selected.isOfType("file")) {
	    // System.out.println("file selected: " + selected);
	    _traceElement = selected;
	    _targetPathField.setText(selected.getSource());
	  }
	 }
	  
	}	 	
   }
    
}
