//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////
package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class manages EPDC "modules".  In the JDE, a module corresponds
 * to a package.
 */
public class Module
{
   /**
    * Create a new module
    * @param ModuleID the EPDC Module ID
    * @param ModuleName the package name
    * @param FullPathModuleName the full path to the package
    */

   public Module(DebugSession debugSession, int ModuleID, String ModuleName, String ModuleNameAsPath) 
   {
      _debugSession = debugSession;
      _moduleID    = ModuleID;
      _moduleName  = ModuleName;
      _moduleNameAsPath = ModuleNameAsPath;

      _parts   = new Hashtable();
      _partIDs = new Hashtable();

      _moduleEntryNew          = true;
      _moduleEntryChanged      = true;
      _moduleEntryHasParts     = false;

      _moduleEntryHasDebugData = true;
      
      // creates dummy part with partId = 1
//      addPart(1, "dummy", "dummy");

      DebugEngine _debugEngine = _debugSession.getDebugEngine();
      Vector filteredModules = _debugEngine.getFilteredModules();
      Enumeration enum = filteredModules.elements();
      while (enum.hasMoreElements())
      {
         String filteredPackage = (String) enum.nextElement();
         // Append a '.' so our matching mechanism will work!
         String tmpModuleName = _moduleName + ".";
         if (tmpModuleName.startsWith(filteredPackage))
         {
            _moduleEntryHasDebugData = false;
         }
      }
   }

   /**
    * Set the startingAddress and endingAddress for sharedLibrary modules
    */
   public void setModuleStartFinishAddress(String start, String finish)
   {
       start = start.substring(2);
       finish = finish.substring(2);
       _startAddress = -1;
       _finishAddress = -1;
       try
       {
          _startAddress = Integer.parseInt(start,16);
          _finishAddress = Integer.parseInt(finish,16);
          _startAddressHex = "0x"+start;
          _finishAddressHex = "0x"+finish;
       }
       catch(java.lang.NumberFormatException exc)
       {
          if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"Module.setModuleStartFinishAddress NUMBER_EXCEPTION start="+start+" finish="+finish);
       }
       if (Gdb.traceLogger.EVT) 
           Gdb.traceLogger.evt(3,"Module.setModuleStartFinishAddress start="+Integer.toHexString(_startAddress)+" finish="+Integer.toHexString(_finishAddress) );
   }

   /**
    * Checks the startingAddress and endingAddress to see if this address is contained (default is true)
    */
   public boolean containsAddress(String address)
   {
       boolean contained = false;
       address = address.substring(2);
       int i = -1;
       try
       {
          i = Integer.parseInt(address,16);
          if (_startAddress<0 || _finishAddress<0)
             contained = false;
          else if (i>_startAddress & i<_finishAddress)
             contained = true;
       }
       catch(java.lang.NumberFormatException exc)
       {
          if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"Module.containsAddress NUMBER_EXCEPTION address="+address );
       }
       if (Gdb.traceLogger.DBG) 
           Gdb.traceLogger.dbg(2,"Module.containsAddress targetAddress="+address +" CONTAINED="+contained+" moduleStart=0x"+Integer.toHexString(_startAddress) +" moduleFinish=0x"+Integer.toHexString(_finishAddress) );
       return contained;
   }

   private Part createPart(int partID, String partName, String fullPartName)
   {
     return new GdbPart(_debugSession, (short) partID, _moduleID, partName, fullPartName);
   }

   /**
    * Add a part to this module.  If the part ID already exists, the old part
    * with the same ID is replaced by the new part
    * @return the new part object
    */
   public Part addPart(int partID, String partName, String fullPartName) 
   {
      Part p = createPart((short) partID, partName, fullPartName);
      if (p == null)
        return null;

      Gdb.debugOutput("Adding part: " + partName + " as part ID# " +
            Integer.toString(partID) + " to module " +
            Integer.toString(_moduleID) + ": " + _moduleName);

      _parts.put(new Integer(partID), p);
      _partIDs.put(partName, new Integer(partID));

      _moduleEntryChanged = true;
      _moduleEntryHasParts = true;
      return p;
   }

   /**
    * Gets the ERepNextModuleEntry class corresponding to this module
    */
   public ERepNextModuleEntry getEPDCModule()
   {
      int flags = _moduleEntryNew ? EPDC.ModuleEntryNew : 0;
      flags |= _moduleEntryHasParts ? EPDC.ModuleEntryHasParts : 0;
      flags |= _moduleEntryHasDebugData ? EPDC.ModuleEntryHasDebugData : 0;

      ERepNextModuleEntry m = new ERepNextModuleEntry(_moduleID, _moduleName,
            _moduleName, flags);

      Enumeration en = _parts.keys();
      while (en.hasMoreElements())
         m.addPartID(((Integer) en.nextElement()).intValue());

      _moduleEntryNew = false;
      _moduleEntryChanged = false;

      return m;
   }

   /**
    * Gets a part.
    * @return the part; if the part does not exist, returns null
    */
   public Part getPart(int partID)
   {
      return (Part) _parts.get(new Integer(partID));
   }

   /**
    * Get the part ID for a part name
    * @return 0 if the part ID is not registered and is "dummy"
    * @return dummy's part id for all other parts
    */
   public int getPartID(String partName)
   {
      if (_partIDs.containsKey(partName))
         return ((Integer) _partIDs.get(partName)).intValue();
         
      return 0;
   }

   /**
    * Return a list of the part IDs contained in this module
    */
   public int[] getPartIDs() 
   {
      int[] partIDs = new int[_partIDs.size()];

      Enumeration en = _partIDs.elements();

      for(int i=0; i<partIDs.length; i++)
         partIDs[i] = ((Integer) en.nextElement()).intValue();
      return partIDs;
   }

   public String getModuleName()
   {
      return _moduleName;
   }

   public String getModuleNameAsPath()
   {
      return _moduleNameAsPath;
   }
   public void setModuleNameAsPath(String s)
   {
      _moduleNameAsPath = s;
   }

   public int getModuleID()
   {
      return _moduleID;
   }


   // data members
   private int _startAddress = -1;
   private int _finishAddress = -1;
   private String _startAddressHex = "0x????????";
   private String _finishAddressHex = "0x????????";
   public String getStartAddressHex()   {  return _startAddressHex; }
   public String getFinishAddressHex()   {  return _finishAddressHex; }

   protected DebugSession _debugSession;
   protected int         _moduleID;
   protected String      _moduleName;
   protected String      _moduleNameAsPath;
   protected Hashtable   _parts;   // contains parts with Part ID as the key
   protected Hashtable   _partIDs; // contains part ID's with part name as the key

   public  boolean isDebuggable() 
   {   return _moduleEntryHasDebugData;    }
   public  void setIsDebuggable(boolean b) 
   {   _moduleEntryHasDebugData=b;         }

   protected boolean _moduleEntryNew;
   protected boolean _moduleEntryChanged;
   protected boolean _moduleEntryHasParts;
   protected boolean _moduleEntryHasDebugData;
}
