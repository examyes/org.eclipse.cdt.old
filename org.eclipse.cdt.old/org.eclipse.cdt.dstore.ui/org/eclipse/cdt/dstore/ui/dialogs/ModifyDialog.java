package org.eclipse.cdt.dstore.ui.dialogs;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

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


public class ModifyDialog extends org.eclipse.jface.dialogs.Dialog implements Listener, ISelectionChangedListener
{
  private   String      _title;
  private   DataElement _status;

  private   DataStore   _dataStore;

  private   Label       _msgText;
  private   TreeViewer  _viewer;

  private   Label       _nameLabel;
  private   Text        _nameEntry;

  private   Label       _sourceLabel;
  private   Text        _sourceEntry;

  private   Button      _okButton;
  private   Button      _cancelButton;

  private   DataElement _element;

  private final static int	SIZING_SELECTION_WIDGET_HEIGHT = 150;
  private final static int	SIZING_SELECTION_WIDGET_WIDTH = 300;

  public ModifyDialog(DataElement element)
  {
    super(null);
    _element = element;
  }

  protected void buttonPressed(int buttonId)
  {
    if (OK == buttonId)
      {	
	setReturnCode(OK);
	DataStore dataStore = _element.getDataStore();	
	_element.setAttribute(DE.A_NAME, _nameEntry.getText());
	_element.setAttribute(DE.A_SOURCE, _sourceEntry.getText());	
	dataStore.setObject(_element);		
      }
    else if (CANCEL == buttonId)
      setReturnCode(CANCEL);
    else
      setReturnCode(buttonId);
    close();
  }

  protected void aboutToShow()
  {
    if (_viewer != null)
      _viewer.refresh();
  }

//  public Control createContents(Composite parent)
  public Control createDialogArea(Composite parent)
  {
   // Composite c= new Composite(parent, SWT.NONE);
    Composite c = (Composite)super.createDialogArea(parent);
    GridLayout layout= new GridLayout();
    c.setLayout(layout);


    _nameLabel = new Label(c, SWT.NONE);
    _nameLabel.setText("Name");

    _nameEntry = new Text(c, SWT.SINGLE | SWT.BORDER);
    _nameEntry.setText(_element.getName());

    _sourceLabel = new Label(c, SWT.NONE);
    _sourceLabel.setText("Source");

    _sourceEntry = new Text(c, SWT.SINGLE | SWT.BORDER);
    _sourceEntry.setText(_element.getSource());

    Composite bottomButtonsGroup = new Composite(c,SWT.NONE);
    bottomButtonsGroup.setLayout(new RowLayout());
    GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL |
                                 GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
    bottomButtonsGroup.setLayoutData(data);


    _okButton = this.createButton(bottomButtonsGroup,OK,"Change", true);
    _cancelButton = this.createButton(bottomButtonsGroup,CANCEL,"Cancel",false);

    // initialize
    this.selectionChanged(new SelectionChangedEvent(_viewer,new StructuredSelection()));

    return c;
  }

  public void handleEvent(Event e)
      {
	Widget source = e.widget;
      }

  public void selectionChanged(SelectionChangedEvent event)
      {
      }
}


