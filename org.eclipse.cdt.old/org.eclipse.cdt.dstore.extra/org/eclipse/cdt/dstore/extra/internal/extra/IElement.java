package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import org.eclipse.core.runtime.*;

public interface IElement extends IAdaptable
{
  public Object getElementProperty(Object key);
}
