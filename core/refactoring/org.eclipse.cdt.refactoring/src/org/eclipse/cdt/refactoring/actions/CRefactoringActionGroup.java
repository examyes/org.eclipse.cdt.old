/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn, Wind River Systems Inc. - ported for rename refactoring impl. 
 *******************************************************************************/

package org.eclipse.cdt.refactoring.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.cdt.refactoring.IPositionConsumer;

import org.eclipse.cdt.internal.refactoring.Messages;

/**
 * Action group that adds refactor actions (for example Rename..., Move..., etc)
 * to a context menu and the global menu bar.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class CRefactoringActionGroup extends ActionGroup implements IPositionConsumer {
    /**
     * Pop-up menu: id of the refactor sub menu (value <code>org.eclipse.cdt.ui.refactoring.menu</code>).
     * 
     * @since 2.1
     */
    public static final String MENU_ID = "org.eclipse.cdt.ui.refactoring.menu"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the reorg group of the refactor sub menu (value
     * <code>reorgGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_REORG = "reorgGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the type group of the refactor sub menu (value
     * <code>typeGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_TYPE = "typeGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the coding group of the refactor sub menu (value
     * <code>codingGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_CODING = "codingGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the undo/redo of the refactor sub menu (value
     * <code>undoRedoGroup</code>).
     * 
     * @since 2.1
     */
    private static final String GROUP_UNDO = "undoRedoGroup"; //$NON-NLS-1$

    private String fGroupName= IWorkbenchActionConstants.GROUP_REORGANIZE;
    private CRenameAction fRenameAction;

	private boolean fIsEditor;
	private IWorkbenchSite fSite;

    
    /**
     * Creates a new <code>RefactorActionGroup</code>. 
     * @deprecated
     */
    public CRefactoringActionGroup(IWorkbenchWindow ww, String groupName) {
        if (groupName != null && groupName.length() > 0) {
            fGroupName= groupName;
        }
        fRenameAction = new CRenameAction();
    }

    public CRefactoringActionGroup(IWorkbenchPart part) {
    	this(part, null);
    }
    
    public CRefactoringActionGroup(IWorkbenchPart part, String groupName) {
        if (groupName != null && groupName.length() > 0) {
            fGroupName= groupName;
        }
        fRenameAction = new CRenameAction();
        if (part instanceof ITextEditor) {
        	setEditor((ITextEditor) part);
        }
        else {
        	init(part.getSite());
        }
    }
    
    public CRefactoringActionGroup(Page part) {
    	fRenameAction= new CRenameAction();
    	init(part.getSite());
    }

    public void init(IWorkbenchSite site) {
        fRenameAction.setSite(site);
        fSite= site;
    }
    
	public void setEditor(ITextEditor textEditor) {
		fIsEditor= true;
        fRenameAction.setEditor(textEditor);
        textEditor.setAction("Rename", fRenameAction); //$NON-NLS-1$
        fillActionBars(textEditor.getEditorSite().getActionBars());
    }


    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        actionBars.setGlobalActionHandler("org.eclipse.cdt.ui.actions.Rename", fRenameAction); //$NON-NLS-1$
        actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), fRenameAction);
        actionBars.updateActionBars();
    }

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    public void fillContextMenu(IMenuManager menu) {
    	updateActionBars();
		if (fRenameAction.isEnabled()) {
			IMenuManager refactorSubmenu = new MenuManager(Messages.getString("CRefactoringActionGroup.RefactorMenu"), MENU_ID); //$NON-NLS-1$
			refactorSubmenu.add(new Separator(GROUP_REORG));
			refactorSubmenu.add(fRenameAction);
			refactorSubmenu.add(new Separator(GROUP_UNDO));
        
			menu.appendToGroup(fGroupName, refactorSubmenu);
		}
    }

	private boolean isApplicableFor(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o= ss.getFirstElement();
				if (o instanceof ICElement && o instanceof ISourceReference) {
					return !(o instanceof IInclude) && !(o instanceof ITranslationUnit);
				}
			}
		}
		return false;
	}

    public void updateActionBars() {
    	if (!fIsEditor) {
    		if (fSite != null) {
    			ISelection sel= fSite.getSelectionProvider().getSelection();
    			if (isApplicableFor(sel)) {
    				CRefactory.getInstance().providePosition(((IStructuredSelection) sel).getFirstElement(), fRenameAction);
    			}
    			else {
    				fRenameAction.setEnabled(false);
    			}
    		}
    		else {
    			fRenameAction.setEnabled(false);
    		}
    	}
    }

    public void setPosition(IFile file, int startPos, String text) {
        fRenameAction.setPosition(file, startPos, text);
    }
}
