package com.ibm.cpp.ui.internal.vcm;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.views.OutputViewer;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.ui.ILinkable;
import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;

import org.eclipse.vcm.internal.core.base.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class RemoteProjectAdapter extends ResourceElement
{
  private IProject[] _repositories;
    static private RemoteProjectAdapter _instance;

  public RemoteProjectAdapter(DataElement root)
  {
    super(root, null, null);    
    _repositories = null;
    _instance = this;
  }

    static public RemoteProjectAdapter getInstance()
    {
	return _instance;
    }  

  public void setChildren(IProject[] repositories)
  {
    _repositories = repositories;    
  }


  public boolean hasChildren(Object o)
  {
    boolean has = (_repositories != null);
    return has;    
  } 
  
  public IProject[] getProjects() 
  {    
    return _repositories;   
  }  

  public Object[] getElements(Object o) 
  {    
    return getChildren(o);   
  }  

  public Object[] getChildren(Object o) 
  {    
    return _repositories;   
  }  

    public void close()
    {
	if (_repositories != null)
	    {
	for (int i = 0; i < _repositories.length; i++)
	    {
		Repository repository = (Repository)_repositories[i];

		try
		    {
			repository.close(null);
		    }
		catch (CoreException e)
		    {
			System.out.println(e);
		    }
	    }
	    }
    }
  
}

