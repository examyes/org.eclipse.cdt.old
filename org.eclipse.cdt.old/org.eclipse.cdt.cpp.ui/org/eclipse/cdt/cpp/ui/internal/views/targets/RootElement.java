package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.viewers.*;
import java.util.*;
import org.eclipse.core.resources.*;
import com.ibm.cpp.ui.internal.*;

public class RootElement extends BaseElement {

	private int counter = 0;
	
	protected int MAX_TARGETS = 100; //TBC
	
	private Vector fTargets;
	
	//property unique keys
	
	public String[] P_TARGETS; 
	public String[] P_ID_TARGETS;
	private Vector descriptors = new Vector();
	//Index of selected table item (in our case, it is a target)
	public Vector indexOfSelectedTableItems = new Vector(100); // updated from the Action Build Target - see actionPerformed method
	private String key = new String("");
	private int unique = 0;

	private IResource root;
	
	// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	protected String Key = "TargetsViewer.RootElement.Key";
	

/**
 * Constructor. Default visibility only called from createSubGroup()
 * Creates a new GroupElement within the passed parent GroupElement,
 * gives it the passed name property, sets Icon.
 * Note: parent GroupElement must be notified to register this as one of it sub entries
 * @param name java.lang.String
 * @param parent com.itp.ui.test.propertysheet.GroupElement
 */
public RootElement(IResource root, RootElement parent) {
	super(root.getName(), parent);
	this.root = root;
	P_ID_TARGETS = new String[MAX_TARGETS];
	P_TARGETS = new String[MAX_TARGETS];
}
/**
 * Adds a OrganizationElement to this GroupElement. Fires DomainEvent to
 * notify all listening viewers (thru DomainModel) to update view.
 * @param newGroup com.ibm.itp.ui.test.propertysheet.Group
 */
public void add(TargetElement obj) {

	if (obj.isTarget()) 
	{
		obj.setID(key);
		getTargets().add(obj);
		// synchronizes backpointer of userGroup: defensive
		obj.setParent(this);

	}

}
/**
 * Deletes a OrganizationElement from this GroupElement. Fires DomainEvent to
 * notify all listening viewers (thru DomainModel) to update view. Called by
 * OrganizationElement.delete()
 * @param newGroup com.ibm.itp.ui.test.propertysheet.Group
 */
public void delete(BaseElement element) {

	if (element.isTarget()) {
		getTargets().remove(element);
		// synchronizes backpointer of userGroup: defensive
	}
}
/**
 * Returns the children of this object.  When this object
 * is displayed in a tree, the returned objects will be this
 * element's children.  Returns an empty enumeration if this
 * object has no children.
 */
public Object[] getChildren(Object o) {
	return getContents().toArray();
}
private Vector getContents() {
	if (fTargets == null)
		fTargets = new Vector();
	return fTargets;
}
/**
 * Overrides Hook in OrganizationElement
 * @see OrganizationElement#isGroup()
 */
public int getCounter() {
	return counter;
}
public Vector getDescriptors()
{	
	return descriptors;
}
public Object getEditableValue() {
	return this.toString();

}
public String getErrorMessage() {
	return null;
}
/* Implemented as part of IPropertySource,
 * returns Vector set of PropertyDescriptors defining availible properties in this IProperty.
 * @see IPropertySource#getPropertyDescriptors()
 * @return java.util.Vector
*/

public IPropertyDescriptor[] getPropertyDescriptors()
{	
	return (IPropertyDescriptor[])(getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]));
}
/**
 * Hook implementation as part of IPropertySource, it defines
 * 	1) P_NAME returns String, name of this element
 *  this property keys are defined in IBasicPropertyConstants
 *
 * @param  propKey java.lang.Object
 * @return java.lang.Object associated property
 **/

public Object getPropertyValue(String propKey)
{
	for(int i =0; i< getTargets().size(); i++)
	{
		if (propKey.equals(P_ID_TARGETS[i]))
			return getTargets().elementAt(i);
			
	}
	return null;	
}
public IResource getRoot() {

	return this.root;
}
public Vector getTargets() {
	if (fTargets == null)
		fTargets = new Vector();
	return fTargets;
}
/**
 * Overrides Hook in OrganizationElement
 * @see OrganizationElement#isGroup()
 */
public boolean isRoot() {
	return true;
}
/**
 * author: yasser
 */
public void resetCounter(int val) {
	counter = val;
}
/**
 * author: yasser
 */
public void setDescriptors(Vector desc) {
	descriptors.removeAllElements();
	for(int i=0; i < desc.size(); i++)
		descriptors.add(i,desc.elementAt(i));
}
/**
 * Insert the method's description here.
 * Creation date: (2/6/01 4:42:45 PM)
 * @param descriptor java.lang.String
 */
protected void setProprtyDescriptor() {

	// setting up the unique key
	key = new String(pluginInstance.getLocalizedString(Key)+unique++); // this will be set as the target's unique key see add(TargetElement)
										//-  needed when removing the target
	// defining descriptors
	int pos = counter;
	P_TARGETS[pos]= pluginInstance.getLocalizedString(Key);
	int extension = pos+MAX_TARGETS; // needed for ordering withinthe targets view
	
	P_ID_TARGETS[pos] = pluginInstance.getLocalizedString(Key)+extension;
	descriptors.addElement(new PropertyDescriptor(P_ID_TARGETS[pos], P_TARGETS[pos]));
	counter++;

	// rest the counter to 0 if descriptors exceeded 100
	//if(descriptors.size() == 100)
		//counter =0;
}
/**
 * author: yasser
 */
public void setTargets(Vector targets) {
	fTargets.removeAllElements();
	for(int i=0; i < targets.size(); i++)
		fTargets.add(i,targets.elementAt(i));
}
}
