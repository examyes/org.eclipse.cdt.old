/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class extends the View class to implement a mixed view.
 */
abstract class MixedView extends View
//mark it abstract so nobody can new one of this directly although it has no abstract method
{

   MixedView(DebugEngine debugEngine, Part parentPart)
   {
      super(debugEngine,  parentPart);
      _noViewLines[_noViewLines.length-1] = _debugEngine.getResourceString("MIXED_NOT_AVAILABLE_MSG");
   }

   public void setDisassemblyLines() 
   {
      _DisassemblyFirst = null;
      _DisassemblyLast  = null;

      int total = _viewLines.size();
      _DisassemblyFirst = new int[total];
      _DisassemblyLast  = new int[total];
      for(int i=0; i<total; i++)
      {
          _DisassemblyFirst[i]=2*i;
          _DisassemblyLast[i]=2*i+1;
      }
   }
   protected int[]    _DisassemblyFirst;
   protected int[]    _DisassemblyLast;


   /**
    * Verify this view.
    */
   public boolean verifyView()
   {
      if (_viewVerify)
         return true;

      int     pathIndex     = 0;
      String  fullPathName  = "";

      boolean verified      = false;

System.out.println("#### MixedView.verifyView part="+_parentPart.getFullPartName() + "(" + _viewBaseFileName + ")" );
      Gdb.debugOutput("Verifying part " + _parentPart.getFullPartName() + "(" + _viewBaseFileName + ")" );
   /**
    * even if source is not available the basefilename should still
    * be available - added by defect 14341
    */
      setViewFileName(1,_viewBaseFileName); 

      // Let's try the source file name directly.  This may have been set
      // by the user via dialog box.
      verified = verifyView(_viewBaseFileName);

      // First try to find the source from the class path entries
      while (!verified && (pathIndex < _debugEngine.getSrcPaths().size()))		//HC:11104
      {
         fullPathName = (String) _debugEngine.getSrcPaths().elementAt(pathIndex) + _viewBaseFileName;  //HC:11104

         verified = verifyView(fullPathName);

         pathIndex++;
      }

      _parentPart.setPartChanged(true);
      _viewVerify_Attempted = true;

      return verified;
   }

  /**
   * Attempts to combine the source View and the disassembly Views.
   */
   public boolean combineSourcePlusDisassemblyViews(String sourceFileName) 
   {
System.out.println("#### MixedView.combineSourcePlusDisassemblyViews(STRING) sourceFileName="+sourceFileName  );
      _fakeNoSource = true;

      if(_parentPart==null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"MixedView.verifyView _parentPart==null");
         return true;
      }

      sourceView = (SourceView)_parentPart.getView(Part.VIEW_SOURCE);
      disassemblyView = (DisassemblyView)_parentPart.getView(Part.VIEW_DISASSEMBLY);
      if(sourceView==null || disassemblyView==null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"MixedView.verifyView source||disassembly==null source="+sourceView+" disassembly="+disassemblyView );
         return true;
      }
      if(!sourceView.isViewVerify() || !disassemblyView.isViewVerify())
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"MixedView.verifyView source||disassembly !isViewVerify source.isViewVerify()="+sourceView.isViewVerify()+" disassembly.isViewVerify()="+disassemblyView.isViewVerify() );
         return false;
      }

      int totalSource = sourceView.getViewNumLines();
      int totalDisassembly = disassemblyView.getViewNumLines();
System.out.println("MixedView.verifyView totalSource="+totalSource+" totalDisassembly="+totalDisassembly );
      if(totalSource<=0 || totalDisassembly<=0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"MixedView.verifyView totalSource||totalDisassembly<=0 totalSource="+totalSource+" totalDisassembly="+totalDisassembly );
         return true;
      }

      _executableLines = new int[totalDisassembly];
      nextExecutableLine = 0;
      int mixedCurrentLine = 1;
      
      for(int i=0; i<totalSource; i++)
      {
         int sourceStartLine = i+1;
         int sourceEndLine = i+1;
         int disassemblyStartLine = 3*i+1;
         int disassemblyEndLine = 3*i+3; 

         mixedCurrentLine = combineSourcePlusDisassemblyLines(mixedCurrentLine, sourceStartLine, sourceEndLine,
                                                             disassemblyStartLine, disassemblyEndLine);
         if(mixedCurrentLine<=0)
            return true;
      }

      int srcMax = sourceView.getViewRecordLength();
      int disMax = disassemblyView.getViewRecordLength();
      if(srcMax>disMax) 
          setViewRecordLength(srcMax);
      else
          setViewRecordLength(disMax);

      setViewLineRange(1,_viewLines.size() );

      _fakeNoSource = false;
      _viewVerify = true;
      setViewFileName(1,sourceFileName);
      _parentPart.setPartChanged(true);
      return true;
   }

  /**
   * Attempts to combine the source Line and the disassembly Lines.
   */
   public int combineSourcePlusDisassemblyLines(int mixedCurrentLine, int sourceStartLine, int sourceEndLine,
                                               int disassemblyStartLine, int disassemblyEndLine) 
   {
System.out.println("#### MixedView.combineSourcePlusDisassemblyLines sourceStartLine="+sourceStartLine+"  sourceEndLine="+sourceEndLine
                    +" disassemblyStartLine="+disassemblyStartLine+" disassemblyEndLine="+disassemblyEndLine );

      if(sourceStartLine>0 && sourceEndLine>0)
      for(int i=sourceStartLine; i<=sourceEndLine; i++)
      {
         String line = sourceView.getViewLine(i);
         if(line==null)
         {
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(2,"MixedView.combineSourcePlusDisassemblyLines string==null for sourceLineNumber="+i );
            return -1;
         }
         line = processViewLine(mixedCurrentLine, line);
         _viewLines.addElement(line);
System.out.println("MixedView.combineSourcePlusDisassemblyLines lineNumber="+mixedCurrentLine+" source="+line );
         mixedCurrentLine++;
      }

      if(disassemblyStartLine>0 && disassemblyEndLine>0)
      for(int i=disassemblyStartLine; i<=disassemblyEndLine; i++)
      {
         String line = disassemblyView.getViewLine(i);
         if(line==null)
         {
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(2,"MixedView.combineSourcePlusDisassemblyLines string==null for disassemblyLineNumber="+i );
            return -1;
         }
         line = processViewLine(mixedCurrentLine, line);
         _viewLines.addElement(line);
System.out.println("MixedView.combineSourcePlusDisassemblyLines lineNumber="+mixedCurrentLine+" disassembly="+line );
         _executableLines[nextExecutableLine++] = mixedCurrentLine;
         mixedCurrentLine++;
      }

      return mixedCurrentLine;
   }

  /**
   * Attempts to load the source file and create index of line number locations.
   */
   public boolean verifyView(String sourceFileName) 
   {
System.out.println("#### MixedView.verifyView(STRING) sourceFileName="+sourceFileName  );

       return combineSourcePlusDisassemblyViews(sourceFileName);
    }

  /**
   * Process a line of source into a format suitable for front-end viewing.  The
   * old prefix is removed, and the new line number is prepended to the source line.
   */
   private String processViewLine(int lineNum, String srcLine)
   {
System.out.println("#### MixedView.processViewLine lineNum="+lineNum +" srcLine="+srcLine  );
      StringBuffer processedLine;

      // Strip off old prefix
      srcLine = srcLine.substring(_prefixl);

      // create new prefix      
      processedLine = new StringBuffer(Integer.toString(lineNum));
      while (processedLine.length() < _prefixl)
         processedLine.insert(0, ' ');

      // combine prefix + viewLine
      processedLine.append(srcLine);

      return processedLine.toString();
   }

   /**
    * Populates this view's _viewLines vector with source lines (if they exist)
    * and adds them to the EPDC Part Get reply.
    * @param rep the reply to add the source lines to
    * @param startLine the first line to get
    * @param numLines,the number of lines to get
    */
   public void getViewLines(ERepPartGet rep, int startLine, int numLines)
   {
System.out.println("#### MixedView.getViewLines startLine="+startLine +" numLines="+numLines  );
      int lineNum, index;
      index   = 0;
      String srcLine;
      StringBuffer processedLine;

      if (_executableLines == null || _executableLines.length == 0)
         Gdb.debugOutput("Warning: Class has no executable line info.\n");

      if (!_viewVerify)
      {
         for (lineNum = 0; lineNum < _noViewLines.length; 
            lineNum++)
         {
            processedLine = new StringBuffer(_noViewLines[lineNum]);
            // create the prefix
            for (int i=0;i<_prefixl;i++)
              processedLine.insert(0,' ');
            rep.addSrcLine(processedLine.toString(),false,false);
         }
         return;
      }

      lineNum = startLine;
      while ((lineNum <= _viewEndLine) && ((lineNum - startLine) < numLines)) 
      {
         if (_viewVerify_Local)
         {
            // NOTE: The extra " " here will be replaced by the 
            // EPDC.SourceLinePlaceHolder character.  This should be removed
            // when the front end has implemented the local source bit.  That
            // is, only use 1 space.
            srcLine = processViewLine(lineNum,"  ");
         }
         else
         {
            srcLine = (String) _viewLines.elementAt(lineNum-1);
         }

         if (_executableLines != null && _executableLines.length > 0)
         {
            // find out if the line is executable
            while ( (index < _executableLines.length-1) &&
               ((_executableLines[index] < lineNum))) 
            {
               index++;
            }

            boolean isExecutable = (_executableLines[index] == lineNum);

            rep.addSrcLine(srcLine, isExecutable, _viewVerify_Local);

            lineNum++;
         }
         else
         {
            rep.addSrcLine(srcLine, false, _viewVerify_Local);
            lineNum++;
         }
      }
   } 

   /**
    * Searches this view for searchString starting at startLine, startColumn for
    * up to numLinesToSearch and returns a two dimensional array object with the position
    * line at index 0 and the column at index 1
    */
   public int[] stringSearch(int srcFileIndex, int startLine,int startColumn,int numLinesToSearch,String searchString, int searchFlags)
   {
System.out.println("#### MixedView.stringSearch="+searchString+" startLine="+startLine +" numLinesToSearch="+numLinesToSearch  );
      int line      = startLine;
      int index     = 0;
      boolean found = false;
      String srcLine;

      boolean caseSensitive = (searchFlags & EPDC.StringFindCaseSensitive) != 0;

      if (_viewVerify_Local || !_viewVerify)
      {
         return null;
      }

      // If we don't care about case, convert the search string to upper case
      if (!caseSensitive)
         searchString = searchString.toUpperCase();

      int count = 0;

      // If we're not starting the search at beginning of the start line, we will 
      // have to make the start line the last line to search.
      if (startColumn > 1)
         numLinesToSearch += 1;

      while ( count < numLinesToSearch && !found)
      {
         // Get the source line to search through and strip off the prefix
         srcLine = (String) _viewLines.elementAt(line-1);  
         srcLine = srcLine.substring(_prefixl);

         // If we don't care about case, convert the source line to upper case
         if (!caseSensitive)
            srcLine = srcLine.toUpperCase();

         // Make sure we don't fall off the end of the source line!
         if (startColumn <= srcLine.length())
         {
             srcLine = srcLine.substring(startColumn-1);
             index = srcLine.indexOf(searchString);

             if (index >= 0)
                found = true;
         }

         // If we didn't find the string, go to the next line and search from
         // column 1
         if (!found)
         {
             count++;
             line++;
             if (line > _viewLines.size())
                line = 1;
             startColumn = 1;
         }
      }

      if (found)
      {
         int[] position = new int[2];
         position[0] = line;
         position[1] = index + startColumn;
         return position;
      }
      else
      {
         return null;
      }
   }

   void addViewToReply(ERepNextPart nextPartRep)
   {
      int viewNo = nextPartRep.createView(_prefixl, _viewValidated ? EPDC.VIEW_VALIDATED : 0);

System.out.println("#### MixedView.addViewToReply viewNo="+viewNo  );
      // Determine this part's source view attributes
      int viewAttr = 0;

      viewAttr = (_viewVerify_Attempted ? EPDC.VIEW_VERIFY_ATTEMPTED : 0) | 
                 (_viewVerify_Attempted_FE ? EPDC.VIEW_VERIFY_ATTEMPTED_FE : 0) | 
                 (_viewVerify_Local ? EPDC.VIEW_LOCAL : 0) | 
                 (_viewCanChange ? EPDC.VIEW_CHANGE_TEXT_VALID : 0);

      // Only turn on VIEW_NO_SWITCH for the source view 
      viewAttr |= EPDC.VIEW_NO_SWITCH;

      // This is a requirement for the local source protocol
      if (_viewVerify_Local)
         viewAttr |= EPDC.VIEW_VERIFIED;

      if (!_viewVerify)
      {
         // Only allow fallback to no source message for source view
         if (_fakeNoSource)
         {
            // We have a null base file name, lie to the front end
            // and say this part's source view was verified even though it
            // wasn't.  We use the noSourceLines array of strings as the source
            // of our message.
            viewAttr |= EPDC.VIEW_VERIFIED;
            nextPartRep.addSrcFile(viewNo, _viewRecLength, 1, _noViewLines.length, _viewFileName, _viewBaseFileName, viewAttr);
         }
         else
         {
            nextPartRep.addSrcFile(viewNo, _viewRecLength, 1, 0, _viewFileName, _viewBaseFileName, viewAttr);
         }
      }
      else
      {
         viewAttr |= EPDC.VIEW_VERIFIED;
         nextPartRep.addSrcFile(viewNo, _viewRecLength, 1, _viewNumLines, _viewFileName, _viewBaseFileName, viewAttr);
      }
   }



   // Data members
   protected int            _moduleID;
   protected ModuleManager  _moduleManager;
   protected SourceView      sourceView;
   protected DisassemblyView disassemblyView;

   protected int             nextExecutableLine = 0;
}
