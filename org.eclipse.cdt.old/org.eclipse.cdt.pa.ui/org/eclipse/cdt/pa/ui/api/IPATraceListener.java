package org.eclipse.cdt.pa.ui.api;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.widgets.*;

public interface IPATraceListener
{
  public Shell getShell();
  public void traceChanged(PATraceEvent e);
}
