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

package org.eclipse.cdt.internal.refactoring.ui;

import org.eclipse.cdt.internal.refactoring.CRenameRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Refactoring Wizard adding the input page.
 */
public class CRenameRefactoringWizard extends RefactoringWizard {

    public CRenameRefactoringWizard(CRenameRefactoring r) {
        super(r, DIALOG_BASED_USER_INTERFACE);
    }

    // overrider
    protected void addUserInputPages() {
        setDefaultPageTitle(getRefactoring().getName());
        CRenameRefactoringInputPage page= new CRenameRefactoringInputPage();
        addPage(page);
    }
}
