package org.eclipse.cdt.cpp.ui.internal.editor.contributor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.editor.*;
import org.eclipse.cdt.cpp.ui.internal.actions.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.*;

import org.eclipse.ui.texteditor.*;
import com.ibm.lpex.alef.*;
import org.eclipse.ui.*;
import java.util.*;

public class CppEditorContextContributor extends LpexContextContributor
{
    private TextOperationAction _caAction;
    private FindObjectAction    _fAction;
    private JumpBackAction      _bAction;

    private IEditorPart _part;


public CppEditorContextContributor()
{
    super();

}

public void contributeToMenu(IMenuManager mbm)
{
    super.contributeToMenu(mbm);

    ResourceBundle bundle = LpexPlugin.getDefault().getResourceBundle();
    _caAction = new TextOperationAction(bundle, "ContentAssistProposal.", (ITextEditor)_part, ISourceViewer.CONTENTASSIST_PROPOSALS);
    _caAction.setText("Code assist@Ctrl+SPACE");
    
    _fAction = new FindObjectAction("Find Selected@F4", ( CppEditor)_part, true);
    _fAction.setText("Find Selected@F4");

    _bAction = new JumpBackAction("Jump Back@F9", (CppEditor)_part);
    _bAction.setText("Jump Back@F9");

    IMenuManager m = mbm.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
    m.add(_caAction);
    m.add(_fAction);
    m.add(_bAction);
}

public void contributeToToolBar(IToolBarManager tbm)
{
    super.contributeToToolBar(tbm);
}

public void setActiveEditor(IEditorPart part)
{
    super.setActiveEditor(part);
    _part = part;
    if (part instanceof ITextEditor)
	{
	    if (_caAction != null)
		{
		    _caAction.setEditor((ITextEditor)_part);
		    _caAction.update();
		}
	    if (_fAction != null)
		{
		    _fAction.setEditor((CppEditor)_part);
		}
	    if (_bAction != null)
		{
		    _bAction.setEditor((CppEditor)_part);
		}
	}
}
}


