package org.eclipse.cdt.linux.help;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

public interface IHelpSearchConstants
{
    public static final String HELP_SEARCH_TYPE_EXACT="help_search_type_exact";
    public static final String HELP_SEARCH_TYPE_CONTAINS="help_search_type_contains";
    public static final String HELP_SEARCH_TYPE_REGEXP="help_search_type_regexp";
    
    public static final String HELP_SEARCH_SCOPE_ALL="help_search_scope_all";
    public static final String HELP_SEARCH_SCOPE_MAN="help_search_scope_man";
    public static final String HELP_SEARCH_SCOPE_INFO="help_search_scope_info";
    public static final String HELP_SEARCH_SCOPE_HTML="help_search_scope_html";
    
    public static final String HELP_SEARCH_BROWSER_KONQUEROR="help_search_browser_konqueror";
    public static final String HELP_SEARCH_BROWSER_GNOMEHELPBROWSER="help_search_browser_gnomehelpbrowser";

    public static final String HELP_SEARCH_PATH_WHATIS="help_search_path_whatis";
    public static final String HELP_SEARCH_PATH_DIR="help_search_path_dir";

    public static final String HELP_SETTINGS_INDEXLOCATION="help_settings_indexlocation";
    public static final String HELP_SETTINGS_PATHSTOINDEX="help_settings_pathstoindex";
    public static final String HELP_SETTINGS_PATHSMODIFIED="help_settings_pathsmodified";
    public static final String HELP_SETTINGS_INDEXEXISTS="help_settings_indexexists";

    //---- Filter constants
    public static final String HELP_FILTER_PATTERNS="help_filter_patterns";

}
