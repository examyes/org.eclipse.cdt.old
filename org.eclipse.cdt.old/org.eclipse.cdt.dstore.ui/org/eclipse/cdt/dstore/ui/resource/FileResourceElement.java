package com.ibm.dstore.ui.resource;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;

import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.Viewer;

import java.util.*; 
import java.io.*;
import java.lang.reflect.*;

/****/
import org.eclipse.core.internal.utils.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;


import org.eclipse.swt.widgets.Shell;

public class FileResourceElement extends ResourceElement implements IFile				     
{  
    private java.io.File _mountedFile = null;

  public FileResourceElement (DataElement e, IProject project)
  {
    super(e, project);    
    initializePath(project);

  }

  public FileResourceElement (DataElement e, Object parent, IProject project)
  {
    super(e, parent, project);    
    initializePath(project);
  }

    public void initializePath(IProject project)
    {
       	if (project != null)
	    {
		IPath newPath = project.getFullPath();
		QualifiedName propertyQName = new QualifiedName("Mount Point", newPath.toString());
		
		String mountPoint = null;
		
		try
		    {			
			ArrayList savedProperty = new ArrayList();
			String propertyString = project.getPersistentProperty(propertyQName);
			if (propertyString != null && propertyString.length() != 0)
			    {
				StringTokenizer st = new StringTokenizer(propertyString, "|", false);
				while (st.hasMoreTokens())
				    {
					savedProperty.add(st.nextToken());
				    }
			    }

			if (savedProperty.size() > 0)
			    {
				mountPoint = (String)savedProperty.get(0);				
			    }
		    }
		catch (CoreException e)
		    {
		    }
		
		if (mountPoint != null && mountPoint.length() > 0)
		    {
			StringBuffer mFName = new StringBuffer();
			IResource resource = this;
			while (!(resource instanceof IProject))
			    {
				mFName.insert(0, resource.getName());
				resource = resource.getParent();
				mFName.insert(0, java.io.File.separator);
			    }
			
			mFName.insert(0, mountPoint);
			String path = mFName.toString();
			java.io.File file = new java.io.File(path);
			if (file.exists())
			    {
				_mountedFile = file;
				_path = new Path(_mountedFile.getAbsolutePath());	  
			    }
		    }
	    }

    }

  public boolean exists(boolean c, boolean p)
      {
        return true;
      }
  

  public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException
  {
    return null;
  }

  public void create(InputStream content, boolean force, IProgressMonitor monitor) throws CoreException 
  {
  }

    public void setMountedFile(java.io.File file)
    {
	_mountedFile = file;
	if (_mountedFile != null)
	    {
		_path = new Path(_mountedFile.getAbsolutePath());	  
	    }
    }

    public java.io.File getMountedFile()
    {
	return _mountedFile;
    }

  public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException
  {
  }

  public void setContents(InputStream in, boolean b, IProgressMonitor m)
  {    
  }

  public void setContents(InputStream in, boolean a, boolean b, IProgressMonitor monitor)
  {    
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    
    if (monitor != null)
	{
	    monitor = Policy.monitorFor(monitor);
	}
    try 
      {
	  if (monitor != null)
	      {
		  monitor.beginTask(Policy.bind("settingContents", new String[] {getFullPath().toString()}), Policy.totalWork);
	      }
	internalSetContents(in, monitor);	  

	long time = System.currentTimeMillis();
	setModificationStamp(time);

	/*
	ResourceInfo info = getResourceInfo(false, true);
	info.incrementContentId();
	((Workspace)workspace).updateModificationStamp(info);  
	*/
      } 
    finally 
      {
	  if (monitor != null)
	      {
		  monitor.done();
	      }
      }
  }

  private void internalSetContents(InputStream in, IProgressMonitor monitor)
  {
      if (_mountedFile == null)
	  {
	      String localPath = getFullPath().toOSString();
	      
	      try
		  {	
		      java.io.File theFile = new java.io.File(localPath);    
		      FileOutputStream output = new FileOutputStream(theFile);
		      transferStreams(in, output, monitor);
		      updateRemoteFile(theFile);
		  }
	      catch (IOException e)
		  {
		  }     
	  }
      else
	  {
	      try
		  {	
		      FileOutputStream output = new FileOutputStream(_mountedFile);
		      transferStreams(in, output, monitor);
		  }
	      catch (IOException e)
		  {
		  }     	      
	  }
  }

  private void updateRemoteFile(java.io.File theFile)
      {
        // now update remote file
        String localPath = getFullPath().toString();
        localPath = localPath.replace('\\', '/');
        String remotePath = (String)_element.getElementProperty(DE.P_SOURCE_NAME);
        remotePath = remotePath.replace('\\', '/');

        if (!remotePath.equals(localPath))
	{		  
	  _element.getDataStore().replaceFile(remotePath, theFile);
	} 
      }

public void transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor) throws IOException {
  try 
    {
      StringBuffer fileBuffer = new StringBuffer();
      
      byte[] buffer = new byte[8192];
      while (true) 
	{
	  int bytesRead = source.read(buffer);
	  if (bytesRead == -1)
	    break;
	  destination.write(buffer, 0, bytesRead);
	  fileBuffer.append(buffer);
	  
	  if (monitor != null)
	      {
		  monitor.worked(1);
	      }
	}

    } 
  finally 
    {
    try 
      {
	source.close();
      } 
    catch (IOException e) 
      {
      }
    try 
      {
	destination.close();
      } 
    catch (IOException e) 
      {
      }
  }
}
  

  public int getType() 
  {
    return IResource.FILE;    
  }
  
  public Object getAdapter(Class aClass)
  {
    Object result = null;
    
    if (aClass == org.eclipse.ui.model.IWorkbenchAdapter.class)    
      {
	return this;	
      }
    
    result = _element.getAdapter(aClass); 
    if (result != null)
      {
	return result;	
      }
    else
      {	
	return Platform.getAdapterManager().getAdapter(this, aClass);
      }  
  }

  public InputStream getContents() throws CoreException 
    {
	return getContents(false);
    }

  public InputStream getContents(boolean force) throws CoreException 
  {
    InputStream result = null;
    java.io.File fileObject = _mountedFile;

    try
    {
	if (fileObject == null)
	    {
		fileObject = _element.getFileObject();
		String fileName = fileObject.getAbsolutePath();

		if (!_path.toString().equals(fileName))
		    {
			_path = new Path(fileName);	  
		    }		
	    }

      if (fileObject != null && fileObject.exists())
	{
	  result = new FileInputStream(fileObject); 	
	}
    }
    catch (FileNotFoundException e)
    {
    }
    
    return result;
  }  

    
  public String getExtendedType() throws CoreException
  {
      return _path.getFileExtension();
  }
  
  public String getFileExtension() 
  {
      return _path.getFileExtension();
  }
  
  public IPath getFullPath()
  {
    return _path;
  }
  
  public IPath getLocation()
    {
	return _path;
    }

   
  public Object[] getChildren(Object o) 
  {
    return new Vector(0).toArray();    
  }  

  public boolean equals(Object other)
  {
      if (other instanceof ResourceElement)
      {
	ResourceElement otherFile = (ResourceElement)other;
	String fileName = getFileName();
	 
 	if (fileName.equals(otherFile.getFileName()))
	  {
	    return true;
	  }	
      }
    
    return false;    
  }

  public boolean isLocal(int depth)
  {
    return true;    
  }
  
  public boolean isPhantom()
  {
    return false;
  }

  public boolean isPhantom(int flags)
  {
    return false;
  }

    public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
	throws CoreException
    {
    }



}


