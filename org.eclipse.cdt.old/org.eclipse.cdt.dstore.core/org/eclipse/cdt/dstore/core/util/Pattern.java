package org.eclipse.cdt.dstore.core.util;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.lang.*;

public class Pattern
{
  private String _pattern;

  private ArrayList _subMatches;
  private ArrayList _matchSchema;
  
  public Pattern(String pattern, ArrayList matchSchema )
  {    
    _pattern = pattern;  
    
    _matchSchema = matchSchema;
  }

  public ArrayList getSubMatches()
  {
    return _subMatches;
  }
      
  public String getSubMatch(String attribute)
  {
    // find attribute index in match schema
    int index = _matchSchema.indexOf(attribute);
    if ((index >= 0) && (index < _subMatches.size()))
      {	
	Object match = _subMatches.get(index);
	return new String((String)match);
      } 
    else
      {
	return new String("null");
      } 
  }
  
  public boolean matches(String compareStr)
  {
    String currentMatch = new String("");
    _subMatches = new ArrayList(); 

    int iText     = 0;   
    int iPattern  = 0;
    int lastStar  = 0;
    int len       = compareStr.length();  

    int patternLen = _pattern.length();
    
    while (iPattern < patternLen)
      {
	char p = _pattern.charAt(iPattern++);
	if (p == '*')
	  {
	    if (currentMatch.length() > 0)
	      {		
		_subMatches.add(new String(currentMatch));
	      }
	    currentMatch = new String("");
	    
	    if (iPattern >= patternLen)
	      {
		while (iText < len)
		  {		    
		    currentMatch += compareStr.charAt(iText++);
		  }
		_subMatches.add(new String(currentMatch));

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
		    currentMatch += t;		
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
	_subMatches.add(new String(currentMatch));	
	return true;	
      }
    else
      {
	return false;	
      }  
  }

  
}

