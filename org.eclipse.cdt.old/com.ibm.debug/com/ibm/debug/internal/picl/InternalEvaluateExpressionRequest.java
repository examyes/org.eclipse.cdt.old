package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/InternalEvaluateExpressionRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:01:14)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

import com.ibm.debug.model.Location;
import com.ibm.debug.model.MonitoredExpression;
import com.ibm.debug.model.ViewInformation;

/**
 * Internal request to evaluate/modify an expression.  This is an immediate
 * evaluate/modify that does not use events.
 */

public class InternalEvaluateExpressionRequest extends PICLEngineRequest {

    private String fExpression;
    private String fNewValue;
    private PICLThread fThreadContext;
    private MonitoredExpression fMonitoredExpression = null;

    /**
     * Constructor for InternalEvaluateExpressionRequest
     * @parm debugTarget The debug target.
     * @parm threadContext The thread context for the evaluation.
     * @parm expression The expression to evaluate.
     * @parm newValue The new value to assign to the expression or null
     * if the expression is to be evaluated only and not modified.
     */
    public InternalEvaluateExpressionRequest(PICLDebugTarget debugTarget,
                                             PICLThread threadContext,
                                             String expression,
                                             String newValue) {
        super(debugTarget);
        fThreadContext = threadContext;
        fExpression = expression;
        fNewValue = newValue;
    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {
        beginRequest();
        Location loc = null;

        try {
            ViewInformation vi = ((PICLStackFrame)fThreadContext.getTopStackFrame()).findSupportedViewInformation();
            loc = fThreadContext.getDebuggeeThread().currentLocationWithinView(vi);
        } catch(IOException ioe) {}

        if (loc == null) {
            endRequest();
            throw new PICLException(PICLUtils.getResourceString(msgKey + "file_not_found"));
        }

        try {
            if (fNewValue == null)
                fMonitoredExpression = fThreadContext.getDebuggeeThread().evaluateExpression(loc, fExpression, 1, 1);
            else
                fMonitoredExpression = fThreadContext.getDebuggeeThread().modifyExpression(loc, fExpression, fNewValue);

            if (fMonitoredExpression == null || isError())
                throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
        }
        catch(IOException ioe) {
            throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
        } finally {
            endRequest();
        }
    }

    /**
     * Gets the resulting monitored expression.
     * @return Returns a MonitoredExpression
     */
    public MonitoredExpression getMonitoredExpression() {
        return fMonitoredExpression;
    }
}

