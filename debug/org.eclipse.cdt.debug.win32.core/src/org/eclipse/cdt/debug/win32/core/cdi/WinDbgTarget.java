/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
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
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.win32.core.dbgeng.IDebugClient;
import org.eclipse.debug.core.ILaunch;

public class WinDbgTarget implements ICDITarget {

	private WinDbgSession session;
	private WinDbgRuntimeOptions runtimeOptions = new WinDbgRuntimeOptions();
	private WinDbgTargetConfiguration configuration = new WinDbgTargetConfiguration();
	private WinDbgProcess process;
	
	static private IDebugClient debugClient = new IDebugClient();
	
	private ICDIThread[] threads = new ICDIThread[0];
	private ICDIRegisterGroup[] registerGroups = new ICDIRegisterGroup[0];
	
	public WinDbgTarget(WinDbgSession session, ILaunch launch, IBinaryObject exe) {
		this.session = session;
		process = new WinDbgProcess(debugClient, launch, exe);
	}
	
	public Process getProcess() {
		return process;
	}

	public ICDITargetConfiguration getConfiguration() {
		return configuration;
	}

	public String evaluateExpressionToString(ICDIStackFrame context,
			String expressionText) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(
			String filename, String function, String name) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIGlobalVariable createGlobalVariable(
			ICDIGlobalVariableDescriptor varDesc) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		return registerGroups;
	}

	public ICDIRegister createRegister(ICDIRegisterDescriptor varDesc)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isTerminated() {
		return process.isTerminated();
	}

	public void terminate() throws CDIException {
		// TODO Auto-generated method stub

	}

	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public void disconnect() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void restart() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOver() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepInto() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOverInstruction() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepIntoInstruction() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void runUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void jump(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void signal() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void signal(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDIRuntimeOptions getRuntimeOptions() {
		return runtimeOptions;
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

	public ICDILineLocation createLineLocation(String file, int line) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIFunctionLocation createFunctionLocation(String file,
			String function) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIThread[] getThreads() throws CDIException {
		return threads;
	}

	public ICDIThread getCurrentThread() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILineBreakpoint setLineBreakpoint(int type,
			ICDILineLocation location, ICDICondition condition, boolean deferred)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type,
			ICDIFunctionLocation location, ICDICondition condition,
			boolean deferred) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(int type,
			ICDIAddressLocation location, ICDICondition condition,
			boolean deferred) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType,
			String expression, ICDICondition condition) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz,
			boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public void deleteAllBreakpoints() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOver(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOverInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepInto(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepIntoInstruction(int count) throws CDIException {
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

	public void suspend() throws CDIException {
		// TODO Auto-generated method stub

	}

	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDISignal[] getSignals() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDITarget getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIExpression createExpression(String code) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIExpression[] getExpressions() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void destroyExpressions(ICDIExpression[] expressions)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public void destroyAllExpressions() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void setSourcePaths(String[] srcPaths) throws CDIException {
		// TODO Auto-generated method stub

	}

	public String[] getSourcePaths() throws CDIException {
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

	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIMemoryBlock createMemoryBlock(String address, int units,
			int wordSize) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public void removeAllBlocks() throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDISession getSession() {
		return session;
	}

}
