package org.eclipse.cdt.dstore.ui.resource;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.Viewer;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.eclipse.ui.internal.model.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Shell;

public class ProjectResourceElement extends ResourceElement implements IProject			     
{  
  public ProjectResourceElement (DataElement e, IProject project)
  {
    super(e, project);    
  }

  public int getType() 
  {
    return Resource.PROJECT;    
  }

    /*
  public void addMapping(IResourceMapping mapping) throws CoreException
  {
  }
    */

  public IProjectNature addNature(String natureId) throws CoreException
  {
    return null;    
  }
  
  public void build(int kind, IProgressMonitor monitor) throws CoreException
  {
    System.out.println("calling build");
    
  }
  
  public void build(int kind, String builderName, Map args, IProgressMonitor monitor) throws CoreException
  {
    System.out.println("calling build");
    
  }
  
  public void close(IProgressMonitor monitor) throws CoreException
  {
  }

  public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException
  {
  }
  
  public void create(IProgressMonitor monitor) throws CoreException
  {
  }
  
  public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException
  {
  }
  
  public IProjectDescription getDescription() throws CoreException
  {
    return null;    
  }
  
    /*
  public IResourceMapping getMapping(String name) throws CoreException
  {
    return null;    
  }
  
  public Map getMappings() throws CoreException
  {
    return null;
  }
    */
  
  public IProjectNature getNature(String natureId) throws CoreException
  {
    return null;    
  }
  
  public String[] getNatureIds() throws CoreException
  {
    return null;  
  }
  
  public IPath getPluginWorkingLocation(IPluginDescriptor plugin)
  {
    return _path;
  }

  public IProject getProject() 
  {
    return this;
  }

  public IContainer getParent() 
  {
    return null;
  }
 
  public boolean hasNature(String natureId) throws CoreException
  {
    return false;
  }
  
  public boolean isOpen()
  {
    return true;
  }
  
  public IProjectDescription newDescription(String projectName)
  {
    return null;
  }
  
    /*
  public IResourceMapping newMapping(String name, IPath local)
  {
    return null;
  }
    */
  public void open(IProgressMonitor monitor) throws CoreException
  {
  }
  
    /*
  public void removeMapping(String name) throws CoreException
  {
  }
    */
  public void removeNature(String natureId) throws CoreException
  {
  }
  
  public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException
  {
  }
  
    /*
  public IStatus validateMapping(IResourceMapping mapping)
  {
    return null;
    } 
    */

  public IResource findMember(IPath path)
  {
    return null;    
  }
  
  public IResource findMember(IPath path, boolean includePhantoms)
  {
    return null;
  }
  
  public IResource findMember(String name)
  {
    return null;
    
  }
  
  public IResource findMember(String name, boolean includePhantoms)
  {
    return null;
  }
  
  public IFile getFile(IPath path)
  {
    return null;
  }
  
  public IFile getFile(String name)
  {
    return null;
  }
  
  public IFolder getFolder(IPath path)
  {
    return null;
  }
  
  public IFolder getFolder(String name)
  {
    return null;
  }
  
  public IResource[] members() throws CoreException
  {
    return null;
  }
  
  public IResource[] members(boolean includePhantoms) throws CoreException
  {
    return null;
  }

    public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException
    {
    }

    public void move(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException
    {
    }

    public IProject[] getReferencedProjects() throws CoreException
    {
	return null;
    }

    public IProject[] getReferencingProjects()
    {
	return null;
    }

}



