/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core.cdi;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
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
import org.eclipse.cdt.windows.debug.core.Activator;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugBreakpoint;
import org.eclipse.cdt.windows.debug.core.IDebugControl;
import org.eclipse.cdt.windows.debug.core.engine.DebugEngine;
import org.eclipse.cdt.windows.debug.core.engine.ResumeCommand;
import org.eclipse.cdt.windows.debug.core.engine.SetBreakpointCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDITarget implements ICDITarget {

	private final WinCDISession session;
	private final WinCDIRuntimeOptions runtimeOptions = new WinCDIRuntimeOptions(this);
	private final WinCDITargetConfiguration config = new WinCDITargetConfiguration(this);
	
	// There is a default thread that we don't get an event when created
	private WinCDIThread[] threads = new WinCDIThread[] { new WinCDIThread(this) };
	
	private List<ICDIBreakpoint> breakpoints = new ArrayList<ICDIBreakpoint>();
	
	private final DebugEngine debugEngine;
	
	public WinCDITarget(WinCDISession session, ILaunch launch,
			File executable) throws CoreException {
		this.session = session;
		String commandLine;
		try {
			commandLine = executable.getCanonicalPath();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to get executable path", e));
		}
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		String initialDirectory = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, ".");
		Map<String, String> environment = new HashMap<String, String>();
		debugEngine = new DebugEngine(commandLine, initialDirectory, environment);
		debugEngine.schedule();
	}
	
	public DebugEngine getDebugEngine() {
		return debugEngine;
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
		return new WinCDIFunctionLocation(file, function);
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
		return config;
	}

	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(
			String filename, String function, String name) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public Process getProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		// TODO create any register gruops we need
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
		// TODO Auto-generated method stub
		return false;
	}

	public void jump(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void restart() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume() throws CDIException {
		debugEngine.scheduleCommand(new ResumeCommand());
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
		// TODO Auto-generated method stub

	}

	public ICDIThread getCurrentThread() throws CDIException {
		// TODO Auto-generated method stub
		return threads[0];
	}

	public ICDIThread[] getThreads() throws CDIException {
		return threads;
	}

	public void deleteAllBreakpoints() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints)
			throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		return breakpoints.toArray(new ICDIBreakpoint[breakpoints.size()]);
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
		final WinCDIFunctionBreakpoint bp
			= new WinCDIFunctionBreakpoint(this, type, location, condition, deferred);
		debugEngine.scheduleCommand(new SetBreakpointCommand(location.getFunction()) {
			@Override
			public int run(DebugEngine engine) {
				int hr = super.run(engine);
				if (!HRESULT.FAILED(hr))
					bp.setDebugBreakpoint(getBreakpoint());
				return hr;
			}
		});
		breakpoints.add(bp);
		return bp;
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
		// No signals here...
		resume();
	}

	public void resume(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub

	}

	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return true;
	}

	public void suspend() throws CDIException {
		// TODO Auto-generated method stub

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
		// TODO create shared libraries
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
