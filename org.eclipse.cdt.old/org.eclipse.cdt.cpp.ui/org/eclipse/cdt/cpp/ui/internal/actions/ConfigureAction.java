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
		
		if(_command.getValue().equals("GENERATE_AUTOCONF_FILES")||_command.getValue().equals("CREATE_CONFIGURE")
			||_command.getValue().equals("RUN_CONFIGURE")||_command.getValue().equals("UPDATE_AUTOCONF_FILES")
			||_command.getValue().equals("UPDATE_CONFIGURE_IN"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
		//enable disable based on object files
		if((_command.getValue().equals("UPDATE_AUTOCONF_FILES")||_command.getValue().equals("UPDATE_CONFIGURE_IN")
		||_command.getValue().equals("UPDATE_MAKEFILE_AM")||_command.getValue().equals("CREATE_CONFIGURE")
		||_command.getValue().equals("RUN_CONFIGURE"))&&!doesAutoconfSupportExists())
			//if (subject.getType().equals("Project"))	
				setEnabled(false);
		if(_command.getValue().equals("RUN_CONFIGUTRE")&&!doesFileExists("configure.in")&&!doesFileExists("Makefile.am"))
			//if (subject.getType().equals("Project"))	
				setEnabled(false);		

		if(_command.getValue().equals("RUN_CONFIGURE")&&!doesFileExists("configure"))
			if (subject.getType().equals("Project"))	
				setEnabled(false);
	}
    public void run()
	{
		boolean execute = true;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		if(_command.getValue().equals("GENERATE_AUTOCONF_FILES"))
		{
			if(doesAutoconfSupportExists())
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String("This action will generate all the files needed for Autoconf Support"+
					"\nExisting configure.in and Makefile.am's will be overwritten"+
					"\nDo you wish to continue?");
				if(!dialog.openConfirm(shell,"Generating configure.in and Makefile.am's ",message))
					execute = false;
			}
		}
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
		{
			if(doesAutoconfSupportExists())
			{
				MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
				String message = new String
				("Trying to update existing configure.in and makefile.am's "+
				"\nconfigure.in and or Makefile.am Files will be generated if missing"
					+"\nIf updated then old configure.in and Makefile.am's will be renamed *.old");
				dialog.openInformation(shell,"Updating configure.in and Makefile.am's ",message);
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
				dialog.openInformation(shell,"Updating Makefile.am's ",message);
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
				dialog.openInformation(shell,"Updating configure.in",message);
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
		File project = _subject.getFileObject();
		File[]fileList = project.listFiles();
		for (int i = 0; i < fileList.length; i++)
			if(fileList[i].getName().equals(fileName))
				return true;
		return false;
	}
	private boolean doesAutoconfSupportExists()
	{
		File project = _subject.getFileObject();
		ProjectStructureManager structure = new ProjectStructureManager(project);
		File[]fileList = structure.getFiles();

		for (int i = 0; i < fileList.length; i++)
			if(fileList[i].getName().equals("Makefile")||fileList[i].getName().equals("Makefile.am")||fileList[i].getName().equals("Configure.in"))
				return true;
		return false;
	}
}


