package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class BrowsePropertyDescriptor extends PropertyDescriptor {
	String title;
	public BrowsePropertyDescriptor(String uniqueName,String displayName, String newTitle) 
	{
		super(uniqueName,displayName);
		title = new String(newTitle);
	}
	public CellEditor createPropertyEditor(Composite parent) 
	{
			return new BrowseCellEditor(parent,title);
	}
	public boolean isXCompatibleWith(IPropertyDescriptor anotherProperty) 
	{
		return false;
	}
}
