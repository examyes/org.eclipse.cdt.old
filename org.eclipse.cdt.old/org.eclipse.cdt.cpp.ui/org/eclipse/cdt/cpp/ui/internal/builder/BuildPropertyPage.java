package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;

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
  private ArrayList _buildHistory;
  private ArrayList _cleanHistory;
  private BuildInvocationEntry _buildInvocationEntry;

  public BuildPropertyPage()
      {
        super();
        _buildHistory = new ArrayList();
        _cleanHistory = new ArrayList();
      }


  protected Control createContents(Composite parent)
      {

        String labelText = "";
        String defaultInvocation = "";
        _buildInvocationEntry = new BuildInvocationEntry(parent, 
							 "Build Invocation", "",
							 "Clean Invocation", "");

        IProject project= getProject();

        if (CppPlugin.isCppProject(project))
        {
          Control _buildInvocationEntryControl = _buildInvocationEntry.getControl();

	  // build history
          _buildHistory.removeAll(_buildHistory);
          _buildHistory = CppPlugin.readProperty(project, "Build History");
          if (_buildHistory.size() == 0)
          {
	      _buildHistory = CppPlugin.readProperty("DefaultBuildInvocation");
          }

	  for (int i = 0; i < _buildHistory.size(); i++)
	      {
		  String item = (String)_buildHistory.get(i);
		  _buildInvocationEntry.addBuild(item, i);
		  if (i == 0)
		      {
			  _buildInvocationEntry.setBuildText(item);
		      }
	      }

	  // clean history
          _cleanHistory.removeAll(_cleanHistory);
          _cleanHistory = CppPlugin.readProperty(project, "Clean History");
          if (_cleanHistory.size() == 0)
          {
	      _cleanHistory = CppPlugin.readProperty("DefaultCleanInvocation");
          }

	  for (int i = 0; i < _cleanHistory.size(); i++)
	      {
		  String item = (String)_cleanHistory.get(i);
		  _buildInvocationEntry.addClean(item, i);
		  if (i == 0)
		      {
			  _buildInvocationEntry.setCleanText(item);
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

	  // build
	  ArrayList list = CppPlugin.readProperty("DefaultBuildInvocation");
	  String defaultStr = "";
	  if (list.size() > 0)
	      {
		  defaultStr = (String)list.get(0);
	      }

	  _buildInvocationEntry.setBuildText(defaultStr);


	  // clean
	  ArrayList clist = CppPlugin.readProperty("DefaultCleanInvocation");
	  String cdefaultStr = "";
	  if (clist.size() > 0)
	      {
		  cdefaultStr = (String)clist.get(0);
	      }

	  _buildInvocationEntry.setCleanText(cdefaultStr);
      }
	
  public boolean performOk()
      {
        String toAdd = _buildInvocationEntry.getBuildText();
        int index= _buildHistory.indexOf(toAdd);

	if (index != -1)
	    {
		_buildHistory.remove(index);
	    }
	_buildHistory.add(0, toAdd);
	CppPlugin.writeProperty(getProject(), "Build History",  _buildHistory);


        String ctoAdd = _buildInvocationEntry.getCleanText();
        int cindex= _cleanHistory.indexOf(ctoAdd);

	if (cindex != -1)
	    {
		_cleanHistory.remove(cindex);
	    }
	_cleanHistory.add(0, ctoAdd);
	CppPlugin.writeProperty(getProject(), "Clean History",  _cleanHistory);


	return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }
}
