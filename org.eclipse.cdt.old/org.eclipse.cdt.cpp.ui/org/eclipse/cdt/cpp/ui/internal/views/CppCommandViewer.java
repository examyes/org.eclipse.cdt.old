package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.cdt.dstore.hosts.views.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.*;

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

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
	setOutputId("org.eclipse.cdt.cpp.ui.CppOutputViewPart");
    }
     
    public void setInput(DataElement input)
    {
	ModelInterface api = _plugin.getModelInterface();
	if (input != _input)
	    {
		_resourceInput = api.findResource((DataElement)input);
		super.setInput((DataElement)input);
	    }
    }

    public void setInput(Object input)
    {
	ModelInterface api = _plugin.getModelInterface();
	if (input instanceof DataElement)
	    {
		if (input != _input)
		    {
			_resourceInput = api.findResource((DataElement)input);
			super.setInput((DataElement)input);
		    }
	    }
	else
	    {	if (input instanceof Repository)
		    {
			Repository project = (Repository)input;
			if (project.isOpen())
			    {
				super.setElementInput(project.getRemoteElement());
			    }
		    }
	        else if (input instanceof IResource)
		    {
			DataElement element = null;

			_resourceInput = (IResource)input;
			element = api.findResourceElement(_resourceInput);
			super.setElementInput(element);
		    }
		else
		    {
			_resourceInput = null;
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

    public void widgetSelected(SelectionEvent e)
    {
	Widget source = e.widget;
	
	if (source == _browseButton)
	    {
		IWorkspace workbench = _plugin.getPluginWorkspace();	

		ModelInterface api = _plugin.getModelInterface();
		DataElement input = api.findWorkspaceElement();
	
		ChooseProjectDialog dialog = new ChooseProjectDialog("Select Directory", input);
		dialog.open();
		if (dialog.getReturnCode() == dialog.OK)
		    {
			java.util.List result = dialog.getSelected();  
			if (result != null && result.size() > 0)
			    {
				DataElement selected = (DataElement)result.get(0);
				if (selected != null)
				    {
					setInput(selected);
				    }
			    }
		    }
		
	    }
	else
	    {
		super.widgetSelected(e);
	    }
    }

}
