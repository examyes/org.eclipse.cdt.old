package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.util.*;

import java.io.*;
import java.util.*;

public final class DataElement implements Serializable, IDataElement
{
    private String              _attributes[];
    private StringBuffer        _buffer;
    
 private boolean             _isReference = false;
 private boolean             _isDescriptor = false;
 private boolean             _isExpanded   = false;
 private boolean             _isUpdated    = false;
 private int                 _depth        = 1;
    
    private DataStore           _dataStore = null;
    private DataElement         _parent = null;
    private DataElement         _descriptor = null;
    
    private ArrayList           _nestedData = null;
    private DataElement         _referencedObject = null;
    
    private DesktopElement      _desktopElement = null;
    private PropertySource      _propertySource = null;
  
  /////////////////////////////////////////
  //
  // Constructors
  //
  /////////////////////////////////////////

    // default constructor
    public DataElement()
    {

	_dataStore   = null;
	_parent      = null;

    }

  /////////////////////////////////////////
  //
  // initialization and settings
  //
  /////////////////////////////////////////

  public void reInit(DataElement parent, DataElement originalObject, DataElement refType)
      {
	if ((parent != null) && (originalObject != null))
	    {
		_dataStore   = originalObject.getDataStore();
		_parent = parent;

		_attributes                  = new String[DE.A_SIZE];
		_attributes[DE.A_TYPE]       = refType.getName();		
		_attributes[DE.A_ID]         = _dataStore.generateId();
		_attributes[DE.A_NAME]       = originalObject.getId();
		_attributes[DE.A_VALUE]      = originalObject.getId();
		_attributes[DE.A_SOURCE]     = originalObject.getSource();
		_attributes[DE.A_ISREF]      = "true";
		_attributes[DE.A_DEPTH]      = originalObject.getAttribute(DE.A_DEPTH);
		
		
		_referencedObject = originalObject;
		_isReference      = true;
		
		initialize(refType);	
	    }
      }

  public void reInit(DataElement parent, DataElement originalObject, String refType)
      {
	if ((parent != null) && (originalObject != null))
	    {

		_dataStore   = originalObject.getDataStore();
		_parent = parent;

		_attributes                  = new String[DE.A_SIZE];
		_attributes[DE.A_TYPE]       = refType;		
		_attributes[DE.A_ID]         = _dataStore.generateId();

		_attributes[DE.A_NAME]       = originalObject.getId();
		_attributes[DE.A_VALUE]      = originalObject.getId();
		_attributes[DE.A_SOURCE]     = originalObject.getSource();
		_attributes[DE.A_ISREF]      = "true";
		_attributes[DE.A_DEPTH]      = originalObject.getAttribute(DE.A_DEPTH);
		
		
		_referencedObject = originalObject;
		_isReference      = true;
		
		initialize();	
	    }
      }

  public void reInit(DataStore dataStore, DataElement parent, DataElement type, String id, String name, String source)
	{
	  _dataStore   = dataStore;
	  _parent      = parent;

	  _attributes = new String[DE.A_SIZE];
	  _attributes[DE.A_TYPE]       = type.getAttribute(DE.A_NAME);
          _attributes[DE.A_ID]         = id;
          _attributes[DE.A_NAME]       = name;
          _attributes[DE.A_VALUE]      = name;
	  _attributes[DE.A_SOURCE]     = source; 
	  _attributes[DE.A_ISREF]      = "false";
	  _attributes[DE.A_DEPTH]      = "2";

	  initialize(type);
	}

  public void reInit(DataStore dataStore, DataElement parent, String type, String id, String name, String source)
	{
	  _dataStore   = dataStore;
	  _parent      = parent;

	  _attributes = new String[DE.A_SIZE];
	  _attributes[DE.A_TYPE]       = type;
          _attributes[DE.A_ID]         = id;
          _attributes[DE.A_NAME]       = name;
          _attributes[DE.A_VALUE]      = name;
	  _attributes[DE.A_SOURCE]     = source; 
	  _attributes[DE.A_ISREF]      = "false";
	  _attributes[DE.A_DEPTH]      = "2";

	  initialize();
	}

  public void reInit(DataStore dataStore, DataElement parent, DataElement type, String id, String name, String source, String isRef)
	{
          _dataStore = dataStore;
          _parent = parent;

	  _attributes = new String[DE.A_SIZE];
	  _attributes[DE.A_TYPE]       = type.getAttribute(DE.A_NAME);
          _attributes[DE.A_ID]         = id;
          _attributes[DE.A_NAME]       = name;
          _attributes[DE.A_VALUE]      = name;
	  _attributes[DE.A_SOURCE]     = source;
	  _attributes[DE.A_ISREF]      = isRef;
	  _attributes[DE.A_DEPTH]      = "2";

	  initialize(type);
	}

  public void reInit(DataStore dataStore, DataElement parent, String type, String id, String name, String source, String isRef)
	{

          _dataStore = dataStore;
          _parent = parent;

	  _attributes = new String[DE.A_SIZE];
	  _attributes[DE.A_TYPE]       = type;
          _attributes[DE.A_ID]         = id;
          _attributes[DE.A_NAME]       = name;
          _attributes[DE.A_VALUE]      = name;
	  _attributes[DE.A_SOURCE]     = source;
	  _attributes[DE.A_ISREF]      = isRef;
	  _attributes[DE.A_DEPTH]      = "2";

	  initialize();
	}

  public void reInit(DataStore dataStore, DataElement parent, DataElement type, String[] attributes)
      {
        _dataStore   = dataStore;
        _parent      = parent;
	
	_attributes = attributes;
	_attributes[DE.A_TYPE] = type.getName();

	initialize(type);
      }

  public void reInit(DataStore dataStore, DataElement parent, String[] attributes)
      {
        _dataStore   = dataStore;
        _parent      = parent;

	_attributes = attributes;

	initialize();
      }



  private void initialize()
    {
	initialize(null);
    }

  private void initialize(DataElement typeDescriptor)
      {
	  _isReference  = false;
	  _isDescriptor = false;
	  _depth        = 1;
	  _referencedObject = null;
	  _isExpanded  = false;
	  _isUpdated   = false;
	  _descriptor  = typeDescriptor;

	  String depth = getAttribute(DE.A_DEPTH);
	  if (depth != null)
	      {	  
		  _depth = Integer.parseInt(depth);	    
	      } 
	  
	  String type  = getAttribute(DE.A_TYPE);
	  String isRef = getAttribute(DE.A_ISREF);
	  if (isRef != null && isRef.equals("true"))
	      {
		  _isReference = true;	
	      }
	  
	  if (type.equals(DE.T_OBJECT_DESCRIPTOR)          ||
	      type.equals(DE.T_COMMAND_DESCRIPTOR)         ||
	      type.equals(DE.T_RELATION_DESCRIPTOR)        ||
	      type.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR) ||
	      type.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR) ||
	      type.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR)
	      )
	      {
		  _isDescriptor = true; 
	      }
	  
	  if (_nestedData != null)
	      _nestedData.clear();
      }

  public synchronized void delete()
      {
	  // set delete attribute
	  setAttribute(DE.A_SOURCE, "deleted");
	  setAttribute(DE.A_VALUE, "deleted");
	  _isUpdated = false;	 
	  _isExpanded = true;
      }

    public boolean isDeleted()
    {    
	String depthAttribute = getAttribute(DE.A_DEPTH);
	String valueAttribute = getAttribute(DE.A_VALUE);
	
	if (_depth == -1 || depthAttribute.equals("-1"))
	    {
		return true;
	    }
	else if (valueAttribute.equals("deleted"))
	    {
		_depth = -1;	  
		setAttribute(DE.A_DEPTH, "-1");
		return true;
	    }
	
	return false;	
      }

  public void addNestedData(ArrayList nestedData, boolean checkUnique)
      {
	  if (nestedData != null)
	      {
		  if (_nestedData == null)
		      {
			  _nestedData = new ArrayList(nestedData.size());
		      }
		  
		  for (int i = 0; i < nestedData.size(); i++)
		      {
			  DataElement child = (DataElement)nestedData.get(i);
			  if (child != null && child != this)
			      {
				  addNestedData(child, checkUnique);
			      }
		      }
	      }
      }

  public void addNestedData(DataElement obj, boolean checkUnique)
      {
	  if (_nestedData == null)
	      {
		  _nestedData = new ArrayList(4);	    
	      }

	  synchronized(_nestedData)
	      {	  
		  boolean alreadyThere = false;
		  if (checkUnique)
		      {
			  alreadyThere = _nestedData.contains(obj);
		      }

		  if (!checkUnique || !alreadyThere)
		      {
			  if (alreadyThere)
			      {
				  return;
			      }
			  else
			      {
				  _nestedData.add(obj);
				  
				  if (obj.getParent() == null)
				      obj.setParent(this);
				  _isExpanded = true;
			      }
		      }
		  
		  _isUpdated = false;
		  obj.setUpdated(false);
	      }	  
      }
    
  public synchronized void removeNestedData(DataElement object)
      {
        _nestedData.remove(object);
        _isExpanded = false;
        _isUpdated = false;
      }

  public synchronized void removeNestedData()
      {
	if (_nestedData != null)
	  {
	      while (_nestedData.size() > 0)
		  {
		      DataElement nestedObject = (DataElement)_nestedData.get(0);
		      _nestedData.remove(nestedObject);
		  }
	  }

	_isExpanded = false;
	_isUpdated = false;
	
      }


  /////////////////////////////////////////
  //
  // gets
  //
  /////////////////////////////////////////

  public String getAttribute(int attributeIndex)
      {
	  return _attributes[attributeIndex];
      }

  public String[] getAttributes()
      {
        return _attributes;
      }

  public String getType()
      {
        return getAttribute(DE.A_TYPE);
      }

  public String getId()
      {
        return getAttribute(DE.A_ID);
      }

  public String getName()
      {
        return getAttribute(DE.A_NAME);
      }

  public String getValue()
      {
        return getAttribute(DE.A_VALUE);
      }

  public String getSource()
      {
        return getAttribute(DE.A_SOURCE);
      }

  public StringBuffer getBuffer()
      {
        if (_buffer == null)
          _buffer = new StringBuffer();

        return _buffer;
      }

  public DataStore getDataStore()
      {
        return _dataStore;
      }

    public void initializeNestedData(int size)
    {
	if (_nestedData == null)
	    {
		_nestedData = new ArrayList(size);
	    }
    }

  public ArrayList getNestedData()
      {
	if (_nestedData == null)
	  {
	    _nestedData = new ArrayList();	    
	  }
        return _nestedData;
      }

  public DataElement get(int index)
  {
    if (_nestedData == null)
      {
	return null;	
      }
    else
      {
        if (getNestedSize() > index)
        {
          Object obj = _nestedData.get(index);
          return (DataElement)obj;
        }
        else
        {
          return null;
        }
      } 
  }

  public int getNestedSize()
  {
    if (_nestedData == null)
      {
	return 0;	
      }
    else
      {	
	return _nestedData.size();
      }
  }  
  
  public DataElement getParent()
      {
        return _parent;
      }

  public DataElement getDescriptor()
      {
	  if (isDeleted())
	      {
		  return null;
	      }

        if (_isReference)
	  {
	    if (_referencedObject == null)
	      {
		_referencedObject = dereference();		
	      }
	    if (this == _referencedObject || _referencedObject.isDeleted())
		{
		    _referencedObject = null;
		}
	    else
		{
		    return _referencedObject.getDescriptor();
		}
	  }
        else if ((_descriptor == null) && (_dataStore != null))
	    {
		_descriptor = _dataStore.findDescriptor(DE.T_OBJECT_DESCRIPTOR, getAttribute(DE.A_TYPE));	   
		if (_descriptor == null)
		    {
			_descriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, getAttribute(DE.A_TYPE), 3);	   
		    }
	    }

        return _descriptor;
      }

  public DataElement getCommandFor(String value)
      {
        DataElement descriptor = getDescriptor();
        for (int i = 0; i < descriptor.getNestedSize(); i++)
        {
          DataElement subDescriptor = (DataElement)descriptor.get(i);
          if (subDescriptor.getType().equals(DE.T_COMMAND_DESCRIPTOR))
          {
            if (subDescriptor.getValue().equals(value))
            {
              return subDescriptor;
            }
          }
        }

        return null;
	}

  public int depth()
      {
        return _depth;
      }

  public boolean isReference()
  {
    return _isReference;
  }

  public boolean isExpanded()
      {
        return _isExpanded;
      }

  public boolean isUpdated()
      {
        return _isUpdated;
      }


  /////////////////////////////////////////
  //
  // sets
  //
  /////////////////////////////////////////

  public void setAttribute(int attributeIndex, String attribute)
      {
        if (attribute != null)
        {
          if ((attributeIndex == DE.A_NAME) && (getAttribute(DE.A_NAME).equals(getAttribute(DE.A_VALUE))))
          {
            _attributes[DE.A_VALUE] = attribute;

          }

          _attributes[attributeIndex] = attribute;
	  _isUpdated = false;
        }
      }

  public void setAttributes(String attributes[])
      {
	for (int i = 0; i < DE.A_SIZE; i++)
	  {	
	    if (attributes[i] != null)
	      {		
		_attributes[i] = attributes[i];
	      }	
	  }

	_isUpdated = false;
      }

  public void setBuffer(StringBuffer buffer)
      {
        _buffer = buffer;
	if (_depth < 2 && buffer.length() > 0)
	    {
		setDepth(2);
	    }

	_isUpdated = false;
      }

  public void appendToBuffer(String text)
      {
        if (_buffer == null)
        {
          _buffer = new StringBuffer();
        }
        _buffer.append(text);
	_isUpdated = false;
      }

  public void setExpanded(boolean flag)
	{
	  _isExpanded = flag;
	}
  public void setUpdated(boolean flag)
      {
        _isUpdated = flag;
      }

  public void setParent(DataElement parent)
      {
        _parent = parent;
      }
  
  public void setDataStore(DataStore dataStore)
      {
        _dataStore = dataStore;
      }

  public void setDepth(int depth)
      {
        _depth = depth;
	setAttribute(DE.A_DEPTH, String.valueOf(depth));
      }


  /////////////////////////////////////////
  //
  // comparisons and searching
  //
  /////////////////////////////////////////

    public boolean isOfType(String typeStr)
    {
	DataElement typeDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, typeStr, 1);
	return isOfType(typeDescriptor);
    }

    public boolean isOfType(DataElement type)
    {
	return isOfType(type, false);
    }

    public boolean isOfType(DataElement type, boolean isDescriptor)
    {
	boolean result = false;

	DataElement descriptor = this;
	if (!isDescriptor)
	    {
		descriptor = getDescriptor();
	    }

	if (descriptor != null && !descriptor.isDeleted())
	    {
		String typeType = type.getType();
		if (typeType.equals(DE.T_OBJECT_DESCRIPTOR))
		    {
			if ((descriptor == type) || (type.getName().equals(_dataStore.getLocalizedString("model.all"))))
			    {
				result = true;
			    }
		    }
		else if (typeType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
		    {
			ArrayList abstracted = type.getAssociated(_dataStore.getLocalizedString("model.abstracts"));
			for (int i = 0; (i < abstracted.size()) && !result; i++)
			    {
				DataElement subDescriptor = (DataElement)abstracted.get(i);
				result = isOfType(subDescriptor, true);
			    }
		    }
	    }

	return result;
    }

  public boolean patternMatch(int attributes[], String patterns[], int numAttributes, boolean ignoreCase)
      {
        int index = 0;
        while (index < numAttributes)
        {
	  String attribute = getAttribute(attributes[index]);
	  String pattern   = patterns[index];
	  
          if (!StringCompare.compare(pattern, attribute, ignoreCase))
          {
            return false;
          }
          index++;
        }

        return true;
      }


    public boolean contains(DataElement object, DataElement property)
    {
	return contains(object, property, 1);
    }

    public boolean contains(DataElement object, DataElement property, int depth)
    {
	if (depth > 0)
	    {
		depth--;

		if (property == null)
		    {
			property = _dataStore.findObjectDescriptor(_dataStore.getLocalizedString("model.contents"));
		    }
		
		if (getNestedSize() == 0)
		    {
			return false;
		    }
		
		if (property != null)
		    {
			if (property.getType().equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
			    {
				for (int i = 0; i < property.getNestedSize(); i++)
				    {
					DataElement subProperty = property.get(i).dereference();
					if (contains(object, subProperty))
					    {
						return true;
					    }
				    }
			    }
			else
			    {
				String type = property.getName();
				if (type.equals(_dataStore.getLocalizedString("model.parent")) && (_parent != null))	
				    {
					if (object == _parent)
					    {
						return true;
					    }
					else
					    {
						return _parent.contains(object, property, depth);
					    }
				    }
				else if (_nestedData != null)
				    {	
					for (int i = 0; i < _nestedData.size(); i++)
					    {
						DataElement nestedObject = (DataElement)_nestedData.get(i);
						if (nestedObject != null)
						    {
							if (nestedObject.isReference())
							    {
								String relType = nestedObject.getAttribute(DE.A_TYPE);
								if (relType.equals(type))
								    {
									DataElement referenced = nestedObject.dereference();
									
									if (referenced == object)
									    {
										return true;
									    }
									else
									    {
										return referenced.contains(object, property, depth);
									    }
								    }
							    }
							else if (type.equals(_dataStore.getLocalizedString("model.contents")))
							    {
								if (nestedObject == object)
								    {
									return true;
								    }
								else
								    {
									return nestedObject.contains(object, property, depth);
								    }
							    }
						    }
					    }
				    }
			    }
		    }
	    }
	return false;
    }
    
    
    public boolean contains(DataElement object)
    {
	return contains(object, 1);
    }
  
    
    public boolean contains(DataElement object, int depth)
      {
	  boolean result = false;
	  if (_nestedData != null)
	      {
		  depth--;
		  for (int i = 0; i < getNestedSize(); i++)
		      {
			  DataElement child = get(i);

			  if (child == object)
			      {
				  return true;
			      }
			  else if (child.dereference() == object)
			      {
				  return true; 
			      }
			  else if (depth > 0)
			      {
				  result = child.contains(object, depth);
				  if (result)
				      {
					  return true;
				      }
			      }
		  }
	  }
	
	  return result;
      }

  public boolean equals(Object arg)
      {
	if (arg instanceof DataElement)
	  {	
	      return arg == this;
	  }

	return false;
      }

  public ArrayList getAssociated(String propertyStr)
    {
	DataElement property = _dataStore.findObjectDescriptor(propertyStr);
	return getAssociated(property);
    }


  public ArrayList getAssociated(DataElement property)
      {	  
	  if (getNestedSize() == 0)
	      {
		  return new ArrayList(1);
	      }

	  ArrayList set = new ArrayList(getNestedSize());

	if (property == null)
	    {
		property = _dataStore.findObjectDescriptor(_dataStore.getLocalizedString("model.contents"));
	    }
	if (property != null)
	    {
		if (property.getType().equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
		    {
			for (int i = 0; i < property.getNestedSize(); i++)
			    {
				DataElement subProperty = property.get(i).dereference();
				ArrayList subSet = getAssociated(subProperty);
				for (int j = 0; j < subSet.size(); j++)
				    {
					DataElement item = (DataElement)subSet.get(j);
					if (!item.isDeleted())
					    set.add(item);
				    }
			    }
		    }
		else
		    {
			String type = property.getName();
			if (type.equals(_dataStore.getLocalizedString("model.parent")) && (_parent != null))	
			    {
				set.add(_parent);
			    }
			else if (_nestedData != null)
			    {	
				for (int i = 0; i < _nestedData.size(); i++)
				    {
					DataElement nestedObject = (DataElement)_nestedData.get(i);
					if (nestedObject != null)
					    {
						if (nestedObject.isReference())
						    {
							String relType = nestedObject.getAttribute(DE.A_TYPE);
							if (relType.equals(type))
							    {
								DataElement referenced = nestedObject.dereference();
								if ((referenced != null) && !referenced.isDeleted())
								    {
									set.add(referenced);
								    }
							    }
						    }
						else if (type.equals(_dataStore.getLocalizedString("model.contents")))
						    {
							if (!nestedObject.isDeleted())
							    set.add(nestedObject);
						    }
					    }
				    }
			    }
		    }
	    }
		
	return set;
      }


  /////////////////////////////////////////
  //
  // query operations
  //
  /////////////////////////////////////////

  public DataElement dereference()
  {
    if (_isReference)
      {
	 
	  if ((_referencedObject != null))
	  {
	    return _referencedObject;	
	  }
	else
	  {            
	      String name = getAttribute(DE.A_NAME);
	      _referencedObject = _dataStore.find(name);
	      if ((_referencedObject != null))
		  {
		      return _referencedObject;
		  }
	      else
		  {
		      return this;
		  }
	  }
      }
    else
	{
	    return this;	
	}
  }

    public DataElement doCommandOn(String command, boolean isSynchronized)
    {
	DataElement status = null;
	if ((_dataStore != null) &&
	    (_dataStore.isConnected()))
	    {
		DataElement cmdDescriptor = _dataStore.localDescriptorQuery(getDescriptor(), command);
		if (cmdDescriptor != null)
		    {	
			if (isSynchronized)
			    {
				status = _dataStore.synchronizedCommand(cmdDescriptor, this);
			    }
			else
			    {
				status = _dataStore.command(cmdDescriptor, this);
			    }
		    }
	    }
	return status;
    }


  public DataElement refresh(boolean isSynchronized)
  {    
      DataElement status = null;
      if ((_dataStore != null) && (_dataStore.isConnected()) && !isDeleted())
      {
	  DataElement queryDescriptor = _dataStore.localDescriptorQuery(getDescriptor(), "C_REFRESH");
	  if (queryDescriptor != null)
	      {	
		  if (isSynchronized)
		      {
			  status = _dataStore.synchronizedCommand(queryDescriptor, this);
		      }
		  else
		      {
			  status = _dataStore.command(queryDescriptor, this);
		      }
		  _isExpanded = true; 
		  _isUpdated = false;
	      }
      }
      return status;
  }

  public void expandChildren()
  {    
      if ((_dataStore != null) && (_dataStore.isConnected() && !isDeleted()))
      {
        DataElement queryDescriptor = _dataStore.localDescriptorQuery(getDescriptor(), "C_QUERY");
        if (queryDescriptor != null)
        {	
          _dataStore.command(queryDescriptor, this);
          _isExpanded = true;
          _isUpdated = false;
        }
      }
  }

    public IDataElement expandChildren(boolean isSynchronized)
    {
	DataElement status = null;
	if ((_dataStore != null) && (_dataStore.isConnected()) && !isDeleted())
	    {
		DataElement queryDescriptor = _dataStore.localDescriptorQuery(getDescriptor(), "C_QUERY");
		if (queryDescriptor != null)
		    {	
			if (isSynchronized)
			    {
				status = _dataStore.synchronizedCommand(queryDescriptor, this);		  
			    }
			else
			    {		  
				status = _dataStore.command(queryDescriptor, this);
			    }
			
			_isExpanded = true;
			_isUpdated = false;
		    }
	    }
	return status;
    }

    public File getFileObject()
    {
	return getFileObject(true);
    }

    public File getFileObject(boolean doSynchronize)
      {
        String source = new String(getAttribute(DE.A_SOURCE));
        if (source.length() > 0)
        {
          int locationIndex = source.lastIndexOf(":");
          if (locationIndex > 1)
          {
            source = source.substring(0, locationIndex);
          }
          
	  String type = getType();
          if (!type.equals(_dataStore.getLocalizedString("model.directory")) &&
	      (getParent() != _dataStore.getDescriptorRoot()))
          {	
            String localPath = _dataStore.mapToLocalPath(source);
            
            if (localPath != null)
            {
	      File result = new File(localPath);
	      if (!result.exists())
		{		  
		  // initiate query
		    DataElement fileDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(),
								 DE.A_NAME,
								 _dataStore.getLocalizedString("model.file"),
								 1);
		    if (fileDescriptor != null)
                  {	
		      DataElement openDescriptor = _dataStore.localDescriptorQuery(fileDescriptor, "C_OPEN");
		      if (openDescriptor != null)
			{
			    if (doSynchronize)
				{
				    DataElement status = _dataStore.synchronizedCommand(openDescriptor, this);
				    if (status.getAttribute(DE.A_NAME).equals(_dataStore.getLocalizedString("model.done")))
					{
					    return new File(localPath);
					}
				    else
					{
					    System.out.println(_dataStore.getLocalizedString("model.timeout"));

					    return null;
					}
				}
			    else
				{
				    _dataStore.command(openDescriptor, this);
				    return null;
				}
			}
		    else
			{
			    System.out.println("no such file");
			    return null;
			}
                  }
		}
	      else
		{		  
		  return result;
		}	      
            }	
          }
        }
        
        return null;
      }

	
  public void fireDomainChanged()
      {
	  _dataStore.fireDomainChanged(new DomainEvent(DomainEvent.INSERT,
						       this,
						       DE.P_NESTED));						
      }
  
  public Object getAdapter(Class key)
      {
	  Object adapter = DesktopElement.getPlatformAdapter(this, key);
	  if (adapter != null)
	      {
		  return adapter;
	      }
	  
	  if (DesktopElement.matches(key))
	      {
		  if (_desktopElement == null)
		      {
			  _desktopElement = new DesktopElement((IDataElement)this);
		      }

		  return _desktopElement;
	      }	  
	  else if (PropertySource.matches(key))
	      {
		  if (_propertySource == null)
		      {
			  _propertySource = new PropertySource(this);
		      }
		  
		  return  _propertySource;	    
	      }
	  return null;
      }

  /////////////////////////////////////////
  //
  // Testing
  //
  /////////////////////////////////////////

    public String toString()
    {
      	return "DataElement " + (_isReference ? "reference" : "") + 
	    "\n{\n\tType:\t" + getType() + 
	    "\n\tName:\t" + getName() +
	    "\n\tValue:\t" + getValue() +
	    "\n\tID:\t" + getId() +
	    "\n\tSource:\t" + getSource() +
	    "\n\tDepth:\t" + _depth +
	    "\n}\n";
    }


  /////////////////////////////////////////
  //
  // IElement implementation
  //
  /////////////////////////////////////////
  public Object getElementProperty(Object name)
      {        
       if(_isReference)
	      {          
		  if (_referencedObject == null)
		      {
			  dereference();
		      }
		  
		  if (_referencedObject != null)
		      {
			  return _referencedObject.getElementProperty(name);
		      }
		  else
		      {
			  return null;
		      }
	      }
	  else
	      {
		  if (DE.P_NOTIFIER.equals(name))
		      {
			  return _dataStore.getDomainNotifier();
		      }
		  else if (DE.P_LABEL.equals(name))
		      {
			  return getAttribute(DE.A_NAME);
		      }
		  else if (DE.P_TYPE.equals(name))
		      {
			  return getAttribute(DE.A_TYPE);
		      }
		  else if (DE.P_NAME.equals(name))
		      {
			  return getAttribute(DE.A_NAME);
		      }
		  else if (DE.P_VALUE.equals(name))
		      {
			  return getAttribute(DE.A_VALUE);
		      }
		  else if (DE.P_ID.equals(name))
		      {
			  return getAttribute(DE.A_ID);
		      }
		  else if (DE.P_SOURCE_NAME.equals(name))
		      {
			  String source = getAttribute(DE.A_SOURCE);
			  int locationIndex =  source.lastIndexOf(":");
			  if (locationIndex > 3)
			      {
				  return source.substring(0, locationIndex);
			      }	
			  return source;	
		      }
		  else if (DE.P_BUFFER.equals(name))
		      {
			  return _buffer;
		      }
		  else if (DE.P_SOURCE.equals(name))
		      {
			  return null;
		      }
		  else if (DE.P_SOURCE_LOCATION.equals(name))
		      {
			  String source = getAttribute(DE.A_SOURCE);
			  if (source != null)
			      {
				  int locationIndex =  source.lastIndexOf(":");
				  if (locationIndex > 3)
				      {
					  int columnIndex = source.lastIndexOf(",");
					  if (columnIndex < locationIndex)
					      {
						  columnIndex = source.length();
					      }
					  try
					      {
						  Integer sourceLocation = new Integer(source.substring(locationIndex + 1, columnIndex));
						  return sourceLocation;
					      }
					  catch (NumberFormatException e)
					      {
						  System.out.println(e);						  
					      }
				      }
			      }
			  
			  return new Integer(1);
		      }
		  else if (DE.P_SOURCE_LOCATION_COLUMN.equals(name))
		      {
			  return null;
		      }
		  else if (DE.P_NESTED.equals(name) || DE.P_CHILDREN.equals(name))
		      {
			  return getNestedData();
		      }
		  else
		      {	
			  return null;
		      }
	      }
       
  }  
      
}





