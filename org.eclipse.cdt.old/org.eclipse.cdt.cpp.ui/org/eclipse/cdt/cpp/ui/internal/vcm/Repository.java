package com.ibm.cpp.ui.internal.vcm;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */ 
 
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.actions.*; 
import com.ibm.cpp.ui.internal.dialogs.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.builder.*;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.DataStoreCorePlugin;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.ui.connections.*;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.vcm.internal.core.base.*;
import org.eclipse.vcm.internal.core.*;
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
 
public class Repository extends Project 
    implements IRepository, IProject, IWorkbenchAdapter, IDataElementContainer, IResource
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

    private class RefreshResourcesAction implements Runnable
    {
	private Repository _repository;

	public RefreshResourcesAction()
	{
	}

	public void run()
	{
	    DataElement refreshDescriptor = _dataStore.localDescriptorQuery(_remoteRoot.getDescriptor(), "C_REFRESH");
	    if (refreshDescriptor != null)
		{	
		    _dataStore.synchronizedCommand(refreshDescriptor, _remoteRoot);
		    Object[] children = internalGetChildren(_remoteRoot, true);
		    for (int i = 0; i < children.length; i++)
			{
			    ResourceElement child = (ResourceElement)children[i];
			    child.refreshLocal(1, null);
			}
		} 
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
	    ConnectionStatus status = _connection.connect(_dataStore.getDomainNotifier(), "com.ibm.cpp.miners");	
	    if ((status != null) && status.isConnected())
		{ 
		    _dataStore = _connection.getDataStore();

		    DataElement hostRoot = _dataStore.getHostRoot();
		    
		    DataElement fsMinerData = _dataStore.findMinerInformation("com.ibm.dstore.miners.filesystem.FileSystemMiner");
		    _remoteRoot = hostRoot.get(0).dereference();

		    
		    _remoteRoot.expandChildren(true);

		    _root.setAttribute(DE.A_ID, _root.getName() + ".repository");
		    _root.addNestedData(_remoteRoot.getNestedData(), false);
		    _root.setDataStore(_remoteRoot.getDataStore());
		    
		    
		    // create directory for project
		    Path rootPath = (Path)Platform.getLocation();
		    String path = rootPath.toOSString() + java.io.File.separator + getName();
		    java.io.File workingPath = new java.io.File(path);
		    if (!workingPath.exists())
			{
			    workingPath.mkdir();	     
			}
		    
		    if (!_connection._isLocal)
			{	
			    _dataStore.setAttribute(DataStoreAttributes.A_LOCAL_PATH, path);
			}
		    
		    
		    ModelInterface api = ModelInterface.getInstance();
		    api.extendSchema(_dataStore.getDescriptorRoot());
		    api.openProject(_repository);

		    
		    _refreshAction.run(); 

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
    
    private RepositoryDescription _description;  
    
    private IPath _phantomPath;
    private IPath _path;

    private Connection _connection;
    protected Vector  _children;

    private RefreshNavigatorAction _refreshAction;
    private RefreshResourcesAction _refreshResourcesAction;

    private ArrayList _persistentProperties;
    private CppPlugin _plugin;
 
    private Workspace _workspace;

  public Repository(Connection connection) 
  {
      super(Platform.getLocation(), 
	    (Workspace)ResourcesPlugin.getWorkspace()); 

      _workspace = (Workspace)ResourcesPlugin.getWorkspace();
      _dataStore = CppPlugin.getDefault().getCurrentDataStore();
      DataElement fsMinerData = _dataStore.findMinerInformation("com.ibm.dstore.miners.filesystem.FileSystemMiner");
      _root = fsMinerData.get(0);  
    _path = new Path(_root.getAttribute(DE.A_SOURCE));
    _connection = connection;
    initialize();
  }

  public Repository(Connection connection, DataElement root)
  {
      super(Path.ROOT.append(root.getAttribute(DE.A_NAME)),
	    (Workspace)ResourcesPlugin.getWorkspace()); 

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
    _refreshAction = new RefreshNavigatorAction("refresh");
    _refreshResourcesAction = new RefreshResourcesAction();

    _plugin = CppPlugin.getDefault();
    _persistentProperties = new ArrayList();
    readProperties();


    _children = new Vector();
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
	if (_children == null)
	    {
		_children = new Vector();
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

	  if (!element.getAttribute(DE.A_TYPE).equals("file"))
	    {	      
	      ArrayList objs = element.getNestedData();	  
	      
	      // hard-coded for now
	      for (int i = 0; i < objs.size(); i++)
		{
		  DataElement obj = ((DataElement)objs.get(i)).dereference();
		  
		  if (obj.getDataStore().filter(_resourceDescriptor, obj))
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
			  else
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
    
        return _children.toArray();
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
    else
	{
	    return null;
	}
  }

  
  public IRepositoryLocation getRepositoryLocation() 
  {
      return null;
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
	_connection.disconnect();  
	_dataStore = _root.getDataStore();	
	saveProperties();
	
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
	if (_refreshAction != null)
	    {
		_refreshAction.run();
	    }
    }

    public void refreshLocal(int depth, IProgressMonitor monitor)
    {
	Display d= ModelInterface.getInstance().getDummyShell().getDisplay();
	d.asyncExec(_refreshResourcesAction);
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
		String value = (String)tokenizer.nextElement();		
		
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
     
    _refreshAction.run();
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
      CppBuilder.doBuild(this);
      refreshLocal(1, null);
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

  public IMergeManager createMergeManager(IResource destination, IResourceEdition beforeEdition, IResourceEdition afterEdition, IProgressMonitor m) 
  {
    return null;
  }
  
public ITeamStream createTeamStream(String name, IProgressMonitor progressMonitor) throws CoreException 
  {
    return null;
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
  
  public IVersionHistory fetchProjectHistory(String name, IProgressMonitor progressMonitor) throws CoreException 
  {
    return null;  
  }
  
  public String[] fetchProjectList(IProgressMonitor progressMonitor) throws CoreException 
  {
    return null;
  }
  
  public IVersionHistory fetchResourceHistory(String projectName, IPath path, IProgressMonitor monitor) throws CoreException 
  {
    return null;	
  }

  public ITeamStream[] fetchTeamStreams(IProgressMonitor progressMonitor) throws CoreException 
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

  TeamStream getTeamStream(String name) throws CoreException 
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


    
}

