/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.gdbCommands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import java.util.*;

/** Launches a gdb process. 
  * <BR>Example:<code> GdbProcess gdb </code>
  * <pre>
  * f0 -> "GdbProcess"
  * f1 -> ( fileName() ( parameters() )? )
  * </pre>
  *
  * @see GdbProcess
 **/

public class GdbProcess {
	public static class NotifyingLineInputStreamReader extends InputStreamReader {
		private InputStream is = null;
		private LinkedList lines = new LinkedList();
		private ThreadedReader tr = null;
		private Object synch;

		public NotifyingLineInputStreamReader(InputStream is_, Object s_) {
			super(is_);
			is = is_;
			synch = s_;
			tr = new ThreadedReader(is);
			tr.start();
		}

		public boolean ready() {
			return !lines.isEmpty();
		}

		public String readLine() {
			synchronized(synch) {			
				while (lines.isEmpty()) {
					try { synch.wait(); }
					catch(InterruptedException e) {}
				}	
				return (String) lines.removeFirst();
			}	
		}

		private void setLine(String str) {
			synchronized(synch) {
				lines.add(str);
				synch.notifyAll();
			}	
		}

		private class ThreadedReader extends Thread {
			private InputStreamReader isr = null;
			private BufferedReader rdr = null;
			//    private char[] isrBuffer = new char[1024+10];
			private ThreadedReader(InputStream is) {
				isr = new InputStreamReader(is);
				rdr = new BufferedReader(isr);
			}
			public void run() {
				String txt = "";
				while (rdr != null) {
					try {
						txt = rdr.readLine();
						if (txt != null)
							setLine(txt);
						else
							close();
					} catch (java.io.IOException excp) {
						close();
					}
				}
				close();
			}
			private void close() {
				if (rdr != null)
					try {
						rdr.close();
					} catch (java.io.IOException excp) {
					}
				rdr = null;
			} // end of run
		}
	}
	public static final int MAXSECONDS = 20;
	private static int MAXLINES =
		GdbDebugSession.MAX_GDB_LINES + GdbDebugSession.MAX_GDB_LINES;
	private static char PMPT[] = {(char) 26, (char) 26 };
	public static final String MARKER = new String(PMPT);
	private static String PROMPT = MARKER + "prompt"; // set annotate 2
	private boolean _annotated = false;
	private boolean _stopOnSharedLibEvents = false;
	
	public boolean getAnnotated() {
		return _annotated;
	}
	public void setAnnotated(boolean b) {
		_annotated = b;
		if (b)
			writeLine("set annotate 2");
		else
			writeLine("set annotate 0");
	}

	private Process proc = null;
	private BufferedWriter wtr = null;
	private InputStream is = null;
	private NotifyingLineInputStreamReader lisr = null;
	private InputStream isError = null;
	private NotifyingLineInputStreamReader lisrError = null;
	private String _lastCommand = "";

	public GdbProcess(String command) {
		if (command == null || command.equals("")) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(2, "GdbProcess received command=null");
			return;
		}
		try {
			_lastCommand = command;
			if (Gdb.traceLogger.EVT)
				Gdb.traceLogger.evt(1, "================ GdbProcess execCommand=" + command);
			proc = Runtime.getRuntime().exec(command);

			wtr = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
			is = proc.getInputStream();
			lisr = new NotifyingLineInputStreamReader(is, this);
			isError = proc.getErrorStream();
			lisrError = new NotifyingLineInputStreamReader(isError, this);
			setAnnotated(true);
		} catch (SecurityException excp) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, "GdbProcess SecurityException=" + excp);		
			System.exit(-1);
		} catch (java.io.IOException excp) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, "GdbProcess IOException=" + excp);
			System.exit(-1);
		}

		String cmd = "set annotate 2";
		boolean ok = writeLine(cmd);
		if (ok) {
			cmd = "set height 0";
			ok = writeLine(cmd);
		}
		if (ok) {
			cmd = "set width 0";
			ok = writeLine(cmd);
		}
		// this setting may cause problem if we have a huge array
		// I tested this array with an array of 1000 elements
		// Gdb only gave back about 700 elements.
		if (ok)
		{
			cmd = "set print elements 0";
			ok = writeLine(cmd);
		}
		// print out char arrays more nicely
		if (ok)
		{
			cmd = "set print null-stop";
			ok = writeLine(cmd);
		}
/*		
		if (Gdb.supportDeferredBreakpoint)
		{		
			if (ok)
			{
	           if (Gdb.traceLogger.DBG)
    	            Gdb.traceLogger.dbg(1, " GdbProcess - set stop-on-solib-events" );               
				cmd = "set stop-on-solib-events 1";
				ok = writeLine(cmd);
			}
		}
*/		
		if (!ok) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(2, "GdbProcess failed to writeLine=" + cmd);				
			return;
		}
		
		_stopOnSharedLibEvents = true;
	}

	public boolean writeLine(String cmd) {
		if (proc != null)
			if (!isProcessRunning())
				killRunningProcess();
		boolean ok = false;
		if (proc == null) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(2, "GdbProcess.writeLine proc==null");
			return ok;
		}
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(3, "GdbProcess writeLine to stdIn=" + cmd);
		try {
			wtr.write(cmd + "\n");
			wtr.flush();
			ok = true;
			_lastCommand = cmd;
		} catch (java.io.IOException excp) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(2, "GdbProcess.writeLine IOException=" + excp);
		}
		return ok;
	}
	public String[] readAllLines() {
		if (proc == null) {
			if (Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(2, "GdbProcess.readAllLines proc==null");
			return null;
		}
		Vector allLines = new Vector();
		String txt = null;
		for (int i = 0; i < MAXLINES; i++) // up to MAXLINES read **OR** until PROMPT
			{
			txt = readLine();
			if (txt != null) {
				if (Gdb.traceLogger.DBG)
					Gdb.traceLogger.dbg(3, "GdbProcess readAllLines=" + txt);
				allLines.addElement(txt);
				if (txt.equals(PROMPT) && !lisr.ready()) // PROMPT '->->prompt', so ALL DOME
					break;
			} else
				break;
		}
		if (allLines.size() >= 1) {
			String str = (String) allLines.elementAt(0);
			if (Gdb.traceLogger.DBG)
				Gdb.traceLogger.dbg(
					3,
					"GdbProcess readAllLines str[0]=" + str + " cmd=" + _lastCommand + "<<<<<<");
			if (str != null && str.equals(_lastCommand)) {
				allLines.removeElementAt(0);
				if (Gdb.traceLogger.DBG)
					Gdb.traceLogger.dbg(1, "GdbProcess readAllLines removing cmd==str[0]==" + str);
			}
		}
		String[] lines = new String[allLines.size()];
		for (int i = 0; i < allLines.size(); i++)
			lines[i] = (String) allLines.elementAt(i);
		return lines;
	}

	public String readLine() {
		if (proc != null)
			if (!isProcessRunning())
				killRunningProcess();
		if (proc == null) {
			if (Gdb.traceLogger.DBG)
				Gdb.traceLogger.dbg(1, "GdbProcess.readLine proc==null");
			return null;
		}
		String txt = null;

		synchronized(this) {
			if (!lisrError.ready() && !lisr.ready()) {
				try { 
					this.wait(200); 
				} catch (InterruptedException e) {
				}
			}	

		for (int i = 0; i < MAXSECONDS; i++) {
			if (lisrError.ready()) {
				txt = lisrError.readLine();
				break;
			}
			if (lisr.ready()) {
				txt = lisr.readLine();
				break;
			}
			if (Gdb.traceLogger.EVT)
					Gdb.traceLogger.evt(1,
						"**************** GdbProcess.readLine rdr NOT ready, will **SLEEP** then retry");
			synchronized(this) {
				if(!lisrError.ready() && !lisr.ready())
					try {
							this.wait(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			if (proc == null) {
				if (Gdb.traceLogger.ERR)
					Gdb.traceLogger.err(1, "GdbProcess.readAllLines proc==null");
				return txt;
			}
		}
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(4, "GdbProcess.readLine=" + txt);
		return txt;
	}

	public boolean isReady() {
		if (lisr == null || proc == null)
			return false;
		return lisr.ready();
	}

	public boolean isProcessRunning() {
		boolean isRunning = false;
		if (proc != null) {
			try {
				int rc = proc.exitValue();
				if (Gdb.traceLogger.EVT)
					Gdb.traceLogger.evt(1, "GdbProcess PROC is FINISHED rc=" + rc + "\n");
				killRunningProcess();
			} catch (IllegalThreadStateException exc) {
				isRunning = true;
			}
		}
		return isRunning;
	}

	public void killRunningProcess() {
		if (proc != null)
			proc.destroy();
		proc = null;
		if (lisr != null) {
			try {
				lisr.close();
			} catch (java.io.IOException excp) {
			}
			lisr = null;
		}
		if (wtr != null) {
			try {
				wtr.close();
			} catch (java.io.IOException excp) {
			}
			wtr = null;
		}
	}	
	
	/**
	 * Gets the stopOnSharedLibEvents.
	 * @return Returns a boolean
	 */
	public boolean stopOnSharedLibEvents() {
		return _stopOnSharedLibEvents;
	}

	/**
	 * Sets the stopOnSharedLibEvents.
	 * @param stopOnSharedLibEvents The stopOnSharedLibEvents to set
	 * if stopOnSharedLibEvents = true - set stop-on-solib-events 1
	 * else - set stop-on-solib-events 0
	 * if an error occurs, _stopOnSharedLibEvents will be left unchanged
	 */
	public void setStopOnSharedLibEvents(boolean stopOnSharedLibEvents) {
		
		boolean ok;
		String cmd;
		boolean backup = _stopOnSharedLibEvents;
		
		if (Gdb.traceLogger.DBG)
				Gdb.traceLogger.dbg(1, "GdbProcess.setStopOnSharedLibEvents:  " + stopOnSharedLibEvents);

		_stopOnSharedLibEvents = stopOnSharedLibEvents;
		
		if (_stopOnSharedLibEvents)
		{
			cmd = "set stop-on-solib-events 1";
			ok = writeLine(cmd);
		}
		else
		{
			cmd = "set stop-on-solib-events 0";
			ok = writeLine(cmd);
		}		
		
		if (!ok)
		{			
			if (Gdb.traceLogger.EVT)
					Gdb.traceLogger.evt(1, "**************** GdbProcess.setStopSharedLibEvents FAILED. _stopOnSharedLibEvents unchanged - " + backup);
			_stopOnSharedLibEvents = backup;
		}
	}

}