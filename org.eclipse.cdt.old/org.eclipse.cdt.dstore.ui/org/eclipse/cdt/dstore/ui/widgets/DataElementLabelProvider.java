package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.core.DataStoreCorePlugin;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.resource.ResourceElement;

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
    private static DataElementLabelProvider _instance;
    

    public DataElementLabelProvider(ImageRegistry registry)
    {
        super();
        _labelProperty = DE.P_VALUE;
	
        _registry = registry;
		String baseDir        = DataStoreCorePlugin.getRootDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH); 
        String imageStr = baseDir + "com.ibm.dstore.ui" + 
	    java.io.File.separator + "icons" + java.io.File.separator + "default.gif";
	
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
			image = ImageDescriptor.createFromFile(null, imageStr).createImage();
			_registry.put(imageStr, image);
			
			image = _registry.get(imageStr);
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
    }
    
    public synchronized String getText(Object obj)
    {
	if (obj instanceof DataElement)
	    {	
		DataElement element = (DataElement)obj; 
		String type  = (String)element.getElementProperty(DE.P_TYPE);
		if (type != null && type.equals("property"))
		    {
			String name = (String)element.getElementProperty(DE.P_NAME);
			String value = (String)element.getElementProperty(DE.P_VALUE);
			return name + " = " + value;
		    }
		else
		    {
			if (_labelProperty == null)
			    {
				_labelProperty = DE.P_VALUE;
			    }
			else if (_labelProperty == DE.P_BUFFER)
			    {
				StringBuffer buffer = (StringBuffer)element.getElementProperty(_labelProperty);
				if (buffer != null)
				    {
					return buffer.toString();
				    }
				else
				    {
					return "null";
				    }
			    }	
			String result = (String)element.getElementProperty(_labelProperty);
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

    public static String getImageString(DataElement element)
    {
	DataStore dataStore   = element.getDataStore();

	String baseDir        = dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH); 			
	String type           = element.getType();

	StringBuffer iconPath = new StringBuffer(baseDir);

	if (type.equals(DE.T_OBJECT_DESCRIPTOR) || 
	    type.equals(DE.T_RELATION_DESCRIPTOR) ||
	    type.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR) ||
	    type.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
            {
		type = element.getName();
		String subDir = element.getSource();
		if (subDir.length() > 0)
		    {
			iconPath.append(subDir);
		    }
		else
		    {
			iconPath.append("com.ibm.dstore.core");
		    }
            }
	else
            {
		DataElement descriptor = element.getDescriptor();
		if (descriptor != null)
		    {
			String subDir = descriptor.getSource(); 
			if (subDir.length() > 0)
			    {
				iconPath.append(subDir);
			    }
			else
			    {
				iconPath.append("com.ibm.dstore.core");
			    }
		    }
            }

	iconPath.append(File.separator);	
	iconPath.append("icons");
	iconPath.append(File.separator);
	iconPath.append(type);
	iconPath.append(".gif");        

	return iconPath.toString();
    }
    
    public Image getColumnImage(Object element, int columnIndex)
    {
	return getImage(element);    
    }
        
    public String getColumnText(Object element, int columnIndex)
    {
	return getText(element);    
    }    
}

