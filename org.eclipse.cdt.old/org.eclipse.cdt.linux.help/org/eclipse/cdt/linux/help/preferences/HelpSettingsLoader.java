package org.eclipse.cdt.linux.help.preferences;

import org.eclipse.jface.dialogs.*;
import org.eclipse.cdt.linux.help.*;

public class HelpSettingsLoader
{

    //load()/save() are called from the HelpPlugin when it is constructed/disposed

    public static void load(HelpSettings hSettings)
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();

	hSettings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP));

	hSettings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML));

	hSettings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,
		     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER));

	hSettings.put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,
		     settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS));
	hSettings.put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,
		     settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR));
	/*
	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,
		     settings.get(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION));
	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,
		     settings.get(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX));
		     */
	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,
		     ".");
	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,
		     ".");
		     

	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,
		     settings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED));
	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,
		     settings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS));

	hSettings.put(IHelpSearchConstants.HELP_FILTER_PATTERNS,
		     settings.get(IHelpSearchConstants.HELP_FILTER_PATTERNS));

	hSettings.put(IHelpSearchConstants.HELP_SETTINGS_SELECTED,
		     settings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_SELECTED));
	
	hSettings.put(IHelpSearchConstants.HELP_TOMCAT_PORT,
		     settings.get(IHelpSearchConstants.HELP_TOMCAT_PORT));

    }

    public static void save(HelpSettings hSettings)
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();

	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT));
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS));
	settings.put(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP));

	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL));
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN));
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO));
	settings.put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML));

	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR));
	settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER));

	settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,
		     hSettings.get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS));
	settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,
		     hSettings.get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR));

	settings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,
		     hSettings.get(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION));
	settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,
		     hSettings.get(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX));
	settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED));
	settings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS));

	settings.put(IHelpSearchConstants.HELP_FILTER_PATTERNS,
		     hSettings.get(IHelpSearchConstants.HELP_FILTER_PATTERNS));

	settings.put(IHelpSearchConstants.HELP_SETTINGS_SELECTED,
		     hSettings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_SELECTED));

	settings.put(IHelpSearchConstants.HELP_TOMCAT_PORT,
		     hSettings.get(IHelpSearchConstants.HELP_TOMCAT_PORT));
    }

}
