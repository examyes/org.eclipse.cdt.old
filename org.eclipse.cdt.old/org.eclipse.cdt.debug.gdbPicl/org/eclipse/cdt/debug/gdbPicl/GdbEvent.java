//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;

import java.util.*;
import java.net.*;
import java.io.*;
import java.text.*;
import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.util.Semaphore;

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

