package com.ibm.dstore.ui.resource;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*; 


public interface IDataElementContainer
{
    public DataElement getElement();  
    public void remove(ResourceElement resource);  
}

