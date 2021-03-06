/**
 * 
 */
package org.eclipse.cdt.msw.debug.core.controller;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.msw.debug.core.model.WinDebugEventCallbacks;
import org.eclipse.cdt.msw.debug.core.model.WinDebugTarget;
import org.eclipse.cdt.msw.debug.dbgeng.DebugInterrupt;
import org.eclipse.cdt.msw.debug.dbgeng.DebugObjectFactory;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugClient;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugControl;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSymbols;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSystemObjects;
import org.eclipse.cdt.msw.debug.dbgeng.SymOpt;

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
	private IDebugSystemObjects debugSystemObjects;
	private IDebugSymbols debugSymbols;
	private WinDebugEventCallbacks debugEventCallbacks;
	
	private List<WinDebugTarget> targets = new LinkedList<WinDebugTarget>();

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
	
	public WinDebugController() {
		super("Windows Debugger");
	}
	
	public void addTarget(WinDebugTarget target) {
		synchronized (targets) {
			targets.add(target);
		}
	}
	
	public void removeTarget(WinDebugTarget target) {
		synchronized (targets) {
			targets.remove(target);
		}
	}
	
	public WinDebugTarget[] getTargets() {
		synchronized (targets) {
			return targets.toArray(new WinDebugTarget[targets.size()]);
		}
	}
	
	public void enqueueCommand(Runnable command) {
		synchronized (commandQueue) {
			commandQueue.add(command);
			commandQueue.notify();
			try {
				controller.getDebugControl().setInterrupt(DebugInterrupt.ACTIVE);
			} catch (HRESULTException e) {
				if (e.getHRESULT() != HRESULTException.E_UNEXPECTED)
					// We'll get unexpected if the target is isn't ready which is OK.
					e.printStackTrace();
			}
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
	
	public IDebugSystemObjects getDebugSystemObjects() {
		return debugSystemObjects;
	}
	
	public WinDebugEventCallbacks getDebugEventCallbacks() {
		return debugEventCallbacks;
	}
	
	public IDebugSymbols getDebugSymbols() {
		return debugSymbols;
	}
	
	public void go(boolean go) {
		this.go = go;
	}
	
	@Override
	public void run() {
		try {
			// create the debug objects
			debugClient = DebugObjectFactory.createClient();
			debugControl = DebugObjectFactory.createControl();
			debugSystemObjects = DebugObjectFactory.createSystemObjects();
			debugSymbols = DebugObjectFactory.createSymbols();
			
			// register our callbacks
			debugEventCallbacks = new WinDebugEventCallbacks();
			debugClient.setEventCallbacks(debugEventCallbacks);
			
			// set the symbol options
			int symbolOptions = debugSymbols.getSymbolOptions();
			symbolOptions &= ~SymOpt.DEFERRED_LOADS;
			symbolOptions |= SymOpt.LOAD_LINES;
			debugSymbols.setSymbolOptions(symbolOptions);
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
					// prepare for next go
					debugControl.getInterrupt();
				} catch (HRESULTException e) {
					if (e.getHRESULT() == HRESULTException.E_UNEXPECTED) {
						// No more targets, set go to false to make sure or we'll be looping for a while
						go = false;
					} else if (e.getHRESULT() == HRESULTException.E_PENDING) {
						// No worries, this usually only happens when we interrupt the wait to run a command
						// Do nothing
					} else
						e.printStackTrace();
				}
			}
			
			Runnable command = dequeueCommand(!go);
			while (command != null) {
				command.run();
				command = dequeueCommand(!go);
			}
		}
	}
	
}
