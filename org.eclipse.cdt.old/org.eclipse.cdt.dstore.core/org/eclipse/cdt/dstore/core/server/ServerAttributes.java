package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.DataStoreAttributes;

import java.lang.*;
import java.util.*;
import java.net.*;
import java.io.*;


public class ServerAttributes extends DataStoreAttributes
{
  public ServerAttributes()
      {
        super();

        try
        {
          String pluginPath = System.getProperty("A_PLUGIN_PATH");
	  pluginPath = pluginPath.trim();
          if ((pluginPath != null) && (pluginPath.length() > 0))
          {
            setAttribute(A_PLUGIN_PATH, pluginPath + File.separator);
          }
          else
          {
            setAttribute(A_PLUGIN_PATH, "/home/");
          }

          setAttribute(A_LOCAL_NAME, InetAddress.getLocalHost().getHostName());

          setAttribute(A_HOST_NAME,  "server_host");
          setAttribute(A_HOST_PATH,  "/home/");
	}
        catch (UnknownHostException e)
        {
        }

      }
}
