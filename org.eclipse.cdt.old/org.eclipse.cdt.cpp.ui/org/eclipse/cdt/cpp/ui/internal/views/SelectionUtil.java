package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import java.util.Iterator;

/**
 * Provides utilities for checking the validity of selections.
 * <p>
 * This class provides static methods only; it is not intended to be instantiated
 * or subclassed.
 * </p>
 */
/* package */ class SelectionUtil {
/* (non-Javadoc)
 * Private constructor to block instantiation.
 */
private SelectionUtil(){
}
/**
 * Returns whether the types of the resources in the given selection are among 
 * the specified resource types.
 * 
 * @param selection the selection
 * @param resourceMask resource mask formed by bitwise OR of resource type
 *   constants (defined on <code>IResource</code>)
 * @return <code>true</code> if all selected elements are resources of the right
 *  type, and <code>false</code> if at least one element is either a resource
 *  of some other type or a non-resource
 * @see IResource#getType
 */
public static boolean allResourcesAreOfType(IStructuredSelection selection, int resourceMask) {
	Iterator resources = selection.iterator();
	while (resources.hasNext()) {
		Object next = resources.next();
		if (!(next instanceof IResource))
			return false;
		if (!resourceIsType((IResource)next, resourceMask))
			return false;
	}
	return true;
}
/**
 * Returns whether the type of the given resource is among the specified 
 * resource types.
 * 
 * @param resource the resource
 * @param resourceMask resource mask formed by bitwise OR of resource type
 *   constants (defined on <code>IResource</code>)
 * @return <code>true</code> if the resources has a matching type, and 
 *   <code>false</code> otherwise
 * @see IResource#getType
 */
public static boolean resourceIsType(IResource resource, int resourceMask) {
	return (resource.getType() & resourceMask) != 0;
}
}
