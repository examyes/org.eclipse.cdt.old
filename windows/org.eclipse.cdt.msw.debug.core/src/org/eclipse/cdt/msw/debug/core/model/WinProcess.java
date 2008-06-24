package org.eclipse.cdt.msw.debug.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.DebugCreateProcessOptions;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

public class WinProcess implements IProcess {

	private String label;
	private ILaunch launch;
	private Map<String, String> attributes = new HashMap<String, String>();
	private boolean terminated = false;
	private int exitCode = 99;
	private WinDebugController controller = WinDebugController.getController();
	
	public WinProcess(String command, String[] args, ILaunch launch) {
		label = command;
		this.launch = launch;
		
		StringBuffer cmdLineBuff = new StringBuffer(command);
		if (args != null)
			for (String arg : args) {
				cmdLineBuff.append(' ');
				cmdLineBuff.append(arg);
			}
		
		final String cmdLine = cmdLineBuff.toString(); 
		controller.enqueueCommand(new Runnable() {
			@Override
			public void run() {
				try {
					controller.getDebugClient().createProcess(0, cmdLine,
							DebugCreateProcessOptions.DEBUG_PROCESS);
					controller.go();
				} catch (HRESULTException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public int getExitValue() throws DebugException {
		return exitCode;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean canTerminate() {
		return !terminated;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void terminate() throws DebugException {
		// TODO 
		terminated = true;
	}

	void terminated(int exitCode) {
		terminated = true;
		this.exitCode = exitCode;
		DebugEvent event = new DebugEvent(this, DebugEvent.TERMINATE);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
	}
	
}
