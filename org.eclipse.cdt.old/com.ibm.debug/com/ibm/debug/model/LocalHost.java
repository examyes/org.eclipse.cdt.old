package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/LocalHost.java, java-model, eclipse-dev, 20011128
// Version 1.29.1.2 (last modified 11/28/01 16:11:05)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import java.io.*;
import java.net.*;
import java.util.Vector;

/** Use this class when the debug engine and SUI are running on the
 *  same machine.
 */

public class LocalHost extends Host
{
  LocalHost(boolean setIPAddress)
  {
    setPlatformID();

    if (setIPAddress)
       try
       {
         InetAddress internetAddress = InetAddress.getLocalHost();
         super.setAddress(internetAddress);
       }
       catch (java.net.UnknownHostException excp)
       {
       }
  }

  private void setPlatformID()
  {
    String osName = System.getProperty("os.name");
    if(Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "os.name="+osName);

    if (osName.equals("AIX"))
       setPlatformID(AIX, false);
    else
    if (osName.equals("Windows NT") | osName.equals("Windows 2000"))
       setPlatformID(WindowsNT, false);
    else
    if (osName.equals("Windows 95"))
       setPlatformID(Windows95, false);
    else
    if (osName.equals("OS/2"))
       setPlatformID(OS2, false);
    else
    if (osName.equals("Linux"))
       setPlatformID(Linux, false);
    else
    if (osName.equals("HP-UX"))
       setPlatformID(HPUX, false);
    else
    if (osName.equals("SunOS") || osName.equals("Solaris"))  // SunOS is for JDK 1.2+
    	setPlatformID(SUNOS, false);                         // Solaris is for JDK1.1
 }

  /**
   * Start a debug engine on the local host.
   * @param engineInfo An object which describes the engine to be started.
   * The method EngineInfo.canBeExeced must return true for this object in order
   * for the engine to be started (i.e. the engineInfo object must represent
   * an <i>executable</i> debug engine).
   * @param productInfo An object which specifies various product-specific
   * information required to start a debug engine e.g. the product prefix,
   * the installation directory, etc.
   * @param connectionInfo An object which specifies how the UI wants to
   * communicate with the debug engine. This object, along with the
   * EngineInfo object, will be used to obtain a string containing the
   * arguments that will be passed to the engine in order to get it to
   * use the specified comm protocol.
   * @param engineArgs An EngineArgs object which specifies additional arguments
   * that are to be passed to the engine, the engine's interpreter, and/or
   * the debuggee's interpreter. Note that several arguments to the engine
   * are automatically constructed based on other information passed into
   * this method (in the connectionInfo object, for example), but the
   * engineArgs object allows client code to pass additional information to
   * the Model that will be used to construct other miscellaneous arguments
   * to the engine.
   * @return true if the engine was loaded successfully, false otherwise.
   */

  public boolean loadEngine(EngineInfo engineInfo,
                            ProductInfo productInfo,
                            ConnectionInfo connectionInfo,
                            EngineArgs engineArgs)
  {
    String cmdArray[] = engineInfo.getCompleteInvocationCommand(productInfo,
                                                                engineArgs,
                                                                connectionInfo,
                                                                getPlatformID());
    try
    {
      String[] envars = engineArgs.getEngineEnvars();
      if( envars == null )
         Runtime.getRuntime().exec(cmdArray);
      else
      {
         int envarCount=0;
         if(envars!=null)
            envarCount=envars.length;
         if(Model.TRACE.EVT && Model.traceInfo())
         {
            for(int i=0; i<envarCount; i++)
            {
               Model.TRACE.evt(1, "LocalHost.loadEngine envar="+envars[i]);
            }
         }

         java.util.Properties old_props = null;
//*******************************************************************************
//********** The following code attempts to change the CurrentWorkingDirectory but
//********** the change is **NOT** passed through to the Exec'ed process.  Instead,
//********** the engine wrappers **could** be changed to accept a -qCWD parameter
/*
         String CWD = "DER_CWD_=";
         for (int i=0; i<envars.length; i++)
            if(envars[i].startsWith(CWD))
            {
               String value = envars[i].substring(CWD.length());
               if(Model.TRACE.EVT && Model.traceInfo())
                  Model.TRACE.evt(1, "LocalHost.loadEngine "+CWD+value);
               old_props = System.getProperties();
               java.util.Properties new_props = System.getProperties();
               String DIR = "user.dir";
               String ss = (String)new_props.get(DIR);
               if(Model.TRACE.DBG && Model.traceInfo())
                  Model.TRACE.dbg(1, "LocalHost.loadEngine OLD "+DIR+"="+ss);
               String old = (String)new_props.put(DIR,value);
               if(Model.TRACE.DBG && Model.traceInfo())
                  Model.TRACE.dbg(1, "LocalHost.loadEngine REPLACED "+DIR+"="+old);
               ss = (String)new_props.get(DIR);
               if(Model.TRACE.DBG && Model.traceInfo())
                  Model.TRACE.dbg(1, "LocalHost.loadEngine NEW "+DIR+"="+ss);
               System.setProperties(new_props);
               break;
            }
//*******************************************************************************
*/
         Runtime.getRuntime().exec(cmdArray,envars);
         if(old_props!=null)
         {
               System.setProperties(old_props);
         }
      }
      return true;
    }
    catch(SecurityException excp)
    {
      if (Model.TRACE.ERR && Model.traceInfo())
        Model.TRACE.err(1, "Security excp thrown");
      return false;
    }
    catch(java.io.IOException excp)
    {
      if (Model.TRACE.ERR && Model.traceInfo())
        Model.TRACE.err(1, excp.getMessage());
      return false;
    }
  }
}
