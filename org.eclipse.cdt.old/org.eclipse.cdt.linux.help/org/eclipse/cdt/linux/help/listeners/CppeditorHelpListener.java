package com.ibm.linux.help.listeners;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.HelpEvent;
import com.ibm.lpex.core.LpexView;

import com.ibm.linux.help.HelpPlugin;


public class CppeditorHelpListener implements HelpListener
{
    private LpexView _lpexView;   
    private HelpPlugin _plugin;
  
    public CppeditorHelpListener(LpexView theLpexView)
    {
	_lpexView = theLpexView;	
	_plugin= HelpPlugin.getDefault();
    }

    public void helpRequested(HelpEvent e)
    {
	
	String line = _lpexView.elementText(_lpexView.currentElement());
	
	StringBuffer keyword = new StringBuffer();

	int column = _lpexView.currentPosition()-2;

	for(int i=column;i>=0;i--)
	    {
		char letter = line.charAt(i);
		if(!isValidIdentifier(letter))
		    break;
		else
		    keyword.insert(0,letter);
	    }	
	_plugin.showMatches(keyword.toString());	
    }   
    private boolean isValidIdentifier(char c)
    {
	if (Character.isLetterOrDigit(c) || c=='_')
	    return true;
	else
	    return false;
    }

}
