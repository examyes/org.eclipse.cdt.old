package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepJobsListGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepJobsListGet extends EPDC_Reply
{
  public ERepJobsListGet(String[] jobNames)
  {
    super(EPDC.Remote_JobsListGet);

    _jobNames= new EStdString[jobNames.length];
  }

  ERepJobsListGet(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super( packetBuffer, dataInputStream );

    int numberOfJobs = dataInputStream.readInt();

    if (numberOfJobs == 0)
        return;

    _jobNames = new EStdString[numberOfJobs];

    OffsetDataInputStream offsetDataInputStream =
                            new OffsetDataInputStream(packetBuffer,
                                                      dataInputStream.readInt()
                                                     );

    for (int i = 0; i < numberOfJobs; i++)
         _jobNames[i] = (new EStdString(packetBuffer, offsetDataInputStream));
   }

  protected int fixedLen()
  {
    return super.fixedLen() + _fixed_length;
  }

  protected int varLen()
  {
    int total = super.varLen();

    for (int i = 0; i < _jobNames.length; i++)
         total += totalBytes(_jobNames[i]);

    return total;
  }

  public String[] getJobNames()
  {
    if (_jobNames == null)
        return null;

    String[] jobNames = new String[_jobNames.length];

    for (int i = 0; i < _jobNames.length; i++)
    {
         if (_jobNames[i] != null)
             jobNames[i] = _jobNames[i].string();
    }

    return jobNames;
  }



  private EStdString[] _jobNames;

  private static final int _fixed_length = 8;
}

