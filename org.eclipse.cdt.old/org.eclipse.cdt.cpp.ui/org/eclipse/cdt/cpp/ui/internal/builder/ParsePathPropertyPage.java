package com.ibm.cpp.ui.internal.builder;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.vcm.*;

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

public class ParsePathPropertyPage extends PropertyPage
{	
  private ParsePathControl _pathControl;

  public ParsePathPropertyPage()
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
          _pathControl = new ParsePathControl(parent, SWT.NONE);	
	  if (project instanceof Repository)
	      {
		  _pathControl.setContext(project);
	      }

          _pathControl.setPaths(CppPlugin.readProperty(project, "Include Path"));
          _pathControl.setLayout(layout);
          return _pathControl;
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
      }
	
  public boolean performOk()
      {
        ArrayList paths = _pathControl.getPaths();
        CppPlugin.writeProperty(getProject(), "Include Path", paths);

	ModelInterface api = ModelInterface.getInstance();
	api.setParseIncludePath(getProject());
	
        return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }

}
