package com.ibm.cpp.miners.search;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.core.util.StringCompare;
import com.ibm.dstore.core.util.regex.text.regex.*;


import java.util.*;

public class SearchMiner extends Miner
  {
    
   public void load()
    {
    }


   public void extendSchema(DataElement schemaRoot)
    {
	DataElement solD       = _dataStore.find(schemaRoot, DE.A_NAME, "Workspace", 1);
	createCommandDescriptor(solD, "Search", "C_SEARCH", false);
	createCommandDescriptor(solD, "Regular Expression Search", "C_SEARCH_REGEX", false);
    }
 
   public DataElement handleCommand(DataElement theCommand)
    {
     String name          = getCommandName(theCommand);
     DataElement  status  = getCommandStatus(theCommand);
     DataElement  subject = getCommandArgument(theCommand, 0);
     DataElement  pattern = getCommandArgument(theCommand, 1);


     if (name.equals("C_SEARCH"))
       {
	 return handleSearch(subject, pattern, status);	 
       }
     else if (name.equals("C_SEARCH_REGEX"))
	 {
	     return handleRegexSearch(subject, pattern, status);
	 }
     
     status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
     return status;
    }

    public DataElement handleRegexSearch(DataElement subject, DataElement pattern, DataElement status)
    {      
      status.setAttribute(DE.A_NAME, getLocalizedString("model.progress"));

      boolean ignoreCase = true;
      
      String caseCheck = pattern.getValue();
      if (caseCheck.equals("check_case"))
	{
	  ignoreCase = false;
	}

      DataElement type = (DataElement)pattern.get(0).dereference();

      // find matches
      compareRegex(pattern.getName(), type, subject, status);
      status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
      _dataStore.update(status);
      return status;      
    } 

    public DataElement handleSearch(DataElement subject, DataElement pattern, DataElement status)
    {      
      status.setAttribute(DE.A_NAME, getLocalizedString("model.progress"));

      boolean ignoreCase = true;
      
      String caseCheck = pattern.getValue();
      if (caseCheck.equals("check_case"))
	{
	  ignoreCase = false;
	}

      DataElement type = (DataElement)pattern.get(0).dereference();

      // find all matching types
      ArrayList matches = _dataStore.findObjectsOfType(subject, type);

      // find matches
      compare(pattern.getName(), matches, status);

      status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
      _dataStore.update(status);
      return status;      
    } 

      public void compareRegex(String patternStr, DataElement type, DataElement root, DataElement status)
      {
	  PatternMatcher matcher   = new Perl5Matcher();
	  PatternCompiler compiler = new Perl5Compiler();

	  Pattern pattern = null;
	  try 
	      {
		  pattern = compiler.compile(patternStr);
	      } 
	  catch(MalformedPatternException e) 
	      {
		  System.err.println(e.getMessage());
		  return;
	      }

	  if (root.getType().equals("Workspace"))
	      {
		  for (int i = 0; i < root.getNestedSize(); i++)
		      {
			  DataElement project = root.get(i);
			  for (int j = 0; j < project.getNestedSize(); j++)
			      {
				  DataElement child = project.get(j);
				  if (child.isReference() && child.getType().equals("Parse Reference"))
				      {
					  compareRegex(matcher, pattern, type, child.dereference(), status);
				      }
			      }
		      }
	      }
	  else
	      {
		  compareRegex(matcher, pattern, type, root, status);
	      }
      }
      
      public void compareRegex(PatternMatcher matcher, Pattern pattern, 
			       DataElement type, DataElement root, DataElement status)
      {
	  for (int i = 0; i < root.getNestedSize(); i++)
	      {
		  DataElement child = root.get(i);		  
		  if (!child.isReference() && matcher.matches(child.getName(), pattern)) 
		      {
			  if (child.isOfType(type))
			      {
				  _dataStore.createReference(status, child, getLocalizedString("model.contents"));
			      }
		      }
		  
		  compareRegex(matcher, pattern, type, child, status);
	      }
      }

      public void compare(String pattern, ArrayList input, DataElement status)
      {
	  for (int i = 0; i < input.size(); i++)
	      {
		  DataElement match = (DataElement)input.get(i);

		  if (!match.isReference() && !match.isDeleted())
		      {		
			  if (compareString(pattern, match.getValue(), false))
			      {
				  
				  _dataStore.createReference(status, match, getLocalizedString("model.contents"));
			      }
		      }
	      }
      }
      
      public boolean compareString(String patternStr, String compareStr, boolean ignoreCase)
      {
	  return StringCompare.compare(patternStr, compareStr, ignoreCase);
      }
  }

