package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.miners.managedproject.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerAction;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;


public class ConfigureAction extends CustomAction
{ 
	public class RunThread extends Handler
	{
		private DataElement _subject;
		private DataElement _status;
		
		public RunThread(DataElement subject, DataElement status)
		{
			_subject = subject;
			_status = status;
		}
		
		public void handle()
		{
			if (_status.getName().equals("done"))
			{
				_subject.refresh(false);
				finish();
			}		
		}
	}
	
	public ConfigureAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		
		if(_command.getValue().equals("CREATE_CONFIGURE")||_command.getValue().equals("RUN_CONFIGURE")||
		_command.getValue().equals("UPDATE_AUTOCONF_FILES")||_command.getValue().equals("UPDATE_CONFIGURE_IN"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
		//enable disable based on object files
		
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES")&& !projectHasSubdir())
				setEnabled(false);		
		
		if((_command.getValue().equals("UPDATE_MAKEFILE_AM")&&!doesFileExists("Makefile.am")))
				setEnabled(false);
				
		if (subject.getType().equals("Project"))	
			if((_command.getValue().equals("UPDATE_CONFIGURE_IN")&&!doesFileExists("configure.in")))
				setEnabled(false);
				
		if(_command.getValue().equals("CREATE_CONFIGURE")
			&&(!doesFileExists("configure.in") || !doesFileExists("Makefile.in")))
				setEnabled(false);
						
		if(_command.getValue().equals("RUN_CONFIGURE")&&!doesFileExists("configure") )
				setEnabled(false);		
		
	}
    public void run()
	{
		boolean execute = true;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
		{
			if(doesAutoconfSupportExist())
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Trying to update existing configure.in and makefile.am's "+
				"\nconfigure.in and or Makefile.am Files will be generated if missing"
					+"\nIf updated then old configure.in and Makefile.am's will be renamed *.old");
				//dialog.openInformation(shell,"Updating configure.in and Makefile.am's ",message);
				execute = dialog.openConfirm(shell,"Updating configure.in and Makefile.am's ",message);
			}
		}	
		if(_command.getValue().equals("UPDATE_MAKEFILE_AM"))
		{
			if(doesFileExists("Makefile.am"))
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Trying to update existing makefile.am's"+
					"\nIf updated then old Makefile.am's will be renamed *.old");
				//dialog.openInformation(shell,"Updating Makefile.am's ",message);
				execute = dialog.openConfirm(shell,"Updating Makefile.am's ",message);
			}
		}	
		if(_command.getValue().equals("UPDATE_CONFIGURE_IN"))
		{
			if(doesFileExists("configure.in"))
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Trying to update existing configure.in "+
					"\nIf updated then old configure.in shall be renamed *.old");
				//dialog.openInformation(shell,"Updating configure.in",message);
				execute = dialog.openConfirm(shell,"Updating configure.in",message);
			}
		}	
		if(execute)
		{			
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());			
			DataElement status = _dataStore.command(configureCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.monitorStatus(status);			
			api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}
	}
	private boolean doesFileExists(String fileName)
	{
		for (int i = 0; i < _subject.getNestedSize(); i++)
		    {
			DataElement child = _subject.get(i).dereference();
			if (!child.isDeleted() && child.getName().equals(fileName))
			    {
					return true;
			    }
		    }
		return false;
	}
    private boolean projectHasSubdir()
    {
    	return projectHasSubdirHelper(_subject);
    }    
    private boolean projectHasSubdirHelper(DataElement root)
	{
		for (int i = 0; i < root.getNestedSize(); i++)
		{
			DataElement child = root.get(i).dereference();
			String type = child.getType();
			if (type.equals("Project") || type.equals("directory"))
				return true;
		}
		return false;
	}


    private boolean doesAutoconfSupportExist()
    {
	return doesAutoconfSupportExistHelper(_subject);
    }
    private boolean doesAutoconfSupportExistHelper(DataElement root)
	{
		for (int i = 0; i < root.getNestedSize(); i++)
		{
			DataElement child = root.get(i).dereference();
			String type = child.getType();
			if (type.equals("file"))
			{
				if (!child.isDeleted())
				{
					String name = child.getName();
					if (name.equals("Makefile")||name.equals("Makefile.am")
						||name.equals("configure.in"))
					{
						return true;
					}
				}
			}
			else if (type.equals("Project") || type.equals("directory"))
			{
				return doesAutoconfSupportExistHelper(child);
			}
		}
		return false;
	}
}


