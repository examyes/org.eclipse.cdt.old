package org.eclipse.cdt.pa.ui.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.*;


public class AddTraceProgramDialog extends org.eclipse.jface.dialogs.Dialog
    implements Listener
{
    private DataElement  _input;
    private String       _title;
    private Button		 _gprofFormatRadio;
    private Button		 _fcFormatRadio;
    private String		 _traceFormat;
    private Text		 _argumentField;
    private String		 _argument;
    
    private static String GPROF_FORMAT = "gprof";
    private static String FC_FORMAT    = "functioncheck";

    // Constructor
    public AddTraceProgramDialog(String title, DataElement input)
    {
	  super(null);
	  _input = input;
	  _title = title;
	  _traceFormat = GPROF_FORMAT;
	  _argument = null;
    }

    protected void buttonPressed(int buttonId)
    {
	  if (OK == buttonId)
	  {	
		setReturnCode(OK);
	  }
	  else if (CANCEL == buttonId)
	    setReturnCode(CANCEL);
	  else
	    setReturnCode(buttonId);
	 
	  close();
    }
    
    
    public String getTraceFormat()
    {
      return _traceFormat;
    }
    
    public String getArgument()
    {
      return _argument;
    }
    

    public Control createDialogArea(Composite parent)
    {
	  Composite c = (Composite)super.createDialogArea(parent);
	  
      Label traceFormatLabel = createBoldLabel(c, "Select the trace format:");
      
      Composite traceFormatGroup = new Composite(c, SWT.NONE);
	  GridLayout layout= new GridLayout();
	  layout.numColumns = 1;
	  traceFormatGroup.setLayout(layout);
	  traceFormatGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	  // Create the two format radio buttons
	  _gprofFormatRadio = new Button(traceFormatGroup, SWT.RADIO);
	  _gprofFormatRadio.setText("gprof");
	  _gprofFormatRadio.addListener(SWT.Selection, this);
	
	  _fcFormatRadio = new Button(traceFormatGroup, SWT.RADIO);
	  _fcFormatRadio.setText("functioncheck");
	  _fcFormatRadio.addListener(SWT.Selection, this);
	  
	  // Create the arguments group
	  Composite argumentGroup = new Composite(c, SWT.NONE);
	  
	  layout = new GridLayout();
	  layout.numColumns = 2;
	  argumentGroup.setLayout(layout);
	  
	  new Label(argumentGroup, SWT.NONE).setText("Arguments:");
	  _argumentField = new Text(argumentGroup, SWT.BORDER);
	  _argumentField.addListener(SWT.Modify, this);
	  
	  GridData data = new GridData(GridData.FILL_HORIZONTAL);
	  data.widthHint = 220;
	  _argumentField.setLayoutData(data);
	  
	  // Create the description text
	  Text description = new Text(c, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
	  description.setText("Note: The executable will be added to the Trace Files view.\n" +
	  		"You can select the appropriate actions from that view to do the run and analysis.");
	  
	  data = new GridData(GridData.GRAB_VERTICAL |GridData.FILL_BOTH);
	  data.horizontalIndent = 8;
	  data.widthHint  = 160;
	  description.setLayoutData(data);
	  
      getShell().setText(_title);
      
	  return c;
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
   
    
    public void handleEvent(Event ev)
    {
	  Widget source = ev.widget;
	
      if (source == _gprofFormatRadio && _gprofFormatRadio.getSelection()) {
	   _traceFormat = GPROF_FORMAT;
	  }
	  else if (source == _fcFormatRadio && _fcFormatRadio.getSelection()) {
	   _traceFormat = FC_FORMAT;
	  }
	  else if (source == _argumentField) {
	   _argument = _argumentField.getText();
	  }
      
    }
    
}
