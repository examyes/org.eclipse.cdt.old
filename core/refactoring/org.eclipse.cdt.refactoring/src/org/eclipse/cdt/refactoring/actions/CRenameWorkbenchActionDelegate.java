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
public class CRenameWorkbenchActionDelegate implements IWorkbenchWindowActionDelegate {
    private CRenameAction fAction= new CRenameAction();
    private IWorkbenchWindow fWorkbenchWindow;
    
    public void init(IWorkbenchWindow window) {
        fWorkbenchWindow= window;
    }
    public void selectionChanged(IAction action, ISelection selection) {
        boolean handled= false;
        IWorkbenchPage workbenchPage= fWorkbenchWindow.getActivePage();
        IWorkbenchPart workbenchPart= null;
        if (workbenchPage != null) {
            workbenchPart= workbenchPage.getActivePart();
            IEditorPart editor= workbenchPage.getActiveEditor();
            if (editor == workbenchPart) {
                fAction.setEditor(editor);
                handled= true;
            }
        }
        
        if (!handled && workbenchPart != null && selection instanceof IStructuredSelection) {
            Object o= ((IStructuredSelection) selection).getFirstElement();
            if (o != null) {
                fAction.setSite(workbenchPart.getSite());
                handled= CRefactory.getInstance().providePosition(o, fAction);
            }
        }
        
        if (!handled) {
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
