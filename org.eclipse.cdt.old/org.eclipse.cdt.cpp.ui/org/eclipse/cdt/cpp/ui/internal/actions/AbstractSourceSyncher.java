package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */


import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;

import java.io.*;
import java.util.*;
import java.lang.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
 
public abstract class AbstractSourceSyncher extends Thread
{
    public ArrayList _changes;
    public ArrayList _projects;


    public AbstractSourceSyncher(ArrayList projects)
    {
	_changes = new ArrayList();
	_projects = projects;
    }
    
    public abstract void run();
    
    public boolean performSynchronization(Repository src, Repository target)
    {
	boolean changed = false;
	DataElement srcElement = src.getElement();
	DataElement targetElement = target.getElement();
	
	String path1 = src.getFullPath().toOSString();
	String path2 = target.getFullPath().toOSString();
	
	File dir1 = new File(path1);
	File dir2 = new File(path2);
	
	Object children[] = src.getChildren(null);
	target.getChildren(null);
	for (int i = 0; i < children.length; i++)
	    {
		ResourceElement resource = (ResourceElement)children[i];
		DataElement element = resource.getElement();
		
		if (resource instanceof IFile)
		    {
			boolean force = false;
			FileResourceElement srcRes = (FileResourceElement)resource;
			FileResourceElement res = (FileResourceElement)target.findResource(element.getName());
			if (res == null)
			    {
				res = (FileResourceElement)target.createResource("file", element.getName());
				force = true;
			    }
			
			if (synchFiles(srcRes, res, force))
			    {
				target.getElement().fireDomainChanged();
				changed = true;
			    }
		    }		
		else if (resource instanceof IFolder)
		    {
			ResourceElement res = target.findResource(element.getName());			    
			if (res == null)
			    {
				res = target.createResource("directory", element.getName());
				target.getElement().fireDomainChanged();
			    }
			
			if (performSynchronization(resource, res))
			    {
				changed = true;
			    }
		    }
	    }
	
	if (changed)
	    {
		target.getElement().fireDomainChanged();
	    }
	
	return changed;
    }	
    
    public boolean performSynchronization(ResourceElement src, ResourceElement target)
    {
	boolean changed = false;
	DataElement srcElement = src.getElement();
	DataElement targetElement = target.getElement();
	
	String path1 = src.getFullPath().toOSString();
	String path2 = target.getFullPath().toOSString();
	
	File dir1 = new File(path1);
	File dir2 = new File(path2);
	
	Object children[] = src.getChildren(null);
	target.getChildren(null);
	for (int i = 0; i < children.length; i++)
	    {
		ResourceElement resource = (ResourceElement)children[i];
		DataElement element = resource.getElement();
		if (resource instanceof IFile)
		    {
			boolean force = false;
			FileResourceElement srcResource = (FileResourceElement)resource;
			FileResourceElement res = (FileResourceElement)target.findResource(element.getName());
			if (res == null)
			    {
				res = (FileResourceElement)target.createResource("file", element.getName());
				force = true;
			    }
			
			if (synchFiles(srcResource, res, force))
			    {
				target.getElement().fireDomainChanged();
				changed = true;
			    }
		    }		
		else if (resource instanceof IFolder)
		    {
			ResourceElement res = target.findResource(element.getName());			    
			if (res == null)
			    {
				res = target.createResource("directory", element.getName());
				target.getElement().fireDomainChanged();
			    }
			
			if (performSynchronization(resource, res))
			    {
				changed = true;
			    }
		    }
	    }
	
	return changed;
    }	
    
    
    public boolean synchFiles(FileResourceElement src, FileResourceElement target, boolean force)
    {
	boolean changed = false;
	long stamp1 = src.getModificationStamp();
	long stamp2 = target.getModificationStamp();
	if ((stamp1 > stamp2))
	    {
		_changes.add(target);
		try
		    {
			InputStream inFile = src.getContents();
			if (inFile != null)
			    {
				String srcPath = src.getFullPath().toOSString();
				String targetPath = target.getFullPath().toOSString();
				
				target.setContents(inFile, true, true, null);
				target.setModificationStamp(stamp1);
				changed = true;
			    }
		    }
		catch (CoreException e)
		    {
		    }
	    }
	return changed;
    }
}
