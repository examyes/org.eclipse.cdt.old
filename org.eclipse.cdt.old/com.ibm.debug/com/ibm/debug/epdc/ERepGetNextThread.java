package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextThread.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:23:35)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/** Class for program state change item */
public class ERepGetNextThread extends EPDC_ChangeItem {

   /** Create a new thread change item (EPDC 305/306) */
   public ERepGetNextThread(EPDC_EngineSession session, int partID, short threadState,
         short threadDebugState, int Priority, int threadSysID, int DU) {

      setEPDCEngineSession(session);

      _RepThrdState = threadState;
      _RepTDbgState = threadDebugState;
      _RepTPriority = Priority;
      _RepTID = threadSysID;
      _RepDU = DU;
      _NViews = session._viewInfo.length;
      _partID = (short)partID;

      _RepWherestop = new EStdView[_NViews];

      _attributes = new EStdAttribute[0];
   }

   /**
    * Construct a thread change item using the 307 format. For backward
    * compatibilty, this ctor will also try to assign values to the 306
    * fields (_RepTID, _RepTPriority, and _RepThrdState) based on the
    * values passed in the attributes array.
    *
    * @param session Contains the negotiated EPDC version.
    * @param threadDebugState Must be one of EPDC.StdThdThawed,
    *        EPDC.StdThdFrozen, or EPDC.StdThdTerminated.
    * @param DU Engine-assigned Id for this thread. Must be > 0.
    * @param whereStopped Must contain an entry for every view supported by
    *        the engine.
    * @param attributes The thread's attributes. Can be null.
    */

   public ERepGetNextThread(EPDC_EngineSession session,
                            short threadDebugState,
                            int DU,
                            EStdView[] whereStopped,
                            EStdAttribute[] attributes)
   {
     setEPDCEngineSession(session);

     _attributes = ((attributes == null) ? new EStdAttribute[0] : attributes);
     _RepTDbgState = threadDebugState;
     _RepDU = DU;
     _RepWherestop = whereStopped;
     _NViews = whereStopped.length;
     _partID = whereStopped[0].getPPID();

     // We'll try to set the value of the old fields even if the
     // EPDC version > 306 just in case some code is relying on the
     // "getter" methods for these fields still working:

     make306Compatible();
   }

   private void make306Compatible()
   {
     for (int i = 0; i < _attributes.length; i++)
     {
         EStdAttribute attribute = _attributes[i];

	      switch (attribute.getType())
	      {
	         case EPDC.ThreadNameOrTID:

   		      try
   		      {
   		         _RepTID = Integer.parseInt(attribute.getValue());
   		      }
   		      catch (NumberFormatException excp)
   		      {
   		      }

		         break;

   	      case EPDC.ThreadPriority:

         		try
         		{
         		  _RepTPriority = Integer.parseInt(attribute.getValue());
         		}
         		catch (NumberFormatException excp)
         		{
         		}

         		break;

	         case EPDC.ThreadState:

         		try
         		{
         		   _RepThrdState = Short.parseShort(attribute.getValue());
         		}
		         catch (NumberFormatException excp)
         		{
                  // It is already defaulted where it is declared
         		}

         		break;
	      }
      }
   }

   ERepGetNextThread(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
   throws IOException
   {
     setEPDCEngineSession(engineSession);

     int epdcVersion = engineSession._negotiatedEPDCVersion;
     short numAttrs = 0;
     int attrOffset = 0;

     if (epdcVersion < 307)
     {
        _RepThrdState = dataInputStream.readShort();
        _RepTDbgState = dataInputStream.readShort();
        _RepTPriority = dataInputStream.readInt();
        _RepTID =       dataInputStream.readInt();
        _RepDU =        dataInputStream.readInt();
        dataInputStream.readInt(); // Reserved field (used to be # of views)
     }
     else
     {
        numAttrs =      dataInputStream.readShort();
        _RepTDbgState = dataInputStream.readShort();
                        dataInputStream.readInt(); // reserved
                        dataInputStream.readInt(); // reserved
        _RepDU =        dataInputStream.readInt();

        if (numAttrs > 0) // Go get the attributes
        {
           _attributes = new EStdAttribute[numAttrs];

           for (int i = 0; i < numAttrs; i++)
              _attributes[i] = new EStdAttribute(packetBuffer, dataInputStream );

           make306Compatible();
        }
     }




     _RepWherestop = new EStdView[engineSession._viewInfo.length];

     for (int i = 0; i < engineSession._viewInfo.length; i++)
	     _RepWherestop[i] = new EStdView(packetBuffer, dataInputStream);


   }

   public short state()
   {
     return _RepThrdState;
   }

   public short debugState()
   {
     return _RepTDbgState;
   }

   public int priority()
   {
     return _RepTPriority;
   }

   public int systemAssignedID()
   {
     return _RepTID;
   }

   public int debugEngineAssignedID()
   {
     return _RepDU;
   }

   public boolean hasTerminated()
   {
     return _RepTDbgState == EPDC.StdThdTerminated;
   }

   /** Adds information about where the program stopped in a view.  One of these must be
     * supplied for each view.
     */
   public void setWhereStopped(int viewNo, int SrcFileIndex, int LineNum) {
      _RepWherestop[viewNo-1] = new EStdView(_partID, (short) viewNo,
               SrcFileIndex, LineNum);
   }

   // Feature 8100
   public void setPartialThreadInfo(int viewNo) {
      _RepWherestop[viewNo-1] = new EStdView((short) 0, (short) 0, 0, 0);
   }

   public EStdView[] whereStopped()
   {
     return _RepWherestop;
   }

   public EStdAttribute[] getAttributes()
   {
     return _attributes;
   }

   protected int toDataStreams(DataOutputStream fixedData,DataOutputStream varData,int baseOffset)
         throws IOException, BadEPDCCommandException
   {

      int epdcVersion = getEPDCVersion();

      if (epdcVersion < 307)
      {
         writeShort(fixedData, _RepThrdState);
         writeShort(fixedData, _RepTDbgState);
         writeInt(fixedData, _RepTPriority);
         writeInt(fixedData, _RepTID);
         writeInt(fixedData, _RepDU);
         writeInt(fixedData, 0);
      }
      else
      {
         writeShort(fixedData, (short)_attributes.length);
         writeShort(fixedData, _RepTDbgState);
         writeInt(fixedData, 0);
         writeInt(fixedData, 0);
         writeInt(fixedData, _RepDU);

         // Write out the attributes array

         for (int i = 0; i < _attributes.length; i++)
         {
            _attributes[i].toDataStreams(fixedData, varData, baseOffset);
            baseOffset += _attributes[i].varLen();
         }
      }

      // write out view information
      for (int i=0; i<_NViews; i++)
      {
         _RepWherestop[i].toDataStreams(fixedData, varData, baseOffset);

         baseOffset += _RepWherestop[i].varLen();
      }


      return fixedLen() + varLen();
   }

   /** Get the "fixed" length.  This includes the EStdView classes */
   protected int fixedLen() {

      int tot = 0;

      // length of this protocol is based on EPDC version

      // fixed length of the attributes if the EPDC version 307+
      if (getEPDCVersion() >=307)
      {
         tot = 16;   // base fixed length

         // now add in the attribute array
         for (int i = 0; i < _attributes.length; i++)
            tot += _attributes[i].fixedLen();
      }
      else
      {
         tot = 20;  // base fixed length for <307
      }


      // fixed length of the EStdView(s)
      for (int i=0; i<_NViews; i++)
         tot += _RepWherestop[i].fixedLen();


      return tot;
   }

   /** Get the variable length */
   protected int varLen() {
      int total = 0;

      if (getEPDCVersion() >= 307)
         for (int i = 0; i < _attributes.length; i++)
             total += _attributes[i].varLen();

      return total;
   }

   // Data fields
   private short _RepThrdState = EPDC.StdThdUnknown;
   private short _RepTDbgState;
   private int _RepTPriority = 0;
   private int _RepTID = 0;
   private int _RepDU;
   private int _NViews;
   private short _partID;

   private EStdView[] _RepWherestop;

   // Added for 307:

   private EStdAttribute[] _attributes;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
