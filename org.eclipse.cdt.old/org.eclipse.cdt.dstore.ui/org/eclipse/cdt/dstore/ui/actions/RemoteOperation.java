package com.ibm.dstore.ui.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.client.*;


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


