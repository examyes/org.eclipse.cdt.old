package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineDaemon.java, java-model, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:14:14)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import java.util.Vector;
import java.io.*;

/**
 * Represents the daemon that listens for remote debug engines.
 * Remote engines that contact this daemon will cause an EngineWaiting event that
 * the client of model can listen for.
 */
public class EngineDaemon extends DebugModelObject {
    private static final String _defaultPort = "8001";
    private DebugDaemon _debugDaemon = null;
    private EventManager _eventManager = new EventManager();
    private Vector _eventListeners = new Vector();

    // class used to handle engine connecting event from daemon

    static class EngineConnected extends DebugDaemon.EngineConnectingAdapter {

        private EngineDaemon _engineDaemon;
        private DebugDaemon.EngineParameters _engineParameters = null;

        public EngineConnected(EngineDaemon engineDaemon) {
            super();
            _engineDaemon = engineDaemon;
        }

        public void engineConnecting(DebugDaemon.EngineConnectingEvent e) {

            // 1FW73ED
            if (Model.TRACE.DBG && Model.traceInfo())
                Model.TRACE.dbg(2,"Engine connected");

            // store the parameters from the debug daemon event

            _engineParameters = e.getEngineParameters();

            // fire all registered listeners

            _engineDaemon._eventManager.fireEvent(new DebugEngineWaitingEvent(this, this , -1),_engineDaemon._eventListeners);
            return;
        }

        // return the connectionInfo object required to connect to the remote
        // debug engine

        public ConnectionInfo getConnectionInfo() {
            return _engineParameters.getConnectionInfo();
        }

        // return the title passed from the remote engine

        public String getTitle() {
            return _engineParameters.getTitle();
        }

        // return a string that contains the arguments passed from the
        // remote engine

        public String getArguments() {
            return _engineParameters.getArguments();
        }

        // return the engine parameters object
        public DebugDaemon.EngineParameters getEngineParameters() {
            return _engineParameters;
        }
    }

    /**
     * Add an engine daemon event listener to this EngineDaemon object. Whenever
     * an event occurs for which there is a corresponding method in the
     * event listener's interface, that method will be called to inform the
     * listener of the event. More than one listener may be added to a given
     * Host object - the listeners will be notified of events in
     * the order in which they were added (i.e. FIFO).
     * @param eventListener The object whose methods will be called when
     * events occur.
     */
    public void addEventListener(EngineDaemonEventListener eventListener)
    {
        if (Model.TRACE.DBG && Model.traceInfo())
            Model.TRACE.dbg(1, "EngineDaemon.addEventListener(" + eventListener + ")");

        _eventListeners.addElement(eventListener);
    }

   /**
    * Remove an engine daemon event listener.
    * @param eventListener The event listener to remove.
    */
   public void removeEventListener(EngineDaemonEventListener eventListener)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, "EngineDaemon.removeEventListener(" + eventListener + ")");

     _eventListeners.removeElement(eventListener);
   }

    /**
     * Check to see if the debug daemon is listening
     * Creation date: (9/18/2000 11:08:01 AM)
     * @return boolean
     */
    public boolean isListening()
    {
        if (_debugDaemon == null)
            return false;

        return _debugDaemon.isListening();
    }

    /**
     * Starts listening for remote engine requests on default port 8001
     * The caller should register a host listener for engine connection events
     * @return boolean - true means successful
     */
    public boolean startListening() {
	    return startListening(_defaultPort);
    }

    public boolean startListening(String portNumber) {
        // initially supports 1 daemon

        // check to make sure that the new port number is a valid number
        int setPortNumber = 0;
        try {
            setPortNumber = Integer.parseInt(portNumber);
        } catch(NumberFormatException ne) {
            return false;
        }

        if (setPortNumber <= 0)
            return false;

        // first check to see if it is already listening
        if (isListening()) return true;

        if (_debugDaemon == null) {
            _debugDaemon = new TCPIPDebugDaemon(portNumber);
            _debugDaemon.setDaemon(true);   // make this a daemon thread
        }

        _debugDaemon.addEngineConnectingListener(new EngineConnected(this));

        // start the daemon listening
        _debugDaemon.startListening();

        return true;
    }

    /**
     * Stop the debug's daemon from listening
     * Creation date: (9/14/2000 5:20:08 PM)
     */
    public void stopListening()
    {
        _debugDaemon.stopListening();
        _debugDaemon = null;   // finished with the daemon
    }
}
