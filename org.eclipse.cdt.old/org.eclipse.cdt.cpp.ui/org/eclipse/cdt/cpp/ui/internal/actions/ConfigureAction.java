package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.CustomMessageDialog;
import org.eclipse.cdt.cpp.ui.internal.dialogs.PreventableMessageBox;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;


public class ConfigureAction extends CustomAction implements SelectionListener
{ 
	CustomMessageDialog dialog;
	String[] extraLabels = new String[]{"Do not show this Dialog again"};
	
	String updateAllDialogKey = "Show_Update_All_Dialog";
	String updateMakefileAmKey = "Show_Update_MakefileAm_Dialog";
	String updateConfigureInKey = "Show_Update_ConfigureIn_Dialog";
	
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
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES")||_command.getValue().equals("UPDATE_CONFIGURE_IN"))
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
	}
    public void run()
	{
		boolean execute = true;
		boolean noConfigfilesExist = true;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
		{
			
			if(doesAutoconfSupportExist())
			{
					String message = new String
						("Attempting to update existing and/or generating configure.in and makefile.am's "
							+"\nOld files will be renamed *.old");
					dialog = new CustomMessageDialog(
										shell,
										"Updating configure.in and Makefile.am ",
										null,
										message,
										2,
										new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
										0,
										extraLabels,
										this);
					int result = dialog.open(updateAllDialogKey);
					if(result!=-1)
						execute = result ==0;
					else
						execute = true;
			}
		}	
		if(_command.getValue().equals("UPDATE_MAKEFILE_AM"))
		{
			if(doesFileExist("Makefile.am"))
			{
					String message = new String
							("Attempting to update existing makefile.am's"+
							"\nIf updated then old Makefile.am's will be renamed *.old");
					dialog = new CustomMessageDialog(
										shell,
										"Updating Makefile.am ",
										null,
										message,
										2,
										new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
										0,
										extraLabels,
										this);
					int result = dialog.open(updateMakefileAmKey);
					if(result!=-1)
						execute = result ==0;
					else
						execute = true;
			}
		}	
		if(_command.getValue().equals("UPDATE_CONFIGURE_IN"))
		{
			if(doesFileExist("configure.in"))
			{
					String message = new String
						("Attempting to update existing configure.in "+
						"\nIf updated then old configure.in shall be renamed *.old");
					dialog = new CustomMessageDialog(
										shell,
										"Updating configure.in",
										null,
										message,
										2,
										new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
										0,
										extraLabels,
										this);
					int result = dialog.open(updateConfigureInKey);
					if(result!=-1)
						execute = result ==0;
					else
						execute = true;
			}
		}	
		

		if(execute)
		{	
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());			
			DataElement status = _dataStore.command(configureCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.monitorStatus(status);			
			api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
			RunThread thread = new RunThread(_subject, status);
			thread.start();
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
	
	public void widgetDefaultSelected(SelectionEvent e)
    {
		widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
		Widget source = e.widget;
		int buttonId = ((Integer)e.widget.getData()).intValue();
		boolean selection = dialog.extraButtons[buttonId].getSelection();
		ArrayList list = new ArrayList();
		if(buttonId == 0)
		{
			// persist this value for thos project
			if(selection)
			{
				//enableRunDialog = false;
				list.add("No");
			}
			else
			{
				//enableRunDialog = true;
				list.add("Yes");
			}
			if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
				org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(updateAllDialogKey,list);
			if(_command.getValue().equals("UPDATE_CONFIGURE_IN"))
				org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(updateConfigureInKey,list);
			if(_command.getValue().equals("UPDATE_MAKEFILE_AM"))
				org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(updateMakefileAmKey,list);

		}
    }
}


