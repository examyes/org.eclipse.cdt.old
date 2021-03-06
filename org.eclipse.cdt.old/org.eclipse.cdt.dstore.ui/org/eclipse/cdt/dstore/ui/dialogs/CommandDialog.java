package org.eclipse.cdt.dstore.ui.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;

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



public class CommandDialog extends org.eclipse.jface.dialogs.Dialog implements Listener, ISelectionChangedListener
{
  private   ArrayList      _inputs;
  private   ArrayList      _outputs;
  private   ArrayList      _labels;
  private   ArrayList      _entries;

  private   Button      _okButton;
  private   Button      _cancelButton;

  private   DataStore   _dataStore;
  private   DataElement _selected;

  private final static int	SIZING_SELECTION_WIDGET_HEIGHT = 150;
  private final static int	SIZING_SELECTION_WIDGET_WIDTH = 300;

  public CommandDialog(String title, ArrayList inputs, DataStore dataStore)
  {
    super(null);
    _inputs = inputs;
    _outputs = new ArrayList();
    _labels = new ArrayList();
    _entries = new ArrayList();

    _dataStore = dataStore;

  }



  protected void buttonPressed(int buttonId)
  {
    setReturnCode(buttonId);

    for (int i = 0; i < _entries.size(); i++)
    {
      if (_entries.get(i) instanceof Text)
      {
        DataElement arg = _dataStore.createObject(null, "value", ((Text)_entries.get(i)).getText(), "", "input" + i);
        _outputs.add(arg);	    	
      }
    }

    close();
  }

  protected void aboutToShow()
      {
      }

  public DataElement getSelected()
      {
        return _selected;
      }

  public ArrayList getValues()
  {
    return _outputs;
  }

  public Control createDialogArea(Composite parent)
  {
    Composite c = (Composite)super.createDialogArea(parent);

    GridLayout layout= new GridLayout();
    layout.marginHeight = 5;
    layout.marginWidth = 5;
    c.setLayout(layout);
    c.setLayoutData(new GridData(GridData.FILL_BOTH));
    for (int i = 0; i < _inputs.size(); i++)
      {
	DataElement input = (DataElement)_inputs.get(i);
	
	Label aLabel = new Label(c, SWT.NONE);	
	_labels.add(aLabel);
	aLabel.setText(input.getName());

	if (input.getType().equals("input"))
	  {	
	    Text aText = new Text(c, SWT.SINGLE | SWT.BORDER);
            GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
                                             GridData.GRAB_HORIZONTAL);
            aText.setLayoutData(textData);

	    _entries.add(aText);	
	  }
        if (_entries.size() > 0)
        {
          Object firstEntry = _entries.get(0);
          if (firstEntry instanceof Text)
            ((Text)firstEntry).setFocus();
        }
      }
    return c;
  }

  public void selectionChanged(SelectionChangedEvent event)
      {
        DataElement selected = ConvertUtility.convert(event);
        if (selected != null)
          _selected = selected;
      }

  public void handleEvent(Event e)
      {
	Widget source = e.widget;
      }
}


