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
        
import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Launches a rename refactoring.
 */          
public class CRenameViewActionDelegate implements IViewActionDelegate, IObjectActionDelegate {
    CRenameAction fAction= new CRenameAction();
    // IViewActionDelegate
    public void init(IViewPart view) {
        fAction.setWorkbenchPart(view);
    }
    // IObjectActionDelegate
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        fAction.setWorkbenchPart(targetPart);
    }
    public void run(IAction action) {
        fAction.run();
    }
    public void selectionChanged(IAction action, ISelection selection) {
        ISourceRange range= null;
        IFile file= null;
        String text= null;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss= (IStructuredSelection) selection;
            Object o= ss.getFirstElement();
            if (o instanceof ISourceReference) {
                ISourceReference sref= (ISourceReference) o;
                try {
                    range= sref.getSourceRange();
                } catch (CModelException e) {
                }
            }
            if (o instanceof ICElement) {
                ICElement e= (ICElement) o;
                IResource r= e.getUnderlyingResource();
                text= e.getElementName();
                if (r instanceof IFile) {
                    file= (IFile) r;
                }
            }
        }
        if (range != null && file != null && text != null) {
            int offset= range.getIdStartPos();
            int useLength= 0;
            int idx= text.length()-1;
            while (idx >= 0) {
                char c= text.charAt(idx);
                if (!Character.isLetterOrDigit(c) &&  c!='_') {
                    text= text.substring(idx+1);
                    break;
                }
                useLength++;
                idx--;
            }
            offset= range.getIdStartPos()+range.getIdLength()-useLength;
            fAction.setPosition(file, offset, text);
            action.setEnabled(fAction.isEnabled());
        }
        else {
            fAction.setEnabled(false);
            action.setEnabled(false);
        }
    }
}
