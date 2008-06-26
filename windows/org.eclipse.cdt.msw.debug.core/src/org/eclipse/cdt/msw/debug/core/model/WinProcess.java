package org.eclipse.cdt.msw.debug.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugClient;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSystemObjects;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

public class WinProcess implements IProcess {

	private String label;
	private ILaunch launch;
	private long handle;
	private Map<String, String> attributes = new HashMap<String, String>();
	private boolean canTerminate = true;
	private boolean terminated = false;
	private int exitCode = 99;
	
	public WinProcess(String command, ILaunch launch, long handle) {
		label = command;
		this.launch = launch;
		this.handle = handle;
		created();
	}
	
	public long getHandle() {
		return handle;
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
		return canTerminate;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void terminate() throws DebugException {
		canTerminate = false;
		final WinDebugController controller = WinDebugController.getController();
		controller.enqueueCommand(new Runnable() {
			@Override
			public void run() {
				try {
					IDebugSystemObjects systemObjects = controller.getDebugSystemObjects();
					int pid = systemObjects.getProcessIdByHandle(handle);
					systemObjects.setCurrentProcessId(pid);
					
					IDebugClient client = controller.getDebugClient();
					client.terminateCurrentProcess();
				} catch (HRESULTException e) {
					e.printStackTrace();
				}
			}
		});
	}

	void created() {
		DebugEvent event = new DebugEvent(this, DebugEvent.CREATE);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
	}
	
	void terminated(int exitCode) {
		terminated = true;
		canTerminate = false;
		this.exitCode = exitCode;
		DebugEvent event = new DebugEvent(this, DebugEvent.TERMINATE);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
	}
	
}
