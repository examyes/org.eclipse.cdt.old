package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.actions.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.views.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.model.*;

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
		super(parent, CppActionLoader.getInstance());
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
