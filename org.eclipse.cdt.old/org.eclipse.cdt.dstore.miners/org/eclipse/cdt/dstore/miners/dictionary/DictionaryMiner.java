package com.ibm.dstore.miners.dictionary;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import com.ibm.dstore.core.util.regex.text.regex.*;
import java.util.*;
import java.lang.*;
import java.io.*;



public class DictionaryMiner extends Miner
{    
   public void load()
    {
	DataElement dictionaryRoot = _dataStore.createObject(_minerData, "dictionary", "English Words"); 

	int offset = Character.digit('a', Character.MAX_RADIX);

	for (int i = 0; i < 26; i++)
	    {
		char letter = Character.forDigit(i + offset, Character.MAX_RADIX);
		DataElement category = _dataStore.createObject(dictionaryRoot, "category", new String("" + letter)); 
	    }

	String fileLocation = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
	String dictionary = fileLocation + File.separator + "com.ibm.dstore.miners" + File.separator + "words";

	try
	    {
		File wordsFile = new File(dictionary);
		FileInputStream inFile = new FileInputStream(wordsFile);
		BufferedReader in= new BufferedReader(new InputStreamReader(inFile));
		
		String line = null;
		while ((line = in.readLine()) != null)
		    {
			char firstChar = line.charAt(0);
			DataElement parent = _dataStore.find(dictionaryRoot, DE.A_NAME, 
							     new String("" + Character.toLowerCase(firstChar)), 1);				
			String type = "word";
			if (!Character.isLowerCase(firstChar))
			    {
				type = "name";
			    }
			_dataStore.createObject(parent, type, line);
		    }
		
		inFile.close();
	    }
	catch (IOException e)
	    {
		System.out.println(e);
	    }
    }

   public void extendSchema(DataElement schemaRoot)
    {
	DataElement dictionary = createObjectDescriptor(schemaRoot, "dictionary", "com.ibm.dstore.miners");
	DataElement category = createObjectDescriptor(schemaRoot, "category", "com.ibm.dstore.miners");
	DataElement word = createObjectDescriptor(schemaRoot, "word", "com.ibm.dstore.miners");
	DataElement name = createObjectDescriptor(schemaRoot, "name", "com.ibm.dstore.miners");

	_dataStore.createReference(dictionary, category);
	_dataStore.createReference(dictionary, word);
	_dataStore.createReference(dictionary, name);
	_dataStore.createReference(category, word);
	_dataStore.createReference(category, name);

	DataElement cmd = createCommandDescriptor(dictionary, "Search Dictionary", "C_SEARCH_DICTIONARY");
	
    }
 
   public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);
	
	if (name.equals("C_SEARCH_DICTIONARY"))
	    {
		DataElement  pattern   = getCommandArgument(theCommand, 1);
		return handleRegexSearch(subject, pattern, status);	 
	    }
	
	status.setAttribute(DE.A_NAME, "done");
	return status;
    }
      
      
      private DataElement handleRegexSearch(DataElement dictionary, DataElement pattern, DataElement status)
      {      
	  status.setAttribute(DE.A_NAME, getLocalizedString("model.progress"));
	  
	  // find matches
	  compareRegex(pattern.getName(), dictionary, status);
	  status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
	  _dataStore.update(status);
	  return status;      
      } 
      
      private void compareRegex(String patternStr, DataElement dictionary, DataElement status)
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
	  
	  compareRegex(matcher, pattern, dictionary, status);
      }
      
      private void compareRegex(PatternMatcher matcher, Pattern pattern, 
				DataElement dictionary, DataElement status)
      {
	  for (int i = 0; i < dictionary.getNestedSize(); i++)
	      {
		  DataElement child = dictionary.get(i);
		  
		  if (!child.isReference() && 
		      (child.getType().equals("word") || child.getType().equals("name")) && 
		      matcher.matches(child.getName(), pattern)) 
		      {
			  _dataStore.createReference(status, child, "contents");
		      }
		  
		  compareRegex(matcher, pattern, child, status);
	      }
      }

  }

