package com.ibm.dstore.ui.resource;

/* 
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.DataStoreCorePlugin;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.Viewer;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
 
/***/
import org.eclipse.core.internal.resources.*;

import org.eclipse.core.resources.*; 
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Shell;

public class ResourceElement extends Container implements IDesktopElement, IDataElementContainer				     
{  
    protected DataElement _element;
    protected Object _parent;
    
    protected DataElement _resourceDescriptor;
    protected Path        _path;
    protected Path        _localPath;
    
    protected Vector   _children;
    protected IProject _project;  
    protected DataStore _dataStore;
  
    protected long  _modificationStamp;

  public ResourceElement (DataElement e, IProject project)
  {
    super(new Path(e.getAttribute(DE.A_SOURCE)), (Workspace)ResourcesPlugin.getWorkspace()); 

    _project = project;
    _path = new Path(e.getAttribute(DE.A_SOURCE));
    _localPath = new Path(e.getAttribute(DE.A_SOURCE));
    _parent = null; 
    _element = e;    
    _dataStore = _element.getDataStore();
    _resourceDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, "directory", 1); 
    _modificationStamp = 0;
  }

  public ResourceElement (DataElement e, Object parent, IProject project)
  {
    super(new Path(e.getAttribute(DE.A_SOURCE)), (Workspace)ResourcesPlugin.getWorkspace()); 

    _project = project;
    _path = new Path(e.getAttribute(DE.A_SOURCE));
    _localPath = new Path(e.getAttribute(DE.A_SOURCE));
    _parent = parent;
    _element = e;    
    _dataStore = _element.getDataStore();
    _resourceDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, "directory", 1);    
    _modificationStamp = 0;
  }

  public void contributeActions(MenuManager menu, Object element, IStructuredSelection selection) 
  {
    System.out.println("contributing...");
  }

  public DataStore getDataStore()
  {
    return _element.getDataStore();    
  }
  
  public DataElement getElement()
  {
    return _element;    
  }
  
  public int getType() 
  {
    String type = _element.getType();
    if (type.equals("project"))
      {
	return Resource.PROJECT;	
      }  
    else if (type.equals("directory"))
      {
	return Resource.FOLDER;
      }
    else
      {	
	return Resource.FILE;
      } 
  }

  public DataElement toElement(Object object)
      {
        DataElement element = null;
        if (object instanceof DataElement)
        {
          element = (DataElement)object;
        }        
	else if (object instanceof ResourceElement)
	  {
	    element = _element;	    
	  }	
        else
        {
          element = _element;
        }
        return element;
      }
  
  public IResource[] getChildren(Container parent, boolean phantom) 
    {
	return getChildren(parent.getFullPath(), phantom);
    }

    public IResource[] getChildren(IPath path, boolean phantom) 
    {
	if (_children == null)
	    {
		_children = new Vector();
	    }

	if (_children.size() == 0)
	    {
		getChildren(null);
	    }

	IResource[] resources = new IResource[_children.size()];
	for (int i = 0; i < _children.size(); i++)
	    {
		ResourceElement child = (ResourceElement)_children.get(i);
		if (!child.getElement().isDeleted())
		    {
			resources[i] = (IResource)child;
		    }
		else
		    {
			//_children.remove(child);
		    }
	    }
	
	return resources;
    }

  public Object[] getChildren(Object o) 
    {
	return getChildren(o, false);
    }

  public Object[] getChildren(Object o, boolean force) 
  {
      if ((_children == null) || (_children.size() == 0) || force)
      {	
	if (_children == null)
	  _children = new Vector();

	DataElement element = toElement(o);
	if (!element.getAttribute(DE.A_TYPE).equals("file"))
	  {	    
    	      element.expandChildren(true);
	    
	    ArrayList objs = element.getNestedData();
	
	    // hard-coded for now
	    for (int i = 0; i < objs.size(); i++)
	      {
		DataElement obj = (DataElement)objs.get(i);
		if (!obj.isDeleted() && obj.getDataStore().filter(_resourceDescriptor, obj))
		  {	    
		    String type = obj.getType();
		    String name = obj.getName();
		    ResourceElement child = findResource(name);
		    if (child != null)
			{
			    DataElement childElement = child.getElement();
			    if (childElement.isDeleted())
				{
				    _children.remove(child);
				    if (!obj.isDeleted())
					{
					    if (type.equals("project"))
						{
						    child = new ProjectResourceElement(obj, _project);				    
						}		
					    else if (type.equals("directory"))
						{
						    child = new FolderResourceElement(obj, this, _project);		
						}
					    else 
						{
						    child = new FileResourceElement(obj, this, _project);		
						}
					    
					    _children.add(child);
					}
				}
			}
		    else
			{
			    if (type.equals("project"))
				{
				    child = new ProjectResourceElement(obj, _project);				    
				}		
			    else if (type.equals("directory"))
				{
				    child = new FolderResourceElement(obj, this, _project);		
				}
			    else 
				{
				    child = new FileResourceElement(obj, this, _project);		
				}
			    
			    _children.add(child);
			}
		  }	
	      }
	    
	  }
      }
    
    return _children.toArray();
  }

  public Image getImage(Object object, Viewer owner) 
  {
      if (getType() == Resource.FILE)
	  {
	      ImageDescriptor imageDes = org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getEditorRegistry().getImageDescriptor(getName());
	      return imageDes.createImage();
	  }
      else
	  {
	      DataElement element = toElement(object);
	      
	      String imageFile = DataElementLabelProvider.getImageString(element);
	      return DataStoreCorePlugin.getPlugin().getImage(imageFile, false);
	  }
  }

  public ImageDescriptor getImageDescriptor(Object object) 
  {
      if (getType() == Resource.FILE)
	  {
	      return org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getEditorRegistry().getImageDescriptor(getName());
	  }
      else
	  {
	      DataElement element = toElement(object);
	      
	      String imageFile = DataElementLabelProvider.getImageString(element);
	      return DataStoreCorePlugin.getPlugin().getImageDescriptor(imageFile, false);
	  }

  }

  public String getPersistentProperty(QualifiedName key) throws CoreException 
  {
    return null;
  }

    public IProject getProject()
    {
	return _project;
    }

  public String getLabel(Object o) 
  {
    return (String)_element.getElementProperty(DE.P_VALUE);
  }

  public String getName()
  {
    return (String)_element.getElementProperty(DE.P_NAME);    
  }

  public String getFileName()
  {
    return _path.toString();    
  }

  public Object getParent(Object o) 
  {
    return _parent;
  }

  public IContainer getParent() 
  {
    return (IContainer)_parent;
  }

  public Object getAdapter(Class aClass)
  { 
    if (aClass == org.eclipse.ui.model.IWorkbenchAdapter.class)    
      {
	return this;	
      }
    
    return _element.getAdapter(aClass); 
  }
  
    public IResource[] members() throws CoreException 
    {
	return members(false);
    } 

    public IResource[] members(boolean phantom) throws CoreException 
    {
	return getChildren((Container)this, true); 
    } 

  public IPath getLocation()
  {
    return _path;    
  }

  public IPath getLocalLocation()
  {
    return _localPath;    
  }

  public boolean exists() 
  {
    return true;
  }

    public boolean exists(IPath p)
	{
	    return true;
	}

    public void checkExists(int flags, boolean checkType)
    {
    }

    public void checkAccessible(int flags) throws CoreException
    {
    }

    public void accept(IResourceVisitor visitor) throws CoreException
    {
    }

    public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException
    {
    }

    public boolean isReadOnly()
    {
	return false;
    }

    private void deleteLocalFile()
    {
	java.io.File fileObject = _element.getFileObject();
	if (fileObject != null && fileObject.exists())
	    {
		fileObject.delete();
	    }
    }

    public void delete(boolean force, IProgressMonitor monitor) throws CoreException 
    {
	delete(force, false, monitor);
    }

    public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException 
    {
	if (_parent instanceof IDataElementContainer)
	    {
		((IDataElementContainer)_parent).remove(this);
	    }

	deleteLocalFile();
	DataStore dataStore = _element.getDataStore();	
        DataElement deleteDescriptor = dataStore.localDescriptorQuery(_element.getDescriptor(), "C_DELETE");
        if (deleteDescriptor != null)
        {	 
	    dataStore.command(deleteDescriptor, _element);
	    IContainer parent = getParent();
	    if (parent instanceof ResourceElement)
		{
		    ((ResourceElement)getParent()).remove(this);
		}
        }
    }

    public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException 
    {
	move(destination, force, false, monitor);
    }

    public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException
    {
	System.out.println("moving");
    }

    public void remove(ResourceElement child)
    {
	_children.remove(child);
    }

    public IMarker createMarker(String type) throws CoreException
    {
	IMarker result = new ElementMarker(this, type);
	return result;
    }

    public void setModificationStamp(long stamp)
    {
	_modificationStamp = stamp;
	// for now, we'll get the actual modification date
	DataElement dateDescriptor = _dataStore.localDescriptorQuery(_element.getDescriptor(), "C_SET_DATE");
	if (dateDescriptor != null)
	    {
		ArrayList args = new ArrayList();
		args.add(_dataStore.createObject(null, "date", "" + stamp));
		_dataStore.synchronizedCommand(dateDescriptor, args, _element);
	    }
    }

    public long getModificationStamp()
    {
	if (_modificationStamp == 0)
	    {
		DataElement dateObj = null;
		ArrayList timeArray = _element.getAssociated("modified at");
		if (timeArray.size() > 0)
		    {
			dateObj = (DataElement)timeArray.get(0); 
		    }
		
		DataElement status = _element.doCommandOn("C_DATE", true);

		if (status != null && status.getNestedSize() > 0)
		    {
			dateObj = status.get(0);
		    }
		    
		if (dateObj != null)
		    {
			Long date = new Long(dateObj.getName());
			_modificationStamp = date.longValue(); 
		    }
		else
		    {
			_modificationStamp = -1;
		    }
	    }
	return _modificationStamp;
    }

    public ResourceElement findResource(String name)
    {
	if (_children != null)
	    {
		for (int i = 0; i < _children.size(); i++)
		    {
			ResourceElement resource = (ResourceElement)_children.get(i);
			if (resource.getName().equals(name))
			    {
				return resource;
			    }
		    }
	    }

	return null;
    }

    public ResourceElement findResource(DataElement element)
    {
	if (_children != null)
	    {
		for (int i = 0; i < _children.size(); i++)
		    {
			ResourceElement resource = (ResourceElement)_children.get(i);
			if (resource.getElement() == element)
			    {
				return resource;
			    }
			else
			    {
				ResourceElement subResource = resource.findResource(element);
				if (subResource != null)
				    {
					return subResource;
				    }
			    }
		    }
	    }	
	return null;
    }
    
    public ResourceElement createResource(String type, String name)
    {
	if (_children == null)
	    {
		_children = new Vector();
	    }

	String path = getFullPath() + java.io.File.separator + name;
	DataElement newResource = _dataStore.createObject(getElement(), type, name, path);

	ResourceElement child = null;
	if (type.equals("directory"))
	    {
		child = new FolderResourceElement(newResource, this, _project);		
	    }
	else 
	    {
		child = new FileResourceElement(newResource, this, _project);		
	    }
	
	_children.add(child);
	
	ArrayList args = new ArrayList();
	args.add(newResource);
	
	DataElement createDescriptor = _dataStore.localDescriptorQuery(getElement().getDescriptor(), "C_CREATE");
        if (createDescriptor != null)
	    {	
		_dataStore.synchronizedCommand(createDescriptor, args, getElement());
	    }

	return child;
    }

    public void removeChildren()
    {
	if (_children != null)
	    {
		for (int i = 0; i < _children.size(); i++)
		    {
			ResourceElement child = (ResourceElement)_children.get(i);
			child.removeChildren();
		    }
		
		_children.clear();
	    }
    }

    public void refreshLocal(int depth, IProgressMonitor monitor)
    {
	if (depth > 0)
	    {
		if (_element.isExpanded())
		    {
			DataElement refreshDescriptor = _dataStore.localDescriptorQuery(_element.getDescriptor(), "C_REFRESH");
			if (refreshDescriptor != null)
			    {	
				_dataStore.synchronizedCommand(refreshDescriptor, _element);	    
				
				Object[] children = getChildren(_element, true);
				for (int i = 0; i < children.length; i++)
				    {
					ResourceElement child = (ResourceElement)children[i];
					child.refreshLocal(depth - 1, null);
				    }
			    }
		    }
	    }  
    }

    public String toString()
    {
	return "ResourceElement:" + _element.getType() + ":" + _element.getName();
    }

    public void update()
    {
	getChildren(_element, true);
    }
}




