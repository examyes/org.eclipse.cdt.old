package org.eclipse.cdt.dstore.core.miners.miner;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;

import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * Miner is the abstact base class of all DataStore extensions (miners).  
 * The DataStore framework knows how to load and route commands to miners
 * because it interfaces miners through the restricted set of interfaces declared here.
 * To add a new miner, developers must extend this class and implement the abstract methods declared here.
 *
 */
public abstract class Miner
{
    public DataStore   _dataStore;
    public DataElement _minerElement; 
    public DataElement _minerData;
    public DataElement _minerTransient;
    
    private boolean    _initialized;
    protected String   _name = null;
    protected String   _value = null;
    
    protected ResourceBundle _resourceBundle = null;  
    
    /**
     * Creates a new Miner
     *
     */
    protected Miner()
    {
	_initialized = false;
    }
    
    /**
     * Shuts down the miner and cleans up it's meta-information
     */
    public void finish()
    {
	DataElement root = _dataStore.getMinerRoot();
	
	_minerData.removeNestedData();
	_minerElement.removeNestedData();
	_dataStore.update(_minerElement);
	
	root.getNestedData().remove(_minerElement);
	root.setExpanded(false);
	root.setUpdated(false);
	
	_dataStore.update(root);
    }
    
    /**
     * Interface to retrieve an NL enabled resource bundle.  
     * Override this function to get access to a real resource bundle.
     */
    public ResourceBundle getResourceBundle()  
    {
	return null;
    }
    
    /**
     * Default method that gets called on a Miner when it is loaded.
     * Override this function to perform some initialization at miner loading time.
     */
    protected void load()
    {
    }

    /**
     * Default method that gets called on a Miner when it is loaded.
     * Override this function to perform some initialization at miner loading time.
     * If loading the miner can result in some failure, set that status to incomplete
     *
     * @param status the status of the initialize miner command
     */
    protected void load(DataElement status)
    {
	load();
    }
    
    /**
     * This gets called after a miner is initialized.  
     * If you need to update element information at that time, override this method.
     *
     */
    protected void updateMinerInfo()
    {
    }

    /**
     * Returns the qualified name of this miner
     *
     * @return the qualified name of this miner
     */
    public String getName()
    {
        if (_name == null)
	    _name = getClass().getName();
        return _name;
    }
    
    /**
     * Returns the name of this miner
     *
     * @return the name of this miner
     */
    public String getValue()
    {
        if (_value == null)
	    {
		String name = getName();
		int indexOfValue = name.lastIndexOf(".");
		_value = name.substring(indexOfValue + 1, name.length());
	    }
        return _value;
    }
    
    /**
     * Issues a specified command on this miner from the DataStore framework.
     * The base class handles "C_INIT_MINERS" but other commands are delegated to
     * the concrete miner implementations through handleCommand()
     *
     * @param command the command that has been sent to this miner
     * @return the status of the command
     */
    public DataElement command(DataElement command)
    {
	String      name   = getCommandName(command);
	DataElement status = getCommandStatus(command);
	long startTime = System.currentTimeMillis();
	
	if (status == null)
	    {
		System.out.println("bad command " + name);
		return null;
	    }
	    
	if (status.getAttribute(DE.A_NAME).equals("start"))
	{    
		status.setAttribute(DE.A_NAME, getLocalizedString("model.working"));
	}

	if (name.equals("C_INIT_MINERS"))
	    {
		//System.out.println("loading " + this + "...");
		if (!_initialized)
		    {
			load(status);
			_initialized = true;
		    }
		updateMinerInfo();
		//System.out.println("...loading " + this);

		DataElement minerRoot = _dataStore.getMinerRoot();
		_dataStore.refresh(minerRoot, true);

		if (status.getAttribute(DE.A_NAME).equals(getLocalizedString("model.incomplete")))
		    {
			_dataStore.refresh(status);
		    }
		else
		    {
			status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
		    }
	    }    
	else
	    {	
		try
		    {
			status = handleCommand(command);
		    }
		catch(Exception e)
		    {
			_dataStore.trace(e);

			status.setAttribute(DE.A_NAME, "Failed with Exception");
			status.setAttribute(DE.A_VALUE, getLocalizedString("model.done"));
			_dataStore.refresh(status);
			
			String exc = null;
			if (e.getMessage() != null)
			    exc = e.getMessage();
			else
			    exc = "Exception";
			DataElement exception = _dataStore.createObject(status, getLocalizedString("model.error"), exc);
		    }
	    }
	
	if (_dataStore.logTimes())
	    {
		long endTime = System.currentTimeMillis(); 
		
		DataElement time    = getCommandTime(command);
		DataElement endMinerTime = _dataStore.createObject(time, getLocalizedString("model.property"), "Time in " + _value);      
		endMinerTime.setAttribute(DE.A_VALUE, new String(endTime - startTime + "ms"));
	    }
	return status;
    }
    
    
    /**
     * Sets the DataStore and performs some fundamental initialization for this miner.  
     * The framework calls this method on a miner before any commands are issued.
     *
     * @param dataStore the DataStore that owns this miner
     */
    public void setDataStore(DataStore dataStore)
    {
	_dataStore = dataStore;
	
	DataElement root = _dataStore.getMinerRoot();
	String name = getName();
	String value = getValue();
	
	_resourceBundle = getResourceBundle();

	_minerElement   = _dataStore.createObject(root, getLocalizedString("model.miner"), name, name);
	_minerElement.setAttribute(DE.A_VALUE, value);
	
	_minerData      = _dataStore.createObject(_minerElement, getLocalizedString("model.data"), getLocalizedString("model.Data"), name);
	_minerTransient = _dataStore.createObject(_minerElement, getLocalizedString("model.transient"), getLocalizedString("model.Transient_Objects"), name);

	
	// extend schema
	DataElement schemaRoot = _dataStore.getDescriptorRoot();
	//System.out.println("extend schema " + name + "...");
	extendSchema(schemaRoot);
	//System.out.println("...extend schema " + name);

	_dataStore.refresh(root, true);	
	_dataStore.refresh(_minerElement);	
    }
    
    
    /**
     * Creates an abstract command descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the command
     * @param value the identifier for this command
     * @return the new command descriptor
     */
    public DataElement createAbstractCommandDescriptor(DataElement descriptor, String name, String value) 
    {
	return _dataStore.createAbstractCommandDescriptor(descriptor, name, getName(), value);
    }
    
    /**
     * Creates a command descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the command
     * @param value the identifier for this command
     * @return the new command descriptor
     */
    public DataElement createCommandDescriptor(DataElement descriptor, String name, String value)
    {
	return createCommandDescriptor(descriptor, name, value, true);
    }
    
    /**
     * Creates a command descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the command
     * @param value the identifier for this command
     * @param visible an indication whether this command descriptor should be visible to an end-user
     * @return the new command descriptor
     */
    public DataElement createCommandDescriptor(DataElement descriptor, String name, String value, boolean visible)
    {
        DataElement cmdD = _dataStore.createCommandDescriptor(descriptor, name, getName(), value);
	if (!visible)
	    {
		cmdD.setDepth(0);
	    }
	
        return cmdD;
    }
    
    /**
     * Creates an abstract object descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the object type
     * @return the new object descriptor
     */
    public DataElement createAbstractObjectDescriptor(DataElement descriptor, String name)
    {
	return _dataStore.createAbstractObjectDescriptor(descriptor, name);
    }

    /**
     * Creates an abstract object descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the object type
     * @param source the plugin location of the miner that owns this object type
     * @return the new object descriptor
     */
    public DataElement createAbstractObjectDescriptor(DataElement descriptor, String name, String source)
    {
	return _dataStore.createAbstractObjectDescriptor(descriptor, name, source);
    }
    
    /**
     * Creates a object descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the object type
     * @return the new object descriptor
     */
    public DataElement createObjectDescriptor(DataElement descriptor, String name)
    {
	return _dataStore.createObjectDescriptor(descriptor, name);
    }

    /**
     * Creates a object descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the object type
     * @param source the plugin location of the miner that owns this object type
     * @return the new object descriptor
     */
    public DataElement createObjectDescriptor(DataElement descriptor, String name, String source)
    {
	return _dataStore.createObjectDescriptor(descriptor, name, source);
    }

    /**
     * Creates a new type of relationship descriptor.  This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain
     *
     * @param descriptor the parent descriptor for the new descriptor
     * @param name the name of the relationship type
     * @return the new relationship descriptor
     */
    public DataElement createRelationDescriptor(DataElement descriptor, String name)
    {
     return _dataStore.createRelationDescriptor(descriptor, name);
    }
 

    /**
     * Creates an abstract relationship between two descriptors.  An abstract relationship between two descriptors
     * indicates that the first descriptor abstracts the second, while the second inherits the
     * properties of the first. This is a helper method that miner may call
     * when it creates or updates the schema for it's tool domain.
     *
     * @param from the abstacting descriptor 
     * @param to the descriptor that is abstracted
     * @return the new relationship descriptor
     */
    public DataElement createAbstractRelationship(DataElement from, DataElement to)
    {
	return _dataStore.createReference(from, to, "abstracts", "abstracted by");
    }
     
    /**
     * Creates a contents relationship between any two elements. 
     *
     * @param from the containing element 
     * @param to the element that is contained
     * @return the new relationship
     */
    public DataElement createReference(DataElement from, DataElement to)
    {
     return _dataStore.createReference(from, to);
    }
    
    /**
     * Helper method for finding an object descriptor in the DataStore schema. 
     *
     * @param descName the name of the descriptor 
     * @return the found object descriptor
     */
    public DataElement findDescriptor(String descName)
    {
	return _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, descName, 1);
    }

    /**
     * Returns the element that represents this miner. 
     *
     * @return the miner element
     */
    public DataElement getMinerElement()
    {
        return _minerElement;
    }
    
    /**
     * Returns the element that contains this miners meta-information. 
     *
     * @return the miner data element
     */
    public DataElement getMinerData()
    {
	return _minerData;
    }
    
    /**
     * Returns the transient object container for this element. 
     *
     * @return the transient element
     */
    public DataElement getMinerTransient()
    {
        return _minerTransient;
    }
    
    /**
     * Identifies a give object descriptor type to be transient in this miner. 
     *
     * @param objectDescriptor the object descriptor type that is transient
     */
    public void makeTransient(DataElement objectDescriptor)
    {
        _dataStore.createReference(_minerTransient, objectDescriptor);
    }
    
    /**
     * Returns the name of a command. 
     * This is a helper method to be used inside handleCommand().
     *
     * @param command a tree of elements representing a command
     * @return the name of the command
     */
    public String getCommandName(DataElement command)
    {
	return (String)command.getAttribute(DE.A_NAME);
    }
    
    /**
     * Returns the status of a command. 
     * This is a helper method to be used inside handleCommand().
     *
     * @param command a tree of elements representing a command
     * @return the status element for the command
     */
    public DataElement getCommandStatus(DataElement command)
    {
	return _dataStore.find(command, DE.A_TYPE, getLocalizedString("model.status"), 1);
    }
    
    /**
     * Returns the status of a command. 
     * This is a helper method to be used inside handleCommand().
     *
     * @param command a tree of elements representing a command
     * @return the element representing time for a command
     */
    public DataElement getCommandTime(DataElement command)
    {
	return _dataStore.find(getCommandStatus(command), DE.A_TYPE, getLocalizedString("model.time"), 1);
    }
    
    /**
     * Returns the number of arguments for this command. 
     * This is a helper method to be used inside handleCommand().
     *
     * @param command a tree of elements representing a command
     * @return the number of arguments for this command
     */
    public int getNumberOfCommandArguments(DataElement command)
    {
	return command.getNestedSize();
    }
    
    /**
     * Returns the argument of a command specified at a given index. 
     * This is a helper method to be used inside handleCommand().
     *
     * @param command a tree of elements representing a command
     * @param arg the index into the commands children
     * @return the argument of the command
     */
    public DataElement getCommandArgument(DataElement command, int arg)
    {
	if (command.getNestedSize() > 0)
	    {
		DataElement argument = command.get(arg);
		if (argument != null)
		    {
			return argument.dereference();
		    }
	    }

	return null;
    }
    
    
    /**
     * If a resource bundle is supplied (ie. getResourceBundle() is overridden),
     * then this is a convenience metho for getting at an NL enabled string
     *
     * @param key the key identifying the string
     * @return the NL enabled string
     */
    public String getLocalizedString(String key)
    {
	try
	    {
		if (_resourceBundle != null && key != null)
		    {
			return _resourceBundle.getString(key);
		    }
	    }
	catch (MissingResourceException mre)
	    {
	    }

	// attempt getting resource from datastore
	return _dataStore.getLocalizedString(key);
    }

    /**
     * Returns the descriptor root for the DataStore schema 
     *
     * @return the descriptor root
     */
    public DataElement getSchemaRoot()
    {
	return _dataStore.getDescriptorRoot();
    }
 
    /**
     * Add this tool's schema to the global DataStore schema.
     * This interface must be implemented by each miner in order to
     * populate the DataStore schema with information about this tool's
     * object model and information about how to communicate with the
     * tool from objects available to the user interface.
     *
     * @param schemaRoot the descriptor root
     */
    public abstract void extendSchema(DataElement schemaRoot);

    /**
     * Handle commands that are routed to this miner.
     * This interface must be implemented by each miner in order to
     * perform tool actions driven from user interface interaction.
     *
     * @param theCommand an instance of a command containing a tree of arguments
     */
    public abstract DataElement handleCommand (DataElement theCommand);
}

