package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebugEngine.java, java-model, eclipse-dev, 20011129
// Version 1.161.2.4 (last modified 11/29/01 14:15:33)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;
import com.ibm.debug.connection.*;
import com.ibm.debug.util.*;
import java.net.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.LineSeparator;
import org.w3c.dom.traversal.*;
import org.xml.sax.InputSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.apache.xerces.dom.DocumentImpl;

/**
 * <br>This class represents a debug engine that can be used to debug
 * a process. Examples of actual debug engines are PICL (OS/2, Win32, and AIX),
 * Wiley (AS/400), and Debug Tool (S/390).
 * <p><u><b>Obtaining and Using a DebugEngine Object</b></u>
 * <p>
 * These are the steps that are typically followed to obtain and use a
 * DebugEngine object:
 * <ol>
 * <li>Obtain a Host object which represents the host on which the actual debug
 * engine is (or will be) running. To obtain the LocalHost object, use
 * Model.getLocalHost. To obtain a RemoteHost object, use Model.getHost.
 * <li>Load the actual debug engine. This can be done by calling
 * Host.loadEngine. Note that this step is optional since the engine may already
 * have been loaded by the user.
 * <li>See if there is a DebugEngine object already in the Model which can
 * be used to represent the actual debug engine. See Host.reuseDebugEngine.
 * If there is no DebugEngine object which can be reused, create a DebugEngine
 * object by calling the Host.getNewEngine method.
 * <li>Connect to the debug engine by calling DebugEngine.connect.
 * </ol>
 * <p><u><b>Using a DebugEngine Object to Debug a Process</b></u>
 * <p>Once a DebugEngine object has been constructed and a connection has been
 * been established between it and an actual debug engine, the
 * DebugEngine object can finally be used to
 * start debugging a program. Methods are available
 * to initialize the debug engine, load a program for debugging, attach to an
 * existing process, etc.
 * <p>In addition, one can register event listeners with the DebugEngine object in
 * in order to receive notification when significant events occur within
 * the debug engine. For example, listeners will be notified whenever the
 * debug engine begins debugging a new process. Listeners must implement the
 * DebugEngineEventListener interface and must be added to the DebugEngine
 * object via the DebugEngine.addEventListener(DebugEngineEventListener) method. More than
 * one listener may be registered with a given DebugEngine object. Whenever
 * an event occurs for which there is a corresponding method in the
 * DebugEngineEventListener interface, the DebugEngine object will call that
 * method for all registered listeners.
 * <p>Note that there are some important <b>restrictions
 * and caveats</b> associated with using a DebugEngine object and/or any of the
 * objects contained within a DebugEngine object. These restrictions exist mostly
 * because of the nature of the EPDC protocol as well as the debug engines which use
 * that protocol.
 * <p>The first restriction is that once we have sent a request to a
 * debug engine, we cannot send another request until the first one has been
 * completely processed and a reply has been received from the debug engine.
 * In the Model, there is a method on the DebugEngine class called isBusy()
 * which will return true if we have sent a request to the debug engine but
 * have not yet received a reply.
 * The Model will not send additional requests to the
 * debug engine if it is busy - any method in the Model that would require sending a
 * request to the debug engine will simply return a value which indicates
 * failure (e.g. null or false) if that method is called while the debug engine is busy.
 * <p>The second restriction is similar to the first: New requests cannot be
 * sent to the debug engine while the Model is being updated. For the most
 * part, Model updates are done immediately after receiving a reply from the
 * debug engine - the Model is updated to reflect the information contained in
 * that reply. There is a method in the Model called DebugEngine.modelIsBeingUpdated
 * which can be called to query whether or not the Model is in the process of
 * being updated.
 * <p>Also note that, in addition to the above methods which allow client code to
 * query the current state of the debug engine, there is also a method in the
 * DebugEngineEventListener interface (modelStateChanged) which will be called to inform listeners
 * when the debug engine becomes busy, when the debug engine is no longer busy, when
 * Model updates are beginning, when the Model updates are ending, etc. See
 * the description of that method for details.
 * <p>Finally, please note the following caveat regarding querying the Model
 * while it is being updated: It is possible to query the Model while it is
 * being updated (as long as doing so does not require sending a request to the
 * debug engine) but there is no guarantee that the information returned will
 * be correct or complete. Because of this, querying the Model while it is
 * being updated is discouraged - it is best to wait until the Model updates
 * are complete before querying the Model.
 * <p>Most of the above restrictions and caveats can be avoided by obtaining
 * a lock on the Model before querying it or doing something that might
 * cause it to be updated. See the ModelUpdateLock class for details.
 * <p><u><b>Objects Contained Within a DebugEngine Object</b></u>
 * <p>The most significant object owned by a DebugEngine object is the
 * DebuggeeProcess object which represents the process currently being debugged
 * by this debug engine. Currently, a DebugEngine object can contain only one
 * such process object at a time.
 * <p>One can also query a DebugEngine to find out what its capabilities are.
 * @see Host
 * @see DebugEngineEventListener
 * @see DebuggeeProcess
 */

public class DebugEngine extends DebugModelObject
{
   /**
    * Construct a DebugEngine object owned by the given Host.
    * @param host The Host where this debug engine resides.
    */

   DebugEngine(Host host)
   {
     _host = host;

     // Add this debug engine to the list maintained by the host:

     host.add(this);
   }

   /**
    * Connect to the actual debug engine represented by this DebugEngine
    * object.
    * @param connectionInfo Information which specifies how to connect to the
    * engine.
    * @param noWait If true and the connection fails the first time do
    * not wait and try again.
    *  @exception java.io.IOException If there is a problem connecting to
    *  the debug engine.
    */

   public void connect(ConnectionInfo connectionInfo, boolean noWait)
   throws java.io.IOException
   {
     Connection connection = connectionInfo.getConnection();
     if(connection != null)
       setConnection(connection);
     else
       setConnection(connectionInfo.getNewConnection(Connection.AS_CLIENT, noWait));

     connection().startDumping();
   }

   /**
    * Connect to the actual debug engine represented by this DebugEngine
    * object.
    * @param connectionInfo Information which specifies how to connect to the
    * engine.
    *  @exception java.io.IOException If there is a problem connecting to
    *  the debug engine.
    */

   public void connect(ConnectionInfo connectionInfo)
   throws java.io.IOException
   {
     connect(connectionInfo, false);
   }

   short getEngineID()
   {
     return _engineID;
   }

   short getPlatformID()
   {
     return _platformID;
   }

   /**
    * Add a debug engine event listener to this DebugEngine object. Whenever
    * an event occurs for which there is a corresponding method in the
    * event listener's interface, that method will be called to inform the
    * listener of the event. More than one listener may be added to a given
    * DebugEngine object - the listeners will be notified of events in
    * the order in which they were added (i.e. FIFO).
    * @param eventListener The object whose methods will be called when
    * events occur.
    */

   public void addEventListener(DebugEngineEventListener eventListener)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, "DebugEngine.addEventListener(" + eventListener + ")");

     _eventListeners.addElement(eventListener);
   }

   /**
    * Remove a debug engine event listener from this DebugEngine object so
    * that it no longer receives event notifications.
    * @param eventListener The event listener to be removed from this
    * DebugEngine object's list of listeners.
    */

   public void removeEventListener(DebugEngineEventListener eventListener)
   {
     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "DebugEngine.removeEventListener(" + eventListener + ")");

     int index = _eventListeners.indexOf(eventListener);

     if (index != -1)
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
   }

   /**
    * Get the Host where this DebugEngine resides.
    */

   public Host host()
   {
     return _host;
   }

   /**
    * Query whether or not this debug engine is loaded and running.
    */

   public boolean isLoaded()
   {
    // if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.isLoaded()");

     return _isLoaded;
   }

   /**
    * Mark this debug engine as having been loaded.
    * @param isLoaded The debug engine will be marked as having
    * been loaded if this arg is 'true', and not loaded if the arg is 'false'.
    */

   public void setIsLoaded(boolean isLoaded)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".setIsLoaded(" + isLoaded + ")");

     _isLoaded = isLoaded;
   }

   /**
    * Get the DebuggeeProcess object representing the process currently being
    * debugged by this debug engine.
    */

   public DebuggeeProcess process()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.process()");

     return _process;
   }

   void handleError(int returnCode, String message)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, this + ".handleError(" + returnCode + ", " + message + ")");

     // Don't queue the error event if we're doing a restore and we've been
     // told to suppress error events during a restore:

     if (savedObjectsAreBeingRestored() && Model.willSuppressErrorEventsDuringRestore())
        return;

     Vector eventListeners = _eventListeners;

     // If this error is due to an "ambiguous breakpoint" request and the
     // Model has been told to handle ambiguous breakpoints, we will ensure
     // that the event gets fired on the Model's special event handler for
     // this situation instead of on the "normal" event listeners.

     // Note that we COULD handle the ambiguous bkp here instead of setting
     // up an event listener to do it, but that would violate the rule which
     // says that no new requests can be made of the engine while the
     // Model is being updated. It seems a little bit cleaner to do it in
     // an event handler instead and let the Model act as just another
     // client of itself.

     if (returnCode == EPDC.ExecRc_AmbiguousContext)
     {
	if (_mostRecentReply instanceof ERepBreakpointLocation)
	{
           if (Model.willHandleAmbiguousBreakpoints())
           {
	      if (Model.TRACE.DBG && Model.traceInfo())
                 Model.TRACE.dbg(3, "Model will attempt to handle ambiguous bkps");

	      eventListeners = new Vector(1);
	      eventListeners.addElement(new AmbiguousBreakpointHandler());
           }
	}
	else
	if (_mostRecentRequest instanceof EReqExpression &&
	    Model.willHandleAmbiguousMonitoredExpressions())
	{
	   if (Model.TRACE.DBG && Model.traceInfo())
	      Model.TRACE.dbg(3, "Model will attempt to handle ambiguous monitored expressions");

	   eventListeners = new Vector(1);
	   eventListeners.addElement(new AmbiguousMonitoredExpressionHandler());
	}
     }
     else
     if (returnCode == EPDC.ExecRc_InvalidStringFormat ||
         returnCode == EPDC.ExecRc_TerminateDebugger) // Defect 16895
     {
        if (Model.TRACE.ERR &&
            Model.traceInfo() &&
            returnCode == EPDC.ExecRc_InvalidStringFormat)
           Model.TRACE.err(1, "ERROR!!! Engine does not support UTF-8 encoded EStdStrings.");
        else
        if (Model.TRACE.DBG &&
            Model.traceInfo() &&
            returnCode == EPDC.ExecRc_TerminateDebugger)
           Model.TRACE.dbg(1, "Engine return code was ExecRc_TerminateDebugger. Engine will be terminated.");

        // Add an event listener which will send the engine a
        // "terminate" request to get rid of it:

        eventListeners.addElement(new ErrorOccurredEngineTerminator());
     }
     else
     if (_mostRecentRequest instanceof EReqPreparePgm ||
         _mostRecentRequest instanceof EReqProcessAttach ||
         _mostRecentRequest instanceof EReqProcessAttach2
        )
     {
        // Remove this once we have our engine reuse working

        // If any of the above requests failed for any reason, kill  the
        // engine:

        eventListeners.addElement(new ErrorOccurredEngineTerminator());
     }
     else
     if (returnCode == EPDC.ExecRc_FileNotFound)
     {
         // Do not add the event to the vector of engine events, because the
         // search for the file in the frontend has not happened yet. The
         // event will oly be fired when neither of the engine or the model
         // (the frontend) can find the file.
         EPDC_Request originalRequest = getMostRecentRequest();
         if (originalRequest instanceof EReqPartSet)
         {
             ErrorOccurredEvent event = new ErrorOccurredEvent(this, returnCode, message, _mostRecentReply.getReplyCode());

             saveErrorOccurredEventForFileNotFound(event);
             return;
         }
     }

     _eventManager.addEvent(new ErrorOccurredEvent(this,
                                                   returnCode,
                                                   message,
                                                   _mostRecentReply.getReplyCode()
                                                  ),
                            eventListeners
                           );
   }

   /**
    * Called when a message has been received from the debug engine but
    * an error has NOT occurred i.e. the return code from the engine was
    * "OK".
    */

   void sendMessage(String message)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, this + ".sendMessage(" + message + ")");

     // Don't queue the message event if we're doing a restore and we've been
     // told to suppress message events during a restore:

     if (savedObjectsAreBeingRestored() && Model.willSuppressMessageEventsDuringRestore())
        return;

     _eventManager.addEvent(new MessageReceivedEvent(this,
                                                     message,
                                                     _mostRecentReply.getReplyCode()
                                                  ),
                            _eventListeners
                           );
   }

   void add(DebuggeeProcess process)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, this + ".add(DebuggeeProcess<" + process.processID() + ">)");

     // Add the DebuggeeStartupController object as a listener for the
     // ProcessAdded event. It will remove itself after it has done whatever
     // it needs to do:

     addEventListener(_debuggeeStartupController);

     _process = process;

     _eventManager.addEvent(new ProcessAddedEvent(this,
                                                  process,
                                                  _mostRecentReply.getReplyCode()
                                                 ),
                            _eventListeners
                           );
   }

   void remove(DebuggeeProcess process)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, this + ".remove(" + process + ")");

     process.prepareToDie();

     _process = null;
   }

   /** Set the mechanism that will be used to communicate with the debug
    *  engine.
    */

   void setConnection(Connection connection)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(3, this + ".setConnection(" + connection + ")");

     _connection = connection;
   }

   Connection connection()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.connection()");

     return _connection;
   }

   /** Tell the debug engine to initialize itself. This method must be
    *  called exactly once for every DebugEngine object and it must be the
    *  first request made of the debug engine after connecting to it - a
    *  debug engine cannot be used to debug a program until it has been
    *  initialized.
    *  Attempting to initialize a debug engine that has already been
    *  initialized is a no-op; the method will return false.
    *  After a successful
    *  initialization, the characteristics and capabilities of the debug
    *  engine will be updated and reflected in the DebugEngine object. These
    *  attributes can then be queried by client code.
    *  <p>Successful initialization also means that this debug engine can now
    *  be used to debug a program.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
    *  @return An indication of whether or not the initialize request was
    *  sent to the debug engine successfully. A return
    *  value of 'true' does not mean that the initialize request succeeded
    *  but rather simply that the request was successfully sent to the
    *  debug engine. Success or failure of the initialize request will be
    *  indicated via the event notification mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    */

   public boolean initialize(byte dominantLanguage,
                             String productPrefix,
                             String debuggerArguments,
                             String remoteSearchPath,
                             int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, this + ".initialize(" + ((sendReceiveControlFlags == sendReceiveSynchronously)?"sendReceiveSynchronously":"sendReceiveAsynchronously") + ")");

     // First exchange EPDC versions with the engine:

     int requestCode = EPDC.Remote_Version;

     if (!prepareForEPDCRequest(requestCode, sendReceiveSynchronously))
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".initialize() fails in DebugEngine.prepareForEPDCRequest(Remote_Initialize_Debug_Engine)");
        return false; // Request can't be sent at this time
     }

     if (_hasBeenInitialized)
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + " has been initialized");

        cancelEPDCRequest(requestCode);
        return false; // Don't initialize more than once.
     }

     // Start listenening for replies from debug engine on another thread:

     if (_EPDCReplyProcessor == null && _isLoaded && _connection != null)
     {
        _replySemaphore = new Semaphore();

        _EPDCReplyProcessor = new EPDCReplyProcessor(this,
                                                     _replySemaphore,
                                                     _engineSession);

        _EPDCReplyProcessorThread = new Thread(_EPDCReplyProcessor);

        _EPDCReplyProcessorThread.start();
     }



     if (!processEPDCRequest(new EReqVersion(MAX_SUPPORTED_EPDC_VERSION), sendReceiveSynchronously))
        return false; // Request could not be sent

     // At the time of Remote_Version request, the EPDC version accepted by
     // the engine is not known yet. Therefore, initialize it to zero.
     _engineSession._negotiatedEPDCVersion = 0;

     // See if FE is too down-level for the engine to work with:

     if (_mostRecentReply.getReturnCode() == EPDC.VERSION_ERROR)
     {
        if (Model.TRACE.ERR && Model.traceInfo())
           Model.TRACE.err(1, "ERROR!!! UI's EPDC support is too down-level for engine to work with");

        // In this case we assume that the engine has shut itself down - we
        // do NOT send it a "terminate" request. However, we do need to remove
        // the engine from the Model:

        _host.remove(this);

        // The above call to Host.remove will have caused a DebugEngineTerminatedEvent
        // to be queued but because we're not in our "normal" event cycle
        // at this point, we need to call EventManager.fireAllQueuedEvents()
        // in order to get the event fired (not optimal):

        _eventManager.fireAllQueuedEvents();

        return false;
     }

     // See if the engine is too down-level for the FE to work with:

     int engineEPDCVersion = ((ERepVersion)_mostRecentReply).getVersion();

     if (engineEPDCVersion < MIN_SUPPORTED_EPDC_VERSION)
     {
        if (Model.TRACE.ERR && Model.traceInfo())
           Model.TRACE.err(1, "ERROR!!! Engine's EPDC support is too down-level for UI to work with");

        // Send the engine a "terminate" request to get rid of it:

        terminate(sendReceiveSynchronously);

        return false;
     }
     else
     if (engineEPDCVersion < MAX_SUPPORTED_EPDC_VERSION)
     {
        // Use the EPDC version the engine supports
        _engineSession._negotiatedEPDCVersion = engineEPDCVersion;
     }
     else
     {  // Engine's max is >= the UI max. Use the UI max.
        _engineSession._negotiatedEPDCVersion = MAX_SUPPORTED_EPDC_VERSION;
     }

     if (Model.TRACE.DBG && Model.traceInfo())
          Model.TRACE.dbg(1,"Negotiated EPDC version is " + _engineSession._negotiatedEPDCVersion);


     // Now do the initialize request:

     requestCode = EPDC.Remote_Initialize_Debug_Engine;

     if (!prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".initialize() fails in DebugEngine.prepareForEPDCRequest(Remote_Initialize_Debug_Engine)");
        return false; // Request can't be sent at this time
     }

     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_Initialize_Debug_Engine");

     return processEPDCRequest(new EReqInitializeDE(_dominantLanguage = dominantLanguage,
                                                    productPrefix,
                                                    debuggerArguments,
                                                    remoteSearchPath),
                               sendReceiveControlFlags);

   }

   /**
    * @deprecated Use the above version instead.
    */

   public boolean initialize(byte dominantLanguage,
                             String productPrefix,
                             int sendReceiveControlFlags)
   throws java.io.IOException
   {
     return initialize(dominantLanguage, productPrefix, null, null, sendReceiveControlFlags);
   }

   void setHasBeenInitialized(ERepInitializeDE initializeReply)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".setHasBeenInitialized()");

     // View Information

     _supportedViews = new ViewInformation[initializeReply.numberOfViews()];

     ERepGetViews[] viewInformation = initializeReply.viewInformation();

     int j;
     for (j = 0; j < viewInformation.length; j++)
	 add(new ViewInformation(viewInformation[j], (short)(j+1)), (short)j);

     // Representation Names

     _repNames = new Representation[initializeReply.numberOfRepNames()];

     EStdString[] repName = initializeReply.repNames();

     for (j = 0; j < repName.length; j++)
	  add(new Representation(repName[j], (short)(j+1)), (short)j);

     // Language Information

     ERepGetLanguages[] langInfo = initializeReply.languageInfo();

     for (j = 0; j < langInfo.length; j++)
         add(new Language(langInfo[j], this), langInfo[j].getLanguageID());

     // Exception Information

     _exceptionInfo = new DebuggeeException[initializeReply.numberOfExceptions()];

     ERepGetExceptions[] excpInfo = initializeReply.exceptionInfo();

     if (excpInfo != null)
     {
         for (j = 0; j < excpInfo.length; j++)
              add(new DebuggeeException(excpInfo[j], this), j);
     }

     _engineID = initializeReply.getEngineID();
     _platformID = initializeReply.getPlatformID();

     // In V7 Java PICL lies to us re: the platform it's running on (always
     // says "AIX"). Therefore, we'll allow other engines to override the
     // host's platform ID if it was initially set based on what Java PICL
     // told us:

     boolean platformIDCanBeOverridden = (_engineID == EPDC.BE_TYPE_JAVA_PICL);

     switch (_platformID)
     {
       case EPDC.PLATFORM_ID_OS2:
            _host.setPlatformID(Host.OS2, platformIDCanBeOverridden);
            break;

       case EPDC.PLATFORM_ID_MVS:
       case EPDC.PLATFORM_ID_VM370:
            _host.setPlatformID(Host.OS390, platformIDCanBeOverridden);
            break;

       case EPDC.PLATFORM_ID_AS400:
            _host.setPlatformID(Host.OS400, platformIDCanBeOverridden);
            break;

       case EPDC.PLATFORM_ID_AIX:
            _host.setPlatformID(Host.AIX, platformIDCanBeOverridden);
            break;

       case EPDC.PLATFORM_ID_NT:
            _host.setPlatformID(Host.WindowsNT, platformIDCanBeOverridden);
            break;

       case EPDC.PLATFORM_ID_JVM:
            _host.setPlatformID(Host.JVM, platformIDCanBeOverridden);
            break;

       case EPDC.PLATFORM_ID_HPUX:
            _host.setPlatformID(Host.HPUX, platformIDCanBeOverridden);
            break;
     }

     int defaultSettings = initializeReply.getDefaultSettings();

     _dateBreakpointsEnabled = ((defaultSettings & EPDC.DateBkpEnable) != 0);

     _entryBreakpointsAutoSetEnabled = ((defaultSettings & EPDC.AutoSetEntryBkpEnable) != 0);

     _hasBeenInitialized = true;
   }

   /**
    * Returns an indication of whether or not this debug engine has been
    * successfully initialized.
    * @see DebugEngine#initialize
    */

   public boolean hasBeenInitialized()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.hasBeenInitialized()");

     return _hasBeenInitialized;
   }

   /** Terminate the debug engine. This method must be
    *  the last request made of the debug engine -
    *  a debug engine cannot be used to debug a program after it has been
    *  terminated.
    *  <p>Currently, the only way that a DebugEngine object will be removed
    *  from the Model (i.e. removed from its owning Host object) is if we
    *  receive a reply from the actual debug engine saying that the "terminate"
    *  request was successful. This has implications:
    *  <ol>
    *  <li>In order to be able to send requests to the debug engine, the Model
    *  requires that the debug engine has been initialized via the
    *  "initialize" request. This means that, currently, one cannot terminate
    *  a debug engine unless it has been previously initialized.
    *  <li>If, for some other reason we cannot send requests to the debug
    *  engine (e.g. the connection to the debug engine is lost), then the
    *  terminate request can obviously not be processed and, hence,
    *  the DebugEngine object will not be removed from the Model.
    *  </ol>
    *  Both of the above problems will be addressed in a future driver - there
    *  will be a way to get rid of the DebugEngine from the Model even if the
    *  "terminate" request cannot be sent to the debug engine.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
    *  @return An indication of whether or not the terminate request was
    *  sent to the debug engine successfully. A return
    *  value of 'true' does not mean that the terminate request succeeded
    *  but rather simply that the request was successfully sent to the
    *  debug engine. Success or failure of the terminate request will be
    *  indicated via the event notification mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    */

   public boolean terminate(int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, this + ".terminate(" + ((sendReceiveControlFlags == sendReceiveSynchronously)?"sendReceiveSynchronously":"sendReceiveAsynchronously") + ")");

     int requestCode = EPDC.Remote_Terminate_Debug_Engine;

     // TODO: If we can't send the request we need to determine why we
     // can't - there may be some situations in which we can't send a
     // terminate request to the DE but we still want to remove this object
     // from the Model and fire a "terminated" event. For example, if for
     // some reason the connection to the DE is lost, we won't be able to
     // send a terminate request to it but we still want to remove the
     // DebugEngine from the Model.

     if (!prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".terminate() fails in DebugEngine.prepareForEPDCRequest(Remote_Terminate_Debug_Engine)");
        return false;
     }

     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_Terminate_Debug_Engine");

     return processEPDCRequest(new EReqTerminateDE(), sendReceiveControlFlags);
   }

   /** send a CommandLogExecute command to the debug engine.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
    *  @return An indication of whether or not the terminate request was
    *  sent to the debug engine successfully. A return
    *  value of 'true' does not mean that the terminate request succeeded
    *  but rather simply that the request was successfully sent to the
    *  debug engine. Success or failure of the terminate request will be
    *  indicated via the event notification mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    */

   public boolean commandLogExecute(String command, int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, this + ".commandLogExecute(" +command+", "+ ((sendReceiveControlFlags == sendReceiveSynchronously)?"sendReceiveSynchronously":"sendReceiveAsynchronously") + ")");
     if(command==null || command.equals(""))    // null or empty commands kill engine
        command = " ";

     int requestCode = EPDC.Remote_CommandLogExecute;

     if (!prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".commandLogExecute() fails in DebugEngine.prepareForEPDCRequest(Remote_CommandLogExecute)");
        return false;
     }

     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_CommandLogExecute");

     return processEPDCRequest(new EReqCommandLogExecute(command), sendReceiveControlFlags);
   }

   public void commandLogResponse(String[] responseLines, int returnCode)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, this + ".commandLogResponse "+responseLines );

     _eventManager.addEvent(new DebugEngineCommandLogResponseEvent(this,
                                                   this,
                                                   returnCode,
                                                   responseLines
                                                  ),
                            _eventListeners
                           );
   }

   /**
    *  Tell the debug engine to load a program and start debugging it. The
    *  debug engine must have been initialized via the initialize() method
    *  before calling the prepareProgram method.
    *  <p>If the debug engine is
    *  able to successfully load the program it will typically respond with
    *  information regarding the process that was created, including the
    *  process ID, thread information, module and part information, etc. Again,
    *  all of this information will be conveyed to client code via the event
    *  listener mechanism. In addition, the Model will be updated with
    *  all of this information so that it can be queried on-demand. For
    *  example, a DebuggeeProcess object will be created and will be owned by
    *  this DebugEngine. In turn, that DebuggeeProcess object will contain
    *  Module and Thread objects representing the modules and threads within
    *  the process.
    *
    *  @param arguments A DebuggeePrepareOptions object containing the name of
    *  the program to be debugged, the args to be passed to that program,
    *  and several options for how that program is to be started by the
    *  the debugger.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
    *  @return An indication of whether or not the "prepare program" request was
    *  sent to the debug engine successfully. A return
    *  value of 'true' does not mean that the request succeeded
    *  but rather simply that it it was successfully sent to the
    *  debug engine. Success or failure of the prepare program request will be
    *  indicated via the event notification mechanism.
    * @see DebuggeePrepareOptions
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    */

   public boolean prepareProgram(DebuggeePrepareOptions options,
                                 int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (options == null)
     {
        if (_debuggeeStartupOptions == null)
           return false;
        else
        if (!(_debuggeeStartupOptions instanceof DebuggeePrepareOptions))
           return false; // If we're going to reuse the options we've already
                         // got (e.g. for a restart) then they had better be
                         // "prepare" options, not "attach" options.
     }
     else
        _debuggeeStartupOptions = options;

     int requestCode = EPDC.Remote_PreparePgm;

     if (!prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".prepareProgram() fails in DebugEngine.prepareForEPDCRequest(Remote_PreparePgm)");
        return false;
     }

/*
     if (!_hasBeenInitialized || _process != null)
*/
     if (Model.checkFCTBit && (!_hasBeenInitialized || _process != null ||
         !getCapabilities().getStartupCapabilities().debugInitializationSupported() &&
         !((DebuggeePrepareOptions)_debuggeeStartupOptions).runToMainEntryPoint())
        )
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".prepareProgram() fails due to DebugEngine.hasNotBeenInitialized");

        cancelEPDCRequest(requestCode);
        return false;
     }

     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_PreparePgm");

     EReqPreparePgm request = null;

     if (_engineSession._negotiatedEPDCVersion == 305)
     {
         request = new EReqPreparePgm(_debuggeeStartupOptions.getDebuggeeName(),                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).getDebuggeeArguments(),
                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).getJobName(),
                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).runToMainEntryPoint(),
                                      (byte)0);
     }
     else
     {
         request = new EReqPreparePgm(_debuggeeStartupOptions.getDebuggeeName(),                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).getDebuggeeArguments(),
                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).getJobName(),
                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).runToMainEntryPoint(),
                                      ((DebuggeePrepareOptions)_debuggeeStartupOptions).getDominantLanguage());
     }

     return processEPDCRequest(request, sendReceiveControlFlags);

   }

   /**
    * Get The XML stream information from the debug engine.
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendReceiveSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return TXDocument The DOM generated after parsing the stream. The DOM
    * is obtained from the engine when the user attempts to get the
    * engine settings and is updated when the user changes some information
    * in the settings. The returned value will be null in case of any errors
    * while processing the request, or when no XML stream is provided by the
    * engine.
    * @exception java.io.IOException If there is a problem communicating
    * with the debug engine.
    */
   public Document getEngineSettings(int sendReceiveControlFlags)
       throws java.io.IOException, org.xml.sax.SAXException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, this + ".getEngineSettings()");

     if (!prepareForEPDCRequest(EPDC.Remote_GetEngineSettings,
                                sendReceiveControlFlags))
         return null;

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(2, "Sending EPDC request: Remote_GetEngineSettings");

     EReqGetEngineSettings request = new EReqGetEngineSettings();
     if (!processEPDCRequest(request, sendReceiveControlFlags))
         return null;

     ERepGetEngineSettings reply = (ERepGetEngineSettings) getMostRecentReply();

     if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
         return null;

     String stream = reply.XMLStream();

     if (stream == null)
         return null;

     // replace all newlines with spaces
     stream = stream.replace('\n', ' ');

     ByteArrayInputStream input    = new ByteArrayInputStream(stream.getBytes(EExtString.getEncoding()));
     InputSource          is       = new InputSource(input);
     DOMParser            parser   = new DOMParser();
     parser.parse(is);
     Document             document = parser.getDocument();

     return document;
   }

   /**
    * Update the Engine settings when the user has added/changed/removed one
    * or more of the settings.
    * @param document The DOM representing the changes the user has made to
    * the engine settings.
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendReceiveSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return This method will either return a DOM when the engine setting
    * have been updated but there are errors in one or more of the engine
    * settings and null otherwise.
    * sent to the debug engine successfully.
    * @exception java.io.IOException If there is a problem communicating
    * with the debug engine.
    */
   public Document putEngineSettings(Document document,
                                     int sendReceiveControlFlags)
       throws java.io.IOException, org.xml.sax.SAXException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, this + ".putEngineSettings()");

     ByteArrayOutputStream output         = new ByteArrayOutputStream();
     BufferedWriter        bufferedWriter = new BufferedWriter(new OutputStreamWriter(output, EExtString.getEncoding()));
     PrintWriter           writer         = new PrintWriter(bufferedWriter);
     OutputFormat          format         = new OutputFormat(document);
     format.setEncoding((String)null);
     format.setLineWidth(200);
     XMLSerializer         xmlWriter      = new XMLSerializer(writer, format);
     xmlWriter.serialize(document);
     xmlWriter.endDocument();

     if (output == null)
         return null;

     byte[] XMLStream = output.toByteArray();

     if (!prepareForEPDCRequest(EPDC.Remote_PutEngineSettings,
                                sendReceiveControlFlags))
         return null;

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(2, "Sending EPDC request: Remote_PutEngineSettings");

     EReqPutEngineSettings request = new EReqPutEngineSettings(XMLStream);
     if (!processEPDCRequest(request, sendReceiveControlFlags))
         return null;

     ERepPutEngineSettings reply = (ERepPutEngineSettings)getMostRecentReply();

     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, this + ".putEngineSettings reply = " + reply);

     if (reply == null)
         return null;

     String stream = null;
     if (reply.getReturnCode() != EPDC.ExecRc_OK)
     {
         stream = reply.XMLStream();

         if (stream == null)
             return null;

         // replace all newlines with spaces
         stream = stream.replace('\n', ' ');

         ByteArrayInputStream input    = new ByteArrayInputStream(stream.getBytes(EExtString.getEncoding()));
         InputSource          is       = new InputSource(input);
         DOMParser            parser   = new DOMParser();
         parser.parse(is);
         document = parser.getDocument();

         return document;
     }
     return null;

   }

   /**
    *  Attach to a process in order to debug it.
    *  The debug engine must have been initialized via the initialize() method
    *  before calling the attach() method.
    *  <p>If the debug engine is
    *  able to successfully attach to the process it will typically respond with
    *  information regarding the that process, including the
    *  process ID, thread information, module and part information, etc. Again,
    *  all of this information will be conveyed to client code via the event
    *  listener mechanism. In addition, the Model will be updated with
    *  all of this information so that it can be queried on-demand. For
    *  example, a DebuggeeProcess object will be created and will be owned by
    *  this DebugEngine. In turn, that DebuggeeProcess object will contain
    *  Module and Thread objects representing the modules and threads within
    *  the process.
    *
    *  @param arguments A DebuggeeAttachOptions object containing the id of
    *  the process to be debugged etc.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
    *  @return An indication of whether or not the "attach" request was
    *  sent to the debug engine successfully. A return
    *  value of 'true' does not mean that the request succeeded
    *  but rather simply that it it was successfully sent to the
    *  debug engine. Success or failure of the attach request will be
    *  indicated via the event notification mechanism.
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    */

   public boolean attach(DebuggeeAttachOptions options,
			 int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (options == null)
        return false;
     else
        _debuggeeStartupOptions = options;

     int requestCode;
     EPDC_Request request;

     SystemProcess process = options.getProcess();

     if (process == null) // Attach using the process id
     {
        requestCode = EPDC.Remote_ProcessAttach;

        if (_engineSession._negotiatedEPDCVersion == 305)
        {
        request = new EReqProcessAttach(options.getProcessID(),
                                        options.getDebuggeeName(),
                                        options.getEventHandlerID(),
                                        (byte)0
                                       );
        }
        else
        {
        request = new EReqProcessAttach(options.getProcessID(),
                                        options.getDebuggeeName(),
                                        options.getEventHandlerID(),
                                        options.getDominantLanguage()
                                       );
        }
     }
     else // Attach using the process index (as generated by the engine)
     {
        requestCode = EPDC.Remote_ProcessAttach2;

        if (_engineSession._negotiatedEPDCVersion == 305)
        {
        request = new EReqProcessAttach2(process.getIndex(),
                                         options.getDebuggeeName(),
                                         (byte)0
                                        );
        }
        else
        {
        request = new EReqProcessAttach2(process.getIndex(),
                                         options.getDebuggeeName(),
                                         options.getDominantLanguage()
                                        );
        }
     }

     if (!prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".attach() fails in DebugEngine.prepareForEPDCRequest(Remote_PreparePgm)");
        return false;
     }

     if (!_hasBeenInitialized
         || _process != null
         || !getCapabilities().getFileCapabilities().processAttachSupported())
/*
     if (!_hasBeenInitialized ||
         !getCapabilities().getStartupCapabilities().debugInitializationSupported() &&
         !_debuggeeStartupOptions.runToMainEntryPoint()
        )
*/
     {
       if (Model.TRACE.ERR && Model.traceInfo())
          Model.TRACE.err(4, this + ".attach() fails due to DebugEngine.hasNotBeenInitialized");

        cancelEPDCRequest(requestCode);
        return false;
     }

     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_ProcessAttach");

     return processEPDCRequest(request,
                               sendReceiveControlFlags);
   }

   /**
    * Ask the debug engine for a list of all processes which can be
    * attached to.
    * @return A Vector of SystemProcess objects representing the processes.
    * In order to attach to one of these processes and start debugging it,
    * client code should construct a DebuggeeAttachOptions object (using the
    * ctor which takes a SystemProcess object) and then call DebugEngine.attach.
    * @see SystemProcess
    * @see DebuggeeAttachOptions
    * @see DebugEngine#attach
    *  @exception java.io.IOException If there is a problem communicating
    *  with the debug engine.
    */

   public Vector getSystemProcessList()
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(3, this + ".getSystemProcessList()");

     final int requestCode = EPDC.Remote_ProcessListGet;

     if (!prepareForEPDCRequest(requestCode, sendReceiveSynchronously))
	return null;

     // If "attach" is not supported, let's assume that we can't ask
     // for a list of processes:

     if (Model.checkFCTBit)
     {
         if (!getCapabilities().getFileCapabilities().processAttachSupported())
         {
             cancelEPDCRequest(requestCode);
             return null;
         }
     }

     if (Model.TRACE.EVT && Model.traceInfo())
	Model.TRACE.evt(3, "Sending EPDC request: Remote_ProcessListGet");

     if (!processEPDCRequest(new EReqProcessListGet(), sendReceiveSynchronously))
	return null;

     ERepProcessListGet reply = (ERepProcessListGet)getMostRecentReply();

     if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
	return null;
     else
     {
        // Each element in the vector retrieved from EPDC is a String[].
        // For each element, we'll use the String[] to construct a
        // SystemProcess object and then reset the Vector element to be
        // the SystemProcess object instead of the String[] (thereby re-using
        // the Vector rather than creating a new one).

        Vector processes = reply.getProcesses();

        if (processes != null)
        {
           int numberOfProcesses = processes.size();

           for (int i = 0; i < numberOfProcesses; i++)
               processes.setElementAt(new SystemProcess((String[])processes.elementAt(i),
                                                        i+1), // List is 1-based
                                      i);
        }

        return processes;
     }
   }

  public ProcessListColumnDetails[] getProcessListColumnDetails()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, this + ".getProcessListColumnDetails()");

    if (_processListColumnDetails == null)
    {
      final int requestCode = EPDC.Remote_ProcessDetailsGet;

      if (!prepareForEPDCRequest(requestCode, sendReceiveSynchronously))
         return null;

      // If "attach" is not supported, let's assume that we can't ask
      // for a list of processes:
      if (Model.checkFCTBit)
      {
          if (!getCapabilities().getFileCapabilities().processAttachSupported())
          {
              cancelEPDCRequest(requestCode);
              return null;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(3, "Sending EPDC request: Remote_ProcessDetailsGet");

      if (!processEPDCRequest(new EReqProcessDetailsGet(), sendReceiveSynchronously))
         return null;

      ERepProcessDetailsGet reply = (ERepProcessDetailsGet)getMostRecentReply();

      if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
         return null;

      Vector epdcColumns = reply.columnInfo();
      int numberOfColumns = 0;

      // Here's the scoop on why the following is more complicated than it
      // should be: Each column has a column ID. EPDC makes no assurances
      // that the columns will be sent to us in order of ascending ID,
      // nor that the ID numbers contain no gaps (e.g. instead of 1, 2, 3, ...
      // they might be 10, 20, 30, ...). So, as we're getting the columns
      // from the EPDC packet, we'll stick them in a Vector. This will ensure
      // that they're in ascending order, but the Vector may contain gaps.
      // To get rid of the gaps, we'll copy non-null elements into an array,
      // and that's what we'll return:

      if (epdcColumns != null && (numberOfColumns = epdcColumns.size()) > 0)
      {
        Vector processListColumnDetails = new Vector(numberOfColumns);

        int i = 0, j = 0;

        for (; i < numberOfColumns; i++)
        {
          ERepGetProcessColumns epdcColumnDetails = (ERepGetProcessColumns)epdcColumns.elementAt(i);
          setVectorElementToObject(new ProcessListColumnDetails(epdcColumnDetails),
                                   processListColumnDetails,
                                   epdcColumnDetails.getColumnID());
        }

        _processListColumnDetails = new ProcessListColumnDetails[numberOfColumns];

        int vectorSize = processListColumnDetails.size();

        for (i = 0; i < vectorSize; i++)
          if (processListColumnDetails.elementAt(i) != null)
             _processListColumnDetails[j++] = (ProcessListColumnDetails)processListColumnDetails.elementAt(i);
      }
    }

    return _processListColumnDetails;
  }

   /**
    * Query whether or not this debug engine is busy servicing a previous
    * request. No additional requests can be sent to the debug engine while it
    * is busy nor can client code obtain a lock on the Model (via getLock) while
    * the debug engine is busy.
    * @see DebugEngineEventListener#modelStateChanged
    * @see DebugEngine#getState
    */

   synchronized public boolean isBusy()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".isBusy() : " + ((_stateFlags & debugEngineIsBusyFlag) != 0));

     return (_stateFlags & debugEngineIsBusyFlag) != 0;
   }

   /**
    * Query whether or not event listeners are in the process of being called
    * back i.e events that were queued as a result of updating the Model are
    * now being fired.
    * No asynchronous requests can be sent to the debug engine while queued
    * events are being fired. Furthermore, only the thread on which events
    * are being fired (i.e. the thread on which event listeners are being
    * called back) can obtain a lock on the Model (via getLock).
    * @see DebugEngineEventListener#modelStateChanged
    * @see DebugEngine#getState
    */

   synchronized public boolean queuedEventsAreBeingFired()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".queuedEventsAreBeingFired() : " + ((_stateFlags & queuedEventsAreBeingFiredFlag) != 0));

     return (_stateFlags & queuedEventsAreBeingFiredFlag) != 0;
   }

   /**
    * Query whether or not the Model is currently being updated due to a reply
    * from the debug engine to a previous
    * request. No additional requests can be sent to the debug engine while
    * the Model is being updated nor can client code obtain a lock on the Model (via getLock) while
    * the it is being updated.
    * Querying the Model while it is being updated is not
    * recommended since it may contain inconsistent information.
    * @see DebugEngineEventListener#modelStateChanged
    * @see DebugEngine#getState
    */

   synchronized public boolean modelIsBeingUpdated()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".modelIsBeingUpdated() : " + ((_stateFlags & modelIsBeingUpdatedFlag) != 0));

     return (_stateFlags & modelIsBeingUpdatedFlag) != 0;
   }

   /**
    * Query whether or not saved objects are currently being restored.
    * @see DebuggeeProcess#restoreSavedObjects
    * @see RestorableObjects#restore
    */

   synchronized public boolean savedObjectsAreBeingRestored()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".savedObjectsAreBeingRestored() : " + ((_stateFlags & savedObjectsAreBeingRestoredFlag) != 0));

     return (_stateFlags & savedObjectsAreBeingRestoredFlag) != 0;
   }

   /**
    * Query whether or not the debug engine is currently able to accept and
    * process new synchronous requests. A debug engine cannot accept new requests if i) there
    * is a previous request outstanding, ii) the Model is being updated based
    * on a previous request, or iii) we are in the process of firing queued
    * events to event listeners and the synch request is not being done from the
    * thread on which the callbacks are being done.
    */

   synchronized public boolean isAcceptingSynchronousRequests()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
     {
        if (isBusy())
           Model.TRACE.dbg(3, "Not Accepting Synchronous Requests Because Engine is Busy");

        if (modelIsBeingUpdated())
           Model.TRACE.dbg(3, "Not Accepting Synchronous Requests Because Model is Being Updated");

        if (queuedEventsAreBeingFired() && Thread.currentThread() != _callbackThread)
           Model.TRACE.dbg(3, "Not Accepting Synchronous Requests Because Non-Callback Thread");
     }

     return !isBusy() && !modelIsBeingUpdated() &&
            (!queuedEventsAreBeingFired() || Thread.currentThread() == _callbackThread);
   }

   /**
    * Query whether or not the debug engine is currently able to accept and
    * process new asynchronous requests. A debug engine cannot accept new
    * asynchronous requests if i) there
    * is a previous request outstanding (i.e. the debug engine is busy), ii)
    * the Model is being updated based on a reply to a previous request, or
    * iii) we are in the process of firing queued events to event listeners.
    */

   synchronized public boolean isAcceptingAsynchronousRequests()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".isAcceptingAsynchronousRequests() : " + (!isBusy() && !modelIsBeingUpdated() && !queuedEventsAreBeingFired()));

     return !isBusy() && !modelIsBeingUpdated() && !queuedEventsAreBeingFired();
   }

   // IMPORTANT: Must call prepareForEPDCRequest before calling
   // processEPDCRequest! If the former returns false, don't call the latter.
   // If prepareForEPDCRequest returns true but you subsequently decide not to call
   // processEPDCRequest, you must call cancelEPDCRequest to let the DebugEngine
   // reset itself.

   boolean prepareForEPDCRequest(int requestCode, int sendReceiveControlFlags)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".prepareForEPDCRequest(" + requestCode + ", " + ((sendReceiveControlFlags == sendReceiveSynchronously)?"sendReceiveSynchronously":"sendReceiveAsynchronously") + ")");

     // Make this request wait until we have either:
     // a) Sent the previous request to the engine, or
     // b) Cancelled the previous request, or
     // c) Rejected the previous request

     _requestSemaphore.countedWait();

     byte oldState = _stateFlags;

     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine current state : " + _stateFlags);

     // This code is synchronized on _lockSemaphore so that no other thread
     // can concurrently...
     // i) Attempt to submit a request to the debug engine
     // ii) Attempt to get a lock on the Model
     // iii) Change the status of the Model
     // None of the above actions should occur while this code is executing.

     synchronized(_lockSemaphore)
     {
       if (!_isLoaded || _connection == null)
       {
          //if (Model.TRACE.ERR && Model.traceInfo())
            //Model.TRACE.err(4, "DebugEngine.prepareForEPDCRequest() fails due to DebugEngine not loaded or no connection");

          _requestSemaphore.countedNotify();
          return false;
       }

       // Normally we won't allow any request to go through if the debug
       // engine is busy servicing a previous request. However, we make
       // an exception for the HALT request - we will allow this to be
       // processed if the engine is currently busy servicing an "execute"
       // (run) request.

       if (requestCode == EPDC.Remote_Halt)
       {
          if (isBusy() &&
              _mostRecentRequest != null &&
              _mostRecentRequest.requestCode() == EPDC.Remote_Execute)
             return true;
          else
          {
             _requestSemaphore.countedNotify();
             return false;
          }
       }
       else
       if ((sendReceiveControlFlags & sendReceiveSynchronously) != 0)
       {
          if (!isAcceptingSynchronousRequests()) // Reject synchronous requests
          {
             if (Model.TRACE.DBG && Model.traceInfo())
                Model.TRACE.dbg(1, "DebugEngine.prepareForEPDCRequest() fails due to DebugEngine.isNotAcceptingSynchronousRequests");

             _requestSemaphore.countedNotify();
             return false;
          }
       }
       else
          if (!isAcceptingAsynchronousRequests()) // Reject asynchronous requests
          {
             if (Model.TRACE.DBG && Model.traceInfo())
                Model.TRACE.dbg(1, "DebugEngine.prepareForEPDCRequest() fails due to DebugEngine.isNotAcceptingAsynchronousRequests");

             _requestSemaphore.countedNotify();
             return false;
          }

       // If there is a valid lock, it must be owned by the current thread in
       // order to proceed:

       if (_currentLock != null &&
           _currentLock.getCurrentThread() != Thread.currentThread())
          {
             if (Model.TRACE.DBG && Model.traceInfo())
                Model.TRACE.dbg(1, "DebugEngine.prepareForEPDCRequest() fails due to lock not owned by current thread");

             _requestSemaphore.countedNotify();
             return false;
          }

       _stateFlags |= debugEngineIsBusyFlag;

       //if (Model.TRACE.DBG && Model.traceInfo())
         //Model.TRACE.dbg(4, "DebugEngine current state : " + _stateFlags);

       // Veto the idle event if there is one:

       if (_idleEvent != null)
       {
          _idleEvent.setIsVetoable(true);
          _idleEvent.veto();
          _idleEvent.setIsVetoable(false);
       }
     }

     //if (Model.TRACE.EVT && Model.traceInfo())
       //Model.TRACE.evt(4, "Firing ModelStateChangedEvent");

     _eventManager.fireEvent(new ModelStateChangedEvent(this,
                                                        this,
                                                        oldState,
                                                        _stateFlags,
                                                        requestCode
                                                       ),
                             _eventListeners
                            );

     return true;
   }

   void cancelEPDCRequest(int requestCode)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".cancelEPDCRequest(" + requestCode + ")");

     byte oldState = _stateFlags;

     if (isBusy()) // The debug engine was busy but it isn't now...
     {
        synchronized(_lockSemaphore)
        {
           _stateFlags ^= debugEngineIsBusyFlag;

          // If there are any threads waiting to get the lock, wake one up
          // and give it another chance to get the lock.

           _lockSemaphore.countedNotify();
        }

        //if (Model.TRACE.EVT && Model.traceInfo())
          //Model.TRACE.evt(4, "Firing ModelStateChangedEvent");

        _eventManager.fireEvent(new ModelStateChangedEvent(this,
                                                           this,
                                                           oldState,
                                                           _stateFlags,
                                                           requestCode
                                                          ),
                                _eventListeners
                               );
     }

     // Now that we have cancelled the request, it's safe
     // to let the code in prepareForEPDCRequest be executed for a subsequent
     // request so we'll "open the gate" again:

     _requestSemaphore.countedNotify();
   }

   // IMPORTANT: Must call prepareForEPDCRequest before calling
   // processEPDCRequest! If the former returns false, don't call the latter.
   // If prepareForEPDCRequest returns true but you subsequently decide not to call
   // processEPDCRequest, you must call cancelEPDCRequest to let the DebugEngine
   // reset itself.
   // By the time this method is called, the DebugEngine must have been marked
   // "busy" by a successful call to prepareForEPDCRequest. This method assumes
   // this to be the case.

   boolean processEPDCRequest(EPDC_Request request, int sendReceiveControlFlags)
   throws java.io.IOException
   {
     processEPDCRequestWithReply(request,
                                 null,
                                 null,
                                 sendReceiveControlFlags,
                                 null);

     return true;
   }

   boolean processEPDCRequest(EPDC_Request request, int sendReceiveControlFlags, Object requestProperty)
   throws java.io.IOException
   {
     processEPDCRequestWithReply(request,
                                 null,
                                 null,
                                 sendReceiveControlFlags,
                                 requestProperty);

     return true;
   }

   boolean processEPDCRequest(EPDC_Request request,
                              Client client,
                              ModelEventListener privilegedListener,
                              int sendReceiveControlFlags)
   throws java.io.IOException
   {
     processEPDCRequestWithReply(request,
                                 client,
                                 privilegedListener,
                                 sendReceiveControlFlags,
                                 null);

     return true;
   }

   EPDC_Reply processEPDCRequestWithReply(EPDC_Request request,
                                          int sendReceiveControlFlags)
   throws java.io.IOException
   {
     return processEPDCRequestWithReply(request,
                                        null,
                                        null,
                                        sendReceiveControlFlags,
                                        null);
   }

   EPDC_Reply processEPDCRequestWithReply(EPDC_Request request,
                                          Client client,
                                          ModelEventListener privilegedListener,
                                          int sendReceiveControlFlags,
                                          Object requestProperty)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, this + ".processEPDCRequest(" + request + ", " + ((sendReceiveControlFlags == sendReceiveSynchronously)?"sendReceiveSynchronously":"sendReceiveAsynchronously") + ")");

     //     System.out.print("Request packet size: ");
     //     System.out.println(request.totalBytes());

     // Tell the EPDC object to write itself out using the stream from
     // our _connection object:

     // set the correct EPDC version this request will be written out with
     request.setEPDCEngineSession(_engineSession);

     // set the request property
     setRequestProperty(requestProperty);

     try
     {
       request.output(_connection);
     }
     catch (java.io.IOException excp)
     {
       handleLostConnection(excp);
     }

     if (request.requestCode() != EPDC.Remote_Halt)
     {
        _mostRecentReply = null;
        _mostRecentRequest = request;
        _currentClient = client;
        _privilegedListener = privilegedListener;
     }

     // Now that we have sent the request to the engine, it's safe
     // to let the code in prepareForEPDCRequest be executed for a subsequent
     // request so we'll "open the gate" again:

     _requestSemaphore.countedNotify();

     // There is no reply defined for a Remote_Halt request
     if (request.requestCode() == EPDC.Remote_Halt)
        return null;

     EPDC_Reply reply = null;

     // synchronized(_replySemaphore)
     {
       _EPDCReplyProcessor.prepareToReceiveReply();

       if ((sendReceiveControlFlags & sendReceiveSynchronously) != 0)
       {  // Wait for reply from debug engine and process reply on this thread:

          reply = _EPDCReplyProcessor.getReply();

          setModelIsBeingUpdated(true);

          // The default behaviour is that events will be queued as the Model
          // is updated by processReply:

          _EPDCReplyProcessor.processReply(reply);

          // Clear out the request property once the reply is processed.
          setRequestProperty(null);

          // Queued events will now be fired to event listeners:

          if ((sendReceiveControlFlags &
               sendReceiveCallerWillCompleteModelUpdates) == 0)
             setModelIsBeingUpdated(false);
       }
       else // We're not going to process the reply on this thread
            // so wake up the _EPDCReplyProcessor thread:
          _replySemaphore.countedNotify();
     }

     return reply;
   }

   void handleLostConnection(java.io.IOException excp)
   throws java.io.IOException
   {
     setIsLoaded(false);

     // We didn't really get the ExecRc_Exception return code from the
     // engine but we're using it as a way of telling the UI that something
     // terrible happened and it can't continue to use this debug engine:

     _eventManager.fireEvent(new ErrorOccurredEvent(this,
                                                    EPDC.ExecRc_Exception,
                                                    null, // UI is expected to supply its own msg text
                                                    -1),
                             _eventListeners);

     _eventManager.setMode(EventManager.abort); // In case we were in the middle
                                                // of firing events when the
                                                // exception occurred.

     _host.remove(this);
     throw excp;
   }

   void setModelIsBeingUpdated(boolean modelIsBeingUpdated)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".setModelIsBeingUpdated(" + modelIsBeingUpdated + ")");

     int requestCode = _mostRecentReply.getReplyCode();

     byte oldState = _stateFlags;

     if (modelIsBeingUpdated) // Model updates are beginning
     {
        synchronized(_lockSemaphore)
        {
          _stateFlags ^= debugEngineIsBusyFlag;

          _stateFlags |= modelIsBeingUpdatedFlag;
        }

        //if (Model.TRACE.EVT && Model.traceInfo())
          //Model.TRACE.evt(4, "Firing ModelStateChangedEvent");

        _eventManager.fireEvent(new ModelStateChangedEvent(this,
                                                           this,
                                                           oldState,
                                                           _stateFlags,
                                                           requestCode
                                                          ),
                                _eventListeners
                                );
     }
     else // Model updates are ending
     {
        synchronized(_lockSemaphore)
        {
          _stateFlags ^= modelIsBeingUpdatedFlag;
          _stateFlags |= queuedEventsAreBeingFiredFlag;
          _callbackThread = Thread.currentThread();
        }

        // Tell client that Model updates are ending and callbacks
        // are beginning:

        //if (Model.TRACE.EVT && Model.traceInfo())
          //Model.TRACE.evt(4, "Firing ModelStateChangedEvent");

        _eventManager.fireEvent(new ModelStateChangedEvent(this,
                                                           this,
                                                           oldState,
                                                           _stateFlags,
                                                           requestCode
                                                          ),
                                _eventListeners
                               );

        // Once all Model updates are complete, fire all queued events
        // (unless we're already in the process of firing queued events):

        if ((oldState & queuedEventsAreBeingFiredFlag) == 0)
        {
          //if (Model.TRACE.EVT && Model.traceInfo())
            //Model.TRACE.evt(1, "Firing all queued events");

          _eventManager.fireAllQueuedEvents();

          oldState = _stateFlags;

          synchronized(_lockSemaphore)
          {
            _stateFlags ^= queuedEventsAreBeingFiredFlag;
            _callbackThread = null;

            // While firing the "idle" event, we might find that the engine
            // becomes busy again. If it does then we'll want to veto the
            // event so that we don't mistakenly tell listeners that we're
            // idle when we're not. In order to veto the event, we save it
            // here so that it will be available when we detect that the
            // engine has become busy again.

            _idleEvent = new ModelStateChangedEvent(this,
                                                    this,
                                                    oldState,
                                                    _stateFlags,
                                                    requestCode
                                                   );

            // If there are any threads waiting to get the lock, wake one up
            // and give it another chance to get the lock.

            _lockSemaphore.countedNotify();
          }

          // Tell clients that callbacks are complete and we're now
          // returning to the "idle" state:

          //if (Model.TRACE.EVT && Model.traceInfo())
            //Model.TRACE.evt(4, "Firing ModelStateChangedEvent");

          _eventManager.fireEvent(_idleEvent,
                                  _eventListeners
                                 );
        }
      }
   }

   void setSavedObjectsAreBeingRestored(boolean savedObjectsAreBeingRestored)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".setSavedObjectsAreBeingRestored(" + savedObjectsAreBeingRestored + ")");

     byte oldState = _stateFlags;

     if (savedObjectsAreBeingRestored &&
         (_stateFlags & savedObjectsAreBeingRestoredFlag) == 0)
     {
        _stateFlags |= savedObjectsAreBeingRestoredFlag;

        _eventManager.fireEvent(new ModelStateChangedEvent(this,
							   this,
							   oldState,
							   _stateFlags,
							   -1 // requestCode N/A
						          ),
			        _eventListeners
			       );
     }
     else
     if ((_stateFlags & savedObjectsAreBeingRestoredFlag) != 0)
     {
	_stateFlags ^= savedObjectsAreBeingRestoredFlag;

	_eventManager.fireEvent(new ModelStateChangedEvent(this,
							   this,
							   oldState,
							   _stateFlags,
							   -1 // requestCode N/A
							  ),
				_eventListeners
			       );
     }

   }

   void setMostRecentReply(EPDC_Reply reply)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".setMostRecentReply(" + reply + ")");

     _mostRecentReply = reply;
   }

   EPDC_Reply getMostRecentReply()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getMostRecentReply()");

     return _mostRecentReply;
   }

   EPDC_Request getMostRecentRequest()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getMostRecentRequest()");

     return _mostRecentRequest;
   }

   void setRequestProperty(Object property)
   {
       _eventManager.setRequestProperty(property);
   }

   Object getRequestProperty()
   {
       return _eventManager.getRequestProperty();
   }

   EventManager getEventManager()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getEventManager()");

     return _eventManager;
   }

   /**
    * Returns the number of view types supported by this debug engine. This
    * information is available only after the debug engine has been
    * successfully initialized via the initialize() method.
    */

   public short numberOfSupportedViews()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.numberOfSupportedViews()");

     return (short)((_supportedViews == null) ? 0 : _supportedViews.length);
   }

   /**
    * Returns an array of ViewInformation objects, one for each kind of view
    * supported by this debug engine.
    * This information is available only after the debug engine has been
    * successfully initialized via the initialize() method.
    */

   public ViewInformation[] supportedViews()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.supportedViews()");

     return _supportedViews;
   }

   Representation[] representations()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.representations()");

     return _repNames;
   }

   /** Note that some elements in the returned array might be null.
    */

   public Language[] getLanguages()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getLanguages()");

     if (_languages == null)
         return null;

     int size = _languages.size();
     if (size == 0)
         return null;

     Language[] languages = new Language[size];

     _languages.copyInto(languages);

     return languages;
   }

   /**
    * Returns an array of DebuggeeException objects.
    * This information is available only after the debug engine has been
    * successfully initialized via the initialize() method.
    */

   public DebuggeeException[] getExceptions()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getExceptions()");

      return _exceptionInfo;
   }

   void setExceptions(DebuggeeException[] exceptions)
   {
     _exceptionInfo = exceptions;
   }

   /**
    * Get an update lock for the Model and make the lock valid for the
    * thread doing the call (i.e. the currently executing thread). There can
    * be at most one valid lock in existence per debug engine at any given time.
    * A new lock cannot be created if any of the following are true:
    * <ul>
    * <li>There already is a valid lock in existence. The thread holding that
    *     lock has not yet released it.
    * <li>The debug engine is currently busy
    * <li>The model is currently being updated
    * <li>Queued events are curently being fired to event listeners and the
    *     thread requesting the lock is not the thread on which event listeners
    *     are being called back.
    * </ul>
    * @param blockOnFail This parameter allows the caller to specify what
    * action should be taken if the requested lock cannot be created. A
    * value of 'false' means that the getLock method will simply return null
    * if the lock cannot be created. A value of 'true' means that the caller
    * wishes to wait until the lock can be created. The thread doing the
    * call will block until such time that the lock can be obtained.
    * <b>Client code should be very careful about specifying that the calling
    * thread should wait - if the conditions for creating a new lock (see
    * above) can only be met by having this thread continue, deadlock will
    * most definitely occur!</b>
    * @see DebugEngine#releaseLock
    */

   public ModelUpdateLock getLock(boolean blockOnFail)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".getLock(" + blockOnFail + ")");

     synchronized(_lockSemaphore)
     {
       if (!canCreateNewLock())
        if (blockOnFail)
        {
          // A waiting thread will be woken up whenever one of the
          // following occurs:
          // - some other thread releases the lock it has
          // - all queued events have been fired
          //
          // Either one of these events by itself is not enough to ensure
          // that a new lock can be created so we have to check each time:

          while (!canCreateNewLock())
          {
            _lockSemaphore.countedWait();
            // System.out.println("About to reset notifications");
            _lockSemaphore.resetNotifications();
          }
        }
        else
          return null;

       return _currentLock = new ModelUpdateLock();
     }
   }

   /**
    * Client code should call this method to release an update lock that
    * it currently owns. Releasing an update lock makes it possible for
    * other threads that might be using the Model to perform actions (i.e.
    * call methods in the Model) which would potentially cause the Model
    * to be updated. Locks should be released as soon as they are no longer
    * needed so that other clients of the Model can perform these actions.
    * @see DebugEngine#getLock
    */

   public void releaseLock(ModelUpdateLock lock)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".releaseLock(" + lock + ")");

     synchronized(_lockSemaphore)
     {
        lock.invalidate();

        if (lock == _currentLock) // Make sure we've been given the
        {                         // right lock before releasing it.
          _currentLock = null;

          // If there are any threads waiting to get the lock, wake one up
          // and give it another chance to get the lock:

          _lockSemaphore.countedNotify();
        }
     }
   }

   /**
    * Query the current state of the Model objects associated with this
    * DebugEngine.
    * @return A set of flags which indicates the current state of the Model.
    * @see DebugEngine#debugEngineIsBusyFlag
    * @see DebugEngine#modelIsBeingUpdatedFlag
    * @see DebugEngine#queuedEventsAreBeingFiredFlag
    * @see DebugEngine#isBusy
    * @see DebugEngine#modelIsBeingUpdated
    * @see DebugEngine#queuedEventsAreBeingFired
    * @see DebugEngine#isAcceptingSynchronousRequests
    * @see DebugEngine#isAcceptingAsynchronousRequests
    * @see DebugEngineEventListener#modelStateChanged
    * @see ModelStateChangedEvent
    */

   public byte getState()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getState()");

     return _stateFlags;
   }

   synchronized boolean canCreateNewLock()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.canCreateNewLock()");

     return _currentLock == null && !isBusy() && !modelIsBeingUpdated() &&
            (!queuedEventsAreBeingFired() || Thread.currentThread() == _callbackThread);
   }

   ViewInformation getViewInformation(short index)
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getViewInformation(" + index + ")");

     return _supportedViews[index - 1];
   }

   private void add(ViewInformation viewInformation, short viewNumber)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".add(" + viewInformation.name() + ", " + viewNumber + ")");

     // Add it to the array which is indexable by view number as well as to
     // the vector that is indexable by view type:

     _supportedViews[viewNumber] = viewInformation;
     setVectorElementToObject(viewInformation, _viewsByType, viewInformation.kind());
   }

   /**
    * Get the ViewInformation object for a particular kind of view e.g.
    * source, disassembly, etc.
    * @param viewType The kind of view requested. The possible values for this
    * argument are defined as constants in EPDC e.g. EPDC.View_Class_Source.
    * @return The ViewInformation object corresponding to the given view
    * type. Will return null if the debug engine does not support the given
    * type of view.
    * @see com.ibm.debug.epdc.EPDC#View_Class_Source
    */

   public ViewInformation getViewInformationByType(short viewType)
   {
     try
     {
        return (ViewInformation)(_viewsByType.elementAt(viewType));
     }
     catch (ArrayIndexOutOfBoundsException excp)
     {
       return null;
     }
   }

   /**
    * Get the ViewInformation object for the Source view. Will return null if the
    * debug engine does not support Source views.
    */

   public ViewInformation getSourceViewInformation()
   {
     return getViewInformationByType(EPDC.View_Class_Source);
   }

   /**
    * Get the ViewInformation object for the Disassembly view. Will return null if the
    * debug engine does not support Disassembly views.
    */

   public ViewInformation getDisassemblyViewInformation()
   {
     return getViewInformationByType(EPDC.View_Class_Disasm);
   }

   /**
    * Get the ViewInformation object for the Mixed view. Will return null if the
    * debug engine does not support Mixed views.
    */

   public ViewInformation getMixedViewInformation()
   {
     return getViewInformationByType(EPDC.View_Class_Mixed);
   }

   /**
    * Get the ViewInformation object for the Listing view. Will return null if the
    * debug engine does not support Listing views.
    */

   public ViewInformation getListingViewInformation()
   {
     return getViewInformationByType(EPDC.View_Class_Listing);
   }

   /**
    * Get the Language object that matches a language id in the engine.
    * @param index The language ID from the list of languages that the
    * current engine supports.
    */
   public Language getLanguageInfo(int index)
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getLanguageInfo(" + index + ")");

     try
     {
        return (Language)(_languages.elementAt(index));
     }
     catch (ArrayIndexOutOfBoundsException excp)
     {
       return null;
     }
   }

   void add(Language lang, int index)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".add(Language:" + lang.name() + ", " + index + ")");

     if (_languages == null)
        _languages = new Vector();

      // EPDC assigns language ids starting from 1 not 0
      setVectorElementToObject(lang, _languages, index);
   }

   Representation getRepresentation(int index)
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
        //Model.TRACE.dbg(4, "DebugEngine.getRepresentation(" + index + ")");

     return _repNames[index];
   }

   private void add(Representation rep, short index)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".add(Representation:" + rep.name() + ", " + index + ")");

      _repNames[index] = rep;
   }

   private void add(DebuggeeException exception, int index)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, this + ".add(DebuggeeException:" + exception.name() + ", " + index + ")");

      _exceptionInfo[index] = exception;
   }

   void prepareToDie()
   {
      if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, this + ".prepareToDie()");

      if (_EPDCReplyProcessorThread != Thread.currentThread())
         _EPDCReplyProcessorThread.stop();

      try
      {
        _connection.close();
      }
      catch (java.io.IOException excp)
      {
      }

      setHasBeenDeleted();

      _eventManager.addEvent(new DebugEngineTerminatedEvent(this,
                                               this,
                                               _mostRecentReply != null ? _mostRecentReply.getReplyCode() :
                                               (_mostRecentRequest != null ? _mostRecentRequest.requestCode() :
                                               -1)
                                              ),
                             _eventListeners
                            );
   }

   void tellChildrenThatOwnerHasBeenDeleted()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.tellChildrenThatOwnerHasBeenDeleted()");

     int m;

     if (_process != null)
        _process.setOwnerHasBeenDeleted();

     if (_registerGroups != null)
        for (m=0; m<_registerGroups.size(); m++)
          if (_registerGroups.elementAt(m) != null)
            ((RegisterGroup)_registerGroups.elementAt(m)).setOwnerHasBeenDeleted();

     if (_stackDetailColumns != null)
        for (m=0; m<_stackDetailColumns.size(); m++)
          if (_stackDetailColumns.elementAt(m) != null)
            ((StackColumnDetails)_stackDetailColumns.elementAt(m)).setOwnerHasBeenDeleted();

     if (_exceptionInfo != null)
        for (m=0; m<_exceptionInfo.length; m++)
          if (_exceptionInfo[m] != null)
            _exceptionInfo[m].setOwnerHasBeenDeleted();
   }

   /**
    * Get this debug engine's set of capabilities. The capabilities will be
    * null if the engine has not yet been initialized via the 'initialize'
    * method. A debug engine's capabilities can change during a debug
    * session. Client code is notified of such changes via the event listener
    * mechanism.
    * @see EngineCapabilities
    * @see EngineCapabilitiesGroup
    * @see EngineCapabilitiesChangedEvent
    * @see DebugEngineEventListener#engineCapabilitiesChanged
    */

   public EngineCapabilities getCapabilities()
   {
     //if (Model.TRACE.DBG && Model.traceInfo())
       //Model.TRACE.dbg(4, "DebugEngine.getCapabilities()");

     return _capabilities;
   }

   void setCapabilities(EngineCapabilities newCapabilities)
   {
      if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(4, this + ".setCapabilities(" + newCapabilities + ")");

      EngineCapabilities oldCapabilities = _capabilities;
      _capabilities = newCapabilities;

      //if (Model.TRACE.EVT && Model.traceInfo())
        //Model.TRACE.evt(4, "Adding EngineCapabilitiesChangedEvent");

      _eventManager.addEvent(new EngineCapabilitiesChangedEvent(this,
                                               this,
                                               oldCapabilities,
                                               newCapabilities,
                                               _mostRecentReply.getReplyCode()
                                              ),
                             _eventListeners
                            );
   }

  /**
   * Returns details of the stack information that the backend supports.
   * @return an array of StackColumnDetails objects if the epdc request is sent
   * successfully or the array is already available, 'null' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see StackColumnDetails
   */
  public StackColumnDetails[] getStackDetails()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, this + ".getStackDetails()");

    if (_stackDetailColumns == null)
    {
      if (!prepareForEPDCRequest(EPDC.Remote_StackDetailsGet, sendReceiveSynchronously))
        return null;

      if (Model.checkFCTBit)
      {
          if (!getCapabilities().getWindowCapabilities().monitorStackSupported())
          {
              cancelEPDCRequest(EPDC.Remote_StackDetailsGet);
              return null;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(3, "Sending EPDC request: Remote_StackDetailsGet");

      if (!processEPDCRequest(new EReqStackDetailsGet(), sendReceiveSynchronously))
        return null;

      ERepStackDetailsGet reply = (ERepStackDetailsGet)getMostRecentReply();

      if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
        return null;

      Vector epdcColumns = reply.getColumnInfo();
      int[] defaultColumnIds = reply.getDefaultColumnIds();

      if (epdcColumns != null && (_numberOfStackDetailColumns = epdcColumns.size()) > 0)
      {
        _stackDetailColumns = new Vector();

        for (int i = 0; i < _numberOfStackDetailColumns; i++)
        {
          ERepGetStackColumns epdcColDetails = (ERepGetStackColumns)epdcColumns.elementAt(i);
          StackColumnDetails stackCol = new StackColumnDetails(epdcColDetails);
          setVectorElementToObject(stackCol, _stackDetailColumns, stackCol.getColumnID());
        }
      }

      // mark default StackColumnDetails objects
      if (defaultColumnIds != null && defaultColumnIds.length > 0)
        for (int j = 0; j < defaultColumnIds.length; j++)
          ((StackColumnDetails)_stackDetailColumns.elementAt(defaultColumnIds[j])).setDefault();
    }

    // Create an array of StackColumnDetails
    if (_stackDetailColumns == null || _stackDetailColumns.size() == 0)
      return null;
    else
    {
      StackColumnDetails[] columns = new StackColumnDetails[_numberOfStackDetailColumns];
      int n = 0;
      for (int m=0; m<_stackDetailColumns.size(); m++)
        if (_stackDetailColumns.elementAt(m) != null)
        {
          columns[n] = (StackColumnDetails)_stackDetailColumns.elementAt(m);
          n++;
        }
      return columns;
    }
  }

  /**
   * Returns an array of register groups that the backend supports.
   * @return an array of RegisterGroup objects if the epdc request is sent
   * successfully or the array is already available, 'null' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see RegisterGroup
   */
  public RegisterGroup[] getRegisterGroups()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, this + ".getRegisterGroups()");

    if (_registerGroups == null)
    {
      //if (Model.TRACE.EVT && Model.traceInfo())
        //Model.TRACE.evt(4, "Preparing to send EPDC request for Remote_RegistersDetailsGet");

      if (!prepareForEPDCRequest(EPDC.Remote_RegistersDetailsGet, sendReceiveSynchronously))
      {
        //if (Model.TRACE.ERR && Model.traceInfo())
          //Model.TRACE.err(2, "Fail in prepareForEPDCRequest");
        return null;
      }

      if (Model.checkFCTBit)
      {
          if (!getCapabilities().getWindowCapabilities().monitorRegistersSupported())
          {
              //if (Model.TRACE.ERR && Model.traceInfo())
              //Model.TRACE.err(2, "Registers not supported by BE");

              cancelEPDCRequest(EPDC.Remote_RegistersDetailsGet);
              return null;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Sending EPDC request: Remote_RegistersDetailsGet");

      if (!processEPDCRequest(new EReqRegistersDetailsGet(), sendReceiveSynchronously))
      {
        //if (Model.TRACE.ERR && Model.traceInfo())
          //Model.TRACE.err(2, "Fail in processEPDCRequest");
        return null;
      }

      ERepRegistersDetailsGet reply = (ERepRegistersDetailsGet)getMostRecentReply();

      if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
      {
        //if (Model.TRACE.ERR && Model.traceInfo())
          //Model.TRACE.err(2, "No Reply");
        return null;
      }

      Vector groups = reply.getGroupInfo();
      int[] defaultGroupIds = reply.getDefaultGroupIds();

      if (groups != null && (_numberOfRegisterGroups = groups.size()) > 0)
      {
        _registerGroups = new Vector();

        for (int i = 0; i < _numberOfRegisterGroups; i++)
        {
          ERepGetRegistersGroups group = (ERepGetRegistersGroups)groups.elementAt(i);
          RegisterGroup regGroup = new RegisterGroup(group);
          setVectorElementToObject(regGroup, _registerGroups, regGroup.getGroupID());
        }
      }
      else
      {
        //if (Model.TRACE.ERR && Model.traceInfo())
          //Model.TRACE.err(2, "Unable to get register groups");
      }

      // mark default RegisterGroup objects
      if (defaultGroupIds != null && defaultGroupIds.length > 0)
        for (int j = 0; j < defaultGroupIds.length; j++)
          ((RegisterGroup)_registerGroups.elementAt(defaultGroupIds[j])).setDefault();
    }

    // Create an array of RegisterGroup objects
    if (_registerGroups == null || _registerGroups.size() == 0)
    {
      //if (Model.TRACE.ERR && Model.traceInfo())
        //Model.TRACE.err(2, "No register groups available");
      return null;
    }
    else
    {
      RegisterGroup[] regGroups = new RegisterGroup[_numberOfRegisterGroups];
      int n = 0;
      for (int m=0; m<_registerGroups.size(); m++)
        if (_registerGroups.elementAt(m) != null)
        {
          regGroups[n] = (RegisterGroup)_registerGroups.elementAt(m);
          n++;
        }
      return regGroups;
    }
  }

  RegisterGroup getRegisterGroup(int id)
  throws java.io.IOException
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "DebugEngine.getRegisterGroup(" + id + ")");

    getRegisterGroups();  // make sure we have asked BE for register groups

    if (_registerGroups == null)
      return null;

    try
    {
      return (RegisterGroup)(_registerGroups.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      return null;
    }
  }

  /**
   * This method is called when a switch to another view is requested.
   * @param sourceLocation the location that needs to be switched to another view.
   * @param viewInformation the required view.
   * @return a Location object if the epdc request is processed successfully and 'null'
   * otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see Location
   */

  public Location switchView(Location sourceLocation, ViewInformation targetView)
  throws IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, this + ".switchView(" + sourceLocation + ", " + targetView.name() + ")");

    if (sourceLocation == null || targetView == null)
       return null;

    return switchView(sourceLocation.getEStdView(), targetView);
  }

  Location switchView(EStdView sourceLocation, ViewInformation targetView)
  throws IOException
  {
    if (sourceLocation == null || targetView == null)
       return null;

    short viewNum = targetView.index();

    if (viewNum == sourceLocation.getViewNo())
       try
       {
         return new Location(_process, sourceLocation);
       }
       catch (LocationConstructionException excp)
       {
         return null;
       }

    if (!prepareForEPDCRequest(EPDC.Remote_ContextConvert, sendReceiveSynchronously))
      return null;

    if (!processEPDCRequest(new EReqContextConvert(sourceLocation, viewNum), sendReceiveSynchronously))
      return null;

    ERepContextConvert reply = (ERepContextConvert)getMostRecentReply();

    if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
      return null;

    try
    {
      return new Location(_process, reply.context());
    }
    catch (LocationConstructionException excp)
    {
      return null;
    }
  }

  /**
   * This method is the same as changeExceptionStatus(), but
   * in addition to changing the exception status, it will also save the
   * the status of the exceptions as debuggee-specific defaults and,
   * optionally, as engine-specific defaults as well.
   * @param saveAsDebuggerDefaults Pass 'true' if the status of the
   * exceptions are to be saved as the engine-specific defaults, otherwise pass 'false'.
   */

  public boolean commitPendingExceptionStateChanges(boolean saveAsDebuggerDefaults)
  throws IOException
  {
     if (!changeExceptionStatus())
        return false;

     RestorableObjects restorableObjects = null;

     if (_process != null)
        restorableObjects = _process.getRestorableObjects();

     _saveAndRestoreExceptionFilters = true;

     if (restorableObjects != null &&
         ((restorableObjects.getSaveFlags() & SaveRestoreFlags.EXCEPTION_FILTERS) != 0))
           restorableObjects.save(true); // true == asynchronously

     if (_process != null &&
         saveAsDebuggerDefaults &&
         (restorableObjects = DebugEngine.findOrCreateEngineSpecificRestorableObjects(_process)) != null)
        restorableObjects.resave(_exceptionInfo);

     return true;
  }

  /**
   * Change the state (enabled or disabled) of every DebuggeeException
   * object contained in this DebugEngine. After a successful call to
   * this method, the pending state of each exception will become the
   * current state. If this method is not successful at updating the
   * state of the exceptions (i.e. it returns false), they will retain
   * whatever state they had before the call.
   * @see DebuggeeException
   * @see DebugEngine#getExceptions()
   */

  public boolean changeExceptionStatus()
  throws IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, this + ".changeExceptionStatus()");

    if (_exceptionInfo != null && _exceptionInfo.length != 0)
    {
      if (!prepareForEPDCRequest(EPDC.Remote_ExceptionStatusChange, sendReceiveSynchronously))
         return false;

      if (Model.checkFCTBit)
      {
         if (!getCapabilities().getExceptionCapabilities().exceptionFilterSupported())
         {
             cancelEPDCRequest(EPDC.Remote_ExceptionStatusChange);
             return false;
         }
      }

      int numExceptions = _exceptionInfo.length, i;
      int[] flags = new int[numExceptions];
      for (i=0; i<numExceptions; i++)
          flags[i] = _exceptionInfo[i].getPendingState();

      if (!processEPDCRequest(new EReqExceptionStatusChange(flags), sendReceiveSynchronously))
         return false;

      ERepExceptionStatusChange reply = (ERepExceptionStatusChange)getMostRecentReply();

      if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
         return false;

      for (i=0; i<numExceptions; i++)
          _exceptionInfo[i].commitPendingStateChange();

      return true;
    }
    else
      return false;
  }

  public DebuggeeStartupOptions getDebuggeeStartupOptions()
  {
    return _debuggeeStartupOptions;
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
          // Write those objects that restorable objects might depend
          // on in order to be restored properly.

          stream.writeObject(_supportedViews);

	  if ((flags & SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS) != 0)
          {
             // Note: Due to a defect in Java's serialization API (defect
             // # 4065313) we're saving more objects than we need to :
             // I wanted to use
             // ObjectOutputStream.replaceObject to write out null if the
             // Language object did not need to be saved. However, this does
             // not work because of the above mentioned bug. (Apparently
             // the bug has been fixed in JDK 1.2). See also the note in
             // ModelObjectOutputStream.java. Other workarounds are possible
             // in order to minimize the amount of info that gets written
             // out but may not be worth the trouble.

	     stream.writeObject(_languages);
          }

	  if ((flags & SaveRestoreFlags.EXCEPTION_FILTERS) != 0)
             if (_saveAndRestoreExceptionFilters)
	        stream.writeObject(_exceptionInfo);
             else
	        stream.writeObject((DebuggeeException[])null);
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
          // Read those objects that restorable objects might depend
          // on in order to be restored properly.

          _supportedViews = (ViewInformation[])stream.readObject();

	  if ((flags & SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS) != 0)
             _languages = (Vector)stream.readObject();

	  if ((flags & SaveRestoreFlags.EXCEPTION_FILTERS) != 0)
	     _exceptionInfo = (DebuggeeException[])stream.readObject();
       }
    }
    else
       stream.defaultReadObject();
  }

  void clearAllChangeFlags()
  {
    _changeFlags = 0;
  }

  void setDefaultDataRepresentationsHaveChanged(boolean haveChanged)
  {
    if (haveChanged)
       _changeFlags |= DEFAULT_DATA_REPRESENTATIONS_HAVE_CHANGED;
    else
    if ((_changeFlags & DEFAULT_DATA_REPRESENTATIONS_HAVE_CHANGED) != 0)
       _changeFlags ^= DEFAULT_DATA_REPRESENTATIONS_HAVE_CHANGED;
  }

  void setExceptionFiltersHaveChanged(boolean haveChanged)
  {
    if (haveChanged)
       _changeFlags |= EXCEPTION_FILTERS_HAVE_CHANGED;
    else
    if ((_changeFlags & EXCEPTION_FILTERS_HAVE_CHANGED) != 0)
       _changeFlags ^= EXCEPTION_FILTERS_HAVE_CHANGED;
  }

  /**
   * Determine if the most recent reply from the debug engine caused any
   * changes to this debug engine's default data representations.
   */

  public boolean defaultDataRepresentationsHaveChanged()
  {
    return (_changeFlags & DEFAULT_DATA_REPRESENTATIONS_HAVE_CHANGED) != 0;
  }

  /**
   * Determine if the most recent reply from the debug engine caused any
   * changes to this debug engine's exception filters.
   */

  public boolean exceptionFiltersHaveChanged()
  {
    return (_changeFlags & EXCEPTION_FILTERS_HAVE_CHANGED) != 0;
  }

  static RestorableObjects findOrCreateEngineSpecificRestorableObjects(DebuggeeProcess process)
  {
    DebugEngine engine = process.debugEngine();

    char[] mnemonics = new char[2];
    mnemonics[0] = Host.getPlatformMnemonic(engine.getPlatformID());
    mnemonics[1] = getEngineTypeMnemonic(engine.getEngineID());

    String key = new String(mnemonics);

    RestorableObjects engineSpecificRestorableObjects =
                      (RestorableObjects)_engineSpecificRestorableObjects.get(key);

    if (engineSpecificRestorableObjects == null)
    {
       // Create a RestorableObjects object with save/restore flags initially
       // set to 0. If the file does not yet exist we'll do a save just to
       // get the basic graph structure saved within the file, then we'll
       // set the save/restore flags the way we want them for subsequent
       // saves and restores:

       String fileName = "esro" + ".@" + key;

       // See if we need to qualify the file name with a directory name:

       DebuggeeStartupOptions debuggeeStartupOptions = engine.getDebuggeeStartupOptions();

       if (debuggeeStartupOptions != null)
       {
          String saveRestoreDirectory = debuggeeStartupOptions.getSaveRestoreDirectory();

          if (saveRestoreDirectory != null && !saveRestoreDirectory.equals(""))
          {
             String fileSeparator = System.getProperty("file.separator");

             if (!saveRestoreDirectory.endsWith(fileSeparator))
                saveRestoreDirectory += fileSeparator;

             fileName = saveRestoreDirectory + fileName;
          }
       }

       engineSpecificRestorableObjects =
             new PersistentRestorableObjects(process,
                                             0,
                                             fileName);

       if (!engineSpecificRestorableObjects.getSerialization().canBeDeserialized())
          try
          {
            // Prime it with just the "infrastructure" objects:

            engineSpecificRestorableObjects.save();
          }
          catch (java.io.IOException excp)
          {
          }

       engineSpecificRestorableObjects.setSaveFlags(SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS |
                                                    SaveRestoreFlags.EXCEPTION_FILTERS);

       engineSpecificRestorableObjects.setRestoreFlags(SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS |
                                                       SaveRestoreFlags.EXCEPTION_FILTERS |
                                                       SaveRestoreFlags.SECONDARY_RESTORE);

       _engineSpecificRestorableObjects.put(key, engineSpecificRestorableObjects);
    }

    return engineSpecificRestorableObjects;
  }

  boolean getSaveAndRestoreExceptionFilters()
  {
    return _saveAndRestoreExceptionFilters;
  }

  void setSaveAndRestoreExceptionFilters(boolean saveAndRestoreExceptionFilters)
  {
    _saveAndRestoreExceptionFilters = saveAndRestoreExceptionFilters;
  }

  static char getEngineTypeMnemonic(short engineID)
  {
    return _engineTypeMnemonics[engineID];
  }

  /**
   * Send a request to get a list of parts (that have not been registered by
   * the backend) from the backend. This request must be sent synchronously.
   * @param module The Module object that contains the part(s). If the module
   * Object is null, the debug engine will search all modules available (not
   * necessarily loaded) to find a part that matches the name specified.
   * @param name The name of the part that will be loaded
   * @return A Vector of part objects that match the name provided, or null
   * if no match is found
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public Vector loadParts(Module module, String name)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(3, this + ".loadParts(" + name + ")");

    if (!prepareForEPDCRequest(EPDC.Remote_PartOpen, sendReceiveSynchronously))
        return null;

    // If module object provided is null, the model assumes the module id of
    // zero to represent all modules available.
    int id = 0;
    if (module != null)
        id = module.id();

    EReqPartOpen request = new EReqPartOpen(id, name);

    if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(3, "Sending EPDC request Remote_PartOpen");

    if (!processEPDCRequest(request, sendReceiveSynchronously))
        return null;

    ERepPartOpen reply = (ERepPartOpen)getMostRecentReply();

    if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
        return null;

    Vector epdcPartIDs = reply.getPartIDs();

    int numberOfPartIDs = 0;

    if (epdcPartIDs == null || (numberOfPartIDs = epdcPartIDs.size()) == 0)
        return null;

    // Build the newParts Vector
    Part newPart = null;
    Short partIdObject = null;
    short partID = 0;
    Vector parts = new Vector(numberOfPartIDs);

    for (int i = 0; i < numberOfPartIDs; i++)
    {
         partIdObject = (Short)(epdcPartIDs.elementAt(i));
         if (partIdObject != null)
         {
             partID = partIdObject.shortValue();
             newPart = process().getPart(partID);

             if (newPart != null)
                 setVectorElementToObject(newPart, parts, partID);
         }
    }

    return parts;
  }

  Client getCurrentClient()
  {
    return _currentClient;
  }

  ModelEventListener getPrivilegedEventListener()
  {
    return _privilegedListener;
  }

  EStdView resolveFunction(ERepEntryGetNext epdcEntry)
  throws java.io.IOException
  {
    if (prepareForEPDCRequest(EPDC.Remote_EntryWhere, sendReceiveSynchronously) &&
	processEPDCRequest(new EReqEntryWhere(epdcEntry.getEntryID()),
                           sendReceiveSynchronously))
    {
       ERepEntryWhere reply = (ERepEntryWhere)getMostRecentReply();

       if (reply.getReturnCode() == EPDC.ExecRc_OK)
       {
	  Vector epdcLocations = reply.getContexts();

	  if (epdcLocations != null)
	  {
	     for (int j = 0; j < epdcLocations.size(); j++)
             {
		 EStdView epdcLocation = (EStdView)epdcLocations.elementAt(j);

		 if (epdcLocation != null && epdcLocation.isComplete())
		    return epdcLocation;

	     } // End of loop through contexts
	  }
       }
    }

    return null;
  }

  byte getDominantLanguage()
  {
    return _dominantLanguage;
  }

   void saveErrorOccurredEventForFileNotFound(ErrorOccurredEvent event)
   {
     _errorOccurredEventForFileNotFound = event;
   }

   void fireErrorOccurredEventForFileNotFound()
   {
     _eventManager.fireEvent(_errorOccurredEventForFileNotFound, _eventListeners);
   }

   /**
    * Provide the local source path list (residing on the frontend). Therefore,
    * for every view file not residing on the engine, the local source path
    * list specified by the user can be used to search for the file.
    * @param path The path list to search for the local source file
    */
   public void setLocalSourcePath(String path)
   {
     _localSourcePath = path;
   }

   String getLocalSourcePath()
   {
     if (_localSourcePath == null || _localSourcePath.length() == 0)
         return null;

     return _localSourcePath;
   }

   /**
    * Inidicates that this engine can generally be reused. Note, however,
    * that before actually reusing the engine, certain other conditions must
    * also be met e.g. it must not currently be debugging a process, etc.
    */

   public boolean canBeReused()
   {
     // TODO: Check the "startup" FCT bit (and give it precedence).

     return Model.willReuseDebugEngines();
   }

   /**
    * Add a module, in AS400 a module is called a program, which can have
    * different types (class, which is for java classes, or service
    * for dlls and default for the rest of module types).
    * @param programName The name of the module to be added
    * @param programType The type of the module
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendReceiveSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return An indication of whether or not the ModuleAdd request was
    * sent to the debug engine successfully.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */
   public boolean addProgram(String programName,
                             int programType,
                             int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(3, this + ".addProgram(" + programName + ", " +
                         programType + ")");

     if (!prepareForEPDCRequest(EPDC.Remote_ModuleAdd,
                                sendReceiveSynchronously))
         return false;

     if (Model.checkFCTBit)
     {
         if (!getCapabilities().getFileCapabilities().moduleAddSupported())
         {
             cancelEPDCRequest(EPDC.Remote_ModuleAdd);
             return false;
         }
     }

     EReqModuleAdd request = new EReqModuleAdd(programName, programType);

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(3, "Sending EPDC request Remote_ModuleAdd");

     if (!processEPDCRequest(request, sendReceiveControlFlags))
         return false;

     return true;
   }

   /**
    * Remove a module, this is only applicable to AS400 program (module)
    * names. The module names in AS400 are unique, the name consists of the
    * type of the module plus the name of the module.
    * @param programName The name of the module
    * @param sendReceiveControlFlags A set of flags which specify the mode
    * in which this request is to be performed. There is a set of constants
    * in DebugEngine which define the possible values for this argument.
    * For example, a value of DebugEngine.sendReceiveDefault means that the
    * request is to be done asynchronously, while a value of
    * DebugEngine.sendReceiveSynchronously
    * means that the request is to be performed synchronously.
    * <p>When done asynchronously,
    * this method will return immediately after sending the request to the
    * debug engine without waiting for a response from
    * the debug engine. The response to the request will be
    * received on a separate thread and client code will be notified of the
    * the response via the event listener mechanism.
    * @return An indication of whether or not the ModuleRemove request was
    * sent to the debug engine successfully.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */
   public boolean removeProgram(String programName, int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(3, this + ".removeProgram(" + programName + ")");

     if (!prepareForEPDCRequest(EPDC.Remote_ModuleRemove,
                                sendReceiveSynchronously))
         return false;

     if (Model.checkFCTBit)
     {
         if (!getCapabilities().getFileCapabilities().moduleRemoveSupported())
         {
             cancelEPDCRequest(EPDC.Remote_ModuleRemove);
             return false;
         }
     }

     Vector modules = process().getModules(programName);

     if (modules == null || modules.size() == 0)
         return false;

     Module module = null;
     int programId = 0;   //programId from EPDC can never be zero

     for (int i = 0; i < modules.size(); i++)
     {
          module = (Module)modules.elementAt(i);
          if (module != null)
          {
              programId = module.id();
              break;
          }
     }

     EReqModuleRemove request = new EReqModuleRemove(programId);

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(3, "Sending EPDC request Remote_ModuleRemove");

     if (!processEPDCRequest(request, sendReceiveControlFlags))
         return false;

     return true;
   }

   /**
    * Get The list of jobs (processes). This request is only applicable to
    * AS/400.
    * @param jobQualification The string to filter the list of jobs, the value
    * null for this parameter, indicates that the request is to get the list of
    * all possible jobs from the engine.
    * @return An array of jobs, or null if the request was not sent
    * successfully or the engine reported an error
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */
   public String[] getJobsList(String jobQualification)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(3, this + ".getJobsList(" + jobQualification + ")");

     if (!prepareForEPDCRequest(EPDC.Remote_JobsListGet,
                                sendReceiveSynchronously))
         return null;

     EReqJobsListGet request = new EReqJobsListGet(jobQualification);

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(3, "Sending EPDC request Remote_JobsListGet");

     if (!processEPDCRequest(request, sendReceiveSynchronously))
         return null;

     ERepJobsListGet reply = (ERepJobsListGet)getMostRecentReply();

     if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
         return null;

     String[] jobNames = reply.getJobNames();

     return jobNames;
   }

   /**
    * Determine if date breakpoints are currently enabled in the debug
    * engine.
    */

   public boolean getDateBreakpointsEnabled()
   {
     return _dateBreakpointsEnabled;
   }

   /**
    * Determine if auto-setting of entry breakpoints is currently enabled in the debug
    * engine.
    */

   public boolean getEntryBreakpointsAutoSetEnabled()
   {
     return _entryBreakpointsAutoSetEnabled;
   }

   /**
    * Tell the debug engine to enable or disable date breakpoints. Only call
    * this method if the engine has indicated that it supports date
    * breakpoints via the EngineBreakpointCapabilities.dateBreakpointsSupported()
    * method.
    * <p>Note: Date breakpoints and auto-set entry breakpoints can both
    * be enabled/disabled at the same time by calling enableSpecialBreakpoints()
    * which is slightly more efficient than calling enableDateBreakpoints()
    * followed by enableEntryBreakpointsAutoSet() since the former requires
    * sending only a single request to the debug engine whereas the latter
    * two calls require sending two requests.
    * @param enable Pass 'true' to enable this kind of breakpoint and pass
    * 'false' to disable this kind of breakpoint.
    * @return 'true' if the request was processed successfully, otherwise
    * 'false'
    * @exception java.io.IOException If there is a problem communicating with
    * the debug engine.
    * @see DebugEngine#enableSpecialBreakpoints
    * @see EngineBreakpointCapabilities#dateBreakpointsSupported
    */

   public boolean enableDateBreakpoints(boolean enable)
   throws java.io.IOException
   {
     return enableSpecialBreakpoints(enable, _entryBreakpointsAutoSetEnabled);
   }

   /**
    * Tell the debug engine to enable or disable auto-set entry breakpoints.
    * Only call
    * this method if the engine has indicated that it supports auto-set entry
    * breakpoints via the EngineBreakpointCapabilities.entryBreakpointsAutoSetSupported()
    * method.
    * <p>Note: Date breakpoints and auto-set entry breakpoints can both
    * be enabled/disabled at the same time by calling enableSpecialBreakpoints()
    * which is slightly more efficient than calling enableDateBreakpoints()
    * followed by enableEntryBreakpointsAutoSet() since the former requires
    * sending only a single request to the debug engine whereas the latter
    * two calls require sending two requests.
    * @param enable Pass 'true' to enable this kind of breakpoint and pass
    * 'false' to disable this kind of breakpoint.
    * @return 'true' if the request was processed successfully, otherwise
    * 'false'
    * @exception java.io.IOException If there is a problem communicating with
    * the debug engine.
    * @see DebugEngine#enableSpecialBreakpoints
    * @see EngineBreakpointCapabilities#entryBreakpointsAutoSetSupported
    */

   public boolean enableEntryBreakpointsAutoSet(boolean enable)
   throws java.io.IOException
   {
     return enableSpecialBreakpoints(_dateBreakpointsEnabled, enable);
   }

   /**
    * Enable or disable both kinds of "special" breakpoints with a single
    * method call.
    * @param enableDateBreakpoints Pass 'true' to enable date breakpoints and pass
    * 'false' to disable date breakpoints.
    * @param enableEntryBreakpointsAutoSet Pass 'true' to enable auto-set entry breakpoints and pass
    * 'false' to disable auto-set entry breakpoints.
    * @return 'true' if the request was processed successfully, otherwise
    * 'false'
    * @exception java.io.IOException If there is a problem communicating with
    * the debug engine.
    * @see DebugEngine#enableDateBreakpoints
    * @see DebugEngine#enableEntryBreakpointsAutoSet
    * @see EngineBreakpointCapabilities#dateBreakpointsSupported
    * @see EngineBreakpointCapabilities#entryBreakpointsAutoSetSupported
    */

   public boolean enableSpecialBreakpoints(boolean enableDateBreakpoints,
                                           boolean enableEntryBreakpointsAutoSet)
   throws java.io.IOException
   {
     if (!prepareForEPDCRequest(EPDC.Remote_BreakpointEntryAutoSet2,
                                sendReceiveSynchronously))
         return false;

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(3, "Sending EPDC request Remote_BreakpointEntryAutoSet2");

     if (!processEPDCRequest(new EReqBreakpointEntryAutoSet2(enableEntryBreakpointsAutoSet,
                                                             enableDateBreakpoints),
                             sendReceiveSynchronously))
         return false;

     EPDC_Reply reply = getMostRecentReply();

     if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
        return false;
     else
     {
        _dateBreakpointsEnabled = enableDateBreakpoints;
        _entryBreakpointsAutoSetEnabled = enableEntryBreakpointsAutoSet;

        return true;
     }
   }

   int getEPDCVersion()
   {
     return _engineSession._negotiatedEPDCVersion;
   }

   public int getMaximumViewFileCacheSize()
   {
     return _maximumViewFileCacheSize;
   }

   /**
    * Set the maximum number of lines that the Model will save for each
    * ViewFile when it reads lines of source (or disassembly, etc.)
    * from the debug engine. This will effect the performance of the
    * debugger - a small cache size will mean that lines will be purged
    * more frequently and must therefore be re-read from the engine if needed again,
    * whereas a large cache size will increase the
    * memory required in the Model to store the lines.
    * <p>The default cache size is 500 lines.
    * <p>Calling this method will affect the cache size for <i>new</i>
    * ViewFile objects only - it will have no effect on the cache size in
    * existing ViewFile objects.
    * @param maximumViewFileCacheSize The maximum number of lines the Model
    * will save in its cache for each ViewFile object. The value of this argument
    * must be > 0.
    */

   public void setMaximumViewFileCacheSize(int maximumViewFileCacheSize)
   {
     if (maximumViewFileCacheSize <= 0)
        return;

     _maximumViewFileCacheSize = maximumViewFileCacheSize;
   }

   /**
    * Remove references so they can be gc'ed.
    */
   void cleanup()
   {
     _localSourcePath = null;
     _host = null;
     _connection = null;

     _EPDCReplyProcessor.cleanup();
     _EPDCReplyProcessor = null;

     _EPDCReplyProcessorThread = null;
     _process = null;
     _viewsByType = null;
     _supportedViews = null;
     _eventListeners.removeAllElements();
     _replySemaphore = null;
     _lockSemaphore = null;
     _requestSemaphore = null;
     _mostRecentReply = null;
     _currentLock = null;
     _eventManager = null;
     _callbackThread = null;
     _capabilities = null;
     _mostRecentRequest = null;
     _currentClient = null;
     _privilegedListener = null;
     //reused _languages = null;
     _repNames = null;
     //reused _exceptionInfo = null;
     _stackDetailColumns = null;
     _registerGroups = null;
     _processListColumnDetails = null;
     _debuggeeStartupOptions = null;
     _errorOccurredEventForFileNotFound = null;
     _idleEvent = null;
     _engineSession = null;
   }

   private boolean _dateBreakpointsEnabled = false;
   private boolean _entryBreakpointsAutoSetEnabled = false;

   private String _localSourcePath;
   private boolean _isLoaded = false;
   private boolean _hasBeenInitialized = false;
   private Host _host;

   /** A mechanism for communicating between SUI and the debug engine.
    */

   private Connection _connection;
   private EPDCReplyProcessor _EPDCReplyProcessor;
   private Thread _EPDCReplyProcessorThread;
   private DebuggeeProcess _process;

   // The following vector is indexable by view type (e.g. EPDC.View_Class_Source)
   // for quick access to a view of a given type:

   private Vector _viewsByType = new Vector();

   // The following array is indexable by view number, which is not the same
   // as view type:

   private ViewInformation[] _supportedViews;

   private Vector _eventListeners = new Vector();
   private Semaphore _replySemaphore;
   private Semaphore _lockSemaphore = new Semaphore();
   private Semaphore _requestSemaphore = new Semaphore(1);
   private EPDC_Reply _mostRecentReply; // We may need to make this a stack of
                                        // replies that have not yet been
                                        // fully processed.
   private ModelUpdateLock _currentLock;
   private EventManager _eventManager = new EventManager();
   private Thread _callbackThread;
   private EngineCapabilities _capabilities;

   private EPDC_Request _mostRecentRequest;
   private Client _currentClient; // The last client to make a request
   private ModelEventListener _privilegedListener;

   private Vector _languages = new Vector();
   private Representation[] _repNames;
   private DebuggeeException[] _exceptionInfo;

   private Vector _stackDetailColumns;
   private Vector _registerGroups;
   private int _numberOfStackDetailColumns;
   private int _numberOfRegisterGroups;

   private ProcessListColumnDetails[] _processListColumnDetails;

   DebuggeeStartupOptions _debuggeeStartupOptions;

   private static DebuggeeStartupController _debuggeeStartupController =
           new DebuggeeStartupController();

   private static Hashtable _engineSpecificRestorableObjects = new Hashtable();

   private static final char[] _engineTypeMnemonics = new char[EPDC.LAST_BE_TYPE];

   // The event where the file is not found by either the engine or the frontend
   private ErrorOccurredEvent _errorOccurredEventForFileNotFound;

   private ModelStateChangedEvent _idleEvent;

   static
   {
     // Make sure all EStdStrings are converted to/from UTF-8:

     EStdString.setEncoding("UTF8");
     EExtString.setEncoding("UTF8");

     _engineTypeMnemonics[EPDC.BE_TYPE_PICL] = 'p';
     _engineTypeMnemonics[EPDC.BE_TYPE_WILEY] = 'w';
     _engineTypeMnemonics[EPDC.BE_TYPE_JAVA_PICL] = 'j';
   }

   private short _engineID;

   // This should probably be in Host, not here:

   private short _platformID;

   private byte _dominantLanguage;

   private boolean _saveAndRestoreExceptionFilters = false;

   private int _changeFlags;

   // Using same values here as in SaveRestoreFlags (just for consistency):

   static final int DEFAULT_DATA_REPRESENTATIONS_HAVE_CHANGED = 0x02000000;

   static final int EXCEPTION_FILTERS_HAVE_CHANGED = 0x01000000;

   private byte _stateFlags;
   private transient EPDC_EngineSession _engineSession =
       new EPDC_EngineSession();

   private static final int MIN_SUPPORTED_EPDC_VERSION = 907;
   private static final int MAX_SUPPORTED_EPDC_VERSION = 907;

   private int _maximumViewFileCacheSize = 500;

   // Model state flags. These states are not necessarily mutually exclusive:

   /** A request has been sent to the debug engine to perform an action and/or
    *  return some information, and we have not yet received a reply to that
    *  request from the debug engine. In this state, the Model will not accept
    *  any new requests (synchronous <b>or</b> asynchronous).
    *  @see DebugEngine#getState
    * @see DebugEngine#isBusy
    * @see DebugEngine#isAcceptingSynchronousRequests
    * @see DebugEngine#isAcceptingAsynchronousRequests
    * @see DebugEngineEventListener#modelStateChanged
    * @see ModelStateChangedEvent
    */

   public static final byte debugEngineIsBusyFlag = 0x01;

   /** A reply has been received from the debug engine and the Model is
    *  currently being updated using information contained in that reply.
    *  In this state, the Model will not accept
    *  any new requests (synchronous <b>or</b> asynchronous).
    *  @see DebugEngine#getState
    * @see DebugEngine#modelIsBeingUpdated
    * @see DebugEngine#isAcceptingSynchronousRequests
    * @see DebugEngine#isAcceptingAsynchronousRequests
    * @see DebugEngineEventListener#modelStateChanged
    * @see ModelStateChangedEvent
    */

   public static final byte modelIsBeingUpdatedFlag = 0x02;

   /** Events that were queued while the Model was being updated are now
    *  being fired to event listeners. In this state, the Model will not
    *  accept asynchronous requests - only synchronous requests are allowed.
    *  @see DebugEngine#getState
    * @see DebugEngine#queuedEventsAreBeingFired
    * @see DebugEngine#isAcceptingSynchronousRequests
    * @see DebugEngine#isAcceptingAsynchronousRequests
    * @see DebugEngineEventListener#modelStateChanged
    * @see ModelStateChangedEvent
    */

   public static final byte queuedEventsAreBeingFiredFlag = 0x04;

   /**
    * Saved objects are in the process of being restored.
    * @see DebuggeeProcess#restoreSavedObjects
    * @see RestorableObjects#restore
    */

   public static final byte savedObjectsAreBeingRestoredFlag = 0x08;

   /**
    * The following flags control how requests and replies are sent to and
    * received from the debug engine. They are passed to the
    * processEPDCRequest method.<p>
    * The default send/receive behaviour is:
    * <ul>
    * <li>Send and receive asynchronously - the reply from the debug engine
    *     is received and processed on a different thread from the one on
    *     which the request was sent.
    * <li>Process reply - the reply will be fully processed after being
    *     received i.e the Model will be updated and event listeners will
    *     be notified of events.
    * </ul>
    */

   public static final int sendReceiveDefault           = 0x00000000;

   /**
    * Use this flag when the reply from the debug engine should be received
    * and processed synchronously i.e. the thread that sent the request will
    * wait for the reply from the debug engine.
    */

   public static final int sendReceiveSynchronously     = 0x00000001;

   /**
    * Use this flag when you do NOT want the reply from the debug engine
    * to be automatically processed by the EPDCReplyProcessor.processReply
    * method.
    * <p><b>NOTE:</b> If this flag is passed to the processEPDCRequest
    * method, then the caller is responsible for doing all of
    * the "bookkeeping" that would have otherwise been done in
    * EPDCReplyProcessor.processReply. This includes the following:
    * <ul>
    * <li>Updating the Model with information received from the debug
    *     engine (this also results in listeners being notified of events
    *     within the Model).
    * <li>Handling the possibility that the debug engine returned an error
    *     code and message in response to the most recent request.
    * <li>Keeping the status of the DebugEngine object up to date, including
    *     the 'isBusy' and the '_modelIsBeingUpdated' flags.
    * </ul>
    * Under normal circumstances, this flag should NOT be used - instead,
    * let EPDCReplyProcessor.processReply do the processing of the reply.
    * When this flag <i>is</i> used, it is usually used in conjunction with the
    * sendReceiveSynchronously flag i.e. processEPDCRequest is called which
    * sends the request and then waits for the reply from the debug engine,
    * but the reply is not processed. Instead, the caller gets the most recent
    * reply after processEPDCRequest has returned, and does the processing
    * of the reply itself.
    */

   static final int sendReceiveDoNotProcessReply = 0x00000002;

   static final int sendReceiveCallerWillCompleteModelUpdates = 0x00000004;
}
