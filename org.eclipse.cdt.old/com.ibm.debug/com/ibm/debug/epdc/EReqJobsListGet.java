package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqJobsListGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:02)
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
 * Get the list of Jobs (AS/400 only)
 */
public class EReqJobsListGet extends EPDC_Request
{
  public EReqJobsListGet(String jobQualification)
  {
    super(EPDC.Remote_JobsListGet);

    _jobQualification = new EStdString(jobQualification);
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    // Write out the offsets of the variable length data:

    _offsetQualificationList = writeOffsetOrZero(dataOutputStream, fixedLen() + super.varLen(), _jobQualification);

    if (_jobQualification != null)
        _jobQualification.output(dataOutputStream);
  }

   /**
    * Return the Qualification List
    * @exception IOException if an I/O error occurs
    */
   public String jobQualification()
   throws IOException
   {
     if ((_jobQualification == null) && (_offsetQualificationList != 0))
     {
          posBuffer(_offsetQualificationList);
          _jobQualification = readStdString();
     }

     if (_jobQualification != null)
         return _jobQualification.string();

     return null;
   }

   protected int varLen()
   {
      return super.varLen() + totalBytes(_jobQualification);
   }

   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   // data fields
   private int _offsetQualificationList;
   private EStdString _jobQualification = null;


   private static final int  _fixed_length = 4;
}
