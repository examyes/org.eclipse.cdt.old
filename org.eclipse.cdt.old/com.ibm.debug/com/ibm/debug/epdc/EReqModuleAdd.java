package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqModuleAdd.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/**
 * Add a module
 */
public class EReqModuleAdd extends EPDC_Request
{
  public EReqModuleAdd(String moduleName, int moduleType)
  {
    super(EPDC.Remote_ModuleAdd);

    _moduleName = new EStdString(moduleName);
    _moduleType = moduleType;
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    // Write out the offsets of the variable length data:

    writeOffsetOrZero(dataOutputStream, fixedLen() + super.varLen(), _moduleName);

    dataOutputStream.writeInt(_moduleType);

    // Now write out the variable length data:

    if (_moduleName != null)
        _moduleName.output(dataOutputStream);
  }

   /**
    * Return the part file name
    * @exception IOException if an I/O error occurs
    */
   public String moduleName()
   throws IOException
   {
     if ((_moduleName == null) && (_offsetModuleName != 0))
     {
          posBuffer(_offsetModuleName);
          _moduleName = readStdString();
     }

     if (_moduleName != null)
         return _moduleName.string();

     return null;
   }

   /**
    * Return module type
    */
   public int moduleType()
   {
      return _moduleType;
   }

   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   protected int varLen()
   {
      return super.varLen() + totalBytes(_moduleName);
   }

   // data fields
   private int _offsetModuleName;
   private EStdString _moduleName;

   private int _moduleType;


   private static final int  _fixed_length = 8;
}
