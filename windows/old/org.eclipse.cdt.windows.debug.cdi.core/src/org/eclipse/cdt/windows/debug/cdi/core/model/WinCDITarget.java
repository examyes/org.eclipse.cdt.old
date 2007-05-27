/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.cdi.core.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget2;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.windows.debug.cdi.core.events.WinCDIDestroyedEvent;
import org.eclipse.cdt.windows.debug.core.sdk.DebugEvent;
import org.eclipse.cdt.windows.debug.core.sdk.DebugProcess;
import org.eclipse.cdt.windows.debug.core.sdk.IDebugEventListener;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDITarget implements ICDITarget2, IDebugEventListener {

	private final WinCDISession session;
	private final WinCDIRuntimeOptions runtimeOptions;
	private final WinCDITargetConfiguration configuration;
	
	private DebugProcess process;
	
	// Temporary for testing
	boolean isTerminated = false;
	
	public WinCDITarget(WinCDISession session, final IPath executable) {
		this.session = session;
		this.runtimeOptions = new WinCDIRuntimeOptions(this);
		this.configuration = new WinCDITargetConfiguration(this);
		
		// DebugProcess thread
		final Object mutex = new Object();
		new Thread() {
			@Override
			public void run() {
				synchronized (mutex) {
					process = new DebugProcess(executable.toOSString(), null, null, null);
					mutex.notifyAll();
				}
				process.addListener(WinCDITarget.this);
				process.eventLoop();
			}
		}.start();
		try {
			synchronized (mutex) {
				if (process == null)
					mutex.wait();
			}
		} catch (InterruptedException e) {
		}
	}
	
	public boolean handleEvent(DebugEvent debugEvent) {
		System.out.println("DebugEvent: " + debugEvent.getDebugEventCode());
		System.out.flush();
		
		switch (debugEvent.getDebugEventCode()) {
		case DebugEvent.EXIT_PROCESS_DEBUG_EVENT:
			// Send destroyed event with no source
			session.eventManager.dispatchEvent(new WinCDIDestroyedEvent());
			break;
		}
		
		return true;
	}
	
	public ICDIGlobalVariableDescriptor[] getGlobalVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDICondition createCondition(int ignoreCount, String expression) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDICondition createCondition(int ignoreCount, String expression,
			String[] threadIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIFunctionLocation createFunctionLocation(String file,
			String function) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIGlobalVariable createGlobalVariable(
			ICDIGlobalVariableDescriptor varDesc) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILineLocation createLineLocation(String file, int line) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIRegister createRegister(ICDIRegisterDescriptor varDesc)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void disconnect() throws CDIException {
		// TODO Auto-generated method stub

	}

	public String evaluateExpressionToString(ICDIStackFrame context,
			String expressionText) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDITargetConfiguration getConfiguration() {
		return configuration;
	}

	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(
			String filename, String function, String name) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public Process getProcess() {
		return process;
	}

	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		// TODO Auto-generated method stub
		return new ICDIRegisterGroup[0];
	}

	public ICDIRuntimeOptions getRuntimeOptions() {
		return runtimeOptions;
	}

	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTerminated() {
		return isTerminated;
	}

	public void jump(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void restart() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void runUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void signal() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void signal(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepInto() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepIntoInstruction() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOver() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOverInstruction() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void terminate() throws CDIException {
		isTerminated = true;
		
		process.destroy();
	}

	public ICDIThread getCurrentThread() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIThread[] getThreads() throws CDIException {
		// TODO Auto-generated method stub
		return new ICDIThread[0];
	}

	public void deleteAllBreakpoints() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(int type,
			ICDIAddressLocation location, ICDICondition condition,
			boolean deferred) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz,
			boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type,
			ICDIFunctionLocation location, ICDICondition condition,
			boolean deferred) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILineBreakpoint setLineBreakpoint(int type,
			ICDILineLocation location, ICDICondition condition, boolean deferred)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType,
			String expression, ICDICondition condition) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void stepInto(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepIntoInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOver(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOverInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume(boolean passSignal) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub

	}

	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

	public void suspend() throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDISignal[] getSignals() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDITarget getTarget() {
		return this;
	}

	public ICDIExpression createExpression(String code) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void destroyAllExpressions() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void destroyExpressions(ICDIExpression[] expressions)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDIExpression[] getExpressions() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIInstruction[] getInstructions(BigInteger startAddress,
			BigInteger endAddress) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIInstruction[] getInstructions(String filename, int linenum)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIInstruction[] getInstructions(String filename, int linenum,
			int lines) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress,
			BigInteger endAddress) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(String filename,
			int linenum) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(String filename,
			int linenum, int lines) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getSourcePaths() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSourcePaths(String[] srcPaths) throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		// TODO Auto-generated method stub
		return new ICDISharedLibrary[0];
	}

	public ICDIMemoryBlock createMemoryBlock(String address, int units,
			int wordSize) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAllBlocks() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDISession getSession() {
		return session;
	}

}
