//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.commands.*;

import java.io.*;
import java.net.*;
import java.util.*;
import com.ibm.debug.epdc.*;      // EPDC classes
import com.ibm.debug.connection.Connection;

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
