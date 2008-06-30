package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStackFrame;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSymbolGroup;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSymbols;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSystemObjects;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

public class WinDebugStackFrame extends DebugElement implements IStackFrame {

	private final WinDebugThread thread;
	private final DebugStackFrame frame;
	
	private WinDebugVariable[] variables;

	private Object fileMutex;
	private String fileName;
	private int lineNumber = -2;
	
	public WinDebugStackFrame(WinDebugThread thread, DebugStackFrame frame) {
		super(thread.getDebugTarget());
		this.thread = thread;
		this.frame = frame;
		thread.addStackFrame(this);
		fireCreationEvent();
	}
	
	@Override
	public int getCharEnd() throws DebugException {
		return -1;
	}

	@Override
	public int getCharStart() throws DebugException {
		return -1;
	}

	public String getFileName() {
		if (fileName == null && fileMutex == null) {
			final WinDebugController controller = WinDebugController.getController();
			fileMutex = new Object();
			synchronized (fileMutex) {
				controller.enqueueCommand(new Runnable() {
					@Override
					public void run() {
						IDebugSymbols symbols = controller.getDebugSymbols();
						try {
							fileName = symbols.getFileByOffset(frame.getInstructionOffset());
						} catch (HRESULTException e) {
							if (e.getHRESULT() != HRESULTException.E_FAIL)
								// get this when the file name isn't found
								e.printStackTrace();
						}
						synchronized (fileMutex) {
							fileMutex.notify();
						}
					}
				});
				try {
					fileMutex.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return fileName;
	}
	
	@Override
	public int getLineNumber() throws DebugException {
		if (lineNumber < -1) {
			final WinDebugController controller = WinDebugController.getController();
			lineNumber = -1;
			final Object mutex = new Object();
			synchronized (mutex) {
				controller.enqueueCommand(new Runnable() {
					@Override
					public void run() {
						IDebugSymbols symbols = controller.getDebugSymbols();
						try {
							lineNumber = symbols.getLineByOffset(frame.getInstructionOffset()) - 1;
						} catch (HRESULTException e) {
							e.printStackTrace();
						}
						synchronized (mutex) {
							mutex.notify();
						}
					}
				});
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return lineNumber;
	}

	@Override
	public String getName() throws DebugException {
		final String[] name = new String[1];
		final WinDebugController controller = WinDebugController.getController();
		synchronized (name) {
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					IDebugSymbols symbols = controller.getDebugSymbols();
					try {
						name[0] = symbols.getNameByOffset(frame.getInstructionOffset());
//						DebugModuleAndId id = symbols.getSymbolEntryByOffset(frame.getInstructionOffset(), 0);
//						if (id != null)
//							name[0] = symbols.getSymbolEntryString(id, 0);
					} catch (HRESULTException e) {
						e.printStackTrace();
					}
					synchronized (name) {
						name.notify();
					}
				}
			});
			try {
				name.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String n = (name[0] == null) ? "<unknown>" : name[0];
		n += " (0x" + Long.toHexString(frame.getInstructionOffset()) + ")";
		String fileName = getFileName();
		if (fileName != null)
			n += " " + fileName + ":" + getLineNumber();
		return n;
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		// TODO Auto-generated method stub
		return new IRegisterGroup[0];
	}

	@Override
	public IThread getThread() {
		return thread;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		if (variables == null) {
			final WinDebugController controller = WinDebugController.getController();
			final Object mutex = new Object();
			synchronized (mutex) {
				controller.enqueueCommand(new Runnable() {
					@Override
					public void run() {
						IDebugSystemObjects systemObjects = controller.getDebugSystemObjects();
						IDebugSymbols symbols = controller.getDebugSymbols();
						int frameNumber = frame.getFrameNumber();
						try {
							int pid = systemObjects.getProcessIdByHandle(((WinDebugTarget)getDebugTarget()).getProcessHandle());
							systemObjects.setCurrentProcessId(pid);
							systemObjects.setCurrentThreadId(thread.getId());
							symbols.setScopeFrameByIndex(frameNumber);
							IDebugSymbolGroup symbolGroup = symbols.getScopeSymbolGroup(IDebugSymbols.DEBUG_SCOPE_GROUP_ALL, null);
							variables = new WinDebugVariable[symbolGroup.getNumberSymbols()];
							for (int i = 0; i < variables.length; ++i) {
								variables[i] = new WinDebugVariable(WinDebugStackFrame.this, symbolGroup, i);
							}
						} catch (HRESULTException e) {
							e.printStackTrace();
						}
						synchronized (mutex) {
							mutex.notify();
						}
					}
				});
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return variables;
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getVariables().length > 0;
	}

	@Override
	public String getModelIdentifier() {
		return "org.eclipse.cdt.debug.core"; //Activator.PLUGIN_ID;
	}

	@Override
	public boolean canStepInto() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepOver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stepInto() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepOver() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepReturn() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canResume() {
		return thread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return thread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return thread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		thread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		thread.suspend();
	}

	@Override
	public boolean canTerminate() {
		return thread.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return thread.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		thread.terminate();
	}

}
