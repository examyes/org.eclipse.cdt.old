package org.eclipse.cdt.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


public class DataElementActionFilter 
{
    private static String                  _type = "type";
    private static DataElementActionFilter _instance;
    
    public static DataElementActionFilter getInstance() 
    {
	if (_instance == null)
	    _instance = new DataElementActionFilter();
	return _instance;
    }
    
    /**
     * @see IActionFilter#testAttribute(Object, String, String)
     */
    public boolean testAttribute(Object target, String name, String value) 
    {
	if (name.equals(_type)) 
	    {
		IDataElement le = (IDataElement)target;
		return le.getType().equals(value);
	    }       
	
	return false;
    }

    public static boolean matches(Class aClass)
    {
	return false;
    }

}
