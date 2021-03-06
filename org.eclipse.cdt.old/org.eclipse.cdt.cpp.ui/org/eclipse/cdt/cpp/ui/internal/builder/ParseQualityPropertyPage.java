package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
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

public class ParseQualityPropertyPage extends PropertyPage
{	
  private ParseQualityControl _qualityControl;

  public ParseQualityPropertyPage()
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
	    _qualityControl = new ParseQualityControl(parent, SWT.NONE);	
	    FillLayout layout2 = new FillLayout();
	    _qualityControl.setLayout(layout2);	
	    
		setCurrent();
	    return _qualityControl;
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


  protected void setCurrent()
  {
  	  IProject project = getProject();
	  ArrayList preferences = CppPlugin.readProperty(project, "ParseQuality");
	    if (!preferences.isEmpty())
		{
		    String preference = (String)preferences.get(0);
		    _qualityControl.setSelection(Integer.parseInt(preference));		    
		}
	    else
		{
		    _qualityControl.setSelection(3);
		}

  }
  
  protected void performDefaults()
      {
	  super.performDefaults();
	  	ArrayList preferences = CppPlugin.readProperty("ParseQuality");
	    if (!preferences.isEmpty())
		{
		    String preference = (String)preferences.get(0);
		    _qualityControl.setSelection(Integer.parseInt(preference));		    
		}
	    else
		{
		    _qualityControl.setSelection(3);
		}
      }
	
  public boolean performOk()
      {
	  int quality = _qualityControl.getSelection();
	  ArrayList preferences = new ArrayList();
	  preferences.add("" + quality);
	  CppPlugin.writeProperty(getProject(), "ParseQuality", preferences);
	  
	  ModelInterface api = ModelInterface.getInstance();
	  api.setParseQuality(getProject());
	
	  return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }

}
