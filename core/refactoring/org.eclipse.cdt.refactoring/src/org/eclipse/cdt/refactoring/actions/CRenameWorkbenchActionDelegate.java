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
 * Launches a rename refactoring.
 */          
public class CRenameWorkbenchActionDelegate implements IWorkbenchWindowActionDelegate {
    private CRenameAction fAction= new CRenameAction();
    private IWorkbenchWindow fWorkbenchWindow;
    
    public void init(IWorkbenchWindow window) {
        fWorkbenchWindow= window;
    }
    public void selectionChanged(IAction action, ISelection selection) {
        boolean disable= true;
        IWorkbenchPage workbenchPage= fWorkbenchWindow.getActivePage();
        if (workbenchPage != null) {
            IEditorPart editor= workbenchPage.getActiveEditor();
            if (editor == workbenchPage.getActivePart()) {
                fAction.setEditor(editor);
                disable= false;
            }
        }
        
        if (disable) {
            fAction.setEnabled(false);
            action.setEnabled(false);
        }
        else {
            action.setEnabled(fAction.isEnabled());
        }
    }        

    public void run(IAction action) {
        fAction.run();
    }
    
    public void dispose() {
    }
}
