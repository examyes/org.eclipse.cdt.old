package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.preferences.*;

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.dstore.core.model.*;

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
    private ParseBehaviourControl _parseBehaviourControl;

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

	    _parseBehaviourControl = new ParseBehaviourControl(control, SWT.NONE);
	    
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
	 
	  ArrayList autoParsePreferences = CppPlugin.readProperty(getProject(), "AutoParse");
	  if (!autoParsePreferences.isEmpty())
	      {
		  String autoParse = (String)autoParsePreferences.get(0);
		  if (autoParse.equals("Yes"))
		      {
			  _parseBehaviourControl.setAutoParseSelection(true);
		      }
		  else
		      {
			  _parseBehaviourControl.setAutoParseSelection(false);
		      }
	      }
	  else
	      {
		  _parseBehaviourControl.setAutoParseSelection(false);
	      }

	  ArrayList autoPersistPreferences = CppPlugin.readProperty(getProject(), "AutoPersist");
	  if (!autoPersistPreferences.isEmpty())
	      {
		  String autoPersist = (String)autoPersistPreferences.get(0);
		  if (autoPersist.equals("Yes"))
		      {
			  _parseBehaviourControl.setAutoPersistSelection(true);
		      }
		  else
		      {
			  _parseBehaviourControl.setAutoPersistSelection(false);
		      }
	      }
	  else
	      {
		  _parseBehaviourControl.setAutoPersistSelection(false);
	      }
      }
    
  public boolean performOk()
      {
	  ArrayList autoParsePreferences = new ArrayList();
	  if (_parseBehaviourControl.getAutoParseSelection())
	      {
		  autoParsePreferences.add("Yes");
	      }
	  else
	      {
		  autoParsePreferences.add("No");
	      }

	  CppPlugin.writeProperty(getProject(), "AutoParse", autoParsePreferences);
	  
	  ArrayList autoPersistPreferences = new ArrayList();
	  if (_parseBehaviourControl.getAutoPersistSelection())
	      {
		  autoPersistPreferences.add("Yes");
	      }
	  else
	      {
		  autoPersistPreferences.add("No");
	      }

	  CppPlugin.writeProperty(getProject(), "AutoPersist", autoPersistPreferences);
	  

	  ModelInterface api = ModelInterface.getInstance();
	  api.setParseQuality(getProject());	

	  return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }

}
