package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/ModulesContentProvider.java, eclipse, eclipse-dev, 20011129
// Version 1.8 (last modified 11/29/01 14:15:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLEmptyModule;
import com.ibm.debug.internal.picl.PICLModule;
import com.ibm.debug.internal.picl.PICLModuleParent;
import com.ibm.debug.internal.picl.PICLPart;
import com.ibm.debug.internal.picl.PICLUtils;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.internal.ui.BasicContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class ModulesContentProvider
	extends BasicContentProvider
	implements IDebugEventListener, ITreeContentProvider {

	/**
	 * Constructor for ModulesContentProvider
	 */
	public ModulesContentProvider() {
		super();
		PICLUtils.logText("ModulesContentProvider()");
		DebugPlugin.getDefault().addDebugEventListener(this);

	}

	/**
	 * @see BasicContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object obj) {
//		PICLUtils.logText("ModulesContentProvider.getElements()");
		//     Do I need a check for the object passed?
		//     for now I assume it is the correct type

		PICLDebugElement debugElement = (PICLDebugElement) obj;

		try {
			return debugElement.getChildren();
		} catch(DebugException de) {
			// for now just return an empty module
			if (debugElement instanceof PICLModuleParent) {
				PICLDebugElement tmpArray[] = new PICLDebugElement[1];
				tmpArray[0] = new PICLEmptyModule(debugElement);
				return tmpArray;
			} else
				return null;
		}
	}

	/**
	 * @see BasicContentProvider#doHandleDebugEvent(DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
//		PICLUtils.logText("ModulesContentProvider.doHandleDebugEvent");
		Object obj = event.getSource();

		// only do the refresh if it is a debug element that we are interested in.
		if (obj instanceof PICLModule || obj instanceof PICLPart)
			refresh(((PICLDebugElement)obj).getParent());
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object obj) {
//		PICLUtils.logText("ModulesContentProvider.doGetChildren()");

		PICLDebugElement debugElement = (PICLDebugElement)obj;

		try {
			return debugElement.getChildren();
		} catch(DebugException de) {
			return null;
		}

	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object arg0) {
//		PICLUtils.logText("ModulesContentProvider.getParent()");
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object obj) {
//		PICLUtils.logText("ModulesContentProvider.hasChildren()");

		return ((PICLDebugElement)obj).hasChildren();
	}

    /**
     * @see BasicContentProvider#dispose()
     */
    public void dispose() {
        super.dispose();
   		DebugPlugin.getDefault().removeDebugEventListener(this);

    }

}

