package com.ibm.dstore.miners.dictionary;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import com.ibm.dstore.core.util.regex.text.regex.*;
import java.util.*;
import java.lang.*;
import java.io.*;



public class DictionaryMiner extends Miner
{    
    private String _dictionary;
    private DataElement _dictionaryDescriptor;
    private DataElement _categoryDescriptor;
    private DataElement _wordDescriptor;
    private DataElement _nameDescriptor;
    private DataElement _containsDescriptor;

   public void extendSchema(DataElement schemaRoot)
    {
	_containsDescriptor  = _dataStore.findDescriptor(DE.T_RELATION_DESCRIPTOR, getLocalizedString("model.contents"));
	_dictionaryDescriptor = createObjectDescriptor(schemaRoot, "dictionary", "com.ibm.dstore.miners");
	_categoryDescriptor   = createObjectDescriptor(schemaRoot, "category", "com.ibm.dstore.miners");
	_wordDescriptor       = createObjectDescriptor(schemaRoot, "word", "com.ibm.dstore.miners");
	_nameDescriptor       = createObjectDescriptor(schemaRoot, "name", "com.ibm.dstore.miners");


	_dataStore.createReference(_dictionaryDescriptor, _categoryDescriptor, _containsDescriptor);
	_dataStore.createReference(_dictionaryDescriptor, _wordDescriptor, _containsDescriptor);
	_dataStore.createReference(_dictionaryDescriptor, _nameDescriptor, _containsDescriptor);
	_dataStore.createReference(_categoryDescriptor, _wordDescriptor, _containsDescriptor);
	_dataStore.createReference(_categoryDescriptor, _nameDescriptor, _containsDescriptor);

	DataElement cmd = createCommandDescriptor(_dictionaryDescriptor, "Search Dictionary", "C_SEARCH_DICTIONARY");	
    }

   public void load()
    {
	String fileLocation = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
	_dictionary = fileLocation + File.separator + "com.ibm.dstore.miners" + File.separator + "dictionary";	

	// english
	loadLanguage("english");
    }

    private void loadLanguage(String language)
    {
	DataElement languageRoot = _dataStore.createObject(_minerData, _dictionaryDescriptor, language); 

	String languageFile = _dictionary + File.separator + language + File.separator + "words";
	int offset = Character.digit('a', Character.MAX_RADIX);

	DataElement categories[] = new DataElement[26];
	for (int i = 0; i < 26; i++)
	    {
		char letter = Character.forDigit(i + offset, Character.MAX_RADIX);
		DataElement category = _dataStore.createObject(languageRoot, _categoryDescriptor, "" + letter); 
		categories[i] = category;
	    }
	try
	    {
		File wordsFile = new File(languageFile);
		FileInputStream inFile = new FileInputStream(wordsFile);
		BufferedReader in= new BufferedReader(new InputStreamReader(inFile));
		
		String line = null;
		while ((line = in.readLine()) != null)
		    {
			char firstChar = Character.toLowerCase(line.charAt(0));
			int index = Character.digit(firstChar, Character.MAX_RADIX) - offset;

			if ((index >= 0)  && (index < categories.length))
			    {
				DataElement parent = categories[index];
				if (parent != null)
				    {
					DataElement typeObject = _wordDescriptor;
					if (!Character.isLowerCase(firstChar))
					    {
						typeObject = _nameDescriptor; 
					    }
					
					_dataStore.createObject(parent, typeObject, line);
				    }
			    }
		    }
		
		inFile.close();
	    }
	catch (IOException e)
	    {
		System.out.println(e);
	    }
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
			  _dataStore.createReference(status, child, _containsDescriptor);
		      }
		  
		  compareRegex(matcher, pattern, child, status);
	      }
      }

  }

