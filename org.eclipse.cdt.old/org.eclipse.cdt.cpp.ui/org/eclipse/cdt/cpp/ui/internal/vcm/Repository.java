package org.eclipse.cdt.cpp.ui.internal.vcm;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */ 
 
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.actions.*; 
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.builder.*;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.DataStoreCorePlugin;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.cdt.dstore.ui.connections.*;
import org.eclipse.cdt.dstore.ui.*;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.eclipse.jface.action.*;


import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;

import org.eclipse.jface.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.preference.PreferenceManager;

import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.ui.model.*;
import org.eclipse.ui.*;
 
public class Repository extends Project 
    implements IProject, IWorkbenchAdapter, 
    IDataElementContainer, IDomainListener, 
    IResource, IActionFilter
{
  private class PersistentProperty
  {
    public String        _key;
    public String        _value;
    
    public PersistentProperty(QualifiedName key, String value)
    {      
      _key  = key.getQualifier();
      _value = value; 
    }

    public PersistentProperty(String key, String value)
    {
      _key = key;
      _value = value;
    }
    
    public String toString()
    {
      return _key + "\t" + _value;      
    }    
  }

    private class OpenConnectionAction implements Runnable
    {
	private Repository _repository;

	public OpenConnectionAction(Repository repository)
	{
	    _repository = repository;
	}

	public void run()
	{
	    DomainNotifier notifier = _dataStore.getDomainNotifier();
	    ConnectionStatus status = _connection.connect(notifier, "org.eclipse.cdt.cpp.miners");	
	    if ((status != null) && status.isConnected())
		{ 
		    _dataStore = _connection.getDataStore();

		    DataElement hostRoot = _dataStore.getHostRoot();
		    
		    DataElement fsMinerData = _dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.filesystem.FileSystemMiner");
		    _remoteRoot = hostRoot.get(0).dereference();

		    
		    _root.setAttribute(DE.A_ID, _root.getName() + ".repository");

		    _root.addNestedData(_remoteRoot.getNestedData(), false);
		    _root.setDataStore(_remoteRoot.getDataStore());
		    
		    
		    // create directory for project
		    String rootPath = _plugin.getStateLocation().
			append(".repositories").toOSString();

		    String path = rootPath + java.io.File.separator + getName();
		    java.io.File workingPath = new java.io.File(path);
		    if (!workingPath.exists())
			{
			    workingPath.mkdir();	     
			}
		    
		    if (!_connection._isLocal)
			{	
			    _dataStore.setAttribute(DataStoreAttributes.A_LOCAL_PATH, path);
			}


		    ISchemaRegistry registry = _plugin.getSchemaRegistry();
		    registry.extendSchema(_dataStore);		    
		    
		    ModelInterface api = ModelInterface.getInstance();
		    api.openProject(_repository);

		    notifier.addDomainListener(_repository);		    
		}
	    else
		{
		    if (status != null)
			{
			    String msg = status.getMessage();
			    MessageDialog failD = new MessageDialog(null, 
								    "Connection Failure", 
								    null, msg, 
								    MessageDialog.INFORMATION,
								    new String[]  { "OK" },
							    0);
		    
			    
			    failD.openInformation(new Shell(), "Connection Failure", msg);          
			}
		}
	}
    }


    private DataStore _dataStore; 
    private DataElement _root;
    private DataElement _remoteRoot;
    private DataElement _resourceDescriptor;
    private DataElement _closedElement;
    
    private RepositoryDescription _description;  
    
    private IPath _phantomPath;
    private IPath _path;

    private Connection _connection;
    protected Vector  _children;


    private ArrayList _persistentProperties;
    private CppPlugin _plugin;
 
    private Workspace _workspace;
    
    private ArrayList _markers;

    public Repository(Connection connection) 
    {
	super(Platform.getLocation(), (Workspace)ResourcesPlugin.getWorkspace()); 
	
	_workspace = (Workspace)ResourcesPlugin.getWorkspace();
	_dataStore = CppPlugin.getDefault().getCurrentDataStore();
	DataElement fsMinerData = _dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.filesystem.FileSystemMiner");
	_root = fsMinerData.get(0);  
	_path = new Path(_root.getAttribute(DE.A_SOURCE));
	_connection = connection;
	initialize();
    }
    
    public Repository(Connection connection, DataElement root)
    {
	super(Platform.getLocation(), (Workspace)ResourcesPlugin.getWorkspace()); 
	
	_workspace = (Workspace)ResourcesPlugin.getWorkspace();
	_path = new Path(root.getAttribute(DE.A_SOURCE)); 
	_phantomPath = Path.ROOT.append(root.getAttribute(DE.A_NAME)); 
	
	_dataStore = root.getDataStore();
	_connection = connection;
	
	_root = root;    
	initialize();
    }
    
    public void initialize()
    {
	_resourceDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, "directory", 1); 
	
	_plugin = CppPlugin.getDefault();
	_persistentProperties = new ArrayList();
	readProperties();
		
	_children = new Vector();
    }
    
    public void shutdown()
    {
    	saveProperties();	
    } 


    public void setClosedElement(DataElement closedElement)
    {
	_closedElement = closedElement;
    }

    public DataElement getClosedElement()
    {
	return _closedElement;
    }

    public void changePath(String path)
    {
	if (_closedElement != null)
	    {
		_closedElement.setAttribute(DE.A_SOURCE, path);
		_closedElement.getDataStore().refresh(_closedElement);
	    }

	_path = new Path(path); 
    }
    
    public ImageDescriptor getImageDescriptor(Object object)
    {
	return _plugin.getImageDescriptor((String)object);
    }

  public Image getImage(Object object, Viewer owner) 
  {
    ImageDescriptor descriptor = null;    
    if (isOpen())
      {
	descriptor = _plugin.getImageDescriptor("project.gif");	
      }
    else
      {
	descriptor = _plugin.getImageDescriptor("project_closed.gif");		
      } 

    return descriptor.createImage();    
  }

  public String getLabel(Object o) 
  {
    return (String)_root.getElementProperty(DE.P_VALUE);
  }

  public Object getParent(Object o) 
  {
    return null;
  } 

  public void contributeActions(MenuManager menu, Object element, IStructuredSelection selection) 
  {
    System.out.println("contributing...");
  }

  public Connection getConnection()
      {
        return _connection;
      }

  public String getName()
  {
    return _root.getName();    
  }
    
  public DataElement getElement()
  {
    return _root;    
  }

    public DataElement getRemoteElement()
    {
	return _remoteRoot;    
    }
  
  public IPath getLocation()
  {
    return _path;    
  }
  
  public DataStore getDataStore()
  {
      if (_remoteRoot != null)
	  {
	      return _remoteRoot.getDataStore();
	  }

      return _dataStore;
  }

    public boolean hasChildren(Object o)
    {
	if (isOpen())
	    {
		return true;
	    }
	else
	    {
		return false; 
	    }
    }
  

    public IResource[] getChildren(IPath path, boolean phantom) 
    {
	if (_children == null || _children.size() == 0)
	    {
		internalGetChildren(_remoteRoot);
	    }

	IResource[] resources = new IResource[_children.size()];
	for (int i = 0; i < _children.size(); i++)
	    {
		ResourceElement child = (ResourceElement)_children.get(i);
		if (!child.getElement().isDeleted())
		    {
			resources[i] = (IResource)child;
		    }
	    }

	return resources;
    }

  public Object[] getChildren(Object o) 
  {
    if (isOpen())
	{
	    return internalGetChildren(o);
	}
    else
	{
	    return new Vector(0).toArray(); 
	}
  }

  public Object[] internalGetChildren(Object o)
    {
	return internalGetChildren(o, false);
    }

  public Object[] internalGetChildren(Object o, boolean force)
      {
        if (_children == null)
	    {
		_children = new Vector();
	    }

	if ((_children.size() == 0) || force)
        {	
          DataElement element = null;

          if (o instanceof DataElement)
          {
            element = (DataElement)o;
          }
          else 
          {
            element = _remoteRoot;
          }

	  if (element.isOfType("directory"))
	      {	      
		  element.expandChildren(true);
		  ArrayList objs = element.getAssociated("contents");	  
	      
	      // hard-coded for now
	      for (int i = 0; i < objs.size(); i++)
		{
		  DataElement obj = ((DataElement)objs.get(i)).dereference();
		  
		  //if (obj.getDataStore().filter(_resourceDescriptor, obj))
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
						  if (obj.isOfType("file"))
						      {
							  if (type.equals("directory"))
							      {
								  child = new FolderResourceElement(obj, this, this);		
							      }
							  else 
							      {
								  child = new FileResourceElement(obj, this, this);		
							      }
							  _children.add(child);		      
						      }
					      }
				      }
			      }
			  else
			  {
			      if (obj.isOfType("file"))
				  {
				      if (type.equals("directory"))
					  {
					      child = new FolderResourceElement(obj, this, this);		
					  }
				      else 
					  {
					      child = new FileResourceElement(obj, this, this);		
					  }
				      _children.add(child);		      
				  }			      
			  }
		      }	
		}
	    }	  
	}
    
        return _children.toArray();
      }
  
    
    public boolean testAttribute(Object target, String name, String value)
    {
	if (value.equals("org.eclipse.cdt.cpp.ui.cppnature"))
	    {
		return true;
	    }
	
	return false;
    }
    
    public Object getAdapter(Class adapter) 
    {
	if (PropertySource.matches(adapter))
	    {
		return new PropertySource(_root);	    
	  }
      else if (adapter == org.eclipse.ui.model.IWorkbenchAdapter.class)
	  {
	      return this;
	  }
      else if (adapter == org.eclipse.ui.IActionFilter.class)
	  {
	      return this;
	  }
      else
	  {
	      return Platform.getAdapterManager().getAdapter(this, adapter);
	  }
  }

  
  
  public int getType() 
  {
    return Resource.PROJECT;    
  }

  
  public boolean isOpen()
  {
    return _connection.isConnected();
  }
 
  public boolean isOpen(int flags)
  {
    return _connection.isConnected();
  }
  
  public void open(IProgressMonitor monitor) throws CoreException
  {
    if (!isOpen())
    {
	if (monitor != null)
	    {
		monitor.done();
	    }
	OpenConnectionAction openAction = new OpenConnectionAction(this);

	Display d= ModelInterface.getInstance().getDummyShell().getDisplay();
	d.asyncExec(openAction);
    }
  }

  public void close(IProgressMonitor monitor) throws CoreException
    {
	close(false, monitor);
    }

  public void close(boolean save, IProgressMonitor monitor) throws CoreException
  {
      if (isOpen())
    {	
	ModelInterface api = ModelInterface.getInstance();
	_children.clear();


	DomainNotifier dnotifier = _dataStore.getDomainNotifier();
	dnotifier.removeDomainListener(this);		    
	_connection.disconnect();  
	_root.setDataStore(_root.getParent().getDataStore());
	_dataStore = _root.getDataStore();
	_remoteRoot = null;


	CppProjectNotifier notifier = api.getProjectNotifier();
	notifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.CLOSE, this));
    }
  }

    public void removeChildren()
    {
	if (_children != null)
	    {
		for (int i = 0; i < _children.size(); i++)
		    {
			ResourceElement child = (ResourceElement)_children.get(i);
			child.removeChildren();
			child = null;
		    }
		
		_children.clear();
	    }
    }

    public void refresh()
    {
    }

    public void refreshLocal(int depth, IProgressMonitor monitor)
    {
    }  

  public String getPropertyPath()
  {
    String path = _plugin.getStateLocation().append(".repositories").toOSString();       
    return path + java.io.File.separator + getName();	  
  }
  

  public void deleteProperties()
  {
    String repDirStr = getPropertyPath();
    java.io.File repDir = new java.io.File(repDirStr);
    repDir.delete();
  }
  
  public void saveProperties()
  {
    String repDirStr = getPropertyPath();
    
    java.io.File repDir = new java.io.File(repDirStr);
    if (!repDir.exists())
      {
	repDir.mkdir();	      
      }
	  
    java.io.File repDirProp = new java.io.File(repDirStr + java.io.File.separator + ".properties");

    try
      {	
	FileOutputStream fileStream = new FileOutputStream(repDirProp);
	StringBuffer buffer = new StringBuffer();  
	for (int i = 0; i < _persistentProperties.size(); i++)
	  {
	    buffer.append(((PersistentProperty)_persistentProperties.get(i)).toString());	
	    buffer.append("\n");	    
	  }
	
	fileStream.write(buffer.toString().getBytes());            
	fileStream.close();
      }
    catch (IOException e)
      {
      }

    //_persistentProperties.clear();
    
  }
  
  public void readProperties()
  {
    String repDirStr = getPropertyPath();
    java.io.File repDirProp = new java.io.File(repDirStr + java.io.File.separator + ".properties");
    if (repDirProp.exists())
      {
        try
	  {
	    FileInputStream inFile = null;	  
	    try
	      {	      
		inFile = new FileInputStream(repDirProp);    
	      }
	    catch (FileNotFoundException e)
	      {
		return;	      
	      }
	    
	    BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
	    
	    String property = null;
	    while ((property = in.readLine()) != null)
	      {
		ArrayList args = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(property, "\t");
		
		String key   = (String)tokenizer.nextElement();
		String value = "";
		if (tokenizer.hasMoreElements())
		    {
			value = (String)tokenizer.nextElement();		
		    }
		
		setPersistentProperty(key, value);		
	      }        
	  }	
	catch (Exception e)
	  {
	  }		
      }
    
  }
  

  public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException
  {
    // for now, ignore the deleteContent
    close(monitor);
    PlatformVCMProvider provider = PlatformVCMProvider.getInstance();
    deleteProperties();
    provider.deleteRepository(this);

    Path rootPath = (Path)Platform.getLocation();
    String path = rootPath.toOSString() + java.io.File.separator + getName();
    java.io.File workingPath = new java.io.File(path);
    if (workingPath.exists())
	{
	    workingPath.delete();	     
	}
     
  }

  private String getInvocation()
  {
    ArrayList history = _plugin.readProperty(getProject(), "Build History");
    if ((history != null) && (history.size() > 0))
      return new String((String)history.get(0));
    return null;
  }

  public void build(int kind, IProgressMonitor monitor) throws CoreException
  {
      CppBuilder.getInstance().doBuild(this);
  }
  
  public void build(int kind, String builderName, Map args, IProgressMonitor monitor) throws CoreException
  {
    build(kind, monitor);    
  }

  public String getPersistentProperty(QualifiedName key) throws CoreException 
  {
    for (int i = 0; i < _persistentProperties.size(); i++)
      {
	PersistentProperty property = (PersistentProperty)_persistentProperties.get(i);
	if (property._key.equals(key.getQualifier()))
	  {
	    return property._value;	    
	  }	
      }
    
    return null;
  }

  public void setPersistentProperty(QualifiedName key, String propertyString) throws CoreException
  {
    for (int i = 0; i < _persistentProperties.size(); i++)
      {
	PersistentProperty property = (PersistentProperty)_persistentProperties.get(i);
	if (property._key.equals(key.getQualifier()))
	  {
	    property._value = propertyString;	    
	    return;
	  }	
      }
    
    PersistentProperty newProperty = new PersistentProperty(key, propertyString);
    _persistentProperties.add(newProperty);
    
  }

  public void setPersistentProperty(String key, String propertyString) throws CoreException
  {
    for (int i = 0; i < _persistentProperties.size(); i++)
      {
	PersistentProperty property = (PersistentProperty)_persistentProperties.get(i);
	if (property._key.equals(key))
	  {
	    property._value = propertyString;	    
	    return;
	  }	
      }
    
    PersistentProperty newProperty = new PersistentProperty(key, propertyString);
    _persistentProperties.add(newProperty);
    
  }
  
  
  public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException
  {
  }
  
  public void create(IProgressMonitor monitor) throws CoreException
  {
  }
  
  public String[] getNatureIds() throws CoreException
  {
    return null;  
  }
  
  public IPath getPluginWorkingLocation(IPluginDescriptor plugin)
  {
    return null;
  }
  
  public boolean hasNature(String natureId) throws CoreException
  {
    return false;
  }
  
    /***
  public void removeMapping(String name) throws CoreException
  {
  }
    ***/
  public void removeNature(String natureId) throws CoreException
  {
  }
  
  public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException
  {
  }
  
  

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


  public void createCompareManagerPlaceholder(IProject project) 
  {
  }


  public void deleteProjectVersion(String projectName, String versionName, IProgressMonitor monitor)
    throws CoreException 
  {
  }

  public void deleteTeamStream(String name, IProgressMonitor monitor) throws CoreException 
  {
  }

  public boolean equals(Object other) 
  {
    return false; 
  }

  public String[] fetchMemberList(String projectName, IPath path, IProgressMonitor monitor)
    throws CoreException 
  {
    return null;
    
  }
  

  public String getFileExtension()
  {
    return null;
  }
  
  public IProjectDescription newDescription(String projectName)
  {
    return null;
    
  }

  public IProject getProject() 
  {
    return this;
  }

  public IContainer getParent() 
  {
    return null;
  }
 

    public ResourceElement findResource(String name)
    {
	for (int i = 0; i < _children.size(); i++)
	    {
		ResourceElement resource = (ResourceElement)_children.get(i);
		if (resource.getName().equals(name))
		    {
			return resource;
		    }
	    }

	return null;
    }

    public ResourceElement findResource(DataElement element)
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
	
	return null;	
    }
  
  public IPath getFullPath()
  {
    return _phantomPath;    
  }

    public IPath getRealPath()
    {
	return _path;
    }


    public ResourceElement createResource(String type, String name)
    {
	String path = getRealPath().toOSString() + java.io.File.separator + name;
	DataElement newResource = _dataStore.createObject(getElement(), type, name, path);

	ResourceElement child = null;
	if (type.equals("directory"))
	    {
		child = new FolderResourceElement(newResource, this, this);		
	    }
	else 
	    {
		child = new FileResourceElement(newResource, this, this);		
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


  public boolean isVersionEnabled() 
  {
    return true; 
  }
  
  public boolean isWritable() 
  {
    return true;
  }
  
  public IStatus validateTeamStreamName(String name) 
  {
    return null;
  }
  
  void writeToStream(DataOutputStream os) throws IOException 
  {
  }

    public IProjectDescription getDescription() throws CoreException
    {
	if (_description == null)
	    _description = new RepositoryDescription();
	
	return _description;
    }


    public IResource[] members() throws CoreException 
    {
	return members(false);
    } 

    public IResource[] members(boolean phantom) throws CoreException 
    {
	return getChildren((Container)this, true); 
    } 


    public boolean isPhantom()
    {
	return false;
    }

    public void remove(ResourceElement child)
    {
	_children.remove(child);
    }

    public String toString()
    {
	return "Repository:" + _root.getType() + ":" + _root.getName();
    }

    public void update()
    {
	internalGetChildren(_remoteRoot, true);
    }


    public ProjectDescription internalGetDescription() 
    {
	ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
	if (info == null)
	    return null;
	return info.getDescription();
    }
 
    public IProject[] getReferencedProjects() throws CoreException 
    {
	return new IProject[0];
    }



    public boolean listeningTo(DomainEvent e)
    {
	if (isOpen() && _connection != null)
	    {
		DataElement dsStatus = _connection.getDataStore().getStatus();	
		DataElement parent = (DataElement)e.getParent();
		
		if (dsStatus == parent)
		    {
			return true;
		    }
	    }
	
	return false;
    }
    
    public void domainChanged(DomainEvent e)
    {
	DataElement status = (DataElement)e.getParent();
	if (!status.getName().equals("okay"))
	    {
		// close the project
		ModelInterface api = ModelInterface.getInstance();
		api.closeProject(this);
		
		try
		    {
			close(null);
		    }
		catch (Exception ex)
		    {
		    }
	    }	
    }

    public Shell getShell()
    {
	return null;
    } 
   public IMarker createMarker(String type) throws CoreException
    {
		MarkerInfo info = new MarkerInfo();
		info.setType(type);
		IMarker result = new ElementMarker(this, type);

		if (_markers == null)
		{
			_markers = new ArrayList();
		}		
		_markers.add(result);

		return result;
    }
    
    public IMarker findMarker(long id) throws CoreException 
    {
    	if (_markers != null)
    	{
    		for (int i = 0; i < _markers.size(); i++)
			{
				IMarker marker = (IMarker)_markers.get(i);
				if (marker.getId() == id)
				{
					return marker;	
				}
			}
			
			return null;
    	}    	
    	else
    	{
    		return null;
    	}
	}


	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException 
	{
		ArrayList results = new ArrayList();
		if (_markers != null)
		{
			int added = 0;

			for (int i = 0; i < _markers.size(); i++)
			{
				IMarker marker = (IMarker)_markers.get(i);
				//if (marker.getType().equals(type))
				{
					results.add(marker);
					added++;	
				}
			}
			
			IMarker[] markers = new IMarker[results.size()];
			for (int j = 0; j < results.size(); j++)
			{
				markers[j] = (IMarker)results.get(j);
			}	
			return markers;			
		}
	
		return new IMarker[0];
	}
	
	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException 
	{
		if (_markers != null)
		{
		  _markers.clear();
		}
	}
    
}

