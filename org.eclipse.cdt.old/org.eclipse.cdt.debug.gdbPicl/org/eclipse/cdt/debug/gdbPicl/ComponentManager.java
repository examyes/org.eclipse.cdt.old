/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;

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
