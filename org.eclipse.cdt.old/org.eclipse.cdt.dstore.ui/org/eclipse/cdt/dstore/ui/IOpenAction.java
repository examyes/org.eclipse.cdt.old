package com.ibm.dstore.ui;

/* 
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;

public interface IOpenAction
{ 
    public void setSelected(DataElement selected);
    public void performGoto(boolean flag);
    public void run();
}
