package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.cpp.ui.internal.preferences.ParseQualityControl;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.wizards.*;

import org.eclipse.swt.layout.*;
import java.io.File;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class ParseBehaviourWorkbookPage 
{
    private Button _autoParseButton;
    private Composite _control;

    public ParseBehaviourWorkbookPage(Composite parent) 
    {
	_control = new Composite(parent, SWT.NONE);

	_autoParseButton = new Button(_control, SWT.CHECK);
	_autoParseButton.setText("Perform parse automatically on resource modification");
	
	_control.setLayout(new GridLayout());

	setDefaults();
    }


    public void setDefaults()
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList preferences = plugin.readProperty("AutoParse");
	if (preferences.isEmpty())
	    {
		_autoParseButton.setSelection(false);
	    }
	else
	    {
		String preference = (String)preferences.get(0);
		if (preference.equals("Yes"))
		    {
			_autoParseButton.setSelection(true);
		    }
		else
		    {
			_autoParseButton.setSelection(false);
		    }
	    }
    }
    
    public ArrayList getAutoParse()
    {
	ArrayList preferences = new ArrayList();
	if (_autoParseButton.getSelection())
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
