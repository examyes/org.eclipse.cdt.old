package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLRequestOperation.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:58:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.*;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import java.io.IOException;

/**
 * A <code>PICLRequestOperation</code> encapsulates a PICL request
 * in an operation. It blocks until the engine is accepting requests,
 * and then sends the request.
 */
public class PICLRequestOperation implements DebugEngineEventListener {

	// Resource String keys
	private static final String ENGINE_NOT_READY= "picl.common.engine_not_ready";

	/**
	 * The request to perform
	 */
	protected IPICLRequest fRequest= null;

	/**
	 * The engine that this operation must wait for
	 */
	protected DebugEngine fEngine= null;

	/**
	 * A flag indicating if we've been notified via a callback from the DebugEngine
	 * that the engine is ready to accept a request
	 */
	protected boolean fNotified= false;

	/**
	 * Constructs an operation to perform the request in the contenxt
	 * of the given debug engine.
	 */
	public PICLRequestOperation(IPICLRequest request, DebugEngine engine) {
		fRequest= request;
		fEngine= engine;
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public synchronized void debugEngineTerminated(DebugEngineTerminatedEvent event) {
		fNotified= true;
		notifyAll();
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public void engineCapabilitiesChanged(EngineCapabilitiesChangedEvent event) {
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public void errorOccurred(ErrorOccurredEvent event) {
	}

	/**
	 * Executes this operation's request when the engine is no longer busy.
	 *
	 * @exception OperationFailedException if the request fails.
	 */
	protected synchronized void execute() throws DebugException {
		boolean ready= isEngineReady();
		if (!ready) {
			fEngine.addEventListener(this);
			try {
				fNotified= false;
				wait(IPICLRequest.PICL_REQUEST_TIME_OUT);
			} catch (InterruptedException e) {
				//should throw time-out exception
				throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,"Time out",e));
			} finally {
				fEngine.removeEventListener(this);
				if (!fNotified) {
					throw generateDebugException(IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(ENGINE_NOT_READY), null);
				}
			}
		}
		try {
			boolean accepted= fRequest.performRequest();
			if (!accepted) {
				throw generateDebugException(IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(ENGINE_NOT_READY), null);
			}
		} catch (IOException e) {
			throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,"Error sending request",e));
		}
	}

	/**
	 * Creates and returns a properly configured debug exception.
	 */
	protected DebugException generateDebugException(int code, String message, Exception wrappedException) {
		return new DebugException(new Status(IStatus.ERROR, "com.ibm.dt.picl", code, message, wrappedException));
	}

	/**
	 * Returns <code>true</code> if the debug engine is ready to accept
	 * this request, otherwise <code>false</code>
	 */
	protected boolean isEngineReady() {
		boolean ready= true;
		int mode= fRequest.getMode();
		if ((mode & IPICLRequest.IDLE) > 0) {
			ready= ready && !fEngine.isBusy();
		}
		if ((mode & IPICLRequest.IMMEDIATE) > 0) {
			ready= ready && true;
		}
		if ((mode & IPICLRequest.ASYNCHRONOUS) > 0) {
			ready= ready && fEngine.isAcceptingAsynchronousRequests();
		}
		if ((mode & IPICLRequest.SYNCHRONOUS) > 0) {
			ready= ready && fEngine.isAcceptingSynchronousRequests();
		}
		return ready;
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public void messageReceived(MessageReceivedEvent event) {
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public synchronized void modelStateChanged(ModelStateChangedEvent event) {
		if (isEngineReady()) {
			fNotified= true;
			notifyAll();
		}
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public void processAdded(ProcessAddedEvent event) {
	}

   	public void commandLogResponse(DebugEngineCommandLogResponseEvent event) {
   	}

}
