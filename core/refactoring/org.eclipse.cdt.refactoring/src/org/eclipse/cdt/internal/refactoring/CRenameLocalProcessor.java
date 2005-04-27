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

import java.util.*;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.refactoring.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Rename processor, setting up input page for a local rename.
 */
public class CRenameLocalProcessor extends CRenameTextProcessor {
    private IScope fScope;
    public CRenameLocalProcessor(CRenameProcessor input, String kind, IScope scope) {
        super(input, kind);
        fScope= scope;
    }
    
    // overrider
    protected int getAcceptedLocations(int selectedOptions) {
        return CRefactory.OPTION_IN_CODE | selectedOptions;
    }
    // overrider
    protected int getAvailableOptions() {
        return 0;
    }
    
    // overrider
    protected int getOptionsForcingPreview() {
        return 0;
    }
    
    // overrider
    protected int getSearchScope() {
        return ICRefactoringSearch.SCOPE_FILE;
    }
    
    protected void analyzeTextMatches(ArrayList matches, IProgressMonitor monitor, 
            RefactoringStatus status) {
        if (fScope != null) {
            CRefactoringArgument argument = getArgument();
            ASTManager r = getAstManager();
            int[] result= new int[] {0, Integer.MAX_VALUE};
            IScope scope= argument.getScope();
            IASTNode node= null;
            try {
                node = scope.getPhysicalNode();
                if (argument.getBinding() instanceof IParameter) {
                    node= node.getParent();
                }
            } catch (DOMException e) {
                r.handleDOMException(argument.getTranslationUnit(), e, status);
            }
            if (node != null) {
                IASTFileLocation loc= ASTManager.getLocationInTranslationUnit(node);
                if (loc != null) {
                    result[0]= loc.getNodeOffset();
                    result[1]= result[0] + loc.getNodeLength();
                }
            }
            int[] range= result;
            for (Iterator iter = matches.iterator(); iter.hasNext();) {
                CRefactoringMatch m = (CRefactoringMatch) iter.next();
                int off= m.getOffset();
                if (off < range[0] || off > range[1]) {
                    iter.remove();
                }
            }
        }
        super.analyzeTextMatches(matches, monitor, status);
    }
}