package org.eclipse.cdt.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


public interface IDomainNotifier
{
  public void addDomainListener(IDomainListener listener);
  public void fireDomainChanged(DomainEvent event);
  public boolean hasDomainListener(IDomainListener listener);
  public void removeDomainListener(IDomainListener listener);
}
