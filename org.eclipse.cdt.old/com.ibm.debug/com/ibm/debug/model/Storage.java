package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Storage.java, java-model, eclipse-dev, 20011128
// Version 1.30.1.2 (last modified 11/28/01 16:12:41)
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

public class Storage extends DebugModelObject
{
  Storage(DebuggeeProcess owningProcess, ERepGetNextMonitorStorageId epdcStorage)
  {
    _owningProcess = owningProcess;
    _id = epdcStorage.id();

    owningProcess.add(this);

    change(epdcStorage, true);
  }

  public void addEventListener(StorageEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(StorageEventListener eventListener)
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

  void change(ERepGetNextMonitorStorageId epdcStorage, boolean isNew)
  {
     _firstLineOffset = epdcStorage.getFirstLineOffset();

     _lastLineOffset = epdcStorage.getLastLineOffset();

     _address = epdcStorage.getAddress();

     _numberOfUnitsPerLine = epdcStorage.unitCount();

     _storageStyle = epdcStorage.getUnitStyle();

     _expression = epdcStorage.getExpression();

     _isEnabled = epdcStorage.isEnabled();

     _exprIsEnabled = epdcStorage.exprIsEnabled();

     ERepGetNextMonitorStorageLine[] lines = epdcStorage.getLines();

     // If the style and/or address has changed, we're expecting the engine
     // to resend the entire storage monitor. That's why we're throwing
     // away whatever storage lines we've currently got and rebuilding from
     // scratch:

     if (!isNew && (epdcStorage.styleChanged() || epdcStorage.addressChanged()))
     {
        _storageLines = new Vector(lines.length);

        DebugEngine debugEngine = _owningProcess.debugEngine();

        int requestCode = debugEngine.getMostRecentReply().getReplyCode();

        debugEngine.getEventManager().addEvent(new StorageChangedEvent(this,
                                                                       this,
                                                                       requestCode
                                                                      ),
                                               _eventListeners
                                              );
     }
     else
       if (_storageLines == null && lines != null)
          _storageLines = new Vector(lines.length);

     if (lines == null)
        return;

     for (int i = 0; i < lines.length; i++)
     {
         int lineNumber = lines[i].getLineNumber();

         if (lineNumber >= _storageLines.size() || _storageLines.elementAt(lineNumber) == null)
         {
            StorageLine line = new StorageLine(this, lines[i], _firstLineOffset + lineNumber);

            setVectorElementToObject(line, _storageLines, lineNumber);
         }
         else
           ((StorageLine)_storageLines.elementAt(lineNumber)).change(lines[i], false);
     }
  }

  public boolean isEnabled()
  {
    return _isEnabled;
  }

  public boolean expressionIsEnabled()
  {
    return _exprIsEnabled;
  }

  /**
   * Returns 'true' if the storage monitor was sucessfully enabled or
   * was already enabled, otherwise, returns false.
   */

  public boolean enable()
  throws java.io.IOException
  {
    if (!_owningProcess.debugEngine().getCapabilities().
                                      getStorageCapabilities().
                                      storageEnableDisableSupported())
    {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(2, "Engine does not support storage monitor enable/disable");

       return false;
    }

    if (!_isEnabled)
       return changeEnablement(true, _exprIsEnabled);
    else
       return true;
  }

  /**
   * Returns 'true' if the storage monitor was sucessfully disabled or
   * was already disabled, otherwise, returns false.
   */

  public boolean disable()
  throws java.io.IOException
  {
    if (!_owningProcess.debugEngine().getCapabilities().
                                      getStorageCapabilities().
                                      storageEnableDisableSupported())
    {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(2, "Engine does not support storage monitor enable/disable");

       return false;
    }

    if (_isEnabled)
       return changeEnablement(false, _exprIsEnabled);
    else
       return true;
  }

  /**
   * Returns 'true' if the expression was sucessfully enabled or
   * was already enabled, otherwise, returns false.
   */

  public boolean enableExpression()
  throws java.io.IOException
  {
    if (!_owningProcess.debugEngine().getCapabilities().
                                      getStorageCapabilities().
                                      storageExprEnableDisableSupported())
    {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(2, "Engine does not support storage monitor expr enable/disable");

       return false;
    }

    if (!_exprIsEnabled)
       return changeEnablement(_isEnabled, true);
    else
       return true;
  }

  /**
   * Returns 'true' if the expression was sucessfully disabled or
   * was already disabled, otherwise, returns false.
   */

  public boolean disableExpression()
  throws java.io.IOException
  {
    if (!_owningProcess.debugEngine().getCapabilities().
                                      getStorageCapabilities().
                                      storageExprEnableDisableSupported())
    {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(2, "Engine does not support storage monitor expr enable/disable");

       return false;
    }

    if (_exprIsEnabled)
       return changeEnablement(_isEnabled, false);
    else
       return true;
  }

  private boolean changeEnablement(boolean enable, boolean enableExpression)
  throws java.io.IOException
  {
     DebugEngine engine = _owningProcess.debugEngine();

     int requestCode = EPDC.Remote_StorageEnablementSet;

     if (!engine.prepareForEPDCRequest(requestCode, DebugEngine.sendReceiveSynchronously) ||
         !engine.processEPDCRequest(new EReqStorageEnablementSet(_id,
                                                                 enable,
                                                                 enableExpression),
                                    DebugEngine.sendReceiveSynchronously)
                                   )
        return false;

     if (engine.getMostRecentReply().getReturnCode() != EPDC.ExecRc_OK)
        return false;
     else
     {
        _isEnabled = enable;
        _exprIsEnabled = enableExpression;
        return true;
     }
  }

  /**
   * Update the debuggee's storage with the given value.
   * Note that in most cases, StorageColumn.update should be used to update
   * storage. This method is provided only for those situations in which
   * StorageColumn.update is not appropriate for some reason.
   */

  public boolean update(String value,
                 int numberOfUnits,
                 int unitFieldIndex,
                 int lineOffset,
                 int columnOffset,
                 int sendReceiveControlFlags)
  throws java.io.IOException
  {
     DebugEngine engine = _owningProcess.debugEngine();

     int requestCode = EPDC.Remote_StorageUpdate;

     if (!engine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
        return false;

     return engine.processEPDCRequest(new EReqStorageUpdate(_id,
							    _address,
                                                            lineOffset,
                                                            columnOffset,
                                                            unitFieldIndex,
                                                            numberOfUnits,
                                                            value),
                                            sendReceiveControlFlags);
  }

  public int getNumberOfUnitsPerLine()
  {
    return _numberOfUnitsPerLine;
  }

  public DebuggeeProcess getOwningProcess()
  {
    return _owningProcess;
  }

  /**
   * Determine the style in which storage in this storage monitor has been formatted
   * by the debug engine.
   */

  public StorageStyle getStorageStyle()
  {
    return StorageStyle.getStorageStyle(_storageStyle);
  }

  /**
   * Get the base address of this monitored storage.
   */

  public String getAddress()
  {
    return _address;
  }

  /**
   * Obtain a Vector of StorageLine objects which contain pre-formatted
   * strings showing storage contents which can be displayed to the user.
   * @see StorageLine
   */

  public Vector getStorageLines()
  {
    return _storageLines;
  }

   /**
    * Change the style in which this storage is being displayed. If successful,
    * this request will result in a StorageChangedEvent being fired.
    * @param storageStyle This argument tells the debug engine
    * several things about how the FE wants the storage to be formatted:
    * <ul>
    * <li>The unit size i.e. how many bytes of storage make up a single unit.
    * Examples: 8 bits per unit, 16 bits per unit, etc.
    * <li>The unit type i.e. what type should the engine interpret each unit
    * of storage as? Examples: int, float, etc.
    * <li>How to format the value. Examples: decimal, hex, etc.
    * </ul>
    * @param numberOfUnitsPerLine Tells the debug engine how many storage units
    * should be displayed per line.
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendRequestSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return 'true' if the request was successfully sent to
    * the debug engine, 'false' otherwise. Note that a return value of 'true'
    * does not imply that the debug engine was able to honour the request but
    * rather simply that the request was successfully sent to the debug engine.
    * Whether or not the request was actually performed by the debug engine will
    * be indicated via the event listener mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    * @see StorageChangedEvent
    */

  public boolean setStyle(StorageStyle storageStyle,
                          int numberOfUnitsPerLine,
                          int sendReceiveControlFlags)
   throws java.io.IOException
  {
     DebugEngine engine = _owningProcess.debugEngine();

     int requestCode = EPDC.Remote_StorageStyleSet;

     if (!engine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
        return false;

     return engine.processEPDCRequest(new EReqStorageStyleSet(_id,
                                                              _address,
                                                              storageStyle.getStyleIdentifier(),
                                                              numberOfUnitsPerLine),
                                            sendReceiveControlFlags);
  }

  /**
   * Tell the debug engine to use a different base address for this storage
   * monitor. If successful, this request will result in a StorageChangedEvent
   * being fired.
   * @param addressExpression A string containing an arbitrary expression. This
   * expression will be evaluated and the resulting value will be used as the
   * base address of the storage to be monitored.
   * <p>
   * @param evaluationContext A Location object which determines the context
   * in which the addressExpression is to be evaluated. This affects various
   * aspects of the evaluation, including a) which expr evaluator will be
   * used and b) the starting scope for name lookup.
   * <p>
   * Note that if the expr needs no evaluation (e.g. a literal value), then
   * this argument is optional - null can be passed instead.
   * <p>
   * @param evaluationThread A DebuggeeThread object which determines the context
   * in which the addressExpression is to be evaluated. If the expr contains
   * references to automatic (i.e. stack-allocated) variables, this arg will
   * be used to determine which thread's stack will be used to retrieve
   * values for those variables.
   * <p>
   * Note that if the expr needs no evaluation (e.g. a literal value), then
   * this argument is optional - null can be passed instead.
   * <p>
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendRequestSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return 'true' if the request was successfully sent to
    * the debug engine, 'false' otherwise. Note that a return value of 'true'
    * does not imply that the debug engine was able to honour the request but
    * rather simply that the request was successfully sent to the debug engine.
    * Whether or not the request was actually performed by the debug engine will
    * be indicated via the event listener mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
   * @see StorageChangedEvent
   * @see Storage#setAddressAndRange
   */

  public boolean setAddress(String addressExpression,
			    Location evaluationContext,
			    DebuggeeThread evaluationThread,
			    int sendReceiveControlFlags)
   throws java.io.IOException
  {
    return setAddressAndRange(addressExpression,
                              evaluationContext,
                              evaluationThread,
                              _firstLineOffset,
                              _lastLineOffset,
                              sendReceiveControlFlags);
  }

  /**
   * Tell the debug engine to use a different base address for this storage
   * monitor as well as a new range of lines relative to that base address.
   * If successful, this request will result in a StorageChangedEvent
   * being fired.
   * @param addressExpression A string containing an arbitrary expression. This
   * expression will be evaluated and the resulting value will be used as the
   * base address of the storage to be monitored.
   * <p>
   * @param evaluationContext A Location object which determines the context
   * in which the addressExpression is to be evaluated. This affects various
   * aspects of the evaluation, including a) which expr evaluator will be
   * used and b) the starting scope for name lookup.
   * <p>
   * Note that if the expr needs no evaluation (e.g. a literal value), then
   * this argument is optional - null can be passed instead.
   * <p>
   * @param evaluationThread A DebuggeeThread object which determines the context
   * in which the addressExpression is to be evaluated. If the expr contains
   * references to automatic (i.e. stack-allocated) variables, this arg will
   * be used to determine which thread's stack will be used to retrieve
   * values for those variables.
   * <p>
   * Note that if the expr needs no evaluation (e.g. a literal value), then
   * this argument is optional - null can be passed instead.
   * <p>
   * @param offsetToFirstLine The monitored storage will be returned from the
   * debug engine as a series of storage <i>lines</i>. This argument tells
   * the debug engine what the first line of monitored storage should be,
   * relative to the base address for the monitor. A negative number may be
   * given in which case the first line of storage will be that many lines
   * <i>before</i> the line containing the base address.
   * <p>
   * @param offsetToLastLine This argument tells the debug engine what the
   * last line of monitored storage should be, relative to the base address for the monitor.
   * <p>
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendRequestSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return 'true' if the request was successfully sent to
    * the debug engine, 'false' otherwise. Note that a return value of 'true'
    * does not imply that the debug engine was able to honour the request but
    * rather simply that the request was successfully sent to the debug engine.
    * Whether or not the request was actually performed by the debug engine will
    * be indicated via the event listener mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
   * @see StorageChangedEvent
   * @see Storage#setAddress
   */

  public boolean setAddressAndRange(String addressExpression,
                                    Location evaluationContext,
                                    DebuggeeThread evaluationThread,
                                    int offsetToFirstLine,
                                    int offsetToLastLine,
                                    int sendReceiveControlFlags)
   throws java.io.IOException
  {
     EStdExpression2 epdcExpression;

     epdcExpression = new EStdExpression2(evaluationContext == null ?
					  null :
					  evaluationContext.getEStdView(),

					  addressExpression,

					  evaluationThread == null ?
					  0 :
					  evaluationThread.debugEngineAssignedID(),

					  0);

     return setAddressAndRange(epdcExpression,
                               offsetToFirstLine,
                               offsetToLastLine,
                               sendReceiveControlFlags);
  }

  private boolean setAddressAndRange(EStdExpression2 epdcExpression,
                                    int offsetToFirstLine,
                                    int offsetToLastLine,
                                    int sendReceiveControlFlags)
   throws java.io.IOException
  {
     DebugEngine engine = _owningProcess.debugEngine();

     int requestCode = EPDC.Remote_StorageRangeSet2;

     if (!engine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
        return false;

     return engine.processEPDCRequest(new EReqStorageRangeSet2(_id,
                                                              epdcExpression,
                                                              offsetToFirstLine,
                                                              offsetToLastLine,
                                                              _isEnabled,
                                                              _exprIsEnabled),
                                      sendReceiveControlFlags);
  }

  /**
   * Get a new range of storage lines without changing the expr which
   * determines the base address of the storage being monitored. If this
   * storage monitor does not contain an expr, then the base address will
   * be used as the expr.
   */

  public boolean setRange(int offsetToFirstLine,
                          int offsetToLastLine,
                          int sendReceiveControlFlags)
   throws java.io.IOException
  {
    return setAddressAndRange((_expression == null ||
                               _expression.getExpressionString() == null) ?
                              new EStdExpression2(null, _address, 0, 0) :
                              _expression,
                              offsetToFirstLine,
                              offsetToLastLine,
                              sendReceiveControlFlags);
  }

  /**
   * Tell the debug engine to stop monitoring this piece of storage.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendRequestSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the remove request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the debug engine was able to honour the request but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the storage monitor was actually deleted by the debug engine will
   * be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean remove(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (debugEngine.prepareForEPDCRequest(EPDC.Remote_StorageFree,
                                           sendReceiveControlFlags) &&
        debugEngine.processEPDCRequest(new EReqStorageFree(_id),
                                           sendReceiveControlFlags))
       return true;
    else
       return false;
  }

  // A call to 'remove' (if successful) will ultimately end up in a call
  // to 'prepareToDie'. 'prepareToDie' gives the Storage object a chance
  // to say goodbye to its event listeners...

  void prepareToDie()
  {
    DebugEngine debugEngine = _owningProcess.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new StorageDeletedEvent(this,
                                                                this,
                                                                requestCode
                                                               ),
                                           _eventListeners
                                          );
  }

  short id()
  {
    return _id;
  }

  /**
   * Get the line offset of the first line of storage in this storage
   * monitor relative to the line containing the base address. A negative
   * number, e.g. -X, means that there are X lines of storage in this storage
   * monitor which precede the line containing the base address. A value of
   * 0 means that the line containing the base address <i>is</i> the first
   * line in the storage monitor. A positive number indicates that the
   * storage monitor does not contain the base address.
   * @see Storage#getLastLineOffset
   * @see StorageLine#getLineOffset
   */

  public int getFirstLineOffset()
  {
    return _firstLineOffset;
  }

  /**
   * Get the line offset of the last line of storage in this storage
   * monitor relative to the line containing the base address. A positive
   * number, e.g. X, means that there are X lines of storage in this storage
   * monitor <i>after</i> the line containing the base address. A value of
   * 0 means that the line containing the base address <i>is</i> the last
   * line in the storage monitor. A negative number indicates that the
   * storage monitor does not contain the base address.
   * @see Storage#getFirstLineOffset
   * @see StorageLine#getLineOffset
   */

  public int getLastLineOffset()
  {
    return _lastLineOffset;
  }

  /**
   * Get the expression string that was evaluated to monitor this storage.
   */

  public String getExpression()
  {
     if (_expression == null)
        return null;
     else
        return _expression.getExpressionString();
  }

  /**
   * Get the thread for the expression used to monitor this storage.
   */
  public DebuggeeThread getExpressionThread()
  {
    if (_expression == null)
        return null;

    return _owningProcess.thread(_expression.getExprDU());
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
          stream.writeInt(_numberOfUnitsPerLine);
          stream.writeShort(_storageStyle);
          stream.writeInt(_firstLineOffset);
          stream.writeInt(_lastLineOffset);
          stream.writeObject(_expression);
          stream.writeBoolean(_isEnabled);
          stream.writeBoolean(_exprIsEnabled);
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
          _numberOfUnitsPerLine = stream.readInt();
          _storageStyle = stream.readShort();
          _firstLineOffset = stream.readInt();
          _lastLineOffset = stream.readInt();
          _expression = (EStdExpression2)stream.readObject();
          _isEnabled = stream.readBoolean();
          _exprIsEnabled = stream.readBoolean();
       }
    }
    else
       stream.defaultReadObject();
  }

  boolean restore(DebuggeeProcess targetProcess, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return targetProcess.monitorStorage(_expression,
					_firstLineOffset,
					_lastLineOffset,
					_storageStyle,
					_numberOfUnitsPerLine,
					_isEnabled,
					_exprIsEnabled,
					null, // client
					null, // privileged listener
					sendReceiveControlFlags);
  }

  public void print(PrintWriter printWriter)
  {
    printWriter.print("Addr=" + _address + " ");
    printWriter.print("Units per line=" + _numberOfUnitsPerLine + " ");
    printWriter.print("Is enabled=" + _isEnabled);
    printWriter.print("   Expr is enabled=" + _exprIsEnabled);
    DebuggeeThread DU = getExpressionThread();
    if (DU != null)
        printWriter.println("   Expr thread id=" + DU.debugEngineAssignedID());

    getStorageStyle().print(printWriter);
    printWriter.println();

    if (_storageLines == null)
       return;

    for (int i=0; i < _storageLines.size(); i++)
        if (_storageLines.elementAt(i) != null)
           ((StorageLine)_storageLines.elementAt(i)).print(printWriter);
  }

  public void cleanup()
  {
    if (_eventListeners != null)
       _eventListeners.removeAllElements();
    _owningProcess = null;
    if (_storageLines != null)
    {
       int cnt = _storageLines.size();
       for (int i = 0; i < cnt; i++)
       {
          StorageLine sl = (StorageLine)_storageLines.elementAt(i);
          if (sl != null)
             sl.cleanup();
       }
       _storageLines.removeAllElements();
       _storageLines = null;
    }
    _address = null;
    _expression = null;
  }

  private transient Vector _eventListeners = new Vector();
  private DebuggeeProcess _owningProcess;
  private short _id;
  private Vector _storageLines;
  private int _numberOfUnitsPerLine;
  private String _address;
  private short _storageStyle;
  private int _firstLineOffset;
  private int _lastLineOffset;
  private EStdExpression2 _expression;
  private boolean _isEnabled;
  private boolean _exprIsEnabled;
}
