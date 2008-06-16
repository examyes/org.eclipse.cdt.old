package org.eclipse.cdt.msw.debug.dbgeng;

public class DebugObject {

	final long p;

	protected DebugObject(long p) {
		if (p == 0)
			throw new NullPointerException();
		this.p = p;
	}

}
