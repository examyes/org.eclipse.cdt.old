package com.ibm.dstore.core.util;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;

public interface ISender
{
  public void sendDocument(String document);
  public void sendDocument(DataElement objectRoot, int depth);
}

