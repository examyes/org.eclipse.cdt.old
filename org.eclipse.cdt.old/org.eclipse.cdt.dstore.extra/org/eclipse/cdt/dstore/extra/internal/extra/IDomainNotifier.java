package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */


public interface IDomainNotifier
{
  public void addDomainListener(IDomainListener listener);
  public void fireDomainChanged(DomainEvent event);
  public boolean hasDomainListener(IDomainListener listener);
  public void removeDomainListener(IDomainListener listener);
}
