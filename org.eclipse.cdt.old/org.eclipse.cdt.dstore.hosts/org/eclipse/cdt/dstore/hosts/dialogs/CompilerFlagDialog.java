package com.ibm.dstore.hosts.dialogs;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.actions.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.views.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.connections.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.ui.*;

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

public class CompilerFlagDialog extends org.eclipse.jface.dialogs.Dialog 
    implements Listener
{
    public Text         _definitionArea;

    protected Button       _cpp;
    protected Button       _c;

	protected ObjectWindow _resultViewer;
    protected HostsPlugin  _plugin;
    protected String       _title;
    protected String       _patternLabel;
    protected String       _actionLabel;
    
    protected DataElement  _root;
    protected int          _width;
    protected int          _height;


    public CompilerFlagDialog(DataElement root)
    {
		super(null);
		_plugin = HostsPlugin.getInstance();
		_title = "Compiler Flag Definition";
		_root = root;

		_patternLabel = "Flag Definition";
		_height = 250;
		_width = 250;
    }

    public Control createDialogArea(Composite parent)
    {
		Composite c = (Composite)super.createDialogArea(parent);

		GridLayout clayout= new GridLayout();
		clayout.numColumns = 2;
		clayout.marginHeight = 5;
		clayout.marginWidth = 5;
		c.setLayout(clayout);

		GridData cgrid = new GridData(GridData.FILL_BOTH);
		c.setLayoutData(cgrid);

		Label definitionLabel = new Label(c, SWT.NONE);
		definitionLabel.setText(_patternLabel);

		_definitionArea = new Text(c, SWT.BORDER);
		GridData egrid = new GridData(GridData.FILL_HORIZONTAL);
		egrid.widthHint = 100;
		_definitionArea.setLayoutData(egrid);

		_cpp = new Button(c, SWT.CHECK);
		_cpp.setText("CXXFLAGS");
		_cpp.addListener(SWT.Selection, this);
		GridData sgrid = new GridData(GridData.FILL_HORIZONTAL);
		sgrid.widthHint = 50;
		_cpp.setLayoutData(sgrid);
		
		_c = new Button(c, SWT.CHECK);
		_c.setText("CFLAGS");
		_c.addListener(SWT.Selection, this);
		GridData cangrid = new GridData(GridData.FILL_HORIZONTAL);
		cangrid.widthHint = 50;
		_c.setLayoutData(cangrid);

		getShell().setText(_title);
		return c;
    }

    public void handleEvent(Event e)
    {
		Widget widget = e.widget;
		if (widget == _c)
	    {
			DataElement status = _resultViewer.getInput();
			DataStore dataStore = status.getDataStore();
			DataElement cmd = status.getParent();
			DataElement cancelCmd = dataStore.localDescriptorQuery(cmd, "C_CANCEL");
			if (cancelCmd != null)
		    {
				dataStore.command(cancelCmd, cmd);
	 		}
    	}
    
	 }
	 public void setDefinition(String def)
	 {
	 	if(!def.equals(""))
	 		_definitionArea.setText(def);
	 	
	 }
}
