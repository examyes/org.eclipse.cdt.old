package org.eclipse.cdt.dstore.ui;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.*;

import java.util.*;
import java.lang.reflect.*;
 
public class ExternalLoader
{
    private ClassLoader    _classLoader;
    private ArrayList      _loadScope;

    public ExternalLoader(ClassLoader classLoader, String loadScope)
    {
	_classLoader = classLoader;
	_loadScope = new ArrayList();
	_loadScope.add(loadScope);
    }

    public ExternalLoader(ClassLoader classLoader, ArrayList loadScope)
    {
	_classLoader = classLoader;
	_loadScope = loadScope;
    }

    public boolean canLoad(String source)
    {
	boolean result = false;
	if (_loadScope != null)
	    {
		for (int i = 0; i < _loadScope.size(); i++)
		    {
			String scope = (String)_loadScope.get(i);
			result = StringCompare.compare(scope, source, true);
			if (result)
			    {
				return result;
			    }
		    }
	    }
	return result;
    }

    public Class loadClass(String source) throws ClassNotFoundException
    {
	return _classLoader.loadClass(source);
    }
}
