package com.ibm.cpp.ui.internal.editor.codeassist;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.resource.*;

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
      return null;
   }

	/**
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
   public char[] getContextInformationAutoActivationCharacters()
   {
      return null;
   }

	/**
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
   public char[] getCompletionProposalAutoActivationCharacters()
      {
        return null; 
      }
	
  /**
   * @see IContentAssistProcessor#computeTips(ITextViewer, int)
   */
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int position)
      {
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
			
		if (results == null || results.size() == 0)
		    {
			dataStore = _plugin.getHostDataStore();
			DataElement dictionaryData =  dataStore.findMinerInformation("com.ibm.dstore.miners.dictionary.DictionaryMiner");

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
		
		if (results != null && results.size() > 0)
		    {
			result= new ICompletionProposal[results.size()];
			for (int i = 0; i < results.size(); i++)
			    {
				DataElement found = ((DataElement)results.get(i)).dereference();
				String text     = (String)found.getElementProperty(DE.P_VALUE);
				String imageStr = com.ibm.dstore.ui.widgets.DataElementLabelProvider.getImageString(found);
				Image image     = _plugin.getImage(imageStr);
				
				int len = currentString.length();
				
				if (text.regionMatches(0, currentString, 0, len))
				    {
					result[i] = new CompletionProposal(text,               // replacement string
									   -len,               // replacement offset
									   text.length(),      // replacement length
									   0,                  // cursor position
									   image,
									   null, null, null);
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
						result[i] = new CompletionProposal(text,              // replacement string
										   0,                 // replacement offset
										   text.length(),     // replacement length
										   0,                 // cursor position
										   image,
										   null, null, null);
					    }
					else
					    {
						lastIndex++;
						String after = currentString.substring(lastIndex);
						result[i] = new CompletionProposal(text,
										   -after.length(),
										   text.length(),
										   0,
										   image,
										   null, null, null);
					    }
				    }
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
	      char lastChar = ' ';
	      
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
				 if (lastChar == '>')
				     isValid = true;
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
		 lastChar = c;
		 column--;
	     }
      }
      
      return currentText.toString();
    }
    
}
