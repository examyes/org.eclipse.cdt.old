package org.eclipse.cdt.dstore.ui;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;


public class SchemaRegistry implements ISchemaRegistry
{
    private ArrayList _initializedDataStores = new ArrayList();
    private ArrayList _extenders = new ArrayList();

    public void registerSchemaExtender(ISchemaExtender extender)
    {
	if (!_extenders.contains(extender))
	    {		
		_extenders.add(extender);
		for (int i = 0; i < _initializedDataStores.size(); i++)
		    {
			DataStore dataStore = (DataStore)_initializedDataStores.get(i);
			DataElement schemaRoot = dataStore.getDescriptorRoot();
			extender.extendSchema(schemaRoot);
		    }
	    }
    }

    public void extendSchema(DataStore dataStore)
    {
	if (!_initializedDataStores.contains(dataStore))
	    {
		DataElement schemaRoot = dataStore.getDescriptorRoot();
		for (int i = 0; i < _extenders.size(); i++)
		    {
			ISchemaExtender extender = (ISchemaExtender)_extenders.get(i);
			extender.extendSchema(schemaRoot);
		    }
		_initializedDataStores.add(dataStore);
	    }
    }

    public ExternalLoader getLoaderFor(String source)
    {
	for (int i = 0; i < _extenders.size(); i++)
	    {
		ISchemaExtender extender = (ISchemaExtender)_extenders.get(i); 
		ExternalLoader loader = extender.getExternalLoader();
		if (loader.canLoad(source))
		    {
			return loader;
		    }
	    }
	return null;
    }
}
