package org.eclipse.cdt.msw.debug.core.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.msw.debug.core.Activator;
import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.DebugConstants;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugBreakpoint;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugControl;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

public class WinDebugTarget extends DebugElement implements IDebugTarget {

	private final String name;
	private final ILaunch launch;
	private final WinProcess process;
	private final List<WinDebugThread> threads = new LinkedList<WinDebugThread>();
	private final Map<String, Integer> bpMap = new HashMap<String, Integer>();
	
	private boolean suspended = false;
	private boolean canSuspend = true;
	private boolean terminated = false;
	
	private final IBreakpointsListener bpListener = new IBreakpointsListener() {
		@Override
		public void breakpointsAdded(final IBreakpoint[] breakpoints) {
			final WinDebugController controller = WinDebugController.getController();
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					IDebugControl control = controller.getDebugControl();
					for (IBreakpoint breakpoint : breakpoints) {
						if (breakpoint instanceof ICLineBreakpoint) {
							ICLineBreakpoint bp = (ICLineBreakpoint)breakpoint;
							try {
								// TODO should be using URI here, but we're only supporting local debug for now
								String fileName = bp.getMarker().getResource().getLocation().toOSString();
								int lineNumber = bp.getLineNumber() + 1;
								String offsetExpr = '`' + fileName + ':' + lineNumber + '`';
								
								if (!bpMap.containsKey(offsetExpr)) {
									IDebugBreakpoint winbp = control.addBreakpoint(
											DebugConstants.DEBUG_BREAKPOINT_CODE,
											DebugConstants.DEBUG_ANY_ID);
								
									winbp.setOffsetExpression(offsetExpr);
									winbp.addFlags(DebugConstants.DEBUG_BREAKPOINT_ENABLED);
									bpMap.put(offsetExpr, winbp.getId());
								}
							} catch (CoreException e) {
								e.printStackTrace();
							} catch (HRESULTException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		}
		
		@Override
		public void breakpointsChanged(IBreakpoint[] breakpoints,
				IMarkerDelta[] deltas) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void breakpointsRemoved(final IBreakpoint[] breakpoints,	IMarkerDelta[] deltas) {
			final WinDebugController controller = WinDebugController.getController();
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					IDebugControl control = controller.getDebugControl();
					for (IBreakpoint breakpoint : breakpoints) {
						if (breakpoint instanceof ICLineBreakpoint) {
							ICLineBreakpoint bp = (ICLineBreakpoint)breakpoint;
							try {
								String fileName = bp.getFileName();
								int lineNumber = bp.getLineNumber();
								String offsetExpr = '`' + fileName + ':' + lineNumber + '`';
								Integer id = bpMap.get(offsetExpr);
								if (id != null) {
									IDebugBreakpoint dbp = control.getBreakpointById(id);
									control.removeBreakpoint(dbp);
								}
							} catch (CoreException e) {
								e.printStackTrace();
							} catch (HRESULTException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		}
	};
	
	public WinDebugTarget(String name, ILaunch launch, WinProcess process) {
		super(null);
		this.name = name;
		this.launch = launch;
		this.process = process;
		WinDebugController.getController().addTarget(this);
		IBreakpointManager bpManager = DebugPlugin.getDefault().getBreakpointManager();
		bpManager.addBreakpointListener(bpListener);
		bpListener.breakpointsAdded(bpManager.getBreakpoints());
	}

	public long getProcessHandle() {
		return process.getHandle();
	}
	
	public void addThread(WinDebugThread thread) {
		threads.add(thread);
	}
	
	public void removeThread(WinDebugThread thread) {
		threads.remove(thread);
	}

	public WinDebugThread getThread(long handle) {
		for (WinDebugThread thread : threads) {
			if (thread.getHandle() == handle)
				return thread;
		}
		return null;
	}
	
	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}
	
	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public IProcess getProcess() {
		return process;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return threads.toArray(new IThread[threads.size()]);
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return !threads.isEmpty();
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		// TODO - not sure anyone even calls this
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		bpListener.breakpointsAdded(new IBreakpoint[] { breakpoint });
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		bpListener.breakpointsChanged(new IBreakpoint[] { breakpoint }, new IMarkerDelta[] { delta });
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		bpListener.breakpointsRemoved(new IBreakpoint[] { breakpoint }, new IMarkerDelta[] { delta });
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsStorageRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canResume() {
		return suspended;
	}

	@Override
	public boolean canSuspend() {
		return canSuspend;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public void resume() throws DebugException {
		if (suspended) {
			final WinDebugController controller = WinDebugController.getController();
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					controller.go(true);
					for (WinDebugTarget target : controller.getTargets())
						target.suspended(false, DebugEvent.CLIENT_REQUEST);
				}
			});
		}
	}

	@Override
	public void suspend() throws DebugException {
		if (canSuspend) {
			canSuspend = false;
			final WinDebugController controller = WinDebugController.getController();
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					controller.go(false);
					for (WinDebugTarget target : controller.getTargets())
						target.suspended(true, DebugEvent.CLIENT_REQUEST);
				}
			});
		}
	}
	
	public void suspended(boolean suspended, int detail) {
		this.suspended = suspended;
		canSuspend = !suspended;
		if (suspended)
			fireSuspendEvent(detail);
		else
			fireResumeEvent(detail);
		
		for (WinDebugThread thread : threads)
			thread.suspended(suspended, detail);
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
		process.terminate();
	}

	@Override
	public boolean canDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public void exitProcess(int exitCode) {
		terminated = true;
		process.terminated(exitCode);
		WinDebugThread deadThreads[] = threads.toArray(new WinDebugThread[threads.size()]);
		for (WinDebugThread thread : deadThreads)
			thread.exitThread(exitCode);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(bpListener);
		WinDebugController.getController().removeTarget(this);
		fireTerminateEvent();
	}
	
}
