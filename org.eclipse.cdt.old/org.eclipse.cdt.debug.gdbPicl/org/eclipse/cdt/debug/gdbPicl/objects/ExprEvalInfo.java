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
// Version %I% (last modified %G% %U%)   (based on Jde 1.6 12/6/00)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

/**
 * Holds expression evaluation status code and diagnostic information.
 */
public abstract class ExprEvalInfo
{

   public ExprEvalInfo(int         status,
	             String      diagInfo,
	             int         stackEntry,
	             int         whereFound)
   {
      _status      = status;
      _diagInfo    = diagInfo;
      _stackEntry  = stackEntry;
      _whereFound  = whereFound;
   }

   public boolean expressionFailed()
   {
      return _status != exprVARIABLEFOUND;
   }

   public String whyFailed(DebugEngine debugEngine)
   {
      Gdb.debugOutput("ExprEvalInfo: whyFailed()");
      String message = "Internal Debug Engine Error: JDE-EXPR-02";

      switch (_status)
      {
         case ExprEvalInfo.exprNOTFOUND:
            message = "EXPRESSION_EVAL_FAILED_MSG";
            break;

         case ExprEvalInfo.exprBADCLASSREF:
            message = "EXPRESSION_EVAL_CIR_NOT_FOUND_MSG";
            break;

         case ExprEvalInfo.exprNULLVALUE:
            message = "EXPRESSION_EVAL_NULL_ENCOUNTERED_MSG";
            break;

         case ExprEvalInfo.exprEXECMETHOD:
            message = "EXPRESSION_EVAL_CANT_EXEC_METHODS_MSG";
            break;

         case ExprEvalInfo.exprDEREFPRIMITIVE:
            message = "EXPRESSION_EVAL_CANT_DEREF_PRIMITIVE_MSG";
            break;

         case ExprEvalInfo.exprINVALIDCONTEXT:
            message = "EXPRESSION_EVAL_ACTIVE_METHOD_MSG";
            break;

         default:
            message = "EXPRESSION_EVAL_FAILED_MSG";
      }

      String msg;

      msg = debugEngine.getResourceString(message);

      if (_diagInfo != null)
         msg += " " + _diagInfo;

      return msg;
   }

   public Object getValue()
   {
      return _value;
   }

   public void setValue(Object value)
   {
      _value = value;
   }


   private Object _value;

   protected int         _status;       // status of evaluation
   protected String      _diagInfo;     // accompanying message
   protected int         _stackEntry;
   protected int         _whereFound;


   final static int exprVARIABLEFOUND    = 1;
   final static int exprNOTFOUND         = 0;
   final static int exprNULLVALUE        = -1;
   final static int exprBADCLASSREF      = -2;
   final static int exprEXECMETHOD       = -3;
   final static int exprINVALID          = -4;
   final static int exprDEREFPRIMITIVE   = -5;
   final static int exprFAILED           = -6;
   final static int exprINVALIDCONTEXT   = -7;

   // "Where Found" constants
   final static int WF_Unknown     = 0;
   final static int WF_Stack       = 1;
   final static int WF_Field       = 2;
   final static int WF_StaticField = 3;

}
