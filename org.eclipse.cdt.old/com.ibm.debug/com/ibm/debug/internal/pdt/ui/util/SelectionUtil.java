package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/SelectionUtil.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:58:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionUtil {

	/**
	 * Returns the elements of the given selection.
	 * Returns an empty array if the selection is empty or if
	 * the given selection is not of type <code>IStructuredSelection</code>.
	 *
	 * @param selection the selection
	 * @return the selected elements
	 *
	 */
	public static Object getFirstElement(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		return ((IStructuredSelection)selection).getFirstElement();
	}
	public static Object getSingleElement(ISelection s) {
		if (!(s instanceof IStructuredSelection))
			return null;
		IStructuredSelection selection= (IStructuredSelection)s;
		if (selection.size() != 1)
			return null;

		return selection.getFirstElement();
	}
	/**
	 * Returns the elements of the given selection.
	 * Returns an empty array if the selection is empty or if
	 * the given selection is not of type <code>IStructuredSelection</code>.
	 *
	 * @param selection the selection
	 * @return the selected elements
	 *
	 */
	public static Object[] toArray(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return new Object[0];
		}
		return ((IStructuredSelection)selection).toArray();
	}
	public static List toList(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection)selection).toList();
		}
		return null;
	}
}
