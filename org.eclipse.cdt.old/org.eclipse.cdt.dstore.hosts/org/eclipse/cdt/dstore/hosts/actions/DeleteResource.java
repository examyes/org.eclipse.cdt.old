package com.ibm.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.dialogs.*;

import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;


public class DeleteResource extends CustomAction
{
    public DeleteResource(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
		super(subject, label, command, dataStore);
	
		String type = subject.getType();
		if (!type.equals("directory") && !type.equals("file"))
	    {
			setEnabled(false);
	    }
    }

	public DeleteResource(java.util.List subjects, String label, DataElement command, DataStore dataStore)
	{
		super(subjects, label, command, dataStore);
		
		for (int i = 0; i < subjects.size(); i++)
		{
			DataElement sub = (DataElement)subjects.get(i);	
			String type = sub.getType();
			if (!type.equals("directory") && !type.equals("file"))
	    	{
				setEnabled(false);
				return;
	    	}
		}		
	}
	

    public void run()
    {
	Shell shell = null;

	String cmd = _command.getValue();
	String dCmdStr = "C_" + cmd; 
	String msg = "About to delete: ";
	
	for (int i = 0; i < _subjects.size(); i++)
		{
			if (i != 0)
			{
				msg += ",";
			}
			msg += " " + ((DataElement)_subjects.get(i)).getName();	
		} 

	
	msg += "\nAre you sure you want to do this?";
	String title = "Delete Resource";

	boolean deleteResource = MessageDialog.openQuestion(shell, title, msg);

	if (deleteResource)
	    {
	    	for (int i = 0; i < _subjects.size(); i++)
	    	{
	    		DataElement subject = (DataElement)_subjects.get(i);
				DataElement cmdDescriptor = _dataStore.localDescriptorQuery(subject.getDescriptor(), 
									    dCmdStr, 4);
				if (cmdDescriptor != null)
		    	{
					_dataStore.command(cmdDescriptor, subject);
		    	}
	    	}
	    }
    }    
}

