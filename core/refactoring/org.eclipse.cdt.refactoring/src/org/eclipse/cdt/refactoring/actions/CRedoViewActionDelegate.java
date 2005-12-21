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
        
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;

/**
 * Undoes a refactoring.
 * @deprecated This action is now longer needed. Undo is now performed via the
 *  global undo/redo stack provided by <code>org.eclipse.core.commands</code>. 
 */          
public class CRedoViewActionDelegate implements IViewActionDelegate, IObjectActionDelegate {
    private IWorkbenchWindowActionDelegate fDelegate;

    public void init(IViewPart view) {
        setActivePart(null, view);
    }
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        if (targetPart == null) {
            if (fDelegate != null) {
                fDelegate.dispose();
                fDelegate= null;
            }
        }
        else {
            if (fDelegate==null) {
                fDelegate= new org.eclipse.ltk.ui.refactoring.RedoRefactoringAction();
                fDelegate.init(targetPart.getSite().getWorkbenchWindow());
            }
        }
    }

    public void run(IAction action) {
        if (fDelegate != null) {
            fDelegate.run(action);
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        if (fDelegate != null) {
            fDelegate.selectionChanged(action, selection);
        }
    }
}
