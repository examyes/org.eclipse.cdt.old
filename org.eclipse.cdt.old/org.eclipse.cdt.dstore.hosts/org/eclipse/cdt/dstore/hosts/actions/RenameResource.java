package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
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


public class RenameResource extends CustomAction
{
    public RenameResource(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
	 super(subject, label, command, dataStore);
	
	 isValid(subject);	
    }
    
    private boolean isValid(DataElement subject)
	{
		String type = subject.getType();
		if (!type.equals("directory") && !type.equals("file"))
		{
			DataElement des = subject.getDescriptor();
			
			if (des == null || !des.isOfType("file"))
		    {
		    	setEnabled(false);	
		    	return false;
		    }
		}	
	    return true;
	}

    public void run()
    {

	 String oldNameStr = new String(_subject.getName());
	 String oldSourceStr = new String(_subject.getSource());
	 
     RenameDialog dialog = new RenameDialog("Rename Resource", _subject.getName());
     dialog.open();
     if (dialog.getReturnCode() == dialog.OK)
     {     
	 	String newNameStr = dialog.getName();

	 	if (!newNameStr.equals(oldNameStr))
	    {
			DataElement cmdDescriptor = _dataStore.localDescriptorQuery(_subject.getDescriptor(), 
									    "C_RENAME", 4);
			if (cmdDescriptor != null)
		    {
		   	    String newSourceStr = _subject.getParent().getSource() + "/" + newNameStr;	
		    	ArrayList args = new ArrayList();
		    	DataElement newName = _dataStore.createObject(null, "name", newNameStr);		    	
		    	args.add(newName);
		    	_dataStore.command(cmdDescriptor, args, _subject);							

		    	DataElement notifyD = _dataStore.localDescriptorQuery(_dataStore.getRoot().getDescriptor(), "C_NOTIFICATION", 1);
		    	if (notifyD != null)
		    	{
		    			ArrayList nargs = new ArrayList();		    		 			
		    			nargs.add(_subject);
		    			nargs.add(_dataStore.createObject(null, _subject.getType(), oldNameStr, oldSourceStr));
		    			DataElement dummyCmd = _dataStore.createObject(null, "dummy command", "C_RENAME");
		    			_dataStore.command(notifyD, nargs, dummyCmd);
		    	}
	
			}
	    }
    }
    }    
}

