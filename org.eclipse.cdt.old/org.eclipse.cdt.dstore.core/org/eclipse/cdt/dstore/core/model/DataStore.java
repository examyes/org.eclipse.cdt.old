package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*; 
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.server.ILoader;

import java.util.*;
import java.lang.*;
import java.io.*;

public class DataStore
{
    private DataStoreAttributes _dataStoreAttributes;
    
    private DataElement         _root;
    private DataElement         _descriptorRoot;
    private DataElement         _logRoot;
    private DataElement         _hostRoot;
    private DataElement         _minerRoot;
    private DataElement         _tempRoot;

    private DataElement         _ticket;  
    
    private CommandHandler      _commandHandler;
    private UpdateHandler       _updateHandler;
    
    private DomainNotifier      _domainNotifier;

    private ILoader             _loader;
    private String              _minersLocation;
    
    private boolean             _isConnected;
    private boolean             _logTimes;
    private int                 _timeout;
    
    private HashMap             _hashMap;
    private ArrayList           _recycled;
    
    private ResourceBundle      _resourceBundle;
    private Random              _random;  
    
    /////////////////////////////////////////
    //
    // Constructors
    //
    /////////////////////////////////////////
    
    public DataStore(DataStoreAttributes attributes)
    {
        _dataStoreAttributes = attributes;
        _commandHandler      = null;
        _updateHandler       = null;
        _domainNotifier      = null;
        _isConnected         = false;
        _logTimes            = false;

	initialize();
      }

  public DataStore(DataStoreAttributes attributes, CommandHandler commandHandler,
                   UpdateHandler updateHandler, DomainNotifier domainNotifier)
      {
        _dataStoreAttributes = attributes;
        _commandHandler      = commandHandler;
        _updateHandler       = updateHandler;
        _domainNotifier      = domainNotifier;
        _isConnected         = true;
        _logTimes            = false;

	initialize();
	createRoot();
      }

    public void initialize()
    {
	_minersLocation = "com.ibm.dstore.core";
	_random = new Random(System.currentTimeMillis());

        _hashMap = new HashMap(100000);
	_recycled = new ArrayList();
	initElements(4000);

	_timeout = 10000;
	try
	    {
		_resourceBundle = ResourceBundle.getBundle("com.ibm.dstore.core.model.DataStoreResources");
	    }
	catch (MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }

    }


  /////////////////////////////////////////
  //
  // Sets
  //
  /////////////////////////////////////////

    public void setTicket(DataElement ticket)
    {
	_ticket = ticket;
    }

    public void setLoader(ILoader loader)
    {
	_loader         = loader;
    }

    public void setMinersLocation(String minersLocation)
    {
	_minersLocation = minersLocation;
	DataElement location = createObject(_tempRoot, "location", _minersLocation);
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SET_MINERS", 1);  
	ArrayList args = new ArrayList();
	args.add(location);
	synchronizedCommand(cmd, args, _root);
    }

    public void setMinersLocation(DataElement location)
    {
	if (_minersLocation != location.getName())
	    {
		_minersLocation = location.getName();
	    }
    }
  
  public void setConnected(boolean isConnected)
      {
        _isConnected = isConnected;
      }
 
  public void setDomainNotifier(DomainNotifier domainNotifier)
      {
        _domainNotifier = domainNotifier;
      }

  public void setUpdateHandler(UpdateHandler updateHandler)
      {
        _updateHandler = updateHandler;
      }

  public void setCommandHandler(CommandHandler commandHandler)
      {
        _commandHandler = commandHandler;
      }

  public void setUpdateWaitTime(int time)
      {
        _updateHandler.setWaitTime(time);
      }

  public void setCommandWaitTime(int time)
      {
        _commandHandler.setWaitTime(time);
      }

    public void setTimeoutValue(int time)
    {
	_timeout = time;
    }

  public void setAttribute(int attribute, String value)
  {
    _dataStoreAttributes.setAttribute(attribute, value);
  }

  public void setLogTimes(boolean flag)
      {
        _logTimes = flag;
      }

  /////////////////////////////////////////
  //
  // Gets
  //
  /////////////////////////////////////////

  public boolean isConnected()
      { 
        return _isConnected;
      }

  public boolean logTimes()
      {
        return _logTimes;
      }

    public DataElement getTicket()
    {
	return _ticket;
    }

  public int getUpdateWaitTime()
      {
        return _updateHandler.getWaitTime();
      }

  public int getCommandWaitTime()
      {
        return _commandHandler.getWaitTime();
      }

    public ResourceBundle getResourceBundle()
    {
	return _resourceBundle;
    }

    public String getLocalizedString(String key)
    {
	try
	    {
		if (_resourceBundle != null && key != null)
		    return _resourceBundle.getString(key);
	    }
	catch (MissingResourceException mre)
	    {
	    }
	return "";
    }

  public String getName()
  {
    return getAttribute(DataStoreAttributes.A_HOST_NAME);    
  }
  
  public DataElement getRoot()
      {
        return _root;
      }

  public DataElement getHostRoot()
  {
    return _hostRoot;
  }

  public DataElement getMinerRoot()
  {
    return _minerRoot;
  }

  public DataElement getLogRoot()
      {
        return _logRoot;
      }

  public CommandHandler getCommandHandler()
  {
    return _commandHandler;    
  }
  
  public UpdateHandler getUpdateHandler()
  {
    return _updateHandler;    
  }  

  public DataElement getDescriptorRoot()
      {
        return _descriptorRoot;
      }

    public ILoader getLoader()
    {
	return _loader;
    }
    
    public String getMinersLocation()
    {
	return _minersLocation;
    }

  public DomainNotifier getDomainNotifier ()
  {
    return _domainNotifier;
  } 

  public String getAttribute(int attribute)
  {
    return _dataStoreAttributes.getAttribute(attribute);
  }

  public int getNumElements()
  {
    return _hashMap.size();    
  }

  public HashMap getHashMap()
      {
        return _hashMap;
      }

    public void initElements(int size)
    {
	for (int i = 0; i < size; i++)
	    {
		_recycled.add(new DataElement());
	    }
    }
  
  public DataElement createElement()  
    {
	DataElement newObject = null;

	if (_recycled.size() > 0)
	    {
		newObject = (DataElement)_recycled.remove(0);
	    }
	else
	    {
		newObject = new DataElement();
	    }

	newObject.setUpdated(false);
	return newObject;
    }
  
  /////////////////////////////////////////
  //
  // Initialization
  //
  /////////////////////////////////////////

  public void createRoot()
  {
    _root        = createObject(null, getLocalizedString("model.root"),
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_ROOT_NAME),
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_ROOT_PATH));

    _descriptorRoot = createObject(_root, DE.T_OBJECT_DESCRIPTOR, getLocalizedString("model.descriptors"));
    _ticket = createObject(_root, getLocalizedString("model.ticket"), "null");

    createRoots();
    initializeDescriptors();
  }

  public void createRoots()
  {
      _tempRoot = createObject(_root, "temp", "Temp Root");
      _logRoot     = createObject(_root, getLocalizedString("model.log"), getLocalizedString("model.Log_Root"));
      _minerRoot   = createObject(_root, getLocalizedString("model.miners"), getLocalizedString("model.Tool_Root"));
      
      _hostRoot = createObject(_root,  getLocalizedString("model.host"),
			       _dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_NAME),
			       _dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_PATH));
  }


  /////////////////////////////////////////
  //
  // DataElement Creation
  //
  /////////////////////////////////////////

  public DataElement createReference(DataElement from, DataElement to)
      {
	// default reference is a containment relationship
        return createReference(from, to, getLocalizedString("model.contents"));
      }

  public DataElement createReference(DataElement parent, DataElement realObject, String relationType)
      {
	// reference with a specified type of relationship
	  DataElement reference = createElement();
	  reference.reInit(parent, realObject, relationType);
	  parent.addNestedData(reference, false);
	  
	  String sugId = reference.getId();
	  _hashMap.put(sugId, reference);

	  return reference;
      }


  public void createReferences(DataElement from, ArrayList to, String type)
  {
    for (int i = 0; i < to.size(); i++)
      {	
	DataElement toObject = (DataElement)to.get(i);
	createReference(from, toObject, type);	 
      }
  }

  public DataElement createReference(DataElement parent, DataElement realObject, 
				     String toRelation, String fromRelation)
    {
	  // reference with "to" relationship
	DataElement toReference = createElement();
	toReference.reInit(parent, realObject, toRelation);
        parent.addNestedData(toReference, false);

        String toId = toReference.getId();
        _hashMap.put(toId, toReference);

	// reference with "from" relationship
	DataElement fromReference = createElement();
        fromReference.reInit(realObject, parent, fromRelation);
        realObject.addNestedData(fromReference, false);
	
        String fromId = fromReference.getId();
        _hashMap.put(fromId, fromReference);
	
        return toReference;
      }


  public void createReferences(DataElement from, ArrayList to, String toRel, String fromRel)
  {
    for (int i = 0; i < to.size(); i++)
      {	
	DataElement toObject = (DataElement)to.get(i);
	createReference(from, toObject, toRel, fromRel);	 
      }
  }

  public DataElement createObject(DataElement parent, String type, String name)
      {
        return createObject(parent, type, name, "");
      }

  public DataElement createObject(DataElement parent, String type, String name, String source)
      {
        String id = generateId(parent, type, name);
	if (id == null)
	  {
	    return null;
	  }
	
        return createObject(parent, type, name, source, id);
      }

  public DataElement createObject(DataElement parent, String type, String name, String source, String sugId)
      {
        String id = makeIdUnique(sugId);

	DataElement newObject = createElement();
	newObject.reInit(this, parent, type, id, name, source); 
        if (parent != null)
        {
          parent.addNestedData(newObject, false);
        }

        _hashMap.put(id, newObject);
        return newObject;
      }

  public DataElement createObject(DataElement parent, String type, String name, String source, String sugId, String isReference)
  {
        String id = makeIdUnique(sugId);

	DataElement newObject = createElement();
	if (parent == null)
	    {
		parent = _tempRoot;
	    }

	newObject.reInit(this, parent, type, id, name, source, isReference); 
        if (parent != null)
        {
          parent.addNestedData(newObject, false);
        }
        _hashMap.put(id, newObject);
        return newObject;
      }

  public DataElement createObject(DataElement parent, String attributes[])
      {
	  DataElement newObject = createElement();

	  if (parent == null)
	      {
		  parent = _tempRoot;
	      }
	  newObject.reInit(this, parent, attributes);

	  if (parent != null)
	      {
		  parent.addNestedData(newObject, false);
	      }
	  
	  _hashMap.put(attributes[DE.A_ID], newObject);
	  return newObject;
      }


    public DataElement createAbstractObjectDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, "com.ibm.dstore.core");
    }   

    public DataElement createAbstractObjectDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, source);
    }   

    public DataElement createObjectDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, "com.ibm.dstore.core");
    }   

    public DataElement createObjectDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, source);
    }   

    public DataElement createAbstractRelationDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, "com.ibm.dstore.core");
    }   

    public DataElement createAbstractRelationDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, source);
    }   

    public DataElement createRelationDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_RELATION_DESCRIPTOR, name, "com.ibm.dstore.core");
    }   

    public DataElement createRelationDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_RELATION_DESCRIPTOR, name, source);
    }   

    public DataElement createAbstractCommandDescriptor(DataElement parent, String name)
    {
	return createAbstractCommandDescriptor(parent, name, name);
    }

    public DataElement createAbstractCommandDescriptor(DataElement parent, String name, String value)
    {
	DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, "com.ibm.dstore.core");
        cmd.setAttribute(DE.A_VALUE, value);        
	return cmd;
    }   

    public DataElement createAbstractCommandDescriptor(DataElement parent, String name, String source, String value)
    {
	DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, source);
        cmd.setAttribute(DE.A_VALUE, value);        
	return cmd;
    }   

    public DataElement createCommandDescriptor(DataElement parent, String name)
    {
	return createCommandDescriptor(parent, name, name);
    }

    public DataElement createCommandDescriptor(DataElement parent, String name, String value)
    {
	DataElement cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, "com.ibm.dstore.core");
        cmd.setAttribute(DE.A_VALUE, value);        
	return cmd;
    }   

    public DataElement createCommandDescriptor(DataElement parent, String name, String source, String value)
    {
	DataElement cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, source);
        cmd.setAttribute(DE.A_VALUE, value);        
	return cmd;
    }   


  public void moveObject(DataElement source, DataElement target)
      {
        DataElement oldParent = source.getParent();
        oldParent.getNestedData().remove(source);
        refresh(oldParent, true);
         
        target.addNestedData(source, false);
        source.setParent(target);  
        refresh(target, true);
      }
  
  public void deleteObjects(DataElement from)
      {
	  for (int i = 0; i < from.getNestedSize(); i++)
	      {
		  DataElement deletee = from.get(i);
		  deleteObjectHelper(from, deletee, 2);		  
	      }
	  
	  refresh(from);
      }

  public void deleteObject(DataElement from, DataElement toDelete)
      {
        deleteObjectHelper(from, toDelete, 5);
	refresh(toDelete);	
      }

  private void deleteObjectHelper(DataElement from, DataElement toDelete, int depth)
      {
	  if (depth > 0)
	      {
		  depth--;
		  toDelete.delete();
		  for (int i = 0; i < toDelete.getNestedSize(); i++)
		      {
			  DataElement subDelete = toDelete.get(i);
			  if (subDelete != null && !subDelete.isDeleted())
			      {
				  deleteObjectHelper(toDelete, subDelete, depth);
			      }
		      }
		  
		  String id = toDelete.getAttribute(DE.A_ID);
		  _hashMap.remove(id);
		  _recycled.add(toDelete);
	      }
      }

    public DataElement replaceDeleted(DataElement deletedObject)
    {
	if (deletedObject != null)
	    {
		synchronized (deletedObject)
		    {
			String name = deletedObject.getName();
			String type = deletedObject.getType();
			
			// find undeleted ancestor
			DataElement parent = deletedObject.getParent();
			if ((parent != null) && parent.isDeleted())
			    {
				parent = replaceDeleted(parent);
			    }
			if ((parent != null) && !parent.isDeleted())
			    {
				for (int i = 0; i < parent.getNestedSize(); i++)
				    {
					DataElement child = parent.get(i);
					if (!child.isDeleted())
					    {
						if (child.getName().equals(name) && child.getType().equals(type))
						    {
							return child;
						    }
					    }
				    }
			    }
		    }
	    }

	return null;
    }   
    
  private String makeIdUnique(String id)
      {
	  if (!_hashMap.containsKey(id))
	      {
		  return id;
	      }
	  else
	      {
		  String newId = String.valueOf(_random.nextInt());
		  while (_hashMap.containsKey(newId))
		      {	    
			  newId = String.valueOf(_random.nextInt());
		      }
		  
		  return newId;
	      }
      }


    private String generateId(DataElement parent, String type, String name)
      {
	  // by default, name will be the id
	  return name;
      }

    public String generateId()
    {
	String newId = String.valueOf(_random.nextInt());
	while (_hashMap.containsKey(newId))
	    {	    
		newId = String.valueOf(_random.nextInt());
	    }
	
	return newId;
    }	

  public boolean contains(String id)
      {
	  return _hashMap.containsKey(id);
      }

  /////////////////////////////////////////
  //
  // Updates
  //
  /////////////////////////////////////////

  public void fireDomainChanged(DomainEvent e)
      {
        _domainNotifier.fireDomainChanged(e);
      }

  public void refresh(ArrayList elements)
      {
        // this gets called in response to a query
        for (int i = 0; i < elements.size(); i++)
        {
          refresh((DataElement)elements.get(i));
        }
      }

  public void refresh(DataElement element)
    {
	refresh(element, false);	
    }

  public void refresh(DataElement element, boolean immediate)
      {
        if ((_updateHandler != null) && (element != null))
        {
	    // update either client or ui
	    element.setUpdated(false);	
	    _updateHandler.update(element, immediate);
        }
      }


  public void update(ArrayList objects)
      {
        // this gets called in response to a query
        for (int i = 0; i < objects.size(); i++)
        {
          update((DataElement)objects.get(i));
        }
      }

  public void update(DataElement dataElement)
      {
       refresh(dataElement);       
      }

  public void updateFile(String remotePath, DataElement associatedObject)
      {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        if (fileName != null)
        {
          File file = new File(fileName);
	  if (file.canRead())
	      {
		  _updateHandler.updateFile(file, associatedObject);
	      }
        }
      }

  public void updateFile(String remotePath, byte[] bytes, int size)
      {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        if (fileName != null)
        {
	    _updateHandler.updateFile(remotePath, bytes, size);
        }
      }

    /***/
    public void updateAppendFile(String remotePath, byte[] bytes, int size)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        if (fileName != null)
        {
	    _updateHandler.updateAppendFile(remotePath, bytes, size);
        }	
    }
    /***/

  public void replaceFile(String remotePath, File file)
      {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
	if (!remotePath.equals(fileName))
	  {
	    _commandHandler.sendFile(remotePath, file);
	  }
      }

  public void setObject(DataElement localObject)
  {
    DataElement cmd = find(_descriptorRoot, DE.A_NAME, getLocalizedString("model.Set"), 2);  
    DataElement status = synchronizedCommand(cmd, localObject, true);
  }

  public void modifyObject(DataElement localObject)
  {
    DataElement cmd = find(_descriptorRoot, DE.A_NAME, getLocalizedString("model.Modify"), 2);  
    DataElement status = _commandHandler.command(cmd, localObject, true);
    waitUntil(status, getLocalizedString("model.done"));
  }

  public void setHost(DataElement localHostObject)
  {
      DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SET_HOST", 1);  
      DataElement status = _commandHandler.command(cmd, localHostObject, false);
      waitUntil(status, getLocalizedString("model.done"));
  }
    
    public void getSchema()
    {
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SCHEMA", 1);
	synchronizedCommand(cmd, _descriptorRoot);
    }
    
    public DataElement initMiners()
    {
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_INIT_MINERS", 1);
	return synchronizedCommand(cmd, _descriptorRoot);
    } 
    
    public boolean showTicket(String ticketStr)
    {
	if (ticketStr == null)
	    {
		ticketStr = "null";
	    }

	DataElement ticket = createObject(_tempRoot, getLocalizedString("model.ticket"), ticketStr);
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_VALIDATE_TICKET", 1);  
	DataElement status = _commandHandler.command(cmd, ticket, false);
	waitUntil(status, getLocalizedString("model.done"));	

	return ticket.getAttribute(DE.A_VALUE).equals(getLocalizedString("model.valid"));
    }
    
    public boolean validTicket()
    {
	if (_ticket.getAttribute(DE.A_VALUE).equals(getLocalizedString("model.valid")))
	    {
		return true;
	    }
	else
	    {
		return false;
	    }
    }

  public DataElement commandByName(String commandName, DataElement object, boolean synch)
  {
    DataElement status = null;
    DataElement cmd = find(_descriptorRoot, DE.A_NAME, commandName, 3);  

    if (synch)
    {
      status = synchronizedCommand(cmd, object);
    }
    else
    {
      status = command(cmd, object);
    }

    return status;
  }

  public DataElement commandByName(String commandName, ArrayList arguments, DataElement object, boolean synch)
  {
    DataElement status = null;
    DataElement cmd = find(_descriptorRoot, DE.A_NAME, commandName, 3);  

    status = command(cmd, arguments, object);
    if (synch)
    {
      waitUntil(status, getLocalizedString("model.done"));
    }
    return status;
  }

  /////////////////////////////////////////
  //
  // Commands
  //
  /////////////////////////////////////////
 
  public void waitUntil(DataElement status, String state)
  {
    int timeWaited = 20;
    boolean timedOut = false;
    boolean notificationEnabled = _domainNotifier.isEnabled();
    if (notificationEnabled)
    {
      _domainNotifier.enable(false);
    }

    while ((status != null) 
	   && !status.getName().equals(state) 
	   && !status.getName().equals(getLocalizedString("model.incomplete")) 
	   && !timedOut)
      {	
        if ((_timeout != -1) && (timeWaited > _timeout))
        {
          // waited too long!
          timedOut = true;
        }
        
        try
        {		
          Thread.currentThread().sleep(timeWaited);
        } 
        catch (InterruptedException e)
        {
          System.out.println(e);
        }
        
        timeWaited += timeWaited;
      }

    if (timedOut)
    {
      status.setAttribute(DE.A_NAME, getLocalizedString("model.timeout"));
    }

   if (notificationEnabled)
    {
      _domainNotifier.enable(true);
    }
  }

    public void cleanBadReferences(DataElement root)
    {
	/****
	for (int i = 0; i < root.getNestedSize(); i++)
	    {
		DataElement child = (DataElement)root.get(i);
		if (child.isReference())
		    {
			DataElement referenced = child.dereference();
			if ((referenced == null) || (referenced == child))
			    {
				deleteObject(child.getParent(), child);
			    }
		    }
		
		cleanBadReferences(child);
	    }
	***/
    }

    public void cancelAllCommands()
    {
	_commandHandler.cancelAllCommands();
    }   

  public DataElement synchronizedCommand(DataElement commandDescriptor, DataElement dataObject)
  {
    return synchronizedCommand(commandDescriptor, dataObject, false);
  }

  public DataElement synchronizedCommand(DataElement commandDescriptor, DataElement dataObject, boolean noRef)
  {
    DataElement status = command(commandDescriptor, dataObject, noRef, true);
    waitUntil(status, getLocalizedString("model.done"));

    return status;
  }

  public DataElement synchronizedCommand(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject)
  {
    DataElement status = command(commandDescriptor, arguments, dataObject, true);
    waitUntil(status, getLocalizedString("model.done"));

    return status;
  }
  
public DataElement command(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject)
    {
	return command(commandDescriptor, arguments, dataObject, false);
    }


public DataElement command(DataElement commandDescriptor, 
			   ArrayList arguments, DataElement dataObject, 
			   boolean immediate)
      {
        if (_commandHandler != null)
        {
	  return _commandHandler.command(commandDescriptor, arguments, dataObject, true, immediate);
        }
	return null;
      }

public DataElement command(DataElement commandDescriptor, 
			   DataElement objectDescriptor, DataElement dataObject
			   )
    {
	return command(commandDescriptor, objectDescriptor, dataObject, false);
    } 

public DataElement command(DataElement commandDescriptor, 
			   DataElement objectDescriptor, DataElement dataObject, 
			   boolean immediate)
      {
        if (_commandHandler != null)
        {
	  return _commandHandler.command(commandDescriptor, objectDescriptor, dataObject, true, immediate);
        }
	return null;
      }

  public DataElement command(DataElement commandDescriptor, DataElement dataObject)
  {
      return command(commandDescriptor, dataObject, false);    
  }
  
  public DataElement command(DataElement commandDescriptor, DataElement dataObject, boolean noRef)
    {
	return command(commandDescriptor, dataObject, noRef, false);
    }

  public DataElement command(DataElement commandDescriptor, DataElement dataObject, boolean noRef, boolean immediate)
      {
        if (_commandHandler != null)
        {
	  return _commandHandler.command(commandDescriptor, dataObject, !noRef);
        }

	return null;
      }

  public DataElement command(DataElement commandObject)
      {
	return _commandHandler.command(commandObject);	
      }
 
  public void flush()
  {
    // flush the whole thing
    flush(_logRoot);
   flush(_hostRoot);
    flush(_minerRoot);
  }
  

  public void flush(DataElement element)
      {
	if (element != null)
	  {	
	      deleteObjects(element);
	  }	
      }



  public DataElement localDescriptorQuery(DataElement object, String keyName)
    {
	return localDescriptorQuery(object, keyName, 5);
    }

  public DataElement localDescriptorQuery(DataElement descriptor, String keyName, int depth)
  {
   if ((descriptor != null) && (depth > 0))
   {	
    for (int i = 0; i < descriptor.getNestedSize(); i++)
    {
     DataElement subDescriptor = (DataElement)descriptor.get(i);
     String type = subDescriptor.getType();
     if (type.equals(DE.T_COMMAND_DESCRIPTOR))
     {
      if (keyName.equals(subDescriptor.getValue()))
       return subDescriptor;		
     }
     else if (type.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR))
     {
      DataElement result = localDescriptorQuery(subDescriptor, keyName, depth - 1);
      if (result != null)
       return result;
     }
    }
    ArrayList abstractDescriptors = descriptor.getAssociated(getLocalizedString("model.abstracted_by"));
    for (int j = 0; j < abstractDescriptors.size(); j++)
    {
     DataElement abstractDescriptor = (DataElement)abstractDescriptors.get(j);
     DataElement result = localDescriptorQuery(abstractDescriptor, keyName, depth - 1);;
     if (result != null)
      return result;
    }
   }
  
   
   return null;
  }

  public DataElement getMinerFor(DataElement commandDescriptor)
      {
        String minerName = commandDescriptor.getSource();
        DataElement theMinerElement = find(_minerRoot, DE.A_NAME, minerName, 1);
        return theMinerElement;
      }

 public ArrayList findObjectsOfType(DataElement root, DataElement type)
    {
	ArrayList results = new ArrayList();
	for (int i = 0; i < root.getNestedSize(); i++)
	    {
		DataElement child = root.get(i);
		if (child.isOfType(type))
		    {
			results.add(child);
		    }

		ArrayList subResults = findObjectsOfType(child, type);
		for (int j = 0; j < subResults.size(); j++)
		    {
			results.add(subResults.get(j));
		    }
	    }

	return results;
    }

    public ArrayList getRelationItems(DataElement descriptor, String fixateOn)
    {
	ArrayList result = new ArrayList();
        if (descriptor != null)
        {
	    // contained relationships
	    for (int i = 0; i < descriptor.getNestedSize(); i++)
		{
		    DataElement object = ((DataElement)descriptor.get(i)).dereference();		
		    
		    String objType = (String)object.getElementProperty(DE.P_TYPE);
		    if (objType.equals(DE.T_RELATION_DESCRIPTOR) || objType.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
			{
			    if (fixateOn != null)
				{
				    String objName = (String)object.getElementProperty(DE.P_NAME);
				    if (objName.equals(fixateOn))
					{
					    if (!result.contains(object))
						result.add(object);
					}
				}
			    else
				{
				    if (!result.contains(object))
					result.add(object);
				}
			}
		}

	    // abstracted relationships
	    ArrayList baseDescriptors = descriptor.getAssociated(getLocalizedString("model.abstracted_by"));
	    for (int j = 0; j < baseDescriptors.size(); j++)
		{
		    DataElement baseDescriptor = (DataElement)baseDescriptors.get(j);
		    ArrayList baseRelations = getRelationItems(baseDescriptor, fixateOn);
		    for (int k = 0; k < baseRelations.size(); k++)
			{
			    DataElement relation = (DataElement)baseRelations.get(k);
			    if (!result.contains(relation))
				{
				    result.add(relation);
				}
			}
		}
        }        

	return result;
    }

    public ArrayList getFilterItems(DataElement descriptor, String fixateOn, DataElement relation)
    {
	ArrayList result = new ArrayList();
        if (descriptor != null)
        {
	    ArrayList filters = null;
	    if ((relation == null) || relation.getName().equals(getLocalizedString("model.contents")))
		{
		    filters = descriptor.getNestedData();
		    if (filters != null)
			{
			    for (int i = 0; i < filters.size(); i++)
				{
				    DataElement object = ((DataElement)(filters.get(i))).dereference();		
				    String objType = (String)object.getElementProperty(DE.P_TYPE);
				    if (objType.equals(DE.T_OBJECT_DESCRIPTOR)  || objType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
					{
					    if (fixateOn != null)
						{
						    String objName = (String)object.getElementProperty(DE.P_NAME);
						    if (objName.equals(fixateOn))
							{
							    if (!result.contains(object))
								result.add(object);
							}
						}
					    else
						{
						    if (!result.contains(object))
							result.add(object);
						}
					}
				}
			}
		}
	    /*
	    if (result.size() < 1 && (_relationSelected != null))
		{
		    filters = _input.getAssociated(_relationSelected.getName());
		    for (int i = 0; i < filters.size(); i++)
			{
			    DataElement object = ((DataElement)filters.get(i)).getDescriptor();		
			    if (object != null)
				{
				    String objType = object.getType();
				    if (objType.equals(DE.T_OBJECT_DESCRIPTOR) || objType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
					{
					    String objName = (String)object.getElementProperty(DE.P_NAME);
					    if (!_filterItems.contains(object))
						{
						    if (_fixatedObjectType != null)
							{
							    if (objName.equals(_fixatedObjectType))
								{
								    _filterItems.add(object);
								}
							}
						    else
							{
							    _filterItems.add(object);
							}
						}
					}
				}
			}
		}
	    */

        }
	return result;
      }



  public ArrayList searchForPattern(DataElement root, int attribute, String pattern, boolean ignoreCase)
  {
    int    attributes[] = { attribute };
    String   patterns[] = { pattern };
    return searchForPattern(root, attributes, patterns, 1, ignoreCase);
  }

  public ArrayList searchForPattern(DataElement root, ArrayList attributes, ArrayList patterns,
				    boolean ignoreCase)
  {
    int att[]    = new int[attributes.size()];
    String ptn[] = new String[attributes.size()];
    for (int i = 0; i < attributes.size(); i++)
      {
	att[i] = ((Integer)attributes.get(i)).intValue();	
	ptn[i] = (String)(patterns.get(i));	
      }
    
    return searchForPattern(root, att, ptn, attributes.size(), ignoreCase);    
  }
  
  public ArrayList searchForPattern(DataElement root, 
				    int attributes[], String patterns[], int numAttributes,
				    boolean ignoreCase)
  {
    return searchForPattern(root, attributes, patterns, numAttributes, ignoreCase, 10);
  }

  public ArrayList searchForPattern(DataElement root, int attributes[], 
                                    String patterns[], int numAttributes,
				    boolean ignoreCase, int depth)
    {
	ArrayList searched = new ArrayList();
	return searchForPattern(root, attributes, patterns, numAttributes, ignoreCase, depth, searched);
    }

  public ArrayList searchForPattern(DataElement root, int attributes[], 
                                    String patterns[], int numAttributes,
				    boolean ignoreCase, int depth, 
				    ArrayList searched)
  {      
    ArrayList result = new ArrayList();
    if (depth > 0)
    {
      for (int i = 0; i < root.getNestedSize(); i++)
      {
	DataElement child = (DataElement)root.get(i);
	child = child.dereference();
        if ((child != null) && !searched.contains(child))
        {
	    searched.add(child);
	    if (child.patternMatch(attributes, patterns, numAttributes, ignoreCase))
		{
		    result.add(child);
		}
	    
	    ArrayList subResults = searchForPattern(child, attributes, patterns, numAttributes, ignoreCase, depth - 1, searched);
	    for (int j = 0; j < subResults.size(); j++)
		{
		    result.add(subResults.get(j));
		}	
	}
      }
    }

    return result;
  }


  public ArrayList fuzzyResolveName(DataElement object, String pattern)
  {
    ArrayList results = new ArrayList();
    if (object != null)
      {	
	for (int i = 0; i < object.getNestedSize(); i++)
	  {
	    DataElement subObject = (DataElement)object.get(i);
	    if (subObject.getName().startsWith(pattern))
	      {
		results.add(subObject);
	      }
	  }

	ArrayList subResults = fuzzyResolveName(object.getParent(), pattern);
	for (int j = 0; j < subResults.size(); j++)
        {
          results.add(subResults.get(j));
        }	
      }

    return results;
  }


  public DataElement findMinerInformation(String minerName)
  {
    DataElement information = null;    
    DataElement minerElement =  find(_minerRoot, DE.A_NAME, minerName, 1);
    if (minerElement != null)
      {	
	information = find(minerElement, DE.A_TYPE, getLocalizedString("model.data"), 1);	
      }
    
    return information;    
  }
  
  public DataElement findObjectDescriptor(String name)
      {
        return find(_descriptorRoot, DE.A_NAME, name, 5);
      }

  public DataElement find(String id)
      {
        DataElement result = (DataElement)_hashMap.get(id);
        
        return result;
      }

  public DataElement find(DataElement root, int attribute, String name)
      {
        return find(root, attribute, name, 10);
      }

  public DataElement find(DataElement root, int attribute, String name, int depth)
      {
        if ((root != null) && (name != null) && !root.isReference() && depth > 0)
        {
          depth--;

          if (StringCompare.compare(name, root.getAttribute(attribute), true))
          {
            return root;
          }
          else
          {
            for (int h = 0; h < root.getNestedSize(); h++)
            {
              DataElement nestedObject = root.get(h);
	      String compareName = nestedObject.getAttribute(attribute);
	      
              if (!nestedObject.isReference() && (compareName != null))
              {
                if (name.compareToIgnoreCase(compareName) == 0)
                {
                  return nestedObject;		
                }
                else
                {
                  DataElement foundObject = find(nestedObject, attribute, name, depth);
                  if (foundObject != null)
                  {
                    return foundObject;
                  }
                }
	      }	
            }
          }
        }
        return null;
      }

  public DataElement resolveName(DataElement object, String keyName)
  {
    if (object != null)
      {	
	for (int i = 0; i < object.getNestedSize(); i++)
	  {
	    DataElement subObject = (DataElement)object.get(i);
	    if (keyName.equals(subObject.getName()))
	      {
		return subObject;
	      }
	  }

	return resolveName(object.getParent(), keyName);
      }

    return null;
  }


  /////////////////////////////////////////
  //
  // File Operations
  //
  /////////////////////////////////////////

  public String mapToLocalPath(String aPath)
      {
	  String result = null;

	  char slash = File.separator.charAt(0);
	  String remotePath = aPath.replace('\\', slash).replace('/', slash);
	  String localRoot = _dataStoreAttributes.getAttribute(DataStoreAttributes.A_LOCAL_PATH).replace('\\', slash).replace('/', slash);
	  String remoteRoot = getHostRoot().getSource().replace('\\', slash).replace('/', slash);

	  if (localRoot.equals(remoteRoot))
	      {
		  result = remotePath;
	      }
	  else if (remotePath.startsWith(localRoot))
	      {
		  result = remotePath;
	      }
	  else if (remotePath.startsWith(remoteRoot))
	      {
		  result = new String(localRoot + remotePath.substring(remoteRoot.length(), remotePath.length()));
	      }
	  else
	      {
		  // file is outside of scope
		  // create temporary location
		  int indexOfDrive = remotePath.indexOf(":");
		  if (indexOfDrive > 0)
		      {
			  remotePath = remotePath.substring(indexOfDrive + 1, remotePath.length());
		      }

		  result = new String(localRoot + remotePath.substring(localRoot.indexOf(slash), remotePath.length()));
	      }

	  return result;
      }
  

  public void saveFile(DataElement root, String remotePath, int depth)
      {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        try
        {
          // need to create directories as well
          File file = new File(fileName);
          if (!file.exists())
          {
            File dir = new File(file.getParent());
            dir.mkdirs();
          }
	  
          File newFile = new File(fileName);
          FileOutputStream fileStream = new FileOutputStream(newFile);
	  PrintStream writer = new PrintStream(fileStream);

          XMLgenerator generator = new XMLgenerator();
          generator.setWriter(writer);
          generator.setBufferSize(1000);
	  generator.generate(root, depth);
	  generator.flush();
	  
          fileStream.close();
        }
        catch (IOException e)
        {
          System.out.println(e);
        }
      }

  public void saveFile(String fileName, InputStream input)
      {
          try
          {
            // need to create directories as well
            File file = new File(fileName);
            if (!file.exists())
            {
	      File parent = new File(file.getParent());	      
	      parent.mkdirs();
            }
            else
            {
              file.renameTo(new File(fileName + ".bak"));
            }

            File newFile = new File(fileName);
            FileOutputStream fileStream = new FileOutputStream(newFile);
            BufferedReader in= new BufferedReader(new InputStreamReader(input));

            StringBuffer buffer = new StringBuffer();
            String line = null;

            while ((line = in.readLine()) != null)
            {
              buffer.append(new String(line));
              buffer.append("\n\r");
            }
            
            fileStream.write(buffer.toString().getBytes());            
            fileStream.close();
          }
          catch (IOException e)
          {
            System.out.println(e);
          }        
      }

    public void saveFile(String remotePath, byte[] buffer)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
	
        if (fileName != null)
        {
          try
          {
            // need to create directories as well
            File file = new File(fileName);
            if (!file.exists())
            {
	      File parent = new File(file.getParent());	      
	      parent.mkdirs();
            }
            else
            {
              file.renameTo(new File(fileName + ".bak"));
            }

            File newFile = new File(fileName);
            FileOutputStream fileStream = new FileOutputStream(newFile);
            fileStream.write(buffer);
            fileStream.close();
          }
          catch (IOException e)
          {
            System.out.println(e);
          }
        }
    }   

    public void appendToFile(String remotePath, byte[] buffer)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
	
        if (fileName != null)
        {
          try
          {
            // need to create directories as well
            File file = new File(fileName);
            if (!file.exists())
		{
		    File parent = new File(file.getParent());	      
		    parent.mkdirs();

		    File newFile = new File(fileName);
		    FileOutputStream fileStream = new FileOutputStream(newFile);
		    fileStream.write(buffer);
		    fileStream.close();
		}
	    else
		{
		    // need to reorganize this so that we don't use up all the memory
		    // divide appendedBuffer into chunks
		    // at > 50M this kills Eclipse
		    File oldFile = new File(fileName);
		    File newFile = new File(fileName + ".new");

		    FileInputStream  oldFileStream = new FileInputStream(oldFile);            
		    FileOutputStream newFileStream = new FileOutputStream(newFile);

		    // write old file to new file
		    int maxSize = 5000000;
		    int written = 0;
		    int oldSize = (int)oldFile.length();		    
		    int bufferSize = (oldSize > maxSize) ? maxSize : oldSize;
		    byte[] subBuffer = new byte[bufferSize];

		    while (written < oldSize)
			{
			    int subWritten = 0;
			    
			    while (written < oldSize && subWritten < bufferSize)
				{
				    int available = oldFileStream.available();
				    available = (bufferSize > available) ? available : bufferSize;
				    int read = oldFileStream.read(subBuffer, subWritten, available);
				    subWritten += read;
				    written += subWritten;
				}
			    
			    newFileStream.write(subBuffer, 0, subWritten);
			}
		    
		    oldFileStream.close();		    		    

		    // write new buffer to new file
		    newFileStream.write(buffer);
		    newFileStream.close();

		    // remote old file
		    oldFile.delete();

		    // rename new file
		    newFile.renameTo(oldFile);
		}
          }
          catch (IOException e)
          {
            System.out.println(e);
          }
        }
    }   

  public void saveFile(String remotePath, StringBuffer contents)
      {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
	
        if (fileName != null)
        {
          try
          {
            // need to create directories as well
            File file = new File(fileName);
            if (!file.exists())
            {
	      File parent = new File(file.getParent());	      
	      parent.mkdirs();
            }
            else
            {
              file.renameTo(new File(fileName + ".bak"));
            }

            File newFile = new File(fileName);


            FileOutputStream fileStream = new FileOutputStream(newFile);
            fileStream.write(contents.toString().getBytes());
            fileStream.close();
          }
          catch (IOException e)
          {
            System.out.println(e);
          }
        }
      }

  public void load(DataElement root, String pathName)
      {
        String fileName = pathName;
        BufferedInputStream document = loadFile(fileName);
        if (document != null)
        {
          try
          {
            XMLparser parser = new XMLparser(this);
            DataElement subRoot = parser.parseDocument(document);
	    if (subRoot != null)
		{
		    root.removeNestedData();
		    root.addNestedData(subRoot.getNestedData(), true);
		    refresh(root);
		}
          }
          catch (IOException e)
          {
          }
        }
      }

  public static BufferedInputStream loadFile(String fileName)
      {
        String document = null;
        File file = new File(fileName);
        if (file.exists() && (file.length() > 0))
        {
          try
          {
            FileInputStream inFile = new FileInputStream(file);            
            BufferedInputStream in= new BufferedInputStream(inFile);

            return in;
          }
          catch (FileNotFoundException e)
          {
            return null;
          }
        }
        else
        {
          return null;
        }
      }

  public String readerToString(BufferedReader in)
      {
        if (in != null)
        {
          StringBuffer buffer= new StringBuffer();
          
          String line;
          try
          {
            while ((line = in.readLine()) != null)
            {
              buffer.append(line);
              buffer.append("\n");
            }
          }
          catch (IOException e)
          {
          }

          return buffer.toString();
        }
        else
        {
          return null;
        }
      }

  /////////////////////
  //
  // tests
  //
  //////////////////////

  public boolean sameTree(DataElement root1, DataElement root2, int depth)
  {
    if (root1.equals(root2))
      {
	depth--;
	
	if (depth > 0)
	  {	
	    int size1 = root1.getNestedSize();
	    int size2 = root2.getNestedSize();
	
	    if (size1 == size2)
	      {	
		for (int i = 0; i < size1; i++)
		  {
		    DataElement child1 = (DataElement)root1.get(i);
		    DataElement child2 = (DataElement)root2.get(i);
		
		    if (!sameTree(child1, child2, depth))		
		      {
			return false;
		      }
		  }

		return true;
	      }
	    else
	      {
		return false;
	      }
	  }	
	else
	  {
	    return true;
	  }
      }

    return false;
  }


  public void walkTree(DataElement root)
  {
    if (root != null)
      {	
	root.expandChildren();
	for (int i = 0; i < root.getNestedSize(); i++)
        {
          DataElement currentElement = (DataElement)root.get(i);
          walkTree(currentElement);
        }
      }
  }

  public boolean filter(DataElement descriptor, DataElement dataElement)
  {
    return filter(descriptor, dataElement, 2);
  }
  

  public boolean filter(DataElement descriptor, DataElement dataElement, int depth)
  {
    if (depth > 0)
      {	
	depth--;
	
	String dataType = (String)dataElement.getElementProperty(DE.P_TYPE);
	String typeStr  = (String)descriptor.getElementProperty(DE.P_NAME);
	
	if (((dataType != null) && (typeStr != null)) &&
	    (dataType.equals(typeStr) || typeStr.equals(getLocalizedString("model.all"))))
	  {
	    return true;
	  }
	else
	  {
	    for (int i = 0; i < descriptor.getNestedSize(); i++)
	      {
		if (filter((DataElement)descriptor.get(i), dataElement, depth))
		  {
		    return true;
		  }
	      }
	    
	    return false;
	  }
      }
    
    return false;
  }

  public boolean filter(ArrayList descriptors, DataElement dataElement)
  {
    for (int i = 0; i < descriptors.size(); i++)
    {
      if (filter((DataElement)descriptors.get(i), dataElement))
      {
        return true;
      }
    }
    
    return false;
  }

  public boolean isTransient(DataElement commandObject)
      {
        boolean isTransient = false;
        DataElement subject = (DataElement)commandObject.get(0);

        DataElement subjectDescriptor = subject.getDescriptor();
        if (subjectDescriptor != null)
        {
          DataElement minerElement = getMinerFor(commandObject);
          DataElement transientObjects = find(minerElement, DE.A_TYPE, getLocalizedString("model.transient"), 1);
	  if (transientObjects != null)
	    {
	      for (int i = 0; i < transientObjects.getNestedSize(); i++)
		{
		  DataElement transientDescriptor = transientObjects.get(i).dereference();
		  if (transientDescriptor == subjectDescriptor)
		    {
		      isTransient = true;
		    }
		}
	    }
	}
	
        return isTransient;
      }

  public void initializeDescriptors()
      {
	// miner-specific descriptors are defined in the miners when they extend the schema

	// these first elements are the most fundamental
	  
	// move this
        DataElement uiCmdD    = createObject(_descriptorRoot, DE.T_UI_COMMAND_DESCRIPTOR, DE.T_UI_COMMAND_DESCRIPTOR);

        DataElement commandDescriptor      = createCommandDescriptor (_descriptorRoot, DE.T_COMMAND_DESCRIPTOR);
        DataElement objectDescriptor     = createObjectDescriptor  (_descriptorRoot, DE.T_OBJECT_DESCRIPTOR);
	DataElement relationDescriptor      = createRelationDescriptor(_descriptorRoot, DE.T_RELATION_DESCRIPTOR);

	DataElement abstractObjectDescriptor      = createAbstractObjectDescriptor   (_descriptorRoot, DE.T_ABSTRACT_OBJECT_DESCRIPTOR);
        DataElement abstractCommandDescriptor      = createAbstractCommandDescriptor  (_descriptorRoot, DE.T_ABSTRACT_COMMAND_DESCRIPTOR);
        DataElement abstractRelationDescriptor      = createAbstractRelationDescriptor (_descriptorRoot, DE.T_ABSTRACT_RELATION_DESCRIPTOR);

        DataElement rootD    = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.root"));
        DataElement hostD    = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.host"));
       
	DataElement logD      = createObjectDescriptor(_descriptorRoot,  getLocalizedString("model.log"));
	DataElement statusD   = createObjectDescriptor(_descriptorRoot,  getLocalizedString("model.status"));

	DataElement deletedD   = createObjectDescriptor(_descriptorRoot,  getLocalizedString("model.deleted"));


	// misc
        DataElement allD      = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.all"));
	DataElement invokeD   = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.invocation"));	
	DataElement patternD  = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.pattern"));	

	DataElement inputD    = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.input"));
	DataElement outputD   = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.output"));

	// types of relationships
	DataElement containsD     = createRelationDescriptor(_descriptorRoot, getLocalizedString("model.contents"));
	DataElement parentD       = createRelationDescriptor(_descriptorRoot, getLocalizedString("model.parent"));	
	DataElement argsD         = createRelationDescriptor(_descriptorRoot, getLocalizedString("model.arguments"));	
	DataElement abstracts     = createRelationDescriptor(_descriptorRoot, getLocalizedString("model.abstracts"));	
	DataElement abstractedBy  = createRelationDescriptor(_descriptorRoot, getLocalizedString("model.abstracted_by"));	

	DataElement caRelations = createAbstractRelationDescriptor(_descriptorRoot, getLocalizedString("model.contents&arguments"));
	createReference(caRelations, containsD);
	createReference(caRelations, argsD);
	
        createReference(objectDescriptor, containsD);
        createReference(objectDescriptor, parentD);
        createReference(objectDescriptor, abstractedBy);

        createReference(abstractObjectDescriptor, containsD);
        createReference(abstractObjectDescriptor, parentD); 
        createReference(abstractObjectDescriptor, abstracts);
        createReference(abstractObjectDescriptor, abstractedBy);

	createReference(statusD, containsD);	
	DataElement statusDetails = createAbstractObjectDescriptor(statusD, getLocalizedString("model.details"));	
        createReference(statusDetails, allD);

	createReference(commandDescriptor, allD);	
	createReference(commandDescriptor, caRelations);
	createReference(commandDescriptor, argsD);	
	createReference(commandDescriptor, containsD);	

        DataElement desDetails = createAbstractObjectDescriptor(objectDescriptor, getLocalizedString("model.details"));
        createReference(desDetails, objectDescriptor);
        createReference(desDetails, commandDescriptor);
        createReference(desDetails, abstractObjectDescriptor);
        createReference(desDetails, abstractCommandDescriptor);
        createReference(desDetails, abstractRelationDescriptor);
        createReference(desDetails, uiCmdD);

        DataElement logDetails = createAbstractObjectDescriptor(logD, getLocalizedString("model.Commands"));
        createReference(logDetails, commandDescriptor);
        createReference(logDetails, allD);        
	createReference(logD, caRelations);
	createReference(logD, containsD);

	 //Base Container Object
        DataElement containerObjectD = createAbstractObjectDescriptor(_descriptorRoot, getLocalizedString("model.Container_Object"));
        createCommandDescriptor(containerObjectD, getLocalizedString("model.Query"),   "*", "C_QUERY");
        createCommandDescriptor(containerObjectD, getLocalizedString("model.Refresh"), "*", "C_REFRESH");
        createCommandDescriptor(containerObjectD, getLocalizedString("model.Open"),    "*", "C_OPEN");
        createCommandDescriptor(containerObjectD, getLocalizedString("model.Close"),   "*", "C_CLOSE");
	createReference(containerObjectD, containsD);
        createReference(containerObjectD, parentD); 
       

      // file objects
	createReference(hostD, containsD);	

        DataElement deviceD  = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.device"), 
						      "com.ibm.dstore.miners");

        DataElement dirD     = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.directory"), 
						      "com.ibm.dstore.miners");

        DataElement folderD  = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.folder"), 
						      "com.ibm.dstore.miners");
        DataElement fileD    = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.file"), 
						      "com.ibm.dstore.miners");

        DataElement fsObject = createAbstractObjectDescriptor(_descriptorRoot, getLocalizedString("model.Filesystem_Objects"), "com.ibm.dstore.miners");
         createReference(containerObjectD, fsObject, getLocalizedString("model.abstracts"), getLocalizedString("model.abstracted_by"));


        createReference(fsObject, deviceD,  getLocalizedString("model.abstracts"), getLocalizedString("model.abstracted_by"));
        createReference(fsObject, dirD,     getLocalizedString("model.abstracts"), getLocalizedString("model.abstracted_by"));
        createReference(fsObject, fileD,    getLocalizedString("model.abstracts"), getLocalizedString("model.abstracted_by"));
        createReference(fsObject, fileD,    getLocalizedString("model.contents"));
        createReference(fsObject, dirD,     getLocalizedString("model.contents"));
	createReference(fsObject, fsObject, getLocalizedString("model.contents"));
	createReference(fsObject, fsObject, getLocalizedString("model.parent"));
	createReference(hostD,    fsObject, getLocalizedString("model.contents"));
	createReference(deviceD,  dirD);
	createReference(deviceD,  fileD);
	createReference(dirD,     dirD);
	createReference(dirD,     fileD);

	DataElement hostDirectories  = createAbstractObjectDescriptor(hostD, getLocalizedString("model.Directories"),
								      "com.ibm.dstore.miners");	
	createReference(hostDirectories, dirD);
	createReference(hostDirectories, deviceD);

	DataElement hostDetails  = createAbstractObjectDescriptor(hostD, getLocalizedString("model.Details"));	        
	createReference(hostDetails, hostDirectories);
        createReference(hostDetails, fileD);

	// miner descriptors
	DataElement minersD      = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.miners"));
	DataElement minerD       = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.miner"));
	DataElement dataD        = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.data"));
	DataElement transientD   = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.transient"));
	DataElement stateD       = createObjectDescriptor(_descriptorRoot, getLocalizedString("model.state"));
        
	DataElement minerDetails = createAbstractObjectDescriptor(minerD, getLocalizedString("model.Miner_Details"));
	createReference(minerDetails, allD);

        DataElement hostsDetails = createAbstractObjectDescriptor(rootD, getLocalizedString("model.Hosts"));	
	createReference(hostsDetails, rootD);	
	createReference(hostsDetails, hostD);	
	createReference(hostsDetails, deviceD);	
	createReference(hostsDetails, dirD);	

        DataElement rootDetails = createAbstractObjectDescriptor(rootD, getLocalizedString("model.Details"));	
	createReference(rootDetails, rootD);	
	createReference(rootDetails, minersD);	
	createReference(rootDetails, minerD);	

        DataElement minerObjects = createAbstractObjectDescriptor(rootD, getLocalizedString("model.Tools"));
        createReference(minerObjects, minersD);
        createReference(minerObjects, minerD);
	createReference(minerObjects, dataD);	
	createReference(minerObjects, stateD);	
	
        DataElement schemaDetails = createAbstractObjectDescriptor(rootD, getLocalizedString("model.Schema"));
        createReference(schemaDetails, objectDescriptor);

        DataElement logInfo = createAbstractObjectDescriptor(rootD, getLocalizedString("model.Logged_Commands"));
        createReference(logInfo, logD);
        createReference(logInfo, commandDescriptor);

        // basic commands
	DataElement cancel = createCommandDescriptor(commandDescriptor, getLocalizedString("model.Cancel"), "*", "C_CANCEL");	
 	DataElement oQuery = createCommandDescriptor(objectDescriptor, getLocalizedString("model.Query"), "-", "C_QUERY");

	DataElement set = createCommandDescriptor(objectDescriptor, getLocalizedString("model.Set"), "-", "C_SET"); 

	DataElement setHost = createCommandDescriptor(rootD, getLocalizedString("model.Set_Host"), "-", "C_SET_HOST");
	setHost.setDepth(0);
	DataElement info  = createCommandDescriptor(rootD, getLocalizedString("model.Init_Miners"), "*", 
						    "C_INIT_MINERS");
	info.setDepth(0);
	DataElement setMiners = createCommandDescriptor(rootD, "Set Miners", "-", 
							"C_SET_MINERS");
	setMiners.setDepth(0);
	DataElement vTicket = createCommandDescriptor(rootD, getLocalizedString("model.Show_Ticket"), "-", 
						      "C_VALIDATE_TICKET");	
	
	vTicket.setDepth(0);
	DataElement sQuery = createCommandDescriptor(rootD, getLocalizedString("model.Get_Schema"), "*", 
						     "C_SCHEMA");	

	sQuery.setDepth(0);
	DataElement oExit = createCommandDescriptor(rootD, getLocalizedString("model.Exit"), "*", "C_EXIT");	 
	oExit.setDepth(0);
      }
}

