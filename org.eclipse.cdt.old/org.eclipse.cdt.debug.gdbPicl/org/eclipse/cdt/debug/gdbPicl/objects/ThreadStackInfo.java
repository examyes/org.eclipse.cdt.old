/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

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
