package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class CppProjectNotifier  
{
  public class FireMainThread implements Runnable
  {
    public FireMainThread()
    {
    }
    
    public void run()
      {    
	  for (int a = 0; a < _events.size(); a++)
	      {
		  CppProjectEvent event = (CppProjectEvent)_events.get(a);
		  for (int i = 0; i < _listeners.size(); i++)
		      {
			  ICppProjectListener listener = (ICppProjectListener)_listeners.get(i);			  
			  listener.projectChanged(event);
		      }
	      }

	  _events.clear();
      } 
  }

    private ArrayList  _listeners;
    private ArrayList  _events;
    private boolean    _enabled;
    private FireMainThread _fire;
    private ModelInterface _api;

    public CppProjectNotifier(ModelInterface api)
      {
        _listeners = new ArrayList();
        _events    = new ArrayList();
        _enabled   = false;
	_fire = new FireMainThread();
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
				  if (!_events.contains(event))
				      {		    
					  _events.add(event);		  
					  d.asyncExec(_fire); 
					  return;
				      }		      
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
