package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
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
	      sendUpdates();
          }
      }
    
    protected void clean(DataElement object)
    {
	clean(object, 1);
    }
    
    protected void clean(DataElement object, int depth)
    {
	if (depth > 0)
	    {
		for (int i = 0; i < object.getNestedSize(); i++)
		    {
			DataElement child = object.get(i);
			clean(child, depth - 1);
			if (child.isDeleted())
			    {
				object.removeNestedData(child);
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
