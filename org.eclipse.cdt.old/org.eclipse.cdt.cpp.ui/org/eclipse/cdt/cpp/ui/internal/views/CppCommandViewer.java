package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.hosts.views.*;

import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.*;

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import java.util.*;
import java.io.*;

import org.eclipse.jface.viewers.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;

import org.eclipse.ui.dialogs.*;

public class CppCommandViewer extends CommandViewer
{  	
    private CppPlugin          _plugin = CppPlugin.getDefault();
    private IResource          _resourceInput;
    
    public CppCommandViewer(Composite parent)
    { 
	super(parent);
	setOutputId("com.ibm.cpp.ui.CppOutputViewPart");
    }
     
    public void setInput(Object input)
    {
	if (input instanceof DataElement)
	    {
		super.setInput(input);
	    }
	else
	    {
		if (input instanceof IResource)
		    {
			DataElement element = null;

			_resourceInput = (IResource)input;
			ModelInterface api = _plugin.getModelInterface();
			element = api.findResourceElement(_resourceInput);
			super.setInput(element);
		    }
		else
		    {
			_resourceInput = null;
		    }
		
		if (input instanceof Repository)
		    {
			Repository project = (Repository)input;
			if (project.isOpen())
			    {
				super.setInput(project.getRemoteElement());
			    }
		    }
		else
		    {
			super.setInput(input);
		    }
	    }
    }
   
    public ArrayList readHistory()
    {
	if (_resourceInput != null)
	    {
		return _plugin.readProperty(_resourceInput, "Command History");
	    }
	return null;
    } 
    
    public void writeHistory()
    {
	if (_resourceInput != null)
	    {
		_plugin.writeProperty(_resourceInput, _plugin.getLocalizedString("CommandViewer.Command_History"), _history);
	    }
    }

}
