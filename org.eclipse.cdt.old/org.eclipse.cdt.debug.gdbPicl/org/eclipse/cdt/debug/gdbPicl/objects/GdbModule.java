/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class manages EPDC "Modules".  In the JDE, a GdbModule corresponds
 * to a package.
 */
public class GdbModule extends Module
{
   /**
    * Create a new GdbModule
    * @param ModuleID the EPDC Module ID
    * @param ModuleName the package name
    * @param FullPathModuleName the full path to the package
    */

   public GdbModule(GdbDebugSession debugSession, int moduleID, String moduleName, String moduleNameAsPath) 
   {
      super(debugSession, moduleID, moduleName, moduleNameAsPath);
/*
      _debugSession = debugSession;
      _moduleID    = moduleID;
      _moduleName  = moduleName;
      _moduleNameAsPath = moduleNameAsPath;

      _parts   = new Hashtable();
      _partIDs = new Hashtable();

      _moduleEntryNew          = true;
      _moduleEntryChanged      = true;
      _moduleEntryHasParts     = false;

      _moduleEntryHasDebugData = false;
*/

/*
      Vector filteredPackages = _debugEngine.getFilteredPackages();
      Enumeration enum = filteredPackages.elements();

      while (enum.hasMoreElements())
      {
         String filteredPackage = (String) enum.nextElement();
         // Append a '.' so our matching mechanism will work!
         String tmpModuleName = _moduleName + ".";
         if (tmpModuleName.toLowerCase().startsWith(filteredPackage))
         {
            _moduleEntryHasDebugData = false;
         }
      }
*/
   }

   /**
    * Add a part to this Module.  If the part ID already exists, the old part
    * with the same ID is replaced by the new part
    * @return the new part object
    */
   public Part addPart(int partID, String partName, String fullFileName) 
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"<<<<<<<<======== GdbModule.addPart Adding part "+Integer.toString(partID)+": " + partName 
             +" to GdbModule " +Integer.toString(_moduleID) + ": " + _moduleName  );

      Part p = new GdbPart(_debugSession, (short) partID, _moduleID, partName, fullFileName); //HC
      // if(p.isDebuggable())  setIsDebuggable(true); // is done in Part.CTOR
      _parts.put(new Integer(partID), p);
      _partIDs.put(partName, new Integer(partID));

      _moduleEntryChanged = true;
      _moduleEntryHasParts = true;
      return p;
   }

   /**
    * Gets the ERepNextModuleEntry class corresponding to this GdbModule
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
    * Return a list of the part IDs contained in this GdbModule
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


 
   // data members
/*
   private GdbDebugSession _debugSession;
   private int         _moduleID;
   private String      _moduleName;
   private String      _moduleNameAsPath;
   private Hashtable   _parts;   // contains parts with Part ID as the key
   private Hashtable   _partIDs; // contains part ID's with part name as the key

   private boolean _moduleEntryNew;
   private boolean _moduleEntryChanged;
   private boolean _moduleEntryHasParts;
   private boolean _moduleEntryHasDebugData;
*/ 
}
