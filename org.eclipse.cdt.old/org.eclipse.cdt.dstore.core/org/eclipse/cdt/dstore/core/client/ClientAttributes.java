package com.ibm.dstore.core.client;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.DataStoreAttributes;

import java.lang.*;
import java.util.*;
import java.net.*;
import java.io.*;


public class ClientAttributes extends DataStoreAttributes
{
  public ClientAttributes()
      {
        super();

        try
        {
          String pluginPath = System.getProperty("A_PLUGIN_PATH");
          if ((pluginPath != null) && (pluginPath.length() > 0))
          {
            setAttribute(A_PLUGIN_PATH, pluginPath + File.separator);
          }

          setAttribute(A_LOCAL_NAME, InetAddress.getLocalHost().getHostName());
          setAttribute(A_LOCAL_PATH, "/tmp/");
          setAttribute(A_HOST_NAME,  "local");	
          setAttribute(A_HOST_PATH,  "/");
	}
	
        catch (UnknownHostException e)
        {
        }
      }
}
