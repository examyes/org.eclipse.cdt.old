package com.ibm.dstore.core.client;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
 
import java.util.*;
import java.lang.*; 
import java.io.*;

public class ClientUpdateHandler extends UpdateHandler
{ 
  public ClientUpdateHandler()
      {
        super();
        _waitIncrement = 100;
      }

    public void updateFile(String path, byte[] bytes, int size)
    {
    }

    public void updateAppendFile(String path, byte[] bytes, int size)
    {
    }

  public void updateFile(File file, DataElement object)
      {
	IDomainNotifier notifier = _dataStore.getDomainNotifier();
        notifier.fireDomainChanged(new DomainEvent(DomainEvent.FILE_CHANGE,
                                                   object,
                                                   DE.P_NESTED));
      }


  public void sendUpdates()
      {
	if (_dataStore != null)
	  {
	    IDomainNotifier notifier = _dataStore.getDomainNotifier();
	    while (_dataObjects.size() > 0)
		{
		  DataElement object = null;
		  synchronized (_dataObjects)
		      {
			  if (_dataObjects.size() > 0)
			      {
				  object = (DataElement)_dataObjects.get(0); 
				  _dataObjects.remove(object);
			      }
		      }

		  if ((object != null) && (!object.isUpdated()))
		      {	
			  clean(object);
			  notify(object);
		      }
		}
	  }
      }


  public void notify(DataElement object)
      {    
	  DataElement parent = object;
        object.setUpdated(true);
        object.setExpanded(true);
	
        IDomainNotifier notifier = _dataStore.getDomainNotifier();

        if (object.getNestedSize() == 0)
        {	      
          notifier.fireDomainChanged(new DomainEvent(DomainEvent.NON_STRUCTURE_CHANGE,
                                                     object,
                                                     DE.P_NESTED));
          
        }
        else
        {	
          notifier.fireDomainChanged(new DomainEvent(DomainEvent.INSERT,
                                                     object,
                                                     DE.P_NESTED));   
        }
      }

}


