package org.eclipse.cdt.dstore.core.client;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
 
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

    public void updateFile(String path, byte[] bytes, int size, boolean binary)
    {
    }

    public void updateAppendFile(String path, byte[] bytes, int size, boolean binary)
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

		  if ((object != null) )
		      {	 
			  clean(object);
			  if (!object.isUpdated())
			      {
				  notify(object);
			      }
		      }
		}
	  }
      }


  public void notify(DataElement object)
      {    
	  DataElement parent = object;

	  if (object.isExpanded())
	      {
		  object.setUpdated(true);
	      }

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


