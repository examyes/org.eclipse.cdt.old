package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.dstore.ui.connections.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import org.eclipse.core.resources.*;

import java.io.*;
import java.util.*;

import org.eclipse.ui.dialogs.*;

public class RemotePropertyPage extends PropertyPage
{	
    private Text _remoteHostNameField;
    private Text _remoteHostPortNumberField;
    private Text _remoteHostDirectoryField;
    private Text _remoteHostMountField;
    private Button _remoteHostDaemonButton;

    private static final int SIZING_TEXT_FIELD_WIDTH = 100;
    private static final int SIZING_INDENTATION_WIDTH = 10;
    
  public RemotePropertyPage()
      {
        super();
      }


  protected Control createContents(Composite parent)
      {
        IProject project= getProject();
	
        if (CppPlugin.isCppProject(project) && (project instanceof Repository))
	    {
		Composite properties = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		properties.setLayout(layout);
		properties.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		// Host name label
		Label hostNameLabel = new Label(properties, SWT.NONE);
		hostNameLabel.setText("Name:");
		
		// Host name entry field
		_remoteHostNameField = new Text(properties,SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		_remoteHostNameField.setLayoutData(data);
			
		// port number label
		Label portNumberLabel = new Label(properties,SWT.NONE);
		portNumberLabel.setText("Port Number:");
	
		// Port number entry field
		_remoteHostPortNumberField = new Text(properties,SWT.BORDER);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		_remoteHostPortNumberField.setLayoutData(data);

		// host directory name label
		Label directoryLabel = new Label(properties,SWT.NONE);
		directoryLabel.setText("Directory:");
		
		// Directory name entry field
		_remoteHostDirectoryField = new Text(properties,SWT.BORDER);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		_remoteHostDirectoryField.setLayoutData(data);
		
		// mounted directory name label
		Label mountedDirectoryLabel = new Label(properties,SWT.NONE);
		mountedDirectoryLabel.setText("Local Mount Point (Optional):");
		
		// mounted Directory name entry field
		_remoteHostMountField = new Text(properties,SWT.BORDER);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		_remoteHostMountField.setLayoutData(data);

		// is daemon
		_remoteHostDaemonButton = new Button(properties, SWT.CHECK);
		_remoteHostDaemonButton.setText("Connect to using daemon");

		performDefaults();
		return properties;
	    }
        else
	    {
		Composite cnr = new Composite(parent, SWT.NONE);
		Label label = new Label(cnr, SWT.NULL);
		label.setText("Not a C/C++ Project");
		return cnr;
	    }
      }
    
  protected void performDefaults()
      {
	  super.performDefaults();

	  Repository project = (Repository)getProject();
	  Connection connection = project.getConnection();

	  _remoteHostNameField.setText(connection.getHost());
	  _remoteHostPortNumberField.setText(connection.getPort());
	  _remoteHostDirectoryField.setText(connection.getDir());
	  _remoteHostDaemonButton.setSelection(connection.isUsingDaemon());

	  ArrayList mountPoints = CppPlugin.readProperty(project, "Mount Point");
	  if (mountPoints != null && mountPoints.size() > 0)
	      {
		  _remoteHostMountField.setText((String)mountPoints.get(0));		  
	      }
      }
	
  public boolean performOk()
      {
	  Repository project = (Repository)getProject();
	  Connection connection = project.getConnection();

	  connection.setHost(_remoteHostNameField.getText());
	  connection.setPort(_remoteHostPortNumberField.getText());
	  connection.setDir(_remoteHostDirectoryField.getText());
	  connection.setIsUsingDaemon(_remoteHostDaemonButton.getSelection());
	  
	  project.changePath(_remoteHostDirectoryField.getText());

	  ArrayList mountPoints = new ArrayList();
	  mountPoints.add(new String(_remoteHostMountField.getText()));
	  CppPlugin.writeProperty(getProject(), "Mount Point",  mountPoints);
	  return true;
      }

  private IProject getProject()
      {
        return (IProject)getElement();
      }
}
