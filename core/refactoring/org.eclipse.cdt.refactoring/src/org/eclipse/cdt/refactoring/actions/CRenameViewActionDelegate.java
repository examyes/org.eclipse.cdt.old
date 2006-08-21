/*******************************************************************************
 * Copyright (c) 2004-2005 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 

package org.eclipse.cdt.refactoring.actions;
        
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

/**
 * Launches a rename refactoring.
 */          
public class CRenameViewActionDelegate implements IViewActionDelegate, IObjectActionDelegate {
    CRenameAction fAction= new CRenameAction();
    // IViewActionDelegate
    public void init(IViewPart view) {
        fAction.setSite(view.getSite());
    }
    // IObjectActionDelegate
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        fAction.setSite(targetPart.getSite());
    }
    public void run(IAction action) {
        fAction.run();
    }
    public void selectionChanged(IAction action, ISelection selection) {
        boolean handled= false;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss= (IStructuredSelection) selection;
            Object o= ss.getFirstElement();
            handled= CRefactory.getInstance().providePosition(o, fAction);
        }
        if (handled) {
            action.setEnabled(fAction.isEnabled());
        }
        else {
            fAction.setEnabled(false);
            action.setEnabled(false);
        }
    }
}
