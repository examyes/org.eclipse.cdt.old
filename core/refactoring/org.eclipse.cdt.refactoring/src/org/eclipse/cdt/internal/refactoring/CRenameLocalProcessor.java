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

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
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
            int[] range= getAstManager().findRangeOfScope(getArgument(), status);
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
    
    protected void handleConflictingBindings(IBinding[] bindings, 
            Set equalBindings, HashSet conflictingBindings, RefactoringStatus status) {
        Collection[] cflc= 
            new Collection[] {new HashSet(), new ArrayList(), new ArrayList()};

        classifyConflictingBindings(equalBindings, conflictingBindings,
                (Set) cflc[0], cflc[1], cflc[2]);

        String errs[]= {Messages.getString("CRenameLocalProcessor.error.shadow"), Messages.getString("CRenameLocalProcessor.error.redeclare"), Messages.getString("CRenameLocalProcessor.error.isShadowed")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        int kind= getArgument().getArgumentKind();
        switch (kind) {
        case CRefactory.ARGUMENT_LOCAL_VAR:
        case CRefactory.ARGUMENT_PARAMETER:
            for (int i = 0; i < 3; i++) {
                Collection coll= cflc[i];
                for (Iterator iter = coll.iterator(); iter.hasNext();) {
                    String msg= errs[i];
                    IBinding conflict = (IBinding) iter.next();
                    String what= null;
                    if (conflict instanceof IEnumerator) {
                        what= Messages.getString("CRenameLocalProcessor.enumerator"); //$NON-NLS-1$
                    }
                    else if (conflict instanceof ICPPField) {
                        what= Messages.getString("CRenameLocalProcessor.field"); //$NON-NLS-1$
                    }
                    else if (conflict instanceof ICPPConstructor) {
                        what= Messages.getString("CRenameLocalProcessor.constructor"); //$NON-NLS-1$
                    }
                    else if (conflict instanceof ICPPMethod) {
                        what= Messages.getString("CRenameLocalProcessor.method"); //$NON-NLS-1$
                    }
                    else if (conflict instanceof IParameter) {
                        if (i==1 && kind==CRefactory.ARGUMENT_LOCAL_VAR) {
                            msg= errs[0];
                        }
                        what= Messages.getString("CRenameLocalProcessor.parameter"); //$NON-NLS-1$
                    }                    
                    else if (conflict instanceof IVariable) {
                        what= Messages.getString("CRenameLocalProcessor.globalVariable"); //$NON-NLS-1$
                        if (ASTManager.isLocalVariable((IVariable) conflict)) {
                            if (i==1 && kind==CRefactory.ARGUMENT_PARAMETER) {
                                msg= errs[2];
                            }
                            what= Messages.getString("CRenameLocalProcessor.localVariable"); //$NON-NLS-1$
                        }
                    }
                    
                    if (what != null) {
                        status.addError(MessageFormat.format(
                                Messages.getString("CRenameLocalProcessor.error.nameErrorWhat"), //$NON-NLS-1$
                                new Object[]{conflict.getName(), msg, what}));
                    }
                }
            }
            return;
        }
        super.handleConflictingBindings(bindings, equalBindings, conflictingBindings, status);
    }

    private void classifyConflictingBindings(Set equalBindings, 
            HashSet conflictingBindings, Set shadows, Collection redecl, Collection barriers) {
        // collect all scopes of the argument renamed.
        String name= getArgument().getName();
        String newName= getReplacementText();
        HashSet scopesOfArg= new HashSet();
        for (Iterator iter = equalBindings.iterator(); iter.hasNext();) {
            IBinding binding = (IBinding) iter.next();
            if (binding.getName().equals(name)) {
                IScope scope= null;
                try {
                    scope = binding.getScope();
                } catch (DOMException e) {
                }
                if (scope != null) {
                    scopesOfArg.add(scope);
                }
            }
        }
        // collect all shadows
        for (Iterator iter = scopesOfArg.iterator(); iter.hasNext();) {
            IScope scope = (IScope) iter.next();
            IBinding[] shadowedBindings= null;
            try {
                shadowedBindings = ASTManager.workaround_find(scope, newName);
            } catch (DOMException e) {
            }
            if (shadowedBindings != null) {
                shadows.addAll(Arrays.asList(shadowedBindings));
            }
        }
        // classify conflicting bindings
        for (Iterator iter = conflictingBindings.iterator(); iter.hasNext();) {
            IBinding binding= (IBinding) iter.next();
            if (binding != null && shadows.contains(binding)) {
                IScope scope= null;
                try {
                    scope = binding.getScope();
                } catch (DOMException e) {
                }
                if (scope != null && scopesOfArg.contains(scope)) {
                    redecl.add(binding);
                    shadows.remove(binding);
                }
            }
            else {
                barriers.add(binding);
            }
        }
    }
}
