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
			DataElement parent = _dataStore.find(dictionaryRoot, DE.A_NAME, new String("" + firstChar), 1);
			_dataStore.createObject(parent, "word", line);
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
	DataElement dictionary = createObjectDescriptor(schemaRoot, "dictionary");
	DataElement category = createObjectDescriptor(schemaRoot, "category");
	DataElement word = createObjectDescriptor(schemaRoot, "word");

	_dataStore.createReference(dictionary, category);
	_dataStore.createReference(dictionary, word);
	_dataStore.createReference(category, word);

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
		  
		  if (!child.isReference() && matcher.matches(child.getName(), pattern)) 
		      {
			  _dataStore.createReference(status, child, "contents");
		      }
		  
		  compareRegex(matcher, pattern, child, status);
	      }
      }

  }

