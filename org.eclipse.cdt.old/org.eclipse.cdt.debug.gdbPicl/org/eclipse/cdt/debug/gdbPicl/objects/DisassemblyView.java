/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

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
      _lineMap = new Hashtable();
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

      if (Gdb.traceLogger.EVT) 
              Gdb.traceLogger.evt(3, "#### DisassemblyView.verifyView part="+_parentPart.getFullPartName() + "(" + _viewBaseFileName + ")" );

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

	  if (Gdb.traceLogger.ERR) 
		Gdb.traceLogger.err(3, "@@@@ DisassemblyView.convertDisassemblyLineToAddress lineNum="+lineNum+" lineAddress="+lineAddress );
		
      return lineAddress;
   }
   
   
/**
 * Method convertLineNumToAddress.
 * Given the line number, find address for that line
 * @param lineNum
 * @return String
 *  - null on error
 *  - address otherwise
 */
   public String convertLineNumToAddress (int lineNum)
   {
		String address = null;
		
		String lineInfo = getViewLine(lineNum);
		
		if (lineInfo != null) {
			lineInfo = lineInfo.trim();
		} else {
			return null;
		}
		
		// get address from view
		if (lineInfo.startsWith("0x")) {
			int idx = lineInfo.indexOf(" ");
			if (idx > 0) {
				address = lineInfo.substring(0, idx);
			}
			else
			{
				address = lineInfo.substring(0, _prefixl);
			}
		} 		
		return address;
   }

   public String convertAddressToLineNum(String targetAddress)
   {   	
      String lineNum = null;

	  Object tmp = _lineMap.get(targetAddress);
	  if (tmp != null)
	  {
	  	// correct line number in disassembly view
	  	lineNum = (String)_lineMap.get(targetAddress).toString();
	  }
	  else
	  {
	  	// first line in disassembly if we can't map
		lineNum = "0";
	  }

      return lineNum;
   }
   
   public boolean containsAddressInView(String address)
   {
   	  if (_lineMap != null && address != null)
   	  {
	 	  Object tmp = _lineMap.get(address);
	 	  return (tmp != null);
   	  }
   	  else
   	  {
   	  	  return true;
   	  }
   }

  /**
   * Attempts to load the disassembly file and create index of line number locations.
   * Since we are now generating the disassembly view dynamically, meaning of verifyView
   * based on a filename has changed.  Before the change, the picl tries to generate the 
   * disassembly view based on a file's start and end address.  However, gdb's disassemble function
   * is based on function scope.  So, the picl will generate the view every time the debugging
   * session is changing scope, steping into a new function.  It will try to disassemble for the entire
   * function based on an address and append the disassembly source at the end of the view.  In this case, verifying
   * a view will require the picl to parse through the call stacks, find out all the stack frame addresses
   * that are associated with the file and generate the disassembly codes for the addresses.
   */
   public boolean verifyView(String sourceFileName) 
   {

	  int currentSrcLineNumIdx = 0;
	  GdbSourceView srcView = (GdbSourceView) _parentPart.getView(Part.VIEW_SOURCE);
	  SourceView.SourceLine srcViewLine;
	  
	  
	  if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"#### DisassemblyView.verifyView(STRING) sourceFileName="+sourceFileName  );
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

      GdbDebugSession gdbDebugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      
      // Get all line numbers associated with a sourceFileName from all call stack
      
      GdbThreadManager threadManager = (GdbThreadManager) gdbDebugSession.getThreadManager();
      
     int slash = sourceFileName.lastIndexOf("/");
     if (slash != -1)
      	sourceFileName = sourceFileName.substring(slash+1);
      
      Vector partThreadComponents = threadManager.getThreadComponentsforPart(sourceFileName);
      
      if (partThreadComponents.size() == 0)
      {
      	 _parentPart.setPartChanged(true);
      	 _fakeNoSource = true;
      }
      
      for (int i=0; i< partThreadComponents.size(); i++)
      {
      	// force to update call stack info during reply
      	// Since disassembly view is generated dynamically, disassembly view may
      	// not be availabe at the time when the call stack info was generated.
      	// As a result, we need to update the threads and make sure that the call
      	// stack info is valid for the new disassembly view.
		((GdbThreadComponent)partThreadComponents.elementAt(i)).setChanged();
      }

	  // update threads forces new call stacks to be included in EPDC reply      
      threadManager.updateThreads();
      
	  Vector locations = threadManager.getStackFramesforPart(sourceFileName);
	  
	  if (locations.size() == 0)
	  {
         _parentPart.setPartChanged(true);
         _fakeNoSource = true;
        
         return true;
	  }
       
	  for (int i=0; i<locations.size(); i++)
	  {
	  	  Integer line = new Integer(((GdbStackFrame)locations.elementAt(i)).getLineNumber());
	  	  String disAddress = ((GdbStackFrame)locations.elementAt(i)).getFrameAddress();
	  	  
	  	  if (disAddress == null && line.intValue() > 0)
	  	  {
	  	  	disAddress = gdbDebugSession._getGdbFile.convertSourceLineToAddress(sourceFileName, line.toString());
	  	  }
	  	  
	  	
//	  	  String disAddress = gdbDebugSession._getGdbFile.convertSourceLineToAddress(sourceFileName, ((Integer)locations.elementAt(i)).toString());
	  	  
	  	  // ignore this location if address is null
	  	  if (disAddress == null)
	  	  	continue;
	      
	      // generate the disassembly code for the location if it's not already part of the view
	      if (!containsAddressInView(disAddress))
	      {		  	
		      String[] lines = gdbDebugSession._getGdbFile.getDisassemblyLines(disAddress);	      
		
		      int lineNum   = 0;
		      int maxLength = 0;
	
		      String srcLine = null;
		      String address = null;
		      
		      int dispNumLines = _viewLines.size();
		      
		      if (lines == null)
		      	continue;
		      
		      int start = 0;
		      int end = 0;
		      
		      while (  lineNum<lines.length && (srcLine=lines[lineNum])!=null )
		      {		      			      	
		      	 int space = srcLine.indexOf(" ");
		      	 if (space != -1)
		      	 {
					 address = srcLine.substring(0, space);
		      	 }
		      	 else
		      	 {
			      	 address = srcLine.substring(0,9);
		      	 }	      	 
		         srcLine = processViewLine(lineNum+1, " "+srcLine);		         		         
		         dispNumLines++;
//		         _viewLines.addElement(srcLine);      
		         
		         /*
		         This hashtable keeps track of the addresses and their associated line numbers
		         in disassembly view.  This hashtable is used for reporting where a debugee stopped
		         in the disassembly.  Before reporting the status of the stack frame to UI, we
		         get the location of a part, convert this line number to an address, and look up
		         this address in the hashtable.  Then we report the associated line number as the
		         correct location of a particular line in disassembly view.
		         */  
		         _lineMap.put(address, new String(Integer.toString(dispNumLines)));
		         
		         /*    0x8049393 <main+283 at payroll.cpp:73>: sub    $0x8,%esp
						0x8049396 <main+286 at payroll.cpp:73>: push   $0x804a22b
						0x804939b <main+291 at payroll.cpp:73>: lea    0xffffff48(%ebp),%eax
						0x80493a1 <main+297 at payroll.cpp:73>: push   %eax
				  */		      			      	
		      	  String keyword = ":";
				  int at = srcLine.indexOf(keyword);	
				  keyword = ">:";
				  int endAt = srcLine.indexOf(keyword);

				  if (at > -1 && endAt > -1 && endAt > at)
				  {
				  	String srcLineNum = srcLine.substring(at+1, endAt);
				  	try {
						int srcLineNumIdx = Integer.parseInt(srcLineNum);
						
						srcViewLine = srcView.getLine(srcLineNumIdx);
						
						if (srcViewLine != null)
						{						
							if (srcLineNumIdx != currentSrcLineNumIdx)
							{
								start = dispNumLines;
								end = dispNumLines;
								currentSrcLineNumIdx = srcLineNumIdx;
								srcViewLine.setDisassemblyIndex(start, end);
							}
							else
							{
								end = dispNumLines;
								srcViewLine.setDisassemblyIndex(start, end);
							}
						}
						
					} catch(NumberFormatException e) 
					{
					}
					
				  }
		                  
		         if (srcLine.length() > maxLength)
		            maxLength = srcLine.length();
		            
		          srcLine = this.removeFileInfo(srcLine);
   		         _viewLines.addElement(srcLine);         
		
		         lineNum++;
		      }      
		      
		      setViewRecordLength(maxLength);
		      setViewLineRange(1, dispNumLines);
		      setExecutableLines();
	      }
	  }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"File opened and read: " + sourceFileName);
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"Read " + Integer.toString(_viewNumLines) + " lines");

      _viewVerify = true;
      setViewFileName(1,sourceFileName);
      _parentPart.setPartChanged(true);
      return true;
   }
   
   /*
   */
   public boolean appendDisassemblyLineByAddress(String disAddress)
   {
      GdbDebugSession gdbDebugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      String[] lines = gdbDebugSession._getGdbFile.getDisassemblyLines(disAddress);	      
		
      int lineNum   = 0;
      int maxLength = 0;
	
      String srcLine = null;
      String address = null;
		      
      int dispNumLines = _viewLines.size();
      
      GdbSourceView srcView = (GdbSourceView)_parentPart.getView(Part.VIEW_SOURCE);
      SourceView.SourceLine srcViewLine;
      int currentSrcLineNumIdx = 0;
      int start = 0;
      int end = 0;
		      
      if (lines == null)
		return false;
		      
      while (  lineNum<lines.length && (srcLine=lines[lineNum])!=null )
      {      	
      	 int space = srcLine.indexOf(" ");
      	 if (space != -1)
      	 {
			 address = srcLine.substring(0, space);
      	 }
      	 else
      	 {
	      	 address = srcLine.substring(0,9);
      	 }	      	 
         srcLine = processViewLine(lineNum+1, " "+srcLine);
		         
         dispNumLines++;
		         
         /*
         This hashtable keeps track of the addresses and their associated line numbers
         in disassembly view.  This hashtable is used for reporting where a debugee stopped
         in the disassembly.  Before reporting the status of the stack frame to UI, we
         get the location of a part, convert this line number to an address, and look up
         this address in the hashtable.  Then we report the associated line number as the
         correct location of a particular line in disassembly view.
         */  
         _lineMap.put(address, new String(Integer.toString(dispNumLines)));
         
          String keyword = ":";
		  int at = srcLine.indexOf(keyword);	
		  keyword = ">:";
		  int endAt = srcLine.indexOf(keyword);

		  if (at > -1 && endAt > -1 && endAt > at)
		  {
		  	String srcLineNum = srcLine.substring(at+1, endAt);
		  	try {
				int srcLineNumIdx = Integer.parseInt(srcLineNum);
				
				srcViewLine = srcView.getLine(srcLineNumIdx);
				
				if (srcViewLine != null)
				{						
					if (srcLineNumIdx != currentSrcLineNumIdx)
					{
						start = dispNumLines;
						end = dispNumLines;
						currentSrcLineNumIdx = srcLineNumIdx;
						srcViewLine.setDisassemblyIndex(start, end);
					}
					else
					{
						end = dispNumLines;
						srcViewLine.setDisassemblyIndex(start, end);
					}
				}
				
			} catch(NumberFormatException e) 
			{
			}
		  }

		  srcLine = this.removeFileInfo(srcLine);
          _viewLines.addElement(srcLine);      
		                  
		 if (srcLine.length() > maxLength)
		 	maxLength = srcLine.length();
		
         lineNum++;
      }      
		      
      setViewRecordLength(maxLength);
      setViewLineRange(1, dispNumLines);
      setExecutableLines();
      _parentPart.setPartChanged(true);

	  return true;
   }

  /**
   * Process a line of disassembly into a format suitable for front-end viewing.  The
   * line number is prepended to the disassembly line, tabs are converted into spaced
   * and newlines/carriage returns are filtered out. 
   * 
   * Bugzilla Defect #7: Do not display line number in disassembly view
   * Line Number is ignored.
   */
   private String processViewLine(int lineNum, String srcLine)
   {
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"#### DisassemblyView.processViewLine lineNum="+lineNum +" srcLine="+srcLine  );
              
      StringBuffer processedLine;

       // Strip off possible new line character at end
      if (srcLine.length() > 0 && srcLine.charAt(srcLine.length()-1) == '\n')
         srcLine = srcLine.substring(0, srcLine.length()-1);

      // Strip off possible carriage return at end
      if (srcLine.length() > 0 && srcLine.charAt(srcLine.length()-1) == '\r')
         srcLine = srcLine.substring(0, srcLine.length()-1);

	  processedLine = new StringBuffer(srcLine);

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
   
   private String removeFileInfo(String line)
   {
   	
	  //    0x8049393 <main+283 at payroll.cpp:73>: sub    $0x8,%esp
	  // remove " at payroll.cpp:73"
	  StringBuffer processedLine;
	  String tempLine;
	  
	  int idx = line.indexOf(" at ");
	  if (idx > -1)
	  {
	  	tempLine = line.substring(0, idx);
	  	processedLine = new StringBuffer(tempLine);
	  	
	  	idx = line.indexOf(">:");
	  	if (idx > -1)
	  	{
	  		tempLine = line.substring(idx);
	  		processedLine.append(tempLine);
	  	}	  	
	  }
	  else
	  {	
		  processedLine = new StringBuffer(line);
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
   	   	  
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"#### DisassemblyView.getViewLines startLine="+startLine +" numLines="+numLines  );
              
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
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"#### DisassemblyView.stringSearch="+searchString+" startLine="+startLine +" numLinesToSearch="+numLinesToSearch  );
              
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

	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"#### DisassemblyView.addViewToReply viewNo="+viewNo  );
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
   
   // hashtable to keep track of addresses and its associated line number in view
   private Hashtable _lineMap;
   
   // Data members
   static byte _prefixl = 11;
  }
