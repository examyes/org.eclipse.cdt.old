package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/LocationBreakpoint.java, java-model, eclipse-dev, 20011128
// Version 1.26.1.2 (last modified 11/28/01 16:11:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * This class represents a breakpoint which can be mapped to a specific
 * location within the debuggee's code. It is subclassed by LineBreakpoint,
 * AddressBreakpoint, and EntryBreakpoint.
 * @see LineBreakpoint
 * @see AddressBreakpoint
 * @see EntryBreakpoint
 */

public abstract class LocationBreakpoint extends Breakpoint
{
  LocationBreakpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    super(owningProcess, epdcBkp);
  }

  void change(ERepGetNextBkp epdcBkp, boolean isNew)
  {
    super.change(epdcBkp, isNew);

    EStdView[] bkpLocations = epdcBkp.getContexts();

    // Get part in which this bkp is set. May be null if bkp is deferred.

    short partID;
    Part previousPart = _part;
    boolean fileHasChanged = false;

    for (int i = 0; i < bkpLocations.length; i++)
    {
         if ((partID = bkpLocations[i].getPPID()) != 0)
         {
             if (_part != getOwningProcess().getPart(partID))
             {
                 _part = getOwningProcess().getPart(partID);
                 break;
             }
             else
             // Check if the current ViewFile differs from the new ViewFile
             // for this breakpoint and if so update the model
             if (!isNew && bkpLocations[i].getSrcFileIndex() != _epdcLocations[i].getSrcFileIndex())
             {
                 fileHasChanged = true;
                 break;
             }
         }
    }

    if (isNew)
    {
       // set the location for adding the breakpoint
       _epdcLocations = bkpLocations;

       if (_part != null) // Tell the part that a bkp has been added
          _part.breakpointAdded(this);
    }
    else
    {
       if (previousPart != _part || fileHasChanged)
       {
          // If the location of the bkp has changed such that it is now
          // in a different part than it used to be, we need to tell the
          // new part that it has been added and the old part
          // that it has been removed.
          // cannot set the location until the breakpoint is removed

          if (previousPart != null)
             previousPart.breakpointRemoved(this);

          // update the location information
          _epdcLocations = bkpLocations;

          if (_part != null)
             _part.breakpointAdded(this);
       }

       _epdcLocations = bkpLocations;

    }
  }

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "LocationBreakpoint.prepareToDie()");

    super.prepareToDie();

    if (_part != null) // Tell the part that the bkp has been removed
       _part.breakpointRemoved(this);
  }

  /**
   * Get the part in which this breakpoint is set. This method is just a
   * shortcut - one can also determine the part in which the breakpoint
   * is set via the Location object returned by the getLocationWithinView
   * method.
   */

  public Part getPart()
  {
    return _part;
  }

  /**
   * Get the location of this breakpoint (file and line number).
   * @param ViewType A ViewInformation object which identifies a particular
   * kind of view (source, disassembly, etc.). The location returned will be
   * a location within a view of this type. Null will be returned if the
   * breakpoint location cannot be mapped to a location within this kind
   * of view.
   */

  public Location getLocationWithinView(ViewInformation viewInformation)
  throws java.io.IOException
  {
    EStdView epdcLocation = _epdcLocations[viewInformation.index()-1];

    try
    {
      return new Location(getOwningProcess(), epdcLocation);
    }
    catch (LocationConstructionException excp)
    {
      return null;
    }
  }

  /**
   * Get the location of this bkp as an EStdView:
   */

  EStdView getEPDCLocation(ViewInformation viewInformation)
  {
    return _epdcLocations[viewInformation.index()-1];
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.print("State: " + (isEnabled() ? "Enabled" : "Disabled"));
      printWriter.println(",  " + (isDeferred() ? "Deferred" : "Active"));
      printWriter.println("Thread ID: " + getThreadID());
      printWriter.println("Breakpoint Every: " + getEveryVal());
      printWriter.println("           To: " + getToVal());
      printWriter.println("           From: " + getFromVal());
      printWriter.println("Module: " + getModuleName());
      printWriter.println("Part: " + getPartName());
      printWriter.println("File: " + getFileName());
      printWriter.println("Address: " + getAddress());
      printWriter.println("Function: " + getFunctionName());

      if (!isDeferred())
      {
          try
          {
            if (getFunction() != null)
               getFunction().print(printWriter);
          }
          catch (java.io.IOException excp)
          {
          }

          ViewInformation[] views = getOwningProcess().debugEngine().supportedViews();

          for (int i = 0; i < views.length; i++)
          {
               if (views[i] != null)
               {
                   Location location = null;

                   try
                   {
                     location = getLocationWithinView(views[i]);
                   }
                   catch(java.io.IOException excp)
                   {
                   }

                   if (location == null)
                     continue;

                   printWriter.print(views[i].name() + " view location: ");

                   location.print(printWriter);

                   printWriter.println();
               }
         }
      }
      printWriter.println();
    }
  }

  /**
   * Returns the name of the function which contains this breakpoint.
   * @return the name of the function which contains this breakpoint.
   */

  public String getFunctionName()
  {
    return _epdcBkp.getEntryName();
  }

  /**
   * Returns the name of the module which contains this breakpoint.
   * @return the name of the module which contains this breakpoint.
   */

  public String getModuleName()
  {
    return _epdcBkp.getDLLName();
  }

  /**
   * Returns the name of the source/part which contains this breakpoint.
   * @return the name of the source/part which contains this breakpoint.
   */

  public String getPartName()
  {
    return _epdcBkp.getSourceName();
  }

  /**
   * Returns the name of the file which contains this breakpoint.
   * @return the name of the file which contains this breakpoint.
   */

  public String getFileName()
  {
    return _epdcBkp.getFileName();
  }

  int getEntryID()
  {
    return _epdcBkp.getEntryID();
  }

  /**
   * Returns the address of this breakpoint.
   * @return the address of this breakpoint.
   */

  public String getAddress()
  {
    return _epdcBkp.getAddress();
  }

  /**
   * Get the conditional expr associated with this breakpoint. Will return
   * null if there is no conditional expr associated with this breakpoint.
   */

  public String getExpression()
  {
    return _epdcBkp.getExprString();
  }

  /**
   * Returns the function in which the breakpoint is set.
   * @return the function in which the breakpoint is set.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */

  public Function getFunction()
  throws java.io.IOException
  {
    return getOwningProcess().getFunction(getEntryID(), true);
  }

  /**
   * This method is used by Java Serialization - it is called from
   * ObjectOutputStream.writeObject in order to serialize objects of this
   * class.
   * <p>IMPORTANT: This method must be kept in synch with the readObject
   * method for this class so that we only ever try to read in those fields
   * that were written out (and in the same order). This includes the
   * possibility that the writeObject method has chosen to write nothing
   * out in which case the readObject method must be prepared to read nothing
   * in.
   * <p>If we want to write out the entire object, we will call the default
   * method provided by Java - ObjectOutputStream.defaultWriteObject. This
   * default method writes out all non-static, non-transient fields.
   */

  private void writeObject(ObjectOutputStream stream)
  throws java.io.IOException
  {
    // See if we want to save all (non-transient) fields in the object
    // or only some of them:

    if (stream instanceof ModelObjectOutputStream)
    {
       int flags = ((ModelObjectOutputStream)stream).getSaveFlags();

       if ((flags & SaveRestoreFlags.ALL_OBJECTS) != 0)
          stream.defaultWriteObject();
    }
    else
       stream.defaultWriteObject();
  }

  /**
   * This method is used by Java Serialization - it is called from
   * ObjectInputStream.readObject in order to serialize objects of this
   * class.
   * <p>IMPORTANT: This method must be kept in synch with the writeObject
   * method for this class so that we only ever try to read in those fields
   * that were written out (and in the same order). This includes the
   * possibility that the writeObject method has chosen to write nothing
   * out in which case the readObject method must be prepared to read nothing
   * in.
   * <p>If we want to read in the entire object, we will call the default
   * method provided by Java - ObjectInputStream.defaultReadObject. This
   * default method reads in all non-static, non-transient fields.
   */

  private void readObject(ObjectInputStream stream)
  throws java.io.IOException,
         java.lang.ClassNotFoundException
  {
    // See if we need to read all (non-transient) fields in the object
    // or only some of them:

    if (stream instanceof ModelObjectInputStream)
    {
       int flags = ((ModelObjectInputStream)stream).getSaveFlags();

       if ((flags & SaveRestoreFlags.ALL_OBJECTS) != 0)
          stream.defaultReadObject();
    }
    else
       stream.defaultReadObject();
  }

  private EStdView[] _epdcLocations;
  private Part _part; // The part containing this bkp
}
