package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.*; 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.resource.*;

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
      IProject project = e.getProject();
	
      String path = null;
      if (e instanceof ResourceElement)
	  {
	      ResourceElement res = (ResourceElement)e;
	      path = res.getElement().getSource();
	  }
      else
	  {
	      path = e.getLocation().toString();
	  }

      DataElement projectElement = api.findProjectElement(project);
      if (projectElement != null)
	  { 
	      DataStore dataStore = projectElement.getDataStore();
	      ArrayList parseReferences = projectElement.getAssociated("Parse Reference");
	      if (parseReferences.size() > 0)
		  {
		      DataElement projectParseInformation = ((DataElement)parseReferences.get(0)).dereference();
		      DataElement parsedSource = dataStore.find(projectParseInformation, DE.A_NAME, "Parsed Files", 1);
		     
		      String path1 = path;		      
		      path1 = path1.replace('/', '\\');
		      String path2 = path1.replace('\\', '/');
		      if (parsedSource != null)
			  {
			      DataElement pathElement = dataStore.find(parsedSource, DE.A_SOURCE, path1, 1);
			      if (pathElement == null)
				  {
				      pathElement = dataStore.find(parsedSource, DE.A_SOURCE, path2, 1);
				  }
			      
			      return pathElement;
			  }
		  }
	  }
      return null;
  }


  public DataElement getContentOutline(IAdaptable e)
  {
      //    return parse((IFile) e);
      return getElementRoot((IFile) e);
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

    DataElement parseMinerData = dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.parser.ParseMiner");

    if (project != null)
	{
	    String prName = project.getName().replace('\\', '/');
	    DataElement projectObj = dataStore.find(parseMinerData, DE.A_NAME, project.getName(), 1);
	    
	    if (projectObj != null)
		{
		    DataElement parsedFiles = dataStore.find(projectObj, DE.A_NAME, "Parsed Files", 1);
		    DataElement pathElement = dataStore.find(parsedFiles, DE.A_NAME, path, 1);
		    
		    if (pathElement != null)
			{
			    api.parse(e);
			}
		    
		    return pathElement;
		}
	}
    return null;
  }
}
