package org.eclipse.cdt.linux.help;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.lang.String;

public class ItemElement 
{
    String _key;
    String _name;
    String _content;
    String _invocation;
    String _section;
    String _type;

    public static final String MAN_TYPE = "M";
    public static final String INFO_TYPE = "I";
    public static final String HTML_TYPE = "H";    

    public ItemElement(String key,String name, String content, String invocation, String section, String type)
    {
	_key=key;
	_name=name;
	_content=content;
	_invocation=invocation;
	_section=section;
	_type=type;

    }
    
    public String getKey()
    {
	return _key;
    }
    public String getName()
    {
	return _name;
    }

    public String getContent()
    {
	return _content;
    }
    public String getInvocation()
    {
	return _invocation;
    }
    public String getSection()
    {
	return _section;
    }
    public String getType()
    {
	return _type;
    }
    
   

    
}
