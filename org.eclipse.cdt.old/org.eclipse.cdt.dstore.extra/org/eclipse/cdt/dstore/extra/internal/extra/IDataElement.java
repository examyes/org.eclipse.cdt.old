package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
