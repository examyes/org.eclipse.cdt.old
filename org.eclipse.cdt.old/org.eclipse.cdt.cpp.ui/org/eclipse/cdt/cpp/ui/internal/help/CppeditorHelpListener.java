package org.eclipse.cdt.cpp.ui.internal.help;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.HelpEvent;
import com.ibm.lpex.core.LpexView;

import java.io.*;

public class CppeditorHelpListener implements HelpListener
{
    private LpexView _lpexView;   
  
    public CppeditorHelpListener(LpexView theLpexView)
    {
	_lpexView = theLpexView;	
    }

    public void helpRequested(HelpEvent e)
    {	
	String line = _lpexView.elementText(_lpexView.currentElement());
	
	StringBuffer keyword = new StringBuffer();

	int column = _lpexView.currentPosition()-2;	

	if(column>=line.length())
	    {
		return; //Do nothing when cursor is *beyond* the end of line
	    }

	for(int i=column;i>=0;i--)
	    {		
		char letter = line.charAt(i);		
		if(!isValidIdentifier(letter))
		    break;
		else
		    keyword.insert(0,letter);
	    }	

	for(int i=column+1;i<line.length();i++)
	    {
		char letter = line.charAt(i);
		if(!isValidIdentifier(letter))
		    break;
		else
		    keyword.append(letter);
	    }

	if(keyword.length()!=0)
	    {
		LaunchSearch.getDefault().showHelpView();	
		LaunchSearch.getDefault().doSearch(keyword.toString(),IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS);
	    }		
    }   

    private boolean isValidIdentifier(char c)
    {
	if (Character.isLetterOrDigit(c) || c=='_')
	    return true;
	else
	    return false;
    }
    
}
