//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;

import com.ibm.debug.epdc.*;
/**
 * Abstract superclass to all classes that manage EPDC components(i.e.
 * parts, threads, modules, etc.
 */
abstract class ComponentManager
{
   DebugSession _debugSession = null;
   DebugEngine  _debugEngine = null;
   
   public ComponentManager()
   {
   }

   public ComponentManager(DebugSession debugSession)
   {
      _debugSession = debugSession;
      _debugEngine  = _debugSession.getDebugEngine();
   }

   /** Adds change packets for this component to a reply packet */
   abstract void addChangesToReply(EPDC_Reply rep);
}
