package org.eclipse.cdt.cpp.miners.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.miners.help.search.*;

import java.util.*;

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
		   //   System.out.println("No value found for String:"+key);
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

    private StringSetting _stringSettings = null;
    private BooleanSetting _booleanSettings = null;

    private String delimiter="||";

    private static HelpSettings _helpSettings = null;
    public static HelpSettings getDefault()
    {
	if(_helpSettings == null)
	    {
		_helpSettings = new HelpSettings();
	    }
	return _helpSettings;
    }

    private HelpSettings()
    {
	_stringSettings = new StringSetting();
	_booleanSettings = new BooleanSetting();
    }

    public boolean getBoolean(String key)
    {
	return _booleanSettings.get(key);
    }

    public String get(String key)
    {
	return _stringSettings.get(key);
    }

    public void put(String key, String value)
    {
	_stringSettings.setValue(key,value);
    }
    public void put(String key, boolean value)
    {
	_booleanSettings.setValue(key,value);
    }
   
    public void loadSettingsFromString(String strSettings)
    {
	StringTokenizer st = new StringTokenizer(strSettings,delimiter);
	int count = st.countTokens();
	if (count==14)
	    {
		put(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT,Boolean.valueOf(st.nextToken()).booleanValue());
	
		put(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS,Boolean.valueOf(st.nextToken()).booleanValue());
		//boolean flag = getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS);

		put(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP,Boolean.valueOf(st.nextToken()).booleanValue());

		if(!isWindows())
		    {
			put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL,Boolean.valueOf(st.nextToken()).booleanValue());
			put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN,Boolean.valueOf(st.nextToken()).booleanValue());
			put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO,Boolean.valueOf(st.nextToken()).booleanValue());
		    }
		else
		    {//disable ALL,MAN,INFO search for windows
			put(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL,false);
			put(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN,false);
			put(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO,false);
		    }
		put(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML,Boolean.valueOf(st.nextToken()).booleanValue());
		
	     
		String oldwhatis = get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS);
		String whatis = st.nextToken();
		
		if (!whatis.equals(oldwhatis) && !isWindows())
		    {
			put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,whatis);
			HelpSearch.reloadMan();
		    }
		
		String oldinfodir = get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR);
		String infodir = st.nextToken();
		
		if(!infodir.equals(oldinfodir) && !isWindows())
		    {
			put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,infodir);
			HelpSearch.reloadInfo();
		    }
		
		String indexLocation = st.nextToken();
		if(indexLocation.equals("null"))
		    put(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,null);
		else
		    put(IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION,indexLocation);

		String pathsToIndex = st.nextToken();
		if(pathsToIndex.equals("null"))
		    put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,null);
		else
		    put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX,pathsToIndex);

		put(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,Boolean.valueOf(st.nextToken()).booleanValue());
		put(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,Boolean.valueOf(st.nextToken()).booleanValue());		
		put(IHelpSearchConstants.HELP_TOMCAT_PORT,st.nextToken());
	    }
	else
	    {
		//System.out.println("invalid number of miner settings:"+count);
	    }
	
    }


    private boolean isWindows()
    {
	return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
	
}
