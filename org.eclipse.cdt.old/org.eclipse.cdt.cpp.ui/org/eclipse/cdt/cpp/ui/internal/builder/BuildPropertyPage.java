package com.ibm.cpp.ui.internal.builder;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.wizards.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import org.eclipse.core.resources.*;

import java.io.*;
import java.util.*;

import org.eclipse.ui.dialogs.*;

public class BuildPropertyPage extends PropertyPage
{	
  private ArrayList _history;
  private BuildInvocationEntry _buildInvocationEntry;

  public BuildPropertyPage()
      {
        super();
        _history = new ArrayList();
      }


  protected Control createContents(Composite parent)
      {

        String labelText = "";
        String defaultInvocation = "";
        _buildInvocationEntry = new BuildInvocationEntry(parent, "Build Invocation", "");

        IProject project= getProject();

        if (CppPlugin.isCppProject(project))
        {
          Control _buildInvocationEntryControl = _buildInvocationEntry.getControl();

          _history.removeAll(_history);
          _history = CppPlugin.readProperty(project, "Build History");
          if (_history.size() == 0)
          {
	      _history = CppPlugin.readProperty("DefaultBuildInvocation");
          }

	  for (int i = 0; i < _history.size(); i++)
	      {
		  String item = (String)_history.get(i);
		  _buildInvocationEntry._invocation.add(item, i);
		  if (i == 0)
		      {
			  _buildInvocationEntry._invocation.setText(item);
		      }
	      }
	  return _buildInvocationEntryControl;
        }
        else
        {
          Composite cnr = new Composite(parent, SWT.NONE);
          Label label = new Label(cnr, SWT.NULL);
          label.setText("Not a C/C++ Project");
          return cnr;
        }
      }

  protected void performDefaults()
      {
	  super.performDefaults();

	  ArrayList list = CppPlugin.readProperty("DefaultBuildInvocation");
	  String defaultStr = "";
	  if (list.size() > 0)
	      {
		  defaultStr = (String)list.get(0);
	      }

	  _buildInvocationEntry.setText(defaultStr);
      }
	
  public boolean performOk()
      {
        Combo  _invocation = _buildInvocationEntry._invocation;
        String toAdd = _invocation.getText();

        int index= _history.indexOf(toAdd);

	if (index != -1)
	    {
		_history.remove(index);
	    }
	_history.add(0, toAdd);
	CppPlugin.writeProperty(getProject(), "Build History",  _history);
	return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }
}
