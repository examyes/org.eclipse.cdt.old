/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.6)
///////////////////////////////////////////////////////////////////////

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
