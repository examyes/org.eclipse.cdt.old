package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.editor.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;

import org.eclipse.ui.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;

import java.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.action.*;
import org.eclipse.core.resources.*;

import com.ibm.lpex.alef.*;
import com.ibm.lpex.core.*;
import com.ibm.lpex.alef.contentassist.*;

import org.eclipse.jface.text.ITextViewer;


public class JumpBackAction extends Action
{
    private CppEditor _editor;

    public JumpBackAction(String title, CppEditor editor)
    {
	super(title);
	_editor = editor;
    } 
    
    public void setEditor(CppEditor editor)
    {
	_editor = editor;
    }

    public void run()
    {   
	IOpenAction openAction = CppActionLoader.getInstance().getOpenAction();
	openAction.resetSelection();
    }
}
