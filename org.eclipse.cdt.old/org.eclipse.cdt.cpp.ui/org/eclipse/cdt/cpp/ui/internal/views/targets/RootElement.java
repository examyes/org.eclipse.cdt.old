package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;



import org.eclipse.jface.viewers.*;
import java.util.*;
import org.eclipse.core.resources.*;
import com.ibm.cpp.ui.internal.*;

public class RootElement extends BaseElement 
{
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

	private IProject root;
	
	// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	protected String Key = "TargetsViewer.RootElement.Key";
	

    /**
     * Constructor. 
     */
    public RootElement(IProject root, RootElement parent) 
    {
	super(root.getName(), parent);
	this.root = root;
	P_ID_TARGETS = new String[MAX_TARGETS];
	P_TARGETS = new String[MAX_TARGETS];
    }
    
    /**
     *
     */
    public void add(TargetElement obj) 
    {	
	if (obj.isTarget()) 
	    {
		obj.setID(key);
		getTargets().add(obj);
		obj.setParent(this);		
	    }
    }

/**
 *
 */
public void delete(BaseElement element) {

	if (element.isTarget()) {
		getTargets().remove(element);
	}
}
/**

 */
public Object[] getChildren(Object o) {
	return getContents().toArray();
}
private Vector getContents() {
	if (fTargets == null)
		fTargets = new Vector();
	return fTargets;
}

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
/** 
*/

public IPropertyDescriptor[] getPropertyDescriptors()
{	
	return (IPropertyDescriptor[])(getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]));
}
/**
 *
 **/

public Object getPropertyValue(Object propKey)
{
	for(int i =0; i< getTargets().size(); i++)
	{
		if (((String)propKey).equals(P_ID_TARGETS[i]))
			return getTargets().elementAt(i);
			
	}
	return null;	
}

public IProject getRoot() 
{
    return this.root;
}

public Vector getTargets() {
	if (fTargets == null)
		fTargets = new Vector();
	return fTargets;
}
public boolean isRoot() {
	return true;
}
/**
 */
public void resetCounter(int val) {
	counter = val;
}
/**
 */
public void setDescriptors(Vector desc) {
	descriptors.removeAllElements();
	for(int i=0; i < desc.size(); i++)
		descriptors.add(i,desc.elementAt(i));
}
/**
 *
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

public void setTargets(Vector targets) {
	fTargets.removeAllElements();
	for(int i=0; i < targets.size(); i++)
		fTargets.add(i,targets.elementAt(i));
}
}
