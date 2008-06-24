/**
 * 
 */
package org.eclipse.cdt.msw.debug.core.controller;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.msw.debug.dbgeng.DebugObjectFactory;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugClient;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugControl;

/**
 * @author DSchaefe
 *
 */
public class WinDebugController extends Thread {

	private static Object mutex = new Object();
	private static WinDebugController controller;
	
	private List<IWinDebugListener> listeners = new LinkedList<IWinDebugListener>();
	private LinkedList<Runnable> commandQueue = new LinkedList<Runnable>();
	private boolean go = false;
	private IDebugClient debugClient;
	private IDebugControl debugControl;
	
	public static WinDebugController getController() {
		synchronized (mutex) {
			if (controller == null) {
				controller = new WinDebugController();
				controller.start();
			}
			return controller;
		}
	}
	
	public void enqueueCommand(Runnable command) {
		synchronized (commandQueue) {
			commandQueue.add(command);
			commandQueue.notify();
		}
	}
	
	private Runnable dequeueCommand(boolean wait) {
		synchronized (commandQueue) {
			if (wait) {
				while (commandQueue.isEmpty())
					try {
						commandQueue.wait();
					} catch (InterruptedException e) {
					}
			} else
				if (commandQueue.isEmpty())
					return null;
			return commandQueue.removeFirst();
		}
	}
	
	public void addListener(IWinDebugListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(IWinDebugListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private void fireEvent(WinDebugEventType type) {
		List<IWinDebugListener> l = new LinkedList<IWinDebugListener>();
		synchronized (listeners) {
			l.addAll(listeners);
		}
		for (IWinDebugListener listener : l)
			listener.handleEvent(type, this);
	}
	
	public IDebugClient getDebugClient() {
		return debugClient;
	}
	
	public IDebugControl getDebugControl() {
		return debugControl;
	}
	
	public void go() {
		go = true;
	}
	
	@Override
	public void run() {
		try {
			debugClient = DebugObjectFactory.createClient();
			debugControl = DebugObjectFactory.createControl();
		} catch (HRESULTException e) {
			
		}
		
		while (true) {
			if (go) {
				try {
					debugControl.waitForEvent(0, IDebugControl.INFINITE);
				} catch (HRESULTException e) {
					if (e.getHRESULT() == HRESULTException.E_UNEXPECTED) {
						// No more targets
						fireEvent(WinDebugEventType.TERMINATED);
						go = false;
					} else
						e.printStackTrace();
				}
			}
			
			// TODO Send out notifications of why we stopped??
			
			Runnable command = dequeueCommand(!go);
			while (command != null) {
				command.run();
				command = dequeueCommand(!go);
			}
		}
	}
	
}
