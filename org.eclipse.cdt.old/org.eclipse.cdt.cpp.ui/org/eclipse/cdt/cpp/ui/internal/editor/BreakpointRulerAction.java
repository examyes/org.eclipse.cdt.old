package com.ibm.cpp.ui.internal.editor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */

import com.ibm.cpp.ui.internal.*;

import com.ibm.lpex.core.*;
import com.ibm.lpex.alef.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;



/**
 *
 */
//public class BreakpointRulerAction extends MarkerRulerAction {	
public class BreakpointRulerAction extends LpexMarkerRulerAction {	

   CppPlugin  _plugin    = CppPlugin.getDefault();
   IWorkspace _workspace = _plugin.getPluginWorkspace();
   IResource _resource = null;
   int _rulerLine;
   int _start = -1;
   int _end = -1;
	private IEditorPart editor;
		
	public BreakpointRulerAction(ResourceBundle bundle, String prefix, IVerticalRuler ruler,
                                                                         ITextEditor editor) {
//		super(bundle, prefix, ruler, editor, IDebugConstants.BREAKPOINT_MARKER, false);
		super(bundle, prefix, ruler, editor, "com.ibm.debug.PICLLineBreakpoint", false);

	}
	
	/**
	 * Checks whether the element the breakpoint refers to is shown in this editor
	 */
	protected boolean breakpointElementInEditor(IBreakpointManager manager, IMarker marker) {
		return true;
	}
	/**
	 * @see MarkerRulerAction#getMarkers
	 */
/*
	protected List getMarkers() {
		
		List breakpoints= new ArrayList();
		
		IDocument document= getDocument();
		AbstractMarkerAnnotationModel model= getAnnotationModel();
		
      System.out.println("BreakpointRulerAction.getMarkers");

		if (model != null)
      {
         try
         {
            IWorkspaceRoot root = _workspace.getRoot();

//				IMarker[] markers= root.findMarkers(IDebugConstants.BREAKPOINT_MARKER, true,
				IMarker[] markers= root.findMarkers("com.ibm.debug.PICLLineBreakpoint", true,
                                                                    IResource.DEPTH_INFINITE);
				if (markers != null)
            {
					IBreakpointManager breakpointManager=
                                              DebugPlugin.getDefault().getBreakpointManager();

               System.out.println("BreakpointRulerAction.getMarkers - markers not null");

               boolean registered, inEditor, includesRuler;

					for (int i= 0; i < markers.length; i++)
               {
                  registered = breakpointManager.isRegistered(markers[i]);
                  inEditor = breakpointElementInEditor(breakpointManager, markers[i]);
                  includesRuler = includesRulerLine(model.getMarkerPosition(markers[i]), document);

                  System.out.println("BreakpointRulerAction.getMarkers - i = " +i);
                  System.out.println("BreakpointRulerAction.getMarkers - registered = " +registered);
                  System.out.println("BreakpointRulerAction.getMarkers - inEditor = " +inEditor);
                  System.out.println("BreakpointRulerAction.getMarkers - includesRuler = " +includesRuler);


						if (breakpointManager.isRegistered(markers[i]) &&
								breakpointElementInEditor(breakpointManager, markers[i]))
                     //                       &&
							//	includesRulerLine(model.getMarkerPosition(markers[i]), document))
                  {
							breakpoints.add(markers[i]);
                     System.out.println("BreakpointRulerAction.getMarkers - add breakpoint to list");
                  }
					}
				}
			}
         catch (CoreException x)
         {
            System.out.println("BreakpointRulerAction.getMarkers CoreException: " +x);
			}
		}

		return breakpoints;
	}
*/
	
	/**
	 * @see MarkerRulerAction#addMarker
	 */
	protected void addMarker()
   {
      IEditorInput editorInput= getTextEditor().getEditorInput();

      int rulerLine= getVerticalRuler().getLineOfLastMouseButtonActivity();
      _rulerLine = rulerLine;

      if (rulerLine >= 0)
      {
           int eolLength = System.getProperty("line.separator").length();
           LpexDocumentLocation loc = new LpexDocumentLocation(rulerLine+1, 1);
           LpexView lpexView = ((LpexAbstractTextEditor)getTextEditor()).getLpexView();
           _start = lpexView.charOffset(loc, eolLength);
           int len = lpexView.queryInt("length", loc);
           if (len > 0)
              len--;
           _end = _start + len;
      }

      IResource resource = getResource();
      _resource = resource;

      IWorkspaceRunnable body = new IWorkspaceRunnable()
      {
         public void run(IProgressMonitor monitor) throws CoreException
         {

            try
            {
               //IMarker breakpoint = _resource.createMarker(IDebugConstants.LINE_BREAKPOINT_MARKER);
               IMarker breakpoint = _resource.createMarker("com.ibm.debug.PICLLineBreakpoint");

               IBreakpointManager breakpointManager=
                                               DebugPlugin.getDefault().getBreakpointManager();
               breakpointManager.configureLineBreakpoint(breakpoint,
                           "com.ibm.debug.internal.picl" , true, _rulerLine + 1, -1, -1);
                  //         "com.ibm.debug.internal.picl" , true, _rulerLine + 1, _start, _end);  revisit - Adrian
                  // since it should match with EditorActionDelegate in case of multiple statements on same line, hence multiple breakpoints

               Map map= breakpoint.getAttributes();
               map.put(IMarker.MESSAGE, "cpp");
               breakpoint.setAttributes(map);

               try
               {
                  breakpointManager.addBreakpoint(breakpoint);
               }
               catch (DebugException de)
               {
                  System.out.println("BreakpointRulerAction: de" +de);
               }
            }
            catch (CoreException ce)
            {
                  System.out.println("BreakpointRulerAction: ce" +ce);
            }

         }
      };

      try
      {
         _workspace.run(body, null);
      }
      catch (CoreException ce)
      {
         System.out.println("BreakpointRulerAction invoke run() : ce" +ce);
      }
	}
	
	/**
	 * @see MarkerRulerAction#removeMarkers
	 */
	protected void removeMarkers(List markers) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		try {
			
			Iterator e= markers.iterator();
			while (e.hasNext()) {
				breakpointManager.removeBreakpoint((IMarker) e.next(), true);
			}
			
		} catch (CoreException e) {
			Shell shell= getTextEditor().getSite().getShell();
			String title= getString(getResourceBundle(), getResourceKeyPrefix() +
                                "error.remove.title", getResourceKeyPrefix() +
                                                         "error.remove.title");
			String msg= getString(getResourceBundle(), getResourceKeyPrefix() +
                            "error.remove.message", getResourceKeyPrefix() +
                                                       "error.remove.message");
			ErrorDialog.openError(shell, title, msg, e.getStatus());
		}
	}
}
