package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLModuleParent.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:59:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Module;
import com.ibm.debug.model.ModuleEventListener;
import com.ibm.debug.model.ModuleUnloadedEvent;
import com.ibm.debug.model.PartAddedEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

public class PICLModuleParent extends PICLDebugElement implements ModuleEventListener {

	private Object[] fExpandedElements = null;

	/**
	 * Constructor for PICLModuleParent
	 */
	public PICLModuleParent(IDebugElement parent) {
		super(parent, PICLDebugElement.MODULE_PARENT);
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
		// loop through the children and remove this object as a listener
		IDebugElement[] modules = null;
		try {
			modules = getChildren();
		} catch(DebugException de) {
			return;
		}
		for (int i=0; i < modules.length; i++)
			((PICLModule)modules[i]).getModule().removeEventListener(this);

	}

	/**
	 * This label is not displayed.
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return "PICLModuleParent label";
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return "PICLModuleParent name";
	}

	/**
	 * Used to add modules to this parent.
	 * @param The module that is to be added
	 */
	public void addModule(Module module) {
		module.addEventListener(this);   // listen for module unload events
        addChild(new PICLModule(this,module));
	}


	/**
	 * @see ModuleEventListener#partAdded(PartAddedEvent)
	 */
	public void partAdded(PartAddedEvent event) {
		// ignore... handled by the PICLModule
	}

	/**
	 * @see ModuleEventListener#moduleUnloaded(ModuleUnloadedEvent)
	 */
	public void moduleUnloaded(ModuleUnloadedEvent event) {
		PICLUtils.logEvent("module unloaded",this);

		// find the PICLModule that must be removed
		Module module = event.getModule();

		IDebugElement[] modules = null;
		try {
			modules = getChildren();
		} catch(DebugException de) {
			return;
		}
		for (int i=0; i < modules.length; i++) {
			if (((PICLModule)modules[i]).getModule().equals(module)) {
				removeChild(modules[i]);
			}
		}
	}

	/**
	 * Saves tree expanded elements
	 * @param expanded elements
	 */
	public void saveExpandedElements(Object[] elements) {
		fExpandedElements = elements;
	}

	/**
	 * Returns tree expanded elements
	 * @return expanded elements
	 */
	public Object[] getExpandedElements() {
		return fExpandedElements;
	}

}

