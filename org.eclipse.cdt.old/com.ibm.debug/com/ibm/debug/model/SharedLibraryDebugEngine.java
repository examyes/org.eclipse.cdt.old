package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/SharedLibraryDebugEngine.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:11:13)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * A debug engine which is packaged as a shared library (e.g. a DLL)
 * and can be loaded by SUI. This type of debug engine can only be constructed
 * with a LocalHost (as opposed to a generic Host or RemoteHost) since it
 * is assumed that a shared library can only be loaded by SUI on the same
 * machine (and in the same process) that SUI itself is running on.
 */

class SharedLibraryDebugEngine extends DebugEngine
{
   SharedLibraryDebugEngine(LocalHost host, String sharedLibraryName)
   {
     super(host);
     _sharedLibraryName = sharedLibraryName;
   }

   String sharedLibraryName()
   {
     return _sharedLibraryName;
   }

   private String _sharedLibraryName;
}
