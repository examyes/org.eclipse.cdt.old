//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class extends the View class to implement a disassembly view.
 */
abstract class DisassemblyView extends View
//mark it abstract so nobody can new one of this directly although it has no abstract method
{

   DisassemblyView(DebugEngine debugEngine, Part parentPart)
   {
      super(debugEngine,  parentPart);
      _noViewLines[_noViewLines.length-1] = _debugEngine.getResourceString("DISASSEMBLY_NOT_AVAILABLE_MSG");
   }

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

System.out.println("#### DisassemblyView.verifyView part="+_parentPart.getFullPartName() + "(" + _viewBaseFileName + ")" );
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

   public String convertDisassemblyLineToAddress(String lineNum)
   {
      String lineAddress = null;
      int targetLine = Integer.parseInt(lineNum);
      if(_viewLines==null || _viewLines.size()<targetLine)
      {
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(3,"DisassemblyView.convertDisassemblyLineToAddress targetLine="+targetLine+" _viewLines="+_viewLines );
          return lineAddress;
      }

      String str = (String)_viewLines.elementAt(targetLine-1);
      String keyword = "0x";
      int address = str.indexOf(keyword);
      if(address<0)
          return lineAddress;
      str = str.substring(address);
      int end = str.indexOf(" ");
      lineAddress = str.substring(0,end);
System.out.println("@@@@ DisassemblyView.convertDisassemblyLineToAddress lineNum="+lineNum+" lineAddress="+lineAddress );
      return lineAddress;
   }

   public String convertAddressToDisassemblyLine(String targetAddress)
   {
      String lineNum = null;
      if(_viewLines==null || _viewLines.size()==0)
      {
          verifyView( _parentPart.getName() );
      } 

      if(_viewLines==null || _viewLines.size()==0)
      {
          if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"DisassemblyView.convertAddressToDisassemblyLine targetAddress="+targetAddress+" _viewLines=null" );
          return lineNum;
      }

      int totalLines = _viewLines.size();
      for(int i=1; i<=totalLines; i++)
      {
         String str = (String)_viewLines.elementAt(i-1);
         String keyword = "0x";
         int address = str.indexOf(keyword);
         if(address<0)
             return lineNum;
         str = str.substring(address);
         int end = str.indexOf(" ");
         String lineAddress = str.substring(0,end);
System.out.println("@@@@ DisassemblyView.convertAddressToDisassemblyLine target="+targetAddress+" i="+i+" lineAddress="+lineAddress );
         if(targetAddress.equals(lineAddress))
            return String.valueOf(i);
      }
      return lineNum;
   }

  /**
   * Attempts to load the disassembly file and create index of line number locations.
   */
   public boolean verifyView(String sourceFileName) 
   {
System.out.println("#### DisassemblyView.verifyView(STRING) sourceFileName="+sourceFileName  );
      File file;
      long offset = 0;

      // If the filename is null or empty, this indicates we should
      // put up a source not available message.  We tell the user we are
      // verified.
      _fakeNoSource = false;
      if (sourceFileName == null || sourceFileName.length() == 0)
      {
         _parentPart.setPartChanged(true);
         _fakeNoSource = true;
         return true;
      }

      file = new File(sourceFileName);
      if (!file.exists() || file.isDirectory())
      {
         return false;
      }

      GdbDebugSession gdbDebugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      String startAddress = ((GdbPart)_parentPart).getStartAddress();
      String endAddress = ((GdbPart)_parentPart).getEndAddress();
      String[] lines = gdbDebugSession._getGdbFile.getDisassemblyLines(startAddress, endAddress);

      int lineNum   = 0;
      int maxLength = 0;
      _viewLines.removeAllElements();
      String srcLine = null;
      while (  lineNum<lines.length && (srcLine=lines[lineNum])!=null )
      {
         srcLine = processViewLine(lineNum+1, " "+srcLine);

         _viewLines.addElement(srcLine);
                  
         if (srcLine.length() > maxLength)
            maxLength = srcLine.length();

         lineNum++;
      }

      setViewRecordLength(maxLength);
      setViewLineRange(1, lineNum);
      setExecutableLines();

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"File opened and read: " + sourceFileName);
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"Read " + Integer.toString(_viewNumLines) + " lines");

      _viewVerify = true;
      setViewFileName(1,sourceFileName);
      _parentPart.setPartChanged(true);
      return true;
   }

  /**
   * Process a line of disassembly into a format suitable for front-end viewing.  The
   * line number is prepended to the disassembly line, tabs are converted into spaced
   * and newlines/carriage returns are filtered out. 
   */
   private String processViewLine(int lineNum, String srcLine)
   {
System.out.println("#### DisassemblyView.processViewLine lineNum="+lineNum +" srcLine="+srcLine  );
      StringBuffer processedLine;

      // create the prefix
      processedLine = new StringBuffer(Integer.toString(lineNum));
      while (processedLine.length() < _prefixl)
         processedLine.insert(0, ' ');

       // Strip off possible new line character at end
      if (srcLine.length() > 0 && srcLine.charAt(srcLine.length()-1) == '\n')
         srcLine = srcLine.substring(0, srcLine.length()-1);

      // Strip off possible carriage return at end
      if (srcLine.length() > 0 && srcLine.charAt(srcLine.length()-1) == '\r')
         srcLine = srcLine.substring(0, srcLine.length()-1);

      processedLine.append(srcLine);

      // change tab characters into spaces (mod8)
      for (int i=0; i<processedLine.length(); i++) 
      {
         if (processedLine.charAt(i) ==  '\t') 
         {
            processedLine.setCharAt(i, ' ');
            int column = (i)%8;
            int spaces = 7-column;
            if(i<=22) spaces +=8; // allow space for longer function names
            if(i<=30) spaces +=8;
            //if(i<=38) spaces +=8;
            for(int z=1; z<spaces; z++)
                processedLine.insert(i+z, " ");
            i += spaces;
         }
         else if (processedLine.charAt(i) == '\n' || processedLine.charAt(i) == '\r')
         {
            processedLine.setCharAt(i, ' ');
         }
      }
      return processedLine.toString();
   }

   /**
    * Populates this view's _viewLines vector with disassembly lines (if they exist)
    * and adds them to the EPDC Part Get reply.
    * @param rep the reply to add the source lines to
    * @param startLine the first line to get
    * @param numLines,the number of lines to get
    */
   public void getViewLines(ERepPartGet rep, int startLine, int numLines)
   {
System.out.println("#### DisassemblyView.getViewLines startLine="+startLine +" numLines="+numLines  );
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
System.out.println("#### DisassemblyView.stringSearch="+searchString+" startLine="+startLine +" numLinesToSearch="+numLinesToSearch  );
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

System.out.println("#### DisassemblyView.addViewToReply viewNo="+viewNo  );
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
   protected int          _moduleID;
   protected ModuleManager _moduleManager;
  }
