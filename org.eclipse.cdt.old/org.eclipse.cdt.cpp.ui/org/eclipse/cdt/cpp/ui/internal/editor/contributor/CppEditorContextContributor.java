package com.ibm.cpp.ui.internal.editor.contributor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.editor.*;

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

    

    IMenuManager m = mbm.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
    m.add(_caAction);
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
	}
}

  /***
public void init(com.ibm.eclipse.tests.internal.core.watson.IActionBars b)
{
    super.init(b);
}
***/
}


