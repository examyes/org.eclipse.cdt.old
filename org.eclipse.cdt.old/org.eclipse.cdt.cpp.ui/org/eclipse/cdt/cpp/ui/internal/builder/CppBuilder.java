package com.ibm.cpp.ui.internal.builder;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.dialogs.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

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
    public CppBuilder()
    {
    }

    static public void doBuild(DataElement project)
    {
	ModelInterface api = CppPlugin.getModelInterface();	
	
	IProject projectR = api.findProjectResource(project); 
	String invocation = getInvocation(projectR);
	
	api.invoke(project, invocation, false);
    }

    static public void doBuild(IProject project)
    {
        if ((project != null) && project.isOpen() &&
	    (CppPlugin.getDefault().isCppProject(project)))
	    {
    		ModelInterface api = CppPlugin.getModelInterface();	
			
    		String path = new String(project.getLocation().toOSString());	
   		String invocation = getInvocation(project);
		DataElement projectElement = api.findProjectElement(project);

   		api.invoke(projectElement, invocation, false);
	    }		
    }

    public IProject[] build(int kind, Map m, IProgressMonitor monitor) throws CoreException
    {

        // kind of build being requested: FULL_BUILD, INCREMENTAL_BUILD or AUTO_BUILD.

        IProject project = getProject();
        boolean goAndBuild = false;

        if (kind == AUTO_BUILD)
        {
           IResourceDelta change = getDelta(project);
           if (change != null)
           {
              IResourceDelta[] children = change.getAffectedChildren();
              for (int i = 0; i < children.length; ++i)
              {
                if (children[i] != null)
                {
                  goAndBuild = true;
                  break;
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
        if (project != null)
        {
          ArrayList history = CppPlugin.readProperty(project, "Build History");
          if ((history != null) && (history.size() > 0))
            return new String((String)history.get(0));
	  else
	      return "gmake";
        }

        return null;
      }
}
