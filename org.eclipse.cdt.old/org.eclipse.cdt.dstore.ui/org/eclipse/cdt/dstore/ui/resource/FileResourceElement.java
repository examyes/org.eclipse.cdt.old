package com.ibm.dstore.ui.resource;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
  }

  public FileResourceElement (DataElement e, Object parent, IProject project)
  {
    super(e, parent, project);    
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
        String localPath = getFullPath().toOSString();
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

    try
    {
	java.io.File fileObject = _mountedFile;
	if (fileObject == null)
	    {
		fileObject = _element.getFileObject();
	    }

      if (fileObject != null && fileObject.exists())
	{
	  String fileName = fileObject.getAbsolutePath();
	  
	  if (!_path.toOSString().equals(fileName))
	    {
	      _path = new Path(fileName);	  
	    }
	   
	  result = new FileInputStream(fileName); 	
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

  public IPath getLocalLocation()
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


