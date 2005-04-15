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

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.core.runtime.*;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

/**
 * Rename processor for methods.
 */
public class CRenameMethodProcessor extends CRenameGlobalProcessor {

    public CRenameMethodProcessor(CRenameProcessor processor, String kind) {
        super(processor, kind);
    }

    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
            throws OperationCanceledException, CoreException {
        CRefactoringArgument argument= getArgument();
        IBinding binding= argument.getBinding();
        if (binding instanceof ICPPConstructor) {
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameConstructor")); //$NON-NLS-1$
        }
        String identifier= argument.getName();
        if (identifier.startsWith("~")) { //$NON-NLS-1$
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameDestructor")); //$NON-NLS-1$
        }
        if (identifier.startsWith("operator") && //$NON-NLS-1$
                identifier.length() > 8 && 
                !CRefactoringUtils.isIdentifierChar(identifier.charAt(8))) { 
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameOperator")); //$NON-NLS-1$
        }
        return super.checkInitialConditions(pm);
    }
    
    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, 
            CheckConditionsContext context) throws OperationCanceledException, CoreException {
        CRefactoringArgument argument= getArgument();
        RefactoringStatus result= new RefactoringStatus();
        IScope scope= argument.getScope();
        if (scope != null) {
            IASTNode node= null;
            try {
                node = scope.getPhysicalNode();
            } catch (DOMException e) {
                getAstManager().handleDOMException(argument.getTranslationUnit(), e, result);
            }
            if (node instanceof IASTCompositeTypeSpecifier) {
                IASTCompositeTypeSpecifier se= (IASTCompositeTypeSpecifier) node;
                IASTName name= ASTManager.getSimpleName(se.getName());
                if (getReplacementText().equals(name.toString())) {
                    return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameToConstructor")); //$NON-NLS-1$
                }
                if (getReplacementText().startsWith("~")) { //$NON-NLS-1$
                    return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameToDestructor")); //$NON-NLS-1$);
                }
                if (!CRefactoringUtils.checkIdentifier(getReplacementText())) {
                    result.merge(RefactoringStatus.createErrorStatus(Messages.getString("CRenameMethodProcessor.warning.illegalCharacters"))); //$NON-NLS-1$
                }
            }                
        }
        if (argument.getArgumentKind() == CRefactory.ARGUMENT_VIRTUAL_METHOD) {
            result.merge(RefactoringStatus.createWarningStatus(Messages.getString("CRenameMethodProcessor.warning.renameVirtual"))); //$NON-NLS-1$
        }

        result.merge(super.checkFinalConditions(monitor, context));
        return result;
    }
}
