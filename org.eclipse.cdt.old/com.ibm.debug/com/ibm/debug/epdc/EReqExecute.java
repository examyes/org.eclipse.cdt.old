package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqExecute.java, java-epdc, eclipse-dev, 20011129
// Version 1.8.1.3 (last modified 11/29/01 14:15:30)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/**
 * Class for Execute Program request.
 */
public class EReqExecute extends EPDC_Request {
   public EReqExecute(int DU, byte how, EStdView stdView)
   {
     super(EPDC.Remote_Execute);
     _DU = DU;
     _How = how;
     _RView = stdView;
     _NSteps = 1;
   }

   EReqExecute(byte[] inBuffer) throws IOException
   {
      super (inBuffer);
      _DU = readInt();
      _How = readChar();
      readChar();       // reserved
      _RView = new EStdView(inBuffer, getOffset());
      _NSteps = readInt();
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      dataOutputStream.writeInt(_DU);
      dataOutputStream.writeByte(_How);
      dataOutputStream.writeByte(0);
      _RView.output(dataOutputStream);
      dataOutputStream.writeInt(_NSteps);
   }

   /**
    * Get the thread ID to execute
    */
   public int getDU() {
      return _DU;
   }

   /**
    * Get the type of execution requested
    * @see EPDC
    */
   public byte getHowExecute() {
      return _How;
   }

   /**
    * Get view information
    */
   public EStdView getViewInfo() {
      return _RView;
   }

   /**
    * Get number of steps requested, if a step execute was requested
    */
   public int getNSteps() {
      return _NSteps;
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length + EStdView._fixedLen() + super.fixedLen();
   }

   /** Return the length of the variable component */
   protected int varLen() {
      return super.varLen();
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     String how;

     switch(_How)
     {
       case EPDC.Exec_Step:
            how = "StepDebug";
            break;

       case EPDC.Exec_StepOver:
            how = "StepOver";
            break;

       case EPDC.Exec_StepInto:
            how = "StepInto";
            break;

       case EPDC.Exec_StepReturn:
            how = "StepReturn";
            break;

       case EPDC.Exec_Go:
            how = "Run";
            break;

       case EPDC.Exec_GoBypass:
            how = "GoBypass";
            break;

       case EPDC.Exec_GoTo:
            how = "JumpToLocation";
            break;

       case EPDC.Exec_RunToCursor:
            how = "RunToLocation";
            break;

       case EPDC.Exec_Undo:
            how = "Undo";
            break;

       case EPDC.Exec_GoException:
            how = "GoException";
            break;

       case EPDC.Exec_GoExceptionRun:
            how = "GoExceptionRun";
            break;

       case EPDC.Exec_ForkFollowChild:
            how = "ForkFollowChild";
            break;

       case EPDC.Exec_ForkFollowParent:
            how = "ForkFollowParent";
            break;

       default:
            how = "UNKNOWN: " + _How;
            break;

     }

     indent(printWriter);
     printWriter.println("How : " + how + "   Thread : " + _DU);

     if (getDetailLevel() >= DETAIL_LEVEL_MEDIUM)
     {
	indent(printWriter);
        _RView.write(printWriter);
        printWriter.println();
     }
   }

   // data fields
   private int _DU;
   private byte _How;
   private EStdView _RView;
   private int _NSteps;

   private final static int _fixed_length = 10;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
