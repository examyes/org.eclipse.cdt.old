package com.ibm.cpp.ui.internal.views.targets;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;

public class BrowsePropertyDescriptor extends PropertyDescriptor {
	String title;
/**
 * Insert the method's description here.
 * Creation date: (1/18/01 11:50:34 AM)
 */
public BrowsePropertyDescriptor(String uniqueName,String displayName, String newTitle) 
{
	super(uniqueName,displayName);
	//name = uniqueName;
	//description = uniqueName;
	//display = displayName;
	title = new String(newTitle);
}
/**
 * The <code>PropertyDescriptor</code> implementation of this 
 * <code>IPropertyDescriptor</code> method returns <code>null</code>.
 * <p>
 * Since no cell editor is returned, the property is read only.
 * </p>
 */
public CellEditor createPropertyEditor(Composite parent) {
		return new BrowseCellEditor(parent,title);
}
/**
 * Returns whether the receiver is compatible with another property descriptor.
 * The property descriptors should be the same type.
 * 
 * @return <code>true</code> if the property descriptors are compatible; <code>false</code> otherwise
 */
public boolean isXCompatibleWith(IPropertyDescriptor anotherProperty) {
/*	if (getAlwaysIncompatible())
		return false;
	if (anotherProperty instanceof BrowsePropertyDescriptor) {
		BrowsePropertyDescriptor spd = (BrowsePropertyDescriptor) anotherProperty;

		// Compare Name
		if (!spd.getId().equals(getId()))
			return false;

		// Compare DisplayName
		if (!spd.getDisplayName().equals(getDisplayName()))
			return false;

		// Compare Category
		if (getCategory() == null) {
			if (spd.getCategory() != null)
				return false;
		} else {
			if (!getCategory().equals(spd.getCategory()))
				return false;
		}

		// Nothing was different.
		return true;
	}*/
	return false;
}
}
