package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import org.eclipse.swt.widgets.*;

public interface ICppProjectListener
{
  public Shell getShell();
  public void projectChanged(CppProjectEvent e);
}
