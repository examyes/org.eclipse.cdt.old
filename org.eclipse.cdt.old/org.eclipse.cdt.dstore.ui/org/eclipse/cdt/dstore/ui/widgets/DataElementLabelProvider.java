package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.DataStoreCorePlugin;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.resource.ResourceElement;

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.*;

public class DataElementLabelProvider  extends LabelProvider implements ILabelProvider, ITableLabelProvider
{ 
    private ImageRegistry _registry;
    private static final String DEFAULT_ICON = "default.gif";
    private Image  _default;
    
    private String _labelProperty;
    private int    _labelPropertyIndex;
    
    private static DataElementLabelProvider _instance;
    private IActionLoader _actionLoader;
    
    private DataElement _attributesD = null;

    public DataElementLabelProvider(ImageRegistry registry, IActionLoader loader)
    {
        super();
        setLabelProperty(DE.P_VALUE);
   
        _registry = registry;
        _actionLoader = loader;
        
		String baseDir        = DataStoreUIPlugin.getInstance().getInstallLocation(); 
        String imageStr = baseDir + java.io.File.separator + "icons" + java.io.File.separator + "default.gif";
	
        Image image = _registry.get(imageStr); 
        if (image == null)
	    {
		_registry.put(imageStr, ImageDescriptor.createFromFile(null, imageStr));
		image = _registry.get(imageStr);
	    }
	
        _default = image;	
	_instance = this;
    }

    public DataElementLabelProvider getInstance()
    {
	return _instance;
    }
    
    public void setLoader(IActionLoader loader)
    {
    	_actionLoader = loader;
    	
    }
    
    public Image getImage(Object i)
    {
	if (i instanceof String)
	    {
		return getImage((String)i);
	    }	
	else if (i instanceof DataElement)
	    {
		DataElement element = (DataElement)i; 
		String imageStr = getImageString(element);
		
		Image result = null;
		if (imageStr != null)
		    {
			result = getImage(imageStr);
		    }
		return result;        
	    }
	else if (i instanceof ResourceElement)
	    {
		ResourceElement resource = (ResourceElement)i;
		return resource.getImage(i, null);	    
	    }	
	else if (i instanceof IProject)
	    {
		IProject project = (IProject)i;
		
		DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		if (project.isOpen())
		    {
			return plugin.getImage("project.gif", true);
		    }
		else
		    {
			return plugin.getImage("project_closed.gif", true);
		    }
	    }
	else
	    {
		return null;
	    }
    }
    
    public Image getImage(String imageStr)
    {
	Image image = null; 
	image = _registry.get(imageStr);
	if (image == null && imageStr != null)
	    {
		File file = new File(imageStr);
		if (file.exists())
		    {
			try
			    {
				image = ImageDescriptor.createFromFile(null, imageStr).createImage();
				_registry.put(imageStr, image);
				
				image = _registry.get(imageStr);
			    }
			catch (org.eclipse.swt.SWTException e)
			    {
				//System.out.println(e);
				System.out.println("invalid image: " + imageStr);
				return _default;
			    }
		    }
		else
		    {
			return _default;
		    }
	    }
	
        return image;
    }
    
    public Image getLabelImage(Viewer viewer, Object obj)
    {
	if (obj != null)
	    {	
		DataElement element = (DataElement)obj; 
		String imageStr = getImageString(element);
		
		Image result = getImage(imageStr);
		return result;       
	    }
	else
	    {
		return null;	
	    }
    }
    
    
    
    
    public void setLabelProperty(String property)
    {
        _labelProperty = property;
      
        if (_labelProperty.equals(DE.P_TYPE))
        {
        	_labelPropertyIndex = DE.A_TYPE;
        }
        else if (_labelProperty.equals(DE.P_NAME))
		{
		    _labelPropertyIndex = DE.A_NAME;
		}
        else if (_labelProperty.equals(DE.P_VALUE))
        {
        	_labelPropertyIndex = DE.A_VALUE;
        }
        else if (_labelProperty.equals(DE.P_SOURCE))
        {
        	_labelPropertyIndex = DE.A_SOURCE;
        }
       
    }
    
    public String getText(Object obj)
    {
	if (obj instanceof DataElement)
	    {	
		DataElement element = (DataElement)obj; 
		String type  = element.getAttribute(DE.A_TYPE);
		if (type != null && type.equals("property"))
		    {
			String name = element.getAttribute(DE.A_NAME);
			String value = element.getAttribute(DE.A_VALUE);
			return name + " = " + value;
		    }
		else
		    {
			if (_labelProperty == null)
			    {
				_labelProperty = DE.P_VALUE;
			    }			
			String result = element.getAttribute(_labelPropertyIndex);
			if (result == null)
			    {
				if (element.isDeleted())
				    {
					result = "deleted";
				    }
				else
				    {
					result = "null";
				    }
			    }
			return result;
		    }
	    }
	else if (obj instanceof ResourceElement)
	    {
		ResourceElement resource = (ResourceElement)obj;	
		String name = resource.getLabel(obj);
		if (name == null)
		    name = "null";
		
		return name;	
	    } 
	else if (obj instanceof IProject)
	    {
		IProject repository = (IProject)obj;
		String name = repository.getName();
		return name;		
	    }    
	else
	    {
		return "null";
	    }
    }


	public String getImageString(String key)
	{
		if (_actionLoader != null)
		{
			return _actionLoader.getImageString(key);
		}	
		else
		{
			return "";
		}
	}
	
    public String getImageString(DataElement element)
    {
    	if (_actionLoader != null)
    	{
    		return _actionLoader.getImageString(element);
    	}
    	else
    	{
    		return "";
    	}    
    }
    
    public Image getColumnImage(Object element, int columnIndex)
    {
	if (columnIndex == 0)
	    {
		return getImage(element);    
	    }
	else
	    {
		return null;
	    }
    }
        
    public String getColumnText(Object element, int columnIndex)
    {
	if (columnIndex == 0)
	    {
		return getText(element);    
	    }
	else if (element instanceof DataElement)
	    {		
	    	
	   	DataElement data = (DataElement)element;
	   	DataStore dataStore = data.getDataStore();
	   	if (_attributesD == null || _attributesD.getDataStore() != dataStore)
	   	{
	   	  _attributesD = dataStore.getAttributesRelation();
	   	}
	   	
	   	
		DataElement descriptor = data.getDescriptor();
		DataElement attributeDescriptor = getAttributeDescriptor(descriptor, columnIndex - 1);				
		
		if (attributeDescriptor != null)
		    {
			ArrayList attributes = data.getAssociated(_attributesD);
			
			if (attributes.size() > 0)
			{
				DataElement attribute = null;
				if (attributes.size() > columnIndex)
				{
					attribute = (DataElement)attributes.get(columnIndex - 1);
					if (attribute.getDescriptor() == attributeDescriptor)
					{
						return getText(attribute);
					}
				}
			
				for (int i = 0; i < attributes.size(); i++)
				    {
					    {
						attribute = (DataElement)attributes.get(i);
						if (attribute.getDescriptor() == attributeDescriptor)
						    {
							return getText(attribute);
						    }
					    }
				    }
			}

			// get attribute attributes
			ArrayList format = attributeDescriptor.getAssociated(_attributesD);
			if (format != null && format.size() > 0)
			    {
				DataElement formatDescriptor = (DataElement)format.get(0);
				String formatStr = formatDescriptor.getName();
				
				if (formatStr.equals("Integer") || formatStr.equals("Date"))
				    {
					return "0";
				    }
				else if (formatStr.equals("Float"))
				    {
					return "0.0";
				    }
				else
				    {
					return "";
				    }		

			    }
			
		    }
		    
		    
		    
	    }
	return "";
    }    


    private DataElement getAttributeDescriptor(DataElement rootDescriptor, int attributeIndex)
    {
    
	int attributeNum = 0;
	
	for (int i = 0; i < rootDescriptor.getNestedSize(); i++)
	{
		DataElement child = rootDescriptor.get(i);
		if (child.isReference())
		{
			if (child.getType().equals("attributes"))
			{		
				if (attributeNum == attributeIndex)
				{
					return child.dereference();				
				}
				attributeNum++;
			}		
		}
	
	}

	return null;
    }
}

