package com.ibm.cpp.ui.internal.editor;

/* 
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.actions.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.editor.contentoutliner.*;
import com.ibm.cpp.ui.internal.editor.codeassist.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.*;

import org.eclipse.core.resources.*;
import com.ibm.lpex.alef.LpexTextEditor;
import com.ibm.lpex.core.LpexView;
import com.ibm.lpex.core.LpexWindow;
import com.ibm.lpex.core.LpexConstants;
import com.ibm.lpex.alef.*;

import org.eclipse.jface.action.IMenuListener;

import org.eclipse.core.resources.*;
import org.eclipse.ui.views.navigator.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ui.*;
import org.eclipse.ui.views.contentoutline.*;
import org.eclipse.ui.texteditor.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.util.ResourceBundle;
import java.util.ArrayList;

public class CppEditor extends LpexTextEditor
{
  protected CppContentOutlinePage page;
  private ResourceBundle fResourceBundle;
  private boolean _isParsed;
  private CppPlugin     _plugin;

  public CppEditor()
      {
         super();
         _isParsed = false;
         _plugin = CppPlugin.getDefault();
         setDocumentProvider(_plugin.getCppDocumentProvider());
      }

  protected void initializeEditor()
   {
      super.initializeEditor();
      super.setSourceViewerConfiguration(new CppSourceViewerConfiguration(this));
   }


    // test
    

   public void createPartControl(Composite parent)
      {
        super.createPartControl(parent);
      }

   /**
    * Returns this editor's resource bundle.
    *
    * @return the editor's resource bundle
    */
   private ResourceBundle getResourceBundle()
      {
        if (fResourceBundle == null)
        {
          fResourceBundle = LpexPlugin.getResourceBundle(); // borrow LPEX plugin's for now...
        }
          return fResourceBundle;
      }

  protected void createActions()
      {
        super.createActions();
        setAction("ManageBreakpoints",
                new BreakpointRulerAction(_plugin.getResourceBundle(),
                                          "ManageBreakpoints.",
                                           getVerticalRuler(),
                                           this));

        setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, getAction("ManageBreakpoints"));		

      }


  public void editorContextMenuAboutToShow(IMenuManager menu)
      {
        super.editorContextMenuAboutToShow(menu);

	/*
	FindObjectAction action = new FindObjectAction("Find Selected@F4", this, true);
	menu.add(action);
	setAction(ITextEditorActionConstants.FIND, action);
	*/
      }


    /**
     * @see AbstractTextEditor#rulerContextMenuAboutToShow
     */
    protected void rulerContextMenuAboutToShow(IMenuManager menu) 
    {
	super.rulerContextMenuAboutToShow(menu);
	// only add/remove breakpoints if clicked on a valid line in the edit window
	if (getVerticalRuler().getLineOfLastMouseButtonActivity() >= 0)
	    {
		addAction(menu, "ManageBreakpoints");
	    }
    }
    
    /**
     * Saves the contents of the target.
     * @see ITextEditor#doSave
     */

    public void doSave(IProgressMonitor monitor)
   {
      super.doSave(monitor);
      IEditorInput input = getEditorInput();
      if (input instanceof IFileEditorInput)
      {
         IFile file = ((IFileEditorInput)input).getFile();
         _isParsed = true;
         ModelInterface api = ModelInterface.getInstance();	 
         api.parse(file, false);
      }
   }

    public void setFocus()
    {
	super.setFocus();
	

	IEditorInput input = getEditorInput();
	if (input instanceof IFileEditorInput)
	    {
		IFile file = ((IFileEditorInput)input).getFile();
		String fileName = file.getLocation().toOSString();


		
	 /***
       	if (!_isParsed)
	      {
            _isParsed = true;
		
      		ModelInterface api = ModelInterface.getInstance();
      		DataStore dataStore = CppPlugin.getDefault().getCurrentDataStore();

      		IProject project = CppPlugin.getDefault().getCurrentProject();

      		DataElement parseMinerData = dataStore.findMinerInformation("com.ibm.cpp.core.miners.parser.ParseMiner");
      		DataElement projectObj = api.findProjectElement(project);
      		DataElement parsedSource = dataStore.find(projectObj, DE.A_NAME, "Parsed Files", 1);
      		
      		DataElement fileObj = dataStore.find(parsedSource, DE.A_NAME, fileName, 1);
      		if (fileObj == null)
      		{
      			api.parse(file, false);
      		}
      	}
      	else
      	{
      	}
	 ***/
      }
    }

  public void gotoMarker(IMarker marker)
      {
        super.gotoMarker(marker);

        LpexView lpexView = getLpexView();
        if (lpexView == null || marker == null)
           return;

        if (marker.getAttribute(IMarker.CHAR_START, 0) == -1) // if line marker
           lpexView.doCommand("set emphasisLength 100"); // emphasize 100 chars
        lpexView.triggerAction(lpexView.actionId("scrollCenter")); // make it a centerpiece
      }

    public void gotoLine(int line)
    {
        LpexView lpexView = getLpexView();
	if (lpexView != null)
	    {
		lpexView.doCommand("locate emphasis line " + line);
		lpexView.doCommand("set position 1");   // cursor on col.1 (/use whatever position appropriate)
		lpexView.doCommand("set emphasisLength 100"); // emphasize the line
		lpexView.triggerAction(lpexView.actionId("scrollCenter")); // make it a centerpiece
	    }
    }

  public Object getAdapter(Class key)
  {
     if (key.equals(IContentOutlinePage.class))
     {
        IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IFileEditorInput)
        {
           IFile input = ((IFileEditorInput)editorInput).getFile();
           String ext = input.getFileExtension().toLowerCase();
           if (ext.equals("cpp") ||
               ext.equals("c") ||
               ext.equals("hpp") ||
               ext.equals("h") ||
               ext.equals("inl") ||
               ext.equals("cxx")
              )
           {
             page = new CppContentOutlinePage(input);
             return page;
           }
         }
	   }
      return super.getAdapter(key);
    }
}
