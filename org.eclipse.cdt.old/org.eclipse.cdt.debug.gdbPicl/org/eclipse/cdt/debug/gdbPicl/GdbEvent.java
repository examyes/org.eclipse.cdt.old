/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;



class GdbEvent 
{
  GdbEvent()
  {  }
  public static GdbEvent dummyEvent()
  { return new DummyEvent(); }
}

class ThreadDeathEvent extends GdbEvent  {}
class ClassPrepareEvent extends GdbEvent  {}
class ClassUnloadEvent extends GdbEvent  {}
class BreakpointEvent extends GdbEvent  {}
class StepEvent extends GdbEvent  {}
class ExceptionEvent extends GdbEvent  {}
class DummyEvent extends GdbEvent  {}

