package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import org.eclipse.swt.widgets.*;

public interface IDomainListener
{
    public boolean listeningTo(DomainEvent e);  
    public void domainChanged(DomainEvent e);
    public Shell getShell();
}
