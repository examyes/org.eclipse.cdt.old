package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/FileUtility.java, java-util, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:32:48)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.*;

/**
 * This class will provide a number of information about a file. This file
 * may reside in a file system in the frontend (local source file) or a file
 * VAJ file. This utility class may also be used by the engine classes to
 * gather information about a file.
 * @see FileSystem
 */
public abstract class FileUtility
{

  FileUtility()
  {
    _sourcePaths = new Vector();
  }

  /**
   * Get the list of the paths to search for a local source file.
   * @return The list of paths as an array of String objects
   */
  public String[] getSourcePaths()
  {
    if (_sourcePaths == null || _sourcePaths.size() == 0)
        return null;

    String[] sourcePaths = new String[_sourcePaths.size()];
    _sourcePaths.copyInto(sourcePaths);

    return sourcePaths;
  }

  void addPath(String path)
  {
    if (_sourcePaths == null)
        _sourcePaths = new Vector();

    _sourcePaths.addElement(path);
  }

  /**
   * Get the record length of a file
   * @return The record length
   */
  public int getRecordLength()
  {
    return _recordLength;
  }

  /**
   * Get the first line in the file (which is one)
   */
  public int getFirstLine()
  {
    return 1;
  }

  /**
   * Get the last line of a file
   */
  public int getLastLine()
  {
    return _lastLine;
  }

  /**
   * Get the fully qualified name of the file
   */
  public String getSourceFileName()
  {
    return _sourceFileName;
  }

  /**
   * Get the base name of a file (without its fully qualified path)
   */
  public String getBaseFileName()
  {
    return _baseFileName;
  }

  /**
   * Get the File object representing this file
   */
  public File getFile()
  {
    File file = new File(_sourceFileName);

    if (file == null || !file.exists())
        return null;

    return file;
  }

  public abstract boolean readFile(String fileName);

  public abstract int[] findString(String text, int startingLine,
                                   int startingColumn, boolean caseSensitive);
  public abstract String[] getLines(int startLine, int numberOfLines);

  Vector _sourcePaths = null;
  int _recordLength = 0;
  int _lastLine = 0;
  String _sourceFileName = null;
  String _baseFileName = null;
}
