/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;
import  java.math.*;

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
      _segments = new Vector(0);
      _parts   = new Hashtable();
      _partIDs = new Hashtable();

      _moduleEntryNew          = true;
      _moduleEntryChanged      = true;
      _moduleEntryHasParts     = false;

      _moduleEntryHasDebugData = true;

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
   public void setModuleSegment(Vector segs)
   {
	_segments = segs;
	if (Gdb.traceLogger.DBG)
		Gdb.traceLogger.dbg(1,"Module.setModuleSegment size="+_segments.size());
	if (Gdb.traceLogger.EVT)
		Gdb.traceLogger.evt(3,"Module.setModuleSegment with segment size="+_segments.size());
   }

	/**
	 * Checks the startingAddress and endingAddress to see if this address is contained (default is true)
	 */
	public boolean containsAddress(String address) {
		boolean contained = false;
		address = address.substring(2);
		BigInteger addr = new BigInteger("-1", 16);
		BigInteger start = new BigInteger("-1", 16);
		BigInteger end = new BigInteger("-1", 16);
		ModuleSegment seg;
		String startAddress, endAddress;
		int i;
	
		if (Gdb.traceLogger.DBG) {
			Gdb.traceLogger.dbg(
				3,
				"Entering Module.containsAddress targetAddress=" + address);
			for (i = 0; i < _segments.size(); i++) {
				seg = (ModuleSegment) _segments.elementAt(i);
				Gdb.traceLogger.dbg(
					2,
					"Module.containsAddress:seg["
						+ i
						+ "].startAddress="
						+ seg.getStartAddress()
						+ " seg["
						+ i
						+ "].endAddress="
						+ seg.getEndAddress());
			}
		}
		try {
			addr = new BigInteger(address, 16);
			for (i = 0; i < _segments.size(); i++) {
				seg = (ModuleSegment) _segments.elementAt(i);
				startAddress = seg.getStartAddress().substring(2);
				endAddress = seg.getEndAddress().substring(2);
				if (startAddress.length() > 0 && endAddress.length() > 0) {
					start = new BigInteger(startAddress, 16);
					end = new BigInteger(endAddress, 16);
				}
				if (addr.compareTo(start) >= 0 && addr.compareTo(end) <= 0) {
					contained = true;
					break;
				}
			}
		} catch (java.lang.NumberFormatException exc) {
			if (Gdb.traceLogger.DBG)
				Gdb.traceLogger.dbg(
					1,
					"Module.containsAddress NUMBER_EXCEPTION address=" + address);
		}
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(
				2,
				"Module.containsAddress targetAddress="
					+ address
					+ " CONTAINED="
					+ contained
					+ "\n");
	
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

//      Enumeration en = _parts.keys();
//      while (en.hasMoreElements())
//         m.addPartID(((Integer) en.nextElement()).intValue());

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
    * @return 0 if the part ID is not registered
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
   
   /**
	 * Gets the _moduleDeleted.
	 * @return Returns a boolean
	 */
	public boolean isDeleted() {
		return _moduleDeleted;
	}
	
	/**
	 * Sets the _moduleDeleted.
	 * @param _moduleDeleted The _moduleDeleted to set
	 */
	public void delete() {
		this._moduleDeleted = true;
	}
	
	/**
	 * Sets the _moduleDeleted.
	 * @param _moduleDeleted The _moduleDeleted to set
	 */
	public void undelete() {
		this._moduleDeleted = false;
	}



   // data members

   Vector _segments = new Vector(0);
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
   protected boolean _moduleDeleted = false;

}
