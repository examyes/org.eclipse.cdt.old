package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.*; 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import java.util.*;

public class DataElementAdapter
{
  private static DataElementAdapter _instance = new DataElementAdapter();

  private DataElementAdapter()
  {
  }

  public static DataElementAdapter getInstance()
  {
    return _instance;
  }

  public DataElement getElementRoot(IFile e)
  {
    ModelInterface api = ModelInterface.getInstance();
    IProject project = (IProject)CppPlugin.getDefault().getCurrentProject();
    DataStore dataStore = DataStoreCorePlugin.getPlugin().getCurrentDataStore();

    String path = new String(e.getLocation().toOSString());

    DataElement parseMinerData = dataStore.findMinerInformation("com.ibm.cpp.miners.parser.ParseMiner");
    DataElement projectObj = dataStore.find(parseMinerData, DE.A_NAME, project.getName(), 1);
   
    if (projectObj != null)
	{
	    DataElement parsedFiles = dataStore.find(projectObj, DE.A_NAME, "Parsed Files", 1);
	    DataElement pathElement = dataStore.find(parsedFiles, DE.A_NAME, path, 1);

	    return pathElement;
	}

    return null;
  }


  public DataElement getContentOutline(IAdaptable e)
  {
    return parse((IFile) e);
  }

  public DataElement parse(IFile e)
  {
    DataStore dataStore = null;
    DataElement file = null;
    String path = null;

    if (e instanceof ResourceElement)
      {
	dataStore = ((DataElement)((ResourceElement)e).getElement()).getDataStore();
	file = ((ResourceElement)e).getElement();
	path = new String(file.getAttribute(DE.A_SOURCE));      	
      }
    else
    {
      dataStore = DataStoreCorePlugin.getPlugin().getCurrentDataStore();
      path = new String(e.getLocation().toOSString());
    }

    IProject project = CppPlugin.getDefault().getCurrentProject();
    ModelInterface api = ModelInterface.getInstance();

    DataElement parseMinerData = dataStore.findMinerInformation("com.ibm.cpp.miners.parser.ParseMiner");
    
    DataElement projectObj = dataStore.find(parseMinerData, DE.A_NAME, project.getName(), 1);
   
    if (projectObj != null)
	{
	    DataElement parsedFiles = dataStore.find(projectObj, DE.A_NAME, "Parsed Files", 1);
	    DataElement pathElement = dataStore.find(parsedFiles, DE.A_NAME, path, 1);

	    return pathElement;
	}
    return null;
  }
}
