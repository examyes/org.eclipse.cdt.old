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


public class CreateConfigureAction extends CustomAction implements SelectionListener
{ 
	CustomMessageDialog box;
	String[] extraLabels;
	boolean enableCreateDialog = true;
	boolean enableCreateUpdate = true;
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
	
	public CreateConfigureAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		
		if(_command.getValue().equals("CREATE_CONFIGURE"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
				
		//enable disable based on object files
			
		if(_command.getValue().equals("CREATE_CONFIGURE")&& doesFileExist("configure")&& configureIsUptodate(_subject))
				setEnabled(false);
						
	}
    public void run()
	{
		boolean execute = true;
		int createUpdate = 0; // 0 == ok do update, 1 == no update , 2 == cancel action
		boolean configfilesExist = false;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		
	
		if(_command.getValue().equals("CREATE_CONFIGURE"))
		{
			String str1;
			String message;
			String preferenceKey = "Show Dialog Create";

			// checking if automatic updating is enabled from the autoconf preferences page
			ArrayList autoUpdateCreate = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty("Auto Update Create");
			if(!autoUpdateCreate.isEmpty())
			{
				String preference = (String)autoUpdateCreate.get(0);
				if (preference.equals("Yes"))
					enableCreateUpdate = true;
				else
					enableCreateUpdate = false;
			}
			
			if(doesAutoconfSupportExist())
			{
				configfilesExist = true;
				if(enableCreateUpdate)
				{
					str1 = new String("\nWould you like the system to update and generate missing configuration files?");
					message = new String("\nGenerating project configuration files"+str1);
					String[] extraLabel = new String[]{"Do not show this Dialog again"};
					box = new CustomMessageDialog(
								shell,
								"Creating configure.in and Makefile.am",
								null,
								message,
								3,
								new String[]{IDialogConstants.YES_LABEL,IDialogConstants.NO_LABEL,IDialogConstants.CANCEL_LABEL},
								0,
								extraLabel,
								this);
					int result = box.open(preferenceKey);
					if(result!= -1)
						createUpdate= result;
					else
						createUpdate = 0;
					
				}
				else
				{
					message = new String("\nUsing existing configuration files to generate the configure script");
					String[] extraLabel = new String[]{"Do not show this Dialog again"};
					box = new CustomMessageDialog(
									shell,
									"Generating configure script",
									null,
									message,
									2,
									new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
									0,
									extraLabel,
									this);
					int result = box.open(preferenceKey);
					if(result!= -1)
						createUpdate= result+1;
					else
						createUpdate = 1;
					// 0 is equiv to 1 ie run with no update , 
					//and 1 is equiv to 2 which is to cancel the action so we need to increment
				}
			}
			else
			{
				configfilesExist = false;
				message = new String("\nGenerating configuration files to generate the configure script");
				String[] extraLabel = new String[]{"Do not show this Dialog again"};
				box = new CustomMessageDialog(
								shell,
								"Generating configure script",
								null,
								message,
								2,
								new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
								0,
								extraLabel,
								this);
				int result = box.open(preferenceKey);
				if(result!= -1)
					if(result==1)
						createUpdate= result+1;
				else
					createUpdate = 0;
				
			}
		}


		if(execute)
		{	
			if(createUpdate==1 && configfilesExist)
			{
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CREATE_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
			else if(createUpdate==0)
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
		    if(state.equals("uptodate"))
		    	return true;
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
		boolean selection = box.extraButtons[buttonId].getSelection();
		ArrayList list = new ArrayList();
		if(buttonId == 0)
		{
			// persist this value for thos project
			if(selection)
			{
				list.add("No");
			}
			else
			{
				list.add("Yes");
			}
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty("Show Dialog Create",list);
		}
    }
}


