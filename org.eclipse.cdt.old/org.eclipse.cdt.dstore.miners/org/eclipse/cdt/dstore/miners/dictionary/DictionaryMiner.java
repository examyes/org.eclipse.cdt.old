package org.eclipse.cdt.dstore.miners.dictionary;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;

import org.eclipse.cdt.dstore.core.util.regex.text.regex.*;
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

    private DataElement _languageRoot;

   public void extendSchema(DataElement schemaRoot)
    {
	_containsDescriptor  = _dataStore.findDescriptor(DE.T_RELATION_DESCRIPTOR, getLocalizedString("model.contents"));
	_dictionaryDescriptor = createObjectDescriptor(schemaRoot, "dictionary", "org.eclipse.cdt.dstore.miners");
	_categoryDescriptor   = createObjectDescriptor(schemaRoot, "category", "org.eclipse.cdt.dstore.miners");
	_wordDescriptor       = createObjectDescriptor(schemaRoot, "word", "org.eclipse.cdt.dstore.miners");
	_nameDescriptor       = createObjectDescriptor(schemaRoot, "name", "org.eclipse.cdt.dstore.miners");


	DataElement dictQuery  = createCommandDescriptor(_dictionaryDescriptor, 
							 getLocalizedString("model.Query"), "C_QUERY", false);

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
	_dictionary = fileLocation + File.separator + "org.eclipse.cdt.dstore.miners" + File.separator + "dictionary";	
	_languageRoot = _dataStore.createObject(_minerData, _dictionaryDescriptor, "english"); 
    }

   public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);
	
	if (name.equals("C_SEARCH_DICTIONARY"))
	    {
		if (_languageRoot.getNestedSize() == 0)
		    {
			loadLanguage(_languageRoot);
		    }

		DataElement  pattern   = getCommandArgument(theCommand, 1);
		return handleRegexSearch(subject, pattern, status);	 
	    }
	else if (name.equals("C_QUERY") && (subject == _languageRoot))
	    {
		// english
		loadLanguage(_languageRoot);
	    }
	
	status.setAttribute(DE.A_NAME, "done");
	return status;
    }


    private void loadLanguage(DataElement languageRoot)
    {
	if (languageRoot.getNestedSize() > 0)
	    {
		return;
	    }
		
	String languageFile = _dictionary + File.separator + languageRoot.getName() + File.separator + "words";
	File wordsFile = new File(languageFile);
	if (wordsFile.exists())
	    {
		int offset = Character.digit('a', Character.MAX_RADIX);
		
		DataElement categories[] = new DataElement[26];
		for (int i = 0; i < 26; i++)
		    {
			char letter = Character.forDigit(i + offset, Character.MAX_RADIX);
			Character theLetter = new Character(letter);
			DataElement category = _dataStore.createObject(languageRoot, _categoryDescriptor, theLetter.toString()); 
			categories[i] = category;
		    }
		try
		    {
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
		
		_dataStore.refresh(languageRoot);
	    }
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

