package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.core.model.*;

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.*;

public class CppBuilder extends IncrementalProjectBuilder
{
    private static CppBuilder _instance = new CppBuilder();


    public CppBuilder()
    {
    }

    public static CppBuilder getInstance()
    {
	return _instance;
    }

    public void doBuild(DataElement project)
    {
	doBuild(project, true);
    }

    public void doBuild(DataElement project, boolean isBuild)
    {
	ModelInterface api = CppPlugin.getModelInterface();	
	
	IProject projectR = api.findProjectResource(project); 

	String invocation = getInvocation(projectR, isBuild);
	
	DataElement status = api.invoke(project, invocation, false);
	
    }

    public void doBuild(IProject project)
    {
	doBuild(project, true);
    }

    public void doBuild(IProject project, boolean isBuild)
    {
        if ((project != null) && project.isOpen() &&
	    (CppPlugin.getDefault().isCppProject(project)))
	    {
    		ModelInterface api = CppPlugin.getModelInterface();	
			
    		String path = new String(project.getLocation().toOSString());	
   		String invocation = getInvocation(project, isBuild);
		DataElement projectElement = api.findProjectElement(project);

   		DataElement status = api.invoke(projectElement, invocation, false);

	    }		
    }

    public IProject[] build(int kind, Map m, IProgressMonitor monitor) throws CoreException
    {

        // kind of build being requested: FULL_BUILD, INCREMENTAL_BUILD or AUTO_BUILD.

        IProject project = getProject();
        boolean goAndBuild = false;

        if (kind == AUTO_BUILD)
        {
           IResourceDelta pchange = getDelta(project);
           if (pchange != null)
           {
	      IResourceDelta[] children = pchange.getAffectedChildren();
	      for (int i = 0; (goAndBuild == false) && (i < children.length); ++i) 
              {
		  IResourceDelta change = children[i];
		  if (change != null)
                {
		    IResource resource = change.getResource();
		    if (resource instanceof IProject)
			{
			}
		    else
			{
			    int ckind = change.getKind();
			    boolean isAdded = ckind == IResourceDelta.ADDED;
			    boolean isRemoved = ckind == IResourceDelta.REMOVED;
			    boolean isChanged = ckind == IResourceDelta.CHANGED;
			    int flags = change.getFlags();		
			    boolean contentChanged = (isChanged  && (flags & IResourceDelta.CONTENT) != 0);
			    if (contentChanged)
				{
				    // is this an editable resource
				    ModelInterface api = CppPlugin.getModelInterface();	
				    if (api.isBeingEdited(resource))
					{
					    goAndBuild = true;
					}
				}
			}
		}
	      }
	   }
	}
	else
        {
           goAndBuild = true;
        }

        if (goAndBuild == true)
        {
        	  monitor.beginTask("Building " + project.getName(), 100);
		  doBuild(project);	
        }



        return null;
    }

  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
      {
        super.setInitializationData(config, propertyName, data);
      }

  static private String getInvocation(IProject project)
    {
	return getInvocation(project, true);
    }

  static private String getInvocation(IProject project, boolean isBuild)
      {
        if (project != null)
        {
	    String property = "Build History";
	    if (!isBuild)
		{
		    property = "Clean History";
		}
	    
          ArrayList history = CppPlugin.readProperty(project, property);
          if ((history != null) && (history.size() > 0))
            return new String((String)history.get(0));
	  else
	      return "gmake";
        }

        return null;
      }
}
