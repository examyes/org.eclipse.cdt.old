package org.eclipse.cdt.dstore.core.client;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.DataStoreAttributes;

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
