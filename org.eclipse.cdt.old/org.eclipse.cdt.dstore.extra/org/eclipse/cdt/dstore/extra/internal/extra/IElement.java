package org.eclipse.cdt.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.core.runtime.*;

public interface IElement extends IAdaptable
{
  public Object getElementProperty(Object key);
}
