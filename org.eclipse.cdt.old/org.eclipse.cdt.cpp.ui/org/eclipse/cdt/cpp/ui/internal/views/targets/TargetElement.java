package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import com.ibm.dstore.core.model.*;

import org.eclipse.jface.viewers.*;
import java.util.*;
import org.eclipse.swt.graphics.RGB;
import com.ibm.cpp.ui.internal.*;

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



	
/**
 * Constructor. Default visibility only called from GroupElement.createSubGroup()
 * Creates a new GroupElement within the passed parent GroupElement,
 * gives it the passed name property, sets Icon
 * Note: parent GroupElement must be notified to register this as one of it sub entries
 * @param name java.lang.String
 * @param parent com.itp.ui.test.propertysheet.GroupElement
 */
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
 * Returns the children of this object.  When this object
 * is displayed in a tree, the returned objects will be this
 * element's children.  Returns an empty enumeration if this
 * object has no children.
 */
public Object[] getChildren(Object o) {
	return new Object[0];
}
/**
 * Overrides OrganizationElement#getDescriptors()
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
 * Implemented as part of IPropertySource,
 * returns Vector set of PropertyDescriptors defining availible properties in this IProperty.
 * @see IPropertySource#getPropertyDescriptors()
 * @return java.util.Vector
*/

public IPropertyDescriptor[] getPropertyDescriptors()
{	
	return (IPropertyDescriptor[])(getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]));
}
/**
 * Implemented as part of IPropertySource framework
 * Defines in addition to existing properties.
 *	1) P_ADDRESS returns Address (IPropertySource), the address of this user
 * 	2) P_FULLNAME returns Name (IPropertySource), the full name of this user
 *  3) P_PHONENUMBER returns String, the phone number of this user
 *  4) P_EMAIL returns EmailAddress (IPropertySource), the email address of this user
 *  5) P_COOP returns Boolean, whether the individual is a coop student or not
 *  6) P_BDAY returns Birthday
 *  7) P_SALARY return java.lang.Float
 *  8) P_HAIRCOLOR, expects RGB
 *  9) P_EYECOLOR, expects RGB
 *	see OrganizationElement#getPropertyValue(Object) for other properties
 *
 * @param  propKey java.lang.Object
 * @return java.lang.Object associated property
*/
public Object getPropertyValue(String propKey) {
	if (propKey.equals(P_ID_NAME))
		return getTargetName();
	if (propKey.equals(P_ID_WORKING_DIR))
		return fWorkingDirectory;
	if (propKey.equals(P_ID_MAKE_INV))
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
 * Return true if the property specified has been changed.
 *
 * @param property Object
 * @return boolean
 */
public boolean isPropertySet(String property) {
	return false;
}
/**
 * Overrides hook in OrganizationElement
 * @see OrganizationElement#isFile()
 * @return boolean
 */
public boolean isTarget() {
	return true;
}
protected void setID(String id)
{
  uniqueID = id;  
}
/**
 * Address is an IElement root property that itself is an IPropertySource defining children properties,
 * The Birthday value is never directly set, as specified in its PropertyDescriptor, and is entirely
 * determined by its childern properties. (Case II: children determine parent)
 *
 * Rather this method is implicity called whenever any of it children properties are changed
 * responsible for sending a PropertySheetDomainEvent to the Notifier telling those observing this
 * property and/or its children to refresh
 *
 * @param		newAddress		com.ibm.itp.ui.views.propertysheet.model.Address
*/

public void setInvocation(String invocation) {
	fMakeInvocation = invocation;
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
	if (name.equals(P_ID_NAME)) {
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
/**
 * Address is an IElement root property that itself is an IPropertySource defining children properties,
 * The Birthday value is never directly set, as specified in its PropertyDescriptor, and is entirely
 * determined by its childern properties. (Case II: children determine parent)
 *
 * Rather this method is implicity called whenever any of it children properties are changed
 * responsible for sending a PropertySheetDomainEvent to the Notifier telling those observing this
 * property and/or its children to refresh
 *
 * @param		newAddress		com.ibm.itp.ui.views.propertysheet.model.Address
*/

public void setWorkingDir(String working) {
	fWorkingDirectory = working;
}
}
