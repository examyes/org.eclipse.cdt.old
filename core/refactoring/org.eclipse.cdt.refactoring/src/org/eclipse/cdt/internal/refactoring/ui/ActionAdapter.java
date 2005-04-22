/*******************************************************************************
 * Copyright (c) 2005 Wind River Systems, Inc. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 

package org.eclipse.cdt.internal.refactoring.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ActionAdapter extends Action {
    private IWorkbenchWindowActionDelegate fDelegate;

    public ActionAdapter(IWorkbenchSite site, String name, IWorkbenchWindowActionDelegate delegate) {
        super(name);
        fDelegate= delegate;
        fDelegate.init(site.getWorkbenchWindow());
    }
    public void update(ISelection sel) {
        fDelegate.selectionChanged(ActionAdapter.this, sel);
    }
    public void run() {
        fDelegate.run(ActionAdapter.this);
    }
    public void dispose() {
        if (fDelegate != null) {
            fDelegate.dispose();
            fDelegate= null;
        }
    }
}
