package org.eclipse.cdt.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


public class DataElementActionFilter implements org.eclipse.ui.IActionFilter
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
	if (name.equals(_type) && target instanceof IDataElement) 
	    {	    	
		IDataElement le = (IDataElement)target;
		if (le.getType().equals(value) || le.isOfType(value))
		    {
			return true;
		    }
	    }       
	
	return false;
    }

    public static boolean matches(Class aClass)
    {	
    	return (aClass == org.eclipse.ui.IActionFilter.class);    
    }

}
