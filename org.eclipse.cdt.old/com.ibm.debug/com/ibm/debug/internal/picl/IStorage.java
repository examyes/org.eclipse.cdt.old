package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IStorage.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:01:20)
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
 * An <code>IStorage</code> represents a storage block  in
 * a debug target.
 */
public interface IStorage extends IDebugElement, IStorageModification {

	/**
	 * Returns the type of this element, encoded as an integer -
	 * <code>IDebugElementTypeConstants.STORAGE</code>.
	 *
	 * @see com.ibm.dt.core.IDebugElementTypeConstants
	 * @see IDebugElement
	 */
	int getElementType();
	/**
	 * Returns the <code>IDebugTarget</code> this storage block is
	 * contained in.
	 *
	 * @see IDebugElement
	 */
	IDebugElement getParent();
	/**
	 * Returns the code page that the translated storage block is displayed in.
	 */
	String getCodePage();
	boolean setCodePage(String codePage);
	/**
	 * Returns the style that the raw storage block is displayed in.
	 */
	String getStyle();
	boolean setStyle(String style);
	/**
	 * Returns the IExpression that defines this storage block
	 */
	IDebugElement getExpression();

	/**
	 * Returns the raw storage block.
	 * Need to determine what return type I really want
	 */
	String getRawStorage();

	/**
	 * Returns the translated storage block.
	 * Need to determine what return type I really want
	 */
	String getTranslatedStorage();

	/**
	 * Returns the start address of this storage block.
	 *
	 */
	long getStartAddress();
	boolean setStartAddress(String address);

	/**
	 * Returns the length of this storage block.
	 *
	 */
	long getLength();
	boolean setLength(String address);

}