package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class DomainNotifier implements IDomainNotifier 
{
  private ArrayList      _listeners;
  private Shell          _shell;
  private boolean        _enabled;
  private FireMainThread _fire;

  public class FireMainThread implements Runnable
  {
      public boolean     _isWorking;
      private DomainEvent     _event;
      
      public FireMainThread(DomainEvent event)
      {
	  _isWorking = false;
	  _event = event;
      }
      
      public void run()
      {    
	  _isWorking = true;
	  
	  if (_event.getType() != DomainEvent.FILE_CHANGE)
	      {
		  for (int i = 0; i < _listeners.size(); i++)
		      {
			  IDomainListener listener = (IDomainListener)_listeners.get(i);
			  if ((listener != null) && listener.listeningTo(_event))
			      {
				  listener.domainChanged(_event);
			      }
		      }	  
	      }	      
	  
	  _isWorking = false;
      } 
  }
  
  public DomainNotifier()
      {
        _listeners = new ArrayList();
        _enabled   = false;
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

  public void addDomainListener(IDomainListener listener)
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

  private Shell findShell()
  {
    for (int i = 0; i < _listeners.size(); i++)
      {
	IDomainListener listener = (IDomainListener)_listeners.get(i);
	Shell shell = listener.getShell();
	if ((shell != null) && !shell.isDisposed())
	    {
		return shell;
	    }
      }   

    return null;
  }
  

  public void fireDomainChanged(DomainEvent event)
      {
	  //	  if (_enabled)
	      {

		  if (_shell == null || (_shell.isDisposed()))
		      {
			  _shell = findShell();	    
		      }
		  
		  if (_shell != null)
		      {	    
			  try
			      {			        
				  Display d = _shell.getDisplay(); 
				  if (d != null)
				      {					  
					  FireMainThread fire = new FireMainThread(event);
					  d.syncExec(fire); 
				      }	      
			      }
			  catch (SWTException e)
			      {
				  System.out.println(e);	      
			      }
		      }	    
	      }
      }	

  public boolean hasDomainListener(IDomainListener listener)
      {
	return _listeners.contains(listener);
      }


  public void removeDomainListener(IDomainListener listener)
      {
        _listeners.remove(listener);
      }
}
