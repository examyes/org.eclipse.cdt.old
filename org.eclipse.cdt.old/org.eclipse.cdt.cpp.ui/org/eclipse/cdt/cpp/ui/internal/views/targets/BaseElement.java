package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
//import org.eclipse.ui.views.properties.IPropertySource;

import org.eclipse.jface.viewers.*;
import java.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.*;
import org.eclipse.core.runtime.*;
import java.util.*;
import com.ibm.cpp.ui.internal.*;

public abstract class BaseElement implements IAdaptable, IPropertySource,IWorkbenchAdapter {
	//
	private RootElement fParent;
	private String fName;
	private ImageDescriptor imageDescriptor;
	
	// NL Enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static String DISPLAYNAME="TargetsViewer.PropertyDescriptor.Display_Name";
	//
	private static Vector descriptors;
	static {
		descriptors = new Vector();
		PropertyDescriptor name = new PropertyDescriptor(IBasicPropertyConstants.P_IMAGE, pluginInstance.getLocalizedString(DISPLAYNAME));
		descriptors.addElement(name);
	}
BaseElement(String name, RootElement parent) {
	fName = name;
	fParent = parent;
}
/**
 * Deletes this OrganizationElement from its parentGroup
 */
public void delete()
{	fParent.delete(this);
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 *
 * @param adapter the adapter class to look up
 * @return a object castable to the given class, 
 *    or <code>null</code> if this object does not
 *    have an adapter for the given class
 */
public Object getAdapter(Class adapter) {
	if (adapter == IPropertySource.class) {
		return this;
	}
	if (adapter == IWorkbenchAdapter.class) {
		return this;
	}
	return null;
}
public Vector getDescriptors()
{	return descriptors;
}
/*
 *
 */
public Object getEditableValue() {
	return this;
}
public ImageDescriptor getImageDescriptor(Object object)
{	return imageDescriptor;
}
/**
 * Returns the name of this element.  This will typically
 * be used to assign a label to this object when displayed
 * in the UI.
 */
public String getLabel(Object o) {
	return getName();
}
String getName()
{
	return fName;
}
public Object getParent(Object o)
{	return fParent;
}
/* Implemented as part of IPropertySource,
 * returns Vector set of PropertyDescriptors defining availible properties in this IProperty.
 * @see IPropertySource#getPropertyDescriptors()
 * @return java.util.Vector
*/

public IPropertyDescriptor[] getPropertyDescriptors()
{	
	return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
}
/**
 * Hook implementation as part of IPropertySource, it defines
 * 	1) P_NAME returns String, name of this element
 *  this property keys are defined in IBasicPropertyConstants
 *
 * @param  propKey java.lang.Object
 * @return java.lang.Object associated property
*/

public Object getPropertyValue(String propKey)
{
	if (propKey.equals(IBasicPropertyConstants.P_TEXT))
		return getName();
	return null;
}
/**
 * Return true if the property specified has been changed.
 *
 * @see IPropertySource#getPropertyDescriptors()
 * @param property Object
 * @return boolean
 */
public boolean isPropertySet(String property) {
	return false;
}
/**
 * Hook. Implemented by GroupElement for use instead of instanceof
 * @return boolean
 */
public boolean isRoot() {
	return false;
}
/**
 * Hook. Implemented by UserElement for use instead of instanceof
 * @return boolean
 */
public boolean isTarget() {
	return false;
}
/**
 * Reset the specified property to its default value.
 *
 * @see IPropertySource#resetPropertyValue
 * @param property Object
 */
public void resetPropertyValue(String property){}
void setImageDescriptor(ImageDescriptor desc)
{	imageDescriptor = desc;
}
void setName(String newName) {
	fName = newName;
}
/**
 * Sets this instance's parent back pointer. Defensive
 * @param newParentGroup com.ibm.itp.ui.test.propertysheet.Group
 */
void setParent(RootElement newParent)
{	fParent = newParent;
}
/**
 * Set the named property to the given value.
 * This method should contain the object value verification and return
 * false if the new value is invalid. Otherwise, return true;
 * The errorMessage should also be set if returning false from this method.
 * @see <code>getErrorMessage</code>
 *
 * Implemented as part of IPropertySource. Defines the following Setable properties
 *	1) P_NAME, expects String, sets the name of this OrganizationElement
 *
 * @param	name 	The name of the Property being set
 * @param	value 	The new value of the property
 */
public void setPropertyValue(String name, Object value) {
	if (name.equals(IBasicPropertyConstants.P_TEXT)) {
		setName((String) value);
		return;
	}
}
}
