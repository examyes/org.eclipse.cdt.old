/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;


/**
 * The DebugEngine class represents one debug engine.  You may instantiate
 * any number of DebugEngine classes. 
 */
public class GdbDebugEngine extends DebugEngine
{
  /**
   * Construct a new DebugEngine from a connection object.
   */
   public GdbDebugEngine(Connection connection)
   {
      super(connection);
      _debugSession = new GdbDebugSession(this); 
      Gdb.initDebugOutput();
   }
   
   public GdbDebugEngine(Connection connection, String gdbPath)
   {
      super(connection);
      if (gdbPath != null && gdbPath != "")
      {
      	setGdbPath(gdbPath);
      }
      _debugSession = new GdbDebugSession(this); 
      Gdb.initDebugOutput();
   }
   
   private String gdbPath;

	/**
	 * Gets the gdbPath
	 * @return Returns a String
	 */
	public String getGdbPath() {
		return gdbPath;
	}
	/**
	 * Sets the gdbPath
	 * @param gdbPath The gdbPath to set
	 */
	public void setGdbPath(String gdbPath) {
		this.gdbPath = gdbPath;
	}

}
