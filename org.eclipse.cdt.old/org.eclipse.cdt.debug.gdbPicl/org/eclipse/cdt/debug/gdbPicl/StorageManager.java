/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GetGdbStorage;

import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * This class manages storage monitors
 */
public class StorageManager extends ComponentManager      //HC
{
   public StorageManager(DebugSession debugSession)
   {
      super(debugSession);
      _storageMonitors = new Hashtable(100);
      _changedStorage  = new Vector();
   }

   /**
    * Adds change packets for this component to a reply packet
    */
   public void addChangesToReply(EPDC_Reply rep)
   { 
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"................ StorageManager.addChangesToReply _changedStorage.size="+_changedStorage.size() );

      for (int i=0; i<_changedStorage.size(); i++)
      {
         StorageMonitor monitor = (StorageMonitor)_changedStorage.elementAt(i);
         ERepGetNextMonitorStorageId changeInfo = monitor.getStorageChangeInfo(rep);

         if (changeInfo != null)
         {   
             if (Gdb.traceLogger.DBG) 
                 Gdb.traceLogger.dbg(2,"................ StorageManager.addChangesToReply name="+changeInfo.getName()+" address="+changeInfo.getAddress()+" isNew="+changeInfo.isNew() );
             rep.addStorageChangePacket(changeInfo);
         }
      }

      _changedStorage.removeAllElements();
   }

  /**
   * Monitor storage and send Storage change packets to SUI when necessary.
   * @param DU the unique thread identification number
   */
   public void monitorStorage(long startAddress, short addressStyle, short unitStyle, int styleUnitCount,
                              int rangeStart, int rangeEnd, int attributes,
                              String exprString, int exprDU, int PPID, int lineNum,  EStdExpression2 expr )
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"############### StorageManager.monitorStorage addressStyle="+addressStyle+" unitStyle="+unitStyle+" styleUnitCount="+styleUnitCount
                     +" rangeStart="+rangeStart+" rangeEnd="+rangeEnd+" attributes="+Integer.toHexString(attributes)
                     +" expr="+expr+" exprDU="+exprDU+" PPID="+PPID+" lineNum="+lineNum  );
      startAddress = evaluateAddressOfExpression(exprString, exprDU);
      if(startAddress >= 0)
         monitorStorage(startAddress, addressStyle, unitStyle, styleUnitCount, rangeStart, rangeEnd, attributes, expr);
      else
      {
         GetGdbStorage _getGdbStorage = ((GdbDebugSession)_debugSession)._getGdbStorage;
         if(_getGdbStorage.lastEvaluationError==null)
             _getGdbStorage.lastEvaluationError = "Negative address="+startAddress;
      }
   }
   private long evaluateAddressOfExpression(String expr, int exprDU)
   {
         GetGdbStorage _getGdbStorage = ((GdbDebugSession)_debugSession)._getGdbStorage;
         String address = _getGdbStorage.evaluateAddressOfExpression(expr, exprDU); 
         long a = -1;
         try
         {
            if(address!=null)
            {
				a = Long.valueOf(address.substring(2), 16).longValue();
            }
         }catch(java.lang.NumberFormatException exc) {
			if(Gdb.traceLogger.ERR)
				Gdb.traceLogger.err(1, "Error Converting Address: " + address);         
         }
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"StorageManager.monitorStorage expr address=0x"+Long.toHexString(a) );
         return a;
   }

  /**
   * Monitor storage and send Storage change packets to SUI when necessary.
   */
   public void monitorStorage(long address, short addressStyle, short unitStyle, int styleUnitCount,
                              int rangeStart, int rangeEnd, int attributes, EStdExpression2 expr )
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageManager.monitorStorage address=0x"+Long.toHexString(address)+" addressStyle="+addressStyle+" unitStyle="+unitStyle+" styleUnitCount="+styleUnitCount
                     +" rangeStart="+rangeStart+" rangeEnd="+rangeEnd+" attributes=0x"+Integer.toHexString(attributes) );
/*
      if(unitStyle!=EPDC.StorageStyle32BitIntHex)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(1,"######## StorageManager.monitorStorage requested UNIMPLEMENTED unitStyle="+unitStyle );
         unitStyle = EPDC.StorageStyle32BitIntHex;
      }
*/

      int columnsPerLine = 4;
      int columns = (rangeEnd-rangeStart+1)*columnsPerLine;
      String totalColumns = Integer.toString(columns);  //"81*4=324";
      String mode = "x"; //x=hex, d=decimal, u=unsigned
      String wordSize = "w"; //b=byte(8-bits), h=halfWord(16-bits) w=word(32-bits) g=giant(64bits)
      int bytesPerColumn = 4;
      int charsPerColumn = 2*bytesPerColumn;
             _baseAddress = "0x"+Long.toHexString(address);
      long     startAddress = address +(rangeStart*columnsPerLine*4); //d 4bytes per column
             _startAddress = "0x"+Long.toHexString(startAddress);
             
/*
      int columnsPerLine = 16;
      int columns = (rangeEnd-rangeStart+1)*columnsPerLine;
      String totalColumns = Integer.toString(columns);  //"81*4=324";
      String mode = "x"; //x=hex, d=decimal, u=unsigned
      String wordSize = "b"; //b=byte(8-bits), h=halfWord(16-bits) w=word(32-bits) g=giant(64bits)
      int bytesPerColumn = 1;
      int charsPerColumn = 2*bytesPerColumn;
             _baseAddress = "0x"+Long.toHexString(address);
      long     startAddress = address +(rangeStart*columnsPerLine*4); // 4bytes per column
             _startAddress = "0x"+Long.toHexString(startAddress);
*/             
      _id++;
      GdbStorageMonitor monitor = new GdbStorageMonitor(_debugSession, _id, totalColumns, columnsPerLine, 
                                                        mode, wordSize, charsPerColumn, rangeStart, rangeEnd, _startAddress, _baseAddress, expr);

      _storageMonitors.put(new Integer(_id), monitor);
      monitor.updateStorage();
      if (monitor.hasChanged() && !_changedStorage.contains(monitor))
          _changedStorage.addElement(monitor);

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"StorageManager.monitorStorage _storageMonitors.size()="+_storageMonitors.size()  );

   }

  /**
   * Stop monitoring the indicated thread's Storage
   * @param DU the unique thread identification number
   */
   public void freeStorage(int id)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageManager.freeStorage id="+id );
          
      GdbStorageMonitor monitor = (GdbStorageMonitor)_storageMonitors.get(new Integer(id));
      if(monitor==null)
         return; // happens when ui collapses thread and sends free of all groups (even if not expanded)
      _storageMonitors.remove(new Integer(id));
   }

  /**
   * Clear list of storage
   */
   void clearStorage()
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageManager.clearStorage ");
      _storageMonitors.clear();
   }
   /**
    * Update Storage
    */
   public void updateStorage()
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"StorageManager.updateStorage _storageMonitors.size="+_storageMonitors.size() );

      // Now cycle through all known StorageMonitors and update their values
      Enumeration elements = _storageMonitors.elements();
      while (elements.hasMoreElements())
      {
         StorageMonitor monitor = (StorageMonitor)elements.nextElement(); 
         if (monitor != null)
         {
            monitor.updateStorage();
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(2,"StorageMonitor.updateStorage id="+monitor.getID()+" baseAddress="+monitor.getBaseAddress()+" hasChanged="+monitor.hasChanged() );

            // Only add this StorageMonitor if it has changed since last update
            if (monitor.hasChanged() && !_changedStorage.contains(monitor))
               _changedStorage.addElement(monitor);
         }
      }
   }

   /**
    * Update Storage monitor
    */
   public void updateStorage(int id)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageManager.updateStorage id="+id );

      GdbStorageMonitor monitor = (GdbStorageMonitor)_storageMonitors.get(new Integer(id));
      if(monitor==null)
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor.updateStorage NO MONITOR for id="+id );
         return; 
      }

      monitor.updateStorage();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageMonitor.updateStorage id="+monitor.getID()+" baseAddress="+monitor.getBaseAddress()+" hasChanged="+monitor.hasChanged() );

      // Only add this StorageMonitor if it has changed since last update
      if (monitor.hasChanged() && !_changedStorage.contains(monitor))
         _changedStorage.addElement(monitor);
   }

   public void modifyStorage(int id, String baseAddress, int lineOffset, int columnOffset, String value)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageManager.modifyStorage id="+id );

      GdbStorageMonitor monitor = (GdbStorageMonitor)_storageMonitors.get(new Integer(id));
      if(monitor==null)
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor.modifyStorage NO MONITOR for id="+id );
         return; 
      }
      monitor.modifyStorage( baseAddress, lineOffset, columnOffset, value);

      monitor.updateStorage();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageMonitor.updateStorage id="+monitor.getID()+" baseAddress="+monitor.getBaseAddress()+" hasChanged="+monitor.hasChanged() );

      // Only add this StorageMonitor if it has changed since last update
      if (monitor.hasChanged() && !_changedStorage.contains(monitor))
         _changedStorage.addElement(monitor);
   }
 
   /**
    * Update Storage monitor range
    */
   public void storageRangeSet(int id, int rangeStart, int rangeEnd)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageManager.storageRangeSet id="+id );

      GdbStorageMonitor monitor = (GdbStorageMonitor)_storageMonitors.get(new Integer(id));
      if(monitor==null)
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"StorageMonitor.storageRangeSet NO MONITOR for id="+id );
         return; 
      }

      monitor.storageRangeSet(rangeStart, rangeEnd);

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"StorageMonitor.updateStorage id="+monitor.getID()+" baseAddress="+monitor.getBaseAddress()+" hasChanged="+monitor.hasChanged() );

      // Only add this StorageMonitor if it has changed since last update
      if (monitor.hasChanged() && !_changedStorage.contains(monitor))
         _changedStorage.addElement(monitor);
   }


   /**
    * Replace a monitors's value.
    * @param exprID the expression ID of the Storage
    * @param rootNodeID the root node ID to set
    * @param newValue the new value
    */
   ExprEvalInfo setValue(int exprID, int rootNodeID, String newValue)
   {
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(1,"######## StorageManager.setValue UNIMPLEMENTED, IGNORED " );
      return null;

/*
      StorageMonitor monitor = (StorageMonitor) _storage.get(new Integer(exprID));
      ExprEvalInfo result = null;

      if (monitor != null)
      {
         result = monitor.setValue(rootNodeID, newValue);
      }
      return result;
*/
   }

   // data fields
   protected Hashtable    _storageMonitors;   // hashtable of thread StorageMonitors
   protected Vector       _changedStorage;
   protected short        _id = 0;
   protected String       _startAddress = "0x0";
   protected String       _baseAddress  = "0x0";
}
