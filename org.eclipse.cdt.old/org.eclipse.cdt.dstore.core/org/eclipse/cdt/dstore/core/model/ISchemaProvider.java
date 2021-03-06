package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

public interface ISchemaProvider
{
    public ISchemaRegistry getSchemaRegistry();
    public ISchemaExtender getSchemaExtender();    
}
