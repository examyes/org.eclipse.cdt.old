package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextMonitorExpr.java, java-epdc, eclipse-dev, 20011129
// Version 1.22.1.3 (last modified 11/29/01 14:15:29)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/**
 * ERepGetNextMonitorExpr change item
 */
public class ERepGetNextMonitorExpr extends EPDC_ChangeItem {

   /**
    * Create new ERepGetNextMonitorExpr object
    */
   public ERepGetNextMonitorExpr(short exprID, EStdTreeNode exprTree,
                                 EStdView exprContext, String stmtNum,
                                 String exprString, int exprDU,
                                 short stackEntryNum, int flags, int type,
                                 String moduleName, String partName,
                                 String viewFileName, int entryID)
   {
      _exprID = exprID;
      _exprTree = exprTree;
      _exprContext = exprContext;
      _stmtNum = new EStdString(stmtNum);
      _exprString = new EStdString(exprString);
      _exprDU = exprDU;
      _monitorStackEntryNum = stackEntryNum;
      _flags = (short)flags;
      _type = (short)type;
      _moduleName = new EStdString(moduleName);
      _partName = new EStdString(partName);
      _viewFileName = new EStdString(viewFileName);
      _monitorEntryID = entryID;
   }

   public ERepGetNextMonitorExpr(byte[] packetBuffer,
                                 DataInputStream dataInputStream)
   throws IOException
   {
     int offset;

     _exprID = dataInputStream.readShort();

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _exprTree = new EStdTreeNode(packetBuffer,
                         new OffsetDataInputStream(packetBuffer, offset));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _exprContext = new EStdView(packetBuffer,
                             new OffsetDataInputStream(packetBuffer, offset));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _stmtNum = new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _exprString = new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset));
     }

     _exprDU = dataInputStream.readInt();
     _parentMonitorExprID = dataInputStream.readShort();
     _flags = dataInputStream.readShort();
     _monitorStackEntryNum = dataInputStream.readShort();
     _type = dataInputStream.readShort();
     _monitorEntryID = dataInputStream.readInt();

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _moduleName = (new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset))
                       );
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _partName = (new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset))
                     );
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _viewFileName = (new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset))
                         );
     }
   }

   /**
    * Return length of fixed component
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Return length of variable component
    */
   protected int varLen() {
      int total = _exprTree.fixedLen() + _exprTree.varLen();
      total += _exprContext.fixedLen();

      total += totalBytes(_stmtNum);
      total += totalBytes(_exprString);
      total += totalBytes(_moduleName);
      total += totalBytes(_partName);
      total += totalBytes(_viewFileName);

      return total;
   }

   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = _fixed_length;

      int offset1 = baseOffset;
      int offset2 = offset1 + _exprTree.fixedLen() + _exprContext.fixedLen();

      ByteArrayOutputStream varBos1 = new ByteArrayOutputStream();
      ByteArrayOutputStream varBos2 = new ByteArrayOutputStream();
      DataOutputStream varDos1 = new DataOutputStream(varBos1);
      DataOutputStream varDos2 = new DataOutputStream(varBos2);

      writeShort(fixedData, _exprID);

      writeOffset(fixedData, offset1);
      _exprTree.setOffsetThisNode(offset1);
      _exprTree.toDataStreams(varDos1, varDos2, offset2);
      offset1 += _exprTree.fixedLen();
      offset2 += _exprTree.varLen();

      writeOffset(fixedData, offset1);
      offset1 += _exprContext.toDataStreams(varDos1, null, 0);

      offset2 += writeOffsetOrZero(fixedData, offset2, _stmtNum);
      offset2 += writeOffsetOrZero(fixedData, offset2, _exprString);

      if (_stmtNum != null)
         _stmtNum.output(varDos2);

      if (_exprString != null)
         _exprString.output(varDos2);

      writeInt(fixedData, _exprDU);
      writeShort(fixedData, _parentMonitorExprID);
      writeShort(fixedData, (short) _flags);
      writeShort(fixedData, _monitorStackEntryNum);
      writeShort(fixedData, (short) _type);
      writeInt(fixedData, _monitorEntryID);

      offset2 += writeOffsetOrZero(fixedData, offset2, _moduleName);
      offset2 += writeOffsetOrZero(fixedData, offset2, _partName);
      offset2 += writeOffsetOrZero(fixedData, offset2, _viewFileName);

      if (_moduleName != null)
         _moduleName.output(varDos2);

      if (_partName != null)
         _partName.output(varDos2);

      if (_viewFileName != null)
         _viewFileName.output(varDos2);

      varData.write(varBos1.toByteArray());
      varData.write(varBos2.toByteArray());

      total += offset2 - baseOffset;
      return total;
   }

   /**
    * Query for a new expression monitor
    */

   public boolean isNewMonitor()
   {
     return (_flags & EPDC.MonNew) != 0;
   }

   /**
    * Query for a deleted expression monitored
    */

   public boolean isDeleted()
   {
     return (_flags & EPDC.MonDeleted) != 0;
   }

   /**
    * Query for a disabled expression monitor
    */

   public boolean isDisabled()
   {
     return (_flags & EPDC.MonEnabled) == 0;
   }

   /**
    * Query for an enabled expression monitor
    */

   public boolean isEnabled()
   {
     return (_flags & EPDC.MonEnabled) != 0;
   }

   /**
    * Query for an expression monitor whose enablement has changed.
    * This means that an enablement will change when a monitored
    * expression has changes its state from enabled to disabled
    * and vice versa.
    */

   public boolean isEnablementChanged()
   {
     return (_flags & EPDC.MonEnablementChanged) != 0;
   }

   /**
    * Query for a monitored expression whose value(s) is(are) changed
    */

   public boolean isMonValueChanged()
   {
     return (_flags & EPDC.MonValuesChanged) != 0;
   }

   /**
    * Query for a monitored expression (or a member of a monitored
    * expression) whose structure has changed (expanded or collapsed)
    */

   public boolean isMonTreeStructChanged()
   {
     return (_flags & EPDC.MonTreeStructChanged) != 0;
   }

   /**
    * Return the EPDC generated id for this monitored expression
    */

   public short getEPDCAssignedID()
   {
      return _exprID;
   }

   /**
    * Return the expression tree created by the backend
    */
   public EStdTreeNode exprTree()
   {
     return _exprTree;
   }

   public void setExprTree(EStdTreeNode tree)
   {
     _exprTree = tree;
   }

   /**
    * Return the context of the monitored expression
    */
   public EStdView getContext()
   {
     return _exprContext;
   }

   /**
    * Return the type of the monitor (program, local, popup, private)
    */
   public short type()
   {
     return _type;
   }

   /**
    * Return the thread id the expression is associated with
    */
   public int threadID()
   {
     return _exprDU;
   }

   public String getExpressionString()
   {
     if (_exprString == null)
        return null;
     else
        return _exprString.string();
   }

   public String getModuleName()
   {
     if (_moduleName == null)
        return null;
     else
        return _moduleName.string();
   }

   public String getPartName()
   {
     if (_partName == null)
        return null;
     else
        return _partName.string();
   }

   public String getFileName()
   {
     if (_viewFileName == null)
        return null;
     else
        return _viewFileName.string();
   }

   public int getEntryID()
   {
     return _monitorEntryID;
   }

   /**
    * Return the statement number. If the debug engine does not support
    * statement breakpoint, or in case of no statement breakpoint this method
    * will return null.
    */
   public String getStmtNumber()
   {
     if (_stmtNum == null)
         return null;

     return _stmtNum.string();
   }

   // data fields
   private short _exprID;
   private EStdTreeNode _exprTree;
   private EStdView _exprContext;
   private EStdString _stmtNum;
   private EStdString _exprString;
   private int _exprDU;
   private short _parentMonitorExprID = (short) 0;
   private short _flags;
   private short _monitorStackEntryNum;
   private short _type;
   private int _monitorEntryID = 0;

   private transient int _monitorModuleOffset;
   private EStdString _moduleName;

   private transient int _monitorPartOffset;
   private EStdString _partName;

   private transient int _monitorFileOffset;
   private EStdString _viewFileName;


   private static final int _fixed_length = 46;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
