package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.*;
import java.util.*;

public interface ISchemaRegistry
{    
    public void registerSchemaExtender(ISchemaExtender extender);
    public void extendSchema(DataStore dataStore);
    public ExternalLoader getLoaderFor(String path);
}
