package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.preferences.*;
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

public class EnvironmentPropertyPage extends PropertyPage
{	
    private Composite             _environmentCanvas;
    private NameValueTableControl _variableControl;
    private NameValueTableControl _systemVariableControl;

    public EnvironmentPropertyPage()
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
	    _environmentCanvas = new Composite(parent, SWT.NONE);
	    GridLayout glayout = new GridLayout();
	    glayout.numColumns = 1;
	    _environmentCanvas.setLayout(glayout);
	    GridData dp2 = new GridData(GridData.FILL_BOTH);
	    _environmentCanvas.setLayoutData(dp2);
	    
	    _variableControl = new NameValueTableControl(_environmentCanvas, SWT.NONE);	
	    FillLayout layout1 = new FillLayout();
	    _variableControl.setLayout(layout1);	

	    _systemVariableControl = new NameValueTableControl(_environmentCanvas, SWT.NONE, "System Environment", false); 
	    FillLayout layout2 = new FillLayout();	
	    _systemVariableControl.setLayout(layout2);	
	    
	    performDefaults();
	    return _variableControl;
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
	  IProject project = getProject();
	  _variableControl.setVariables(CppPlugin.readProperty(project, "Environment"));

	  DataStore dataStore = CppPlugin.getDataStore();
	  if (project instanceof Repository)
	      {
		  dataStore = ((Repository)project).getDataStore();
	      }

	DataElement systemInfo = dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.environment.EnvironmentMiner");
	DataElement systemEnvironment = systemInfo.get(0);
	
	ArrayList svariables = new ArrayList();
	for (int i = 0; i < systemEnvironment.getNestedSize(); i++)
	    {
		DataElement var = (DataElement)systemEnvironment.get(i);
		svariables.add(var.getName());
	    }

	_systemVariableControl.setVariables(svariables);
      }
	
  public boolean performOk()
      {
        ArrayList variables = _variableControl.getVariables();
        CppPlugin.writeProperty(getProject(), "Environment", variables);

	ModelInterface api = ModelInterface.getInstance();
	api.setEnvironment(getProject());
	
        return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }

}
