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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


/**
 * Processor adding constructor and destructor to the bindings to be renamed.
 */
public class CRenameClassProcessor extends CRenameTypeProcessor {

    public CRenameClassProcessor(CRenameProcessor processor, String kind) {
        super(processor, kind);
    }
    
    protected IBinding[] getBindingsToBeRenamed(RefactoringStatus status) {
        CRefactoringArgument argument= getArgument();
        IBinding binding= argument.getBinding();
        ArrayList bindings= new ArrayList();
        if (binding != null) {
            bindings.add(binding);
        }
        if (binding instanceof ICPPClassType) {
            ICPPClassType ctype= (ICPPClassType) binding;
            try {
                ICPPConstructor[] ctors= ctype.getConstructors();
                if (ctors != null) {
                    bindings.addAll(Arrays.asList(ctors));
                }
                
                IScope scope= ctype.getCompositeScope();
                if (scope != null) {
                    IBinding[] dtors= scope.find("~" + argument.getName()); //$NON-NLS-1$
                    if (dtors != null) {
                        bindings.addAll(Arrays.asList(dtors));
                    }
                }
            } catch (DOMException e) {
                getAstManager().handleDOMException(argument.getTranslationUnit(), e, status);
            }
        }
        return (IBinding[]) bindings.toArray(new IBinding[bindings.size()]);
    }
}