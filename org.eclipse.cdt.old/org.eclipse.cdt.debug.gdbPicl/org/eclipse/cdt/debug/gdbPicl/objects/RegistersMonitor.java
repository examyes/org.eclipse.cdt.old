/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.gdbCommands.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * RegistersMonitor.
 */
public abstract class RegistersMonitor
{
   GetGdbRegisters _getGdbRegisters = null;
   String[] _generalNames   = null;
   String[] _generalValues  = null;
   int      _maxGeneral     = 0;
   String[] _floatNames     = null;
   String[] _floatValues    = null;
   int      _maxFloat       = 0;
   Vector   _changedRegisters = new Vector(0);

   private class ChangedRegister
   {   String name;   String value;   int group;   int id;   int flag;
       ChangedRegister(String n, String v, int g, int i, int f)
       {  name = n;  value=v;  group = g;  id = i;  flag = f; }
   }

   public RegistersMonitor(DebugSession debugSession, int DU)
   {
      _debugSession = debugSession;
      _DU           = DU;
      _getGdbRegisters = ((GdbDebugSession)_debugSession)._getGdbRegisters;
   }

   public void trackGeneral(boolean b)
   { 
       if(b==false)
       { 
          if(_trackingGeneral)
             removeGeneralRegisters();
          _generalNames = null;
          _maxGeneral = 0;
       }
       _trackingGeneral = b;
   }
   public void trackFloat(boolean b)
   { 
       if(b==false)
       {
          if(_trackingFloat)
             removeFloatRegisters();
          _floatNames = null;
          _maxFloat = 0;
       }
       _trackingFloat = b;
   }
   public boolean isEmpty() 
   {  
      return !(_trackingGeneral | _trackingFloat);  
   }

   public void trackGroupID(int i)
   { 
           if(i==1) trackGeneral(true);
      else if(i==2) trackFloat(true);
      else
      {  if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"INTERNAL ERROR: RegistersMonitor.trackGroupID="+i );
      }
   }
   public void freeGroupID(int i)
   { 
           if(i==1) trackGeneral(false);
      else if(i==2) trackFloat(false);
      else
      {  if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"INTERNAL ERROR: RegistersMonitor.freeGroupID="+i );
      }
   }

   /**
    * Return whether this registers values have changed
    */
   public boolean hasChanged()
   {
      return _regValuesChanged;
   }

   public void updateRegisters()
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"RegistersMonitor.updateRegisters");

      _getGdbRegisters.updateRegisters(_DU);

      int flags = EPDC.RegisterValueChanged;
      if(_trackingGeneral)
      {
         if(_generalNames==null)
         {
            _generalNames  = _getGdbRegisters.getGeneralNames();
            _maxGeneral    = _generalNames.length;
            _generalValues   = new String[_maxGeneral];
            for (int z=0; z<_maxGeneral; z++)
                _generalValues[z]="?";
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"RegistersMonitor.updateRegisters CREATED GEN_NAMES and GEN_VALUES MAX_GEN="+_maxGeneral  );
            flags = flags | EPDC.RegisterNew;
         }
         String[] genValues = _getGdbRegisters.getGeneralValues();
   
   		 if (genValues != null)
   		 {
	         for (int z=0; z<_maxGeneral; z++)
	         {
	            int groupID = 1;
	            char delimiter = '\u0009';
	            
		        if (z > genValues.length)
	            	break;
	            
	       		int x = genValues[z].indexOf(delimiter);
	       		
	       		if (x != -1)
	       			genValues[z] = genValues[z].substring(0, x);            
	            
	            if( !_generalValues[z].equals(genValues[z]) )
	            {
	               _changedRegisters.addElement( new ChangedRegister(_generalNames[z], genValues[z], groupID, z, flags) );
	               _generalValues[z] = genValues[z];
	               _regValuesChanged = true;
	               if (Gdb.traceLogger.DBG) 
	                   Gdb.traceLogger.dbg(1,"RegistersMonitor.updateRegisters GENERAL HAS_CHANGED name="+_generalNames[z]+" value="+genValues[z]+" groupID="+groupID+" registerID="+z  );
	            }
	         }
   		 }
      }

      flags = EPDC.RegisterValueChanged;
      if(_trackingFloat)
      {
         if(_floatNames==null)
         {
            _floatNames    = _getGdbRegisters.getFloatNames();
            _maxFloat      = _floatNames.length;
            _floatValues   = new String[_maxFloat];
            for (int z=0; z<_maxFloat; z++)
                _floatValues[z]="?";
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"RegistersMonitor.updateRegisters CREATED FLT_NAMES and FLT_VALUES MAX_FLT="+_maxFloat  );
            flags = flags | EPDC.RegisterNew;
         }
         String[] fltValues = _getGdbRegisters.getFloatValues();
         
         if (fltValues != null)
         {
	         for (int z=0; z<_maxFloat; z++)
	         {
	            int groupID = 2;
	            
	            char delimiter = '\u0009';
	            
	            if (z >= fltValues.length)
	            	break;
	            
	       		int x = fltValues[z].indexOf(delimiter);
	       		
	       		if (x != -1)
	       			fltValues[z] = fltValues[z].substring(0, x);
	       			            
	            if( !_floatValues[z].equals(fltValues[z]) )
	            {
	               _changedRegisters.addElement( new ChangedRegister(_floatNames[z], fltValues[z], groupID, z, flags) );
	               _floatValues[z] = fltValues[z];
	               _regValuesChanged = true;
	               if (Gdb.traceLogger.DBG) 
	                   Gdb.traceLogger.dbg(1,"RegistersMonitor.updateRegisters FLOAT HAS_CHANGED name="+_floatNames[z]+" value="+fltValues[z]+" groupID="+groupID+" registerID="+z  );
	            }
	         }
         }
      }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"RegistersMonitor.updateRegisters _maxGeneral="+_maxGeneral+" _maxFloat="+_maxFloat+" _changedRegisters.size="+_changedRegisters.size()  );
   }

   public void removeGeneralRegisters()
   {
     if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"RegistersMonitor.removeGeneralRegisters");

      int flags = EPDC.RegisterDeleted;
      if(_trackingGeneral)
      {
         for (int z=0; z<_maxGeneral; z++)
         {
            int groupID = 1;
            _changedRegisters.addElement( new ChangedRegister(_generalNames[z], _generalValues[z], groupID, z, flags) );
         }
         _regValuesChanged = true;
      }
      _generalNames   = null;
      _generalValues  = null;
      _maxGeneral     = 0;

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"RegistersMonitor.removeGeneralRegisters _maxGeneral="+_maxGeneral+" _maxFloat="+_maxFloat+" _changedRegisters.size="+_changedRegisters.size()  );
   }

   public void removeFloatRegisters()
   {
     if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"RegistersMonitor.removeFloatRegisters");

      int flags = EPDC.RegisterDeleted;
      if(_trackingFloat)
      {
         for (int z=0; z<_maxFloat; z++)
         {
            int groupID = 2;
            _changedRegisters.addElement( new ChangedRegister(_floatNames[z], _floatValues[z], groupID, z, flags) );
         }
         _regValuesChanged = true;
      }
      _floatNames   = null;
      _floatValues  = null;
      _maxFloat     = 0;

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"RegistersMonitor.removeFloatRegisters _maxGeneral="+_maxGeneral+" _maxFloat="+_maxFloat+" _changedRegisters.size="+_changedRegisters.size()  );
   }

   /**
    * Get the REpGetNextRegister item for this register.  If the thread 
    * associated with this register is no longer active, null is returned.
    */
   public ERepGetNextRegister getRegisterChangeInfo(EPDC_Reply rep)
   {

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"RegistersMonitor.getRegisterChangeInfo _changedRegisters.size="+_changedRegisters.size() );

      ERepGetNextRegister changeInfo = null;
      int size = _changedRegisters.size();

      for(int z=0; z<size; z++)
      {
         ChangedRegister r = (ChangedRegister)_changedRegisters.elementAt(z);
         if(r==null) continue;

         String name    = r.name;
         String value   = r.value;
         int registerID = r.id;
         int groupID    = r.group;
         int flags      = r.flag;
         int type       = EPDC.ConstantRegister;
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"RegistersMonitor.getRegisterChangeInfo _DU="+_DU+" groupID="+groupID+" registerID="+registerID+" name="+name+" value="+value+" flags="+flags+" type="+type );
         rep.addRegisterChangePacket( new ERepGetNextRegister(_DU, groupID, registerID, name, value, flags, type) ); 
      }
      _regValuesChanged = false;
      _changedRegisters.removeAllElements();
      return changeInfo; 
   }

   // data fields
   protected DebugSession _debugSession;
//   protected int _groupID;
   protected int _DU;

   // status flags
   protected boolean _regValuesChanged = false;
   protected boolean _trackingGeneral  = false;
   protected boolean _trackingFloat    = false;
}
