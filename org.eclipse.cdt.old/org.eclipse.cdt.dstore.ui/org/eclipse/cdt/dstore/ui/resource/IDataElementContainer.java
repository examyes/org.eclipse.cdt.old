package org.eclipse.cdt.dstore.ui.resource;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*; 


public interface IDataElementContainer
{
    public DataElement getElement();  
    public void remove(ResourceElement resource);  
}

