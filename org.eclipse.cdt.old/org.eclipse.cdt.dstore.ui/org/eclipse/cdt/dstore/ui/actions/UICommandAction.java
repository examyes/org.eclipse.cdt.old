package org.eclipse.cdt.dstore.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*;
import java.util.*;


import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.window.*;

import org.eclipse.core.runtime.*;

public class UICommandAction extends Action
{
    private DataStore          _dataStore;
    private DataElement        _descriptor;
    private List               _subjects;        
    
    public UICommandAction(DataElement subject, String label, DataElement descriptor, DataStore dataStore)
    {	
	super(label);
	_dataStore = dataStore;
	_descriptor = descriptor;
	_subjects = new ArrayList();
	_subjects.add(subject);
    }

    public UICommandAction(List subjects, String label, DataElement descriptor, DataStore dataStore)
    {	
	super(label);
	_dataStore = dataStore;
	_descriptor = descriptor;
	_subjects = subjects;
    }

    public ArrayList initCommandInput(DataElement object)
    {
      ArrayList result = new ArrayList();
      
      for (int i = 0; i < object.getNestedSize(); i++)
	{
	  DataElement child = ((DataElement)object.get(i));
	  if (child.getType().equals("input"))
	    {		  
	      result.add(child.dereference());
	    }
	  else if (child.getType().equals("select"))
	    {
	      result.add(child.dereference());
	    }	      
	}

      return result;
    }
    
    public ArrayList initCommandOutput(DataElement object)
    {
      ArrayList result = new ArrayList();
      
      for (int i = 0; i < object.getNestedSize(); i++)
	{
	  DataElement child = ((DataElement)object.get(i));
	  if (child.getType().equals("output"))
	    {		  
	      result.add(child.dereference());
	    }
	}      

      return result;  
    }
    
    
    public void run()
    {
	for (int i = 0; i < _subjects.size(); i++)
	    {
		DataElement selected = (DataElement)_subjects.get(i);
		if (selected != null)
		    {
			ArrayList output = initCommandOutput(_descriptor);	  
			ArrayList input  = initCommandInput(_descriptor);
			
			// find out if we have enough information to issue command
			ArrayList arguments = null;
			if (input.size() > 0)
			    {		  
				CommandDialog dialog = new CommandDialog(_descriptor.getName(), input, _dataStore);	      
				dialog.open();
				if (dialog.getReturnCode() != dialog.OK)
				    return;
				
				arguments = dialog.getValues(); 
			    }	

			doCommand(arguments, selected);
	      }    	  
	}
    }   

    public void doCommand(ArrayList arguments, DataElement selected)
    {
	DataElement statusObject = null;
	if (arguments != null)
	    {
		statusObject = _dataStore.command(_descriptor, arguments, selected);
	    }
	else
	    {
		statusObject = _dataStore.command(_descriptor, selected);
	    }
	
	if (statusObject != null && statusObject.getName().equals("incomplete"))
	    {		
		ArrayList subOutput = initCommandOutput(statusObject);
		ArrayList subInput = initCommandInput(statusObject);
		
		if (subInput.size() > 0)
		    {
			CommandDialog dialog = new CommandDialog(statusObject.getName(), 
								 subInput, _dataStore);
			dialog.open();
			
			if (dialog.getReturnCode() != dialog.OK)
			    return;
			
			ArrayList fixedArguments =  dialog.getValues(); 
			doCommand(fixedArguments, selected);
		    }
	    }
    }
}

