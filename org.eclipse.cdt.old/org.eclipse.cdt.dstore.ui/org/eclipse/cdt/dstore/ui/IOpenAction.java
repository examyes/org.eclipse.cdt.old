package com.ibm.dstore.ui;

/* 
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;

public interface IOpenAction
{ 
    public void setSelected(DataElement selected);
    public void performGoto(boolean flag);
    public void run();
}
