package com.ibm.debug.launch;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/launch/PICLStartupInfo.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:07)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.ILauncher;

import com.ibm.debug.WorkspaceSourceLocator;

/**
 * This class is used to provide launch or startup information for a debug
 * session.  This class is abstract and cannot be instantiated.  One of its
 * subclasses (e.g. PICLAttachInfo, PICLLoadInfo) should be instantiated
 * instead.  This class is not intended to be subclassed outside of this
 * plugin.
 */

public abstract class PICLStartupInfo {

    private Object fResource;
    private ILauncher fLauncher;
    private WorkspaceSourceLocator fWorkspaceSourceLocator;
    private String fTitle;

    /**
     * Sets the resource to associate with the launch.  This must be
     * either an instance of IResource or IProject.
     * The resource must be set for a launch to be successful.
     * @param resource The resource to set
     * @see org.eclipse.core.resources.IResource
     * @see org.eclipse.core.resources.IProject
     */
    public void setResource(Object resource) {
        fResource = resource;
    }

    /**
     * Gets the resource
     * @return The resource which is either an instance of IResource or
     *         IProject, or null if it has not been set.
     * @see org.eclipse.core.resources.IResource
     * @see org.eclipse.core.resources.IProject
     */
    public Object getResource() {
        return fResource;
    }

    /**
     * Sets the launcher to associate with the launch.  The launcher must be
     * set for a launch to be successful.
     * @param launcher The launcher to set
     * @see ILauncher
     */
    public void setLauncher(ILauncher launcher) {
        fLauncher = launcher;
    }

    /**
     * Gets the launcher
     * @return The launcher
     * @see ILauncher
     */
    public ILauncher getLauncher() {
        return fLauncher;
    }

    /**
     * Sets the workspace source locator to associate with the launch.  The
     * workspace source locator must be set for a launch to be successful.
     * @param workspaceSourceLocator The workspace source locator to set
     * @see WorkspaceSourceLocator
     */
    public void setWorkspaceSourceLocator(WorkspaceSourceLocator workspaceSourceLocator) {
        fWorkspaceSourceLocator = workspaceSourceLocator;
    }

    /**
     * Gets the workspace source locator
     * @return The workspace source locator
     * @see WorkspaceSourceLocator
     */
    public WorkspaceSourceLocator getWorkspaceSourceLocator() {
        return fWorkspaceSourceLocator;
    }

    /**
     * Sets the title.  The title does not need to be set for a successful
     * launch and may be left as null.
     * @param title The title to set. This string is displayed to the user
     *              as part of the launch info in the debug view.
     */
    public void setTitle(String title) {
        fTitle = title;
    }

    /**
     * Gets the title
     * @return The title
     */
    public String getTitle() {
        return fTitle;
    }
}
