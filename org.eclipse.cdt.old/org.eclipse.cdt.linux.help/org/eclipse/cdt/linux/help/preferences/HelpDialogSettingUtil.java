package com.ibm.linux.help.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.linux.help.*;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.DialogSettings;

import java.lang.*;

public class HelpDialogSettingUtil
{
    public static void updateDisplayedSettings()
    {	
	StringBuffer displayedSettings= new StringBuffer();
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();
	displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_TITLE)+":");

	if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT))
	    {
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_EXACT));
	    }
	else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS))
	    {
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_SUBSTRING));
	    }
	else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP))
	    {		
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_REGEXP));
	    }
	displayedSettings.append("  "+HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_TITLE)+":");
	if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL))
	    {
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_ALL));
	    }
	else
	    {
		boolean scopeSelected = false;
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN))
		    {		 
			displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_MAN));
			scopeSelected = true;
		    }
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO))
		    {
			if(scopeSelected) displayedSettings.append(","); 
			displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_INFO));
			scopeSelected = true;
		    }
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML))
		    {
			if(scopeSelected) displayedSettings.append(","); 
			displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_HTML));
		    }
	    }
	HelpPlugin.getDefault().getView().setLabelSettings(displayedSettings.toString());
    }
   
    public static void setDefaultSettings()
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();
	if ( !(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP) ) )
	    {
		// Set default search MODE to SUBSTRING.
		settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS, true);
	    }
	if ( !(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO) ||
	      settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML) ) )
	    {
		// Set default search TYPE to MAN,INFO.
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN, true);
		settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO, true);
	    }	
    }

}
