package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ExecutableDebugEngine.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:11:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.net.*;
import java.io.*;
import com.ibm.debug.epdc.*;
import com.ibm.debug.connection.*;

/** A debug engine which is packaged as an executable and can be loaded by
  * SUI as a separate process (or is already running in a separate process).
  */

public class ExecutableDebugEngine extends DebugEngine
{
   /** Construct a debug engine with the given name on the given host machine.
    *  If the debug engine is already running in an existing process and
    *  therefore does not need to be loaded by SUI, null can be passed as the
    *  exeName.
    */

   public ExecutableDebugEngine(Host host, String exeName)
   {
     super(host);

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(1, "Creating ExecutableDebugEngine : Host=" + host.name() + " ExeName=" + exeName);

     // If no exeName given, assume the debug engine is already loaded:

     if ((_exeName = exeName) == null)
        super.setIsLoaded(true);
   }

   /** Establish a connection with the debug engine using a socket. A
    *  return value of 'false' indicates that the connection could not be
    *  established.
    */

   public boolean connectUsingSocket(int portNumber)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(1, "ExecutableDebugEngine[" + this.exeName() + "].connectUsingSocket(" + portNumber + ")");

     Host host = super.host();
     InetAddress internetAddress = null;

     if (host == null || (internetAddress = host.internetAddress()) == null)
     {
        if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(3, "Either Host or Host.internetAddress() is null");
        return false;
     }

     try
     {
       super.setConnection(new SocketConnection(internetAddress, portNumber));
       connection().startDumping();
       return true;
     }
     catch(java.io.IOException excp)
     {
       if (Model.TRACE.ERR && Model.traceInfo())
         Model.TRACE.err(1, excp.getMessage());
       return false;
     }
   }

   public String exeName()
   {
     return _exeName;
   }

   public void setDebugEngineProcess(java.lang.Process debugEngineProcess)
   {
     _debugEngineProcess = debugEngineProcess;
     super.setIsLoaded(true);
   }

   public java.lang.Process debugEngineProcess()
   {
     return _debugEngineProcess;
   }

   /**
    * Set the args that were used on the most recent invocation of this
    * debug engine.
    */

   public void setCurrentArgs(String args)
   {
     _currentArgs = args;
   }

  /**
   * This method is used by Java Serialization - it is called from
   * ObjectOutputStream.writeObject in order to serialize objects of this
   * class.
   * <p>IMPORTANT: This method must be kept in synch with the readObject
   * method for this class so that we only ever try to read in those fields
   * that were written out (and in the same order). This includes the
   * possibility that the writeObject method has chosen to write nothing
   * out in which case the readObject method must be prepared to read nothing
   * in.
   * <p>If we want to write out the entire object, we will call the default
   * method provided by Java - ObjectOutputStream.defaultWriteObject. This
   * default method writes out all non-static, non-transient fields.
   */

  private void writeObject(ObjectOutputStream stream)
  throws java.io.IOException
  {
    // See if we want to save all (non-transient) fields in the object
    // or only some of them:

    if (stream instanceof ModelObjectOutputStream)
    {
       int flags = ((ModelObjectOutputStream)stream).getSaveFlags();

       if ((flags & SaveRestoreFlags.ALL_OBJECTS) != 0)
          stream.defaultWriteObject();
    }
    else
       stream.defaultWriteObject();
  }

  /**
   * This method is used by Java Serialization - it is called from
   * ObjectInputStream.readObject in order to serialize objects of this
   * class.
   * <p>IMPORTANT: This method must be kept in synch with the writeObject
   * method for this class so that we only ever try to read in those fields
   * that were written out (and in the same order). This includes the
   * possibility that the writeObject method has chosen to write nothing
   * out in which case the readObject method must be prepared to read nothing
   * in.
   * <p>If we want to read in the entire object, we will call the default
   * method provided by Java - ObjectInputStream.defaultReadObject. This
   * default method reads in all non-static, non-transient fields.
   */

  private void readObject(ObjectInputStream stream)
  throws java.io.IOException,
         java.lang.ClassNotFoundException
  {
    // See if we need to read all (non-transient) fields in the object
    // or only some of them:

    if (stream instanceof ModelObjectInputStream)
    {
       int flags = ((ModelObjectInputStream)stream).getSaveFlags();

       if ((flags & SaveRestoreFlags.ALL_OBJECTS) != 0)
          stream.defaultReadObject();
    }
    else
       stream.defaultReadObject();
  }

   private String _exeName;
   private transient java.lang.Process _debugEngineProcess;
   private String _currentArgs;
}
