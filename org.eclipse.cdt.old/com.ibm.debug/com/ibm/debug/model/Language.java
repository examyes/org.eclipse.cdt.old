package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Language.java, java-model, eclipse-dev, 20011128
// Version 1.18.1.2 (last modified 11/28/01 16:13:16)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;

import java.util.Vector;
import java.io.*;

/**
 * Class representing any possible programming language for a given debug
 * engine.
 */
public class Language extends DebugModelObject
{
  Language(ERepGetLanguages lang, DebugEngine engine)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "Creating Language : LanguageName(" + lang.getLanguageName() + ")");

    _id = lang.getLanguageID();
    _name = lang.getLanguageName();
    _debugEngine = engine;
  }

  /**
   * Restore the default reps for types in this language.
   */

  boolean restore(DebuggeeProcess targetProcess, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (_types == null || _types.size() == 0)
       return false;

    // Get the language with the same id as the one being restored:

    Language targetLanguage = targetProcess.debugEngine().getLanguageInfo(_id);

    // This shouldn't happen, but...

    if (targetLanguage == null)
       return false;

    // Tell every type in this Language object to restore itself into the
    // target Language object:

    for (int i = 0; i < _types.size(); i++)
    {
        Type type = (Type)_types.elementAt(i);

        if (type != null)
           type.restore(targetLanguage, sendReceiveControlFlags);
    }

    return true;
  }

  Type getType(int index)
  throws java.io.IOException
  {
    if (_types == null)
       getTypes();

    if (index < 0 || _types == null || index >= _types.size())
       return null;
    else
       return (Type)_types.elementAt(index);
  }

  /**
   * Send a request to get the list of types for
   * this programming language. This request must be sent synchronously
   * because the language id parameter that is needed to process the
   * types is not provided by the engine in the reply to this request.
   * @return 'an array of language types' if the request to get the list of
   * types was sent successful, and 'null' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public Type[] getTypes()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, this + ".getTypes()");

    if (_types == null)
    {
	if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_TypesNumGet,
				       _debugEngine.sendReceiveSynchronously))
	    return null;

	EReqTypesNumGet request = new EReqTypesNumGet(_id);

	if (Model.TRACE.EVT && Model.traceInfo())
	  Model.TRACE.evt(3, "Sending EPDC request Remote_TypesNumGet");

	if (!_debugEngine.processEPDCRequest(request,
					_debugEngine.sendReceiveSynchronously))
	    return null;

	ERepTypesNumGet reply = (ERepTypesNumGet)_debugEngine.getMostRecentReply();

	if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
	    return null;

	Vector epdcTypes = reply.types();

	if (epdcTypes == null || (_numberOfTypes = epdcTypes.size()) == 0)
	    return null;

	_types = new Vector(_numberOfTypes);

	// Create the vector of types

	for (int i = 0; i < _numberOfTypes; i++)
	{
	     ERepTypesAndRepsGetNext item = (ERepTypesAndRepsGetNext)epdcTypes.elementAt(i);

	     setVectorElementToObject(new Type(item, _debugEngine, this),
				      _types,
				      item.typeIndex());
	}
    }

    int sizeOfTypesVector = 0;

    if (_types == null || (sizeOfTypesVector = _types.size()) == 0)
       return null;

    // The _types vector is indexable by the type id and may contain nulls.
    // Instead of returning this to the caller, we'll create an array with no
    // nulls in it:

    Type[] types = new Type[_numberOfTypes];

    for (int i = 0, j = 0; i < sizeOfTypesVector; i++)
        if (_types.elementAt(i) != null)
           types[j++] = (Type)_types.elementAt(i);

    return types;
  }

/*
   boolean setDefaultRepresentation(int typeIndex, int repIndex)
   throws java.io.IOException
   {
     if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_RepForTypeSet,
                                            _debugEngine.sendReceiveSynchronously))
         return false;

     EReqRepForTypeSet request = new EReqRepForTypeSet(_id,
                                                       typeIndex,
                                                       repIndex);

     if (!_debugEngine.processEPDCRequest(request, _debugEngine.sendReceiveSynchronously))
         return false;

     EPDC_Reply reply = _debugEngine.getMostRecentReply();

     if (reply == null ||
         reply.getReturnCode() != EPDC.ExecRc_OK ||
         reply.getReplyCode() != EPDC.Remote_RepForTypeSet)
        return false;
     else
        return true;
   }
*/

   /**
    * Return the name of the current language
    */
   public String name()
   {
     return _name;
   }

   /**
    * Return the language id that corresponds to the list of languages
    * the current engine supports.
    */
   public int getLanguageID()
   {
     return _id;
   }

   /**
    * For each Type object owned by this Language object, set the default
    * representation to be the same as the pending default representation.
    * <p>Once changed, the representations will also be saved as the defaults for
    * the current debuggee and <i>optionally</i> (depending on the value of the
    * 'saveAsDebuggerDefaults' arg)
    * as the debugger-wide defaults for this language.
    * @param saveAsDebuggerDefaults This method always saves the given
    * representations as the default for the current debuggee, but in a addition
    * to that, this arg can be used to have them saved
    * as the debugger-wide defaults for this language as well.
    * @return 'true' if the pending default representation for all types in
    * this language were successfully committed. 'false' if at least one
    * pending default representation could not be committed.
    */

   public boolean commitPendingDefaultRepresentationChanges(boolean saveAsDebuggerDefaults)
   throws java.io.IOException
   {
     int sizeOfTypesVector = 0;

     if (_types == null || (sizeOfTypesVector = _types.size()) == 0)
        return false;

     boolean result = true; // Assume it'll work until we find otherwise.

     DebuggeeProcess process = _debugEngine.process();

     RestorableObjects restorableObjects = null;

     if (process != null)
        restorableObjects = process.getRestorableObjects();

     // If "autosave" is enabled for the process, turn it off for the duration
     // of the following loop. We'll explicitly do a save after the loop
     // instead.

     int saveFlags = 0;

     if (restorableObjects != null)
     {
        saveFlags = restorableObjects.getSaveFlags();

        if ((saveFlags & SaveRestoreFlags.AUTOSAVE) != 0)
           restorableObjects.setSaveFlags(saveFlags ^ SaveRestoreFlags.AUTOSAVE);
     }

     for (int i = 0; i < sizeOfTypesVector; i++)
     {
         Type type = (Type)_types.elementAt(i);

         if (type != null && !type.commitPendingDefaultRepresentation())
            result = false; // At least one could not be changed
     }

     _saveAndRestore = true;

     if (restorableObjects != null)
     {
        restorableObjects.setSaveFlags(saveFlags);

        if ((saveFlags & SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS) != 0)
           restorableObjects.save(true); // true == asynchronously
     }

     if (process != null &&
         saveAsDebuggerDefaults &&
         (restorableObjects = DebugEngine.findOrCreateEngineSpecificRestorableObjects(process)) != null)
        restorableObjects.resave(this);


     return result;
   }

   /**
    * For each Type object owned by this Language object, set the pending
    * default representation to be the same as the current default
    * representation.
    */

   public void cancelPendingDefaultRepresentationChanges()
   {
     int sizeOfTypesVector = 0;

     if (_types == null || (sizeOfTypesVector = _types.size()) == 0)
        return;

     for (int i = 0; i < sizeOfTypesVector; i++)
     {
         Type type = (Type)_types.elementAt(i);

         if (type != null)
            type.cancelPendingDefaultRepresentation();
     }
   }


   /**
    *   This is a getter method for the Identifier Parser for
    *   a particular language. If parser does not exist yet, it
    *   will be created.                             cmvc12329A
    */
   public IdentifierParser getIdentifierParser() throws java.io.IOException
   {
     // If parser have not been created yet... then create it!
     if( null == _parser )
     {
       // Parser differ based on the language...
       switch( getLanguageID() )
       {
         // All C family languages use C++ parser
         case EPDC.LANG_C:
         case EPDC.LANG_CPP:
         case EPDC.LANG_JAVA:
         case EPDC.LANG_FORTRAN:
            _parser = new IdentifierParserForCPP();
            break;

         // All 390 languages and COBOL use COBOL parser
         case EPDC.LANG_PLI:
         case EPDC.LANG_PLX86:
         case EPDC.LANG_COBOL:
            _parser = new IdentifierParserForCobol();
            break;

         // For RPG languages use ILE RPG for AS/400 parser
         case EPDC.LANG_RPG:
            _parser = new IdentifierParserForRPG(true);
            break;

         // For AS/400 OPM RPG use OPM RPG for AS/400 parser
         case EPDC.LANG_OPM_RPG:
            _parser = new IdentifierParserForOPMRPG(true);
            break;

         // For AS/400 CL use CL for AS/400 parser
         case EPDC.LANG_CL_400:
            _parser = new IdentifierParserForCL400();
            break;

         // By default, use C++ parser
         default:
            _parser = new IdentifierParserForCPP();
       } // end switch(languageID)
     } // end if(parser does not exist)

     return _parser;
   } // end getIdentifierParser()


   static char getLanguageMnemonic(byte languageID)
   {
     switch (languageID)
     {
       case EPDC.LANG_C:
            return 'c';

       case EPDC.LANG_CPP:
            return 'd';

       case EPDC.LANG_PLX86:
            return 'x';

       case EPDC.LANG_PLI:
            return 'p';

       case EPDC.LANG_RPG:
            return 'r';

       case EPDC.LANG_COBOL:
            return 'b';

       case EPDC.LANG_ALP_ASM:
            return 'a';

       case EPDC.LANG_OPM_RPG:
            return 'o';

       case EPDC.LANG_CL_400:
            return 'l';

       case EPDC.LANG_JAVA:
            return 'j';

       case EPDC.LANG_FORTRAN:
            return 'f';

     }

     return 0;
   }


   boolean getSaveAndRestore()
   {
     return _saveAndRestore;
   }

   void setSaveAndRestore(boolean saveAndRestore)
   {
     _saveAndRestore = saveAndRestore;
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
             // Note: Due to a defect in Java's serialization API (defect
             // # 4065313) we're saving more objects than we need to :
             // I wanted to use
             // ObjectOutputStream.replaceObject to write out null if the
             // Type object did not need to be saved. However, this does
             // not work because of the above mentioned bug. (Apparently
             // the bug has been fixed in JDK 1.2). See also the note in
             // ModelObjectOutputStream.java. Other workarounds are possible
             // in order to minimize the amount of info that gets written
             // out but may not be worth the trouble.

             if (_saveAndRestore)
                stream.writeObject(_types);
             else
                stream.writeObject((Vector)null);

             stream.writeInt(_id);
             // stream.writeBoolean(_saveAndRestore);
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
             _types = (Vector)stream.readObject();
             _id = stream.readInt();
             // _saveAndRestore = stream.readBoolean();
          }
       }
    }
    else
       stream.defaultReadObject();
  }

   private Vector _types;
   private int _numberOfTypes;
   private int _id;
   private boolean _saveAndRestore = false;
   private String _name;
   private DebugEngine _debugEngine;
   private IdentifierParser _parser = null; // cmvc12329A

}

