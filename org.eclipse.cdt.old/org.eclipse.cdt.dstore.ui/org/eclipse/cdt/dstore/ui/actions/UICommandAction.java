package com.ibm.dstore.ui.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.io.*;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.window.*;

import org.eclipse.core.runtime.*;

  public class UICommandAction extends Action
  {
    private DataStore          _dataStore;
    private DataElement        _descriptor;
    private DataElement        _subject;

    public UICommandAction(DataElement subject, String label, DataElement descriptor, DataStore dataStore)
    {	
      super(label);
      _dataStore = dataStore;
      _descriptor = descriptor;
      _subject = subject;
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
      DataElement selected = _subject;

      if (selected != null)
	{
	  RemoteOperation op = new RemoteOperation(_descriptor, selected, _dataStore);

	  ArrayList output = initCommandOutput(_descriptor);	  
	  ArrayList input  = initCommandInput(_descriptor);
	  	  
	  // find out if we have enough information to issue command
	  if (input.size() > 0)
	    {		  
	      CommandDialog dialog = new CommandDialog(_descriptor.getName(), input, _dataStore);	      
	      dialog.open();
	      if (dialog.getReturnCode() != dialog.OK)
		return;
	      
	      ArrayList arguments =  dialog.getValues(); 
	      op.setArgument(arguments);
	    }	

	  try
	      {
		  op.run(new NullProgressMonitor());
	      }
	  catch (java.lang.reflect.InvocationTargetException e)
	      {
	      }
	  DataElement statusObject = op.getStatus();
	  
	  if (statusObject != null && statusObject.getName().equals("incomplete"))
	    {		
	      ArrayList subOutput = initCommandOutput(statusObject);
	      ArrayList subInput = initCommandInput(statusObject);
	      
	      if (subInput.size() > 0)
		{
		  CommandDialog dialog = new CommandDialog(statusObject.getName(), subInput, _dataStore);	      
		  dialog.open();
		  
		  if (dialog.getReturnCode() != dialog.OK)
		    return;

		  ArrayList arguments =  dialog.getValues(); 
		  op.setArgument(arguments);
		  try
		      {
			  op.run(new NullProgressMonitor());
		      }
		  catch (java.lang.reflect.InvocationTargetException e)
		      {
		      }		  
		}
	    }    	  
	}
    }
    
  }

