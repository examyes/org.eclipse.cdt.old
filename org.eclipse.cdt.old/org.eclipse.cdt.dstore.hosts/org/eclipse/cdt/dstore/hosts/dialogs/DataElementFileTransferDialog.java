package com.ibm.dstore.hosts.dialogs;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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

public class DataElementFileTransferDialog extends org.eclipse.jface.dialogs.Dialog 
    implements Listener, ITransferListener
{
    private ObjectWindow _localViewer;
    private ObjectWindow _remoteViewer;

    private DataElement  _localInput;
    private DataElement  _remoteInput;

    private Button       _localHome;
    private Button       _remoteHome;
    
    private Button       _localBack;
    private Button       _remoteBack;

    private Button       _sendRemote;
    private Button       _sendLocal;

    private HostsPlugin  _plugin;
    private String       _title;

    private Text         _progressIndicator;

    public DataElementFileTransferDialog(String title, DataElement localInput, DataElement remoteInput)
    {
	super(null);
	_localInput  = localInput;
	_remoteInput = remoteInput;
	_plugin = HostsPlugin.getInstance();
	_title = title;
    }

    public Control createDialogArea(Composite parent)
    {
	Composite c = (Composite)super.createDialogArea(parent);

	GridLayout clayout= new GridLayout();
	clayout.numColumns = 3;
	clayout.marginHeight = 2;
	clayout.marginWidth = 2;
	c.setLayout(clayout);

	GridData cgrid = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(cgrid);

	// local viewer
	Group localGroup = new Group(c, SWT.NONE);
	localGroup.setText("Local Files");

	GridLayout llayout= new GridLayout();
	llayout.numColumns = 2;
	llayout.marginHeight = 2;
	llayout.marginWidth = 2;

	GridData lgrid = new GridData(GridData.FILL_BOTH);
	lgrid.heightHint = 300;
	lgrid.widthHint = 300;

	localGroup.setLayout(llayout);
	localGroup.setLayoutData(lgrid);


	Composite localButtons = new Composite(localGroup, SWT.NONE);
	GridLayout lblayout= new GridLayout();
	lblayout.numColumns = 1;
	lblayout.marginHeight = 2;
	lblayout.marginWidth = 1;
	localButtons.setLayout(lblayout);
	localButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

	_localHome = new Button(localButtons, SWT.FLAT);
	_localHome.addListener(SWT.Selection, this);
	_localHome.setImage(_plugin.getImageDescriptor("home.gif").createImage());
	_localHome.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

	_localBack = new Button(localButtons, SWT.FLAT);
	_localBack.addListener(SWT.Selection, this);
	_localBack.setImage(_plugin.getImageDescriptor("up.gif").createImage());
	_localBack.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));


	DataStore localDataStore = _localInput.getDataStore();
	_localViewer = new ObjectWindow(localGroup, 0, localDataStore, 
					_plugin.getImageRegistry(), _plugin.getDialogActionLoader(), true);

	_localViewer.setSorter(new DataElementSorter(DE.P_NAME));
	_localViewer.setInput(localDataStore.getHostRoot());

	GridLayout lvlayout= new GridLayout();
	lvlayout.numColumns = 1;
	lvlayout.marginHeight = 2;
	lvlayout.marginWidth = 2;

	GridData lvgrid = new GridData(GridData.FILL_BOTH);
	lvgrid.heightHint = 250;
	lvgrid.widthHint = 250;

	_localViewer.setLayout(lvlayout);
	_localViewer.setLayoutData(lvgrid);
       



	// control panel
	Composite controlPanel = new Composite(c, SWT.NULL);
	
	GridData buttonGrid1 = new GridData();
	buttonGrid1.heightHint = 20;
	buttonGrid1.widthHint = 20;

	_sendRemote = new Button(controlPanel, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
	_sendRemote.setLayoutData(buttonGrid1);
	_sendRemote.addListener(SWT.Selection, this);

	GridData buttonGrid2 = new GridData();
	buttonGrid2.heightHint = 20;
	buttonGrid2.widthHint = 20;

	_sendLocal = new Button(controlPanel, SWT.ARROW | SWT.LEFT | SWT.FLAT);
	_sendLocal.setLayoutData(buttonGrid2);
	_sendLocal.addListener(SWT.Selection, this);

	GridLayout playout= new GridLayout();
	playout.numColumns = 1;
	playout.marginHeight = 1;
	playout.marginWidth = 1;
	controlPanel.setLayout(playout);



	// remote viewer
	Group remoteGroup = new Group(c, SWT.NONE);
	remoteGroup.setText("Remote Files");

	GridLayout rlayout= new GridLayout();
	rlayout.numColumns = 2;
	rlayout.marginHeight = 2;
	rlayout.marginWidth = 2;

	GridData rgrid = new GridData(GridData.FILL_BOTH);
	rgrid.heightHint = 300;
	rgrid.widthHint = 300;

	remoteGroup.setLayout(rlayout);
	remoteGroup.setLayoutData(rgrid);

	Composite remoteButtons = new Composite(remoteGroup, SWT.NONE);
	GridLayout rblayout= new GridLayout();
	rblayout.numColumns = 1;
	rblayout.marginHeight = 2;
	rblayout.marginWidth = 1;
	remoteButtons.setLayout(rblayout);
	remoteButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

	_remoteHome = new Button(remoteButtons, SWT.FLAT);
	_remoteHome.addListener(SWT.Selection, this);
	_remoteHome.setImage(_plugin.getImageDescriptor("home.gif").createImage());
	_remoteHome.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

	_remoteBack = new Button(remoteButtons, SWT.FLAT);
	_remoteBack.addListener(SWT.Selection, this);
	_remoteBack.setImage(_plugin.getImageDescriptor("up.gif").createImage());
	_remoteBack.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

	DataStore remoteDataStore = _remoteInput.getDataStore();
	_remoteViewer = new ObjectWindow(remoteGroup, 0, remoteDataStore, 
					 _plugin.getImageRegistry(), _plugin.getDialogActionLoader(), true);
	_remoteViewer.setSorter(new DataElementSorter(DE.P_NAME));
	_remoteViewer.setInput(remoteDataStore.getHostRoot());


	GridLayout rvlayout= new GridLayout();
	rvlayout.numColumns = 1;
	rvlayout.marginHeight = 2;
	rvlayout.marginWidth = 2;

	GridData rvgrid = new GridData(GridData.FILL_BOTH);
	rvgrid.heightHint = 250;
	rvgrid.widthHint = 250;

	_remoteViewer.setLayout(rvlayout);
	_remoteViewer.setLayoutData(rvgrid);


	// progress
	Group progress = new Group(c, SWT.NONE);
	progress.setText("State");
	GridLayout prglayout= new GridLayout();
	progress.setLayout(prglayout);

	GridData progressGrid =  new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER);
	progressGrid.horizontalSpan = 3;
	progress.setLayoutData(progressGrid);

	_progressIndicator = new Text(progress, SWT.SINGLE | SWT.READ_ONLY);
	_progressIndicator.setText("Ready");
	_progressIndicator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
	
	
	getShell().setText(_title);

	return c;
    }

    public void handleEvent(Event e)
    {
	Widget widget = e.widget;
	if (widget == _sendRemote)
	    {
		DataElement target = _remoteViewer.getInput();

		IStructuredSelection es= (IStructuredSelection) _localViewer.getSelection();
		java.util.List list = es.toList();
		for (int i = 0; i < list.size(); i++)
		    {
			DataElement source = (DataElement)list.get(i);			
			TransferFiles transferAction = new TransferFiles("transfer", source, target, this);
			transferAction.start();
		    }

		/***
		DataElement source = _localViewer.getSelected();
		DataElement target = _remoteViewer.getInput();

		TransferFiles transferAction = new TransferFiles("transfer", source, target, this);
		transferAction.start();
		***/
	    }
	else if (widget == _sendLocal)
	    {
		DataElement target = _localViewer.getInput();

		IStructuredSelection es= (IStructuredSelection) _remoteViewer.getSelection();
		java.util.List list = es.toList();
		for (int i = 0; i < list.size(); i++)
		    {
			DataElement source = (DataElement)list.get(i);			
			TransferFiles transferAction = new TransferFiles("transfer", source, target, this);
			transferAction.start();
		    }
		/*
		DataElement source = _remoteViewer.getSelected();
		DataElement target = _localViewer.getInput();

		TransferFiles transferAction = new TransferFiles("transfer", source, target, this);
		transferAction.start();
		*/
	    }
	else if (widget == _localBack)
	    {
		DataElement input = _localViewer.getInput();
		DataElement parent = input.getParent();
		if (parent.getType().equals("directory") || 
		    parent.getType().equals("device") ||
		    parent.getType().equals("data"))
		    {
			_localViewer.setInput(parent);
		    }
	    }
	else if (widget == _remoteBack)
	    {
		DataElement input = _remoteViewer.getInput();
		DataElement parent = input.getParent();
		if (parent.getType().equals("directory") || 
		    parent.getType().equals("device") ||
		    parent.getType().equals("data"))
		    {
			_remoteViewer.setInput(parent);
		    }
	    }
	else if (widget == _localHome)
	    {
		DataElement input = _localViewer.getInput();
		_localViewer.setInput(input.getDataStore().getHostRoot());
	    }
	else if (widget == _remoteHome)
	    {
		DataElement input = _remoteViewer.getInput();
		_remoteViewer.setInput(input.getDataStore().getHostRoot());
	    }
    }

    public void update(String message)
    {
	_progressIndicator.setText(message);
    }
}
