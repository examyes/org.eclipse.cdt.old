package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.cdt.linux.help.*;
import org.eclipse.cdt.linux.help.display.*;

import java.util.*;

public class IndexPageControl extends Composite implements Listener
{
    private HelpPlugin _plugin;
    
    private Text whatisPath;
    private Text dirPath;

    private Label whatisLabel;
    private Label dirLabel;

    private  IndexPathControl _indexControl;

    private boolean _isRemote = false;

    public IndexPageControl(Composite parent, int style)
    {
	super(parent, style);
    	
	_plugin = HelpPlugin.getDefault();

	Composite container = new Composite(this,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	layout.makeColumnsEqualWidth=true;
	layout.marginWidth=0;
	container.setLayout(layout);
	container.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	
	// The LOCATION group
	Group locationGroup= new Group(container,SWT.NULL);	
	locationGroup.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PATH_TITLE));
	GridLayout locationLayout= new GridLayout();
	locationLayout.numColumns=1;
	GridData locationGridData = new GridData(GridData.FILL_HORIZONTAL);
	locationGroup.setLayoutData(locationGridData);
	locationGroup.setLayout(locationLayout);
	
	whatisLabel = new Label(locationGroup,SWT.NONE);
	whatisLabel.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PATH_MAN));
	
	whatisPath = new Text(locationGroup,SWT.SINGLE|SWT.BORDER);
	GridData whatisPathGridData= new GridData(GridData.FILL_HORIZONTAL);
	whatisPath.setLayoutData(whatisPathGridData);
	
	dirLabel = new Label(locationGroup,SWT.NONE);
	dirLabel.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PATH_INFO));
	dirPath = new Text(locationGroup,SWT.SINGLE|SWT.BORDER);
	GridData dirPathGridData= new GridData(GridData.FILL_HORIZONTAL);
	dirPath.setLayoutData(dirPathGridData);
	
	_indexControl = new IndexPathControl(container, SWT.NONE, _isRemote);
	GridLayout dlayout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);
	_indexControl.setLayout(dlayout);
	_indexControl.setLayoutData(dData);
	
	loadSettingsToWidget();

	checkOS();
    }

    public void handleEvent(Event e)
    {

    }
    
    public void performOk()
    {		
	//store all settings
	storeSettings();

	if(!_indexControl.isEmpty())
	    {
		//may need to create an Index
		_indexControl.checkIndexCreation();
	    }
    }

    public void performDefaults()
    {
	whatisPath.setText("/var/cache/man/whatis");
	dirPath.setText("/usr/share/info/dir");
    }

    private void loadSettingsToWidget()
    {
	HelpSettings settings = new HelpSettings(_isRemote);
	settings.read();
	String whatisLocation = settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS);
	if(whatisLocation != null)
	    whatisPath.setText(whatisLocation);
	String dirLocation = settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR);
	if(dirLocation != null)
	    dirPath.setText(dirLocation);
    }

    private void storeSettings()
    {
	_indexControl.storeSettings();

	HelpSettings settings = new HelpSettings(_isRemote);
	settings.read();
	settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,whatisPath.getText());
	settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,dirPath.getText());
	settings.write(); //commit to disk
    }

    private void checkOS()
    {
	if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
	{
	    whatisLabel.setEnabled(false);
	    dirLabel.setEnabled(false);

	    whatisPath.setEnabled(false);
	    dirPath.setEnabled(false);
	}

   }
}
