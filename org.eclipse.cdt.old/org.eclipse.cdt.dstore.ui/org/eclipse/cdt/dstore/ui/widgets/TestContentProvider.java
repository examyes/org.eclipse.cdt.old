package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;

import java.io.*;
import java.util.*;

/****/
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.viewers.*;

 
public class TestContentProvider extends ContainerContentProvider
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

