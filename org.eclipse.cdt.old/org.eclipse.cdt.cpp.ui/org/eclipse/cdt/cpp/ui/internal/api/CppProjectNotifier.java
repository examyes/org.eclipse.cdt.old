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
  private Shell      _shell;
  private boolean    _enabled;
    private FireMainThread _fire;

    public CppProjectNotifier()
      {
        _listeners = new ArrayList();
        _events    = new ArrayList();
        _enabled   = false;
	_fire = new FireMainThread();
      }

  public void setShell(Shell shell)
  {  
    _shell = shell; 
  }
  
  public void enable(boolean on)
      { 
        _enabled = on;
      }

  public boolean isEnabled()
      {
        return _enabled;
      }

  public void addProjectListener(ICppProjectListener listener)
      {
	  
	if (!_listeners.contains(listener))
        {
	    _listeners.add(listener);
	    if (_shell == null)
		{
		    _shell = listener.getShell();
		}
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
	  if ((_shell != null) && (!_shell.isDisposed()))
	      {
		  try
		      {			      
			  Display d = _shell.getDisplay(); 
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
