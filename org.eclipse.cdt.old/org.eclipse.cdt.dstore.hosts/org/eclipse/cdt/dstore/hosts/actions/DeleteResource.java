package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.cdt.dstore.ui.actions.*;


import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;

import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;


public class DeleteResource extends CustomAction
{
    private HostsPlugin _plugin;
    public DeleteResource(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
	super(subject, label, command, dataStore);
	
	if (!isValid(subject))
	    {
		return;	
	    }	
	_plugin = HostsPlugin.getDefault();
    }
    
    public DeleteResource(java.util.List subjects, String label, DataElement command, DataStore dataStore)
    {
	super(subjects, label, command, dataStore);
	
	for (int i = 0; i < subjects.size(); i++)
	    {
		DataElement sub = (DataElement)subjects.get(i);			
		if (!isValid(sub))
		    {
			return;
		    }
	    }		
	_plugin = HostsPlugin.getDefault();
    }
    
    private boolean isValid(DataElement subject)
    {
	String type = subject.getType();
	if (type.equals("Project")) // hack
	    {
		setEnabled(false);
		return false;
	    }
	return true;
    }
    
    public void run()
    {
	Shell shell = null;
	
	String cmd = _command.getValue();
	String dCmdStr = "C_" + cmd; 
	String msg = _plugin.getLocalizedString("actions.About_to_delete") + " ";
	
	int maxsize = 30;
	for (int i = 0; i < _subjects.size() && i <= maxsize; i++)
	    {
		if (i > 0)
		    {
			msg += "\t\t";
		    }
		msg += ((DataElement)_subjects.get(i)).getName() + "\n";
		
		if (i == maxsize)
		    {
			msg += "\t\t...\n";
		    }	
	    } 
	
	
	msg += _plugin.getLocalizedString("actions.Confirm_Delete");
	String title = _plugin.getLocalizedString("actions.Delete_Resource");

	boolean deleteResource = MessageDialog.openQuestion(shell, title, msg);

	if (deleteResource)
	    {
	    	for (int i = 0; i < _subjects.size(); i++)
	    	{
	    		DataElement subject = (DataElement)_subjects.get(i);
				DataElement cmdDescriptor = _dataStore.localDescriptorQuery(subject.getDescriptor(), 
									    dCmdStr, 4);
									    
				closeEditor(subject);
				if (cmdDescriptor != null)
		    	{
		    		DataElement notifyD = _dataStore.localDescriptorQuery(_dataStore.getRoot().getDescriptor(), "C_NOTIFICATION", 1);
		    		if (notifyD != null)
		    		{
		    			DataElement dummyD = _dataStore.createObject(null, "dummy", "C_DELETE");
		    			ArrayList args = new ArrayList();
		    			args.add(subject);
		    			_dataStore.command(notifyD, args, dummyD);
		    		}
		    		
		    		
					_dataStore.command(cmdDescriptor, subject);									 
		    	}
	    	}
	    }
    }    
    
  private void closeEditor(DataElement fileElement)
  {
	IWorkbench desktop = HostsPlugin.getDefault().getWorkbench();

	IWorkbenchWindow[] windows = desktop.getWorkbenchWindows();
	for (int a = 0; a < windows.length; a++)
	    {	
		IWorkbenchWindow window = windows[a];
		IWorkbenchPage[] pages = window.getPages();
		for (int b = 0; b < pages.length; b++)
		    {
			IWorkbenchPage page = pages[b];
			IEditorPart[] editors = page.getEditors();
		        for (int c = 0; c < editors.length; c++)
			    {
				IEditorPart editor = editors[c];
				if (editor.getEditorInput() instanceof IFileEditorInput)
				  {
				    IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
				    if (input != null)
				      {
						IFile file = input.getFile();
						if (file instanceof ResourceElement)
						{
							ResourceElement resFile = (ResourceElement)file;
							if (resFile.getElement() == fileElement)
							{
								page.closeEditor(editor, false);	
							}	
						}
				  	  }
				  }
			    }
		    }	
	    }
    }
    
 
}

