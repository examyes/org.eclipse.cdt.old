package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/BreakpointModifyRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:00:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Breakpoint;
import java.io.IOException;
import org.eclipse.core.resources.IMarker;

public abstract class BreakpointModifyRequest extends BreakpointRequest {

	protected final String msgKey = super.msgKey + "modify.";

	protected Breakpoint fBreakpoint = null;
	public BreakpointModifyRequest(
		PICLDebugTarget debugTarget,
		Breakpoint breakpoint) {
		super(debugTarget);

		fBreakpoint = breakpoint;
	}

	protected boolean updateEnableSetting(IMarker marker) {

		try {
			boolean enableSetting =
				getDebugTarget().getBreakpointManager().isEnabled(marker);
			if (enableSetting != fBreakpoint.isEnabled())
				if (enableSetting)
					fBreakpoint.enable(syncRequest());
				else
					fBreakpoint.disable(syncRequest());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}