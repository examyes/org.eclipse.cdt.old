package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebugModelObject.java, java-model, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:11:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;

abstract public class DebugModelObject implements java.io.Serializable
{
  DebugModelObject(Client owningClient)
  {
    _owningClient = owningClient;
  }

  DebugModelObject()
  {
  }

  final void setHasBeenDeleted()
  {
    _hasBeenDeleted = true;

    tellChildrenThatOwnerHasBeenDeleted();
  }

  final void setOwnerHasBeenDeleted()
  {
    _ownerHasBeenDeleted = true;

    // Calling this method here implies that we consider an "owner" to include
    // both direct as well as indirect owners:

    tellChildrenThatOwnerHasBeenDeleted();
  }

  /** This method is intended to be overridden in each subclass since only the
   *  subclasses know who their children are. A default implementation of
   *  this method has been provided instead of making the method abstract
   *  so that classes which don't have any children aren't required to
   *  implement the method. The default implementation does nothing.
   */

  void tellChildrenThatOwnerHasBeenDeleted()
  {
  }

  public boolean hasBeenDeleted()
  {
    return _hasBeenDeleted;
  }

  public boolean ownerHasBeenDeleted()
  {
    return _ownerHasBeenDeleted;
  }

  public boolean thisObjectOrItsOwnerHasBeenDeleted()
  {
    return _hasBeenDeleted || _ownerHasBeenDeleted;
  }

  void setIsPrivate(boolean isPrivate)
  {
    _isPrivate = isPrivate;
  }

  public boolean isPrivate()
  {
    return _isPrivate;
  }

  static void setVectorElementToObject(Object object, Vector vector, int index)
  {
    if (index > vector.size() - 1)
       vector.setSize(index + 1);

    try
    {
      vector.setElementAt(object, index);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

  static void addObjectToHashtable(Object object, Hashtable hashtable, Object key)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(2, "In DebugModelObject.addObjectToHashtable");

    if (hashtable == null || object == null || key == null)
       return;

    Vector vectorOfObjectsWithKey = (Vector)hashtable.get(key);

    if (vectorOfObjectsWithKey == null)
    {
       if (Model.TRACE.DBG && Model.traceInfo())
          Model.TRACE.dbg(3, "No objects with this key yet - " +
                             " creating Vector and adding it to the hashtable");

       vectorOfObjectsWithKey = new Vector();
       hashtable.put(key, vectorOfObjectsWithKey);
    }

    // Add the object to the Vector:

    vectorOfObjectsWithKey.addElement(object);
  }

  static void removeObjectFromHashtable(Object object, Hashtable hashtable, Object key)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(2, "In DebugModelObject.removeObjectFromHashtable");

    if (hashtable == null || object == null || key == null)
       return;

    Vector vectorOfObjectsWithKey = (Vector)hashtable.get(key);

    if (vectorOfObjectsWithKey == null)
    {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(2, "WARNING: There are no objects with this key " +
                             "in the hashtable");
       return;
    }

    // Remove the object from the Vector:

    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, "Removing object from Vector in hashtable");

    vectorOfObjectsWithKey.removeElement(object);

    if (vectorOfObjectsWithKey.isEmpty())
    {
       if (Model.TRACE.DBG && Model.traceInfo())
          Model.TRACE.dbg(2, "There are no more objects with this key -" +
                             "removing the Vector from the hashtable");
       hashtable.remove(key);
    }
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.print("hasBeenDeleted=" + (_hasBeenDeleted ? " true " : " false "));
       printWriter.print("ownerHasBeenDeleted=" + (_ownerHasBeenDeleted ? " true " : " false "));
    }
  }

  private boolean _hasBeenDeleted = false;
  private boolean _ownerHasBeenDeleted = false;
  private boolean _isPrivate = false;
  private Client  _owningClient;
}
