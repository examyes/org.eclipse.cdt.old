package org.eclipse.cdt.dstore.ui;

/* 
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.jface.action.*;

public interface IOpenAction 
{ 
    public void resetSelection();
    public void setSelected(DataElement selected);
    public void performGoto(boolean flag);
    public void run();
    public void setLocation(String filename, int location);
}
