package org.eclipse.cdt.cpp.ui.internal.editor;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.ResourceBundle;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.cpp.ui.internal.editor.codeassist.CppSourceViewerConfiguration;
import org.eclipse.cdt.cpp.ui.internal.editor.contentoutliner.CppContentOutlinePage;
import org.eclipse.cdt.dstore.core.model.DE;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStore;
import org.eclipse.cdt.dstore.ui.resource.ResourceElement;
import org.eclipse.cdt.linux.help.listeners.CppeditorHelpListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.ibm.debug.pdt.DebugEditorActionContributor;
import com.ibm.lpex.alef.LpexPlugin;
import com.ibm.lpex.alef.LpexSourceViewer;
import com.ibm.lpex.alef.LpexTextEditor;
import com.ibm.lpex.core.LpexView;
import com.ibm.lpex.core.LpexWindow;

public class CppEditor extends LpexTextEditor
{
  protected CppContentOutlinePage page;
  private ResourceBundle fResourceBundle;
  private boolean _isParsed;
  private CppPlugin     _plugin;
  private DebugEditorActionContributor dbgEditorContributor;

  public CppEditor()
      {
         super();
         dbgEditorContributor = new DebugEditorActionContributor();
         _isParsed = false;
         _plugin = CppPlugin.getDefault();
	         setDocumentProvider(_plugin.getCppDocumentProvider());
//setDocumentProvider(new org.eclipse.ui.editors.text.FileDocumentProvider());
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
        dbgEditorContributor.createDebugRulerActions(this,getVerticalRuler());
        dbgEditorContributor.createDebugMenuActions(this);


	///////////////////////////////
	LpexSourceViewer lpexSourceViewer= (LpexSourceViewer)getSourceViewer();	
	LpexWindow lpexWindow = lpexSourceViewer.getLpexWindow();	
	LpexView lpexView = lpexSourceViewer.getLpexView();	
	try{
	    lpexWindow.addHelpListener(new CppeditorHelpListener(lpexView));
	}catch(Exception e){	
	    e.printStackTrace();
	}
	//////////////////////////////

      }


  public void editorContextMenuAboutToShow(IMenuManager menu)
      {
        super.editorContextMenuAboutToShow(menu);
        dbgEditorContributor.addDebugEditorMenuActions(menu,
           DebugEditorActionContributor.BREAKPOINT_MENU_ACTION |
           DebugEditorActionContributor.JUMP_MENU_ACTION |
           DebugEditorActionContributor.RUN_MENU_ACTION |
           DebugEditorActionContributor.MONITOR_MENU_ACTION |
           DebugEditorActionContributor.STORAGE_MENU_ACTION);

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
		 //addAction(menu, "ManageBreakpoints");
		 dbgEditorContributor.addDebugEditorRulerActions(menu,
								 DebugEditorActionContributor.BREAKPOINT_MENU_ACTION |
								 DebugEditorActionContributor.JUMP_MENU_ACTION |
								 DebugEditorActionContributor.RUN_MENU_ACTION);
	     }
	 setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK,
		   getAction(DebugEditorActionContributor.BREAKPOINT_RULER_ACTION_ID));
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
         ModelInterface api = ModelInterface.getInstance();	 	
         IFile file = ((IFileEditorInput)input).getFile();
         if (isCFile(file))
         {
			 if (file instanceof ResourceElement)
		     {
				 DataElement fileElement = ((ResourceElement)file).getElement();
				 api.parse(fileElement, false, false);
		     }
			 else
	    	 {
				 api.parse(file, false);
	     	 }
         	_isParsed = true;
      	}
      }
   }

    public void setFocus()
    {
	super.setFocus();
	

	IEditorInput input = getEditorInput();
	if (input instanceof IFileEditorInput)
	    {
		IFile file = ((IFileEditorInput)input).getFile();
		if (isCFile(file))
		{
			String fileName = file.getLocation().toOSString();		
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
      	}

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

  private boolean isCFile(IFile input)
  {
  	 String ext = input.getFileExtension().toLowerCase();
     if (ext.equals("cpp") ||
               ext.equals("c") ||
               ext.equals("hpp") ||
               ext.equals("h") ||
               ext.equals("inl") ||
               ext.equals("cxx")
              )	
              {
              	return true;
              }

     return false;
  }

  public Object getAdapter(Class key)
  {
     if (key.equals(IContentOutlinePage.class))
     {
        IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IFileEditorInput)
        {
           IFile input = ((IFileEditorInput)editorInput).getFile();
			if (isCFile(input))
           {
             page = new CppContentOutlinePage(input);
             return page;
           }
         }
	   }
      return super.getAdapter(key);
    }
}
