package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.editor.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.views.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;

import org.eclipse.ui.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.ui.dialogs.*;

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
