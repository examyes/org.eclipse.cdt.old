package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*; 
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.server.ILoader;

import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * <code>DataStore</code> is the heart of the <code>DataStore</code> Distributed Tooling Framework.
 * This class is used for creating, deleting and accessing <code>DataElement</code>s and for communicating commands 
 * to miners (tools).  
 *
 * <p>
 * Every <code>DataStore</code> has both a command handler and an update handler.  The command 
 * handler is responsible for sending commands, in the form of <code>DataElement</code> trees, to the appropriate
 * implementer, either directly to the miner, or indirectly over the communication layer through a server 
 * <code>DataStore</code>.  The update handler is responsible for notifying listeners about changes in the 
 * <code>DataStore</code>, either directly via a <code>DomainNotifier</code> or indirectly over the communication
 * layer through a client <code>DataStore</code>.  
 * </p>
 *
 */
public final class DataStore
{
    private DataStoreAttributes _dataStoreAttributes;
    
    private DataElement         _root;
    private DataElement         _descriptorRoot;
    private DataElement         _logRoot;
    private DataElement         _hostRoot;
    private DataElement         _minerRoot;
    private DataElement         _tempRoot;
    private DataElement         _status;

    private DataElement         _ticket;  

    private DataStoreSchema     _dataStoreSchema;    
    private CommandHandler      _commandHandler;
    private UpdateHandler       _updateHandler;
    private ByteStreamHandler         _byteStreamHandler;
    
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

    private int                 _initialSize;

    private File                _traceFileHandle;
    private RandomAccessFile    _traceFile;
    private boolean             _tracingOn;

    private ArrayList           _waitingStatuses = null;
    
    /**
     * Creates a new <code>DataStore</code> instance
     *
     * @param attributes the default attributes of the <code>DataStore</code>
     */
    public DataStore(DataStoreAttributes attributes)
    {
        _dataStoreAttributes = attributes;
        _commandHandler      = null;
        _updateHandler       = null;
        _domainNotifier      = null;
        _isConnected         = false;
        _logTimes            = false;
	_initialSize         = 100000;

	initialize();
      }

    /**
     * Creates a new DataStore instance
     *
     * @param attributes the default attributes of the <code>DataStore</code>
     * @param initialSize the initial number of preallocated <code>DataElement</code>s
     */
    public DataStore(DataStoreAttributes attributes, int initialSize)
    {
        _dataStoreAttributes = attributes;
        _commandHandler      = null;
        _updateHandler       = null;
        _domainNotifier      = null;
        _isConnected         = false;
        _logTimes            = false;
	_initialSize         = initialSize;

	initialize();
      }

    /**
     * Creates a new <code>DataStore</code> instance
     *
     * @param attributes the default attributes of the <code>DataStore</code>
     * @param commandHandler the DataStore's handler for sending commands
     * @param updateHandler the DataStore's handler for doing updates
     * @param domainNotifier the domain notifier 
     */
    public DataStore(DataStoreAttributes attributes, 
		     CommandHandler commandHandler,
		     UpdateHandler updateHandler, 
		     DomainNotifier domainNotifier)
      {
        _dataStoreAttributes = attributes;
        _commandHandler      = commandHandler;
        _updateHandler       = updateHandler;
        _domainNotifier      = domainNotifier;
        _isConnected         = true;
        _logTimes            = false;
	_initialSize         = 100000;

	initialize();
	createRoot();
      }

    /**
     * Creates a new DataStore instance
     *
     * @param attributes the default attributes of the <code>DataStore</code>
     * @param commandHandler the DataStore's handler for sending commands
     * @param updateHandler the DataStore's handler for doing updates
     * @param domainNotifier the domain notifier 
     * @param initialSize the initialNumber of preallocated <code>DataElement</code>s 
     */
  public DataStore(DataStoreAttributes attributes, 
		   CommandHandler commandHandler,
                   UpdateHandler updateHandler, 
		   DomainNotifier domainNotifier,
		   int initialSize)
      {
        _dataStoreAttributes = attributes;
        _commandHandler      = commandHandler;
        _updateHandler       = updateHandler;
        _domainNotifier      = domainNotifier;
        _isConnected         = true;
        _logTimes            = false;
	_initialSize         = initialSize;

	initialize();
	createRoot();
      }

    /**
     * Sets the ticket for this <code>DataStore</code>.  A ticket is used to prevent unauthorized users
     * from accessing the <code>DataStore</code>
     *
     * @param ticket the <code>DataElement</code> representing the ticket
     */
    public void setTicket(DataElement ticket)
    {
	_ticket = ticket;
    }

    /**
     * Sets the loader for this <code>DataStore</code>.  The loader is used to load miners (extension tools). 
     *
     * @param loader the loader for the miners this <code>DataStore</code> will be using
     */
    public void setLoader(ILoader loader)
    {
	_loader         = loader;
    }

    /**
     * Tells the <code>DataStore</code> where to find the miners which it needs to load. 
     *
     * @param minersLocation a string representing the location of the miners
     */
    public DataElement setMinersLocation(String minersLocation)
    {
	_minersLocation = minersLocation;
	DataElement location = createObject(_tempRoot, "location", _minersLocation);
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SET_MINERS", 1);  
	ArrayList args = new ArrayList();
	args.add(location);
	//synchronizedCommand(cmd, args, _root);
	return command(cmd, args, _root);
    }

    /**
     * Tells the <code>DataStore</code> where to find the miners which it needs to load. 
     *
     * @param minersLocation a <code>DataElement</code> representing the location of the miners
     */
    public void setMinersLocation(DataElement location)
    {
	if (_minersLocation != location.getName())
	    {
		_minersLocation = location.getName();
	    }
    }
  
    /**
     * Tells the <code>DataStore</code> that it is connected to it's tools 
     *
     * @param isConnected indicates whether it is connected or not
     */
    public void setConnected(boolean isConnected)
    {
	_isConnected = isConnected;
    }
 
    /**
     * Sets the <code>DataStore</code>'s DomainNotifier 
     *
     * @param domainNotifier the domainNotifier
     */
    public void setDomainNotifier(DomainNotifier domainNotifier)
    {
        _domainNotifier = domainNotifier;
    }

    /**
     * Sets the <code>DataStore</code>'s handler for doing updates 
     *
     * @param updateHandler the handler for doing updates
     */
    public void setUpdateHandler(UpdateHandler updateHandler)
    {
        _updateHandler = updateHandler;
    }

    /**
     * Sets the <code>DataStore</code>'s handler for sending commands to miners 
     *
     * @param commandHandler the handler for sending commands to miners
     */
    public void setCommandHandler(CommandHandler commandHandler)
    {
        _commandHandler = commandHandler;
    }

    /**
     * Sets the time the update handler sleeps in between update requests 
     *
     * @param time interval to wait
     */
    public void setUpdateWaitTime(int time)
    {
        _updateHandler.setWaitTime(time);
    }

    /**
     * Sets the time the command handler sleeps in between command requests 
     *
     * @param time interval to wait
     */
    public void setCommandWaitTime(int time)
    {
        _commandHandler.setWaitTime(time);
    }

    /**
     * Sets the maximum amount of time that the <code>DataStore</code> will wait to receive a response
     * for a synchronous command
     *
     * @param time interval to wait
     */
    public void setTimeoutValue(int time)
    {
	_timeout = time;
    }

    /**
     * Sets an attribute of the <code>DataStore</code>  
     *
     * @param attribute index of the attribute to set
     * @param value value to set the attribute at the give index
     */
    public void setAttribute(int attribute, String value)
    {
	_dataStoreAttributes.setAttribute(attribute, value);
    }

    /**
     * Tells the <code>DataStore</code> to log durations of commands  
     *
     * @param flag whether to log times or not
     */
    public void setLogTimes(boolean flag)
    {
        _logTimes = flag;
    }

    /**
     * Indicates whether this <code>DataStore</code> is virtual or not.  A virtual <code>DataStore</code>  
     * is one that does not have it's own tools, but rather communicates with a non-virtual
     * <code>DataStore</code> that does.
     *
     * @return whether the <code>DataStore</code> is virtual or not
     */
    public boolean isVirtual()
    {
	if (_commandHandler instanceof org.eclipse.cdt.dstore.core.client.ClientCommandHandler)
	    {
		return true;
	    }
	else
	    {
		return false;
	    }
    }  

    /**
     * Indicates whether this <code>DataStore</code> is connected to it's miners or another <code>DataStore</code>
     *
     * @return whether the <code>DataStore</code> is connected or not
     */
    public boolean isConnected()
    { 
        return _isConnected;
    }

    /**
     * Indicates whether this <code>DataStore</code> logs the durations of commands
     *
     * @return whether the <code>DataStore</code> logs command times or not
     */
    public boolean logTimes()
    {
        return _logTimes;
    }

    /**
     * Returns the <code>DataStore</code>'s ticket
     *
     * @return the ticket
     */
    public DataElement getTicket()
    {
	return _ticket;
    }

    /**
     * Returns the time the update handler waits between requests
     *
     * @return wait time
     */
    public int getUpdateWaitTime()
    {
        return _updateHandler.getWaitTime();
    }
    
    /**
     * Returns the time the command handler waits between requests
     *
     * @return wait time
     */
    public int getCommandWaitTime()
    {
        return _commandHandler.getWaitTime();
    }

    /**
     * Returns the ResourceBundle used to retrieve NL enabled strings
     *
     * @return the resource bundle
     */
    public ResourceBundle getResourceBundle()
    {
	return _resourceBundle;
    }

    /**
     * Retrieves an NL enabled string given a key
     *
     * @param key a key that maps to a string
     * @return the mapping for the given key
     */
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

    /**
     * Returns the name of the <code>DataStore</code>
     *
     * @return the name of the <code>DataStore</code>
     */
    public String getName()
    {
	return getAttribute(DataStoreAttributes.A_HOST_NAME);    
    }
  
    /**
     * Returns the root <code>DataElement</code> in the <code>DataStore</code>.
     * The root <code>DataElement</code> has no parent and contains every <code>DataElement</code>
     * in the <code>DataStore</code> through a <code>DataElement</code> tree
     *
     * @return the root <code>DataElement</code>
     */
    public DataElement getRoot()
    {
        return _root;
    }

    /**
     * Returns the host root <code>DataElement</code> in the <code>DataStore</code>.
     * The host root <code>DataElement</code> is a child of root and references
     * <code>DataElement</code>s in the <code>DataStore</code> that are related to host information
     *
     * @return the host root <code>DataElement</code>
     */
    public DataElement getHostRoot()
    {
	return _hostRoot;
    }

    /**
     * Returns the miner root <code>DataElement</code> in the <code>DataStore</code>.
     * The miner root <code>DataElement</code> is a child of root and contains
     * <code>DataElement</code>s the represent tools and the information that tools possess
     *
     * @return the miner root <code>DataElement</code>
     */
    public DataElement getMinerRoot()
    {
	return _minerRoot;
    }
    
    /**
     * Returns the status of the <code>DataStore</code>.  
     *
     * @return the status of the <code>DataStore</code>
     */
    public DataElement getStatus()
    {
	return _status;
    }  

    /**
     * Returns the log root <code>DataElement</code> of the <code>DataStore</code>.
     * The log root contains all commands that are issued from the <code>DataStore</code>
     *
     * @return the log root
     */
    public DataElement getLogRoot()
    {
        return _logRoot;
    }

    /**
     * Returns the descriptor root <code>DataElement</code> of the <code>DataStore</code>.
     * The descriptor root contains the schema for the <code>DataStore</code> and it's tools
     *
     * @return the descriptor root
     */
    public DataElement getDescriptorRoot()
      {
        return _descriptorRoot;
      }
    
    /**
     * Returns the temp root <code>DataElement</code> of the <code>DataStore</code>.
     * The temp root contains temporary information.
     *
     * @return the temp root
     */
    public DataElement getTempRoot()
    {
	return _tempRoot;
    }

    /**
     * Returns the handler for sending commands.
     *
     * @return the command handler
     */
    public CommandHandler getCommandHandler()
    {
	return _commandHandler;    
    }
  
    /**
     * Returns the handler for doing updates.
     *
     * @return the update handler
     */
    public UpdateHandler getUpdateHandler()
    {
	return _updateHandler;    
    }  
    
    /**
     * Returns the loader that is used for loading miners.
     *
     * @return the loader
     */
    public ILoader getLoader()
    {
	return _loader;
    }
    
    /**
     * Returns the location of the miners.
     *
     * @return the location of the miners
     */
    public String getMinersLocation()
    {
	return _minersLocation;
    }

    /**
     * Returns the domain notifier.
     *
     * @return the domain notifier
     */
    public DomainNotifier getDomainNotifier ()
    {
	return _domainNotifier;
    } 

    /**
     * Returns the attribute indicated by an index.
     *
     * @param the index of the attribute to get
     * @return the attribute
     */
    public String getAttribute(int attribute)
    {
	return _dataStoreAttributes.getAttribute(attribute);
    }

    /**
     * Returns the number of live elements in the <code>DataStore</code>.
     *
     * @return the number of live elements
     */
    public int getNumElements()
    {
	return _hashMap.size();    
    }

    /**
     * Returns the table of live elements in the <code>DataStore</code>.
     *
     * @return the table of live elements
     */
    public HashMap getHashMap()
    {
        return _hashMap;
    }

  
    /**
     * Initializes the <code>DataStore</code> by creating the root elements
     *
     */
    public void createRoot()
    {
	_root        = createObject(null, getLocalizedString("model.root"),
				    _dataStoreAttributes.getAttribute(DataStoreAttributes.A_ROOT_NAME),
				    _dataStoreAttributes.getAttribute(DataStoreAttributes.A_ROOT_PATH),
				    "rootID");
	
	_descriptorRoot = createObject(_root, DE.T_OBJECT_DESCRIPTOR, getLocalizedString("model.descriptors"),
				       "", "schemaID");
	
	_ticket = createObject(_root, getLocalizedString("model.ticket"), "null", "", "ticketID");
	
	createRoots();
	initializeDescriptors();
    }
    

    /**
     * Creates a contents relationship between two <code>DataElement</code>s
     *
     * @param from the element that contains the other
     * @param to the element that is contained by the other
     * @return the new reference
     */
    public DataElement createReference(DataElement from, DataElement to)
    {
	// default reference is a containment relationship
        return createReference(from, to, getLocalizedString("model.contents"));
    }

    /**
     * Creates a relationship between two <code>DataElement</code>s given a type of relationship
     *
     * @param parent the element that references the other element
     * @param realObject the element that is referenced by the parent element
     * @param relationType the descriptor element that represents the type of relationship between parent and realObject
     * @return the new reference
     */
    public DataElement createReference(DataElement parent, DataElement realObject, DataElement relationType)
      {
	  if (parent != null)
	      {

		  // reference with a specified type of relationship
		  DataElement reference = createElement();
		  
		  reference.reInit(parent, realObject, relationType);
		  parent.addNestedData(reference, false);
		  
		  String sugId = reference.getId();
		  _hashMap.put(sugId, reference);
		  
		  refresh(parent);

		  
		  return reference;
	      }
	  else
	      {
		  return null;
	      }
      }

    /**
     * Creates a relationship between two <code>DataElement</code>s given a type of relationship
     *
     * @param parent the element that references the other element
     * @param realObject the element that is referenced by the parent element
     * @param relationType the string that represents the type of relationship between parent and realObject
     * @return the new reference
     */
    public DataElement createReference(DataElement parent, DataElement realObject, String relationType)
      {
	  if (parent != null)
	      {
		  // reference with a specified type of relationship
		  DataElement reference = createElement();
		  
		  DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, relationType);
		  if (toDescriptor != null)
		      {
			  reference.reInit(parent, realObject, toDescriptor);
		      }
		  else
		      {
			  reference.reInit(parent, realObject, relationType);
		      }
		  
		  parent.addNestedData(reference, false);
		  
		  String sugId = reference.getId();
		  _hashMap.put(sugId, reference);

		  refresh(parent);
		  
		  return reference;
	      }
	  return null;
      }


    /**
     * Creates a set of  relationships between one <code>DataElement</code> and a set of <code>DataElement</code>s given a type of relationship
     *
     * @param from the element that references the other elements
     * @param to a list of elements that from references
     * @param type the string that represents the type of relationships between from and to
     * @return the new reference
     */
    public void createReferences(DataElement from, ArrayList to, String type)
    {
	DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, type);
	if (toDescriptor != null)
	    {
		createReferences(from, to, toDescriptor);
	    }
	else
	    {
		for (int i = 0; i < to.size(); i++)
		    {	
			DataElement toObject = (DataElement)to.get(i);
			createReference(from, toObject, type);	 
		    }
	    }
    }

    /**
     * Creates a set of  relationships between one <code>DataElement</code> and a set of <code>DataElement</code>s given a type of relationship
     *
     * @param from the element that references the other elements
     * @param to a list of elements that from references
     * @param type the descriptor element that represents the type of relationships between from and to
     */
    public void createReferences(DataElement from, ArrayList to, DataElement type)
    {
	for (int i = 0; i < to.size(); i++)
	    {	
		DataElement toObject = (DataElement)to.get(i);
		createReference(from, toObject, type);	 
	    }
    }
    
    
    /**
     * Creates a two-way relationship between two elements
     *
     * @param parent an element that references the other element
     * @param realObject an element that references the other element 
     * @param toRelation the descriptor element that represents the type of relationship between parent and realObject
     * @param fromRelation the descriptor element that represents the type of relationship between realObject and parent
     * @return the new reference
     */
    public DataElement createReference(DataElement parent, DataElement realObject, 
				     DataElement toRelation, DataElement fromRelation)
    {
	if (parent != null)
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
		
		refresh(parent);
		
		return toReference;
	    }
	return null;
      }

    /**
     * Creates a two-way relationship between two elements
     *
     * @param parent an element that references the other element
     * @param realObject an element that references the other element 
     * @param toRelation the string that represents the type of relationship between parent and realObject
     * @param fromRelation the string that represents the type of relationship between realObject and parent
     * @return the new reference
     */
    public DataElement createReference(DataElement parent, DataElement realObject, 
				     String toRelation, String fromRelation)
    {
	if (parent != null)
	    {
		// reference with "to" relationship
		DataElement toReference = createElement();
		DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, toRelation);
		if (toDescriptor != null)
		    {
			toReference.reInit(parent, realObject, toDescriptor);
		    }
		else
		    {
			toReference.reInit(parent, realObject, toRelation);
		    }
		
		parent.addNestedData(toReference, false);
		
		String toId = toReference.getId();
		_hashMap.put(toId, toReference);
		
		// reference with "from" relationship
		DataElement fromReference = createElement();
		
		DataElement fromDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, fromRelation);
		if (fromDescriptor != null)
		    {
			fromReference.reInit(realObject, parent, fromDescriptor);
		    }
		else
		    {
			fromReference.reInit(realObject, parent, fromRelation);
		    }
		
		realObject.addNestedData(fromReference, false);
		
		
		String fromId = fromReference.getId();
		_hashMap.put(fromId, fromReference);
		refresh(parent);
 	
		return toReference;
	    }
	return null;
      }

    /**
     * Creates a set of two-way relationship between a <code>DataElement</code> and a list of elements
     *
     * @param from an element that references the other elements
     * @param to a list of elements that reference from 
     * @param toRel the descriptor element that represents the type of relationship between from and to
     * @param fromRel the descriptor element that represents the type of relationship between to and from
     */
    public void createReferences(DataElement from, ArrayList to, DataElement toRel, DataElement fromRel)
    {
      for (int i = 0; i < to.size(); i++)
	  {	
	      DataElement toObject = (DataElement)to.get(i);
	      createReference(from, toObject, toRel, fromRel);	 
	  }
    }

    /**
     * Creates a set of two-way relationship between a DataElement and a list of elements
     *
     * @param from an element that references the other elements
     * @param to a list of elements that reference from 
     * @param toRel the string that represents the type of relationship between from and to
     * @param fromRel the string that represents the type of relationship between to and from
     */
    public void createReferences(DataElement from, ArrayList to, String toRel, String fromRel)
    {
	DataElement toDescriptor   = findDescriptor(DE.T_RELATION_DESCRIPTOR, toRel);
	DataElement fromDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, fromRel);
	
	if ((toDescriptor != null) && (fromDescriptor != null))
	    {
		createReferences(from, to, toDescriptor, fromDescriptor);
	    }
	else
	    {
		for (int i = 0; i < to.size(); i++)
		    {	
			DataElement toObject = (DataElement)to.get(i);
			createReference(from, toObject, toRel, fromRel);	 
		    }
	    }
    }
    

    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the descriptor representing the type of the new element 
     * @param name the name of the new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, DataElement type, String name)
    {
        return createObject(parent, type, name, "");
    }

    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the string representing the type of the new element 
     * @param name the name of the new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, String type, String name)
    {
        return createObject(parent, type, name, "");
    }

    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the descriptor element representing the type of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, DataElement type, String name, String source)
    {
        String id = generateId();
        return createObject(parent, type, name, source, id);
    }

    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the string representing the type of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, String type, String name, String source)
    {
        String id = generateId(parent, type, name);
	if (id == null)
	    {
		return null;
	    }
	
        return createObject(parent, type, name, source, id);
    }

    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the descriptor element representing the type of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param sugId the suggested ID for the new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, DataElement type, String name, String source, String sugId)
    {
	return createObject(parent, type, name, source, sugId, false);
    }
    
    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the string representing the type of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param sugId the suggested ID for the new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, String type, String name, String source, String sugId)
    {
	return createObject(parent, type, name, source, sugId, false);
    }

    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the descriptor element representing the type of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param sugId the suggested ID for the new element
     * @param isReference an indication whether the new element is a reference
     * @return the new element
     */
    public DataElement createObject(DataElement parent, DataElement type, String name, 
				    String source, String sugId, boolean isReference)
    {
        String id = makeIdUnique(sugId);
	
	DataElement newObject = createElement(); 
	if (parent == null)
	    {
		parent = _tempRoot;
	    }
	
	newObject.reInit(parent, type, id, name, source, isReference); 
	
        if (parent != null)
	    {
		parent.addNestedData(newObject, false);
	    }

        _hashMap.put(id, newObject);

	refresh(parent);
        return newObject;
    }

 
    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param type the string representing the type of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param sugId the suggested ID for the new element
     * @param isReference an indication whether the new element is a reference
     * @return the new element
     */
    public DataElement createObject(DataElement parent, String type, String name, String 
				    source, String sugId, boolean isReference)
    {
        String id = makeIdUnique(sugId);
	
	DataElement newObject = createElement();
	if (parent == null)
	    {
		parent = _tempRoot;
	    }
	
	DataElement descriptor = findDescriptor(DE.T_OBJECT_DESCRIPTOR, type);
	if (descriptor != null  && (parent != _descriptorRoot))
	    {
		newObject.reInit(parent, descriptor, id, name, source, isReference); 
	    }
	else
	    {
		newObject.reInit(parent, type, id, name, source, isReference); 
	    }

        if (parent != null)
	    {
		parent.addNestedData(newObject, false);
	    }

        _hashMap.put(id, newObject);
	refresh(parent);
        return newObject;
      }
    
    /**
     * Creates a new <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param attributes the attributes to use in this new element
     * @return the new element
     */
    public DataElement createObject(DataElement parent, String attributes[])
      {
	  DataElement newObject = createElement();

	  if (parent == null)
	      {
		  parent = _tempRoot;
	      }

	  DataElement descriptor = findDescriptor(DE.T_OBJECT_DESCRIPTOR, attributes[DE.A_TYPE]);
	  if (descriptor != null  && (parent != _descriptorRoot))
	      {
		  newObject.reInit(parent, descriptor, attributes);
	      }
	  else
	      {
		  newObject.reInit(parent, attributes);
	      }

	  if (parent != null)
	      {
		  parent.addNestedData(newObject, false);
	      }
	  
	  _hashMap.put(attributes[DE.A_ID], newObject);
	  return newObject;
      }

    /**
     * Creates a new abstract object descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @return the new descriptor element
     */
    public DataElement createAbstractObjectDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, "org.eclipse.cdt.dstore.core", name);
    }   

    /**
     * Creates a new abstract object descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @return the new descriptor element
     */
    public DataElement createAbstractObjectDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, source, name);
    }   

    /**
     * Creates a new object descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @return the new descriptor element
     */
    public DataElement createObjectDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, "org.eclipse.cdt.dstore.core", name);
    }   

    /**
     * Creates a new object descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the name of the new element
     * @return the new descriptor element
     */
    public DataElement createObjectDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, source, name);
    }   

    /**
     * Creates a new abstract relation descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @return the new descriptor element
     */
    public DataElement createAbstractRelationDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, "org.eclipse.cdt.dstore.core", name);
    }   

    /**
     * Creates a new abstract relation descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @return the new descriptor element
     */
    public DataElement createAbstractRelationDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, source, name);
    }   

    /**
     * Creates a new relation descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @return the new descriptor element
     */
    public DataElement createRelationDescriptor(DataElement parent, String name)
    {
	return createObject(parent, DE.T_RELATION_DESCRIPTOR, name, "org.eclipse.cdt.dstore.core", name);
    }   

    /**
     * Creates a new relation descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @return the new descriptor element
     */
    public DataElement createRelationDescriptor(DataElement parent, String name, String source)
    {
	return createObject(parent, DE.T_RELATION_DESCRIPTOR, name, source, name);
    }   

    /**
     * Creates a new abstract command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @return the new descriptor element
     */
    public DataElement createAbstractCommandDescriptor(DataElement parent, String name)
    {
	return createAbstractCommandDescriptor(parent, name, name);
    }

    /**
     * Creates a new abstract command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param value the value used to identify the command
     * @return the new descriptor element
     */
    public DataElement createAbstractCommandDescriptor(DataElement parent, String name, String value)
    {
	DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, "org.eclipse.cdt.dstore.core", name);
        cmd.setAttribute(DE.A_VALUE, value);        
	return cmd;
    }   

    /**
     * Creates a new abstract command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param value the value used to identify the command
     * @return the new descriptor element
     */
    public DataElement createAbstractCommandDescriptor(DataElement parent, String name, String source, String value)
    {
	DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, source, name);
        cmd.setAttribute(DE.A_VALUE, value);        
	return cmd;
    }   

    /**
     * Creates a new command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @return the new descriptor element
     */
    public DataElement createCommandDescriptor(DataElement parent, String name)
    {
	return createCommandDescriptor(parent, name, name); 
    } 

    /**
     * Creates a new command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param value the value used to identify the command
     * @return the new descriptor element
     */
    public DataElement createCommandDescriptor(DataElement parent, String name, String value)
    {
		DataElement cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, "org.eclipse.cdt.dstore.core", name);
        cmd.setAttribute(DE.A_VALUE, value);        
		return cmd;
    }   

    /**
     * Creates a new command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param value the value used to identify the command
     * @return the new descriptor element
     */
    public DataElement createCommandDescriptor(DataElement parent, String name, String source, String value)
    {
		DataElement cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, source, name);
        cmd.setAttribute(DE.A_VALUE, value);        
		return cmd;
    }   

    /**
     * Creates a new command descriptor <code>DataElement</code>
     *
     * @param parent the parent of the new element 
     * @param name the name of the new element
     * @param source the source location of the new element
     * @param value the value used to identify the command
     * @param visible indicates whether the command is visible or not
     * @return the new descriptor element
     */
    public DataElement createCommandDescriptor(DataElement parent, String name, String source, 
					       String value, boolean visible)
    {
	DataElement cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, source, name);
        cmd.setAttribute(DE.A_VALUE, value);        
	if (!visible)
	    {
		cmd.setDepth(0);
	    }
	
	return cmd;
    }   


    /**
     * Moves a element from one location in the <code>DataStore</code> tree to another
     *
     * @param source the element to move 
     * @param target the element to move source to
     */
    public void moveObject(DataElement source, DataElement target)
    {
        DataElement oldParent = source.getParent();
        oldParent.getNestedData().remove(source);
        refresh(oldParent, true);
	
        target.addNestedData(source, false);
        source.setParent(target);  
        refresh(target, true);
    }
  
    /**
     * Deletes all the elements contained in from
     *
     * @param from the element from which to delete objects from  
     */
    public void deleteObjects(DataElement from)
      {
	  if (from != null)
	      {
		  for (int i = from.getNestedSize() - 1; i >= 0; i--)
		      {
			  DataElement deletee = from.get(i);
			  if (deletee != null)
			      {
				  deleteObjectHelper(from, deletee, 5);		  
			      }
		      }
		  
		  refresh(from);
	      }
      }

    /**
     * Deletes an element from another element
     *
     * @param from the element from which to delete an object from  
     * @param toDelete the element to remove  
     */
    public void deleteObject(DataElement from, DataElement toDelete)
    {
	if (toDelete != null)
	    {
		deleteObjectHelper(from, toDelete, 5);
		refresh(toDelete);	
		refresh(from);
	    }
    }
    
    /**
     * Replaces a deleted object
     */
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
    
    /**
     * Checks if a <code>DataElement</code> with a given ID exists in the <code>DataStore</code>
     *
     * @param id the id to look for  
     * @return whether it exists or not  
     */
    public boolean contains(String id)
    {
	return _hashMap.containsKey(id);
    }

    /**
     * Refresh a set of <code>DataElement</code>s
     *
     * @param elements a list of elements to refresh
     */
    public void refresh(ArrayList elements)
    {
        // this gets called in response to a query
        for (int i = 0; i < elements.size(); i++)
	    {
		refresh((DataElement)elements.get(i));
	    }
    }

    /**
     * Refresh a <code>DataElement</code>
     *
     * @param element an element to refresh
     */
    public void refresh(DataElement element)
    {
    	if (element != null)
    	{
    		if (element.isReference()) 
    		{
    			refresh(element.dereference(), false);
    		}    	
		refresh(element, false);	
    	}
    }

    /**
     * Refresh a <code>DataElement</code> - immediately if indicated
     *
     * @param element an element to refresh
     * @param immediate indicates to do the refresh immediately
     */
    public void refresh(DataElement element, boolean immediate)
    {
        if ((_updateHandler != null) && (element != null))
	    {
		// update either client or ui
		//element.setUpdated(false);	
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
    
    /**
     * Transfers a file from a server to a client.  The should only be called from
     * a miner on a different machine from the client.
     *
     * @param remotePath the path of the file on the server side 
     * @param associatedObject the element representing this file
     */
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

    /**
     * Transfers a file from a server to a client.  This should only be called from
     * a miner on a different machine from the client.  If a file exists on the client
     * side that the server file maps to then the existing client file will be replaced.
     *
     * @param remotePath the path of the file on the server side 
     * @param bytes an array of bytes representing a file 
     * @param size the number of bytes to transfer
     */
    public void updateFile(String remotePath, byte[] bytes, int size, boolean binary)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        if (fileName != null)
        {
	    _updateHandler.updateFile(remotePath, bytes, size, binary);
        }
      }

    /**
     * Transfers and appends a file from a server to a client.  This should only be called from
     * a miner on a different machine from the client.  If a file exists on the client
     * side that the server file maps to then the existing client file will be appended to
     *
     * @param remotePath the path of the file on the server side 
     * @param bytes an array of bytes representing a file 
     * @param size the number of bytes to transfer
     */
    public void updateAppendFile(String remotePath, byte[] bytes, int size, boolean binary)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        if (fileName != null)
        {
	    _updateHandler.updateAppendFile(remotePath, bytes, size, binary);
        }	
    }

    /**
     * Transfers a file from a client to a server.  The should only be called from
     * a client on a different machine from the server.  If a file exists on the server
     * side that the client file maps to then the existing server file will be replaced.
     *
     * @param remotePath the path of the file on the server side 
     * @param file the file to transfer 
     */
    public void replaceFile(String remotePath, File file)
    {
	remotePath = new String(remotePath.replace('\\', '/'));
   	String fileName = mapToLocalPath(remotePath);
	
	String dsName = getName();
	if (!dsName.equals("local"))
	    {
		_commandHandler.sendFile(remotePath, file);
	    }
	else
	    {
		saveFile(remotePath, file);
	    }
    }
      
    /**
     * Transfers a file from a client to a server.  The should only be called from
     * a client on a different machine from the server.  If a file exists on the server
     * side that the client file maps to then the existing server file will be replaced.
    *
     * @param remotePath the path of the file on the server side 
     * @param bytes an array of bytes representing a file 
     * @param size the number of bytes to transfer
     */
    public void replaceFile(String remotePath, byte[] bytes, int size, boolean binary)
    {
	remotePath = new String(remotePath.replace('\\', '/'));

	_commandHandler.sendFile(remotePath, bytes, size, binary);
   }
  
    /**
     * Transfers a file from a client to a server.  The should only be called from
     * a client on a different machine from the server.  If a file exists on the server
     * side that the client file maps to then the existing server file will be appended to.
     *
     * @param remotePath the path of the file on the server side 
     * @param bytes an array of bytes representing a file 
     * @param size the number of bytes to transfer
     */
    public void replaceAppendFile(String remotePath, byte[] bytes, int size, boolean binary)
    {
		remotePath = new String(remotePath.replace('\\', '/'));
	
		_commandHandler.sendAppendFile(remotePath, bytes, size, binary);  	
    }


	public void sendFile(String remotePath)
	{
		_byteStreamHandler.sendBytes(remotePath);		
	}

    /**
     * Makes a given client element available on the server
     *
     * @param localObject the element to transfer
     */
    public void setObject(DataElement localObject)
    {
	setObject(localObject, true);
    }
    
    /**
     * Makes a given client element available on the server
     *
     * @param localObject the element to transfer
     * @param noRef indicates whether the element is a reference or not
     */
    public void setObject(DataElement localObject, boolean noRef)
    {
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SET", 1);  
	DataElement status = synchronizedCommand(cmd, localObject, noRef);
    }

    public void modifyObject(DataElement localObject)
    {
	DataElement cmd = find(_descriptorRoot, DE.A_NAME, getLocalizedString("model.Modify"), 2);  
	DataElement status = _commandHandler.command(cmd, localObject, true);
	waitUntil(status, getLocalizedString("model.done"));
    }

    /**
     * Used at <code>DataStore</code> initialization time to indicate where to point the host root
     *
     * @param localHostObject the client host element to transfer to the server site 
     */
    public DataElement setHost(DataElement localHostObject)
    {
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SET_HOST", 1);  
	DataElement status = _commandHandler.command(cmd, localHostObject, false);
	waitUntil(status, getLocalizedString("model.done"));
	return status;
    }
    
    /**
     * Used at <code>DataStore</code> initialization time to setup the schema
     *
     */
    public DataElement getSchema()
    {
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_SCHEMA", 1);
       	return command(cmd, _descriptorRoot);
    }
    
    /**
     * Used at <code>DataStore</code> initialization time to initialize the miners
     *
     * @return the status element for the initMiners command
     */
    public DataElement initMiners()
    {
	DataElement cmd = localDescriptorQuery(_root.getDescriptor(), "C_INIT_MINERS", 1);
	return synchronizedCommand(cmd, _descriptorRoot);
    } 
    
    
    /**
     * Used at <code>DataStore</code> initialization validate access to the <code>DataStore</code>
     *
     * @param ticketStr ticket string
     * @return and indication of whether the ticket is valid or not
     */
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
    
    /**
     * Indicates whether a client has permission to access the <code>DataStore</code>
     *
     * @return and indication of whether the ticket is valid or not
     */
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

    /**
     * Creates and issues a dataStore command from the specified command name 
     *
     * @param commandName the name of the command
     * @param object the subject of the command
     * @param synch an indication whether to synchronize the command or not
     * @return the status of the command
     */
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

    /**
     * Creates and issues a dataStore command from the specified command name 
     *
     * @param commandName the name of the command
     * @param arguments the arguments of the command
     * @param object the subject of the command
     * @param synch an indication whether to synchronize the command or not
     * @return the status of the command
     */
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

    /**
     * Wait until a given status element reached the specified state.  
     * This is used for issuing synchronized commands
     *
     * @param status the status element 
     * @param state the state to wait until 
     */
    public void waitUntil(DataElement status, String state)
    {
    	waitUntil(status, state, _timeout); 
    }

    public boolean isWaiting(DataElement status)
    {	
	return _waitingStatuses.contains(status);
    }
    
    public void stopWaiting(DataElement status)
    {
	_waitingStatuses.remove(status);
    } 

    public void startWaiting(DataElement status)
    {
	_waitingStatuses.add(status);
    }

    public void waitUntil(DataElement status, String state, int timeout)   
    {
	int timeToWait = 1000;
	int timeWaited = 0;
	boolean timedOut = false;
	startWaiting(status);

	while ((status != null) 
	       && (_status == null || _status.getName().equals("okay"))
	       && !status.getName().equals(state) 
	       && !status.getValue().equals(state)
	       && !status.getName().equals(getLocalizedString("model.incomplete")) 
	       && !timedOut)
	    {	
		if ((timeout != -1) && (timeWaited > timeout))
		    {
			// waited too long!
			timedOut = true;
		    }
		
		try
		    {		
			Thread.currentThread().sleep(timeToWait);
		    } 
		catch (InterruptedException e)
		    {
			System.out.println(e);
		    }
		
		timeWaited += timeToWait;

		if (!isWaiting(status))
		    {
			// stopped waiting
			return;
		    }
	    }

	stopWaiting(status);
	
	if (timedOut)
	    {
		status.setAttribute(DE.A_NAME, getLocalizedString("model.timeout"));
	    }
	
    }

    public void cleanBadReferences(DataElement root)
    {
    }

    /**
     * Tells the command handler to cancel all pending commands.  
     */
    public void cancelAllCommands()
    {
	_commandHandler.cancelAllCommands();
    }   

    /**
     * Creates and issues a synchronized command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param dataObject the subject of the command
     * @return the status of the command
     */
    public DataElement synchronizedCommand(DataElement commandDescriptor, DataElement dataObject)
    {
	return synchronizedCommand(commandDescriptor, dataObject, false);
    }

    /**
     * Creates and issues a synchronized command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param dataObject the subject of the command
     * @param noRef and indication of whether the subject should be referenced or not
     * @return the status of the command
     */
    public DataElement synchronizedCommand(DataElement commandDescriptor, DataElement dataObject, boolean noRef)
    {
	DataElement status = command(commandDescriptor, dataObject, noRef, true);
	waitUntil(status, getLocalizedString("model.done"));
	
	return status;
    }
    
    /**
     * Creates and issues a synchronized command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param dataObject the subject of the command
     * @param noRef and indication of whether the subject should be referenced or not
     * @return the status of the command
     */
    public DataElement synchronizedCommand(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject)
    {
	DataElement status = command(commandDescriptor, arguments, dataObject, true);
	waitUntil(status, getLocalizedString("model.done"));
	
	return status;
    }
  
    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param arguments the arguments for the command
     * @param dataObject the subject of the command
     * @return the status of the command
     */
    public DataElement command(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject)
    {
	return command(commandDescriptor, arguments, dataObject, false);
    }
    

    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param arguments the arguments for the command
     * @param dataObject the subject of the command
     * @param immediate indicates whether the command should be placed first on the request queue 
     * @return the status of the command
     */
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

    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param arg an argument for the command
     * @param dataObject the subject of the command
     * @return the status of the command
     */    
    public DataElement command(DataElement commandDescriptor, 
			       DataElement arg, DataElement dataObject
			       )
    {
	return command(commandDescriptor, arg, dataObject, false);
    } 
    
    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param arg an argument for the command
     * @param dataObject the subject of the command
     * @param immediate indicates whether the command should be placed first on the request queue 
     * @return the status of the command
     */    
    public DataElement command(DataElement commandDescriptor, 
			       DataElement arg, DataElement dataObject, 
			       boolean immediate)
    {
        if (_commandHandler != null)
	    {
		return _commandHandler.command(commandDescriptor, arg, dataObject, true, immediate);
	    }
	return null;
    }

    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param dataObject the subject of the command
     * @return the status of the command
     */    
    public DataElement command(DataElement commandDescriptor, DataElement dataObject)
    {
	return command(commandDescriptor, dataObject, false);    
    }
    
    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param dataObject the subject of the command
     * @param noRef an indication of whether to reference the subject or not
     * @return the status of the command
     */     
    public DataElement command(DataElement commandDescriptor, DataElement dataObject, boolean noRef)
    {
	return command(commandDescriptor, dataObject, noRef, false);
    }

    
    /**
     * Creates and issues a command.  
     *
     * @param commandDescriptor the comamnd descriptor for the command
     * @param dataObject the subject of the command
     * @param noRef an indication of whether to reference the subject or not
     * @param immediate an indication of whether
     * @return the status of the command
     */     
    public DataElement command(DataElement commandDescriptor, DataElement dataObject, boolean noRef, boolean immediate)
    {
        if (_commandHandler != null)
	    {
		return _commandHandler.command(commandDescriptor, dataObject, !noRef);
	    }
	
	return null;
    }
    
    /**
     * Issues a command.  
     *
     * @param commandObject an instance of a command
     * @return the status of the command
     */     
    public DataElement command(DataElement commandObject)
    {
	return _commandHandler.command(commandObject);	
    }
 
    /**
     * Delete information from the <code>DataStore</code>.  
     *
     */     
    public void flush()
    {
	// flush the whole thing
	flush(_logRoot);
	flush(_hostRoot);
	flush(_minerRoot);
	flush(_tempRoot);
    }
    
    /**
     * Delete information from the <code>DataStore</code> contained by an element.  
     *
     * @param element the element from which to delete
     */     
    public void flush(DataElement element)
      {
	if (element != null)
	  {	
	      deleteObjects(element);
	  }	
      }

    
    /**
     * Find a command descriptor element in the schema with the given value.  
     *
     * @param object the object descriptor representing the type of object that can issue such a command
     * @param keyName the value of the command to search for
     * @return the command descriptor for the specified command
     */     
    public DataElement localDescriptorQuery(DataElement object, String keyName)
    {
	return localDescriptorQuery(object, keyName, 5);
    }

    /**
     * Find a command descriptor element in the schema with the given value.  
     *
     * @param object the object descriptor representing the type of object that can issue such a command
     * @param keyName the value of the command to search for
     * @param depth the depth of abstraction to search 
     * @return the command descriptor for the specified command
     */     
    public DataElement localDescriptorQuery(DataElement descriptor, String keyName, int depth)
    {
	if ((descriptor != null) && (depth > 0))
	    {	
		for (int i = 0; i < descriptor.getNestedSize(); i++)
		    {
			DataElement subDescriptor = (DataElement)descriptor.get(i).dereference();
			String type = subDescriptor.getType();
			if (type == null)
			    {
			    }
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
		
		DataElement abstractedBy = find(_descriptorRoot, DE.A_NAME, getLocalizedString("model.abstracted_by"), 1);
		ArrayList abstractDescriptors = descriptor.getAssociated(abstractedBy);
		int numInherited = abstractDescriptors.size();
		
		for (int j = 0; j < numInherited; j++)
		    {
			DataElement abstractDescriptor = (DataElement)abstractDescriptors.get(j);
			
			DataElement result = localDescriptorQuery(abstractDescriptor, keyName, depth - 1);		    		    
			if (result != null)
			    {
				return result;		      
			    }
		    }		  
	    }
	
	return null;
    }
    

    /**
     * Finds the element that represents the miner that implements a particular command.  
     *
     * @param commandDescriptor a command descriptor
     * @return the element representing a miner
     */     
    public DataElement getMinerFor(DataElement commandDescriptor)
      {
        String minerName = commandDescriptor.getSource();
        DataElement theMinerElement = find(_minerRoot, DE.A_NAME, minerName, 1);
        return theMinerElement;
      }

    /**
     * Finds all the elements that are of a given type from a specified element.  
     *
     * @param root where to search from 
     * @param type the descriptor representing the type of the objects to search for 
     * @return a list of elements
     */     
    public ArrayList findObjectsOfType(DataElement root, DataElement type)
    {
	ArrayList results = new ArrayList();
	ArrayList searchList = root.getAssociated("contents");
	if (searchList != null)
	{
		for (int i = 0; i < searchList.size(); i++)
	    {
		DataElement child = (DataElement)searchList.get(i);
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
		}

	return results;
    }

    /**
     * Finds all relationship descriptor types that can be applied to a particular element.  
     *
     * @param descriptor the object descriptor that uses relationships 
     * @param fixateOn a filter for the type of relationships to look for 
     * @return a list of relationship descriptor elements 
     */     
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
/*
	DataElement containsD   = findDescriptor(DE.T_RELATION_DESCRIPTOR, getLocalizedString("model.contents"));	
	if (!result.contains(containsD))
	    {
		result.add(containsD);
	    }

	DataElement parentD   = findDescriptor(DE.T_RELATION_DESCRIPTOR, getLocalizedString("model.parent"));	
	if (!result.contains(parentD))
	    {
		result.add(parentD);
	    }

	DataElement descriptorD   = findDescriptor(DE.T_RELATION_DESCRIPTOR, getLocalizedString("model.descriptor_for"));	
	if (!result.contains(descriptorD))
	    {
		result.add(descriptorD);
	    }
*/

	return result;
    }

    /**
     * Finds all object descriptor types that can exist relative to a particular object type with a given relation.  
     *
     * @param descriptor the object descriptor to apply this to 
     * @param fixateOn a filter for the type of object descriptors to look for 
     * @param relation the relationship that a element can have with other elements 
     * @return a list of object descriptor elements 
     */     
    public ArrayList getFilterItems(DataElement descriptor, String fixateOn, DataElement relation)
    {
	ArrayList result = new ArrayList();
        if (descriptor != null)
        {
	    ArrayList filters = null;
	    if ((relation == null) || relation.getName().equals(getLocalizedString("model.contents")))
		{
		    filters = descriptor.getAssociated(getLocalizedString("model.contents"));
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

		    // abstracted filters
		    ArrayList baseDescriptors = descriptor.getAssociated(getLocalizedString("model.abstracted_by"));
		    for (int j = 0; j < baseDescriptors.size(); j++)
			{
			    DataElement baseDescriptor = (DataElement)baseDescriptors.get(j);
			    ArrayList baseFilters = getFilterItems(baseDescriptor, fixateOn, relation);
			    for (int k = 0; k < baseFilters.size(); k++)
				{
				    DataElement filter = (DataElement)baseFilters.get(k);
				    if (!result.contains(filter))
					{
					    result.add(filter);
					}
				}
			}
		    
		}
	    //   /*
	    if (result.size() < 1 && (relation != null))
		{
		    filters = descriptor.getAssociated(relation.getName());
		    for (int i = 0; i < filters.size(); i++)
			{
			    DataElement object = (DataElement)filters.get(i);		
			    if (object != null)
				{
				    String objType = object.getType();
				    if (objType.equals(DE.T_OBJECT_DESCRIPTOR) || objType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
					{
					    String objName = (String)object.getElementProperty(DE.P_NAME);
					    if (!result.contains(object))
						{
						    if (fixateOn != null)
							{
							    if (objName.equals(fixateOn))
								{
								    result.add(object);
								}
							}
						    else
							{
							    result.add(object);
							}
						}
					}
				}
			}
		}
	    //*/

        }
	return result;
      }


    /**
     * Find all elements from a given element that match a certain attribute.  
     *
     * @param root the element to search from 
     * @param attribute the index of the attribute to match 
     * @param pattern the value to compare with the attribute 
     * @param ignoreCase an indication whether to ignore case for the attribute or not 
     * @return the list of matches
     */     
    public ArrayList searchForPattern(DataElement root, int attribute, String pattern, boolean ignoreCase)
    {
	int    attributes[] = { attribute };
	String   patterns[] = { pattern };
	return searchForPattern(root, attributes, patterns, 1, ignoreCase);
    }
    
    /**
     * Find all elements from a given element that match a certain set of attributes.  
     *
     * @param root the element to search from 
     * @param attributes a list of attributes to match 
     * @param patterns a list of values to compare with the attributes 
     * @param ignoreCase an indication whether to ignore case for the attributes or not 
     * @return the list of matches
     */     
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
  
    /**
     * Find all elements from a given element that match a certain set of attributes.  
     *
     * @param root the element to search from 
     * @param attributes a list of attribute indexes to match 
     * @param patterns a list of values to compare with the attributes 
     * @param numAttributes the number of attributes to match 
     * @param ignoreCase an indication whether to ignore case for the attributes or not 
     * @return the list of matches
     */     
    public ArrayList searchForPattern(DataElement root, 
				      int attributes[], String patterns[], int numAttributes,
				      boolean ignoreCase)
    {
	return searchForPattern(root, attributes, patterns, numAttributes, ignoreCase, 10);
    }
    
    /**
     * Find all elements from a given element that match a certain set of attributes.  
     *
     * @param root the element to search from 
     * @param attributes a list of attribute indexes to match 
     * @param patterns a list of values to compare with the attributes 
     * @param numAttributes the number of attributes to match 
     * @param ignoreCase an indication whether to ignore case for the attributes or not 
     * @param depth how deep to search 
     * @return the list of matches
     */     
    public ArrayList searchForPattern(DataElement root, int attributes[], 
				      String patterns[], int numAttributes,
				      boolean ignoreCase, int depth)
    {
	ArrayList searched = new ArrayList();
	return searchForPattern(root, attributes, patterns, numAttributes, ignoreCase, depth, searched);
    }
    

    /**
     * Find all elements from a given element that match a certain set of attributes.  
     *
     * @param root the element to search from 
     * @param attributes a list of attribute indexes to match 
     * @param patterns a list of values to compare with the attributes 
     * @param numAttributes the number of attributes to match 
     * @param ignoreCase an indication whether to ignore case for the attributes or not 
     * @param depth how deep to search 
     * @param searched a list of objects already searched 
     * @return the list of matches
     */     
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
				
				ArrayList subResults = searchForPattern(child, attributes, 
									patterns, numAttributes, 
									ignoreCase, depth - 1, searched);
				for (int j = 0; j < subResults.size(); j++)
				    {
					result.add(subResults.get(j));
				    }	
			    }
		    }
	    }
	
	return result;
    }


    /**
     * Returns the element that represents the specified miner's data.  
     *
     * @param minerName the qualified name of the miner
     * @return the element representing the miner information
     */     
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
    
    /**
     * Finds a descriptor element with a specified type and name.  
     *
     * @param type the type of the descriptor
     * @param name the name of the descriptor
     * @return the found descriptor
     */     
    public DataElement findDescriptor(String type, String name)
    {
	if (_descriptorRoot != null)
	    {
		synchronized(_descriptorRoot)
		    {
			for (int i = 0; i < _descriptorRoot.getNestedSize(); i++)
			    {
				DataElement descriptor = _descriptorRoot.get(i);
				if (descriptor.getName().equals(name) &&
				    descriptor.getType().equals(type))
				    {
					return descriptor;
				    }
			    }
		    }
	    }
	
	return null;
    }
    
    /**
     * Finds an object descriptor element with a specified name.  
     *
     * @param name the name of the descriptor
     * @return the found descriptor
     */     
    public DataElement findObjectDescriptor(String name)
      {
        return find(_descriptorRoot, DE.A_NAME, name, 5);
      }

    /**
     * Finds an element with the specified ID.  
     *
     * @param id the ID of the descriptor
     * @return the found element
     */     
    public DataElement find(String id)
    {
        DataElement result = (DataElement)_hashMap.get(id);        
        return result;
    }

    /**
     * Finds an element matching a specified attribute and name.  
     *
     * @param root the element to search from
     * @param attribute the index of the attribute to compare
     * @param name the name of the element
     * @return the first found element
     */     
    public DataElement find(DataElement root, int attribute, String name)
    {
        return find(root, attribute, name, 10);
    }
    
    /**
     * Finds an element matching a specified attribute and name.  
     *
     * @param root the element to search from
     * @param attribute the index of the attribute to compare
     * @param name the name of the element
     * @param depth the depth of the search
     * @return the first found element
     */     
    public DataElement find(DataElement root, int attribute, String name, int depth)
      {
        if ((root != null) && (name != null) && !root.isReference() && depth > 0)
        {
          depth--;

          if (StringCompare.compare(name, root.getAttribute(attribute), false))
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
              
                if (name.compareTo(compareName) == 0)
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


    /**
     * Get the mapping from a remote path to a local path.  
     *
     * @param aPath the remote path
     * @return the local path
     */     
    public String mapToLocalPath(String aPath)
    {
	String result = null;
	
	char slash = '/';
	String remotePath = aPath.replace('\\', slash);
	String localRoot = _dataStoreAttributes.getAttribute(DataStoreAttributes.A_LOCAL_PATH).replace('\\', slash);
	String remoteRoot = getHostRoot().getSource().replace('\\', slash);
	
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
		result = new String(localRoot + slash + remotePath.substring(remoteRoot.length(), remotePath.length()));
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
		
		result = new String(localRoot + remotePath);
	    }
	
	return result;
    }
    
    
    /**
     * Persist the <code>DataStore</code> tree from a given root   
     *
     * @param root the element to persist from
     * @param remotePath the path where the persisted file should be saved
     * @param depth the depth of persistance from the root
     */         
    public void saveFile(DataElement root, String remotePath, int depth)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = mapToLocalPath(remotePath);
        try
        {
          // need to create directories as well
          File file = new File(fileName);
		  try
		  {	
		      file = file.getCanonicalFile();
		  }
		  catch (IOException e)
		  {
		  }
		  
          if (!file.exists())
          {
            File dir = new File(file.getParent());
            dir.mkdirs();
            file.createNewFile();
          }
	  
          File newFile = new File(file.getCanonicalPath());
          
          if (newFile.canWrite())
          {
   		  	FileOutputStream fileStream = new FileOutputStream(newFile);
	  	  	PrintStream fileWriter = new PrintStream(fileStream);
			BufferedWriter dataWriter = new BufferedWriter(new OutputStreamWriter(fileStream, "UTF-8"));

          XMLgenerator generator = new XMLgenerator(this);
          generator.setIgnoreDeleted(true);
          generator.setFileWriter(fileWriter);
          generator.setDataWriter(dataWriter);
          generator.setBufferSize(1000);
	  generator.generate(root, depth);
	  generator.flushData();
	  
          fileStream.close();
          }
        }
        catch (IOException e)
        {
          System.out.println(e);
        }
      }

  
    /**
     * Save a file in the specified location   
     *
     * @param localPath the path where to save the file
     * @param file the file to save
     */         
    public void saveFile(String localPath, File file)
    {
    	_byteStreamHandler.receiveBytes(localPath, file);
    }
	
    /**
     * Save a file in the specified location   
     *
     * @param remotePath the path where to save the file
     * @param buffer the buffer to save in the file
     */         
    public void saveFile(String remotePath, byte[] buffer, int size, boolean binary)
    {
    	_byteStreamHandler.receiveBytes(remotePath, buffer, size, binary);   
    }   

    /**
     * Append a file to the specified location   
     *
     * @param remotePath the path where to save the file
     * @param buffer the buffer to append into the file
     */         
    public void appendToFile(String remotePath, byte[] buffer, int size, boolean binary)
    {
    	_byteStreamHandler.receiveAppendedBytes(remotePath, buffer, size, binary);
    }   

    
    /**
     * Load a persisted <code>DataStore</code> tree into the specified <code>DataElement</code>   
     *
     * @param root the root element of the persisted tree 
     * @param pathName the location of the persisted file
     */         
    public void load(DataElement root, String pathName)
    {
        String fileName = pathName;
 
        FileInputStream inFile = loadFile(fileName);
        if (inFile != null)
        {        	        
         BufferedInputStream document = new BufferedInputStream(inFile);
           
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
   
    }
    
    
    public static FileInputStream loadFile(String fileName)
      {
        String document = null;
        File file = new File(fileName);
        if (file.exists() && (file.length() > 0))
        {
          try
          {
            FileInputStream inFile = new FileInputStream(file);            

            return inFile;
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


    /**
     * Indicate whether a given descriptor can contain the specified element   
     *
     * @param descriptor the object descriptor to test 
     * @param dataElement the object to test against
     * @return and indication whether dataElement can be in an object of type descriptor
     */         
    public boolean filter(DataElement descriptor, DataElement dataElement)
    {
	return filter(descriptor, dataElement, 2);
    }
  
    /**
     * Indicate whether a given descriptor can contain the specified element   
     *
     * @param descriptor the object descriptor to test 
     * @param dataElement the object to test against
     * @param depth how far to search
     * @return and indication whether dataElement can be in an object of type descriptor
     */         
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

    /**
     * Indicate whether a given set of descriptors can contain the specified element   
     *
     * @param descriptors the object descriptors to test 
     * @param dataElement the object to test against
     * @return and indication whether dataElement can be in an object of type descriptor
     */         
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
    
    /**
     * Indicate whether an command is specified as transient   
     *
     * @param commandObject the object descriptors to test 
     * @return and indication whether the command is transient
     */         
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
    
    private void initializeDescriptors()
    {
	_dataStoreSchema.extendSchema(_descriptorRoot);
    }

    private void initialize()
    {
	_minersLocation = "org.eclipse.cdt.dstore.core";
	_random = new Random(System.currentTimeMillis());
	

	_hashMap = new HashMap(2 * _initialSize);
	_recycled = new ArrayList(_initialSize);
	initElements(_initialSize);

	_timeout = 20000;
	try
	    {
		_resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.dstore.core.model.DataStoreResources");
	    }
	catch (MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }

	_dataStoreSchema = new DataStoreSchema(this);
	_traceFileHandle = new File(".dstoreTrace");
	_tracingOn = true;
	try
	    {
		_traceFile = new RandomAccessFile(_traceFileHandle, "rw");		
	    }
	catch (IOException e)
	    {
	    }

	_waitingStatuses = new ArrayList();

	startTracing();
	setByteStreamHandler();
    }
    
    public void setByteStreamHandler()
    {
    	setByteStreamHandler(null);
    }
    
    public void setByteStreamHandler(ByteStreamHandler handler)
    {
   		_byteStreamHandler = handler;	
   		if (_byteStreamHandler == null)
   		{
   			_byteStreamHandler = new ByteStreamHandler(this);	
   		}
    }
 

    /**
     * Preallocates a set of <code>DataElement</code>s.
     *
     * @param the number of elements to preallocate
     */
    private void initElements(int size)
    {
	for (int i = 0; i < size; i++)
	    {
		_recycled.add(new DataElement(this));
	    }
    }
  
    /**
     * Returns a new <code>DataElement</code> by either using an existing preallocated <code>DataElement</code> or
     * by creating a new one.
     *
     * @return the new <code>DataElement</code>
     */
    private DataElement createElement()  
    {
	DataElement newObject = null;
	int numRecycled = _recycled.size();

	if (numRecycled > 0)
	    {
		/*
		if (numRecycled > _MAX_FREE)
		    {
			int numRemoved = numRecycled - _MAX_FREE;
			for (int i = 1; i <= numRemoved; i++)
			    {
				DataElement toRemove = (DataElement)_recycled.remove(numRemoved - i);
				toRemove = null;
			    }
		    }
		*/

		newObject = (DataElement)_recycled.remove(0);
	    }
	else
	    {
		newObject = new DataElement(this);
	    }

	newObject.setUpdated(false);
	return newObject;
    }

    private void createRoots()
    {
	_tempRoot = createObject(_root, "temp", "Temp Root", "", "tempID");
	
	_logRoot     = createObject(_root, getLocalizedString("model.log"), 
				    getLocalizedString("model.Log_Root"), "", "logID");
	
	
	_minerRoot   = createObject(_root, getLocalizedString("model.miners"), 
				    getLocalizedString("model.Tool_Root"), "", "minersID");
	
	_hostRoot = createObject(_root,  getLocalizedString("model.host"),
				 _dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_NAME),
				 _dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_PATH), "hostID");
	
	_status = createObject(_root, getLocalizedString("model.status"), "okay", "", "statusID");
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
	//return name;
	return generateId();
    }
    
    /**
     * Generates a new unique ID to be used by a <code>DataElement</code>
     *
     * @return the new id  
     */
    protected String generateId()
    {
	String newId = String.valueOf(_random.nextInt());
	while (_hashMap.containsKey(newId))
	    {	    
	    	newId = String.valueOf(_random.nextInt());
	    }
	
	return newId;
    }	


    private ArrayList fuzzyResolveName(DataElement object, String pattern)
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


    private DataElement resolveName(DataElement object, String keyName)
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
    
    // this should be gone
    private String readerToString(BufferedReader in)
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

  private boolean sameTree(DataElement root1, DataElement root2, int depth)
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


    private void walkTree(DataElement root)
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

    public void startTracing()
    {
	try
	    {
		_traceFile.seek(_traceFileHandle.length());
	    }
	catch (IOException e)
	    {
	    }

	trace("-----------------------------------------");
	trace("Start Tracing at " + System.currentTimeMillis());
    }


    public void trace(String str)
    {
	internalTrace(str);
    }  

    public void trace(Exception e)
    {
	internalTrace(e.getMessage());
    }  

    private void internalTrace(String message)
    {
	if (_tracingOn && _traceFile != null && message != null)
	    {		
		try
		    {
			_traceFile.writeBytes(message);		
			_traceFile.writeBytes(System.getProperty("line.separator"));
		    }
		catch (IOException e)
		    {
		    }
	    }
    }

    public void finish()
    {
 	flush();
	if (_tracingOn)
	    {
		try
		    {
			_traceFile.writeBytes("Finished Tracing");
			_traceFile.writeBytes(System.getProperty("line.separator"));
			_traceFile.close();
		    }
		catch (IOException e)
		    {
		    }
	    }
    }

}

