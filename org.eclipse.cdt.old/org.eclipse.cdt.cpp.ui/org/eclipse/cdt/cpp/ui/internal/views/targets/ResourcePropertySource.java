package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
//import org.eclipse.ui.views.properties.IPropertySource;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.views.properties.*;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import java.util.*;
import com.ibm.cpp.ui.internal.*;

/**
 * A Resource property source.
 */
public class ResourcePropertySource implements IPropertySource {
	// The element for the property source
	protected IResource element;

	// Error message when setting a property incorrectly
	// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	protected String errorMessage = "TargetsViewer.Resource.PropertySource.Error_Message";
	
	// Property Descriptors
	static protected IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[3];
	{
		PropertyDescriptor descriptor;

		// resource name
		descriptor = new PropertyDescriptor(IBasicPropertyConstants.P_TEXT, IResourcePropertyConstants.P_LABEL_RES);
		descriptor.setAlwaysIncompatible(true);
		propertyDescriptors[0] = descriptor;

		// Relative path
		descriptor = new PropertyDescriptor(IResourcePropertyConstants.P_PATH_RES, IResourcePropertyConstants.P_DISPLAYPATH_RES);
		descriptor.setAlwaysIncompatible(true);
		propertyDescriptors[1] = descriptor;

		// readwrite state
		descriptor = new PropertyDescriptor(IResourcePropertyConstants.P_EDITABLE_RES, IResourcePropertyConstants.P_DISPLAYEDITABLE_RES);
		descriptor.setAlwaysIncompatible(true);
		propertyDescriptors[2] = descriptor;

	}
/**
* Create a PropertySource and store its IElement
*/
public ResourcePropertySource(IResource res) {
	this.element = res;
}
/**
 * Do nothing because properties are read only.
 */
public Object getEditableValue() {
	return this;
}
/**
 * Return the Property Descriptors for the receiver.
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	return propertyDescriptors;
}
/**
 * Return the value of the property named name.
 */
public Object getPropertyValue(Object obj) 
{
    return getPropertyValue((String)obj);
}

public Object getPropertyValue(String name) {
	if (name.equals(IBasicPropertyConstants.P_TEXT)) {
		return element.getName();
	}
	if (name.equals(IResourcePropertyConstants.P_PATH_RES)) {
		return element.getFullPath().toString();
	}
	if (name.equals(IResourcePropertyConstants.P_EDITABLE_RES)) {
		if (element.isReadOnly())
			return "false";
		else
			return "true";
	}
	return null;
}
/**
 * Answer true if the value of the specified property 
 * for this object has been changed from the default.
 */
public boolean isPropertySet(String property) {
	return false;
}

public boolean isPropertySet(Object property) {
	return false;
}
/**
 * Reset the specified property's value to its default value.
 * Do nothing because properties are read only.
 * 
 * @param   property    The property to reset.
 */
public void resetPropertyValue(String property) {}

public void resetPropertyValue(Object property) {}
/**
 * Do nothing because properties are read only.
 */
public void setPropertyValue(String name, Object value) {
}

public void setPropertyValue(Object name, Object value) {
}
}
