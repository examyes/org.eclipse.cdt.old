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
// Version %I% (last modified %G% %U%)   (based on Jde 1.2)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.util.*;

public class ThreadStackInfo
   {

      public ThreadStackInfo(int DU, short partID, int entryID, int numLocals, int stackEntryNum)
      {
         _DU         = DU;
         _partID     = partID;
         _entryID    = entryID;
         _numLocals  = numLocals;
         _exprIDs    = new Hashtable();
         _stackEntryNum = stackEntryNum;
      }

      public int getDU() { return _DU; }
      public short getpartID() { return _partID; }
      public int getentryID() { return _entryID; }
      public int getnumLocals() { return _numLocals; }
      public Hashtable getexprIDs() { return _exprIDs; }
      public int getStackEntryNum() { return _stackEntryNum; }

      public void setpartID(short partID) { _partID = partID; }
      public void setentryID(int entryID) { _entryID = entryID; }
      public void setnumLocals (int numLocals) { _numLocals = numLocals; }

      private int       _DU;             // The thread DU we are monitoring local
                                         // variables for
      private short     _partID;         // The class name we were in
      private int       _entryID;        // The method name we were in
      private int       _numLocals;      // The number of local variables we had showing
      private Hashtable _exprIDs;        // Hash table of (varName,exprID) pairs
      private int       _stackEntryNum;  // Stack entry number. This is used when locals are
                                 //  requested at a stack entry other than the current
                                 // NOTE: a stackentry number of 0 means monitor locals
                                 //       at the programs current stopped location in
                                 //       the thread

   }
