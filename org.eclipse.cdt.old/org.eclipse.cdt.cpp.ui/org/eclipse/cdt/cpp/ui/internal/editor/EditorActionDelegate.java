package com.ibm.cpp.ui.internal.editor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.cpp.ui.internal.*;

import com.ibm.lpex.core.*;
import com.ibm.lpex.alef.*;

import java.util.Map;
/**
 * This class is used to demonstrate editor action extensions.
 * An extension should be defined in the readme plugin.xml.
 */
public class EditorActionDelegate implements IEditorActionDelegate {
	private IEditorPart editor;

	IFileEditorInput _input;
                int  _line;
   CppPlugin  _plugin    = CppPlugin.getDefault();
   IWorkspace _workspace = _plugin.getPluginWorkspace();

/**
 * Creates a new EditorActionDelegate.
 */
public EditorActionDelegate() {
}
/* (non-Javadoc)
 * Method declared on IActionDelegate
 */
public void run(IAction action) {
	IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
	IFile file = input.getFile();
   _input = input;

   LpexView lpexView = ((LpexAbstractTextEditor)editor).getLpexView();
   if (lpexView == null)
      return;

   // retrieve document line for the cursored element (may be a show line), if any
   int line = lpexView.lineOfElement(lpexView.currentElement());
   if (line == 0)
      return;

   _line = line;

	if (file != null)
	    {

          IWorkspaceRunnable body = new IWorkspaceRunnable()
          {
             public void run(IProgressMonitor monitor) throws CoreException
             {
                try
      		    {

      		
            		IResource resource= (IResource) _input.getAdapter(IFile.class);

          	//	   IMarker breakpoint = file.createMarker("com.ibm.debug.PICLLineBreakpoint");
          		   IMarker breakpoint = resource.createMarker("com.ibm.debug.PICLLineBreakpoint");
          			
          			IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
          			breakpointManager.configureLineBreakpoint(breakpoint, "com.ibm.debug.internal.picl" , true,
                                                            _line, -1, -1);  // to revisit, just a line breakpoint for now! - Adrian
                  // since it should match with BreakpointRulerAction in case of multiple statements on same line, hence multiple breakpoints
          			
          			Map map= breakpoint.getAttributes();
          			map.put(IMarker.MESSAGE, "cpp");
          			breakpoint.setAttributes(map);
          			
          			try
          			{
          			  breakpointManager.addBreakpoint(breakpoint);
                  }
          			catch (DebugException de)
          			{
          			  System.out.println(de);
          			}
      		    }
                catch (CoreException ce)
      		    {
      			   System.out.println(ce);
      		    }
             }
          };
          try
          {
             _workspace.run(body, null);
          }
          catch (CoreException ce)
          {
             System.out.println("EditorActionDelegate invoke run() : ce" +ce);
          }
	    }
}
/**
 * The <code>EditorActionDelegate</code> implementation of this
 * <code>IActionDelegate</code> method
 *
 * Selection in the desktop has changed. Plugin provider
 * can use it to change the availability of the action
 * or to modify other presentation properties.
 *
 * <p>Action delegate cannot be notified about
 * selection changes before it is loaded. For that reason,
 * control of action's enable state should also be performed
 * through simple XML rules defined for the extension
 * point. These rules allow enable state control before
 * the delegate has been loaded.</p>
 */
public void selectionChanged(IAction action, ISelection selection) {
}
/**
 * The <code>EditorActionDelegate</code> implementation of this
 * <code>IEditorActionDelegate</code> method
 *
 * The matching editor has been activated. Notification
 * guarantees that only editors that match the type for which
 * this action has been registered will be tracked.
 *
 * @param action action proxy that represents this delegate in the desktop
 * @param editor the matching editor that has been activated
 */
public void setActiveEditor(IAction action, IEditorPart editor) {
	this.editor = editor;
}
}
