package com.ibm.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

public class DomainNotifier implements IDomainNotifier 
{
    public DomainNotifier()
      {
      }
  
    public void enable(boolean on)
    { 
    }

    public boolean isEnabled()
    {
        return false;
    }

  public void addDomainListener(IDomainListener listener)
      {
      }


  public void fireDomainChanged(DomainEvent event)
      {
      }	

  public boolean hasDomainListener(IDomainListener listener)
      {
	  return false;
      }

  public void removeDomainListener(IDomainListener listener)
      {
      }
}
