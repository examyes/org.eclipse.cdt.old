package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/MonitorContentProvider.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:00:14)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.BasicContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLStackFrame;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLVariable;

public class MonitorContentProvider extends BasicContentProvider implements ITreeContentProvider, IDebugEventListener {

	/**
	 * Constructs a new monitor content provider
	 */
	public MonitorContentProvider() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * @see IContentProvider
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		PICLDebugElement debugElement = (PICLDebugElement)parent;
		try {
			return debugElement.getChildren();
		} catch (DebugException de) {
			return null;
		}
	}
	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		if ((event.getSource() instanceof PICLVariable) || (event.getSource() instanceof PICLThread)) {
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
					refresh();
					break;
				case DebugEvent.CHANGE:
					refresh(event.getSource());
					break;
				case DebugEvent.CREATE:
					refresh();
					break;
				case DebugEvent.TERMINATE:
					refresh();
					break;
			}
		}
	}
	/**
	 * @see IStructuredContentProvider
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
	/**
	 * @see ITreeContentProvider
	 */
	public Object getParent(Object child) {
		return ((PICLDebugElement)child).getParent();
	}
	/**
	 * @see ITreeContentProvider
	 */
	public boolean hasChildren(Object parent) {
		return ((PICLDebugElement)parent).hasChildren();
	}
	/**
	 * Helper method to remove the given element
	 */
	protected void remove(final Object element) {
	}
	/**
	 * Helper method to remove all elements
	 */
	protected void removeAll() {
	}
}
