package org.eclipse.cdt.dstore.ui.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.client.*;


import java.util.*;

import org.eclipse.ui.actions.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;

public class RemoteOperation implements IRunnableWithProgress
					//, IDomainListener
{
  private DataElement      _command;
  private DataElement      _object;
  private ArrayList           _arguments;
  
  private DataElement      _statusObject;

  private IProgressMonitor _pm;
  private DataStore        _dataStore;

  private static int       _progressMade;
  private static boolean   _stop;

  public RemoteOperation(DataElement descriptor,
                         DataElement object,
                         DataStore dataStore)
      {
        super();
        _command      = descriptor;
        _object       = object;
        _dataStore    = dataStore;
	_progressMade = 0;
	_stop         = false;
	_statusObject = null;
	_arguments    = null;	
      }

  public void setArgument(ArrayList arguments)
  {
    _arguments = arguments;
  }
  

  protected void doSubTask(IProgressMonitor pm, int i)
      {
	pm.worked(i);
      }

  protected void execute(IProgressMonitor pm) 
      {
	_pm = pm;
        String task   = _command.getName();
	
	if (_arguments == null)
	  {	    
	    _statusObject = _dataStore.synchronizedCommand(_command, _object);	
	  }
	else 
	  {
	    _statusObject = _dataStore.synchronizedCommand(_command, _arguments, _object);		    
	  }

	pm.beginTask(task + ": " + _object.getName(), 100);


	_pm.done();			
      }

  public boolean isCancelable()
      {
        return true;
      }

  public void run(IProgressMonitor monitor) throws InvocationTargetException
  {
      execute(monitor);
  }
  
  public DataElement getStatus()
  {
    return _statusObject;
  }
}


