package org.eclipse.cdt.dstore.hosts.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.hosts.*;

import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;



public class RenameDialog extends org.eclipse.jface.dialogs.Dialog implements Listener, ISelectionChangedListener
{

  private   Text        _aText;

  private   String      _name;
  private   String      _title;
  
  private final static int	SIZING_SELECTION_WIDGET_HEIGHT = 150;
  private final static int	SIZING_SELECTION_WIDGET_WIDTH = 300;
    
    private HostsPlugin _plugin;

  public RenameDialog(String title, String oldName)
  {
    super(null);
    _title = title;
    _name = oldName;
    _plugin = HostsPlugin.getInstance();
  }



  protected void buttonPressed(int buttonId)
  {
    setReturnCode(buttonId);
    _name = _aText.getText();
    close();
  }

  protected void aboutToShow()
      {
      }


  public String getName()
  {
  	return _name;
  }
 
  public Control createDialogArea(Composite parent)
  {
    Composite c = (Composite)super.createDialogArea(parent);

    GridLayout layout= new GridLayout();
    c.setLayout(layout);
    c.setLayoutData(new GridData(GridData.FILL_BOTH));
  
  	Label aLabel = new Label(c, SWT.NONE);	
	aLabel.setText(_plugin.getLocalizedString("dialogs.New_Name"));
	
	_aText = new Text(c, SWT.SINGLE | SWT.BORDER);
    GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
                                             GridData.GRAB_HORIZONTAL);
    _aText.setLayoutData(textData);
	_aText.setText(_name);

	getShell().setText(_title);
    return c;
  }

  public void selectionChanged(SelectionChangedEvent event)
      {
      }

  public void handleEvent(Event e)
      {
	Widget source = e.widget;
      }
}


