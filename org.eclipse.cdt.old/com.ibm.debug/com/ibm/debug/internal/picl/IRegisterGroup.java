package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IRegisterGroup.java, eclipse, eclipse-dev, 20011128
// Version 1.10 (last modified 11/28/01 16:01:18)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.model.IDebugElement;

/**
 * An <code>IRegisterGroup</code> represents a group of registers in
 * a debug target. A register group's parent will be an <code>IStackFrame</code>.
 * It may have children of type <code>IRegister</code>.
 * Thus, registers groups are represented by a tree, rooted by
 * the stack frame in which they are visible, with the registers contained in
 * their group as their children. IRegister groups do not have a value. They only
 * have a name. Their purpose is to display the registers in logical groups.
 *
 * <p>An implementation may choose to re-use or discard
 * register groups on iterative suspends. Clients
 * cannot assume that the groups are identical or equal accross
 * iterative suspensions and must check for equality on iterative
 * suspensions if they wish to re-use the objects. However, as long as
 * all stack frames are running on the same platform, the register groups
 * should not change.
 *
 * <p>An implementation that preserves equality
 * across iterative suspensions may display more desirable behavior in
 * some clients. For example, if register groups are preserved
 * while stepping, a UI client would be able to update the UI incrementally,
 * rather than collapse and redraw the entire list or tree.
 *
 * @see IValue
 * @see IStackFrame
 * @see IRegister
 *
 */

public interface IRegisterGroup extends IDebugElement{


	/**
     * Starts monitoring the given register group. Registers are monitored
     * as a group, rather than individually. Returns an array of IRegisters belonging
     * to the group.  This array should be used to add change listeners to each register.
	 */
	IDebugElement[] startMonitoringRegisterGroup();

   /**
     * Stops monitoring the given register group. This should be used
     * when the register group node is collapsed or is no longer visible.
	 */
	void stopMonitoringRegisterGroup();

	/**
	 * Returns whether this group is currently being monitored.
	 */
	public boolean isMonitored();


}