package org.eclipse.cdt.dstore.ui.resource;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Shell;

public class FolderResourceElement extends ResourceElement implements IFolder			     
{  
  public FolderResourceElement (DataElement e, IProject project)
  {
    super(e, project);    
  }

  public FolderResourceElement (DataElement e, Object parent, IProject project)
  {
    super(e, parent, project);    
  }


  public int getType() 
  {
    return IResource.FOLDER;    
  }

  public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException
  {
  }
  
    /*
  
  public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException
  {  
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
}

