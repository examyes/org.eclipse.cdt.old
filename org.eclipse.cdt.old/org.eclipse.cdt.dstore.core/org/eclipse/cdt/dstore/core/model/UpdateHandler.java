package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
//import com.ibm.datastore.core.extra.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public abstract class UpdateHandler extends Handler
{
  protected  ArrayList    _dataObjects;

  public UpdateHandler()
      {
        _dataObjects = new ArrayList();
      }

  public void handle()
      {
          if (!_dataObjects.isEmpty())
          {
	      System.out.println("update...");
	      sendUpdates();
          }
      }
    
    protected void clean(DataElement object)
    {
	clean(object, 3);
    } 
    
    protected void clean(DataElement object, int depth)
    {
	if ((depth > 0) && (object != null))
	    {
		for (int i = 0; i < object.getNestedSize(); i++)
		    {
			DataElement child = object.get(i);			
			clean(child, depth - 1);
			if (child != null)
			    {
				if (child.isDeleted())
				    {
					synchronized(object)
					    {
						object.removeNestedData(child);
						child.clear();
					    }
				    }						
			    }
		    }
	    }
    }

  public void update(ArrayList objects)
      {
        for (int i = 0; i < objects.size(); i++)
        {
          update((DataElement)objects.get(i));
        }
      }

  public void update(DataElement object)
      {
	  update(object, false);
      }

    public void update(DataElement object, boolean immediate)
      {
	  synchronized (_dataObjects)
	      {
		  if (immediate)
		      {			  
			  _dataObjects.add(0, object);
			  handle();
		      }
		  else
		      {
			  if (!_dataObjects.contains(object))
			      {					  
				  _dataObjects.add(object);
			      }
		      }
	      }
      }

    // implemented by derived
    public abstract void sendUpdates();
    public abstract void updateFile(File file, DataElement associatedElement);
    public abstract void updateFile(String path, byte[] bytes, int size);
    public abstract void updateAppendFile(String path, byte[] bytes, int size);
}
