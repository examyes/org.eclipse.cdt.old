package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.dnd.*; 

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

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
