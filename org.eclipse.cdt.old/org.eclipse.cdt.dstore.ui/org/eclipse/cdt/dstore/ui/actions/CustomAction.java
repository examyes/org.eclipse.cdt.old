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

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class CustomAction extends Action
{
  protected DataStore          _dataStore;
  protected DataElement        _command;
  protected DataElement        _subject;
  
  public CustomAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(label);
        _dataStore = dataStore;
        _command = command;
        _subject = subject; 
      }

  public CustomAction(String label) 
      {	
        super(label);
        _dataStore = null;
        _command = null;
        _subject = null; 
      }
  
    public void setSubject(DataElement subject)
    {
	_subject = subject;
    }  

  public void run()
      {
        String msg = "Extend CustomAction and add a ui_commanddescriptor to add your own custom action!";
        MessageDialog explainD = new MessageDialog(null,
						   "Custom Action", 
						   null, 
						   msg, 
						   MessageDialog.INFORMATION,
                                                   new String[]  { "OK" },
						   0);
                                          

        explainD.openInformation(new Shell(), "CustomAction", msg);          
      }
    
  }

