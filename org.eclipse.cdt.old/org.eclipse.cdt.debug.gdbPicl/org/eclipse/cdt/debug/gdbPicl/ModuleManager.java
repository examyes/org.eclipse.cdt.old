/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;
import  org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.util.*;
import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Manages the module entry table and the part table
 */
public class ModuleManager extends ComponentManager
{
   ModuleManager(DebugSession debugSession)
   {
      super(debugSession);
      _modules        = new Vector();
      _moduleIDs      = new Hashtable();
      _changedParts   = new Vector();
      _changedModules = new Vector();
      _partToModule   = new Hashtable();
      _partID         = 0;
      _entryIDs       = new Vector();
   }

   /** Update module and parts tables to contain to the known parts */
   void updateModules() 
   {
      if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"Updating module entries and parts table to correspond to known parts");

      if (!_newPartsLoaded)
      {
          if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"No new parts have been added");
          return;
      }

      String[] parts = _debugEngine.getDebugSession().getNewPartsList();

//?????????????????????????????????????????????????????????????????????????
      if (Gdb.traceLogger.ERR) 
         Gdb.traceLogger.err(2,"\n\n######## ModuleManager.updateModules registering "+parts.length+" new parts  ############### SHOULD BE PARTS");

      for (int i=0; i<parts.length; i++)
      {
          checkPart(parts[i]);
      }
      _newPartsLoaded = false;
//?????????????????????????????????????????????????????????????????????????
   }


   /**
    * Returns the moduleID for the specified module name.  Returns -1 if the
    * module does not exist.
    */
   public int getModuleID(String moduleName)
   {
   	  // default to 1 to prevent infinite loop
      int moduleID = 1;
      if(moduleName==null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getModuleID moduleName==null");
         return moduleID;
      }
 
      if (_moduleIDs.containsKey(moduleName) )
      {
         Integer moduleIDInt = (Integer) _moduleIDs.get(moduleName);
         moduleID = moduleIDInt.intValue();
      }
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"ModuleManager.getModuleID moduleName="+moduleName+ " moduleID="+moduleID  );
      return moduleID;
    }

   /**
    * Get the module IDs
    */
   int[] getModuleIDs() 
   {
      int[] modules = new int[_modules.size()];
      for(int i=0; i<_modules.size(); i++)
      {
          Module module = (Module) _modules.elementAt(i);
          modules[i] = module.getModuleID();
      }
      return modules;
   }

   /**
    * Returns the ModuleID for the specified part name.  Returns -1 if the
    * part does not exist.
    */
   public int getModuleID(String partName, String fullPartName) 
   {
      //Gdb.debugOutput("Attempting to get part ID for " + partName);
      if(partName==null || partName.equals("") || fullPartName==null || fullPartName.equals("") )
         return 0;

      Module module = null;

      int[] moduleIDs = getModuleIDs();
      for(int j=0; j<moduleIDs.length; j++)
      {
            int partID = getPartID( moduleIDs[j], partName ); 
            if (partID<=0)
                return 1;

            Part part = getModule(moduleIDs[j]).getPart(partID);
            if(part==null)
            {
                if (Gdb.traceLogger.ERR) 
                    Gdb.traceLogger.err(2,"ModuleManager.getModuleID part==NULL for moduleID="+moduleIDs[j] +" partID="+partID );
                return 1;
            }

            if(!part.getSourceFileName().equals(fullPartName) )
            {   if (Gdb.traceLogger.ERR) 
                    Gdb.traceLogger.err(2,"ModuleManager.getModuleID partName="+partName+" MISMATCH target fullPartName="+fullPartName+" actual part.fullFileName="+part.getSourceFileName()  );
                return moduleIDs[j]; // should search through other parts !!
            }


            return moduleIDs[j];
      }
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"ModuleManager.getModuleID could not find partName="+partName +" fullPartName="+fullPartName );
      return 1;
   }

   /**
    * Returns the PartID for the specified moduleID+partName.  Returns -1 if the
    * part does not exist.
    */
   public int getPartID(int moduleID, String partName) 
   {
      if(moduleID<=0 || _modules.size()==0)
         return -1;
      Module module = (Module) _modules.elementAt(moduleID-1);
      return module.getPartID(partName);
   }


   /**
    * Returns the First PartID for the specified part name.  Returns -1 if the
    * part does not exist.
    */
   public int getPartID(String partName) 
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(3,"ModuleManager.getPartID for partName=" + partName);
//      Gdb.debugOutput("Attempting to get part ID for " + partName);

      int partID = -1;

      Vector partIDs = getPartIDs(partName);
      if(partIDs!=null && partIDs.size()>1)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"######## ModuleManager.getPartID IGNORING additional/totalPartsSize="+partIDs.size()+" for part="+partName );
      }
      if(partIDs!=null && partIDs.size()>0)
      {
          partID = ((Integer)partIDs.elementAt(0)).intValue();
      }

      return partID;
   }

   /**
    * Returns the first ModuleID for the specified part name.  Returns -1 if the
    * part does not exist.
    */
   public int findFirstModuleID(String partName) 
   {
      int moduleID = -1;

      int partID = getPartID(partName);
      if(partID>0)
      {
          Part p = getPart(partID);
          moduleID = p.getModuleID();
      }

      return moduleID;
   }


   /**
    * Returns the part ID for the given entry ID.
    */
   public int getPartID(int entryID) 
   {
      if (_entryIDs == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getPartID _entryIDs not yet initialized");
         return 0;
      }
      if (entryID <= 0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getPartID entryID="+entryID );
         return 0;
      }
      Integer entryIDInt = (Integer) _entryIDs.elementAt(entryID-1);

      if (entryIDInt == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getPartID invalid entryID==null");
         return -1;
      }

      int methodID = entryIDInt.intValue();
      return methodID >>> 16;
   }

   /**
    * Get the part IDs contained in the given module.  Returns an empty
    * list if the moduleID is invalid.
    */
   public int[] getPartIDs(int moduleID) 
   {
   	  if (_modules == null)
   	  	return new int[0];
   	
      Module module = (Module)_modules.elementAt(moduleID-1);

      if (module == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getPartIDs invalid moduleID="+moduleID);
         return new int[0];
      }
      else
      {
         return module.getPartIDs();
      }
   }

   /**
    * Get the part IDs of all parts with the given name.  Returns an empty
    * list if the part name is not found in any package.
    */
   public Vector getPartIDs(String partName) 
   {
      int i;
      Vector partIDVec = new Vector();
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Getting parts with name: " + partName);

      for (i=1; i<=_partID; i++)       
      {
         Part p = getPart(i);
         if (p != null && 
              (p.getPartName().equals(partName) 
               || p.getFullPartName().equals(partName) ))
         {
            partIDVec.addElement(new Integer(i));
         }
      }
 
      return partIDVec;
   }
 
   /**
    * Checks if the given module part is in the module entry and parts table.
    * If not, it is added.
    */
   public void addModulePart(String moduleName, String fullModuleName, String partName, String fullPartName) 
   {
      int moduleID;
      Module module = null;

      if (_moduleIDs.containsKey(moduleName)) 
      {
         moduleID = (((Integer) _moduleIDs.get(moduleName))).intValue();
         module = (Module) _modules.elementAt(moduleID-1);
      }
      else 
      {
         // moduleName is not registered, create a new module entry
         moduleID = _modules.size() + 1;

         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"<<<<<<<<======== ModuleManager.addModulePart Adding Module "+Integer.toString(moduleID) +": "+moduleName);

         module = new Module(_debugSession, moduleID, moduleName, fullModuleName);

         _modules.addElement(module);
         _moduleIDs.put(moduleName, new Integer(moduleID));
      }

      if (module.getPartID(partName) == 0) 
      {
         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(2,"ModuleManager Adding ID="+(_partID+1)+" part=" + partName + " from module=" +
              moduleName + " to module entry table.");

         Part p =module.addPart(++_partID, partName, fullPartName );
         _partToModule.put(new Integer(_partID), new Integer(moduleID));
         if( partIsDebuggable(_partID) )
             module.setIsDebuggable(true);

         // add the module (if not already added) to the _changedModules vectors
         // and add the new part to the _changedParts vector
         if (!_changedModules.contains(module))
         {   _changedModules.addElement(module);
         }
         _changedParts.addElement(p);
      }
   }

   /**
    * Checks if the given module is in the module entry table.
    * If not, it is added.
    */
   public void addModule(String moduleName, String fullModuleName) 
   {
      int moduleID;
      Module module = null;

      if (_moduleIDs.containsKey(moduleName)) 
      {
         moduleID = (((Integer) _moduleIDs.get(moduleName))).intValue();
         module = (Module) _modules.elementAt(moduleID-1);
      }
      else 
      {
         // moduleName is not registered, create a new module entry
         moduleID = _modules.size() + 1;

         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(2,"---------------- ModuleManager.addModule Adding module " + Integer.toString(moduleID) +": " +moduleName);

         module = new Module(_debugSession, moduleID, moduleName, fullModuleName);

         _modules.addElement(module);
         _moduleIDs.put(moduleName, new Integer(moduleID));
         _changedModules.addElement(module);
      }
   }


   public Part addPart(int moduleID, String partName, String fullPartName)
   {
         Module module = (Module)_modules.elementAt(moduleID-1);

         Part p = module.addPart(++_partID, partName, fullPartName );
         _partToModule.put(new Integer(_partID), new Integer(moduleID));
         if( partIsDebuggable(_partID) )
             module.setIsDebuggable(true);

         // add the module (if not already added) to the _changedModules vectors
         // and add the new part to the _changedParts vector
         if (!_changedModules.contains(module))
         {   _changedModules.addElement(module);
         }
         _changedParts.addElement(p);
         
         return p;
   }

   public Module getModule(int moduleID)
   {
      if(moduleID<=0 || _modules.size()==0)
         return null;
      Module module = (Module) _modules.elementAt(moduleID-1);
      return module;
   }

   public Module getModule(String moduleName)
   {
      int moduleID = getModuleID(moduleName);
      if(moduleID<=0 || _modules.size()==0)
         return null;
      Module module = (Module) _modules.elementAt(moduleID-1);
      return module;
   }

   /**
    * Get the module name of the given module ID.  Returns an empty string
    * if the moduleID is invalid.
    */
   public String getModuleName(int moduleID) 
   {
      if(moduleID<=0 || _modules==null || moduleID>_modules.size())
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getModuleName invalid moduleID="+moduleID );
         return "";
      }

      Module module = (Module) _modules.elementAt(moduleID-1);

      if (module == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getModuleName invalid moduleID="+moduleID );
         return "";
      }
      else
      {
         return module.getModuleName();
      }
   }

   /**
    * Get the module name as a path for the given module ID.  Returns an empty 
    * string if the moduleID is invalid.
    */
   String getModuleNameAsPath(int moduleID) 
   {
      Module module = (Module) _modules.elementAt(moduleID-1);

      if (module == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getModuleNameAsPath invalid moduleID="+moduleID );
         return "";
      }
      else
      {
         return module.getModuleNameAsPath();
      }
   }

   /**
    * Return number of parts
    */
   int numParts() 
   {
      return _partID;
   }

   /**
    * Return number of modules
    */
   public int numModules() 
   {
      return _modules.size();
   }

   /**
    * Return whether or not a part is verified.
    */
   boolean isPartVerified(int partID)
   {
      Part p = getPart(partID);

      if (p == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.isPartVerified invalid partID="+partID);
         return false;
      }
      else
      {
         return p.isVerified();
      }
   }

   /**
    * Checks if the given part is in the module entry and parts table.
    */
   public void checkPart(String partName) 
   {
      if (Gdb.traceLogger.ERR) 
      {   Gdb.traceLogger.err(2,"######## ModuleManager ****UNUSED**** checkPart(partName)  ########## partName="+partName );
          Thread.currentThread().dumpStack();
      }

//      int moduleID = findFirstModuleID(String partName) 
// /////////////// we could search every module for a non-debuggable part?  WHY??

      return;
   }

   /**
    * Checks if the given part is in the module entry and parts table.
    */
   public void checkPart(int moduleID, String partName) 
   {
      if(moduleID<=0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.checkPart INCOMING moduleID="+moduleID );
         return;
      }

      Module module = (Module) _modules.elementAt(moduleID-1);
      
      String shortName;
      int lastSlash = partName.lastIndexOf("/");    
      if (lastSlash != -1)
      {
		shortName = partName.substring(lastSlash+1);
      }
      else
      {
       	shortName = partName;
      }
      
      if (module.getPartID(shortName) == 0) 
      {

         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"####### ModuleManager.checkPart ***UNIMPLEMENTED*** moduleID="+moduleID+" partName="+partName +"\n" );

         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.checkPart BLINDLY Adding part " + partName + " moduleID="+ moduleID );
 
         Part part = addPart(moduleID, shortName, partName);
      }
   }

   public void addChangedPart(Part part)
   {
      if (!_changedParts.contains(part))
         _changedParts.addElement(part);
   }

   public void addChangesToReply(EPDC_Reply rep)
   {
      // first make Part change packet

      for (int i=0; i<_changedParts.size(); i++)
      {
         Part part = (Part) _changedParts.elementAt(i);

         rep.addPartChangePacket(part.getEPDCPart());
      }

      _changedParts.removeAllElements();

      // Now make Module change packet
      for (int i=0; i<_changedModules.size(); i++)
      {
         rep.addModuleChangePacket(
            ((Module) _changedModules.elementAt(i)).getEPDCModule());
      }

      _changedModules.removeAllElements();
   }

   /**
    * Parses a part name into package and class name.
    * @return a two-element array.  The first element is the package name, the
    * second is the class name.
    */
   private String[] parseClassName(String className)
   {
      String[] parsedName = new String[2];

      // Find the last "." in the className.  The substring before this period 
      // is the package name.  The substring after it is the class name
      int index = className.lastIndexOf(".", className.length());
      if (index == -1) 
      {
         parsedName[0] = _debugEngine.getResourceString("DEFAULT_PACKAGE_TEXT");
         parsedName[1] = new String(className);
      } 
      else {
         parsedName[0] = className.substring(0, index);
         parsedName[1] = className.substring(index+1, className.length());
      }
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"######## ModuleManager GET RID OF PARSED_NAME_3 ########## className="+className
             +" >>parsedName[0]="+parsedName[0]+" >>parsedName[1]="+parsedName[1]+" ######### index="+index );
      parsedName[1] = className;

      return parsedName;
   }

   /** Get the PartName for this part ID */
   public String getPartName(int partID)
   {
       String name = null;
       if(partID<=0)
       {
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(3,"ModuleManager,getPartName invalid partID="+partID );
            return name;
       }
       Part p = getPart(partID);
       if(p==null)
       {
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(3,"ModuleManager,getPartName part==null for partID="+partID );
            return name;
       }
       
       return p.getPartName();
   }

   /** Get the Part for this part ID */
   public Part getPart(int partID)
   {
      Module module;
      Part   part;
 
      Integer partIDInt   = new Integer(partID);
      Integer moduleIDInt = (Integer)_partToModule.get(partIDInt);

      if (moduleIDInt == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getPart invalid partID="+partID);
         return null;
      }
      else
      {
         module = (Module) _modules.elementAt( moduleIDInt.intValue()-1);
         return module.getPart(partID);
      }
   }

   /**
    * Get the entry for the given entry ID
    */
   public ERepEntryGetNext getEntry(int entryID) 
   {
     EStdView stdView = new EStdView((short)0,(short)0,0,0);
     ERepEntryGetNext empty = new ERepEntryGetNext(0, "nullName","nullDemangled", "nullReturnType", stdView);

      if (_entryIDs == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getEntry _entryIDs not yet initialized");
         return empty;
      }
      if (entryID <= 0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntry entryID="+entryID );
         return empty;
      }

      Integer entryIDInt = (Integer)_entryIDs.elementAt(entryID-1);

      if (entryIDInt == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntry invalid entryID==null");
         return empty;
      }

      int methodID = entryIDInt.intValue();
      Part part = getPart(methodID >> 16);

      if (part == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getEntry invalid methodID="+methodID );
         return null;
      }
      else
      {
         return part.getEntry(methodID % 0x10000);
      }
   }

   /**
    * Get the entries (methods) corresponding to a part ID and entry name
    * @return array of ERepEntryGetNext items
    */
   public ERepEntryGetNext[] getEntries(int partID, String entryName, boolean caseSensitive) 
   {
      if ((entryName == null) || (entryName.equals("")))
      {
         Part part = getPart(partID);

         if (part == null)
         {
            return new ERepEntryGetNext[0];
         }
         else
         {
            return part.getEntries();
         }
      }
      else
      {
         Part part = getPart(partID);

         if (part == null)
         {
            return new ERepEntryGetNext[0];
         }
         else
         {
            return part.getEntries(entryName, caseSensitive);
         }
      }
   }

   /**
    * Get the list of entry ids in this part that match an entry name
    */
   int[] getEntryIDs(int partID, String entryName, boolean caseSensitive)
   {
     Part part = getPart(partID);

     if (part == null)
         return new int[0];

     if (entryName == null || entryName.equals(""))
         return null;
     else
         return part.getEntryIDs(entryName, caseSensitive);
   }

   /**
    * Add this method to the entry ID vector and get an entry ID for the SUI
    * @return SUI entry ID
    */
   public int addEntry(int partID, int methodIndex) 
   {
      _entryIDs.addElement(new Integer((partID << 16) + methodIndex));
      return _entryIDs.size();
   }

   /**
    * Get the line number for an entry
    * @return the entry line number, or 0 if it does not exist
    */
   public int getEntryLineNumber(int entryID) 
   {
      if (_entryIDs == null)
      {
         if (Gdb.traceLogger.ERR) 
            Gdb.traceLogger.err(2,"ModuleManager.getEntryLineNumber _entryIDs not yet initialized");
         return 0;
      }
      if (entryID <= 0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryLineNumber entryID="+entryID );
         return 0;
      }

      Integer entryIDInt = (Integer)_entryIDs.elementAt(entryID-1);

      if (entryIDInt == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryLineNumber invalid entryID==null");
         return 0;
      }

      int methodID = entryIDInt.intValue();

      // get the part ID (the upper 2 bytes of the entry ID)
      int partID = methodID >>> 16;
      Part p = getPart(partID);

      if (p == null)
      {
         return 0;
      }
      else
      {
         return p.getEntryLineNumber(methodID % 0x10000);
      }
   }

   /**
    * Clear list of parts and modules
    */
   void clearModules() {
      _partID = 0;
      _modules.removeAllElements();
      _moduleIDs.clear();
      _changedParts.removeAllElements();
      _changedModules.removeAllElements();
      _partToModule.clear();
      _entryIDs.removeAllElements();
   }

   /**
    * Get the entry ID for the given part ID and line number.  Returns -1
    * if the partID is invalid.
    */
   public int getEntryID(int partID, int lineNum) 
   {
      Part p = getPart(partID);
 
      if (p == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryID invalid partID=partID");
         return -1;
      }
      else
      {
         return p.getEntryID(lineNum);
      }
   }

   /**
    * Get the entry ID for the given part ID and method name.  Returns -1
    * if the partID is invalid.
    */
   public int getEntryID(int partID, String methodName) 
   {
      Part p = getPart(partID);

      if (p == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryID invalid partID=partID");
         return -1;
      }
      else
      {
         return p.getEntryID(methodName);
      }
   }

   /**
    * Get the method index for an entry ID. Returns -1 if the entryID is
    * invalid.
    */
   int getMethodIndex(int entryID) 
   {
      if (_entryIDs == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getMethodIndex _entryIDs not yet initialized");
         return 0;
      }
      if (entryID <= 0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getMethodIndex entryID="+entryID );
         return 0;
      }

      Integer entryIDInt = (Integer)_entryIDs.elementAt(entryID-1);

      if (entryIDInt == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getMethodIndex invalid entryID==null");
         return -1;
      }
      else
      {
         return entryIDInt.intValue() % 0x10000;
      }
   }

   /**
    * Get the entry name for the given entry ID
    * @return the entry ID, or an empty string if not found
    */
   public String getEntryName(int entryID) 
   {
      if (_entryIDs == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getEntryName _entryIDs not yet initialized");
         return "";
      }
      if (entryID <= 0)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryName entryID="+entryID );
         return "";
      }

      Integer entryIDInt = (Integer) _entryIDs.elementAt(entryID-1);
      if (entryIDInt == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryName invalid entryID==null");
         return "";
      }

      int methodID = entryIDInt.intValue();
      int partID = methodID >>> 16;

      Part part = getPart(partID);

      if (part == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.getEntryName invalid partID="+partID );
         return "";
      }
      else
      {
         return part.getEntryName(methodID % 0x10000);
      }
   }

   /**
    * Get the full part name for the specified part ID. Returns an empty
    * string if the part is invalid.
    */
   public String getFullPartName(int partID) 
   {
      Part part = getPart(partID);

      if (part == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"ModuleManager.getFullPartName invalid partID="+partID );
         return "";
      }
      else
      {
         return part.getFullPartName();
      }
   }

   public void setModuleDebuggable(String moduleName, boolean b)
   { 
      int moduleID = getModuleID(moduleName);
      setModuleDebuggable(moduleID, b);
   }
   public void setModuleDebuggable(int moduleID, boolean b)
   { 
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"ModuleManager.setModuleDebuggable moduleID="+moduleID+" debuggable="+b );
      if(moduleID<=0 || _modules.size()==0)
         return;
      Module module = (Module) _modules.elementAt(moduleID-1);
      module.setIsDebuggable(b);
   }

   /**
    * Return whether the part has source associated with it
    */
   public boolean partIsDebuggable(int partID) 
   {
      Part part = getPart(partID);
 
      if (part == null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"ModuleManager.isPartDebuggable invalid partID="+partID );
         return false;
      }
      else
      {
         return part.isDebuggable();
      }
   }

   /**
    * Set this flag to true when a new module is loaded, and false otherwise.
    */
   void setNewModulesLoaded(boolean flag)
   {
     _newPartsLoaded = flag;
   }
 
   /**
    * Set the startingAddress and endingAddress for sharedLibrary modules
    */
   public void setModuleStartFinishAddress(String moduleName, Vector segments)
   { 
      int moduleID = getModuleID(moduleName);
      setModuleStartFinishAddress(moduleID, segments);
   }
   /**
    * Set the startingAddress and endingAddress for sharedLibrary modules
    */
   private void setModuleStartFinishAddress(int moduleID, Vector segments)
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"ModuleManager.setModuleStartFinishAddress moduleID="+moduleID+" with segments size="+segments.size() );
      if(moduleID<=0 || _modules.size()==0)
         return;
      Module module = (Module) _modules.elementAt(moduleID-1);
      module.setModuleSegment(segments);
   }

   /**
    * Checks the startingAddress and endingAddress to see if this address is contained (default is true)
    */
   public int containsAddress(String address)
   {
       if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"ModuleManager.containsAddress address="+address );
       int[] moduleIDs = getModuleIDs();
       for(int j=0; j<moduleIDs.length; j++)
       {
           boolean b = containsAddress(moduleIDs[j],address);
           if(b)
              return moduleIDs[j];
       }
       return getModuleID( ((GdbDebugSession)_debugEngine.getDebugSession()).getProgramName() );
   }
   /**
    * Checks the startingAddress and endingAddress to see if this address is contained (default is true)
    */
   private boolean containsAddress(String moduleName, String address)
   {
      int moduleID = getModuleID(moduleName);
      return containsAddress(moduleID, address);
   }
   /**
    * Checks the startingAddress and endingAddress to see if this address is contained (default is true)
    */
   boolean containsAddress(int moduleID, String address)
   {
      if(moduleID<=0 || _modules.size()==0)
         return false;
       Module module = (Module) _modules.elementAt(moduleID-1);
       return module.containsAddress(address);
   }
   
   boolean isNewModule(String moduleName)
   {
      if (_moduleIDs.containsKey(moduleName)) 
      {
      	if (this.getModule(moduleName).isDeleted())
      	{
      		this.getModule(moduleName).undelete();
      		return true;
      	}
      	else
      	{      		
	        return false;
      	}
      }
      else
      {
      	return true;
      }
   }


   // Data Members
   protected Vector     _modules;
   protected Hashtable  _moduleIDs;   // stores module ID's with module names as 
                                    // the key

   protected Vector     _changedParts;   // stores changed parts
   protected Vector     _changedModules; // stores changed modules
   protected int        _partID;         // maximum part ID assigned
   protected Hashtable  _partToModule;   // hashtable of module ID's to 
                                       // corresponding part ID

   protected Vector    _srcPaths;        // list of paths to search for source
   protected Vector    _entryIDs;        // vector that maps SUI entry IDs to JDE
                                       // entry IDs JDE entry ID's are 4 byte 
                                       // integer (ints) with the part ID as the
                                       // upper two bytes and the method index as
                                       // the lower two bytes
   private boolean _newPartsLoaded = false;
}
