package com.ibm.dstore.ui;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.DataStoreCorePlugin;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.extra.internal.extra.*;

import java.io.*;
import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.*;

public class ConvertUtility
{
  public static DataElement convert(SelectionChangedEvent e)
  {
    DataElement data = null;
    ISelection selection = e.getSelection();
    IStructuredSelection es= (IStructuredSelection) selection;

    Object obj = es.getFirstElement();
    if (obj instanceof DataElement)
      {	
	data = (DataElement)obj;
      }
    else if (obj instanceof IDataElementContainer)
      {
	IDataElementContainer container = (IDataElementContainer)obj;
	data = container.getElement();	
      }    
    else if (obj instanceof IProject)
	{
	    DataStore dataStore = DataStoreCorePlugin.getInstance().getCurrentDataStore();
	    data = (DataElement)convert(dataStore, dataStore.getRoot(), (IProject)obj);	    
	}
    else if (obj instanceof IResource)
      {	
	    DataStore dataStore = DataStoreCorePlugin.getInstance().getCurrentDataStore();
	    data = (DataElement)convert(dataStore, dataStore.getRoot(), (IResource)obj);
      }
    

    return data;
  }

  public static DataElement convert(ISelection selection)
  {
    DataElement data = null;
    IStructuredSelection es= (IStructuredSelection) selection;
    Object obj = es.getFirstElement();
    
    if (obj instanceof DataElement)
      {	
	data = (DataElement)obj;
      }
    else if (obj instanceof IDataElementContainer)
      {
	IDataElementContainer container = (IDataElementContainer)obj;
	return container.getElement();	
      }    

    return data;
  }

  public static DataElement convert(DataStore dataStore, DataElement root, IAdaptable adp)
  {
    if (adp instanceof IWorkspace)
      {
	return convert(dataStore, root, (IWorkspace)adp);	
      }
    else if (adp instanceof IResource)
      {
	return convert(dataStore, root, (IResource)adp);	
      }
    else
      {
	return null;	
      }    
  }
  

  public static DataElement convert(DataStore dataStore, DataElement root, IWorkspace ws)
  {
    DataElement object = dataStore.find(root, DE.A_NAME, "workspace", 2); 
    if (object == null)
      {
	object = dataStore.createObject(root, "directory", "workspace", Platform.getLocation().toOSString());
      }  

    return object;    
  }
  

  public static DataElement convert(DataStore dataStore, DataElement root, IProject res)
    {
	return dataStore.createObject(root, "directory", res.getName(), res.getLocation().toOSString());
    }

  public static DataElement convert(DataStore dataStore, DataElement root, IResource res)
  {
    DataElement object = dataStore.find(root, DE.A_NAME, res.getName(), 2); 
    if (object == null)
      {
	if (res instanceof ResourceElement)
	  {
	    object = ((ResourceElement)res).toElement(res);	    
	  }	
	else if (res instanceof IFile)
	  {
	    object = dataStore.createObject(root, "file", res.getName(), res.getLocation().toOSString());
	  }
	else if (res instanceof IFolder)
	  {
	    object = dataStore.createObject(root, "directory", res.getName(), res.getLocation().toOSString());
	  }
	else if (res instanceof IProject)
	  {
	    object = dataStore.createObject(root, "directory", res.getName(), res.getLocation().toOSString());
	  }
      }

    return object;    
  }
  
  
  public static DataElement elementToDataElement(DataElement parent, 
                                                 IElement e, 
                                                 DataStore dataStore)
      {
        return null;
      } 
}

