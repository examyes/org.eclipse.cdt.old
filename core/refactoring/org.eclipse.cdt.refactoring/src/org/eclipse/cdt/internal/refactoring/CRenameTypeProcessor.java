/*******************************************************************************
 * Copyright (c) 2005 Wind River Systems, Inc.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


/**
 * Handles conflicting bindings for types.
 */
public class CRenameTypeProcessor extends CRenameGlobalProcessor {

    public CRenameTypeProcessor(CRenameProcessor processor, String kind) {
        super(processor, kind);
    }
        
    protected void handleConflictingBindings(IBinding[] bindings, 
            Set equalBindings, HashSet conflictingBindings, RefactoringStatus status) {
        for (Iterator iter = conflictingBindings.iterator(); iter.hasNext();) {
            IBinding conflict = (IBinding) iter.next();
            if (conflict instanceof ICompositeType ||
                    conflict instanceof ICPPNamespace ||
                    conflict instanceof IEnumeration ||
                    conflict instanceof ITypedef) {
                String name= ASTManager.getFirstEnclosingName(conflict);
                status.addError(MessageFormat.format(
                        Messages.getString("CRenameTypeProcessor.error.conflictingDecl"), //$NON-NLS-1$
                        new Object[]{name}));
            }
        }
    }
}
