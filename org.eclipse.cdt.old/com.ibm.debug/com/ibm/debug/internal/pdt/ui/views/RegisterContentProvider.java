package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/RegisterContentProvider.java, eclipse, eclipse-dev, 20011128
// Version 1.8 (last modified 11/28/01 15:59:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.BasicContentProvider;
import org.eclipse.debug.internal.ui.DebugUIUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;

import com.ibm.debug.internal.picl.IRegister;
import com.ibm.debug.internal.picl.IRegisterGroup;
import com.ibm.debug.internal.picl.PICLRegisterGroupParent;

/**
 * Provide the contents for a register viewer.
 */
public class RegisterContentProvider extends BasicContentProvider implements IDebugEventListener, ITreeContentProvider {
	/**
	 * Constructs a new provider
	 */
	public RegisterContentProvider() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	/**
	 * Unregisters this content provider from the debug plugin so that
	 * this object can be garbage-collected.
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		super.dispose();
	}
	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 * <code>IRegister</code>s will return an empty set.
	 * <code>IRegisterGroup</code>s will return an array of IRegisters.
	 * <code>PICLRegisterGroupParent</code>s will return an array of IRegisterGroups.
	 */
	protected Object[] doGetChildren(Object parent) {

		try {
			if (parent instanceof IRegisterGroup)
			{
				if(!((IRegisterGroup)parent).isMonitored() )
				{
					return ((IRegisterGroup)parent).startMonitoringRegisterGroup();
				}
				else return ((IDebugElement) parent).getChildren();
			}
			else if (parent instanceof PICLRegisterGroupParent)
			{
				return ((IDebugElement) parent).getChildren();
			}
		} catch (DebugException de) {
			DebugUIUtils.logError(de);
		}
		return new Object[0];
	}

	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
				refresh();
				break;
			case DebugEvent.CHANGE:
				refresh(event.getSource());
				break;
		}
	}


	/**
	 * Returns the children for the given <code>IDebugElement</code>.
	 * @see BasicContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	/**
	 * Returns the parent for the given <code>IDebugElement</code>.
	 * @see ITreeContentProvider
	 */
	public Object getParent(Object item) {
		Object parent = null;
  		if (item instanceof IRegister || item instanceof IRegisterGroup) {
    	    parent = ((IDebugElement) item).getParent();
  		}

        if (parent instanceof IValue) {
			parent = ((IValue)parent).getVariable();
        }

        return parent;

	}
	/**
	 * Returns whether the object has children. By definition, registers do not have children,
	 * Register groups and register group parents always have children.  They are hard coded here
	 * because the model will not know of these children until they are monitored.
	 * @see ITreeContentProvider
	 */
	public boolean hasChildren(Object item) {
		try {
			if (item instanceof IRegisterGroup || item instanceof PICLRegisterGroupParent) {
				return true; //((IRegisterGroup)item).hasChildren();
			} else if(item instanceof IRegister) {
				return false; //((IRegister)item).getValue().hasChildren();
			} else {
				return ((IDebugElement) item).hasChildren();
			}
		} catch (DebugException de) {
			return false;
		}
	}

	
}



