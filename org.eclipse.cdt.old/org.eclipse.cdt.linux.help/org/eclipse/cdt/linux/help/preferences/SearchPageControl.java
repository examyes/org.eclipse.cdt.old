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

public class SearchPageControl extends Composite implements Listener
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
    private Button radioBrowser3; 
    
    private boolean _isRemote = false;

    public SearchPageControl(Composite parent, int style)
    {
	super(parent, style);
    	
	_plugin = HelpPlugin.getDefault();

	
	// The TYPE group
	Group typeGroup= new Group(this,SWT.NULL);	
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

	Composite container = new Composite(this,SWT.NONE);
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
	radioBrowser3=createRadioButton(browserGroup,_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_BROWSER_DEFAULT));
 
	checkBrowsers();
	loadSettingsToWidget();
    }

    public void handleEvent(Event e)
    {
	Widget source = e.widget;
	if (radioButton1==source)
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
			checkBox1.setSelection(false);
			checkBox1.setEnabled(false);
		    }
	    }	    
	else if (radioButton2==source || radioButton3==source)
	    {
		if(radioButton2.getSelection()|| radioButton3.getSelection())
		    {
			checkBox4.setSelection(false);
			checkBox4.setEnabled(false);
			checkBox1.setSelection(false);
		    }
	    }	

    	if(checkBox1==source)
	    {	   		
		if(checkBox1.getSelection())
		    {
			checkBox2.setSelection(true);
			checkBox3.setSelection(true);
			checkBox4.setSelection(true);			
		    }
		else
		    {
			checkBox2.setSelection(false);
			checkBox3.setSelection(false);
			checkBox4.setSelection(false);
   		    }		
	    }
	else if(checkBox2==source)
	    {
		if(!checkBox2.getSelection())
		    {
			checkBox1.setSelection(false);//disable ALL
		    }
	    }
	else if(checkBox3==source)
	    {
		if(!checkBox3.getSelection())
		    {    
			checkBox1.setSelection(false);//disable ALL
		    }
	    }
	else if(checkBox4==source)
	    {
		if(!checkBox4.getSelection())
		    {
			checkBox1.setSelection(false); //disable ALL
		    }
	    }	   
    }
    
    public void performOk()
    {		
	//store all settings
	storeSettings();
    }
    public void performDefaults()
    {
	radioButton1.setSelection(false);
	radioButton2.setSelection(true);
	radioButton3.setSelection(false);


	checkBox1.setSelection(false);
	checkBox2.setSelection(true);
	checkBox3.setSelection(true);
	checkBox4.setSelection(false);
	
	checkBox1.setEnabled(false);
	checkBox2.setEnabled(true);
	checkBox3.setEnabled(true);
	checkBox4.setEnabled(false);
	
	boolean kdeBrowserExists=HelpBrowserUtil.existsCommand("konqueror");
	boolean gnomeBrowserExists=HelpBrowserUtil.existsCommand("gnome-help-browser");	
	if(kdeBrowserExists)
	    {
		radioBrowser1.setSelection(true);
		radioBrowser2.setSelection(false);
		radioBrowser3.setSelection(false);
	    }
	else if(gnomeBrowserExists)
	    {
		radioBrowser1.setSelection(false);
		radioBrowser2.setSelection(true);
		radioBrowser3.setSelection(false);
	    }
	else
	    {
		radioBrowser1.setSelection(false);
		radioBrowser2.setSelection(false);
		radioBrowser3.setSelection(true);
	    }
    }


    private void checkBrowsers()
    {
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
    }

    private void loadSettingsToWidget()
    {
	HelpSettings settings = new HelpSettings(_isRemote);
	settings.read();

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
	radioBrowser3.setSelection(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT));
	
	if(!radioButton1.getSelection())
	    {
		checkBox4.setSelection(false);
		checkBox4.setEnabled(false);	

		checkBox1.setSelection(false);
		checkBox1.setEnabled(false);
	    }	
    }

    private void storeSettings()
    {
	HelpSettings settings = new HelpSettings(_isRemote);
	settings.read();
	
	settings.put(IHelpSearchConstants.HELP_SETTINGS_SELECTED,true); //indicate user selection

	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT, radioButton1.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS, radioButton2.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP, radioButton3.getSelection());

	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL, checkBox1.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN, checkBox2.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO, checkBox3.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML, checkBox4.getSelection());

	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR, radioBrowser1.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER, radioBrowser2.getSelection());
	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT, radioBrowser3.getSelection());
  
	ArrayList preferences =settings.getPreferences();
	settings.write(); //commit to disk
    }

    private Button createRadioButton(Composite parent, String label)
    {
	Button button = new Button(parent,SWT.RADIO|SWT.LEFT);
	button.setText(label);
	button.addListener(SWT.Selection,this);
	return button;
    }

    private Button createCheckBox(Composite group, String label)
    {
	Button button = new Button(group, SWT.CHECK|SWT.LEFT);
	button.setText(label);
	button.addListener(SWT.Selection,this);
	return button;
    }
}
