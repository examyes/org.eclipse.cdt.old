package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.editor.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.api.*;

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


public class FindObjectAction extends Action
{
    private CppEditor _editor;
    private LpexView  _lpexViewer;
    private boolean   _findDeclaration;

    public FindObjectAction(String title, CppEditor editor, boolean findDeclaration)
    {
	super(title);
	_findDeclaration = findDeclaration;
	_editor = editor;
	if (_editor != null)
	    {
		_lpexViewer = editor.getLpexView();
	    }
    }
    
    public void setEditor(CppEditor editor)
    {
	_editor = editor;
	if (_editor != null)
	    {
		_lpexViewer = editor.getLpexView();
	    }
    }
    
    public void run()
    {
	if (_lpexViewer != null)
	    {
		String str = getCurrentText(_lpexViewer);		
		int line = _lpexViewer.currentElement();
		
		String path = null;
		IEditorInput input = _editor.getEditorInput();
		if (input instanceof IFileEditorInput)
		    {
			IFile file = ((IFileEditorInput)input).getFile();
			path = new String(file.getLocation().toString());
			
			IProject project    = file.getProject();			
			CppPlugin plugin   = CppPlugin.getDefault();
			ModelInterface api  = plugin.getModelInterface();
			
			DataElement projectRoot = api.findProjectElement(project);
			if (projectRoot != null)
			    {
				DataStore dataStore = projectRoot.getDataStore();
				DataElement commandDescriptor = dataStore.localDescriptorQuery(projectRoot.getDescriptor(), 
											       "C_FIND_DECLARATION");
				if (commandDescriptor != null)
				    {				
					DataElement patternLoc = dataStore.createObject(null, "source", str, path+":"+line);
					
					ArrayList args = new ArrayList();
					args.add(patternLoc);
					DataElement status = dataStore.synchronizedCommand(commandDescriptor, args, projectRoot);
					
					if (status.getNestedSize() > 0)
					    {
						IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
						IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
						
						IWorkbenchPage persp= win.getActivePage();
						ILinkable viewPart = (ILinkable)persp.findView("com.ibm.cpp.ui.SelectedObjectViewPart");
						
						if (viewPart == null)
						    {
							try
							    {
								persp.showView("com.ibm.cpp.ui.SelectedObjectViewPart");
								viewPart = (ILinkable)persp.findView("com.ibm.cpp.ui.SelectedObjectViewPart");
								
							    }
							catch (PartInitException e)
							    {
								System.out.println(e);
							    }
						    }
						
						if (viewPart != null)
						    {
							viewPart.setInput(status.get(0));							
						    }
					    }
				    }		
			    }
		    }
	    }
    }
    
    private String getCurrentText(LpexView viewer)
    {
	StringBuffer currentText = new StringBuffer();
	String text = viewer.elementText(viewer.currentElement());
	
	// ZERO-based column preceding cursor's
	int cursor = viewer.currentPosition();
	int columnBegin = cursor - 2;
	int columnEnd   = cursor - 1;
	
	if (text != null && text.length() > columnBegin) 
	    {
		while (columnBegin >= 0) 
		    {
			char c = text.charAt(columnBegin);
			if (isSeparator(c))
			    break;
			currentText.insert(0, c);
			columnBegin--;
		    }

		while (columnEnd < text.length()) 
		    {
			char c = text.charAt(columnEnd);
			if (isSeparator(c))
			    break;
			currentText.append(c);
			columnEnd++;
		    }
	    }
	
	return currentText.toString();
    }

    boolean isSeparator(char c)
    {
	boolean result = !Character.isLetterOrDigit(c);
	return result;
    }
}
