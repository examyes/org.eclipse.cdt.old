package org.eclipse.cdt.cpp.ui.internal.editor.codeassist;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.resource.*;

import java.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.core.resources.*;

import com.ibm.lpex.alef.*;
import com.ibm.lpex.core.*;
import com.ibm.lpex.alef.contentassist.*;

import org.eclipse.jface.text.ITextViewer;

public class CppCompletionProcessor implements IContentAssistProcessor
{
  private CppPlugin _plugin;
  private IFile     _input;

  public CppCompletionProcessor(IFile input)
      {
        _plugin = CppPlugin.getDefault();
        _input = input;
      }
	
	
    /**
     * @see IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage()
    {
        return null;
    }
    
    /**
     * @see IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator()
    {
	System.out.println("get validator");
	return null;
    }
    
    /**
     * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
	//System.out.println("getcontextinfoautoactivationchars");
	return null;
	//	return new char[] { '(' };
    }
    
    /**
     * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {	  
        return new char[] { '.','>' };
    }
    
    /**
     * @see IContentAssistProcessor#computeTips(ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int position)
    {
	System.out.println("compute tips");
        return null;
    }
    
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position)
      {
        ICompletionProposal[] result = null;
        LpexTextViewer lview = (LpexTextViewer)viewer;
        String currentString = getCurrentText(lview);
        if (currentString != null
            && currentString.length() > 0)
	{
        	// find the parser
        	ModelInterface api = _plugin.getModelInterface();

        	IProject project = _input.getProject();

		DataElement status = null;
		ArrayList results = null;
		
		DataStore dataStore = null;
		DataElement projectRoot = api.findProjectElement(project);
		if (projectRoot != null)
		    {
			dataStore = projectRoot.getDataStore();
			DataElement commandDescriptor = dataStore.localDescriptorQuery(projectRoot.getDescriptor(),
										       "C_CODE_ASSIST");
			if (commandDescriptor != null)
			    {				
				ArrayList args = new ArrayList();	
				int line = lview.getLpexView().currentElement();
				
				String path = null;
				if (_input instanceof FileResourceElement)
				    {
					path = ((FileResourceElement)_input).getElement().getSource();
				    }
				else
				    {
					path = new String(_input.getLocation().toOSString());
				    }

				DataElement patternLoc = dataStore.createObject(null, "source", currentString, path+":"+line);				
				args.add(patternLoc);
				status = dataStore.synchronizedCommand(commandDescriptor, args, projectRoot);
				results = status.getNestedData();
			    }
		    }
			
			/*
		if (results == null || results.size() == 0)
		    {

			dataStore = _plugin.getHostDataStore();
			DataElement dictionaryData =  dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.dictionary.DictionaryMiner");

			String language = "english";
			DataElement root = dataStore.find(dictionaryData, DE.A_NAME, language, 1);
			if (root != null)
			    {
				DataElement pattern = dataStore.createObject(null, "pattern", currentString + ".*");
				DataElement search = dataStore.localDescriptorQuery(root.getDescriptor(),
										    "C_SEARCH_DICTIONARY", 1);
				if (search != null)
				    {	
					ArrayList sargs = new ArrayList();
					sargs.add(pattern);
					status = dataStore.synchronizedCommand(search, sargs, root);
					results = status.getNestedData();
				    }
			    }
		    }
		    */
		
		if (results != null && results.size() > 0)
		    {
			result= new ICompletionProposal[results.size()];
			for (int i = 0; i < results.size(); i++)
			    {
				DataElement found = ((DataElement)results.get(i)).dereference();
				String text     = found.getAttribute(DE.A_VALUE);

				// extract out '(' and ')'
				String replace = extractParams(text);

				String imageStr = CppActionLoader.getInstance().getImageString(found);
				Image image     = _plugin.getImage(imageStr);
				
				//ContextInformation info = null;
				ContextInformation info = new ContextInformation(image, text, text);
				int len = currentString.length();
				
				if (text.regionMatches(0, currentString, 0, len))
				    {
					result[i] = new CompletionProposal(replace,               // replacement string
									   -len,               // replacement offset
									   replace.length(),      // replacement length
									   0,                  // cursor position
									   image,
									   text, 
									   info, 
									   text);
				    }
				else
				    {
					int lastDotIndex = currentString.lastIndexOf(".");
					int lastPointerIndex = currentString.lastIndexOf("->");
					int lastIndex = 0;
					
					if (lastDotIndex == -1 && lastPointerIndex == -1)
					    lastIndex = -1;
					else
					    lastIndex = Math.max(lastDotIndex, lastPointerIndex);
					
					if (lastIndex == -1 || (lastIndex == len-1))
					    {
						result[i] = new CompletionProposal(replace,              // replacement string
										   0,                 // replacement offset
										   replace.length(),     // replacement length
										   0,                 // cursor position
										   image,
										   text, 
										   info, 
										   text);
					    }
					else
					    {
						lastIndex++;
						String after = currentString.substring(lastIndex);
						result[i] = new CompletionProposal(replace,
										   -after.length(),
										   replace.length(),
										   0,
										   image,
										   text, 
										   info, 
										   text);
					    }
				    }
			    }
		    }
	}


	return result;
      }

    private String extractParams(String text)
    {
	String result = new String("");
	boolean paramName = false;
	for (int i = 0; i< text.length(); i++)
	    {

		char c = text.charAt(i);
		switch (c)
		    {
		    case '(':
				paramName = true;
				result += c;
				break;
		    case ')':
				paramName = false;
				result += ' ';
				result += c;
				break;
		    case ',':
		        result += ' ';
				result += c;
				break;
		    default:
				if (!paramName)		
			    {
					result += c;
			    }
		    }
	    }
	return result;
    }

    private String getCurrentText(LpexTextViewer viewer)
    {
      StringBuffer currentText = new StringBuffer();

      LpexView lpexView = viewer.getLpexView();
      String text = lpexView.elementText(lpexView.currentElement());

      // ZERO-based column preceding cursor's
      int column = lpexView.currentPosition() - 2;

      if (text != null && text.length() > column)
	  {
         while (column >= 0)
	     {		
		 char c = text.charAt(column);
		
		 boolean isValid = Character.isLetterOrDigit(c);
		 if (!isValid)
		     {
			 switch (c)
			     {
			     case '_':
			     case '.':
				 isValid = true;
				 break;
			     case '-':
				 {
				     if ((column + 1) < text.length())
					 {
					     if (text.charAt(column + 1) == '>')
						 isValid = true;
					
					 }				
				 }
				 break;
			     case '>':
				 {
				     if ((column - 1) > 0)
					 {
					     if (text.charAt(column - 1) == '-')
						 isValid = true;
					 }
				 }
				 break;
			     default:
				 isValid = false;
				 break;
			     }
		     }
		
		 if (!isValid)
		     {
			 break;
		     }

		 currentText.insert(0, c);
		 column--;
	     }
      }

      return currentText.toString();
    }

}
