package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.*;
import java.util.*;

public interface ISchemaExtender
{
    /**
     * Add this tool's schema to the global DataStore schema.
     * This interface must be implemented by each miner in order to
     * populate the DataStore schema with information about this tool's
     * object model and information about how to communicate with the
     * tool from objects available to the user interface.
     *
     * @param schemaRoot the descriptor root
     */
    public abstract void extendSchema(DataElement schemaRoot);

    public abstract ExternalLoader getExternalLoader();     
}
