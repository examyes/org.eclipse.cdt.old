package org.eclipse.cdt.cpp.ui.internal.help;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.core.resources.*;

import java.util.*;
import java.io.*;

public class HelpSettings
{
    private class StringSetting
    {
	private class StringItem
	{
	    public String _key = null;
	    public String _value = null;
	    
	    public StringItem(String key,String value)
	    {
		_key=key;
		_value=value;
	    }
	}

	private ArrayList _stringList = null;

	public StringSetting()
	{
	    _stringList = new ArrayList();
	}

	public String get(String key)
	{
	   StringItem item = find(key);
	   if (item != null)
	       {
		   return item._value;
	       }
	   else
	       {
		   //  System.out.println("No value found for String:"+key);
		   return null;
	       }
	}

	public void setValue(String key, String value)
	{
	    StringItem item = find(key);
	    if (item != null)
		{
		    item._value = value;
		}
	    else
		{
		    item = new StringItem(key,value);
		    _stringList.add(item);
		}
	 
	}

	private StringItem find(String key)
	{
	    for(int i=0;i<_stringList.size();i++)
		{
		    StringItem item = (StringItem)_stringList.get(i);
		    if(item._key.equals(key))
			{
			    return item;
			}
		}
	    return null;
	}
    }

    private class BooleanSetting
    {
	private class BooleanItem
	{
	    public String _key = null;
	    public boolean _value = false;
	    
	    public BooleanItem(String key,boolean value)
	    {
		_key=key;
		_value=value;
	    }

	}

	private ArrayList _booleanList = null;

	public BooleanSetting()
	{
	    _booleanList = new ArrayList();
	}

	public boolean get(String key)
	{
	    BooleanItem item = find(key);
	    if (item != null)
		{
		    return item._value;
		}
	    else
		{
		    //System.out.println("No value found for:"+key);
		    return false;
		}
	}

	public void setValue(String key, boolean value)
	{
	    BooleanItem item = find(key);
	    if (item != null)
		{
		    item._value = value;
		}
	    else
		{
		    item = new BooleanItem(key,value);
		    _booleanList.add(item);
		}
	 
	}

	private BooleanItem find(String key)
	{
	    for(int i=0;i<_booleanList.size();i++)
		{
		    BooleanItem item = (BooleanItem)_booleanList.get(i);
		    if(item._key.equals(key))
			{
			    return item;
			}
		}
	    return null;
	}
    }

    private StringSetting _stringPreferences = null;
    private BooleanSetting _booleanPreferences = null;

    private boolean _isRemote;
    private StringSetting _stringProperties = null;
    private BooleanSetting _booleanProperties = null;

    private String delimiter="||";

    public HelpSettings()
    {
	this(false);
    }

    public HelpSettings(boolean isRemote)
    {
	//Preferences(for Local)
	_stringPreferences = new StringSetting();
	_booleanPreferences = new BooleanSetting();

	//Properties(for Remote)
	this._isRemote = isRemote;
	_stringProperties = new StringSetting();
	_booleanProperties = new BooleanSetting();
    }

    //----------getters and setters for Properties
    public boolean getBooleanProperty(String key)
    {
	return _booleanProperties.get(key);
    }
     public String getProperty(String key)
    {
	return _stringProperties.get(key);
    }
    public void putProperty(String key, String value)
    {
	_stringProperties.setValue(key,value);
    }
    public void putProperty(String key, boolean value)
    {
	_booleanProperties.setValue(key,value);
    }

    //------------getters and setters for Preferences
    public boolean getBooleanPreference(String key)
    {
	return _booleanPreferences.get(key);
    }
     public String getPreference(String key)
    {
	return _stringPreferences.get(key);
    }
    public void putPreference(String key, String value)
    {
	_stringPreferences.setValue(key,value);
    }
    public void putPreference(String key, boolean value)
    {
	_booleanPreferences.setValue(key,value);
    }
    

    //----getters ----
    public boolean getBoolean(String key)
    {
	if(_isRemote && isPropertyName(key))
	    return getBooleanProperty(key);
	else
	    return getBooleanPreference(key);
    }

    public String get(String key)
    {
	if(_isRemote && isPropertyName(key))
	    return getProperty(key);
	else
	    return getPreference(key);
    }

    //----setters-----
    public void put(String key, String value)
    {
	if(_isRemote && isPropertyName(key))
	    putProperty(key,value);
	else
	    putPreference(key,value);
    }
    public void put(String key, boolean value)
    {
	if(_isRemote && isPropertyName(key))
	    putProperty(key,value);
	else
	    putPreference(key,value);
    }

    

    public String settingsToString()
    {
	StringBuffer settings= new StringBuffer();
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT))).append(delimiter);
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS))).append(delimiter);
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP))).append(delimiter);

	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL))).append(delimiter);
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN))).append(delimiter);
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO))).append(delimiter);
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML))).append(delimiter);

	settings.append(get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS)).append(delimiter);
	settings.append(get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR)).append(delimiter);
	
	String indexLocation = get(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION);
	if(indexLocation==null)
	    settings.append("null").append(delimiter);
	else
	    settings.append(indexLocation).append(delimiter);
	
	String pathsToIndex = get(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX);
	if(pathsToIndex==null)
	    settings.append("null").append(delimiter);
	else
	    settings.append(pathsToIndex).append(delimiter);

	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED))).append(delimiter);
	settings.append(String.valueOf(getBoolean(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS))).append(delimiter);

	settings.append(get(IHelpSearchConstants.HELP_TOMCAT_PORT)).append(delimiter);

	return settings.toString();
    }

    public void write()
    {
	IProject project = CppPlugin.getCurrentProject();
	
	//write LOCAL preference
	CppPlugin.writeProperty("Help",getPreferences());
	
	if(_isRemote)
	    {
		// write REMOTE property
		CppPlugin.writeProperty(project,"Help",getProperties());
	    }
    }

    public void read()
    {
	// read preferences
	ArrayList preferences = CppPlugin.readProperty("Help");

	ArrayList properties=null;	
	if(_isRemote)
	    {
		//read REMOTE properties
		IProject project = CppPlugin.getCurrentProject();
	        properties = CppPlugin.readProperty(project,"Help");
	    }

	//set default preferences if none exist 
	if(preferences.size()==0)
	    {
		putDefaultPreferences();
	    }
	else
	    {
		putPreferences(preferences);
	    }

	//set default properties if none exist(only for remote projects)
	if(_isRemote && properties!=null)
	    {
		if (properties.size()==0)
		    {
			putDefaultProperties();
		    }
		else
		    {
			putProperties(properties);
		    }
	    }		
    }
    
    //return a list of strings in 'name=value' form
    private ArrayList getPreferences()
    {
	ArrayList result = new ArrayList();

	result.add(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT)));
	result.add(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS)));
	result.add(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP)));

	result.add(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL)));
	result.add(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN)));
	result.add(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO)));
	result.add(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML)));
	
	result.add(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR)));
	result.add(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER)));
	result.add(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT)));
	
	result.add(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS + "="+ 
		   getPreference(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS));
	result.add(IHelpSearchConstants.HELP_SEARCH_PATH_DIR + "="+ 
		   getPreference(IHelpSearchConstants.HELP_SEARCH_PATH_DIR));

	result.add(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION + "="+ 
		   getPreference(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION));
	result.add(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX + "="+ 
		   getPreference(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX));

	result.add(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED)));
	result.add(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS)));

	result.add(IHelpSearchConstants.HELP_FILTER_PATTERNS + "="+ 
		   getPreference(IHelpSearchConstants.HELP_FILTER_PATTERNS));

	result.add(IHelpSearchConstants.HELP_SETTINGS_SELECTED + "="+ 
		   String.valueOf(getBooleanPreference(IHelpSearchConstants.HELP_SETTINGS_SELECTED)));

	result.add(IHelpSearchConstants.HELP_TOMCAT_PORT + "="+ 
		   getPreference(IHelpSearchConstants.HELP_TOMCAT_PORT));
	return result;
    }

    //return a list of strings in 'name=value' form
    private ArrayList getProperties()
    {
	ArrayList result = new ArrayList();
	
	result.add(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS + "="+ 
		   getProperty(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS));
	result.add(IHelpSearchConstants.HELP_SEARCH_PATH_DIR + "="+ 
		   getProperty(IHelpSearchConstants.HELP_SEARCH_PATH_DIR));

	result.add(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION + "="+ 
		   getProperty(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION));
	result.add(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX + "="+ 
		   getProperty(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX));

	result.add(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED + "="+ 
		   String.valueOf(getBooleanProperty(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED)));
	result.add(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS + "="+ 
		   String.valueOf(getBooleanProperty(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS)));

	return result;
    }
    
    

    private void putPreferences(ArrayList list)
    {
	for (int i=0; i<list.size(); i++)
	    {
		String prop = (String)list.get(i);
		String name,value;
		int pos= prop.indexOf("=");
		name = prop.substring(0,pos);
		value = prop.substring(pos+1);

		if (name.equals(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER) ||
		    name.equals(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT) ||
		    name.equals(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED) ||
		    name.equals(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS) ||
		    name.equals(IHelpSearchConstants.HELP_SETTINGS_SELECTED) )
		    {
			//booleans
			putPreference(name,Boolean.valueOf(value).booleanValue());
			
		    }
		else
		    {
			//strings
			putPreference(name,value);
		    }
	    }	
    }

    private void putProperties(ArrayList list)
    {
	for (int i=0; i<list.size(); i++)
	    {
		String prop = (String)list.get(i);
		String name,value;
		int pos= prop.indexOf("=");
		name = prop.substring(0,pos);
		value = prop.substring(pos+1);

		if (name.equals(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED) ||
		    name.equals(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS) )
		    {
			//booleans
			putProperty(name,Boolean.valueOf(value).booleanValue());
		    }
		else
		    {
			//strings
			putProperty(name,value);
		    }
	    }	
    }



    private void putDefaultPreferences()
    {
	boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("window");

	putPreference(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT,false);
	putPreference(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS,true);
	putPreference(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP,false);
	
	putPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL,false);
	putPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN,true);
	putPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO,true);
	putPreference(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML,false);

	//Pick a default Browser if none selected
	boolean kdeBrowserExists = existsCommand("konqueror");
	boolean gnomeBrowserExists = existsCommand("gnome-help-browser");		
       	if (kdeBrowserExists)
	    {
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,true);
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,false);
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT,false);
	    }
	else if (gnomeBrowserExists)
	    {			
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,false);
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,true);
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT,false);
	    }
	else
	    {
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,false);
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,false);
		putPreference(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT,true);
	    }

	putPreference(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,"/var/cache/man/whatis");
	putPreference(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,"/usr/share/info/dir");

	putPreference(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,false);
	putPreference(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,false);

	putPreference(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,null);
	putPreference(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,null);

	putPreference(IHelpSearchConstants.HELP_FILTER_PATTERNS,null);

	putPreference(IHelpSearchConstants.HELP_SETTINGS_SELECTED,false);

	putPreference(IHelpSearchConstants.HELP_TOMCAT_PORT,"8080");
    }
    
    private void putDefaultProperties()
    {
	putProperty(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,"/var/cache/man/whatis");
	putProperty(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,"/usr/share/info/dir");

	putProperty(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,false);
	putProperty(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,false);

	putProperty(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,null);
	putProperty(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,null);
    }

    private boolean isPropertyName(String name)
    {
	if (name.equals(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS)||
	    name.equals(IHelpSearchConstants.HELP_SEARCH_PATH_DIR)||
	    name.equals(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED)||
	    name.equals(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS)||
	    name.equals(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION)||
	    name.equals(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX)    )
	    return true;
	else
	    return false;
    }

    private static boolean existsCommand(String command)
    {

	// Always return FALSE for Windows
	String theOs = System.getProperty("os.name");
	if (theOs.toLowerCase().startsWith("window"))
	    return false;


	String[] arg=new String[3];
	arg[0]="sh";
	arg[1]="-c";
	arg[2]="which "+command;
	
	Process p;
	try{
	    p=Runtime.getRuntime().exec(arg);
	    
	    BufferedReader in= new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line=in.readLine();	
	    if(line!= null &&line.endsWith(command) && line.indexOf(" ")==-1)
		return true;
	    else
		return false;
	}catch(Exception e){
	    e.printStackTrace();
	    return false;
	}
    }

}
