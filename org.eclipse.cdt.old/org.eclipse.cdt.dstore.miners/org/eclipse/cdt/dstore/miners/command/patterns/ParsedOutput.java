package com.ibm.dstore.miners.command.patterns;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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





















