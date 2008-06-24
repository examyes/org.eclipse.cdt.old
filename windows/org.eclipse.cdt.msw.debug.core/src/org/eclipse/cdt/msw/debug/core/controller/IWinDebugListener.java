package org.eclipse.cdt.msw.debug.core.controller;

public interface IWinDebugListener {

	public void handleEvent(WinDebugEventType type, WinDebugController controller);
	
}
