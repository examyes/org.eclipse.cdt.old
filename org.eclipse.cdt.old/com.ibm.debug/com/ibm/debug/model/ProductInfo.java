package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ProductInfo.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:45)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class ProductInfo
{
  public ProductInfo(String productPrefix)
  {
    _productPrefix = productPrefix;
  }

  /**
   * @param productPrefix If this String is not null, it will be prepended
   * to the name of the debug engine when the engine is invoked.
   * @param installDirectory If this String is not null, it will be used
   * by the Model when launching debug engine(s). The Model will append
   * "bin" to the given directory name and then will use the resulting
   * directory name to fully qualify the name of the engine when invoking it.
   * For example, if the installDirectory is "/usr/idebug/" and the name of
   * the debug engine (including product prefix) is "derdfsrv", then the
   * Model will exec the engine using the fully-qualified name
   * "/usr/idebug/bin/derdfsrv". If no install directory is given, then
   * the engine will be execed without qualification (e.g. "derdfsrv") in
   * which case it is assumed that the engine is on the user's path.
   * <p>Note that the install directory can be overridden using the property
   * INSTALL_DIR.
   */

  public ProductInfo(String productPrefix, String installDirectory)
  {
    _productPrefix = productPrefix;
    _installDirectory = installDirectory;
  }

  public String getProductPrefix()
  {
    return _productPrefix;
  }

  public String getInstallDirectory()
  {
    return _installDirectory;
  }

  private String _productPrefix;
  private String _installDirectory;
}
