package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IRegister.java, eclipse, eclipse-dev, 20011128
// Version 1.10 (last modified 11/28/01 16:01:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;

/**
 * An <code>IRegister</code> represents a register in
 * a debug target. A register's parent will be an <code>IRegisterGroup</code>.
 * A Register is always a leaf on the tree, with a parent IRegisterGroup which
 * belongs to the stack frame in which it is visible.
 *
 * <p>The contents of a register are represented by an @see IVariable.
 *
 * <p>An implementation may choose to re-use or discard
 * registers on iterative thread suspensions. Clients
 * cannot assume that registers are identical or equal across
 * iterative thread suspensions and must check for equality on iterative
 * suspensions if they wish to re-use the objects.
 *
 * <p>An implementation that preserves equality
 * across iterative suspensions may display more desirable behavior in
 * some clients. For example, if registers are preserved
 * while stepping, a UI client would be able to update the UI incrementally,
 * rather than collapse and redraw the entire list or tree.  It can also
 * highlight those registers which have changed since the last iteration.
 */
public interface IRegister extends IVariable, IValueModification {

	/**
	 * Check if the register has changed in value and should be highlighted.
	 * @param Reset the changed flag after returning its current value.
	 * Once a register has changed it will continue to return true until it is reset.
	 */
	boolean hasChanged(boolean reset);



}


