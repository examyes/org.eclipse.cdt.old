package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/MonitoredExpression.java, java-model, eclipse-dev, 20011129
// Version 1.29.1.3 (last modified 11/29/01 14:15:35)
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
 * Class representing monitored expressions in the model
 */
public class MonitoredExpression extends DebugModelObject
{

  MonitoredExpression(DebuggeeProcess owningProcess, ERepGetNextMonitorExpr epdcMonitoredExpr)
  {
     _owningProcess = owningProcess;

     change(epdcMonitoredExpr, true);

  }

  public MonitoredExpressionTreeNode getValue()
  {
    return _rootNode;
  }

  /**
   * Return the ID number of the thread
   */
  public int threadID()
  {
    return _epdcMonitoredExpr.threadID();
  }

  /**
   * Return the thread the monitored expression is associated with
   */
  public DebuggeeThread getThread()
  {
    return _owningProcess.getThread(_epdcMonitoredExpr.threadID());
  }

  /**
   * Return EPDC assigned id
   */

  short getMonitoredExpressionAssignedID()
  {
    return _epdcMonitoredExpr.getEPDCAssignedID();
  }

  /**
   * Return the process owning the monitored expression
   */

  public DebuggeeProcess getOwningProcess()
  {
    return _owningProcess;
  }

  public void addEventListener(MonitoredExpressionEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(MonitoredExpressionEventListener eventListener)
  {
    int index = _eventListeners.indexOf(eventListener);

    if (index != -1)
    {
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
    }
  }

  /**
    * Send a request to remove a monitored expression
    * @param sendReceiveControlFlags, this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to delete the monitored expression
    * was sent successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */

  public boolean remove(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionFree,
                                           sendReceiveControlFlags))
        return false;

    short id = _epdcMonitoredExpr.getEPDCAssignedID();

    EReqExpressionFree request = new EReqExpressionFree(id);

    if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
        return false;
    else
        return true;
  }

 /**
  * Notify listeners that Monitor has ended.
  */

  void prepareToDie()
  {
     DebugEngine debugEngine = getOwningProcess().debugEngine();

     int requestCode = debugEngine.getMostRecentReply().getReplyCode();

     debugEngine.getEventManager().addEvent(new MonitoredExpressionEndedEvent(
                                                this, this, requestCode),
                                            _eventListeners);
  }

  /**
   * Send a request to disable a monitored expression. Please note that this
   * does not mean that the monitored expression should be deleted.
   * @param sendReceiveControlFlags, this flag indicates the state in
   * which the request is to be sent.
   * @return 'true' if the request to disable the monitored expression
   * was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */

  public boolean disable(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionDisable,
                                           sendReceiveControlFlags))
        return false;

    // If the monitored expression is already disabled, cancel the request
    // so that the debug engine will not be busy any more.
    // If the current debug engine cannot disable a monitored expression
    // cancel the request.
    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getMonitorCapabilities().monitorEnableDisableSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionDisable);
            return false;
        }
    }

    if (isDisabled())
    {
        debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionDisable);
        return false;
    }

    short id = _epdcMonitoredExpr.getEPDCAssignedID();

    EReqExpressionDisable request = new EReqExpressionDisable(id);

    if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
        return false;
    else
        return true;
  }

  /**
   * Send a request to enable a monitored expression. All monitored expression
   * are enabled by default. This request will be sent when the monitored
   * expression has already been disabled.
   * @param sendReceiveControlFlags, this flag indicates the state in
   * which the request is to be sent.
   * @return 'true' if the request to enable the monitored expression
   * was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */

  public boolean enable(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionEnable,
                                           sendReceiveControlFlags))
        return false;

    // If the monitored expression is already enabled, cancel the request
    // so that the debug engine will not be busy any more.
    // If the current debug engine cannot enable a monitored expression
    // cancel the request.
    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getMonitorCapabilities().monitorEnableDisableSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionEnable);
            return false;
        }
    }

    if (isEnabled())
    {
        debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionEnable);
        return false;
    }

    short id = _epdcMonitoredExpr.getEPDCAssignedID();

    EReqExpressionEnable request = new EReqExpressionEnable(id);

    if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
        return false;
    else
        return true;
  }

  /**
   * Process all possible changes to a monitored expression. This will include
   * 1) enabling a monitored expression
   * 2) disabling a monitored expression
   * 3) modifying a monitored expression value
   * 4) modifying the tree structure of a monitored expression of a complex
   *    type
   * parameters: monitored expression change packet, and a flag to
   * distinguish between a new monitored expression and a changed one. In
   * case of a new one no change event will be fired.
   */

  void change(ERepGetNextMonitorExpr epdcMonitoredExpr, boolean isNew)
  {
     _epdcMonitoredExpr = epdcMonitoredExpr;

     EStdTreeNode exprTree = _epdcMonitoredExpr.exprTree();
     short type = exprTree.getTreeNodeData().getGenericNodeType();

     switch (type)
     {
       case EPDC.StdScalarNode:
            _rootNode = new ScalarMonitoredExpressionTreeNode(exprTree, this);
            break;
       case EPDC.StdStructNode:
            _rootNode = new StructMonitoredExpressionTreeNode(exprTree,this);
            break;
       case EPDC.StdClassNode:
            _rootNode = new ClassMonitoredExpressionTreeNode(exprTree, this);
            break;
       case EPDC.StdArrayNode:
            _rootNode = new ArrayMonitoredExpressionTreeNode(exprTree, this);
            break;
       case EPDC.StdPointerNode:
            _rootNode = new PointerMonitoredExpressionTreeNode(exprTree, this);
            break;
     }

     if (isNew)
         return;

     DebugEngine debugEngine = getOwningProcess().debugEngine();

     int requestCode = debugEngine.getMostRecentReply().getReplyCode();

     debugEngine.getEventManager().addEvent(new
                      MonitoredExpressionChangedEvent(this, this, requestCode),
                      _eventListeners);
  }

  /**
   * Query for a disabled monitored expression
   * @return 'true' if the monitored expression is disabled, and 'false'
   * otherwise.
   */

  public boolean isDisabled()
  {
    return _epdcMonitoredExpr.isDisabled();
  }

  /**
   * Query for an enabled expression monitor
   * @return 'true' if the monitored expression is enabled, and 'false'
   * otherwise.
   */

  public boolean isEnabled()
  {
    return _epdcMonitoredExpr.isEnabled();
  }

  /**
   * Return the monitored expression change packet
   */
  protected ERepGetNextMonitorExpr getEPDCMonitoredExpression()
  {
    return _epdcMonitoredExpr;
  }

  /**
   * Return the type of the monitor (program, local, popup, or private)
   */
  public short getMonitorType()
  {
    return _epdcMonitoredExpr.type();
  }

  /**
   * Get the function which contains the context for this monitored expr.
   * May return null.
   */

  public Function getFunction()
  throws java.io.IOException
  {
    return _owningProcess.getFunction(_epdcMonitoredExpr.getEntryID(), true);
  }

  /**
   * Return the location of the monitored expression. If the part or
   * the view of the monitored expression cannot be found, this function
   * will return null.
   */
  public Location getLocation()
  throws java.io.IOException
  {
/*
     EStdView context = _epdcMonitoredExpr.getContext();
     Part part = _owningProcess.getPart(context.getPPID());

     if (part == null)
         return null;

     View view = part.getView(context.getViewNo());
     if (view == null)
         return null;

     ViewFile file = view.file(context.getSrcFileIndex());
     if (file == null)
         return null;

     return new Location(file, context.getLineNum());
*/

     try
     {
       return new Location(_owningProcess, _epdcMonitoredExpr.getContext());
     }
     catch (LocationConstructionException excp)
     {
       return null;
     }
  }

  /**
   * Return the statement number (390 ONLY). If the debug engine does not
   * support statement breakpoints, or if the prefix area is null, the
   * statement number returned will be zero.
   */
  public int getStatementNumber()
  {
    String stmtField = _epdcMonitoredExpr.getStmtNumber();
    if (stmtField == null)
        return 0;

    try
    {
       return Integer.parseInt(stmtField);
    }
    catch (java.lang.NumberFormatException excp)
    {
      System.out.println("Number Format Exception");
      return 0;
    }
  }

  boolean restore(DebuggeeProcess targetProcess, int sendReceiveControlFlags)
  throws java.io.IOException
  {
/*
    byte attributes = EPDC.MonDefer;

    if (_epdcMonitoredExpr.isEnabled())
       attributes |= EPDC.MonEnable;
*/

    EStdView context = _epdcMonitoredExpr.getContext();

    // context.setPPID((short)0);

    // get the statement number if the engine supports that
    String stmtNumber = null;
    DebugEngine debugEngine = targetProcess.debugEngine();

    if (debugEngine.getEPDCVersion() > 305)
    {
        if (debugEngine.getCapabilities().getBreakpointCapabilities().statementBreakpointSupported())
        {
            stmtNumber = _epdcMonitoredExpr.getStmtNumber();
        }
    }

    return targetProcess.monitorExpression(new EStdView((short)0,
                                                        context.getViewNo(),
                                                        0,
                                                        context.getLineNum()),
                                           _epdcMonitoredExpr.threadID(),
                                           _epdcMonitoredExpr.getExpressionString(),
                                           // attributes,
                                           (byte)(EPDC.MonDefer | EPDC.MonEnable),
                                           _epdcMonitoredExpr.type(),
                                           _epdcMonitoredExpr.getModuleName(),
                                           _epdcMonitoredExpr.getPartName(),
                                           _epdcMonitoredExpr.getFileName(),
                                           stmtNumber,
                                           sendReceiveControlFlags);
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
       {
          // If we're saving the expr only so it can be restored (i.e.
          // as part of the profile information), then we don't want to
          // save the entire value tree:

          EStdTreeNode saveTree = _epdcMonitoredExpr.exprTree();
          _epdcMonitoredExpr.setExprTree(null);

          stream.writeObject(_epdcMonitoredExpr);

          _epdcMonitoredExpr.setExprTree(saveTree);
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
       {
          _epdcMonitoredExpr = (ERepGetNextMonitorExpr)stream.readObject();
       }
    }
    else
       stream.defaultReadObject();
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      super.print(printWriter);

      printWriter.println();

      MonitoredExpressionTreeNode node = getValue();
      node.print(printWriter);

      printWriter.print(" threadID: " + this.threadID());

      printWriter.println(" Monitor Type: " + this.getMonitorType());
    }
  }

  private transient Vector _eventListeners = new Vector();
  private ERepGetNextMonitorExpr _epdcMonitoredExpr;
  private MonitoredExpressionTreeNode _rootNode;
  private DebuggeeProcess _owningProcess;
}
