/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;
import org.eclipse.cdt.debug.gdbPicl.objects.*;
import org.eclipse.cdt.debug.gdbPicl.gdbCommands.*;

import java.util.*;
import java.net.*;
import java.io.*;
import java.text.*;

import com.ibm.debug.epdc.*;
import com.ibm.debug.epdc.ECPLog;
import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.epdc.ERepCommandLog;
import com.ibm.debug.epdc.EStdCmdLogLine;
import com.ibm.debug.util.Semaphore;
import com.ibm.debug.util.Platform;

public class GdbDebugSession extends DebugSession {
	private boolean echoInternalCommands = false;
	private boolean IdeFilesAndMethods = true;
	//see getMethodsForAllParts() and updateAllParts() and getPartMethods()
	public static final int MAX_GDB_LINES = 50000;

	public GdbCommandAndResponse _gdbCommandAndResponse = null;
	public GetGdbBreakpoints _getGdbBreakpoints = null;
	public GetGdbFile _getGdbFile = null;
	public GetGdbLocals _getGdbLocals = null;
	public GetGdbModuleParts _getGdbModuleParts = null;
	public GetGdbPartMethods _getGdbPartMethods = null;
	public GetGdbRegisters _getGdbRegisters = null;
	public GetGdbStorage _getGdbStorage = null;
	public GetGdbSharedLibraries _getGdbSharedLibraries = null;
	public GetGdbThreads _getGdbThreads = null;
	public GdbExceptions _gdbExceptions = null;
	private GdbProcess gdbProcess = null;
	public GdbProcess getGdbProcess() {
		return gdbProcess;
	}
	public String prefix = ""; //"GdbDebugSession> ";
	public Vector uiMessages = new Vector();
	public Vector cmdResponses = new Vector();
	public Vector monitorChangedID = new Vector();
	public Vector monitorChangedName = new Vector();
	public Vector monitorChangedValue = new Vector();

	// CURRENT LOGICAL POSITION ///////////////////////////////////////////////
	private String _currentLineNumber = "";
	private String _currentFileName = "";
	private String _currentFunctionName = "";
	private String _currentFrameAddress = "";
	private int _currentModuleID = 0;
	private String _currentModuleName = "";
	private int _currentPartID = 0;
	public String getCurrentFileName() {
		return _currentFileName;
	}
	public String getCurrentLineNumber() {
		return _currentLineNumber;
	}
	public int getCurrentModuleID() {
		return _currentModuleID;
	}
	public void setCurrentModuleID(int currentModuleID) {
		_currentModuleID = currentModuleID;
	}
	public String getCurrentFunctionName() {
		return _currentFunctionName;
	}
	public String getCurrentFrameAddress() {
		return _currentFrameAddress;
	}
	public void setCurrentFileLineModule(
		String currentLineNumber,
		String currentFileName,
		String currentFunctionName,
		String currentFrameAddress,
		int currentModuleID,
		String currentModuleName,
		int currentPartID) {
		_currentLineNumber = currentLineNumber;
		_currentFileName = currentFileName;
		_currentFunctionName = currentFunctionName;
		_currentFrameAddress = currentFrameAddress;
		_currentModuleID = currentModuleID;
		_currentModuleName = currentModuleName;
		_currentPartID = currentPartID;
	}

	private int _currentThreadID = 0;
	public int getCurrentThreadID() {
		return _currentThreadID;
	}
	public void setCurrentThreadID(int ID) {
		_currentThreadID = ID;
	}

	GdbDebugEngine _gdbDebugEngine = null;
	public GdbDebugSession(GdbDebugEngine debugEngine) {
		super(debugEngine);
		_gdbDebugEngine = debugEngine;

		String gdbPath = _gdbDebugEngine.getGdbPath();
		if (gdbPath != null && gdbPath != "") {
			gdbPath += "/";
		}

		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(1, "GdbDebugSession constructor");

		String shell = "cmd.exe /C ";
		if (Platform.isUnix())
			shell = "";
		try {
			String cmd;
			if (gdbPath != null) {
				cmd = shell + " " + gdbPath + "gdb -nw ";
			} else {
				cmd = shell + " gdb -nw ";
			}

			if (Gdb.traceLogger.EVT)
				Gdb.traceLogger.evt(
					1,
					"================ GdbDebugSession, will create GdbProcess with command=" + cmd);
			gdbProcess = new GdbProcess(cmd);
		} catch (Exception exc) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, getResourceString("UNHANDLED_EXCEPTION") + exc);
			gdbProcess = null;
		}
		if (gdbProcess == null) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, getResourceString("GDB_PROCESS_NULL"));
			return;
		}

		for (int i = 0; i <= GdbProcess.MAXSECONDS; i++) {
			if (gdbProcess.isReady()) {
				if (Gdb.traceLogger.EVT)
					Gdb.traceLogger.evt(
						1,
						"<<<<<<<<======== GdbDebugSession constructor GdbProcess isReady");
				break;
			} else {
				if (Gdb.traceLogger.EVT)
					Gdb.traceLogger.evt(
						2,
						"GdbDebugSession constructor waiting for GdbProcess, will **SLEEP** and retry");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		boolean ready = true;
		while (ready) {
			ready = gdbProcess.isReady();
			if (!ready) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				ready = gdbProcess.isReady();
			}
			if (ready) {
				String line = gdbProcess.readLine();
				if (line != null && !line.equals("") && !line.startsWith(gdbProcess.MARKER)) {
					addLineToUiMessages(line); // reads GDB copyright message
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(3, "GdbDebugSession constructor 'gdb' response=" + line);
				}
			}
		}
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "<<<<<<<<######## GdbDebugSession 'gdb' started");

	}

	public void createManagers() {
		_moduleManager = new GdbModuleManager(this);
		_threadManager = new GdbThreadManager(this);
		_breakpointManager = new GdbBreakpointManager(this);
		_variableMonitorManager = new GdbVariableMonitorManager(this);
		_localVariablesMonitorManager = new GdbLocalVariablesMonitorManager(this);
		_registerManager = new GdbRegisterManager(this);
		_storageManager = new GdbStorageManager(this);

		createGdbCommandObjects();
	}

	private void createGdbCommandObjects() {
		_gdbCommandAndResponse = new GdbCommandAndResponse(this);
		_getGdbThreads = new GetGdbThreads(this);
		_getGdbBreakpoints = new GetGdbBreakpoints(this);
		_getGdbFile = new GetGdbFile(this);
		_getGdbLocals = new GetGdbLocals(this);
		_getGdbModuleParts = new GetGdbModuleParts(this);
		_getGdbPartMethods = new GetGdbPartMethods(this);
		_getGdbRegisters = new GetGdbRegisters(this);
		_getGdbStorage = new GetGdbStorage(this);
		_getGdbSharedLibraries = new GetGdbSharedLibraries(this);
		_gdbExceptions = new GdbExceptions(this);
	}

	///////  GDB uses stackFrame context rather than user specified source context //////////////
	///////  This code should be used by GdbVariableMonitor.java:
	//  private String _tempLineNumber = null;
	//  private String _tempFileName = null;
	//  public void setTemporaryContext(String fileName,String lineNo)
	//  {   if(!fileName.equals(_currentFileName))
	//      {  if (Gdb.traceLogger.ERR)
	//             Gdb.traceLogger.err(2,"######## GdbDebugSession.setTemporaryContext CANNOT set context.fileName="+fileName+" context.lineNum="+lineNo );
	//      }
	//     _tempLineNumber = _currentLineNumber;
	//     _tempFileName = _currentFileName;
	//     changeContext(fileName,lineNo);
	//  }
	//  public void restoreContext()
	//  {  changeContext(_tempFileName,_tempLineNumber);
	//      _tempLineNumber = null;
	//      _tempFileName = null;
	//  }
	//  public void changeContext(String fileName, String lineNumber)
	//  {     String cmd = "list ";//+fileName+":"+lineNumber+","+lineNumber;
	//        if(fileName!=null && !fileName.equals(""))
	//           cmd += fileName+":";
	//        cmd +=lineNumber+","+lineNumber;
	//        boolean ok = executeGdbCommand(cmd);
	//        if( !ok )
	//           return;
	//        addCmdResponsesToUiMessages();
	//        cmdResponses.removeAllElements();
	//  }
	///////  GDB uses stackFrame context rather than user specified source context //////////////

	public void addCmdResponsesToUiMessages() {
		if (!echoInternalCommands)
			return;

		if (cmdResponses.size() > 0) {
			int length = cmdResponses.size();
			for (int i = 0; i < length; i++) {
				String str = (String) cmdResponses.elementAt(i);
				addLineToUiMessages(str);
			}
		}
	}
	
	public void addCmdResponsesToUiMessages(boolean force) {
		if (!echoInternalCommands && !force)
			return;

		if (cmdResponses.size() > 0) {
			int length = cmdResponses.size();
			for (int i = 0; i < length; i++) {
				String str = (String) cmdResponses.elementAt(i);
				addLineToUiMessages(str, force);
			}
		}
	}
	
	public void checkResponseForException() {
		if (cmdResponses.size() > 0) {
			_whyExceptionMsg = null;
			int length = cmdResponses.size();
			for (int i = 0; i < length; i++) {
				String str = (String) cmdResponses.elementAt(i);
				
				// produce output in GDB Console or Console 
				// all the messages are added here
				this.addLineToUiMessages(str, true);

				if (str.startsWith("Program received signal")) {
					if (_terminatePending)
						_whyStop = WS_HaltRequest;
					else
						_whyStop = WS_ExceptionThrown;

					if (i < (length - 4)) {
						String s = (String) cmdResponses.elementAt(i + 1);
						s += ": " + (String) cmdResponses.elementAt(i + 3);
						str += " " + s;
					}
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							1,
							"$$$$$$$$$$$$$$$$ GdbDebugSession.checkResponseForException Received Exception="
								+ str
								+ "\n");
					_whyExceptionMsg = str;
				} else if (str.startsWith("Program terminated with signal")) {
					_whyStop = WS_PgmQuit;
					if (i < (length - 4)) {
						String s = (String) cmdResponses.elementAt(i + 1);
						s += ": " + (String) cmdResponses.elementAt(i + 3);
						str += " " + s;
					}
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							1,
							"$$$$$$$$$$$$$$$$ GdbDebugSession.checkResponseForException Termination Exception="
								+ str
								+ "\n");
					_whyExceptionMsg = str;
				} else if (str.startsWith("Program exited with code")) {
					_whyStop = WS_PgmQuit;
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							1,
							"$$$$$$$$$$$$$$$$ GdbDebugSession.checkResponseForException Termination="
								+ str
								+ "\n");
					_whyExceptionMsg = str;
				} else if (str.startsWith("Program exited normally")) {
					_whyStop = WS_PgmQuit;
				} else if (str.startsWith("The program is not being run")) {
					_whyStop = WS_PgmQuit;
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							1,
							"$$$$$$$$$$$$$$$$ GdbDebugSession.checkResponseForException Termination="
								+ str
								+ "\n");
					_whyExceptionMsg = str;
				}
				int space = str.indexOf(" ");
				if (space > 0 && str.charAt(space - 1) == ':')
					addLineToUiMessages(str);
			}
		}
	}

	public void addLineToUiMessages(String line) {
		if (!echoInternalCommands)
			return;

		uiMessages.addElement(line);
	}
	
	public void addLineToUiMessages(String line, boolean force) {
		if (!echoInternalCommands && !force)
			return;

		uiMessages.addElement(line);
	}


	public void addChangesToUiMessages() {
		if (!echoInternalCommands)
			return;

		if (cmdResponses.size() > 0) {
			int length = cmdResponses.size();
			for (int i = 0; i < length; i++) {
				String str = (String) cmdResponses.elementAt(i);

				int space = str.indexOf(" ");
				if (space > 0 && str.charAt(space - 1) == ':')
					addLineToUiMessages(str);
			}
		}
	}

	public MethodInfo[] getPartMethods(GdbPart part) {
		if (!part.isDebuggable())
			return null;

		if (IdeFilesAndMethods) // see also addMethodsForAllParts
			{
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					2,
					"####### GdbDebugSession.getPartMethods part="
						+ part.getName()
						+ " ???***IS*** BYPASSED??? (let IDE framework control this)");
			return new MethodInfo[0];
		}

		MethodInfo[] methods = _getGdbPartMethods.getPartMethods(part);
		if (Gdb.traceLogger.EVT) {
			if (methods != null)
				for (int i = 0; i < methods.length; i++)
					Gdb.traceLogger.evt(
						3,
						"---------------- GdbDebugSession.getPartMethods methods["
							+ i
							+ "] name="
							+ methods[i]._name
							+ " lineNum="
							+ methods[i]._lineNum);
		}

		addMethodsToPart(part, methods);
		return part.getMethods();
	}

	public void addMethodsToPart(GdbPart part, MethodInfo[] methods) {
		MethodInfo[] debuggableMethods =
			_getGdbPartMethods.getDebuggableMethods(part, methods);
		if (Gdb.traceLogger.EVT) {
			if (debuggableMethods != null)
				for (int i = 0; i < debuggableMethods.length; i++)
					Gdb.traceLogger.evt(
						3,
						"---------------- GdbDebugSession.addMethodsToPart DEBUGGABLE methods["
							+ i
							+ "] name="
							+ debuggableMethods[i]._name
							+ " lineNum="
							+ debuggableMethods[i]._lineNum);
		}

		int[] entryIDs = new int[debuggableMethods.length];
		int partID = part.getPartID();
		for (int x = 0; x < debuggableMethods.length; x++)
			entryIDs[x] = _moduleManager.addEntry(partID, x);
		part.setEntryIDs(entryIDs);
		part.setMethods(debuggableMethods);
	}

	public void getMethodsForAllParts() {
		if (IdeFilesAndMethods) // see also updateAllParts and getPartMethods
			{
			//        if (Gdb.traceLogger.EVT)
			//            Gdb.traceLogger.evt(1,"######## GdbDebugSession.getMethodsForAllParts ***BYPASSED*** (let IDE framework control this)" );
			//        return;
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					2,
					"######## GdbDebugSession.getMethodsForAllParts ???SHOULD BE BYPASSED??? (let IDE framework control this)");
		}
		////////////////////// The code below is not needed if the IDE Famework keeps the list of methods //////////////////////////
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"########>>>>>>>> GdbDebugSession.getMethodsForAllParts - time consuming");

		_getGdbPartMethods.getMethodsForAllParts();
	}

	public int cmdCommandLogExecute(java.lang.String cmd, ERepCommandLog _rep) {
		
		ECPLog cp = new ECPLog();
		
		if (gdbProcess == null) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, getResourceString("GDB_PROCESS_NULL"));
			String str = getResourceString("ENGINE_TERMINATED_MSG");
			_whyStop = WS_PgmQuit;
			_rep.setReturnCode(EPDC.ExecRc_TerminateDebugger);
			_rep.setMessage(str);
			cp.addCmdLogLine(str);
			_rep.addLogChangePacket(cp);
			//         _whyExceptionMsg = str;
			//         _rep.setExceptionMsg( str );

			return _whyStop;
		}

		if (cmd.equalsIgnoreCase("setEcho on")) {
			cp.addCmdLogLine(cmd);
			_rep.addLogChangePacket(cp);
			//         uiMessages.addElement(cmd);
			echoInternalCommands = true;
			return 1;
		} else if (cmd.equalsIgnoreCase("setEcho off")) {
			cp.addCmdLogLine(cmd);
			_rep.addLogChangePacket(cp);
			//        uiMessages.addElement(cmd);
			echoInternalCommands = false;
			return 1;
		}

		boolean ok = executeGdbCommand(cmd);
		if (!ok)
			return -1;

		boolean b = echoInternalCommands;
		echoInternalCommands = true;
		addCmdResponsesToUiMessages();
		echoInternalCommands = b;

		if (uiMessages.size() > 0) {
			int length = uiMessages.size();
			
			for (int i = 0; i < length; i++) {
				String str = (String) uiMessages.elementAt(i);
				if (str == null || str.equals("")) {
					str = " ";
				}
				cp.addCmdLogLine(str);
			}
			uiMessages.removeAllElements();
		}

		_rep.addLogChangePacket(cp);

		if (gdbProcess == null || !gdbProcess.isProcessRunning()) {
			_whyStop = WS_PgmQuit;
			String str = getResourceString("ENGINE_TERMINATED_MSG");
			if (Gdb.traceLogger.EVT)
				Gdb.traceLogger.evt(1, "GdbDebugSession.cmdCommandLogExecute " + str);
			_rep.setReturnCode(EPDC.ExecRc_TerminateDebugger);
			_rep.setMessage(str);
			cp.addCmdLogLine(str);
			//         _whyExceptionMsg = str;
			//         _rep.setExceptionMsg( str );

			_rep.addLogChangePacket(cp);
			return _whyStop;
		}

		updateMonitors();
		updateRegisters();
		updateStorage();
		getCurrentFileLineModule();

		return 1;
	}

	// ########################################### WAIT-FOR-EVENT #########################################
	private void waitForEvent() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "????????????????? GdbDebugSession.waitForEvent");
	}
	// #################################################################################################

	private void addModulePart(String moduleName, String partName) {
		_parts.put(moduleName, partName);
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(
				1,
				"???????????????? GdbDebugSession.addModulePart ?UNUSED? moduleName="
					+ moduleName
					+ " partName="
					+ partName
					+ "\n");
		Thread.currentThread().dumpStack();
	}

	private void removePart(String partName) {
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(
				1,
				"???????????????? GdbDebugSession.removePart ?UNUSED? partName="
					+ partName
					+ "\n");
		Thread.currentThread().dumpStack();
		//_parts.remove(partName);
	}

	private void printToConsole(final InputStream stream) {
		Thread thread = new Thread() {
			public void run() {
				try {
					PrintStream out = System.out;
					BufferedReader in = new BufferedReader(new InputStreamReader(stream));

					String line = null;
					while ((line = in.readLine()) != null)
						out.println("LINE: " + line);
				} catch (IOException excp) {
					System.out.println("IOException: " + excp.toString());
					System.exit(0);
				}
			}
		};

		thread.setPriority(Thread.MAX_PRIORITY - 1);
		thread.start();
	}

	private void setMainBreakpoint() {
		Gdb.debugOutput("GdbDebugSession: setMainBreakpoint()");
	}

	public boolean setStartProgramName(
		String programName,
		String parms,
		String[] errMsg) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"########>>>>>>>> GdbDebugSession.setStartProgramName - time consuming   programName="
					+ programName
					+ " parms="
					+ parms);
		_mainProgram = programName;

		if (programName == null || programName.equals("")) {
			if (Gdb.traceLogger.EVT)
				Gdb.traceLogger.evt(
					1,
					"################ GdbDebugSession INVALID_START_PROGRAM_NAME_MSG programName="
						+ programName);
			return false;
		}

		String[] lines = null;
		while (gdbProcess.isReady())
			lines = gdbProcess.readAllLines();

		String cmd = "file " + programName;
		_params = parms;
		if (_params == null)
			_params = "";
		if (_params.indexOf("\"") >= 0)
			_params = _params.replace('\"', ' ');
		boolean ok = executeGdbCommand(cmd);
		if (!ok)
			return false;

		lines = getTextResponseLines();
		boolean found = false;
		String lastMsg = "";
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] != null
				&& !lines[i].equals("")
				&& !lines[i].startsWith(gdbProcess.MARKER)) {
				if (lines[i].endsWith("done."))
					found = true;
				else if (lines[i].length() > 2)
					lastMsg = lines[i];
			}
		}

		if (!found) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_LOAD_PROGRAM")
						+ programName
						+ " ("
						+ lastMsg
						+ ")");
			return false;
		} else
			for (int i = 0; i < lines.length; i++) {
				if (lines[i] != null
					&& !lines[i].equals("")
					&& !lines[i].startsWith(gdbProcess.MARKER)) {
					addLineToUiMessages(prefix + lines[i]);
					if (Gdb.traceLogger.DBG)
						Gdb.traceLogger.dbg(
							2,
							"GdbDebugSession.setStartProgramName 'file' response=" + lines[i]);
				}
			}

		found = getMainModule();
		if (!found) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, getResourceString("GDBPICL_FAILED_TO_FIND_MAIN_MODULE"));
			return false;
		} else
			for (int i = 0; i < lines.length; i++) {
				if (lines[i] != null
					&& !lines[i].equals("")
					&& !lines[i].startsWith(gdbProcess.MARKER)) {
					cmdResponses.addElement(prefix + lines[i]);
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							2,
							"GdbDebugSession.setStartProgramName getMainModule response=" + lines[i]);
				}
			}

		//    updateSharedLibraries();
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"<<<<<<<<######## GdbDebugSession.setStartProgramName DONE");

		// add dummy part
		_moduleManager.checkPart(1, "file-not-found");

		return true;
	}

	public boolean remoteAttach(
		int processIndex,
		String filename,
		String[] errorMsg) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"########>>>>>>>>  GdbDebugSession.remoteAttach() processIndex: "
					+ processIndex
					+ "  filename: "
					+ filename);

		String programName = "";
		String directory = "";
		int indexOfSlash = filename.lastIndexOf("/");
		if (indexOfSlash > 0) {
			programName = filename.substring(indexOfSlash + 1, filename.length());
			directory = filename.substring(0, indexOfSlash);
		} else {
			programName = filename;
		}

		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(
				2,
				"GdbDebugSession.remoteAttach() directory = "
					+ directory
					+ "  programName = "
					+ programName);

		String cmd = "directory " + directory;
		boolean ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_ATTACH_TO_PROCESSID") + processIndex);
		}

		cmd = "file " + programName;
		ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_ATTACH_TO_PROCESSID") + processIndex);
		}

		cmd = "attach " + processIndex;
		ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_ATTACH_TO_PROCESSID") + processIndex);

		}
		_mainProgram = programName;
		_attachedProcessID = processIndex;
		_debuggeeProcessID = (new Integer(_attachedProcessID)).toString();

		addCmdResponsesToUiMessages();

		checkCurrentPart(_currentModuleID);

		updateSharedLibraries();
		updateAllParts();
		//     updateAllMethodsForAllParts();
		updateMonitors();
		updateRegisters();
		updateStorage();
		monitorChangedID.removeAllElements(); // remove model-restored monitors
		monitorChangedName.removeAllElements();
		monitorChangedValue.removeAllElements();
		getCurrentFileLineModule();

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "<<<<<<<<######## GdbDebugSession.remoteAttach() DONE ");
		_attached = true;
		return true;
	}

	public boolean remoteDetach(
		int processIndex,
		int processDetachAction,
		String[] errorMsg) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.dbg(
				1,
				"????????????????? GdbDebugSession.remoteDetach " + processIndex);

		String cmd = "detach";
		boolean ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_DETACH_FROM_PROCESSID") + processIndex);
		}

		return ok;
	}

	public boolean closeDebugger() {
		//System.out.println("????????????????? GdbDebugSession.closeDebugger trace="   );
		//Thread.currentThread().dumpStack();
		return false;
	}

	public void runToMain() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "########>>>>>>>> GdbDebugSession.runToMain");

		String cmd = "break main";
		boolean ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_RUN_TO_MAIN") + cmd);
			return;
		}
		addCmdResponsesToUiMessages();

		cmd = "run " + _params;
		ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_RUN_TO_MAIN") + cmd);
			return;
		}

		if (cmdResponses.size() > 0) {
			int length = cmdResponses.size();
			for (int i = 0; i < length; i++) {
				String str = (String) cmdResponses.elementAt(i);
				if (str != null
					&& !str.equals("")
					&& (str.indexOf(".dll") < 0)
					&& (str.indexOf(" DLL") < 0))
					addLineToUiMessages(str);
			}
		}

		// check to make sure that debugee started ok
		checkResponseForException();

		if (_whyStop == WS_PgmQuit || _whyStop == WS_ExceptionThrown) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_RUN_TO_MAIN") + cmd);

			// do not continue if debugee is started with exception.
			// we will encounter more problems if we do, may go into infinite loop
			return;
		}
	
		// On platform like AIX, main module's addresses are not valid
		// until after the program is run.  Even though we have already obtained
		// addresses for main module previously, we need to update those info
		// after the program is run.
		updateMainSegment();

		//RW
		cmd = "info program";
		ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.DBG)
				Gdb.traceLogger.dbg(1, "failed execution of cmd: " + cmd);
			return;
		}
		String[] lines = getTextResponseLines();
		String process = "process ";
		int ix = 0;
		int dot = 0;
		_debuggeeProcessID = "";
		for (int i = 0; i < lines.length; i++) {
			ix = lines[i].indexOf(process);
			if (ix > 0) {
				dot = lines[i].indexOf(".");
				_debuggeeProcessID = lines[i].substring(ix + process.length(), dot);
				if (Gdb.traceLogger.DBG)
					Gdb.traceLogger.dbg(
						1,
						"GdbDebugSession.runToMain() _debuggeeProcessID: " + _debuggeeProcessID);
				break;
			}
			else
			{
				process = "(LWP ";
				ix = lines[i].indexOf(process);
				if (ix > 0)
				{
					int bracket = lines[i].lastIndexOf(")");
					if (bracket > 0)
						_debuggeeProcessID = lines[i].substring(ix + process.length(), bracket);
					else
						_debuggeeProcessID = lines[i].substring(ix + process.length(), ix + process.length()+5);					
					if (Gdb.traceLogger.DBG)
						Gdb.traceLogger.dbg(
							1,
							"GdbDebugSession.runToMain() _debuggeeProcessID: " + _debuggeeProcessID);
					break;						
				}				
			}
		}

		// RW

		cmd = "clear main";
		ok = executeGdbCommand(cmd);
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_RUN_TO_MAIN") + cmd);
			return;
		}
		addCmdResponsesToUiMessages();

		checkCurrentPart(_currentModuleID);

		// enable deferred breakpoints       
		if (!Gdb.supportDeferredBreakpoint)
			 ((GdbBreakpointManager) _breakpointManager).enableDeferredBreakpoints();

		updateSharedLibraries();
		updateAllParts();
		//     updateAllMethodsForAllParts();
		updateMonitors();
		updateRegisters();
		updateStorage();
		monitorChangedID.removeAllElements(); // remove model-restored monitors
		monitorChangedName.removeAllElements();
		monitorChangedValue.removeAllElements();
		getCurrentFileLineModule();

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "<<<<<<<<######## GdbDebugSession.runToMain DONE ");
	}

	public void enableDeferredBreakpoints() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"<<<<<<<<######## GdbDebugSession.enableDeferredBreakpoints ");
		((GdbBreakpointManager) _breakpointManager).enableDeferredBreakpoints();
	}

	public Part checkCurrentPart(int moduleID) {
		Part part = _getGdbModuleParts.checkCurrentPart(moduleID);
		return part;
	}

	private void updateRegisters() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(3, " GdbDebugSession.updateRegisters");
		_registerManager.updateRegisters();
	}
	private void updateStorage() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(3, " GdbDebugSession.updateStorage");
		_storageManager.updateStorage();
	}

	private void updateMonitors() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				2,
				"######## GdbDebugSession.updateMonitors BYPASSING (GDB command 'display' could be used to them) ??NOT NEEDED??");
	}

	private void updateAllMethodsForAllParts() {
		getMethodsForAllParts();
	}

	boolean getMainModule() {
		return _getGdbModuleParts.getMainModule();
	}

	private boolean updateMainSegment() {
		return _getGdbModuleParts.updateMainSegment();
	}

	public void updateSharedLibraries() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"========>>>>>>>> GdbDebugSession.updateSharedLibraries");

		GetGdbSharedLibraries.ModuleInfo[] moduleInfo =
			_getGdbSharedLibraries.updateSharedLibraries();
		if (moduleInfo != null) {
			for (int i = 0; i < moduleInfo.length; i++)
				if (moduleInfo[i] != null) {
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							1,
							"<<<<<<<<======== GdbDebugSession.updateSharedLibraries name="
								+ moduleInfo[i].name
								+ " moduleInfo[i].segments.size()="
								+ moduleInfo[i].segments.size()
								+ " fullFileName="
								+ moduleInfo[i].fullFileName);
					_moduleManager.addModule(moduleInfo[i].name, moduleInfo[i].fullFileName);
					_moduleManager.setModuleStartFinishAddress(
						moduleInfo[i].name,
						moduleInfo[i].segments);
				}
		}
	}

	private void updateAllParts() {
		if (IdeFilesAndMethods) // see also addMethodsForAllParts  and getPartMethods
			{
			//        if (Gdb.traceLogger.EVT)
			//            Gdb.traceLogger.evt(1,"######## GdbDebugSession.updateAllParts ***BYPASSED*** (let IDE framework control this)" );
			//        return;
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					2,
					"######## GdbDebugSession.updateAllParts ???SHOULD BE BYPASSED??? (let IDE framework control this)");
		}
		////////////////////// The code below is not needed if the IDE Famework keeps the list of Files //////////////////////////
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"################ GdbDebugSession.updateAllParts - time consuming");

		_getGdbModuleParts.updateAllParts();
	}

	public Part isPartInModule(String partName, String moduleName) {
		Part part = _getGdbModuleParts.isPartInModule(partName, moduleName);
		return part;
	}

	public boolean isFile(String fileName) {
		return _getGdbModuleParts.isFile(fileName);
	}

	boolean getCurrentFileLineModule() {
		return _getGdbModuleParts.getCurrentFileLineModule();
	}

	public boolean executeGdbCommand(String cmd) {
		return _gdbCommandAndResponse.executeGdbCommand(cmd);
	}

	public String[] getTextResponseLines() {
		int length = cmdResponses.size();
		String[] lines = new String[length];
		for (int i = 0; i < length; i++) {
			lines[i] = (String) cmdResponses.elementAt(i);
		}
		cmdResponses.removeAllElements();
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(
				3,
				"GdbDebugSession.getTextResponseLines lines.length=" + lines.length);
		return lines;
	}

	public void getGdbResponseLines() {
		_gdbCommandAndResponse.getGdbResponseLines();
	}

	public void setLastUserCmd(int cmd, int depth) {
		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(
				2,
				"######## GdbDebugSession.setLastUserCmd=" + cmd + " BYPASSED ??NOT NEEDED??");
	}

	int getLastUserCmd(int cmd) {
		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(
				2,
				"######## UNUSED?? GdbDebugSession.getLastUserCmd="
					+ cmd
					+ " BYPASSED ??NOT NEEDED??");
		return 0;
	}

	int getLastUserDepth(int depth) {
		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(
				2,
				"######## UNUSED?? GdbDebugSession.getLastUserDepth=" + depth);
		return 0;
	}

	String[] getNewPartsList() {
		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(2, "######## UNUSED?? GdbDebugSession.getPartsList");

		if (_parts == null || _parts.size() == 0)
			return null;

		int partsListSize = _parts.size();
		String[] partsList = new String[partsListSize - _partStartIndex];

		// The keys are the part names
		Enumeration partEnum = _parts.keys();

		//while (partEnum.hasMoreElements())
		for (int i = _partStartIndex; partEnum.hasMoreElements(); i++)
			partsList[i - _partStartIndex] = (String) partEnum.nextElement();

		_partStartIndex = partsListSize - 1;

		return partsList;
	}

	public String[] getPartsList() {
		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(2, "######## UNUSED?? GdbDebugSession.getPartsList");

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(3, "GdbDebugSession.getPartsList");

		if (_parts == null || _parts.size() == 0)
			return null;

		String[] partsList = new String[_parts.size()];
		Enumeration partEnum = _parts.keys();
		int partCount = 0;

		while (partEnum.hasMoreElements())
			partsList[partCount++] = (String) partEnum.nextElement();

		return partsList;
	}

	public String getFullProgramName(String name) {

		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(
				2,
				"######## UNUSED?? GdbDebugSession: getFullProgramName(" + name + ")");
		Thread.currentThread().dumpStack();

		return name;
	}

	public String stopThreadName() {
		int stopThread = _threadManager._stoppingThread;
		String s = new Integer(stopThread).toString();
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(2, "GdbDebugSession.stopThreadName=" + s);
		return s;
	}

	public int whyStop() {
		return _whyStop;
	}

	public boolean isWaiting() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "$$$$$$$$$$$$$$$$ GdbDebugSession.isWaiting ???? \n");
		return false;
	}

	public void setTmpBkpt(int partID, int lineNum) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				2,
				"-------->>>>>>>> GdbDebugSession.setTmpBkpt partID="
					+ partID
					+ " lineNum="
					+ lineNum);
	}

	public void clearTmpBkpt() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(2, "-------->>>>>>>> GdbDebugSession.clearTmpBkpt");
	}

	// ##############################################################################################
	// ##############################################################################################
	public int cmdStep(String threadName, boolean stepOver)
	{
		return cmdStep(threadName, stepOver, Part.VIEW_SOURCE);
	}


	public int cmdStep(String threadName, boolean stepOver, int view) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"========>>>>>>>> GdbDebugSession.cmdStep threadName="
					+ threadName
					+ " over="
					+ stepOver);

		_whyStop = WS_Unknown;
		String cmd = "step ";
		if (stepOver)
			cmd = "next ";
			
		if (view != Part.VIEW_SOURCE)	
			cmd = "stepi ";

		boolean ok = executeGdbCommand(cmd);
		if (!ok)
			return _whyStop;

		_whyStop = WS_BkptHit;

		checkResponseForException();
		//      addChangesToUiMessages();
		updateMonitors();
		updateRegisters();
		updateStorage();
		getCurrentFileLineModule();
		return _whyStop;
	}

	public int cmdStepDebug(String threadName) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"========>>>>>>>> GdbDebugSession.cmdStepDebug threadName=" + threadName);

		// Make note of where we started to step debug
		ThreadComponent tc = _threadManager.getThreadComponent(threadName);
		String fileName = tc.fileName(0);
		String methodName = tc.methodName(0, false);
		int lineNumber = tc.lineNumber(0);
		int lastStop;
		_whyStop = WS_Unknown;

		// We will do a shallow step debug until we either a) reach debuggable
		// code or b) we step over the statement we started at (see below)
		// or c) we hit some significant event (breakpoint, exception, etc)
		do {
			do {
				cmdStep(threadName, false);
				if (!tc.isDebuggable() && wasSystemEvent())
					cmdStepReturn(threadName);
			}
			// Note: If we are on the same line that we started at, we don't care
			// if there is a user breakpoint there since we've already stopped
			while (fileName.equals(tc.fileName(0))
				&& methodName.equals(tc.methodName(0, false))
				&& lineNumber == tc.lineNumber(0)
				&& (wasSystemEvent() || _whyStop == WS_UserBkptHit));
			// Do nothing
		}
		while (!tc.isDebuggable() && wasSystemEvent());
		checkResponseForException();
		updateMonitors();
		updateRegisters();
		updateStorage();
		getCurrentFileLineModule();

		return _whyStop;
	}
	
	public int cmdGoToAddress(String address)
	{		
		_whyStop = WS_Unknown;

		String cmd = "break * " + address;
		boolean ok = executeGdbCommand(cmd);
		if (!ok)
			return _whyStop;

		addChangesToUiMessages();

		String breakIndex = null;
		String BREAKPOINT_keyword = "Breakpoint ";
		String str = (String) cmdResponses.elementAt(0);
		if (str.startsWith("Note: breakpoint"))
			str = (String) cmdResponses.elementAt(1);
		if (str.startsWith(BREAKPOINT_keyword)) {
			str = str.substring(BREAKPOINT_keyword.length());
			int space = str.indexOf(" ");
			if (space > 0) {
				breakIndex = str.substring(0, space);
			}
		}
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "GdbDebugSession.cmdGoTo breakIndex=" + breakIndex);
		if (breakIndex != null) {
			cmd = "jump * " + address;
			ok = executeGdbCommand(cmd);
			if (!ok)
				return _whyStop;

			addChangesToUiMessages();

			cmd = "delete " + breakIndex;
			ok = executeGdbCommand(cmd);
			if (!ok)
				return _whyStop;

			//        getGdbResponseLines();  //  cmdResponses.removeAllElements();
			addChangesToUiMessages();
		}
		_whyStop = WS_BkptHit;
		checkResponseForException();
		getCurrentFileLineModule();

		return _whyStop;
	}

	public int cmdGoTo(String threadName, String lineNum) {
		_whyStop = WS_Unknown;
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"========>>>>>>>> GdbDebugSession.cmdGoTo lineNum="
					+ lineNum
					+ " threadName="
					+ threadName);

		String cmd = "break " + lineNum;
		boolean ok = executeGdbCommand(cmd);
		if (!ok)
			return _whyStop;

		addChangesToUiMessages();

		String breakIndex = null;
		String BREAKPOINT_keyword = "Breakpoint ";
		String str = (String) cmdResponses.elementAt(0);
		if (str.startsWith("Note: breakpoint"))
			str = (String) cmdResponses.elementAt(1);
		if (str.startsWith(BREAKPOINT_keyword)) {
			str = str.substring(BREAKPOINT_keyword.length());
			int space = str.indexOf(" ");
			if (space > 0) {
				breakIndex = str.substring(0, space);
			}
		}
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "GdbDebugSession.cmdGoTo breakIndex=" + breakIndex);
		if (breakIndex != null) {
			cmd = "jump " + lineNum;
			ok = executeGdbCommand(cmd);
			if (!ok)
				return _whyStop;

			addChangesToUiMessages();

			cmd = "delete " + breakIndex;
			ok = executeGdbCommand(cmd);
			if (!ok)
				return _whyStop;

			//        getGdbResponseLines();  //  cmdResponses.removeAllElements();
			addChangesToUiMessages();
		}
		_whyStop = WS_BkptHit;
		checkResponseForException();
		getCurrentFileLineModule();

		return _whyStop;
	}

	public int cmdRun_User() {
		_whyStop = WS_Unknown;
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "========>>>>>>>> GdbDebugSession.cmdRun_User");

		String cmd = "cont ";

		boolean ok = executeGdbCommand(cmd);
		if (!ok)
			return _whyStop;

		//_whyStop = WS_BkptHit;
		_whyStop = WS_UserBkptHit;
		checkResponseForException();
		//     addChangesToUiMessages();

		updateMonitors();
		updateRegisters();
		updateStorage();
		getCurrentFileLineModule();
		return _whyStop;
	}

	public int cmdStepReturn_User(String threadName) {
		_whyStop = WS_Unknown;
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"========>>>>>>>> GdbDebugSession.cmdStepReturn threadName=" + threadName);

		String cmd = "finish ";

		if (!executeGdbCommand(cmd)) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_ECECUTE_STEP_RETURN"));
			return _whyStop;
		}

		//     getGdbResponseLines();
		_whyStop = WS_BkptHit;
		checkResponseForException();
		//     addChangesToUiMessages();
		updateMonitors();
		updateRegisters();
		updateStorage();
		getCurrentFileLineModule();
		return _whyStop;
	}

	public void cmdStepReturn(String threadName) {
		cmdStepReturn_User(threadName);
	}

	public int setWatchpoint(String expression) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.setWatchpoint expression=" + expression);

		int ret = _getGdbBreakpoints.watchBreakpoint(expression);

		addChangesToUiMessages();
		return ret;
	}
	public int setLineBreakpoint(int partID, int lineNumber) {
		String fileName = _moduleManager.getFullPartName(partID);
		String lineNum = new Integer(lineNumber).toString();

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.setLineBreakpoint partID="
					+ partID
					+ " fileName="
					+ fileName
					+ " lineNum="
					+ lineNum);

		int ret = _getGdbBreakpoints.lineBreakpoint(fileName, lineNum);

		addChangesToUiMessages();
		return ret;
	}

	// set breakpoint with filename and line number
	public int setLineBreakpoint(String fileName, int lineNumber) {
		String lineNum = new Integer(lineNumber).toString();

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.setLineBreakpoint fileName="
					+ fileName
					+ " lineNum="
					+ lineNum);

		int ret = _getGdbBreakpoints.lineBreakpoint(fileName, lineNum);

		addChangesToUiMessages();
		return ret;
	}
	
	// set breakpoint with address
	public int setAddressBreakpoint(String address) {

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.setAddressBreakpoint address="
					+ address);
					
		// check to see if address is valid
		boolean ok = executeGdbCommand("x " + address);
		String[] lines = getTextResponseLines();
		if (lines.length > 0) {
			int index = lines[0].indexOf("Cannot access memory at address");
			if (index != -1)
			{
				if (Gdb.traceLogger.ERR)
					Gdb.traceLogger.err(1, "Bad Address Breakpoint Location:  " + address);
				return -1;
			}
		}			
				
		int ret = _getGdbBreakpoints.addressBreakpoint(address);

		addChangesToUiMessages();
		return ret;
	}

	/**
	 * Remove a breakpoint at a specific location
	 */
	public void clearBreakpoint(int partID, int lineNumber) {
		String fileName = _moduleManager.getFullPartName(partID);
		String lineNum = new Integer(lineNumber).toString();

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.clearBreakpoint partID="
					+ partID
					+ " fileName="
					+ fileName
					+ " lineNum="
					+ lineNum);

		int ret = _getGdbBreakpoints.clearBreakpoint(fileName, lineNum);

		addChangesToUiMessages();
		return;
	}
	
	/**
	 * Delete a breakpoint with breakpoint ID
	 */
	public void clearBreakpoint(int id) {

		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.clearBreakpoint gdbBkptID="
					+ id);

		int ret = _getGdbBreakpoints.deleteBreakpoint(id);

		addChangesToUiMessages();
		return;
	}

	public int setMethodBreakpoint(int partID, int methodIndex) {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"-------->>>>>>>> GdbDebugSession.setMethodBreakpoint partID="
					+ partID
					+ " methodIndex="
					+ methodIndex);

		Part part = _moduleManager.getPart(partID);
		if (part == null)
			return -1;
		String methodName = part.getEntryName(methodIndex % 0x10000);
		int bracket = methodName.indexOf("(");
		if (bracket > 0)
			methodName = methodName.substring(0, bracket);

		String cmd = "break " + methodName;

		if (!executeGdbCommand(cmd)) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(
					1,
					getResourceString("GDBPICL_FAILED_TO_EXECUTE_METHOD_BREAKPOINT") + cmd);
			return -1;
		}
		// possible responses:
		// Breakpoint 3 at 0x40113a: file TestGnu2.c, line 8.
		// Function "doSleep" not defined.
		// Note: breakpoint 3 also set at pc 0x40113a.

		addChangesToUiMessages();

		int lineNum = part.getEntryLineNumber(methodIndex % 0x1000);
		return lineNum;
	}

	public boolean terminateDebuggee() {
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(1, "################ GdbDebugSession.terminateDebuggee");

		if (_attached) {
			remoteDetach(_attachedProcessID, 1, null);
		} else {
			String cmd = "kill ";
			if (!executeGdbCommand(cmd)) {
				if (Gdb.traceLogger.ERR)
					Gdb.traceLogger.err(
						1,
						getResourceString("GDBPICL_FAILED_TO_EXECUTE_TERMINATE"));
				return false;
			}
			String[] lines = getTextResponseLines();
			if (lines.length > 0) {
				for (int i = 0; i < lines.length; i++)
					if (Gdb.traceLogger.EVT)
						Gdb.traceLogger.evt(
							1,
							"???????????????? GdbDebugSession.terminateDebuggee KILL returned str="
								+ lines[i]);
			}

			// in case there is another prepareProgram(restart) instead of a TerminateDebugEngine
			if (Gdb.traceLogger.EVT)
				Gdb.traceLogger.evt(
					1,
					"---------------- GdbDebugSession.terminateDebuggee is recreating Managers incase of following prepareProgram (restart)");
			createManagers();

		}
		return true;
	}

	/**
	 * Inform the RemoteDebugger which exceptions we wish to be informed of and
	 * which we wish to ignore.
	 */
	void initExceptions() {
		int[] exceptionStatus = _gdbExceptions.getExceptionStatus();
		String[] exceptionNames = _gdbExceptions.getExceptionNames();
		int maxExceptions = exceptionStatus.length;
		for (int i = 0; i < exceptionNames.length; i++) {
			if (exceptionStatus[i] == 1)
				catchException(i, exceptionNames[i]);
			else
				ignoreException(i, exceptionNames[i]);
		}
	}

	public void ignoreException(int index, String name) {
		_gdbExceptions.ignoreException(index, name);
	}

	public void catchException(int index, String name) {
		_gdbExceptions.catchException(index, name);
	}

	public void cmdStepException(String threadName) {
		if (Gdb.traceLogger.ERR)
			Gdb.traceLogger.err(
				1,
				"######## GdbDebugSession.cmdStepException UNSUPPORTED STEP-EXCEPTION threadName="
					+ threadName
					+ "\n");
		cmdStep(threadName, false);
	}

	public void cmdExamineException(String threadName) {
		/////////////////// ***************** UGLY STRT ************************
		String osName = System.getProperties().getProperty("os.name");
		if (osName.startsWith("Windows")) {
			cmdStep(threadName, false); // why does Windows Gdb need a step first ??
			if (_whyStop == WS_PgmQuit)
				return;
		}
		/////////////////// ***************** UGLY DONE ************************

		if (_currentLineNumber.equals("")) {
			_whyExceptionMsg =
				getResourceString("CANNOT_RETRY_EXCEPTION_MUST_RUN_OR_TERMINATE");
			_whyStop = WS_ExceptionThrown;
			return;
		}

		cmdGoTo(threadName, _currentLineNumber);
	}

	public String exceptionName() {
		return _whyExceptionMsg;
	}
	
		public String getDataAddress() {
		return _dataAddress;
	}
	public void setDataAddress(String dataAddress) {
		_dataAddress = dataAddress;
	}

	// References to other Gdb components
	public GdbDebugEngine getGdbDebugEngine() {
		return (GdbDebugEngine) _debugEngine;
	}
	
	   /** Adds the breakpoint change packets to the reply packet */
    public void addChangesToReply(EPDC_Reply rep) {
		ECPLog cp = new ECPLog();

		if (uiMessages.size() > 0) {
			int length = uiMessages.size();
			
			for (int i = 0; i < length; i++) {
				String str = (String) uiMessages.elementAt(i);
				if (str == null || str.equals("")) {
					str = " ";
				}
				cp.addCmdLogLine(str);
//				cp.addPgmOutputLine(str);
			}
			uiMessages.removeAllElements();
		}

		rep.addLogChangePacket(cp);
    }

	// add the name to the list of dll to stop for    
    public int setLoadBreakpoint(String dllName)
    {
    	int id;
	   	id = _dllToStop.size();
    	
    	_dllToStop.put(new Integer(id), dllName);

    	gdbProcess.setStopOnSharedLibEvents(true);    	    	
    	
    	return id;
    }
    
	// remove dll name from the list of dll to stop for
    public void clearLoadBreakpoint(int id)
    {
    	Integer dllId = new Integer(id);
    	
    	if (_dllToStop.containsKey(dllId))
    	{
    		_dllToStop.remove(dllId);
    		if (resetStopOnSharedLibEvents())
    		{
    			gdbProcess.setStopOnSharedLibEvents(false);
    		}
    	}
    }
    
    // return true - if num of deferred bkpt + num of load bkpt is zero
    // false - otherwise
    public boolean resetStopOnSharedLibEvents()
    {
    	int numDeferred = ((GdbBreakpointManager)this.getBreakpointManager()).getNumDeferredBkpt();
    	int loadBkpt = _dllToStop.size();
    	
    	return ((numDeferred + loadBkpt) == 0);
    }


	// data members

	private String _mainProgram;
	private String _params = "";
	public String getProgramName() {
		return _mainProgram;
	}
	private String _debuggeeProcessID = "";
	public String getDebuggeeProcessID() {
		return _debuggeeProcessID;
	}
	private boolean _terminatePending = false;
	private int _attachedProcessID;
	public int getAttachedProcessID() {
		return _attachedProcessID;
	}
	private boolean _attached = false;
	public void setTerminatePending(boolean b) {
		_terminatePending = b;
	}
	private Hashtable _parts = new Hashtable();
	private Hashtable _dllToStop = new Hashtable();
	protected String _dataAddress = "";

}
