package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IStorageModification.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:01:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;

/**
 * Interface describing support for modification of a variable's
 * value(s) in a debug target.
 */
public interface IStorageModification {

	/**
	 * Attempts to set the value of the storage to the
	 * new value.
	 */
	public void setRawStorageValue(String newValue) throws DebugException;
	/**
	 * Attempts to set the value of the storage to the
	 * new value.
	 */
	public void setTranslatedStorageValue(String newValue) throws DebugException;

	/**
	 * Returns <code>true</code> if the modification of raw storage
	 * is allowed.
	 */
	public boolean supportsRawStorageModification();

	/**
	 * Returns <code>true</code> if the modification of translated
	 * storage is allowed.
	 */
	public boolean supportsTranslatedStorageModification();

	/**
	 * Returns <code>true</code> if the new value is
	 * valid for this type of storage.  This will fail if the storage
	 * style is int and the user enters abc.
	 */
	public boolean verifyRawStorageValue(String newValue);
	/**
	 * Returns <code>true</code> if the new value is
	 * valid for this type of variable.  This should almost always work.
	 * This is a placeholder for now.
	 */
	public boolean verifyTranslatedStorageValue(String newValue);


}

