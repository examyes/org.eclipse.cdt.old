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

package org.eclipse.cdt.internal.refactoring;

import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.core.runtime.*;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.*;

/**
 * Abstract base for all different rename processors used by the top 
 * processor.
 */
public abstract class CRenameProcessorDelegate {
    private CRenameProcessor fTopProcessor;

    protected CRenameProcessorDelegate(CRenameProcessor topProcessor) {
        fTopProcessor= topProcessor;
    }
    
    abstract public String getProcessorName();
    abstract public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
        throws CoreException, OperationCanceledException;
    abstract public RefactoringStatus checkFinalConditions(IProgressMonitor pm, 
            CheckConditionsContext context) throws CoreException;
    abstract public Change createChange(IProgressMonitor pm) throws CoreException;

    final public CRefactoringArgument getArgument() {
        return fTopProcessor.getArgument();
    }
    final public String getReplacementText() {
        return fTopProcessor.getReplacementText();
    }
    final public int getSelectedScope() {
        return fTopProcessor.getScope();
    }
    final public int getSelectedOptions() {
        return fTopProcessor.getSelectedOptions();
    }
    final public String getSelectedWorkingSet() {
        return fTopProcessor.getWorkingSet();
    }
    final public CRefactory getManager() {
        return fTopProcessor.getManager();
    }
    final public ASTManager getAstManager() {
        return fTopProcessor.getAstManager();
    }

    /**
     * The options presented by the page in the refactoring wizard.
     */
    protected int getAvailableOptions() {
        return 0;
    }

    /**
     * The options each of which forces the preview, when selected.
     */
    protected int getOptionsForcingPreview() {
        return 0;
    }

    /**
     * The options that need the scope definition. When one of them is 
     * selected, the scope options are enabled.
     */
    protected int getOptionsEnablingScope() {
        return 0;
    }
}
