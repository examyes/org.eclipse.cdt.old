package com.ibm.dstore.core.util;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;

public interface ISender
{
  public void sendDocument(String document);
  public void sendDocument(DataElement objectRoot, int depth);
}

