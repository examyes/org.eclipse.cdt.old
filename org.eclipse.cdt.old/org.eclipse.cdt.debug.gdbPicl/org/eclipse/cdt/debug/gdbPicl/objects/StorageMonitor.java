/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;
import  org.eclipse.cdt.debug.gdbPicl.gdbCommands.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * StorageMonitor.
 */
public abstract class StorageMonitor
{
   GetGdbStorage _getGdbStorage = null;
   String[] _storageAddresses   = null;
   String[] _storageContents    = null;
   int      _maxLines           = 0;
   Vector   _changedStorage     = new Vector(0);

   private class ChangedStorage
   {   short id; int lineNum; String address; String contents;  int flag;
       ChangedStorage(short i, int l, String a, String c, int f)
       {  id = i;   lineNum = l;  address = a;   contents = c;   flag = f; }
   }

   public StorageMonitor(DebugSession debugSession, short ID, String totalColumns, int columnsPerLine, String mode, 
                                                    String wordSize, int charsPerColumn,
                                                    int startLine, int endLine, String startAddress, String baseAddress,
                                                    EStdExpression2 expr )
   {
      _debugSession = debugSession;
      _id = ID;
      _totalColumns = totalColumns; 
      _columnsPerLine = columnsPerLine;
/*      
      if(_columnsPerLine!=4)
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor unsupported columnsPerLine="+_columnsPerLine ); 
         _columnsPerLine = 4;     
      }
*/      
      _startLine = startLine;
      _endLine = endLine;
      _mode = mode;
/*      
      if(!_mode.equals("x"))
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor unsupported mode="+_mode ); 
         _mode = "x";     
      }
      _wordSize = wordSize;
      if(!_wordSize.equals("w"))
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor unsupported wordsize="+_wordSize ); 
         _wordSize = "w";     
      }
      _charsPerColumn = charsPerColumn;
      if(_charsPerColumn!=8)
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor unsupported charsPerColumn="+_charsPerColumn ); 
         _charsPerColumn = 8;     
      }
*/      
      _bytesPerColumn = _charsPerColumn/2;
      _startAddress = startAddress; // the start (first) address to be fetched
      _baseAddress  = baseAddress;  // the base (default) address to be displayed
      _expr = expr;
      _getGdbStorage = ((GdbDebugSession)_debugSession)._getGdbStorage;
   }


   /**
    * Return whether this registers values have changed
    */
   public boolean hasChanged()
   {
      return _storageChanged;
   }

   public void storageRangeSet(int rangeStart, int rangeEnd)
   {
       _maxLines           = rangeEnd-rangeStart+1;
       _storageAddresses = new String[_maxLines];
       _storageContents = new String[_maxLines];
       for (int z=0; z<_maxLines; z++)
       {    _storageAddresses[z]="?";
            _storageContents[z]="?";
       }

       _startLine = rangeStart;
       _endLine = rangeEnd;
//       _totalColumns = String.valueOf(_columnsPerLine * (rangeEnd-rangeStart+1));
       _totalColumns = String.valueOf(_columnsPerLine * _maxLines);
       long    startAddress = Long.parseLong(_baseAddress.substring(2),16) +(_startLine*_columnsPerLine*_bytesPerColumn);
             _startAddress = "0x"+Long.toHexString(startAddress);
       if (Gdb.traceLogger.DBG) 
           Gdb.traceLogger.dbg(1,"StorageMonitor.storageRangeSet _baseAddress="+_baseAddress+" _startAddress="+_startAddress );

       updateStorage();
   }

   public void modifyStorage(String baseAddress, int lineOffset, int columnOffset, String value)
   {
       if (Gdb.traceLogger.EVT) 
           Gdb.traceLogger.evt(1,"StorageMonitor.modifyStorage baseAddress="+baseAddress+" lineOffset="+lineOffset
             +" columnOffset="+columnOffset+" value="+value );

       if(!baseAddress.equals(_baseAddress))
       {
          if (Gdb.traceLogger.EVT) 
              Gdb.traceLogger.evt(1,"StorageMonitor.modifyStorage baseAddress="+baseAddress+" _baseAddress="+_baseAddress );
          return;
       }
       if(lineOffset>=_maxLines)
       {
          if (Gdb.traceLogger.EVT) 
              Gdb.traceLogger.evt(1,"StorageMonitor.modifyStorage lineOffset="+lineOffset+" _maxLines="+_maxLines );
          return;
       }
       if(columnOffset>=_columnsPerLine)
       {
          if (Gdb.traceLogger.EVT) 
              Gdb.traceLogger.evt(1,"StorageMonitor.modifyStorage columnOffset="+columnOffset+" _columnsPerLine="+_columnsPerLine );
          return;
       }

       long    actualAddressInt = Long.parseLong(_baseAddress.substring(2),16) +(lineOffset*_columnsPerLine*_bytesPerColumn) +(columnOffset*_bytesPerColumn);
       String actualAddressStr = "0x"+Long.toHexString(actualAddressInt);
       if (Gdb.traceLogger.DBG) 
           Gdb.traceLogger.dbg(1,"StorageMonitor.modifyStorage actualAddressStr="+actualAddressStr +" value="+value  );

       String type = "{int}";
            if (_bytesPerColumn==4)
          type = "{int}";
       else if (_bytesPerColumn==2)
          type = "{short}";
       else if (_bytesPerColumn==1)
          type = "{char}";
       else if (_bytesPerColumn==8)
          type = "{long}";

       if(_mode=="x")
          value = "0x"+value;
       String cmd = "set "+type +actualAddressStr +"=" +value;
       boolean ok = ((GdbDebugSession)_debugSession).executeGdbCommand(cmd);
       if(!ok)
           return;
 
       String[] lines = ((GdbDebugSession)_debugSession).getTextResponseLines();
       if(lines.length>0)
       {   if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(1,"ERROR: StorageMonitor.modifyStorage response="+lines[0] );
           return;
       }
   }

   public void updateStorage()
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"StorageMonitor.updateStorage _totalColumns="+_totalColumns+" _mode="+_mode+" _wordSize="+_wordSize+" _startAddress="+_startAddress  );

      _getGdbStorage.updateStorage(_totalColumns, _columnsPerLine, _mode, _wordSize, _charsPerColumn, _startAddress );
      if(_getGdbStorage.lastEvaluationError!=null)
      {
         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"StorageMonitor.updateStorage error="+_getGdbStorage.lastEvaluationError ); 
         return;
      }

      _maxLines = _getGdbStorage.getMaxLines();

      if(_maxLines<=0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(1,"StorageMonitor.updateStorage returned lines="+_maxLines ); 
         return;
      }
      int flags = EPDC.MonStorEnabled | EPDC.MonStorContentsChanged | EPDC.MonStorAddressChanged;

      if (_storageAddresses==null)
      {  
         _storageAddresses = new String[_maxLines];
         _storageContents = new String[_maxLines];
         for (int z=0; z<_maxLines; z++)
         {    _storageAddresses[z]="?";
              _storageContents[z]="?";
         }
         flags = EPDC.MonStorNew;
      }

      String[] newAddresses = _getGdbStorage.getStorageAddresses();
      String[] newContents = _getGdbStorage.getStorageContents();
      for (int z=0; z<_maxLines; z++)
      {
//         if( !newContents[z].equals(_storageContents[z]) )
//TODO: sending back a single changeLine breaks javaUI
         {
            if( !newContents[z].equals(_storageContents[z]) )
               _storageChanged = true;
            int line = z; //???????????? (z+_startLine);
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"StorageMonitor.updateStorage CHANGED address="+_storageAddresses[z]+" new="+newContents[z]+" old="+_storageContents[z]   );
            _changedStorage.addElement( new ChangedStorage(_id, line, newAddresses[z], newContents[z], flags) );
            _storageAddresses[z] = newAddresses[z];
            _storageContents[z] = newContents[z];
         }
      }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"StorageMonitor.updateStorage _maxLines="+_maxLines+" _changedStorage.size="+_changedStorage.size()  );
   }

   /**
    * Get the REpGetNextStorageId item for this storage.  If the thread 
    * associated with this storage is no longer active, null is returned.
    */
   public ERepGetNextMonitorStorageId getStorageChangeInfo(EPDC_Reply rep)
   {

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"StorageMonitor.getRegisterChangeInfo _changedStorage.size="+_changedStorage.size() );

      ERepGetNextMonitorStorageId changeInfo = null;
      int size = _changedStorage.size();

         int startLineOffset = _startLine; 
         int endLineOffset = _endLine;  // 81 lines * 4 cols/line = 324 total columns
         EStdStorageRange range= new EStdStorageRange(startLineOffset,endLineOffset);
         String baseAddress = _baseAddress;
         int lineOffset = 0;
         int unitOffset = 0;
         EStdStorageLocation location = new EStdStorageLocation(baseAddress, lineOffset, unitOffset);
         EStdExpression2 expr= _expr;
//         short unitStyle=EPDC.StorageStyle32BitIntHex;
         short unitStyle=EPDC.StorageStyleByteHexCharacter;
         int unitCount=16; // 4 columns
         short addressStyle=1;  // flat
         // Line Offsets:
         int firstAddress   = 0x01;
         int secondAddress  = 0x0A;
         int firstContents  = 0x0B;
         int secondContents = 0x2C;
         int attributeIndex = 0x2D;
      ERepGetNextMonitorStorageLine[] lines = new ERepGetNextMonitorStorageLine[size];

      String attributes = "\5\5\5\5";  //4=allocated, 2=changed, 1=editable
      short flags = 0;
      short  id   = 0;  
           
      //???????? StorageMonitor.getStorageChangeInfo should use z+_startLine instead of z ????????????
      for(int z=0; z<size; z++)
      {
         ChangedStorage s = (ChangedStorage)_changedStorage.elementAt(z);
         if(s==null) continue;

         String contents = s.contents;
         String address = s.address +"\0";
         int lineNum = s.lineNum;
         flags = (short)s.flag;
                          
         id   = s.id;  

		 String translatedText = getTranlsatedText(contents);

         contents = contents  + "\0" + translatedText + "\0"+ attributes;
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(3,"StorageMonitor.getStorageChangeInfo line="+z+" address="+address+" flags="+Integer.toHexString(flags)+" contents="+contents );

         ERepGetNextMonitorStorageLine storageLine = new ERepGetNextMonitorStorageLine(lineNum,address,contents);
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"StorageMonitor.getStorageChangeInfo getLineNumber="+storageLine.getLineNumber()
                    +" getAddress="+storageLine.getAddress()+" getStorage="+storageLine.getStorage()[0] );
         lines[z] = storageLine;
    
//    0x00 0x33 -- bytes in line (decimal 51)
//    0x00 -- end-of-bytes
//01  0x31 0x32 0x33 0x34 0x35 0x36 0x37 0x38 -- 8 character 1st address
//    0x00 -- end-of-1st-address
//         -- 2nd Address unused
//0A  0x00 -- end-of-2nd-address
//0B  0x31 0x32 0x33 0x34 0x35 0x36 0x37 0x38 -- 8 characters, 1st word
//    0x31 0x32 0x33 0x34 0x35 0x36 0x37 0x38 -- 8 characters, 2nd word
//    0x31 0x32 0x33 0x34 0x35 0x36 0x37 0x38 -- 8 characters, 3rd word
//    0x31 0x32 0x33 0x34 0x35 0x36 0x37 0x38 -- 8 characters, 4th word
//    0x00 -- end-of-1st-Contents
//         -- 2nd Contents unused (could be Ascii Chars if used
//2C  0x00 -- end-of-2nd-Contents
//2D  0x00 0x00 0x00 0x00 -- 4 attributes
      }

         if(getStorageDeleted())
         {
         	id = getID();
         	flags = (short)EPDC.MonStorDeleted;
         }

ERepGetNextMonitorStorageId storage = new ERepGetNextMonitorStorageId(
            id, range, location, expr, unitStyle, unitCount, addressStyle, 
            firstAddress, secondAddress, firstContents, secondContents, attributeIndex, flags, lines);
      changeInfo = storage;
      _storageChanged = false;
      _changedStorage.removeAllElements();

      return changeInfo; 
   }
   
   /*
   		Translate raw data from storage monitor to character representation
   */
   private String getTranlsatedText(String inputStr)
   {
   		String resultStr = "";
   	
   		if (inputStr.length() != 32)
   		{
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(1,"Cannot translated raw data to characters:"  + inputStr);
   			return new String("............");
   		}
   			
		for (int i=0; i<=30; i=i+2)
		{
			try
			{
				int intValue = Integer.parseInt(inputStr.substring(i, i+2), 16);
				char translatedChar = (char)intValue;
				
				if (Character.isLetterOrDigit(translatedChar))
					resultStr += translatedChar;
				else
					// put "." for non-printable data
					resultStr += '.';					
			}
			catch(NumberFormatException exp)
			{
	            if (Gdb.traceLogger.ERR) 
	                Gdb.traceLogger.err(1,"Cannot translated raw data to characters:"  + inputStr);
				return new String("................");
			}			
		} 
		
		return resultStr;  			
   }
   
/*
   // data fields
   protected DebugSession _debugSession;
   protected short  _id = 1;
   public    short  getID() { return _id; }
   protected String _totalColumns = "40";
   protected String _mode = "x"; //x=hex, d=decimal, u=unsigned
   protected String _wordSize = "b"; //b=byte(8-bits), h=halfWord(16-bits) w=word(32-bits) g=giant(64bits)
   protected String _startAddress = "0x0"; // the start (first) address to be fetched
   protected String _baseAddress  = "0x0"; // the base (default) address to be displayed
   public    String getBaseAddress() { return _baseAddress; }
   protected int    _charsPerColumn = 2;
   protected int    _bytesPerColumn = _charsPerColumn/2;
   protected int    _columnsPerLine = 8;
   protected int    _startLine = 0;
   protected int    _endLine = 0;
   protected EStdExpression2 _expr = null;
*/   

   protected DebugSession _debugSession;
   protected short  _id = 1;
   public    short  getID() { return _id; }
   protected String _totalColumns = "40";
   protected String _mode = "x"; //x=hex, d=decimal, u=unsigned
   protected String _wordSize = "w"; //b=byte(8-bits), h=halfWord(16-bits) w=word(32-bits) g=giant(64bits)
   protected String _startAddress = "0x0"; // the start (first) address to be fetched
   protected String _baseAddress  = "0x0"; // the base (default) address to be displayed
   public    String getBaseAddress() { return _baseAddress; }
   protected int    _charsPerColumn = 8;
   protected int    _bytesPerColumn = _charsPerColumn/2;
   protected int    _columnsPerLine = 4;
   protected int    _startLine = 0;
   protected int    _endLine = 0;
   protected EStdExpression2 _expr = null;

   // status flags
   protected boolean _storageChanged = false;
   protected boolean _storageDeleted = false;
	/**
	 * Gets the storageDeleted
	 * @return Returns a boolean
	 */
	public boolean getStorageDeleted() {
		return _storageDeleted;
	}
	/**
	 * Sets the storageDeleted
	 * @param storageDeleted The storageDeleted to set
	 */
	public void setStorageDeleted(boolean storageDeleted) {
		_storageDeleted = storageDeleted;
	}

}
