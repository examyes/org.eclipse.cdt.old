package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/MonitoredExpressionChangedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:11:49)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class MonitoredExpressionChangedEvent extends MonitoredExpressionEvent
{
  MonitoredExpressionChangedEvent(Object source,
                                MonitoredExpression monitor, int requestCode)
  {
    super(source, monitor, requestCode);
  }

  void fire(ModelEventListener listener)
  {
    ((MonitoredExpressionEventListener)listener).monitoredExpressionChanged(this);

  }

  /**
   * Query the event of a monitored expression whose enablement has changed.
   * This means that an enablement will change when a monitored
   * expression has changes its state from enabled to disabled
   * and vice versa.
   * @return 'true' if the monitored expression has changed from an
   * enabled to disabled state and vice versa. Otherwise return 'false'.
   */
  public boolean isEnablementChanged()
  {
    return super.getMonitoredExpression().getEPDCMonitoredExpression().isEnablementChanged();
  }

  /**
   * Query the event of a monitored expression whose value has changed
   * @return 'true' if the value of the monitored expression is changed,
   * and 'false' otherwise.
   */
  public boolean isValueChanged()
  {
    return super.getMonitoredExpression().getEPDCMonitoredExpression().isMonValueChanged();
  }

  /**
   * Query the event of a monitored expression of a complex type
   * (array, struct, class) for any possible change in one or more of
   * its nodes.
   * @return 'true' if the user has expanded or collapsed one or more nodes
   * in the structure and 'false' otherwise.
   */
  public boolean isTreeStructureChanged()
  {
    return super.getMonitoredExpression().getEPDCMonitoredExpression().isMonTreeStructChanged();
  }

}
