package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.dnd.*; 

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.jface.resource.*;

import org.eclipse.core.runtime.IAdaptable;
import java.util.ArrayList;
 
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.dnd.*;

import org.eclipse.jface.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.part.*;

  public class ObjectSelectionChangedListener implements ISelectionChangedListener
  {
      private IOpenAction   _gotoAction;
      private boolean       _isEnabled;
      private ObjectWindow  _window;

    public ObjectSelectionChangedListener(ObjectWindow window, IOpenAction openAction)
    {
	_window = window;
	_gotoAction = openAction;
	_isEnabled = true;
    }
      
      public void enable(boolean flag)
      {
	  _isEnabled = flag;
      }  
      
      public boolean isEnabled()
      {
	  return _isEnabled;
      }
      
      public void selectionChanged(SelectionChangedEvent e)
      {
	  if (_isEnabled)
	      {
		  DataElement selected = ConvertUtility.convert(e);
		  if (selected != null)
		      {
			  selected.expandChildren();
			  DataElement root = selected.dereference();			  
			  _window.setSelected(root, true);		
			  if (_gotoAction != null)
			      {
				  _gotoAction.setSelected(selected);
				  _gotoAction.performGoto(false);	  
			      }
		      }
	      }
      }
  }
