package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/FileTreeContentProvider.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:58:48)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.*;
import java.util.*;


/**
 * Provides content for a tree viewer that shows only compiled language source files.
 */
public class FileTreeContentProvider implements ITreeContentProvider{

	private Viewer viewer;
/**
 * Creates a new FileTreeContentProvider.
 */
public FileTreeContentProvider() {
}
/**
 * The visual part that is using this content provider is about
 * to be disposed. Deallocate all allocated SWT resources.
 */
public void dispose() {}
/**
 * @see ITreeContentProvider#getChildren
 */
public Object[] getChildren(Object element) {
	if (element instanceof IWorkspace) {
		return ((IWorkspace) element).getRoot().getProjects();
	} else if (element instanceof IContainer) {
		IContainer container = (IContainer)element;
		if (container.isAccessible()) {
		    try {
			    List children = new ArrayList();
			    IResource[] members = container.members();
			    for (int i = 0; i < members.length; i++) {
				    if (members[i].getType() == IResource.FILE) {
					    children.add(members[i]);
				    }
			    }
			    return children.toArray();
			} catch (CoreException e) {
				// this should never happen because we call #isAccessible before invoking #members
			}
		}
	}
	return new Object[0];
}
/**
 * @see ITreeContentProvider#getElements
 */
public Object[] getElements(Object element) {
	return getChildren(element);
}
/**
 * @see ITreeContentProvider#getParent
 */
public Object getParent(Object element) {
	if (element instanceof IResource)
		return ((IResource) element).getParent();
	return null;
}
/**
 * @see ITreeContentProvider#hasChildren
 */
public boolean hasChildren(Object element) {
	return getChildren(element).length > 0;
}
/**
 * @see IContentProvider#inputChanged
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	this.viewer = viewer;
}
/**
 * @see IContentProvider#isDeleted
 */
public boolean isDeleted(Object element) {
	return ((element instanceof IResource) && !((IResource) element).exists());
}
}


