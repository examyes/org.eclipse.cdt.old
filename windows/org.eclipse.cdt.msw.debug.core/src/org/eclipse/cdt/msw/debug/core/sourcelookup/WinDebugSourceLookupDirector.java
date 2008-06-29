package org.eclipse.cdt.msw.debug.core.sourcelookup;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

public class WinDebugSourceLookupDirector extends AbstractSourceLookupDirector {

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new WinDebugSourceLookupParticipant() });
	}

}
