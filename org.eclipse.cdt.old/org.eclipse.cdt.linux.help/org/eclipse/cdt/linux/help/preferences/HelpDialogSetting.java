package com.ibm.linux.help.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.linux.help.*;

import com.ibm.linux.help.display.HelpBrowserUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

public class HelpDialogSetting extends Dialog implements SelectionListener
{
    private HelpPlugin _plugin;

    private Button radioButton1;
    private Button radioButton2;
    private Button radioButton3;

    private Button checkBox1;
    private Button checkBox2;
    private Button checkBox3;
    private Button checkBox4;    

    private Button radioBrowser1;
    private Button radioBrowser2;

    private Text whatisPath;
    private Text dirPath;

    private Label whatisLabel;
    private Button whatisLoadButton;
    private Label dirLabel;
    private Button dirLoadButton;

    public IndexPathControl _indexControl;

    public HelpDialogSetting(Shell parentShell)
    {
	super(parentShell);
	_plugin=HelpPlugin.getDefault();
    }

    protected void configureShell(Shell newShell)
    {
	super.configureShell(newShell);	
	newShell.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_TITLE));

	//FIXME:configure help here
	// Workbench.setHelp(newShell,new String[] {"stuff"});
    }

    protected Control createDialogArea(Composite parent)
    {
	Composite composite=(Composite)super.createDialogArea(parent);

	GridLayout compositeLayout = new GridLayout();	
	composite.setLayout(compositeLayout);

	// The TYPE group
	Group typeGroup= new Group(composite,SWT.NULL);	
	typeGroup.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_TITLE));

	GridData groupGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL);	
	typeGroup.setLayoutData(groupGridData);

	GridLayout typeLayout = new GridLayout();
	typeLayout.numColumns = 3;
	typeLayout.makeColumnsEqualWidth=true;	
	typeGroup.setLayout(typeLayout);

	GridData typeData= new GridData(GridData.FILL_HORIZONTAL);
	typeGroup.setLayoutData(typeData);
		
	radioButton1 = createRadioButton(typeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_EXACT));
	radioButton2 = createRadioButton(typeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_SUBSTRING));
	radioButton3 = createRadioButton(typeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_REGEXP));	

	Composite container = new Composite(composite,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.makeColumnsEqualWidth=true;
	layout.marginWidth=0;
	container.setLayout(layout);
	container.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	//The SCOPE group
	Group scopeGroup= new Group(container,SWT.NULL);	
	scopeGroup.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_TITLE));

	GridData scopeGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |GridData.GRAB_HORIZONTAL| GridData.VERTICAL_ALIGN_FILL);
	scopeGridData.horizontalSpan=1;
	scopeGroup.setLayoutData(scopeGridData);

	GridLayout scopeLayout = new GridLayout();	
	scopeGroup.setLayout(scopeLayout);	
		
	checkBox1=createCheckBox(scopeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_ALL));
	checkBox2=createCheckBox(scopeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_MAN));
	checkBox3=createCheckBox(scopeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_INFO));
	checkBox4=createCheckBox(scopeGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_HTML));

	//The BROWSER Group
	Group browserGroup = new Group(container,SWT.NULL);	
	browserGroup.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_BROWSER_TITLE));
	GridLayout browserLayout = new GridLayout();
	
	GridData browserGridData=new GridData(GridData.HORIZONTAL_ALIGN_FILL |GridData.VERTICAL_ALIGN_FILL);
	browserGroup.setLayoutData(browserGridData);
	browserGroup.setLayout(browserLayout);
			
	radioBrowser1=createRadioButton(browserGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_BROWSER_KDE));
	radioBrowser2=createRadioButton(browserGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_BROWSER_GNOME));		

	// The LOCATION group
	Group locationGroup= new Group(composite,SWT.NULL);	
	locationGroup.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PATH_TITLE));
	GridLayout locationLayout= new GridLayout();
	locationLayout.numColumns=2;
	GridData locationGridData = new GridData(GridData.FILL_HORIZONTAL);
	locationGroup.setLayoutData(locationGridData);
	locationGroup.setLayout(locationLayout);
		
	whatisLabel = new Label(locationGroup,SWT.NONE);
	whatisLabel.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PATH_MAN));

	whatisPath = new Text(locationGroup,SWT.SINGLE|SWT.BORDER);
	GridData whatisPathGridData= new GridData(GridData.FILL_HORIZONTAL);
	whatisPath.setLayoutData(whatisPathGridData);
	/***
	whatisLoadButton = new Button(locationGroup,SWT.PUSH);
	whatisLoadButton.setText("Load whatis");
	whatisLoadButton.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e)
		{
		    HelpSearch.loadMan();
		}		
	    });
	***/
	
	dirLabel = new Label(locationGroup,SWT.NONE);
	dirLabel.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PATH_INFO));
	dirPath = new Text(locationGroup,SWT.SINGLE|SWT.BORDER);
	GridData dirPathGridData= new GridData(GridData.FILL_HORIZONTAL);
	dirPath.setLayoutData(dirPathGridData);
	/*****
	dirLoadButton = new Button(locationGroup,SWT.PUSH);
	dirLoadButton.setText("Load dir");
	dirLoadbutton.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e)
		{
		    HelpSearch.loadInfo();
		}		
	    });
	***/
	
	// add some defaults settings if appropriate
	checkDefaultSettings();	
		
	_indexControl = new IndexPathControl(composite,SWT.NONE);

	GridLayout dlayout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);
	
	_indexControl.setLayout(dlayout);
	_indexControl.setLayoutData(dData);	
		
	// set all widgets to the current settings
	loadSettingsToWidgets();

	return composite;
    }

    // set all widgets to the current settings
    private void loadSettingsToWidgets()
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();	
	radioButton1.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT));
	radioButton2.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS));
	radioButton3.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP));

	checkBox1.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL));
	checkBox2.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN));
	checkBox3.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO));
	checkBox4.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML));
	
	if(radioBrowser1.isEnabled())
	    radioBrowser1.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR));
	if(radioBrowser2.isEnabled())
	    radioBrowser2.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER));

	String whatisLocation = settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS);
	if(whatisLocation != null)
	    whatisPath.setText(whatisLocation);
	String dirLocation = settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR);
	if(dirLocation != null)
	    dirPath.setText(dirLocation);
	
	if(!checkBox2.getSelection())
	    {
		whatisLabel.setEnabled(false);
		whatisPath.setEnabled(false);
	    }
	if(!checkBox3.getSelection())
	    {
		dirLabel.setEnabled(false);
		dirPath.setEnabled(false);
	    }
	if(!radioButton1.getSelection())
	    {
		checkBox4.setSelection(false);
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML, checkBox4.getSelection());
		checkBox4.setEnabled(false);	
		_indexControl.setEnabled(false);

		checkBox1.setSelection(false);
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL, checkBox1.getSelection());
		checkBox1.setEnabled(false);
	    }	

	String theOs = System.getProperty("os.name");
	if (theOs.toLowerCase().startsWith("window"))
	    {	
		if(radioButton1.getSelection())
		    {
			radioButton2.setEnabled(false);
			radioButton3.setEnabled(false);
			checkBox1.setEnabled(false);
			checkBox2.setEnabled(false);
			checkBox3.setEnabled(false);
		    }	
	    }	

    }

    private Button createRadioButton(Composite parent, String label)
    {
	Button button = new Button(parent,SWT.RADIO|SWT.LEFT);
	button.setText(label);
	button.addSelectionListener(this);
	return button;
    }

    private Button createCheckBox(Composite group, String label)
    {
	Button button = new Button(group, SWT.CHECK|SWT.LEFT);
	button.setText(label);
	button.addSelectionListener(this);
	return button;
    }

    public void widgetDefaultSelected(SelectionEvent e)
    {	
    }

    public void widgetSelected(SelectionEvent e)
    {	
	// WiNDOWS specific
	String theOs = System.getProperty("os.name");
	if (theOs.toLowerCase().startsWith("window"))
	    {
		if(radioButton1==(Button)e.getSource())
		    {
			return ;
		    }
	    }


	if (radioButton1==(Button)e.getSource())
	    {
		if(radioButton1.getSelection())
		    {
			checkBox4.setEnabled(true);
			checkBox1.setEnabled(true);
		    }
		else	
		    {
			checkBox4.setSelection(false);
			checkBox4.setEnabled(false);
			_indexControl.setEnabled(false);
			checkBox1.setSelection(false);
			checkBox1.setEnabled(false);
		    }
	    }	    
	else if (radioButton2==(Button)e.getSource() ||
		 radioButton3==(Button)e.getSource())
	    {
		if(radioButton2.getSelection())
		    {
			checkBox4.setSelection(false);
			checkBox4.setEnabled(false);
			_indexControl.setEnabled(false);
			checkBox1.setSelection(false);
		    }
	    }	    	
	
	if(checkBox1==(Button)e.getSource())
	    {	   		
		if(checkBox1.getSelection())
		    {
			checkBox2.setSelection(true);
			checkBox3.setSelection(true);
			checkBox4.setSelection(true);
			whatisLabel.setEnabled(true);
			whatisPath.setEnabled(true);
			dirLabel.setEnabled(true);
			dirPath.setEnabled(true);
			_indexControl.setEnabled(true);			

		    }
		else
		    {
			checkBox2.setSelection(false);
			checkBox3.setSelection(false);
			checkBox4.setSelection(false);
			whatisLabel.setEnabled(false);
			whatisPath.setEnabled(false);
			dirLabel.setEnabled(false);
			dirPath.setEnabled(false);
			_indexControl.setEnabled(false);
		    }		
	    }
	else if(checkBox2==(Button)e.getSource())
	    {
		if(checkBox2.getSelection())
		    {
			whatisLabel.setEnabled(true);
			whatisPath.setEnabled(true);
		    }
		else
		    {
			checkBox1.setSelection(false);//disable ALL
			whatisLabel.setEnabled(false);
			whatisPath.setEnabled(false);
		    }
	    }
	else if(checkBox3==(Button)e.getSource())
	    {
		if(checkBox3.getSelection())
		    {
			dirLabel.setEnabled(true);
			dirPath.setEnabled(true);
		    }
		else
		    {
			checkBox1.setSelection(false);//disable ALL
			dirLabel.setEnabled(false);
			dirPath.setEnabled(false);
		    }
	    }
	else if(checkBox4==(Button)e.getSource())
	    {
		if(checkBox4.getSelection())
		    {
			_indexControl.setEnabled(true);
		    }
		else
		    {
			_indexControl.setEnabled(false);
			checkBox1.setSelection(false); //disable ALL
		    }
	    }	   
	
	
    }
    
    private void checkIndexCreation()
    {
	_indexControl.checkIndexCreation();
    }    

    private void storeSettings()
    {
	//store all settings
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT, radioButton1.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS, radioButton2.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP, radioButton3.getSelection());

	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL, checkBox1.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN, checkBox2.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO, checkBox3.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML, checkBox4.getSelection());


	////////buttons may not be enabled!!!
	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR, radioBrowser1.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER, radioBrowser2.getSelection());

	settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS, whatisPath.getText());
	settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR, dirPath.getText());

	_indexControl.storeSettings();
    }


    public void okPressed()
    {		

	//store all settings
	storeSettings();

	if(!_indexControl.isEmpty())
	    {
		//may need to create an Index
		checkIndexCreation();
	    }
	
	super.okPressed();

	//update the label with the new settings.
	HelpDialogSettingUtil.updateDisplayedSettings();
    }
   

    // check if settings exist. If not, set some defaults.
    private void checkDefaultSettings()
    {	
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();

	// WINDOWS specific
	String theOs = System.getProperty("os.name");
	if (theOs.toLowerCase().startsWith("window"))
	    {
		//Only html(i.e exact) with Windows. 		
		settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT, true);
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML, true);
	    }
	
	//set the default search TYPE if appropriate
	if ( !(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP) ) )
	    {		
		// Set default search TYPE to SUBSTRING.
		radioButton2.setSelection(true);
		settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS, true);
	    }
	
	//set the default search SCOPE if appropriate
	if ( !(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML) ) )
	    {
		// Set default search SCOPE to MAN,INFO.
		checkBox2.setSelection(true);
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN, true);
		checkBox3.setSelection(true);
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO, true);		
	    }	

	// activate/deactivate browsers if they are present/absent.
	boolean kdeBrowserExists=HelpBrowserUtil.existsCommand("konqueror");
	boolean gnomeBrowserExists=HelpBrowserUtil.existsCommand("gnome-help-browser");	
	if (kdeBrowserExists)
	    {
		radioBrowser1.setEnabled(true);
	    }
	else
	    {
		radioBrowser1.setEnabled(false);
	    }
	
	if (gnomeBrowserExists)
	    {
		radioBrowser2.setEnabled(true);
	    }
	else
	    {
		radioBrowser2.setEnabled(false);
	    }	

	//set a default browser if needed
	if (!(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR) ||
	     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER)))
	    {		
		if(kdeBrowserExists)
		    {
			radioBrowser1.setSelection(true);
			settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,true);
		    }
		else if(gnomeBrowserExists)
		    {
			radioBrowser2.setSelection(true);
			settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,true);
		    }
				
	    }
	
	//set the default path to the 'whatis' database(Used to search for MAN pages) if needed
	if (settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS)==null)
	    {
		//set default path to 'whatis'
		String thePath = HelpSearchUtil.getDefaultWhatisPath();
		if(thePath!=null)
		    settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,thePath);
	    }
	
	//set the default path to the Top 'dir' INFO node if needed
	if (settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR)==null) 
	    {
		//set default path to the top 'dir' node
		String thePath= HelpSearchUtil.getDefaultDirPath();
		if(thePath!=null)
		    settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,thePath);
	    }
    }

    protected void cancelPressed()
    {
	//FIXME: ADD stuff here

	super.cancelPressed();
    }

}
