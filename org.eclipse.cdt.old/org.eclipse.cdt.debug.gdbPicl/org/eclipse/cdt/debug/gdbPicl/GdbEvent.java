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
// Version %I% (last modified %G% %U%)   (based on Jde 6/2/98 1.101)
///////////////////////////////////////////////////////////////////////

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

