package com.ibm.dstore.miners.command.patterns;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.lang.*;

//This is just a convenience object for storing information parsed out of a line of output.
public class ParsedOutput
{
 public String  type;
 public String  text;
 public String  file;
 public int     line;
 public int     col;

 public ParsedOutput (String theType, String theText, String theFile, int theLine, int theColumn)
 {
  type = theType;
  text = theText; 
  file = theFile; 
  line = theLine; 
  col  = theColumn;
 }
}





















