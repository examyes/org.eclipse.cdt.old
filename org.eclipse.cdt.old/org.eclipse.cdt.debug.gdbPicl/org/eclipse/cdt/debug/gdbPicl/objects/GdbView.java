/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class stores information corresponding to an EPDC "GdbView". 
 */
abstract class GdbView
{
   GdbView(GdbDebugEngine debugEngine, GdbPart parentPart)
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
   }

   /**
    * Attempt to verify this view using the classpath and current base filename.
    */
   abstract boolean verifyView();

   /**
    * Attempt to verify this view with the provided filename.
    */
   abstract boolean verifyView(String srcFileName);

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
   void setViewInfo(int srcFileIndex, EViews eview)
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

   void setViewVerify(boolean verified)
   {
      _viewVerify = verified;
   }

   boolean isViewVerify()
   {
      return _viewVerify;
   }

   void setViewVerifyAttempted(boolean attempted)
   {
      _viewVerify_Attempted = attempted;
   }

   boolean isViewVerifyAttempted()
   {
      return _viewVerify_Attempted;
   }

   void setViewVerifyAttemptedFE(boolean attempted)
   {
      _viewVerify_Attempted_FE = attempted;
   }

   boolean isViewVerifyAttemptedFE()
   {
      return _viewVerify_Attempted_FE;
   }

   void setViewVerifyLocal(boolean local)
   {
      _viewVerify_Local = local;
   }

   boolean isViewVerifyLocal()
   {
      return _viewVerify_Local;
   }

   void setGdbViewCanChange(boolean change)
   {
      _viewCanChange = change;
   }

   boolean isGdbViewCanChange()
   {
      return _viewCanChange;
   }

   void setViewLineRange(int startLine, int endLine)
   {
      _viewStartLine = startLine;
      _viewEndLine   = endLine;
      _viewNumLines  = (_viewEndLine - _viewStartLine) + 1;
   }

   int getGdbViewNumLines()
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
      Gdb.debugOutput( "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ GdbView.setViewFileName Setting source file for part " + _parentPart.getFullPartName() + " to " + fileName);

      _viewFileName = fileName;
   }

   /**
    * Return the base source file name for this part
    */
   String getBaseViewFileName()
   {
      return _viewBaseFileName;
   }

   /**
    * Set the source file name for this part
    */
   void setBaseViewFileName(String fileName)
   {
      Gdb.debugOutput( "GdbView.setBaseFileName Setting base source view file for part " + _parentPart.getFullPartName() + " to " + fileName);

      _viewBaseFileName = fileName;
   }

   // Data members
   static byte _prefixl = 5;

   GdbDebugEngine  _debugEngine;
   GdbPart         _parentPart;

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
}
