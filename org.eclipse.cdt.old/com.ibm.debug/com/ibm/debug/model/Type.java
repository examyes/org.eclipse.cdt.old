package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Type.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:13:17)
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
import java.util.Vector;

/**
 * Class representing different types allowed for each programming language.
 */
public class Type extends DebugModelObject
{
  Type(ERepTypesAndRepsGetNext epdcTypes, DebugEngine engine, Language owningLanguage)
  {
    _name = epdcTypes.typeName();
    _typeIndex = epdcTypes.typeIndex();
    _pendingDefaultRepIndex = _defaultRepIndex = epdcTypes.defaultRep();
    short[] reps = epdcTypes.repsForType();
    _representations = new Vector(reps.length);
    _owningLanguage = owningLanguage;
    _engine = engine;

    for (int i = 0; i < reps.length; i++)
	_representations.addElement(engine.getRepresentation(reps[i]-1));
  }

  /**
   * @see Language#commitPendingDefaultRepresentationChanges
   * @see Language#cancelPendingDefaultRepresentationChanges
   * @see Type#getPendingDefaultRep
   * @see Type#cancelPendingDefaultRepresentation
   * @see Type#commitPendingDefaultRepresentation
   */

  public boolean setPendingDefaultRepresentation(Representation rep)
  {
    int repIndex = -1;

    if (rep == null || _representations == null ||
        (repIndex = _representations.indexOf(rep)) == -1)
       return false;
    else
    {
       _pendingDefaultRepIndex = repIndex;
       return true;
    }
  }

  /**
   * @see Language#commitPendingDefaultRepresentationChanges
   * @see Language#cancelPendingDefaultRepresentationChanges
   * @see Type#commitPendingDefaultRepresentation
   */

  public void cancelPendingDefaultRepresentation()
  {
    _pendingDefaultRepIndex = _defaultRepIndex;
  }

  /**
   * @see Language#commitPendingDefaultRepresentationChanges
   * @see Language#cancelPendingDefaultRepresentationChanges
   * @see Type#cancelPendingDefaultRepresentation
   */

  public boolean commitPendingDefaultRepresentation()
  throws java.io.IOException
  {
    return setDefaultRepresentation(_pendingDefaultRepIndex);
  }

  /**
   * Send a request to set the default representation of a type.
   * This request must be sent synchronously because the language id that
   * is needed to process the correct type is not provided in the reply to
   * this request. Therefore, the reply to this request is verified right
   * after the request is sent.
   * @param rep the Representation object of a given type of a given language
   * @return 'true'if the request to change the default representation was
   * send successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */

  public boolean setDefaultRepresentation(Representation rep)
  throws java.io.IOException
  {
    int repIndex = -1;

    if (rep == null || _representations == null ||
        (repIndex = _representations.indexOf(rep)) == -1)
       return false;
    else
       return setDefaultRepresentation(repIndex);
  }

  boolean setDefaultRepresentation(int repIndex)
  throws java.io.IOException
  {
    // _owningLanguage.setRestoreFlag(true);

    if (repIndex == _defaultRepIndex)
       return true;

    int sendReceiveFlags = DebugEngine.sendReceiveCallerWillCompleteModelUpdates |
                           DebugEngine.sendReceiveSynchronously;

    if (!_engine.prepareForEPDCRequest(EPDC.Remote_RepForTypeSet,
					   sendReceiveFlags))
	return false;

    EReqRepForTypeSet request = new EReqRepForTypeSet(_owningLanguage.getLanguageID(),
						      _typeIndex,
						      repIndex);

    if (!_engine.processEPDCRequest(request, sendReceiveFlags))
	return false;

    EPDC_Reply reply = _engine.getMostRecentReply();

    if (reply == null ||
	reply.getReturnCode() != EPDC.ExecRc_OK ||
	reply.getReplyCode() != EPDC.Remote_RepForTypeSet)
    {
       _engine.setModelIsBeingUpdated(false);
       return false;
    }
    else
    {
       _defaultRepIndex = repIndex;
       _engine.setModelIsBeingUpdated(false);
       return true;
    }
  }

  /**
   * Return the default representation for this type
   */

  public Representation getDefaultRep()
  {
    return (Representation)_representations.elementAt(_defaultRepIndex);
  }

  /**
   * Return the pending default representation for this type
   */

  public Representation getPendingDefaultRep()
  {
    return (Representation)_representations.elementAt(_pendingDefaultRepIndex);
  }

  /**
   * Return the array of all representations for this type
   */

  public Representation[] getRepresentationsArray()
  {
    Representation[] reps = new Representation[_representations.size()];
    _representations.copyInto(reps);
    return reps;
  }

  /**
   * Return the type name
   */

  public String name()
  {
    return _name;
  }

  /**
   * Return the type index.
   */

  private int typeIndex()
  {
    return _typeIndex;
  }

  /**
   * Return the index of the default representation type
   */

  int defaultRepIndex()
  {
    return _defaultRepIndex;
  }

  int pendingDefaultRepIndex()
  {
    return _pendingDefaultRepIndex;
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
       else
       if ((flags & SaveRestoreFlags.RESTORABLE_OBJECTS) != 0)
       {
          if ((flags & SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS) != 0)
          {
             stream.writeInt(_typeIndex);
             stream.writeInt(_defaultRepIndex);
             stream.writeObject(_owningLanguage);
          }
       }
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
       else
       if ((flags & SaveRestoreFlags.RESTORABLE_OBJECTS) != 0)
       {
          if ((flags & SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS) != 0)
          {
             _typeIndex = stream.readInt();
             _defaultRepIndex = stream.readInt();
             _owningLanguage = (Language)stream.readObject();
          }
       }
    }
    else
       stream.defaultReadObject();
  }

  /**
   * Restore the default reps for this type.
   */

  boolean restore(Language targetLanguage, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    // Get the Type object with the same index as this Type object:

    Type targetType = targetLanguage.getType(_typeIndex);

    // This shouldn't happen, but...

    if (targetType == null)
       return false;
    else
    {
       // Tell the target type to use the same default representation as this
       // object. It will ignore the request if it's already using
       // that rep:

       return targetType.setDefaultRepresentation(_defaultRepIndex);
    }
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.println();

      printWriter.println("Type Index: " + typeIndex());
      printWriter.println("Type Name: " + name());
      printWriter.println("Number of Reps: " + _representations.size());
      printWriter.println("Defaults Rep Index: " + defaultRepIndex());
      printWriter.println("Rep Array-");
      for (int i = 0; i < _representations.size(); i++)
      {
           ((Representation)_representations.elementAt(i)).print(printWriter);
      }
    }
  }

  private Vector _representations;
  private String _name;
  private int _typeIndex;
  private int _defaultRepIndex;
  private int _pendingDefaultRepIndex;
  private Language _owningLanguage;
  private DebugEngine _engine;
}
