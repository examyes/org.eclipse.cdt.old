package org.eclipse.cdt.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.widgets.*;

public interface IDomainListener
{
    public boolean listeningTo(DomainEvent e);  
    public void domainChanged(DomainEvent e);
    public Shell getShell();
}
