package org.eclipse.cdt.dstore.core.util;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.lang.*; 

public class StringCompare
{
  public StringCompare()
  {    
  }
  
  public static boolean compare(String pattern, String compareStr, boolean noCase)
  { 
      if ((pattern == null) || (compareStr == null))
	  return false;

    if (noCase)
      {
	pattern    = pattern.toUpperCase();
	compareStr = compareStr.toUpperCase();
      }

    String currentMatch = new String("");

    int iText     = 0;   
    int iPattern  = 0;
    int lastStar  = 0;
    int len       = compareStr.length();  

    int patternLen = pattern.length();
    
    while (iPattern < patternLen)
      {
	char p = pattern.charAt(iPattern++);
	if (p == '*')
	  {
	    
	    if (iPattern >= patternLen)
	      {
		while (iText < len)
		  {		    
		      iText++;
		  }
		return true;		
	      }
	    else
	      {
		lastStar = iPattern;		
	      }	    
	  }
	else
	  {
	    if (iText >= len)
	      {
		return false;
	      }
	    else
	      {
		char t = compareStr.charAt(iText++);
		if (p == t)
		  {
		    if ((lastStar > 0) && 
			(iPattern >= patternLen) &&
			(iText < len))
		      {
		      }		    	
		    else
		      {			
			continue;			
		      }
		    
		  }
		else
		  {
		    if (lastStar == 0)
		      {
			return false;			
		      }
		  }
		
		int matched = iPattern - lastStar - 1;
		iPattern = lastStar;
		
		iText -= matched;
	      }	    
	  }		
      }

    if (iText >= len)
      {
	return true;	
      }
    else
      {
	return false;	
      }  
  }

  
}

