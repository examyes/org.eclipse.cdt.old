package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ProcessListColumnDetails.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:41)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.PrintWriter;

public class ProcessListColumnDetails
{
  ProcessListColumnDetails(ERepGetProcessColumns epdcColumnDetails)
  {
    _columnName = epdcColumnDetails.getColumnName();
    _columnNameAlignment = epdcColumnDetails.getColumnNameAlignment();
    _columnTextAlignment = epdcColumnDetails.getColumnTextAlignment();
  }

  public String getColumnName()
  {
    return _columnName;
  }

  /**
   * Return alignment of column name.
   * There are three types of alignment: Centered, LeftJustified and RightJustified.
   * @see com.ibm.debug.epdc.EPDC#Centered
   * @see com.ibm.debug.epdc.EPDC#LeftJustified
   * @see com.ibm.debug.epdc.EPDC#RightJustified
   */

  public int getColumnNameAlignment()
  {
    return _columnNameAlignment;
  }

  /**
   * Return alignment of column contents.
   * There are three types of alignment: Centered, LeftJustified and RightJustified.
   * @see com.ibm.debug.epdc.EPDC#Centered
   * @see com.ibm.debug.epdc.EPDC#LeftJustified
   * @see com.ibm.debug.epdc.EPDC#RightJustified
   */

  public int getColumnTextAlignment()
  {
    return _columnTextAlignment;
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.print(" " + getColumnName());
      printWriter.print(" " + getColumnNameAlignment());
      printWriter.print(" " + getColumnTextAlignment());
      printWriter.println();
    }
  }

  private String _columnName;
  private int _columnNameAlignment;
  private int _columnTextAlignment;
}
