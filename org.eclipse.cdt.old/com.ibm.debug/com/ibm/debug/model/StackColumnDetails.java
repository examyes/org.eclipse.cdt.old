package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StackColumnDetails.java, java-model, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:12:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.util.Vector;
import java.io.*;

/**
 * This class represents stack columns.
 * Each stack column object contains column id, column name, column name alignment and
 * column text alignment.
 */

public class StackColumnDetails extends DebugModelObject
{
  StackColumnDetails (ERepGetStackColumns epdcStackColumn)
  {
    _epdcStackColumn = epdcStackColumn;
  }

 /**
  * Return column id.
  */
 int getColumnID()
  {
    return _epdcStackColumn.getColumnID();
  }

  /**
   * Return column name as assigned by the backend.
   */

  public String getColumnName()
  {
    return _epdcStackColumn.getColumnName();
  }

  /**
   * Return name of column alignment. There are three types of alignment: Centered,
   * LeftJustified and RightJustified.
   * @see com.ibm.debug.epdc.EPDC#Centered
   * @see com.ibm.debug.epdc.EPDC#LeftJustified
   * @see com.ibm.debug.epdc.EPDC#RightJustified
   */

  public int getColumnNameAlignment()
  {
    return _epdcStackColumn.getColumnNameAlignment();
  }

  /**
   * Return name of column text alignment. There are three types of alignment:
   * Centered, LeftJustified and RightJustified.
   * @see com.ibm.debug.epdc.EPDC#Centered
   * @see com.ibm.debug.epdc.EPDC#LeftJustified
   * @see com.ibm.debug.epdc.EPDC#RightJustified
   */

  public int getColumnTextAlignment()
  {
    return _epdcStackColumn.getColumnTextAlignment();
  }

  void setDefault()
  {
    _isDefault = true;
  }

  /**
   * @return 'true' if this is a default column as set by the backend,
   * 'false' otherwise.
   */

  public boolean isDefault()
  {
    return _isDefault;
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.print(" " + getColumnID());
      printWriter.print(" " + getColumnName());
      printWriter.print(" " + getColumnNameAlignment());
      printWriter.print(" " + getColumnTextAlignment());
      printWriter.println();
      printWriter.println("IsDefault: " + _isDefault);

      super.print(printWriter);
      printWriter.println();
    }
  }

  private ERepGetStackColumns _epdcStackColumn;
  private boolean _isDefault = false;
}
