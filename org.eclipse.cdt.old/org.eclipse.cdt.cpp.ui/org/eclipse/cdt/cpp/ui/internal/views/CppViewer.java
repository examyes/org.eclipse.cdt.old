package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.ISelected;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;
import java.lang.reflect.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

public class CppViewer extends TreeViewer implements ISelectionChangedListener, ISelected, ITreeViewerListener
{  
  private DataElement  _selected;
  private DataElement  _currentInput;

  public CppViewer(Composite parent) 
      {
	super(parent);
        addSelectionChangedListener(this);
        addTreeListener(this);
      }


  public void fillContextMenu(MenuManager menu)
      {
        fillContextMenuHelper(menu, getSelected());
      }


  public void fillContextMenuHelper(MenuManager menu, DataElement object)
      {
      }

  public void fillLocalToolBar(ToolBarManager toolBarManager) 
      {
      }


  public void handleLinkEvent(SelectionChangedEvent event) 
      {
	IStructuredSelection sel = (IStructuredSelection) event.getSelection();
	if (sel.isEmpty())
          return;
	IElement input = null;
	input = (IElement)sel.getFirstElement();

	if (input instanceof DataElement)
	  setInput((DataElement)input);
      }

  public void setInput(DataElement input)
  {     
    super.setInput((IElement)input); 
    if (input != null)
      {
	_currentInput = input;   
      }    
  }
  

  public void setSelected(DataElement selected)
      {
        _selected = selected;
      }

  public DataElement getSelected()
      {
        return _selected;
      }

  public void selectionChanged(SelectionChangedEvent e)
      {
        DataElement selected = ConvertUtility.convert(e);
        if (selected != null)
	{
	  selected.expandChildren();
	  setSelected(selected);
	}
      }


  public void treeCollapsed(TreeExpansionEvent event)
      {
      }
  
  public void treeExpanded(TreeExpansionEvent event)
    {
      DataElement selected = (DataElement)event.getElement();
      if (selected != null)
      {
        selected.expandChildren();
      }
    }

}

