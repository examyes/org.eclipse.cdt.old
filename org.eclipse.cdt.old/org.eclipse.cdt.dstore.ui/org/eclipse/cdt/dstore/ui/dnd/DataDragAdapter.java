package com.ibm.dstore.ui.dnd;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.ui.part.*;


public class DataDragAdapter implements DragSourceListener 
{
  ISelectionProvider _selectionProvider;

  public DataDragAdapter(ISelectionProvider provider) 
  {
    _selectionProvider = provider;
  }

  public void dragFinished(DragSourceEvent event) 
  {
    DataElement selected = getSelected();
  }

    public void dragStart(DragSourceEvent event)
    {
    }

  public void dragSetData(DragSourceEvent event) 
  {
    DataElement selected = getSelected();
    PluginTransferData data = new PluginTransferData("hi", selected.getAttribute(DE.A_ID).getBytes());
    
    event.data = data;
  }

  public DataElement getSelected()
  {
    DataElement selected = ConvertUtility.convert(_selectionProvider.getSelection());    
    return selected; 
  }
  
}
