package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*; 

import java.util.*; 

public interface IDataElement extends IElement
{
    String getName();
    String getId();
    ArrayList getNestedData();
    int getNestedSize();

    void expandChildren();
    IDataElement expandChildren(boolean b);

    Object getElementProperty(Object obj);
    ArrayList getAssociated(String key);
}
