package com.ibm.dstore.ui.dialogs;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.connections.*;
import com.ibm.dstore.core.model.*;

import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import org.eclipse.jface.dialogs.*;


public class ConnectDialog extends org.eclipse.jface.dialogs.Dialog implements Listener
{
    private   Button    _remoteConnect;
    
    private   Text      _nameText;
    
    private   Text      _ipText;
    private   Text      _portText;
    private   Button    _connectToUsingDaemon;
    
    private   Text      _dirText;
    
    private   boolean    _isLocal;
    private   boolean    _isUsingDaemon;

    private  String     _name;
    private  String     _ip;
    private  String     _port;
    private  String     _directory;
    
    private final static int	SIZING_SELECTION_WIDGET_HEIGHT = 150;
    private final static int	SIZING_SELECTION_WIDGET_WIDTH = 300;
    
    private Connection _connection;

    public ConnectDialog(String title)
    {
	super(null);
	_connection = null;
    }

    public ConnectDialog(String title, Connection connection)
    {
	super(null);
	_connection = connection;
    }

    
    protected void buttonPressed(int buttonId)
    {
	setReturnCode(buttonId);
	
	_name = _nameText.getText();
	_ip = _ipText.getText();
	_port = _portText.getText();
	_directory = _dirText.getText();
	_isLocal = !_remoteConnect.getSelection();
	_isUsingDaemon = _connectToUsingDaemon.getSelection();
	
	close();
    }
    
    protected void aboutToShow()
    {
    }
    
    public boolean isLocal()
    {
        return _isLocal;
    }
    
    public boolean isUsingDaemon()
    {
	return _isUsingDaemon;
    }  

    public String getName()
    {
	return _name;
    }
    
    public String getHostIP()
    {
	return _ip;
    }
     
    public String getPort()
    {
	return _port;
    }
    
    public String getHostDirectory()
    {
	return _directory;
    }
        
    public Control createContents(Composite parent)
    {
	super.createContents(parent);
	
	Composite c= (Composite)getDialogArea();
	GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
					 GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
	textData.widthHint = 80;

	
	
	GridLayout layout= new GridLayout();
	layout.numColumns = 2;
	layout.marginHeight = 5;
	layout.marginWidth = 5;
	layout.verticalSpacing=4;
	c.setLayout(layout);

	Label nameLabel = new Label(c, SWT.NONE);
	nameLabel.setText("Name");

	_nameText = new Text(c, SWT.BORDER);
	GridData dp0 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
	_nameText.setLayoutData(dp0);
	
	_remoteConnect = new Button(c, SWT.CHECK);
	_remoteConnect.setText("Work Remotely");
	_remoteConnect.addListener(SWT.Selection, this);
	
	Group sGroup = new Group(c, SWT.NONE);
	sGroup.setText("Server");
	
	GridLayout serverLayout = new GridLayout();
	serverLayout.numColumns = 4;
	serverLayout.marginHeight = 5;
	serverLayout.marginWidth = 5;
	serverLayout.verticalSpacing=4;
	sGroup.setLayout(serverLayout);

	Label ipLabel = new Label(sGroup, SWT.NONE);	
	ipLabel.setText("Host IP");
	
	_ipText = new Text(sGroup, SWT.SINGLE | SWT.BORDER);
	
	Label portLabel = new Label(sGroup, SWT.NONE);	
	portLabel.setText("Port");
	
	_portText = new Text(sGroup, SWT.SINGLE | SWT.BORDER);
	
	_connectToUsingDaemon = new Button(sGroup, SWT.CHECK);
	_connectToUsingDaemon.setText("Connect to using daemon");		
	GridData cdData = new GridData();
	cdData.horizontalAlignment = GridData.FILL;
	cdData.grabExcessHorizontalSpace = true;
	cdData.horizontalSpan = 3;
	_connectToUsingDaemon.setLayoutData(cdData);

	GridData serverData = new GridData();
	serverData.horizontalAlignment = GridData.FILL;
	serverData.grabExcessHorizontalSpace = true;
	sGroup.setLayoutData(serverData);
	
	Label dirLabel = new Label(c, SWT.NONE);	
	dirLabel.setText("Root Directory");
	
	_dirText = new Text(c, SWT.SINGLE | SWT.BORDER);
	_dirText.setLayoutData(textData);
	
	_ipText.setEnabled(false);
	_portText.setEnabled(false);
	
	setDefaults();

	getShell().setText("Connect");
	return c;
    }

    public void setDefaults()
    {
	if (_connection == null)
	    {
		_nameText.setText("Untitled");
		_remoteConnect.setSelection(false);
		_connectToUsingDaemon.setSelection(true);
		_ipText.setText("127.0.0.1");
		_portText.setText("4033");	
		_dirText.setText("/");
	    }
	else
	    {
		_nameText.setText(_connection.getName());		

		boolean isLocal = _connection.isLocal();
		_remoteConnect.setSelection(!isLocal);
		_ipText.setEnabled(!isLocal);
		_portText.setEnabled(!isLocal);
		_connectToUsingDaemon.setEnabled(!isLocal);

		_connectToUsingDaemon.setSelection(_connection.isUsingDaemon());
		_ipText.setText(_connection.getHost());
		_portText.setText(_connection.getPort());	
		_dirText.setText(_connection.getDir());
	    }
    }

    
    public void handleEvent(Event e)
    {
	Widget source = e.widget;
	if (source == _remoteConnect)
	    {
		boolean isLocal = !_remoteConnect.getSelection();
		_ipText.setEnabled(!isLocal);
		_portText.setEnabled(!isLocal);
		_connectToUsingDaemon.setEnabled(!isLocal);
	    }
    }
}


