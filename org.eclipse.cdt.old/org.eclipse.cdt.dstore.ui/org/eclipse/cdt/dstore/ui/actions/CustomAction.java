package org.eclipse.cdt.dstore.ui.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
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
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
//import org.eclipse.swt.widgets.*;

public class CustomAction extends Action
{
    protected DataStore          _dataStore;
    protected DataElement        _command;
    protected DataElement        _subject;
    protected List               _subjects;
    private   boolean            _supportsMultiple;

  public CustomAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(label);
        _dataStore = dataStore;
        _command = command;
        _subject = subject; 
		_subjects = new ArrayList();
		_subjects.add(_subject);
		_supportsMultiple = false;
      }

  public CustomAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
      {	
        super(label);
        _dataStore = dataStore;
        _command = command;
	    _subjects = subjects;
        _subject = (DataElement)_subjects.get(0); 
        _supportsMultiple = true;
      }

  public CustomAction(String label) 
      {	
        super(label);
        _dataStore = null;
        _command = null;
        _subject = null; 
        _subjects = null; 
        _supportsMultiple = false;
      }
      
    public boolean supportsMultiple()
    {
    	return _supportsMultiple;
    }
  
    public void setSubject(DataElement subject)
    {
	_subject = subject;
	_subjects = new ArrayList();
	_subjects.add(_subject);
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
                                          

        explainD.openInformation(new org.eclipse.swt.widgets.Shell(), "CustomAction", msg);          
      }
    
  }

