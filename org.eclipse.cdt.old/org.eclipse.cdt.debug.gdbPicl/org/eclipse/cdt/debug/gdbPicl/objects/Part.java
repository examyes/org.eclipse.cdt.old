/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/Part.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:44)   (based on Jde 1.35.1.30 12/7/00)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;


/**
 * This class stores information corresponding to an EPDC "Part".  For the Java
 * debugger, a part corresponds to a class.  A Part object maintains a list of 
 * View objects.
 */
public abstract class Part
{
   public Part(DebugSession debugSession)
   {
      _debugSession = debugSession;
   }

   /** Returns part name corresponding to this part. */
   public String getPartName()
   {
      return _partName;
   }

  /**
   * Verified our views for this part.
   */
   public void verifyViews() 
   {
      // Ask all our views to verify themselves 
      for (int viewNum = 0; viewNum < NUM_VIEWS; viewNum ++)
      {
         _views[viewNum].verifyView();
      }
   }

   public View getView(int viewNum)
   {
      if (viewNum >0 && viewNum <= NUM_VIEWS)
      {
         return _views[viewNum-1];
      }
      else
      {
         Gdb.debugOutput("Invalid view number requested");
         return null;
      }
   }

  /**
   * Gets the view lines for the specified view.
   */
   void getViewLines(ERepPartGet rep, int viewNum, int startLine, int numLines)
   {
      if (viewNum >0 && viewNum <= NUM_VIEWS)
      {
         _views[viewNum-1].getViewLines(rep, startLine, numLines);
      }
      else
      {
         Gdb.debugOutput("Invalid view number requested");
      }
   }

   public void setPartChanged(boolean changed)
   {
      _PartChanged = changed;
      _debugSession.getModuleManager().addChangedPart(this);
   }

   boolean isPartChanged() {
      return _PartChanged;
   }

   boolean isPartNew() {
      return _PartNew;
   }

   boolean isPartDeleted() {
      return _PartDeleted;
   }

   public boolean isVerified()
   {
      return _PartVerified;
   }

   public int getModuleID()
   {
      return _moduleID;
   }

   public short getPartID()
   {
      return _partID;
   }

   public String getName()
   {
      return _partName;
   }

   public String getSourceFileName()
   {
      return _fullPartName;
   }
   public String getFullPartName()
   {
      return _fullPartName;
   }

   /** Get the part change packet corresponding to this part. */
   public ERepNextPart getEPDCPart()
   {
      int partAttr;

      partAttr  = _InUse ? EPDC.InUse : 0;
      partAttr |= _PartVerified ? EPDC.Verified : 0;
      partAttr |= _PartNew ? EPDC.PartNew : 0;
      partAttr |= _PartDeleted ? EPDC.PartDeleted : 0;
      partAttr |= _PartChanged ? EPDC.PartChanged : 0;
      partAttr |= _SymbolTbl ? EPDC.SymbolTbl : 0;

      _PartNew = false;

      DebugEngine _debugEngine = _debugSession.getDebugEngine();
      ERepNextPart nextPartRep = 
         new ERepNextPart(_debugEngine.getSession(), _partID, partAttr,
                          _PartLang, _partName, _fullPartName, "", _moduleID);

      for (int viewNum = 0; viewNum < NUM_VIEWS; viewNum++)
      {
         _views[viewNum].addViewToReply(nextPartRep);
      }

      return nextPartRep;
   }

   /**
    * Get the entry given by methodIndex and return it as an ERepEntryGetNext 
    * structure
    */
   public ERepEntryGetNext getEntry(int methodIndex)
   {
      if (_methods == null)
         loadMethods();
 
      EStdView stdView = new EStdView(_partID,(short)1,1,_methods[methodIndex]._lineNum);

      return new ERepEntryGetNext(_entryIDs[methodIndex],
           _methods[methodIndex]._name, _methods[methodIndex]._fullName,
           _methods[methodIndex]._returnType, stdView);
   }

   /**
    * Return an array of indexes for all entries in this part.
    */
   public int[] getEntryIDs()
   {
     if (_methods == null)
         loadMethods();

     if (_entryIDs == null || _entryIDs.length == 0)
         return new int[0];

     return _entryIDs;

   }

   /**
    * Get the entries (methods) for this part and return the information in an
    * array of ERepEntryGetNext classes.
    */
   public ERepEntryGetNext[] getEntries() 
   {
      if (_methods == null)
         loadMethods();

      if (_methods == null || _methods.length==0)
         return null;

      ERepEntryGetNext[] entries = new ERepEntryGetNext[_methods.length];

      for (int i=0; i<_methods.length; i++)
      {
         // create entry packet
         EStdView stdView = new EStdView(_partID,(short)1,1,_methods[i]._lineNum);
         entries[i] = new ERepEntryGetNext(_entryIDs[i], _methods[i]._name,
                     _methods[i]._fullName, _methods[i]._returnType, stdView);
      }

      return entries;
   }

  /**
   * Get the entries that start with <code> methodName </code> and return as 
   *an array of
   * ERepEntryGetNext classes
   */
   public ERepEntryGetNext[] getEntries(String methodName, boolean caseSensitive)
   {
      if (_methods == null)
         loadMethods();

      // add all entries that match methodName to a vector
      Vector entryVec = new Vector();
      for (int i=0; i<_methods.length; i++)
      {
         if ( (_methods[i]._fullName.length()) >= methodName.length())
         {
            String ourMethodName = 
               _methods[i]._fullName.substring(0,methodName.length());

            if (caseSensitive && ourMethodName.equals(methodName) ||
                !caseSensitive && ourMethodName.equalsIgnoreCase(methodName))
            {
               EStdView stdView = new EStdView(_partID,(short)1,1,_methods[i]._lineNum);
               entryVec.addElement(new ERepEntryGetNext(_entryIDs[i],
                  _methods[i]._name, _methods[i]._fullName,
                  _methods[i]._returnType, stdView));
            }
         }
      }

      // now create the array
      ERepEntryGetNext[] entries = new ERepEntryGetNext[entryVec.size()];
      for (int i=0; i<entryVec.size(); i++)
      {
         entries[i] = (ERepEntryGetNext) entryVec.elementAt(i);
      }

      return entries;
   }

   public int[] getEntryIDs(String methodName, boolean caseSensitive)
   {
     if (_methods == null)
         loadMethods();

     if (_entryIDs == null || _entryIDs.length == 0)
         return new int[0];

     // add all entries that match methodName to a vector
     Vector ids = new Vector();
     for (int i=0; i<_methods.length; i++)
     {
          if ((_methods[i]._fullName.length()) >= methodName.length())
          {
               String name = _methods[i]._fullName.substring(0,methodName.length());

               if (caseSensitive && name.equals(methodName) ||
                   !caseSensitive && name.equalsIgnoreCase(methodName))
               {
                   ids.addElement(new Integer(_entryIDs[i]));
               }
          }
     }

     int[] entryIDs = new int[ids.size()];
     for (int i = 0; i < ids.size(); i++)
          entryIDs[i] = ((Integer)ids.elementAt(i)).intValue();

     return entryIDs;
   }

   /**
    * Fetches and stores a list of methods for this part
    */
   abstract void loadMethods();                //HC

   /**
    * Get the first executable line number for in a method
    * @return line number, or 0 if entry is not registered
    */
   public int getEntryLineNumber(int methodNum)
   {
      if (methodNum > _methods.length)
         return 0;
      Gdb.debugOutput("Part.getEntryLineNumber Getting method line number for " + _fullPartName + ":" +
         _methods[methodNum]._fullName);

      return _methods[methodNum]._lineNum;
   }

   /**
    * Get the entry id for a specific line number.  This is done by finding the
    * last method that begins before the given line number.
    * @return entry ID
    */
   public int getEntryID(int lineNum) 
   {
      int minMeth=0, minDist=0;

      if (_methods == null)
      {
         loadMethods();
      }

      // No methods found
      if (_methods==null || _methods.length == 0 )
	return 0;

      // Find the method corresponding to this line number.  This is done by 
      // finding the method which begins at the smallest number of lines before
      // lineNum.
      for (int i=0; i<_methods.length; i++) 
      {
         if ((_methods[i]._startLineNum <= lineNum) && ((i==0) ||
                (lineNum-_methods[i]._startLineNum < minDist))) 
         {
            minMeth = i;
            minDist = lineNum-_methods[i]._startLineNum;
         }
      }
      Gdb.debugOutput("Part.getEntryID Line " + lineNum + " is in method " + 
      		      _methods[minMeth]._fullName);

      // get entry ID
     if(_entryIDs==null || _entryIDs.length<=minMeth)
     {
         return -1;
     }
     return _entryIDs[minMeth];
   }

   /**
    * Get the entry ID given the method name.  The entry ID returned 
    * corresponds to the first method that matches all characters in the given
    * method name.
    * @return entry ID, or 0 if not found
    */
   public int getEntryID(String methodName)
   {
      int index=0, returnIndex;
      boolean methodFound = false;

      if (_methods == null)
      {
         loadMethods();
      }

      // No methods found
      if (_methods==null || _methods.length == 0)
	return 0;

      Gdb.debugOutput("Searching for function " + methodName);

      while ((index < _methods.length) && !methodFound) 
      {
         if ( (_methods[index]._fullName.length()) >= methodName.length())
         {
            if (_methods[index]._fullName.substring(0,
                   methodName.length()).equals(methodName))
            {
               methodFound = true;
            }
         }
         ++index;
      }

      if (methodFound) 
      {
         Gdb.debugOutput("Found function " + _methods[index-1]._fullName);
         return _entryIDs[index-1];
      }

      Gdb.debugOutput("Function not found");
      return 0;
   }

   /**
    * Get the entry name given the method index.
    */
   public String getEntryName(int index) {
      if (_methods == null)
      {
         loadMethods();
      }

      return _methods[index]._fullName;
   }

   /**
    * Returns whether this part has source associated with it.
    */
   public boolean isDebuggable()
   {
      return _SymbolTbl;
   }

   // Data members
   protected DebugSession _debugSession;
   protected short       _partID;
   protected int         _moduleID;
   protected String      _partName;
   protected String      _fullPartName;
   protected View[]      _views;
   protected String      _startAddress;
   protected String      _endAddress;

   // Part attributes
   protected boolean _InUse;
   protected boolean _PartNew;
   protected boolean _PartDeleted;
   protected boolean _PartChanged;
   protected boolean _PartVerified;
   protected boolean _SymbolTbl;


   // Entry information
   public void setEntryIDs(int[] entryIDs)
   {   _entryIDs = entryIDs;  }
   protected int[] _entryIDs;              // array of entry IDs by method index
   public void setMethods(MethodInfo[] methods)
   {   _methods = methods;  }
   public MethodInfo[] getMethods()
   {   return _methods;  }
   protected MethodInfo[] _methods;

   private static final byte _PartLang = EPDC.LANG_CPP;

   public static final int NUM_VIEWS        = 3;
   public static final int VIEW_SOURCE      = 1;
   public static final int VIEW_DISASSEMBLY = 2;
   public static final int VIEW_MIXED       = 3;
}
