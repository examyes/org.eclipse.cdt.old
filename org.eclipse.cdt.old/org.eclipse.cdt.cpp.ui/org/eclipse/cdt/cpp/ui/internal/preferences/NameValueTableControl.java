package com.ibm.cpp.ui.internal.preferences;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.wizards.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;

import java.util.ArrayList;

public class NameValueTableControl extends Composite implements Listener
{
    public class EditNameValueDialog extends org.eclipse.jface.dialogs.Dialog
    {
	private   Text      _name;
	private   Text      _value;

	private   String    _nameStr;
	private   String    _valueStr;

	private   String    _title;

	public EditNameValueDialog(String title)
	{
	    super(null);
	    _title = title;
	}

	protected void buttonPressed(int buttonId)
	{
	    setReturnCode(buttonId);
	    _nameStr = _name.getText();
	    _valueStr = _value.getText();
	    close();
	}

	public String getName()
	{
	    return _nameStr;
	}

	public String getValue()
	{
	    return _valueStr;
	}

	public void setName(String name)
	{
	    _nameStr = name;
	    if (_name != null)
		{
		    _name.setText(name);
		}
	}

	public void setValue(String value)
	{
	    _valueStr = value;
	    if (_value != null)
		{
		    _value.setText(value);
		}
	}
	
	protected void aboutToShow()
	{
	}

	public Control createContents(Composite parent)
	{
	    super.createContents(parent);
	    
	    Composite c= (Composite)getDialogArea();
	    GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
					     GridData.GRAB_HORIZONTAL);
	    
	    
	    GridLayout layout= new GridLayout();
	    layout.numColumns = 1;
	    layout.marginHeight = 4;
	    layout.marginWidth = 5;
	    c.setLayout(layout);
	    c.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    Composite u = new Composite(c, SWT.NONE);
	    Label nameLabel = new Label(u, SWT.NONE);	
	    nameLabel.setText("Name:");
	    
	    _name = new Text(u, SWT.SINGLE | SWT.BORDER);
	    if (_nameStr != null)
		{
		    _name.setText(_nameStr);
		}
	    else
		{
		    _name.setText("");
		}
	    _name.setLayoutData(textData);
	    
	    GridLayout uLayout = new GridLayout();
	    uLayout.numColumns = 3;
	    uLayout.marginHeight = 1;
	    uLayout.marginWidth = 5;
	    u.setLayout(uLayout);
	    u.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    Composite p = new Composite(c, SWT.NONE);      
	    Label valueLabel = new Label(p, SWT.NONE);	
	    valueLabel.setText("Value:");
	    
	    _value = new Text(p, SWT.SINGLE | SWT.BORDER);
	    if (_valueStr != null)
		{
		    _value.setText(_valueStr);
		}
	    else
		{
		    _value.setText("");
		}
	    _value.setLayoutData(textData);
	    
	    GridLayout pLayout = new GridLayout();
	    pLayout.numColumns = 3;
	    pLayout.marginHeight = 1;
	    pLayout.marginWidth = 5;
	    p.setLayout(pLayout);
	    p.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    getShell().setText(_title);
	    
	    return c;
	}
}


    private Table       _table;
    private Button      _addButton;
    private Button      _removeButton;
    private Button      _editButton;

    public NameValueTableControl(Composite cnr, int style) 
    {
	super(cnr, style);
	initialize("Variables", true);
    }

    public NameValueTableControl(Composite cnr, int style, String title) 
    { 
	super(cnr, style);
	initialize(title, true);
    }

    public NameValueTableControl(Composite cnr, int style, String title, boolean editable) 
    { 
	super(cnr, style);
	initialize(title, editable);
    }

    public void initialize(String title, boolean editable)
    {
	Group group = new Group(this, SWT.NONE);
	group.setText(title);

	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	group.setLayout(layout);
	group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// create table 
	_table = new Table(group, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);	
	_table.setHeaderVisible(true);
	_table.setLinesVisible(true);

	//	GridLayout tlayout = new GridLayout();
	TableLayout tlayout = new TableLayout();
	//tlayout.numColumns = 1;
	_table.setLayout(tlayout);

	GridData tgrid = new GridData(GridData.FILL_BOTH);
	tgrid.heightHint = 80;
       	_table.setLayoutData(tgrid);

	TableColumn tc1 = new TableColumn(_table, SWT.NONE, 0);
	tc1.setText("Name");
	tc1.setWidth(120);

	TableColumn tc2 = new TableColumn(_table, SWT.NONE, 1);
	tc2.setText("Value");
	tc2.setWidth(200);

	
	if (editable)
	    {
		// create canvas for buttons
		Composite buttonCanvas = new Composite(group, SWT.NONE);
		
		// create add button
		_addButton = new Button(buttonCanvas, SWT.PUSH);
		_addButton.addListener(SWT.Selection, this);
		_addButton.setText("Add...");
		GridData dp1 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		dp1.widthHint = 80;
		_addButton.setLayoutData(dp1);
		
		// create edit button
		_editButton = new Button(buttonCanvas, SWT.PUSH);
		_editButton.addListener(SWT.Selection, this);
		_editButton.setText("Edit...");
		GridData dp2 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		dp2.widthHint = 80;
		_editButton.setLayoutData(dp2);
		
		// create remove button
		_removeButton = new Button(buttonCanvas, SWT.PUSH);
		_removeButton.addListener(SWT.Selection, this);
		_removeButton.setText("Remove");
		GridData dp3 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		dp3.widthHint = 80;
		_removeButton.setLayoutData(dp3);
		
		GridLayout blayout = new GridLayout();
		blayout.numColumns = 1;
		GridData bp3 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);	
		buttonCanvas.setLayout(blayout);
		buttonCanvas.setLayoutData(bp3);
	    }

	setLayout(new GridLayout());
	setLayoutData(new GridData(GridData.FILL_BOTH));
    }

  public void handleEvent(Event e)
  {
    Widget source = e.widget;

    if (source == _addButton)
	{
	    EditNameValueDialog dialog = new EditNameValueDialog("Add Variable");
	    dialog.open();
	    String name  = dialog.getName();
	    String value = dialog.getValue();
	    
	    TableItem newItem = new TableItem(_table, SWT.NONE);		
	    newItem.setText(0, name);
	    newItem.setText(1, value);
	}
    else if (source == _editButton)
	{	    
	    TableItem editItem = null;
	    for (int i = 0; (i < _table.getItemCount()) && (editItem == null); i++)
		{
		    if (_table.isSelected(i))
			{
			    editItem = _table.getItem(i);
			}
		}

	    if (editItem != null)
		{
		    String name1  = editItem.getText(0);
		    String value1 = editItem.getText(1);
		    
		    EditNameValueDialog dialog = new EditNameValueDialog("Edit Name Value Pair");
		    dialog.setName(name1);
		    dialog.setValue(value1);
		    dialog.open();
		    
		    String name2  = dialog.getName();
		    String value2 = dialog.getValue();
		    
		    editItem.setText(0, name2);
		    editItem.setText(1, value2);
		}
	}
    else if (source == _removeButton)
	{	    
	    for (int i = 0; i < _table.getItemCount(); i++)
		{
		    if (_table.isSelected(i))
			{
			    _table.remove(i);
			}
		}
	}
  }

    public ArrayList getVariables()
    {
        ArrayList result = new ArrayList();
        for (int i = 0; i < _table.getItemCount(); i++)
        {
	    TableItem item = _table.getItem(i);
	    String name  = item.getText(0);
	    String value = item.getText(1);
	    String variable = name + "=" + value;
	    result.add(variable);
        }
        return result;

    }

    public void setVariables(ArrayList variables)
    {
	_table.setRedraw(false);
	for (int i = 0; i < variables.size(); i++)
	    {
		String variable = (String)variables.get(i);
		int separatorIndex = variable.indexOf('=');
		String name = variable.substring(0, separatorIndex);
		String value = variable.substring(separatorIndex + 1, variable.length());

		TableItem newItem = new TableItem(_table, SWT.NONE);		
		newItem.setText(0, name);
		newItem.setText(1, value);
	    }
	_table.setRedraw(true);
    }

}
