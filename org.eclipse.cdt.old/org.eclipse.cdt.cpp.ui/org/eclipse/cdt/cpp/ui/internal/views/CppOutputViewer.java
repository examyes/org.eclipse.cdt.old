package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.actions.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.views.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class CppOutputViewer extends OutputViewer
{
    private OpenEditorAction      _cppOpenEditorAction;
    private CancelAction 	 	  _cancelAction;
    private ShellAction			  _shellAction;
    private HistoryAction	      _backAction;
    private HistoryAction         _forwardAction;
    
  public class CancelAction extends Action
  {
    public CancelAction(String name, ImageDescriptor image)
    {
      super(name, image);

    }

      public void run()
      {
	  DataElement input = getCurrentInput();
	  if (input != null)
	      {
		  DataElement command = input.getParent();
		  cancel(command);
	      }
      }

      public void cancel(DataElement command)
      {
	  DataStore dataStore = command.getDataStore();
	  DataElement commandDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Cancel");
	  if (commandDescriptor != null)
	      {	
		  dataStore.command(commandDescriptor, command, false, true);
	      }
      }
  }

  public class ShellAction extends Action
  {
  	public ShellAction(String name, ImageDescriptor image)
  	{
  		super(name, image);
  	}
  	
  	public void run()
  	{
  		CppPlugin plugin = CppPlugin.getDefault();
  		ModelInterface api = plugin.getModelInterface();
  		IProject project = plugin.getCurrentProject();
  		if (project != null)
  		{
  			DataElement projectElement = api.findProjectElement(project);
  			if (projectElement != null)
  			{
  				api.invoke(projectElement, "sh", true);
  			}
  		}
  	}
  }	

  public class HistoryAction extends Action
  {
    private DataElement _status;
    private int         _increment;

    public HistoryAction(String name, ImageDescriptor image, int increment)
    {
      super(name, image);
      _increment = increment;
    }

      public void run()
    {
      DataElement status = _currentInput;
      if (status != null)
      {
        DataElement command = status.getParent();

        DataStore dataStore = command.getDataStore();
        DataElement logRoot = dataStore.getLogRoot();

        ArrayList commands = logRoot.getNestedData();
        int thisIndex = commands.indexOf(command);

        DataElement newStatus = null;
        int newIndex = thisIndex + _increment;
        boolean found = false;
        while (!found && (newIndex > -1) && (newIndex < commands.size()))
        {
          DataElement newCommand = (DataElement)commands.get(newIndex);	
	  String commandName = newCommand.getName();

	  if (commandName.equals("C_COMMAND") ||
	      commandName.equals("C_SEARCH") ||
	      commandName.equals("C_SEARCH_REGEX"))
	    {	
	      newStatus  = newCommand.get(newCommand.getNestedSize() - 1);
	      if (newStatus != null)
		{	
		  found = (newStatus.getNestedSize() > 1);
		}	
	    }
	  newIndex += _increment;
	}
	
        if (found && newStatus != null)
        {
          setInput(newStatus);
        }
      }
    }
  }

    public CppOutputViewer(Table parent)
    {
		super(parent, CppActionLoader.getInstance());
    }

    protected void handleDoubleSelect(SelectionEvent event)
    {
	if (_selected != null)
	    {
		if (_cppOpenEditorAction == null)
		    {
			_cppOpenEditorAction = new OpenEditorAction(_selected);
		    }
		
		_cppOpenEditorAction.setSelected(_selected);
		_cppOpenEditorAction.run();    
	    }
    }
    
    
  public void fillLocalToolBar(IToolBarManager toolBarManager)
  {
  	CppPlugin plugin = CppPlugin.getDefault();
  	
  	_shellAction = new ShellAction("Launch Shell",plugin.getImageDescriptor("command"));
    toolBarManager.add(_shellAction);
 
  	_backAction  = new HistoryAction(plugin.getLocalizedString("OutputViewer.back"),
					 plugin.getImageDescriptor("back"), -1);
    toolBarManager.add(_backAction);
    
    _forwardAction = new HistoryAction(plugin.getLocalizedString("OutputViewer.forward"),
					 plugin.getImageDescriptor("forward"), 1);
    toolBarManager.add(_forwardAction);
    
    _cancelAction = new CancelAction(plugin.getLocalizedString("OutputViewer.Cancel"),
					plugin.getImageDescriptor("cancel"));
    toolBarManager.add(_cancelAction);


    enableActions();
  } 
    
  public void enableActions()
  {
  	DataElement input = getCurrentInput();
  	if (input != null && !input.getName().equals("done"))
		{		
			_cancelAction.setEnabled(true);  	
		}
	else
		{
			_cancelAction.setEnabled(false);
		}
	
	_backAction.setEnabled(_currentInput != null);
	_forwardAction.setEnabled(_currentInput != null);
	
	CppPlugin plugin = CppPlugin.getDefault();
	IProject project = plugin.getCurrentProject();
	if (project != null && project.isOpen())
	{
		_shellAction.setEnabled(true);
	}
	else
	{
		_shellAction.setEnabled(false);
	}
  }
}
