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
	CustomMessageDialog box;
	String[] extraLabels;
	boolean enableConfigureDialog = true;
	boolean enableConfigureUpdate = true;
	String configueDialogPrefernceKey = "Show_Configure_Dialog";
	String configureUpdatePreferenceKey = "Update_When_Configure";
	String targetKey = "Target_Type";
	
	private int dialogButtonPushed = -1;
	
	private final int DEFAULT = 0;
	private final int PROGRAM_TARGET = 100;	
	private final int STATIC_TARGET = 101;	
	private final int SHARED_TARGET = 102;
	private int targetType = DEFAULT;	
	
	boolean dialogHas2Buttons = false;
	
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
		
		if(_command.getValue().equals("CONFIGURE"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
										
	}
    public void run()
	{
		//boolean execute = true;
		int configureUpdate = 0; // 0 == ok do update, 1 == no update , 2 == cancel action
		boolean configFilesExist = false;
		boolean sourceExistInTopLevelDir = false;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		
		if (sourceFilesExist())
		{
			sourceExistInTopLevelDir = true;
		}
		
		if(_command.getValue().equals("CONFIGURE") && sourceExistInTopLevelDir && !doesAutoconfSupportExist())
		{
			dialogHas2Buttons=true;
			String str1 = "";
			String message;
			String[] extraLabel = new String[]{"Program","Static Lib","Shared Lib"};
			String title = "Creating configure script";
			message = new String("\nPlease select one of the following targets for your project"+str1);
			box = new CustomMessageDialog(
					shell,
					title,
					null,
					message,
					3,
					new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
					1<<4,0,
					extraLabel,
					this,
					targetKey);
					
			dialogButtonPushed = box.open();
		}
		
		else if(_command.getValue().equals("CONFIGURE"))
		{
			String str1;
			String message;
			String[] extraLabel = new String[]{"Do not show this dialog again"};
			String title = "Generating and running configure script";

			// checking if automatic updating is enabled from the autoconf preferences page
			ArrayList autoUpdateConfigure = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty(configureUpdatePreferenceKey);
			if(!autoUpdateConfigure.isEmpty())
			{
				String preference = (String)autoUpdateConfigure.get(0);
				if (preference.equals("Yes"))
					enableConfigureUpdate = true;
				else
					enableConfigureUpdate = false;
			}
			
			if(doesAutoconfSupportExist())
			{
				configFilesExist = true;
				String[] buttonTitles = new String[]{IDialogConstants.YES_LABEL,IDialogConstants.NO_LABEL,IDialogConstants.CANCEL_LABEL};
				if(enableConfigureUpdate)
				{
					str1 = new String("\nWould you like the system to update and generate missing configuration files?");
					
					if(doesFileExist("configure"))
					{
						if(!configureIsUptodate(_subject))
							message = new String("\nRegenerating and running configure script - configure is not up to date "+str1);
						else
						{
							dialogHas2Buttons = true;
							buttonTitles = new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL};
							message = new String("\nRunning configure script - configure is up to date");
						}
					}
					else
						message = new String("\nGenerating and running configure script"+str1);

					box = new CustomMessageDialog(
								shell,
								title,
								null,
								message,
								3,
								buttonTitles,
								0,
								extraLabel,
								this,
								configueDialogPrefernceKey);
					int result = box.open();
					if(result!= -1)
						configureUpdate= result;
					else
						configureUpdate = 0;
					
				}
				else
				{
					message = new String("\nUsing existing configuration files to create and run configure script");
					box = new CustomMessageDialog(
									shell,
									title,
									null,
									message,
									2,
									new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
									0,
									extraLabel,
									this,
									configueDialogPrefernceKey);
					int result = box.open();
					if(result!= -1)
						configureUpdate= result+1;
					else
						configureUpdate = 1;
					// 0 is equiv to 1 ie run with no update , 
					//and 1 is equiv to 2 which is to cancel the action so we need to increment
				}
			}
			else
			{
				configFilesExist = false;
				message = new String("\nGenerating and running configure script");
				box = new CustomMessageDialog(
								shell,
								title,
								null,
								message,
								2,
								new String[]{IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL},
								0,
								extraLabel,
								this,
								configueDialogPrefernceKey);
				int result = box.open();
				if(result!= -1)
					if(result==1)
						configureUpdate= result+1;
				else
					configureUpdate = 0;
				
			}
		}

		if(configureUpdate==1 && configFilesExist)
		{
			if(!dialogHas2Buttons)  // this to take care in case the cancel button pressesd when the dialog has 2 buttons only
			{
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
		}
		else if(configureUpdate==0 && targetType==DEFAULT&&dialogButtonPushed!=1) 
		{
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());			
			DataElement status = _dataStore.command(configureCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.monitorStatus(status);			
			api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}	
		// "Progarm" target
		else if(configureUpdate==0 && targetType==PROGRAM_TARGET && dialogButtonPushed!=1) 
		// targetSelection 0 means no selection was made and it will default to program
		{
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CONFIGURE_PROGRAM");			
			DataElement status = _dataStore.command(configureCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.monitorStatus(status);			
			api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}	
		
		// Static Target
		else if(configureUpdate==0 && targetType==STATIC_TARGET && dialogButtonPushed!=1)
		{
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CONFIGURE_STATIC");			
			DataElement status = _dataStore.command(configureCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.monitorStatus(status);			
			api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}	
		else if(configureUpdate==0 && targetType==SHARED_TARGET & dialogButtonPushed!=1)
		{
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CONFIGURE_SHARED");			
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
	
	private boolean sourceFilesExist()
    {
	return sourceFilesExistHelper(_subject);
    }
    private boolean sourceFilesExistHelper(DataElement root)
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
					if (isSourceFile(name))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isSourceFile(String name)
	{
		if(!name.endsWith(".c")&& !name.endsWith(".C")&&!name.endsWith(".cpp")&&!name.endsWith(".CPP")
			&&!name.endsWith(".cc")&&!name.endsWith(".CC")&&!name.endsWith(".cxx")&&!name.endsWith(".CXX")
			&&!name.endsWith(".c++")&&!name.endsWith(".C++"))
		{
			return false;
		}
		return true;
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
		ArrayList list = new ArrayList();
		int buttonId = ((Integer)e.widget.getData()).intValue();
		if(buttonId<100)
		{
			boolean selection = box.extraButtons[buttonId].getSelection();
			if(buttonId == 0)
			{
				// persist this value for this project
				if(selection)
				{
					list.add("No");
				}
				else
				{
					list.add("Yes");
				}
				org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(configueDialogPrefernceKey,list);
			}
			
			// set default project target to Default - needed for Autoconf Manager
			targetType = DEFAULT;
			list = new ArrayList();
			list.add("Default");
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(targetKey,list);

		}
		// the 100+ numbering scheme means that this button is not the one responsibe for show/hide the dialog 
		// default will be Program
		
		
		if(buttonId == 100)
		{
			targetType = PROGRAM_TARGET;
			// default target
			list.add("Program");
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(targetKey,list);
		}
		if(buttonId == 101)
		{
			targetType = STATIC_TARGET;
			list.add("Static");
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(targetKey,list);
		}
		if(buttonId == 102)
		{
			targetType = SHARED_TARGET;
			list.add("Shared");
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(targetKey,list);
		}
    }
}


