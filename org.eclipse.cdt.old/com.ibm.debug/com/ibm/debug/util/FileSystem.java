package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/FileSystem.java, java-util, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:32:49)
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
import java.net.*;

/**
 * This class provides file utilities to manipulate files that belong to a
 * file system (vs. VAJ file).
 * @see FileUtility
 */
public class FileSystem extends FileUtility
{

  public FileSystem(String searchPath)
  {
    super();
    addSearchPath(searchPath);
  }

  /**
   * This function takes a string representing a list of paths and adds each
   * path to the source search paths list.   Paths in the string are assumed
   * to be separated by the path.separator property from System.getProperties()
   */
  void addSearchPath(String searchPath)
  {
    if (searchPath == null)
        return;

    StringTokenizer tokenizer = new StringTokenizer(searchPath, File.pathSeparator, false);

    // add the user's working directory to the list
    _sourcePaths.addElement(System.getProperty("user.dir"));

    while (tokenizer.hasMoreTokens())
    {
           String path = tokenizer.nextToken();
           _sourcePaths.addElement(setFileSeparator(path));
    }
  }

  /**
   * Add an additional path in which to search for source
   */
  String setFileSeparator(String path)
  {
    // Append a trailing file separator if there isn't one already
    if (path.charAt(path.length()-1) != File.separatorChar)
    {
        path += File.separatorChar;
    }

    return path;
  }

  /**
   * Collect the path list information from the engine and the UI and return
   * a vector of paths.
   * @param list The path list that will be added to the UI supplied list
   * @param fileName The base file name which may be added each path in the list
   * @return A Vector of path lists that can be used to find the local
   * source file
   */
  public Vector buildPathList(String[] list, String fileName)
  {
    Vector combinedList = new Vector();
    String[] FEList = getSourcePaths();

    if (list != null)
    {
        for (int i = 0; i < list.length; i++)
        {
             combinedList.addElement(list[i]);

             if (!isADirectory(list[i]))
             {
                 if (FEList != null)
                 {
                     for (int j = 0; j < FEList.length; j++)
                          combinedList.addElement(verifyPath(FEList[j], list[i]));
                 }
             }
             else
             {
                combinedList.addElement(verifyPath(list[i], fileName));
                if (FEList != null)
                {
                    for (int j = 0; j < FEList.length; j++)
                         combinedList.addElement(verifyPath(FEList[j], fileName));
                }
             }
        }

    }
    else
    if (FEList != null)
    {
        for (int j = 0; j < FEList.length; j++)
             combinedList.addElement(verifyPath(FEList[j], fileName));
    }
    return combinedList;
  }

  /**
   * Return a file name either by just specifying its path (with or without a
   * file separator i.e. slash or backslash) or a its fully qualified
   * name.
   * @param path The path of the file. If the path is null the file name will
   * be returned.
   * @param The name of the file
   * @return The string represting the file name
   */
  String verifyPath(String path, String fileName)
  {
    if (path == null || path.length() == 0)
        return fileName;

    if (path.endsWith(fileName))
        return path;

    path = setFileSeparator(path);

    return path + fileName;
  }

  /**
   * Open a file that resides within the file system, read the lines of the
   * file and save the following information and then close the file:
   * 1) the offset of each line
   * 2) the record length of the file
   * 3) the first and last line number in the file
   * 4) the base file name
   * 5) the fully qualified name of the file
   * @param fileName The fully qualified name of the file to be read
   * @exception FileNotFoundException If the file is not found in the file
   * system.
   * @exception java.io.IOException If there is a problem communicating with
   * the debug engine.
   * @exception IllegalArgumentException If an illegal access mode is specified
   * reading the file.
   * @exception SecurityException If the file does not have read access
   * @return false in case the name specified is not a file or it is a directory or in case of any exception and return true otherwise.
   */
  public boolean readFile(String fileName)
  {
    File file = new File(fileName);

    if (file == null || !file.exists() || !file.isFile() || file.isDirectory())
        return false;

    BufferedReader reader = null;
    int lineNumber   = 0;
    _recordLength = 0;
    _lineOffsets = new Vector();
    int nextOffset = 0;
    int lastOffset = 0;
    int relativeOffset = 0;
    int originalOffset = 0;
    int tabCount = 0;
    char[] buffer = new char[1000];
    int readCount = 0;
    char currentChar;
    int temp_recordLength;

    try
    {
      reader = new BufferedReader(new FileReader(file));
    // For the first line read the offset will be zero
      _lineOffsets.addElement(new Integer(nextOffset));
      lineNumber++;
      readCount = reader.read(buffer,0,1000);
      while (readCount > 0)
      {
        for (relativeOffset = 0 ; relativeOffset < readCount; relativeOffset++)
        {
           currentChar = buffer[relativeOffset];
           if (currentChar =='\t')
              tabCount++;

           if ((currentChar == '\r') || (currentChar == '\n'))
           {
             temp_recordLength = relativeOffset - lastOffset + (tabCount*7);
             if (temp_recordLength > _recordLength)
                   _recordLength = temp_recordLength;
             tabCount = 0;

             relativeOffset++;
             nextOffset = originalOffset + relativeOffset;
             if (relativeOffset == readCount) {    // at end of buffer
             	if (currentChar == '\n')
                {
                   lineNumber++;
                   _lineOffsets.addElement(new Integer(nextOffset));
                }
                break;
             }
             if (currentChar == '\r')
             {
                nextOffset++;
                relativeOffset++;
             }
             lastOffset = relativeOffset;
             lineNumber++;
             _lineOffsets.addElement(new Integer(nextOffset));
             relativeOffset--;     // to catch consecutive CR/LFs
          }

        } // end of for loop
        originalOffset = originalOffset + readCount ;
        readCount = reader.read(buffer,0,1000);
      }  // end of while ....
      reader.close();
    }
    catch (FileNotFoundException e)
    {
      System.out.println("FileNotFound exception");
      return false;
    }
    catch (IOException e)
    {
      System.out.println("IO exception while reading file");
      return false;
    }

    _lastLine = lineNumber;
    _sourceFileName = file.getPath();
    _baseFileName = file.getName();
    return true;
  }

  /**
   * Return the position of a string that is being search within the file.
   * The position will be returned as an array of two elements: the line
   * and the column where the string starts. To search of the string the
   * entire file will be searched.
   * @param text The search string
   * @param starting line The line where the search begins.
   * @param startingColumn The column of the line where the search begins.
   * @param caseSensitive The flag to check if the search string is case
   * sensitive or not.
   * @return The integer array representing the position of the search string
   */
  public int[] findString(String text, int startingLine,
                          int startingColumn, boolean caseSensitive)
  {
    int line      = startingLine;
    int index     = 0;
    boolean found = false;

    int firstLine = getFirstLine();
    int numberOfLinesToSearch = _lastLine - firstLine + 1;

    String srcLine;
    String[] sourceLines = new String[numberOfLinesToSearch];

    // If we don't care about case, convert the search string to upper case
    if (!caseSensitive)
        text = text.toUpperCase();

    // We are doing a search from the middle of the file, we have to be
    // able to loop back to this point by the time the search is over
    if (startingLine > firstLine)
    {
        String[] firstChunk = null;
        String[] lastChunk = null;

        // First read from the middle of the file to the end of the file
        lastChunk = new String[_lastLine - startingLine + 1];
        lastChunk = getLines(startingLine, numberOfLinesToSearch);

        // Wrap around and read from the beginning of the file until the line
        // you read in the middle of the file
        firstChunk = new String[startingLine-firstLine];
        firstChunk = getLines(firstLine, startingLine-1);

        // Put the two arrays together so that we can begin the string
        // search
        for (int i = 0; i < startingLine-1; i++)
             sourceLines[i] = firstChunk[i];

        for (int i = startingLine-1; i < numberOfLinesToSearch; i++)
        {
             sourceLines[i] = lastChunk[index];
             index++;
        }

    }
    else
       // The search is from the beginning of the file
       sourceLines = getLines(startingLine, numberOfLinesToSearch);

    int count = 0;
    index = 0;

    // If we're not starting the search at beginning of the start line, we will
    // have to make the start line the last line to search.
    if (startingColumn > 1)
       numberOfLinesToSearch += 1;

    while (count < numberOfLinesToSearch && !found)
    {
           // Get the source line to search through, we need to subtract one
           // because the vector of sourceLines starts at zero and not one
           srcLine = sourceLines[line-1];

           // If we don't care about case, convert the source line to upper case
           if (!caseSensitive)
               srcLine = srcLine.toUpperCase();

           // Make sure we don't fall off the end of the source line!
           if (startingColumn <= srcLine.length())
           {
               srcLine = srcLine.substring(startingColumn-1);
               index = srcLine.indexOf(text);

               if (index >= 0)
                   found = true;
           }

           // If we didn't find the string, go to the next line and search from
           // column 1
           if (!found)
           {
               count++;
               line++;
               if (line > sourceLines.length)
                   line = 1;
               startingColumn = 1;
           }
      }

      if (found)
      {
          int[] position = new int[2];
          position[0] = line;                       // line
          position[1] = index + startingColumn;     // column

          return position;
      }

      return null;
  }

  /**
   * Get a number of lines from the local source file. In order to get the
   * lines, the file will be opened and only the lines from the range specified
   * (startLine to numberOfLines) will be read and returned.
   * @param startLine The first line to be read in the file
   * @param numberOfLines Total number of lines to be read in the file
   * @exception FileNotFoundException If the file is not found in the file
   * system.
   * @exception java.io.IOException If there is a problem communicating with
   * the debug engine.
   * @return An array of String objects representing the lines read. In case
   * the file cannot be opened or an exception occurs return null.
   */
  public String[] getLines(int startLine, int numberOfLines)
  {

    int lineNumber = startLine - 1;
    int lastLineToRead = lineNumber + numberOfLines;
    String[] sourceLines = new String[numberOfLines];
    int index = 0;
    int lineOffset = ((Integer)_lineOffsets.elementAt(lineNumber)).intValue();

    // open and read the file
    File file = getFile();
    if (file == null)
        return null;

    try
    {
      BufferedReader reader = null;
      reader = new BufferedReader(new FileReader(file));

      // Skip until the lineoffset in the file and start reading
      if (reader.ready())
          reader.skip(lineOffset);

      while (reader.ready() && lineNumber < lastLineToRead)
      {
             sourceLines[index] = reader.readLine();
             index++;
             lineNumber++;
      }
      reader.close();
    }
    catch (FileNotFoundException e)
    {
      System.out.println("FileNotFound exception");
      return null;
    }
    catch (IOException e)
    {
      System.out.println("IO exception while reading file");
      return null;
    }

    return sourceLines;
  }

  /**
   * Check if the name specified corresponds to a directory
   * @param name The string the may represent a directory
   * @return 'true' if the name is a directory and 'false' otherwise.
   */
  public boolean isADirectory(String name)
  {
    File file = new File(name);

    if (file != null && (file.isDirectory() || file.isAbsolute()))
        return true;

    return false;
  }

  /**
   * Return the URL representation for a given file.
   * @param file The file.
   * @return A string represent the URL.
   */
  public static String toURL(File file) throws MalformedURLException
  {
    String path = file.getAbsolutePath();
    if (File.separatorChar != '/')
    {
      path = path.replace(File.separatorChar, '/');
    }
    if (!path.startsWith("/"))
    {
      path = "/" + path;
    }
    if (!path.endsWith("/") && file.isDirectory()) {
      path = path + "/";
    }
    URL url = new URL("file", "", path);
    return url.toString();
  }

  private Vector _lineOffsets;
}
