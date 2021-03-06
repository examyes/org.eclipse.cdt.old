package org.eclipse.cdt.linux.help.filter;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.dstore.core.util.regex.text.regex.*;
import org.eclipse.cdt.linux.help.*;
import org.eclipse.cdt.linux.help.ItemElement;
import org.eclipse.cdt.linux.help.preferences.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.DataStoreCorePlugin;
import org.eclipse.cdt.linux.help.views.*;

public class HelpFilter
{
    private static ArrayList _indexList = null;// "list of indexes" of filtered ItemElements 
    private static ArrayList _patternList = null; //"list of patterns" to check against.

    DataElement status;
    String _keyText;

    public HelpFilter()
    {		
    }
    
     // check whether 'key' is a valid one.(i.e. neither 'null' nor "")
    private boolean checkValidKey(String key)
    {
	if(key==null)
	    return false;
	else if(key.trim().length()==0)
	    return false;
	else
	    return true;
    }

    //Given a 'list' of ItemElements, update the '_indexList' to reflect the filters in place.
    public void updateIndexList(ArrayList list)
    {	
	if(_indexList==null)
	    _indexList=new ArrayList();
	else
	    _indexList.clear();
	
	//load the 'list of patterns' to check against
	loadPatterns();

	//Store in '_indexList' the indexes of the list of ItemElements that do not match any of the patterns
	for(int i=0;i<list.size();i++)
	    {
		if(!isMatched((ItemElement)list.get(i)))
		    _indexList.add(new Integer(i));
	    }	
    }

    //updates the '_patternList' with the stored patterns
    public void loadPatterns ()
    {
	ArrayList filterList = new ArrayList();
	
	HelpSettings settings = new HelpSettings();
	settings.read();

	String filters = settings.get(IHelpSearchConstants.HELP_FILTER_PATTERNS);	

	if(filters==null)
	    {
		_patternList = new ArrayList();
		return;
	    }

	StringTokenizer tokenizer = new StringTokenizer(filters,"##");
	while(tokenizer.hasMoreTokens())
	    {
		filterList.add(tokenizer.nextToken());
	    }
	
	_patternList = filterList;	
    }

    //return 'true' if the given ItemElement is matched by any of the patterns in '_patternList'
    private boolean isMatched(ItemElement item)
    {	
	PatternMatcher matcher = new Perl5Matcher();
	PatternCompiler compiler = new Perl5Compiler();

	Pattern pattern = null;
	for(int i=0;i<_patternList.size();i++)
	    {
		String filterPattern = (String)_patternList.get(i);		
		try
		    {
			pattern = compiler.compile(filterPattern);
		    }
		catch (MalformedPatternException e)
		    {
			System.err.println(e.getMessage());
			e.printStackTrace();
		    }

		//select the 'name'(man) or the 'key'(info) of the ItemElement to compare against the pattern.
		String theItem= item.getName();
		if(theItem==null) 
		    theItem=item.getKey();
		
		if(matcher.matches(theItem,pattern))   
		    return true; 		
	    }	
	return false;
    }

    // returns a "list" of filtered ItemElements. Assume '_indexList' has been previously updated. 
    public ArrayList getFilteredResults()
    {
	//get the list of ItemElements 
	ArrayList unfilteredList = HelpPlugin.getDefault().getList();

	//create a new list of ItemElements that correspond to the '_indexList' and return it.
	ArrayList filteredList = new ArrayList();
	for(int i=0;i<_indexList.size();i++)
	    {
		filteredList.add(unfilteredList.get(((Integer)_indexList.get(i)).intValue()));
	    }
	return filteredList;
    }

    //Given an 'index' into the "list of indexes", it returns the corresponding ItemElement
    public ItemElement getItem(int index)
    {	
	int itemElementIndex = ((Integer)_indexList.get(index)).intValue();
	return HelpPlugin.getDefault().getItemElement(itemElementIndex);
    }

}
