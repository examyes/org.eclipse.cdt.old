package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;

import java.io.*;
import java.util.*;

import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.viewers.*;

 
public class DataElementContentProvider extends ContainerContentProvider
{
  private DataElement _input; 

  public void inputChanged(Viewer visualPart,
			   Object oldInput,
			   Object newInput)
  {
    _input = (DataElement)newInput;    
  }

  public boolean isDeleted(Object element)
  {
    return false;
  }
}

