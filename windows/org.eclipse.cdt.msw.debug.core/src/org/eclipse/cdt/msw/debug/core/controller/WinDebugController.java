/**
 * 
 */
package org.eclipse.cdt.msw.debug.core.controller;

import java.util.LinkedList;

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
	
	private LinkedList<Runnable> commandQueue = new LinkedList<Runnable>();
	private boolean go = false;
	private IDebugClient debugClient;
	private IDebugControl debugControl;
	
	public static WinDebugController getController() {
		synchronized (mutex) {
			if (controller == null) {
				controller = new WinDebugController();
				controller.start();
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			// TODO uh, oh
		}
		
		// let the starter go
		synchronized (mutex) {
			mutex.notify();
		}
		
		while (true) {
			if (go) {
				try {
					debugControl.waitForEvent(0, IDebugControl.INFINITE);
				} catch (HRESULTException e) {
					if (e.getHRESULT() == HRESULTException.E_UNEXPECTED) {
						// No more targets, set go to false to make sure or we'll be looping for a while
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
