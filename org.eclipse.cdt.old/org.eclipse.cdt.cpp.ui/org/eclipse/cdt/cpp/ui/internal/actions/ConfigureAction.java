package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

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

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;


public class ConfigureAction extends CustomAction
{ 
//	ProjectStructureManager structureManager;
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
		
		if((_command.getValue().equals("UPDATE_MAKEFILE_AM")&&!doesFileExist("Makefile.am")))
				setEnabled(false);
				
		if (subject.getType().equals("Project"))	
			if((_command.getValue().equals("UPDATE_CONFIGURE_IN")&&!doesFileExist("configure.in")))
				setEnabled(false);
			
		if(_command.getValue().equals("CREATE_CONFIGURE")&& doesFileExist("configure")&& configureIsUptodate(_subject))
				setEnabled(false);
				
						
		if(_command.getValue().equals("RUN_CONFIGURE")&&!doesFileExist("configure") )
				setEnabled(false);		
		
	}
    public void run()
	{
		boolean execute = true;
		boolean runUpdate = true;
		boolean createUpdate = true;
		boolean noConffilesExist = true;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
		{
			if(doesAutoconfSupportExist())
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Attempting to update existing configure.in and makefile.am's "+
				"\nconfigure.in and or Makefile.am Files will be generated if missing"
					+"\nIf updated then old configure.in and Makefile.am's will be renamed *.old");
				//dialog.openInformation(shell,"Updating configure.in and Makefile.am's ",message);
				execute = dialog.openConfirm(shell,"Updating configure.in and Makefile.am's ",message);
			}
		}	
		if(_command.getValue().equals("UPDATE_MAKEFILE_AM"))
		{
			if(doesFileExist("Makefile.am"))
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Attempting to update existing makefile.am's"+
					"\nIf updated then old Makefile.am's will be renamed *.old");
				//dialog.openInformation(shell,"Updating Makefile.am's ",message);
				execute = dialog.openConfirm(shell,"Updating Makefile.am's ",message);
			}
		}	
		if(_command.getValue().equals("UPDATE_CONFIGURE_IN"))
		{
			if(doesFileExist("configure.in"))
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Attempting to update existing configure.in "+
					"\nIf updated then old configure.in shall be renamed *.old");
				//dialog.openInformation(shell,"Updating configure.in",message);
				execute = dialog.openConfirm(shell,"Updating configure.in",message);
			}
		}	
		
		if(_command.getValue().equals("CREATE_CONFIGURE"))
		{
			MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
			String str1,str2;
			if(doesAutoconfSupportExist())
			{
				noConffilesExist = false;
				str1 = new String(
				"Attempting to update existing configure.in and makefile.am's "+
				"\nconfigure.in and or Makefile.am Files will be generated if missing"+
				"\nOld existing configuration files will be renamed *.old if updated");
				str2 = new String("\nPress Cancel to skip updating  - recommended if you are not the package maintainer");
			}
			else
			{
				noConffilesExist = true;
				str1 = "";str2="";
			}
			String message = new String
			(str1+"\nGenerating project configuration files"+str2);
			createUpdate = dialog.openConfirm(shell,"Creating configure.in and Makefile.am's ",message);
		}
		if(_command.getValue().equals("RUN_CONFIGURE"))
		{
			if(!configureIsUptodate(_subject))
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("\nThe system detects that configure script is not up to date"+
				"\nUpdating project configuration files and regenerating configure may be needed"+
				"\nOld existing configuration files will be renamed *.old if updated"+
				"\nPress OK to update project configuration files, or "+
				"\nPress Cancel to skip updating  - recommended if you are not the package maintainer");
				runUpdate = dialog.openConfirm(shell,"Updating configure.in, Makefile.am's and generating configure ",message);
			}
			else
			{
				runUpdate = false;
			}
		}
			
		if(execute)
		{	
			if(!createUpdate&&!noConffilesExist)
			{
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CREATE_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
			else if(!runUpdate)
			{
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_RUN_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
			else if(createUpdate&&runUpdate)
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
	}
	private boolean doesFileExist(String fileName)
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
						||name.equals("Makefile.in")||name.equals("configure.in"))
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
	
	private boolean configureIsUptodate(DataElement root)
	{
		DataElement cmdD = _dataStore.localDescriptorQuery(root.getDescriptor(), "C_CHECK_UPDATE_STATE", 4);
	
		if (cmdD != null)
		{
			DataElement status = _dataStore.synchronizedCommand(cmdD, root);		
			DataElement updateState = (DataElement)status.get(0);
		    String state = updateState.getName();
		    System.out.println("state = " + state);
		    if(state.equals("uptodate"))
		    	return true;
		}
		return false;
	}
}


