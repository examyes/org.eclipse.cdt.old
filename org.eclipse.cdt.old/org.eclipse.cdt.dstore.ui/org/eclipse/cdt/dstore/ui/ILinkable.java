package org.eclipse.cdt.dstore.ui;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

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
