package org.eclipse.cdt.pa.ui.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class PATraceNotifier  
{
  public class FireMainThread implements Runnable
  {
      private PATraceEvent _event;
      
      public FireMainThread(PATraceEvent event)
      {
	  _event = event;
      }
    
      public void run()
      {    
	  for (int i = 0; i < _listeners.size(); i++)
	      {
		  IPATraceListener listener = (IPATraceListener)_listeners.get(i);			  
		  listener.traceChanged(_event);
	      }	  
      } 
  }

    private ArrayList  _listeners;
    private boolean    _enabled;
    private PAModelInterface _api;

    // Constructor
    public PATraceNotifier(PAModelInterface api) {
        
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

    public Shell getShell() {
     return _api.getShell();
    }
    
    public void addTraceListener(IPATraceListener listener)
    {
	 if (!_listeners.contains(listener))
	 {
		_listeners.add(listener);
	 }
    }

    public void removeTraceListener(IPATraceListener listener)
    {
        _listeners.remove(listener);
    }

    public boolean hasTraceListener(IPATraceListener listener)
    {
		return _listeners.contains(listener);
    }

    
    public void fireTraceChanged(PATraceEvent event)
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
