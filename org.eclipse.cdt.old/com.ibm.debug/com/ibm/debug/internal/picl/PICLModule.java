package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLModule.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 15:59:52)
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

public class PICLModule extends PICLDebugElement implements ModuleEventListener {

	private Module fModule = null;

	/**
	 * Constructor for PICLModule
	 */
	public PICLModule(IDebugElement parent, Module module) {
		super(parent, PICLDebugElement.MODULE);
		fModule = module;
		fModule.addEventListener(this);
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
		if (fModule != null)
			fModule.removeEventListener(this);
	}

	/**
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return fModule.getQualifiedName();
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return fModule.getQualifiedName();
	}

	/**
	 * @see ModuleEventListener#partAdded(PartAddedEvent)
	 */
	public void partAdded(PartAddedEvent event) {
		PICLUtils.logEvent("part added",this);
		addChild(new PICLPart(this,event.getPart()));
	}

	/**
	 * @see ModuleEventListener#moduleUnloaded(ModuleUnloadedEvent)
	 */
	public void moduleUnloaded(ModuleUnloadedEvent event) {
		PICLUtils.logEvent("module unloaded (ignored)",this);
		// ignore because this is handled by the PICLModuleParent
	}

	/**
	 * Gets the module
	 * @return Returns a Module
	 */
	public Module getModule() {
		return fModule;
	}
}

