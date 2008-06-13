package org.eclipse.cdt.msw.debug.dbgeng;

public enum DebugAttach {

	debugAttachNoninvasive(3);
	
	private int value;
	
	private DebugAttach(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
}
