package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.actions.*;
import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.views.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.TableContentProvider;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.model.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class CppOutputViewer extends OutputViewer  
{
    private OpenEditorAction      _cppOpenEditorAction;
    
    public CppOutputViewer(Table parent)
    {
	super(parent);
    }

    protected void handleDoubleSelect(SelectionEvent event)
    {
	if (_selected != null)
	    {
		if (_cppOpenEditorAction == null)
		    {
			_cppOpenEditorAction = new OpenEditorAction(_selected);
		    }
		
		_cppOpenEditorAction.setSelected(_selected);
		_cppOpenEditorAction.run();    
	    }
    }
    
}
