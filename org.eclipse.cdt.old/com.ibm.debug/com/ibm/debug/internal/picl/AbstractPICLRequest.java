package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/AbstractPICLRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:57:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Provides common behavior for requests:<ul>
 * <li>isImmediate
 * <li>isSynchronous
 * </ul>
 */

public abstract class AbstractPICLRequest implements IPICLRequest {

	/**
	 * Request mode
	 */
	protected int fMode= 0;

    /**
     * Flag to indicate if request was internal.
     */
    protected boolean fIsInternal;

	/**
	 * Creates a request with the given synchronous and immediate
	 * options.
	 */
	public AbstractPICLRequest(int mode) {
		super();
		fMode= mode;
	}

	/**
	 * Creates a request with the given synchronous and immediate
	 * options, and internal request setting.
	 */
	public AbstractPICLRequest(int mode, boolean isInternal) {
        this(mode);
        fIsInternal = isInternal;
	}

	/**
	 *
	 * @return int
	 */
	public int getMode() {
		return fMode;
	}

    /**
     * Returns if this request is internal or not.
     * @return boolean
     */
    public boolean isInternal() {
        return fIsInternal;
    }

    /**
     * Sets the is internal flag indicating whether this is an internal
     * request or not.  The default is false (not internal).
     * @parm isInternal set the is internal flag to this value
     */
    public void setInternal(boolean isInternal) {
        fIsInternal = isInternal;
    }
}
