package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.cpp.ui.internal.preferences.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;

import org.eclipse.swt.layout.*;
import java.io.File;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class ParseBehaviourWorkbookPage 
{
    private ParseBehaviourControl _parseBehaviourControl;

    private Composite _control;

    public ParseBehaviourWorkbookPage(Composite parent) 
    {
	_control = new Composite(parent, SWT.NONE);

	_parseBehaviourControl = new ParseBehaviourControl(_control, SWT.NONE);

	_control.setLayout(new GridLayout());

	setDefaults();
    }


    public void setDefaults()
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList autoParse = plugin.readProperty("AutoParse");
	if (autoParse.isEmpty())
	    {
		_parseBehaviourControl.setAutoParseSelection(false);
	    }
	else
	    {
		String preference = (String)autoParse.get(0);
		if (preference.equals("Yes"))
		    {
			_parseBehaviourControl.setAutoParseSelection(true);
		    }
		else
		    {
			_parseBehaviourControl.setAutoParseSelection(false);
		    }
	    }

	ArrayList autoPersist = plugin.readProperty("AutoPersist");
	if (autoPersist.isEmpty())
	    {
		_parseBehaviourControl.setAutoPersistSelection(false);
	    }
	else
	    {
		String preference = (String)autoPersist.get(0);
		if (preference.equals("Yes"))
		    {
			_parseBehaviourControl.setAutoPersistSelection(true);
		    }
		else
		    {
			_parseBehaviourControl.setAutoPersistSelection(false);
		    }
	    }
    }
    
    public ArrayList getAutoParse()
    {
	ArrayList preferences = new ArrayList();
	if (_parseBehaviourControl.getAutoParseSelection())
	    {
		preferences.add("Yes");
	    }
	else
	    {
		preferences.add("No");
	    }
	return preferences;
    }

    public ArrayList getAutoPersist()
    {
	ArrayList preferences = new ArrayList();
	if (_parseBehaviourControl.getAutoPersistSelection())
	    {
		preferences.add("Yes");
	    }
	else
	    {
		preferences.add("No");
	    }
	return preferences;
    }

    protected Control getControl() 
    {
	return _control;
    }
}
