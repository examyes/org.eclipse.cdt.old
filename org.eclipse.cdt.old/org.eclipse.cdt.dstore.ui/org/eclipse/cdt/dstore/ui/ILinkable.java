package com.ibm.dstore.ui;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;

public interface ILinkable
{
    boolean isLinked();
    boolean isLinkedTo(ILinkable to);  
    void setLinked(boolean flag);
    void linkTo(ILinkable to);
    void unlinkTo(ILinkable to);
    void setInput(DataElement input);
    void resetView();
}
