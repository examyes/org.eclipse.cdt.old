package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.ui.views.properties.IPropertySource; 
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.jface.viewers.*;
import java.util.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.cdt.cpp.ui.internal.*;

public class TargetElement extends BaseElement {

	private String fTargetName;
	private String fWorkingDirectory;
	private DataElement fStatus;
	private String fMakeInvocation;

	//Property unique keys
	public static final String P_ID_NAME = "Target.2.Name";
	public static final String P_ID_WORKING_DIR = "Target.3.Working";
	public static final String P_ID_MAKE_INV = "Target.1.Invocation";

	//Property display keys
	// NL enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	protected static String P_NAME = "TargetsViewer.Target_Element.Name";
	protected static String P_WORKING_DIR = "TargetsViewer.Target_Element.Working_Directory";
	protected static String P_MAKE_INV = "TargetsViewer.Target_Element.Invocation";
	protected static String BROWSE = "TargetsViewer.Target_Element.Browse";

	private static Vector descriptors;

	// internal id
	private String uniqueID;
	// descriptors
	static {
		descriptors = new Vector();
		descriptors.addElement(new TextPropertyDescriptor(P_ID_NAME, pluginInstance.getLocalizedString(P_NAME)));
		descriptors.addElement(
			new BrowsePropertyDescriptor(P_ID_WORKING_DIR, pluginInstance.getLocalizedString(P_WORKING_DIR), pluginInstance.getLocalizedString(BROWSE)));
		descriptors.addElement(new TextPropertyDescriptor(P_ID_MAKE_INV, pluginInstance.getLocalizedString(P_MAKE_INV)));
	}

TargetElement(String name, RootElement parent) {
	super(name, parent);
}
/**
 * 
 */
TargetElement(String name, String working_dir, String make_invocation, RootElement parent) {
	super(name, parent);
	setName(name);
	this.fTargetName = name;
	this.fWorkingDirectory= working_dir;
	this.fMakeInvocation = make_invocation;

	DataStore dataStore = CppPlugin.getCurrentDataStore();
	this.fStatus = dataStore.createObject(null, "status", "name");
	setParent(parent);
	
}
/**
 * *
 */
public Object[] getChildren(Object o) {
	return new Object[0];
}
/**
 * 
 */
public Vector getDescriptors()
{
	return descriptors;
}
/*
 *
 */
public Object getEditableValue() {
	return getTargetName();
}
String getID()
{
	return uniqueID;
}
/**
 * Returns the logical parent of the given object in its tree.
 */
public Object getMakeInvocation() {
	return fMakeInvocation;
}
/**
 *
*/

public IPropertyDescriptor[] getPropertyDescriptors()
{	
	return (IPropertyDescriptor[])(getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]));
}
/**
*
*/
public Object getPropertyValue(Object propKey) {
	if (((String)propKey).equals(P_ID_NAME))
		return getTargetName();
	if (((String)propKey).equals(P_ID_WORKING_DIR))
		return fWorkingDirectory;
	if (((String)propKey).equals(P_ID_MAKE_INV))
		return fMakeInvocation;

	return super.getPropertyValue(propKey);
}
public DataElement getStatus()
{
  return fStatus;  
}
/**
 * Returns the logical parent of the given object in its tree.
 */
public Object getTargetName() {
	return fTargetName;
}
/**
 * Returns the logical parent of the given object in its tree.
 */
public Object getWorkingDirectory() {
	return fWorkingDirectory;
}
/**
 *
 */
public boolean isPropertySet(String property) {
	return false;
}
/**
*
 */
public boolean isTarget() {
	return true;
}
protected void setID(String id)
{
  uniqueID = id;  
}


public void setInvocation(String invocation) {
	fMakeInvocation = invocation;
}
/**
 * @param	name 	The name of the Property being set
 * @param	value 	The new value of the property
 */
public void setPropertyValue(Object name, Object value) {
	if (((String)name).equals(P_ID_NAME)) {
		setTargetName((String)value);
	}
	if (name.equals(P_ID_MAKE_INV)) {
		setInvocation(value.toString());
	}
	if (name.equals(P_ID_WORKING_DIR)) {
		setWorkingDir(value.toString());
	}
}
public void setStatus(DataElement status)
{
  fStatus = status;  
}
protected void setTargetName(String name)
{
  fTargetName = name;  
}

public void setWorkingDir(String working) {
	fWorkingDirectory = working;
}
}
