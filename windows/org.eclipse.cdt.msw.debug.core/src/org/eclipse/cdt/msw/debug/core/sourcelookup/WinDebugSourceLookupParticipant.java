package org.eclipse.cdt.msw.debug.core.sourcelookup;

import org.eclipse.cdt.msw.debug.core.model.WinDebugStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;

public class WinDebugSourceLookupParticipant extends AbstractSourceLookupParticipant {

	@Override
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof WinDebugStackFrame)
			return ((WinDebugStackFrame)object).getFileName();
		else
			return null;
	}

}
