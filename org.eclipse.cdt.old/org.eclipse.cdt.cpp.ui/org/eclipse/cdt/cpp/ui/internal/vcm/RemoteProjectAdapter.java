package org.eclipse.cdt.cpp.ui.internal.vcm;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.views.OutputViewer;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.cdt.dstore.ui.ILinkable;
import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.resource.*;

import org.eclipse.vcm.internal.core.base.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class RemoteProjectAdapter extends ResourceElement
{
    private PlatformVCMProvider _provider;
    static private RemoteProjectAdapter _instance;

  public RemoteProjectAdapter(DataElement root)
  {
    super(root, null, null);    

    _provider = PlatformVCMProvider.getInstance();

    _instance = this;
  }

    static public RemoteProjectAdapter getInstance()
    {
	return _instance;
    }  

  public void setChildren(IProject[] repositories)
  {
  }


  public boolean hasChildren(Object o)
  {
    boolean has = (getProjects() != null);
    return has;    
  } 
  
  public IProject[] getProjects() 
  {    
      return _provider.getKnownProjects();
  }  

  public Object[] getElements(Object o) 
  {    
    return getChildren(o);   
  }  

  public Object[] getChildren(Object o) 
  {    
      return getProjects();
  }  

    public void close()
    {
	IProject[] repositories = getProjects();
	
	if (repositories != null)
	    {
		for (int i = 0; i < repositories.length; i++)
		    {
			Repository repository = (Repository)repositories[i];

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

