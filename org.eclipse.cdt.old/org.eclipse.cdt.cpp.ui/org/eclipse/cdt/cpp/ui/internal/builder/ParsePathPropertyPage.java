package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

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
			  if (!project.isOpen())
			  {
			  	_pathControl.setRemote(true);
			  }
	      }

          _pathControl.setLayout(layout);
          setCurrent();
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

  protected void setCurrent()
  {
  	IProject project = getProject();
  	 _pathControl.setPaths(CppPlugin.readProperty(project, "Include Path"));

  }

  protected void performDefaults()
   {
	  super.performDefaults();
	  _pathControl.setPaths(CppPlugin.readProperty("DefaultParseIncludePath"));
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
