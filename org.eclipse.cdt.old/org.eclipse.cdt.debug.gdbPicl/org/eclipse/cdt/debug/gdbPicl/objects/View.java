/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/View.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:50)   (based on Jde 10/12/97 1.20)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class stores information corresponding to an EPDC "View". 
 */
public abstract class View
{
   public View(DebugEngine debugEngine, Part parentPart)
   {
      _debugEngine  = debugEngine;
      _parentPart   = parentPart;

      _viewValidated           = true;
      _viewFileName            = null;
      _viewVerify              = false;
      _viewVerify_Attempted    = false;
      _viewVerify_Attempted_FE = false;
      _viewVerify_Local        = false;
      _viewCanChange           = true;
      _viewRecLength           = 0;
      _viewLines               = new Vector();
      _viewStartLine           = -1;
      _viewEndLine             = -1;
      _viewNumLines            = -1;

      _executableLines = new int[0];

      _noViewLines = new String[3];
      _noViewLines[0] = "";
      _noViewLines[1] = "";
      _noViewLines[2] = _debugEngine.getResourceString("SOURCE_NOT_AVAILABLE_MSG");
   }

   /**
    * Attempt to verify this view using the classpath and current base filename.
    */
   public abstract boolean verifyView();

   /**
    * Attempt to verify this view with the provided filename.
    */
   public abstract boolean verifyView(String srcFileName);

   /**
    * Populate this view's _viewLines vector beginning at startLine 
    * for numLines lines. The lines are added to the EPDC Part Get reply.
    */ 
   public abstract void getViewLines(ERepPartGet rep, int startLine, int numLines);

   /**
    * Searches this view for searchString starting at startLine, startColumn 
    * for up to numLinesToSearch and returns a two dimensional array object 
    * with the position line at index 0 and the column at index 1
    */
   public abstract int[] stringSearch(int srcFileIndex, int startLine,int startColumn,int numLinesToSearch,String searchString, int searchFlags);

   /**
    * Adds this view to the specified reply.
    */
   abstract void addViewToReply(ERepNextPart nextPartRep);

   /**
    * Sets the view attributes in this view to match those found in the 
    * specified EViews.
    */
   public void setViewInfo(int srcFileIndex, EViews eview)
   {
      // We ignore the srcFileIndex since we can only have one file for 
      // each class

      if (eview.name() != null) // only if it is not null defect 14341
        _viewFileName            = eview.name();
      _viewVerify              = eview.hasBeenVerified();
      _viewVerify_Attempted    = eview.verificationAttempted();
      _viewVerify_Attempted_FE = eview.verificationAttemptedFE();
      _viewVerify_Local        = eview.verificationLocal();
      _viewCanChange           = eview.fileNameCanBeOverridden();
      _viewRecLength           = eview.recordLength();
      setViewLineRange(eview.firstLineNumber(), eview.lastLineNumber());
   }

   void setViewRecordLength(int recordLength)
   {
      _viewRecLength = recordLength;
   }

   int getViewRecordLength()
   {
      return _viewRecLength;
   }

   void setViewValidated(boolean validated)
   {
      _viewValidated = validated;
   }

   boolean isViewValidated()
   {
      return _viewValidated;
   }

   public void setViewVerify(boolean verified)
   {
      _viewVerify = verified;
   }

   public boolean isViewVerify()
   {
      return _viewVerify;
   }

   public void setViewVerifyAttempted(boolean attempted)
   {
      _viewVerify_Attempted = attempted;
   }

   boolean isViewVerifyAttempted()
   {
      return _viewVerify_Attempted;
   }

   public void setViewVerifyAttemptedFE(boolean attempted)
   {
      _viewVerify_Attempted_FE = attempted;
   }

   public boolean isViewVerifyAttemptedFE()
   {
      return _viewVerify_Attempted_FE;
   }

   public void setViewVerifyLocal(boolean local)
   {
      _viewVerify_Local = local;
   }

   public boolean isViewVerifyLocal()
   {
      return _viewVerify_Local;
   }

   void setViewCanChange(boolean change)
   {
      _viewCanChange = change;
   }

   boolean isViewCanChange()
   {
      return _viewCanChange;
   }

   void setViewLineRange(int startLine, int endLine)
   {
      _viewStartLine = startLine;
      _viewEndLine   = endLine;
      _viewNumLines  = (_viewEndLine - _viewStartLine) + 1;
   }

   int getViewNumLines()
   {
      return _viewNumLines;
   }

   String getViewFileName()
   {
      return _viewFileName;
   }

   /**
    * Set the source file name for this part
    */
   void setViewFileName(int srcFileIndex, String fileName)
   {
      Gdb.debugOutput( "Setting source file for part " + _parentPart.getFullPartName() + " to " + fileName);

      _viewFileName = fileName;
   }

   /**
    * Return the base source file name for this part
    */
   public String getBaseViewFileName()
   {
      return _viewBaseFileName;
   }

   /**
    * Set the source file name for this part
    */
   void setBaseViewFileName(String fileName)
   {
      Gdb.debugOutput( "Setting base source view file for part " + _parentPart.getFullPartName() + " to " + fileName);

      _viewBaseFileName = fileName;
   }

   // Data members
   static byte _prefixl = 5;

   DebugEngine  _debugEngine;
   Part         _parentPart;

   Vector  _viewLines;
   boolean _viewValidated;
   
   String  _viewFileName;
   String  _viewBaseFileName;
   boolean _viewVerify;
   boolean _viewVerify_Attempted;
   boolean _viewVerify_Attempted_FE;
   boolean _viewVerify_Local;
   boolean _viewCanChange;
   int     _viewRecLength;
   int     _viewStartLine;
   int     _viewEndLine;
   int     _viewNumLines;

   public void setExecutableLines() 
   {
      int total = _viewLines.size();
      _executableLines = new int[total];
      for(int i=0; i<total; i++)
      {
          _executableLines[i]=i+1;
      }
   }

   public String getViewLine(int lineNumber)
   {
      if(_viewLines==null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"View.getViewLine _viewLines==null");
         return null;
      }
      if(lineNumber<_viewStartLine || lineNumber>_viewEndLine )
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"View.getViewLine lineNumber="+lineNumber+" _viewStartLine="+_viewStartLine+" _viewEndLine="+_viewEndLine );
         return null;
      }
      return (String)_viewLines.elementAt(lineNumber-_viewStartLine);
   }
   protected int[]    _executableLines;
   protected boolean  _fakeNoSource = false;
   // This is displayed when there are no view lines available.
   protected String[] _noViewLines;
}
