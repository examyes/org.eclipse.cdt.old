package com.ibm.cpp.ui.internal.builder;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.preferences.*;

import com.ibm.cpp.ui.internal.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.*;

import org.eclipse.ui.dialogs.*;

public class AutoParsePropertyPage extends PropertyPage
{	
    private Button _autoParseButton;

  public AutoParsePropertyPage()
      {
        super();
      }


  protected Control createContents(Composite parent)
      {
        IProject project= getProject();
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;

        if (CppPlugin.isCppProject(project))
        {
	    Composite control = new Composite(parent, SWT.NONE);

	    _autoParseButton = new Button(control, SWT.CHECK);
	    _autoParseButton.setText("Perform parse automatically on resource modification");
	    
	    control.setLayout(new GridLayout());
	    performDefaults();
	    return control;
        }
        else
        {
          Composite cnr = new Composite(parent, SWT.NONE);
          Label label = new Label(cnr, SWT.NULL);
          label.setText("Not a C/C++ Project");
          cnr.setLayout(layout);
          cnr.setLayoutData(new GridData(GridData.FILL_BOTH));
          return cnr;
        }
      }


  protected void performDefaults()
      {
	  super.performDefaults();	  
	  
	  ArrayList preferences = CppPlugin.readProperty(getProject(), "AutoParse");
	  if (!preferences.isEmpty())
	      {
		  String autoParse = (String)preferences.get(0);
		  if (autoParse.equals("Yes"))
		      {
			  _autoParseButton.setSelection(true);
		      }
		  else
		      {
			  _autoParseButton.setSelection(false);
		      }
	      }
	  else
	      {
		  _autoParseButton.setSelection(false);
	      }
      }
    
  public boolean performOk()
      {
	  ArrayList preferences = new ArrayList();
	  if (_autoParseButton.getSelection())
	      {
		  preferences.add("Yes");
	      }
	  else
	      {
		  preferences.add("No");
	      }

	  CppPlugin.writeProperty(getProject(), "AutoParse", preferences);
	  
	  ModelInterface api = ModelInterface.getInstance();
	  api.setParseQuality(getProject());	

	  return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }

}
