/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;
import  org.eclipse.cdt.debug.gdbPicl.gdbCommands.GetGdbFile;

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
      _mixedExecutableLines = new Vector();
      _lineMap = new Hashtable();
      _viewConstructed = false;
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

   
   private boolean constructMixedView(String sourceFileName, int startLine, int numLines)
   {
      int totalSource = sourceView.getViewNumLines();
      int totalDisassembly = disassemblyView.getViewNumLines();
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"MixedView.verifyView totalSource="+totalSource+" totalDisassembly="+totalDisassembly );
      if(totalSource<=0 || totalDisassembly<=0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"MixedView.verifyView totalSource||totalDisassembly<=0 totalSource="+totalSource+" totalDisassembly="+totalDisassembly );
         return false;
      }

      int mixedCurrentLine = 1;
      int disStart = -1;
      int disEnd = -1;
      SourceView.SourceLine srcLine;
      
      GdbDebugSession gdbDebugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      
      // based on information from disassembly view, create association of source view and disassembly view
	  /*    0x8049393 <main+283 at payroll.cpp:73>: sub    $0x8,%esp
			0x8049396 <main+286 at payroll.cpp:73>: push   $0x804a22b
			0x804939b <main+291 at payroll.cpp:73>: lea    0xffffff48(%ebp),%eax
			0x80493a1 <main+297 at payroll.cpp:73>: push   %eax
	  */
      
      for(int i=1; i<=totalSource; i++)
      {
      	srcLine = sourceView.getLine(i);
      	
		disStart = srcLine.getStart();
		disEnd = srcLine.getEnd();
      	
      	if (disStart < 0 || disEnd < 0)
      	{
	      	// get line information by "info line filename.cpp:lineNum"
	      	GetGdbFile.StartEnd startEnd = gdbDebugSession._getGdbFile.getLineStartEnd(sourceFileName, i);
	      	
	      	if (startEnd == null)
	      	{
				String line = sourceView.getViewLine(i);
	   			if (line != null)
	   			{
					line = processViewLine(line);      			
	      			_viewLines.addElement(line);
	      			mixedCurrentLine++;
	   			}
	   			continue;
	      	}
	      	
	      	// if the line contains no source
	      	if (startEnd.endAddress == null)
	      	{
	      		if (Gdb.traceLogger.DBG) 
	              Gdb.traceLogger.dbg(3, "### MixedView.combineSourcePlusDisassemblyViews ### No code for line " + i + " get next address:  " + startEnd.startAddress);   		
	      		
	      		// get the next executable line by "info line *address" where address comes from prev "info line" call
	      		String nextLineNum = gdbDebugSession._getGdbFile.convertAddressToSourceLine(startEnd.startAddress);
	      		// add source from current line to next executable line to _viewLine
	      		SourceView.SourceLine line;
	      		String strLine;
	      		
	      		int nextLine = Integer.parseInt(nextLineNum);
	      		
	      		if (Gdb.traceLogger.DBG) 
	              Gdb.traceLogger.dbg(3, "### MixedView.combineSourcePlusDisassemblyViews ### No code for line " + i + " next line:  " + nextLine);  
	      		
	      		for (int j=i; j< nextLine; j++)
	      		{
	      			line = sourceView.getLine(j);

	      			if (line != null)
	      			{
	      				disStart = line.getStart();
						disEnd = line.getEnd();
						
						// gdb may be bad returning next line info... in case we have disassembly code for a certain line already, combine it
						if (disStart != -1 && disEnd != -1)
						{		      			
				   			int sourceStartLine = j;
				   			int sourceEndLine = j;
				   			
				      		if (Gdb.traceLogger.DBG) 
				              Gdb.traceLogger.dbg(3, "### MixedView.combineSourcePlusDisassemblyViews ### For line " + i + " start disp line: " + disStart + " end disp line:  " + disEnd);  
				      			
				   			// combineSourcePlusDisassemblyLines
							mixedCurrentLine = combineSourcePlusDisassemblyLines(mixedCurrentLine, sourceStartLine, sourceEndLine,
								disStart, disEnd);
						}
						else
						{
						
								strLine = processViewLine(line.toString());      			
				      			_viewLines.addElement(strLine);
				      			mixedCurrentLine++;
						}
	      			}
	      		}
	      		// i = next executable line
	      		i = nextLine-1;
	      		// continue with the for loop
	      	}
	      	// else
	      	else
	      	{
	      		// get start address
	      		// if disassembly does not contain address
	      		if (!disassemblyView.containsAddressInView(startEnd.startAddress))
	      		{
	      			// disassemble by the address and append to the end of the view
	      			boolean ok = disassemblyView.appendDisassemblyLineByAddress(startEnd.startAddress);
	      			
	      			// just add line to view if we can't disassembly the given address successfully
	      			if (!ok)
	      			{
	      				String line = sourceView.getViewLine(i);
	      				if (line != null)
	      				{
							line = processViewLine(line);      			
			      			_viewLines.addElement(line);
			      			mixedCurrentLine++;
	      				}
			      		continue;
	      			}	      			
	      		}

				srcLine = sourceView.getLine(i);
				disStart = srcLine.getStart();
				disEnd = srcLine.getEnd();
				
				if (disStart != -1 && disEnd != -1)
				{		      			
		   			int sourceStartLine = i;
		   			int sourceEndLine = i;
		   			
		      		if (Gdb.traceLogger.DBG) 
		              Gdb.traceLogger.dbg(3, "### MixedView.combineSourcePlusDisassemblyViews ### For line " + i + " start disp line: " + disStart + " end disp line:  " + disEnd);  
		      			
		   			// combineSourcePlusDisassemblyLines
					mixedCurrentLine = combineSourcePlusDisassemblyLines(mixedCurrentLine, sourceStartLine, sourceEndLine,
						disStart, disEnd);
				}
				else
				{
					String line = srcLine.toString();
	      			if (line != null)
	      			{
						line = processViewLine(line);      			
		      			_viewLines.addElement(line);
		      			mixedCurrentLine++;
	      			}
				}
	      	}
      	}
      	else
      	{
      		mixedCurrentLine = combineSourcePlusDisassemblyLines(mixedCurrentLine, i, i, disStart,disEnd);
      	}
      }
      
      // add all integers from _mixedExecutableLines to _executableLines
      _executableLines = new int[_mixedExecutableLines.size()];
      
      for (int i=0; i<_mixedExecutableLines.size(); i++)
      {
      	_executableLines[i] = ((Integer)_mixedExecutableLines.elementAt(i)).intValue();
      }   	

	  // update view info      
      int srcMax = sourceView.getViewRecordLength();
      int disMax = disassemblyView.getViewRecordLength();
      if(srcMax>disMax) 
          setViewRecordLength(srcMax);
      else
          setViewRecordLength(disMax);

//      setViewLineRange(1,_viewLines.size() );
      setViewLineRange(1,sourceView.getViewNumLines() + disassemblyView.getViewNumLines());

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
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"#### MixedView.combineSourcePlusDisassemblyLines sourceStartLine="+sourceStartLine+"  sourceEndLine="+sourceEndLine
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
         line = processViewLine(line);
         _viewLines.addElement(line);
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"MixedView.combineSourcePlusDisassemblyLines lineNumber="+mixedCurrentLine+" source="+line );
         mixedCurrentLine++;
      }
      
      String address;

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
//         line = processViewLine(mixedCurrentLine, line);
         _viewLines.addElement(line);
	     if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"MixedView.combineSourcePlusDisassemblyLines lineNumber="+mixedCurrentLine+" disassembly="+line );
              
		 int space = line.indexOf(" <");
		 if (space > 0)
		 {
		 	address = line.substring(0, space);
		 	address = address.trim();
		 	_lineMap.put(address, new String(Integer.toString(mixedCurrentLine)));
		 }              
         _mixedExecutableLines.addElement(new Integer(mixedCurrentLine));               
         
         mixedCurrentLine++;
      }

      return mixedCurrentLine;
   }

  /**
   * Verify View:
   *   - make sure source view and disassembly views are verified
   *   - find the record length of the views
   *   - find the length of mixed view
   */
   public boolean verifyView(String sourceFileName) 
   {
	  if (Gdb.traceLogger.EVT) 
              Gdb.traceLogger.evt(1,"#### MixedView.verifyView(STRING) sourceFileName="+sourceFileName  );

	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"#### MixedView.combineSourcePlusDisassemblyViews(STRING) sourceFileName="+sourceFileName  );
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

//      _viewConstructed = constructMixedView(_viewFileName, startLine, numLines);

      int srcMax = sourceView.getViewRecordLength();
      int disMax = disassemblyView.getViewRecordLength();
      if(srcMax>disMax) 
          setViewRecordLength(srcMax);
      else
          setViewRecordLength(disMax);

//      setViewLineRange(1,_viewLines.size() );
      setViewLineRange(1,sourceView.getViewNumLines() + disassemblyView.getViewNumLines());

      _fakeNoSource = false;
      _viewVerify = true;
      setViewFileName(1,sourceFileName);
      _parentPart.setPartChanged(true);
      return true;

    }

  /**
   * Process a line of source into a format suitable for front-end viewing.  The
   * old prefix is removed, and the new line number is prepended to the source line.
   */
   private String processViewLine(String srcLine)
   {
   	
	    StringBuffer processedLine;
      
      processedLine = new StringBuffer(srcLine.substring(0,super._prefixl));

      // Strip off old prefix
      srcLine = srcLine.substring(super._prefixl);

      // create new prefix      
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
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"#### MixedView.getViewLines startLine="+startLine +" numLines="+numLines  );
              
      if (!_viewConstructed)
      {
		  GdbDebugSession gdbDebugSession = (GdbDebugSession)_debugEngine.getDebugSession();
 		  GdbThreadManager threadManager = (GdbThreadManager) gdbDebugSession.getThreadManager();
      	        
	      _viewConstructed = constructMixedView(_viewFileName, startLine, numLines);        
	      threadManager.forceUpdate();
      }
              
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
            srcLine = processViewLine(Integer.toString(lineNum) + "  ");
         }
         else
         {
         	if (lineNum-1 > _viewLines.size())
         	{
				srcLine = " ";
         	}
         	else
         	{
	            srcLine = (String) _viewLines.elementAt(lineNum-1);
         	}
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
              Gdb.traceLogger.dbg(3,"#### MixedView.stringSearch="+searchString+" startLine="+startLine +" numLinesToSearch="+numLinesToSearch  );
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
              Gdb.traceLogger.dbg(1,"#### MixedView.addViewToReply viewNo="+viewNo  );
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
		else {
			// get next executable line in mixed view
			for (int x = lineNum + 1; x < getViewNumLines(); x++) {
				lineInfo = getViewLine(x);
				lineInfo = lineInfo.trim();

				if (lineInfo.startsWith("0x")) {
					int idx = lineInfo.indexOf(" ");
					if (idx > 0) {
						address = lineInfo.substring(0, _prefixl);
					}
					break;
				}
			}
		}
				
		return address;
   }
   
   private boolean validDisStartEnd(int start, int end)
   {
   		// start line must be less than end line   		
   		if (start > end)
   			return false;
   			
		// examine start and end line and make sure they are from the same function
		int functionBegin;
		int functionEnd;
		String startFn = "startFn";
		String endFn = "endFn";
		
		String line = disassemblyView.getViewLine(start);
		functionBegin = line.indexOf("<");
		functionEnd = line.indexOf("+");
		
		if (functionEnd < 0)
		{
			functionEnd = line.indexOf(">");
		}
		
		if (functionBegin > 0 && functionEnd >0)
		{
			startFn = line.substring(functionBegin+1, functionEnd);
		}
		
		line = disassemblyView.getViewLine(end);
		functionBegin = line.indexOf("<");
		functionEnd = line.indexOf("+");
		
		if (functionEnd < 0)
		{
			functionEnd = line.indexOf(">");
		}
		
		if (functionBegin > 0 && functionEnd >0)
		{
			endFn = line.substring(functionBegin+1, functionEnd);
		}
		
		if (!endFn.equals(startFn))
			return false;
   			
   		return true;
   }
   
   private int findValidEndLine(int start)
   {
   		int end = start;
   		int totalDisassembly = disassemblyView.getViewNumLines();	
   		String line;
   		String startFn = "startFn";
   		int functionBegin;
   		int functionEnd;
   		String lineFn = "lineFn";
   		
   		line = disassemblyView.getViewLine(start);
		if (line != null)
		{
			functionBegin = line.indexOf("<");
			functionEnd = line.indexOf("+");
		
			if (functionEnd < 0)
			{
				functionEnd = line.indexOf(">");	
			}
		
			if (functionBegin > 0 && functionEnd >0)
			{
				startFn = line.substring(functionBegin+1, functionEnd);
			}
		}
   		
   		for (int i=start; i<=totalDisassembly; i++)
   		{   			
   			line = disassemblyView.getViewLine(i);
   			if (line != null)
   			{
				functionBegin = line.indexOf("<");
				functionEnd = line.indexOf("+");
		
				if (functionEnd < 0)
				{
					functionEnd = line.indexOf(">");	
				}
		
				if (functionBegin > 0 && functionEnd >0)
				{
					lineFn = line.substring(functionBegin+1, functionEnd);
				}
				
				if (startFn.equals(lineFn))
				{
					end = i;
				}
				else
				{
					break;
				}
   			}
   		}
   		
   		return end;
   }


   // Data members
   protected int            _moduleID;
   protected ModuleManager  _moduleManager;
   protected SourceView      sourceView;
   protected DisassemblyView disassemblyView;

   protected Vector			 _mixedExecutableLines;
   protected Hashtable       _lineMap;
   static byte _prefixl = 11;
   private boolean _viewConstructed;
}
