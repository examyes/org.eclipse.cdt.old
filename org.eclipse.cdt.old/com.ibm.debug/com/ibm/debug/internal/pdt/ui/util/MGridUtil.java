package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/MGridUtil.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:58:34)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class MGridUtil {

	/**
	 * Creates a grid data object that occupies vertical and horizontal
	 * space.
	 */
	static public MGridData createFill() {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= gd.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.verticalAlignment= gd.FILL;
		gd.grabExcessVerticalSpace= true;
		return gd;
	}

	/**
	 * Creates a grid data object that occupies horizontal space.
	 */
	static public MGridData createHorizontalFill() {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= gd.FILL;
		gd.grabExcessHorizontalSpace= true;
		return gd;
	}

	/**
	 * Creates a grid data object that occupies vertical space.
	 */
	static public MGridData createVerticalFill() {
		MGridData gd= new MGridData();
		gd.verticalAlignment= gd.FILL;
		gd.grabExcessVerticalSpace= true;
		return gd;
	}
}
