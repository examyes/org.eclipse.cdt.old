package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLLaunchManager.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.dialogs.PICLLaunchWizard;
import com.ibm.debug.internal.pdt.ui.dialogs.PICLLaunchWizardDialog;
import com.ibm.debug.launch.IPICLLauncher;
import com.ibm.debug.launch.PICLStartupInfo;

import java.util.*;

public class PICLLaunchManager {

    private static PICLLaunchManager fLaunchManager = null;

    public static PICLLaunchManager getPICLLaunchManager() {
        if(fLaunchManager == null)
            fLaunchManager = new PICLLaunchManager();

        return fLaunchManager;
    }

    public boolean handleLaunch(final Object connectionKey, final PICLStartupInfo startupInfo, final Hashtable pairs) {
        if(startupInfo.getResource() != null && startupInfo.getLauncher() != null) {
            PICLDebugPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    ((IPICLLauncher)startupInfo.getLauncher().getDelegate()).launch(connectionKey, startupInfo, pairs);
                }
            });
        } else {
            final IStructuredSelection selection = resolveSelection(startupInfo.getResource());

            final Object[] launchers = resolveLaunchers(selection, startupInfo.getLauncher());
            if (launchers.length == 0) {
                // could not determine any launchers to use to launch
                // very unlikely to happen
                return false;
            }

            PICLDebugPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    useWizard(launchers, selection, connectionKey, startupInfo, pairs);
                }
            });
        }

        return true;
    }

    protected static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    protected String getMode() {
        return ILaunchManager.DEBUG_MODE;
    }

    /**
     * Resolves and returns a structured selection.
     */
    protected IStructuredSelection resolveSelection(Object resource) {
        IStructuredSelection selection = null;
        if(resource != null)
            selection = new StructuredSelection(resource);

        return selection;
    }

    /**
     * Resolves and returns the applicable launcher(s) to be used to launch the
     * elements in the specified selection.
     */
    protected Object[] resolveLaunchers(IStructuredSelection selection, ILauncher launcher) {

        if(launcher != null && launcher.getDelegate() instanceof IPICLLauncher) {
            Object[] launchers = new Object[1];
            launchers[0] = launcher;
            return launchers;
        }

        List launchers;
        if (selection == null || selection.isEmpty()) {
            launchers= Arrays.asList(getLaunchManager().getLaunchers(getMode()));
        } else {
            launchers= new ArrayList(2);
            Iterator elements= selection.iterator();
            while (elements.hasNext()) {
                Object element= elements.next();
                ILauncher defaultLauncher= null;
                try {
                    IResource resource= null;
                    if (element instanceof IAdaptable) {
                        IAdaptable el= (IAdaptable)element;
                        resource= (IResource)el.getAdapter(IResource.class);
                        if (resource == null) {
                            resource= (IProject)el.getAdapter(IProject.class);
                        }
                    }
                    IProject project= null;
                    if (resource != null) {
                        project= resource.getProject();
                    }
                    if (project != null) {
                        defaultLauncher= getLaunchManager().getDefaultLauncher(project);
                    }
                    if (defaultLauncher != null) {
                        if (!defaultLauncher.getModes().contains(getMode())) {
                            defaultLauncher= null;
                        }
                    }
                } catch (CoreException e) {
                }
                if (defaultLauncher != null) {
                    if (!launchers.contains(defaultLauncher)) {
                        launchers.add(defaultLauncher);
                    }
                }
            }
            if (launchers.isEmpty()) {
                launchers= Arrays.asList(getLaunchManager().getLaunchers(getMode()));
            }
        }

        return resolveVisibleLaunchers(launchers);
    }
    /**
     * Determines and returns the selection that provides context for the launch,
     * or <code>null</code> if there is no selection.
     */
    protected IStructuredSelection resolveSelection(IWorkbenchWindow window) {
        if (window == null) {
            return null;
        }
        ISelection selection= window.getSelectionService().getSelection();
        if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            // there is no obvious selection - go fishing
            selection= null;
            IWorkbenchPage page= window.getActivePage();
            if (page == null) {
                //workspace is closed
                return null;
            }

            // first, see if there is an active editor, and try its input element
            IEditorPart editor= page.getActiveEditor();
            Object element= null;
            if (editor != null) {
                element= editor.getEditorInput();
            }

            if (selection == null && element != null) {
                selection= new StructuredSelection(element);
            }
        }
        return (IStructuredSelection)selection;
    }
    protected Object[] resolveVisibleLaunchers(List launchers) {
        List visibleLaunchers= new ArrayList(2);
        Iterator itr= launchers.iterator();
        while (itr.hasNext()) {
            ILauncher launcher= (ILauncher)itr.next();
            if (launcher.getDelegate() instanceof IPICLLauncher &&
                PICLDebugPlugin.getDefault().isVisible(launcher)) {
                //cannot use itr.remove() as the list may be a fixed size list
                visibleLaunchers.add(launcher);
            }
        }
        return visibleLaunchers.toArray();
    }
    protected Object[] resolveWizardLaunchers(Object[] launchers) {
        List wizardLaunchers= new ArrayList(2);
        for (int i= 0 ; i < launchers.length; i++) {
            ILauncher launcher= (ILauncher)launchers[i];
            if (PICLDebugPlugin.getDefault().hasWizard(launcher)) {
                wizardLaunchers.add(launcher);
            }
        }
        return wizardLaunchers.toArray();
    }

    /**
     * Use the wizard to do the launch.
     */
    protected void useWizard(Object[] launchers, IStructuredSelection selection, Object connectionKey, PICLStartupInfo startupInfo, Hashtable pairs) {
        launchers= resolveWizardLaunchers(launchers);
        PICLLaunchWizard lw= new PICLLaunchWizard(launchers, selection, connectionKey, startupInfo, pairs);
        PICLLaunchWizardDialog dialog= new PICLLaunchWizardDialog(PICLDebugPlugin.getActiveWorkbenchWindow().getShell(), lw);
        dialog.open();
    }
}
