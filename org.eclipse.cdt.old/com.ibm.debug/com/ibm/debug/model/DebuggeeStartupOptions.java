package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeStartupOptions.java, java-model, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:13:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public abstract class DebuggeeStartupOptions
{
  public DebuggeeStartupOptions(String debuggeeName,
                                int saveRestoreFlags,
                                String saveRestoreDirectory,
                                boolean restoreSavedObjects,
                                boolean executeAfterStartup)
  {
    _debuggeeName = debuggeeName;
    _saveRestoreFlags = saveRestoreFlags;
    _saveRestoreDirectory = saveRestoreDirectory;
    _restoreSavedObjects= restoreSavedObjects;
    _executeAfterStartup = executeAfterStartup;
  }

  public String getDebuggeeName()
  {
    return _debuggeeName;
  }

  public int getSaveRestoreFlags()
  {
    return _saveRestoreFlags;
  }

  public String getSaveRestoreDirectory()
  {
    return _saveRestoreDirectory;
  }

  public boolean restoreSavedObjects()
  {
    return _restoreSavedObjects;
  }

  public boolean executeAfterStartup()
  {
    return _executeAfterStartup;
  }

  private String _debuggeeName;
  private int _saveRestoreFlags;
  private String _saveRestoreDirectory;
  private boolean _restoreSavedObjects;
  private boolean _executeAfterStartup;
}
