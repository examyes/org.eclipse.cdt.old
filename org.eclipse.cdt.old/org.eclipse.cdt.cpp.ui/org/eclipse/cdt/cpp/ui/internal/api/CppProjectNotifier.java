package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class CppProjectNotifier  
{
  public class FireMainThread implements Runnable
  {
      private CppProjectEvent _event;
      
      public FireMainThread(CppProjectEvent event)
      {
	  _event = event;
      }
    
      public void run()
      {    
	  for (int i = 0; i < _listeners.size(); i++)
	      {
		  ICppProjectListener listener = (ICppProjectListener)_listeners.get(i);			  
		  listener.projectChanged(_event);
	      }	  
      } 
  }

    private ArrayList  _listeners;
    private boolean    _enabled;
    private ModelInterface _api;

    public CppProjectNotifier(ModelInterface api)
      {
        _listeners = new ArrayList();
        _enabled   = false;
	_api = api;
      }

    public void enable(boolean on)
    { 
        _enabled = on;
    }
    
    public boolean isEnabled()
    {
        return _enabled;
    }

    public Shell getShell()
    {
	return _api.getDummyShell();
    }
    
    public void addProjectListener(ICppProjectListener listener)
    {	
	if (!_listeners.contains(listener))
	    {
		_listeners.add(listener);
	    }
      }

  public void removeProjectListener(ICppProjectListener listener)
      {
        _listeners.remove(listener);
      }

  public boolean hasProjectListener(ICppProjectListener listener)
      {
	return _listeners.contains(listener);
      }

  public void fireProjectChanged(CppProjectEvent event)
    {
	if (isEnabled())
	    {
		if ((getShell() != null) && (!getShell().isDisposed()))
		    {
			try
			    {			      
				Display d = getShell().getDisplay(); 
				if (d != null)
				    {
					FireMainThread fire = new FireMainThread(event);
					d.asyncExec(fire); 
				    }	      
			    }
			catch (SWTException e)
			    {
				System.out.println(e);	      
			    }
		    }
	    }	
    }
    
}
