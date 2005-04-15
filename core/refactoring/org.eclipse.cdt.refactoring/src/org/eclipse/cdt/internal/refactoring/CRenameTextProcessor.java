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
import org.eclipse.cdt.refactoring.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.*;

/**
 * Performs text search, filtering by options and finally creates the change object.
 */
public class CRenameTextProcessor extends CRenameProcessorDelegate {

    private ArrayList fMatches= null;
    private String fKind;

    public CRenameTextProcessor(CRenameProcessor processor, String kind) {
        super(processor);
        fKind= kind;
    }
    
    // overrider
    public String getProcessorName() {
        String identifier= getArgument().getName();
        if (identifier != null) {
            return MessageFormat.format(
                    Messages.getString("CRenameTextProcessor.wizard.title"),  //$NON-NLS-1$
                    new Object[] {fKind, identifier});
        }
        return null;
    }

    
    // overrider
    protected int getAvailableOptions() {
        return CRefactory.OPTION_ASK_SCOPE | 
        	CRefactory.OPTION_IN_CODE |
        	CRefactory.OPTION_IN_COMMENT | 
        	CRefactory.OPTION_IN_MACRO_DEFINITION |
        	CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE |
        	CRefactory.OPTION_IN_STRING_LITERAL;
    }

    protected int getOptionsForcingPreview() {
    	return CRefactory.OPTION_IN_CODE |
	    	CRefactory.OPTION_IN_COMMENT | 
	    	CRefactory.OPTION_IN_MACRO_DEFINITION |
	    	CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE |
	    	CRefactory.OPTION_IN_STRING_LITERAL;        
    }
    
    protected int getOptionsEnablingScope() {
        return getOptionsForcingPreview();
    }
    
    protected int getSearchScope() {
        return getSelectedScope();
    }
    
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return new RefactoringStatus();
    }

    // overrider
    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, 
            CheckConditionsContext context) throws CoreException, OperationCanceledException {
	    RefactoringStatus result= new RefactoringStatus();
	    monitor.beginTask(Messages.getString("CRenameTextProcessor.task.checkFinalCondition"), 2); //$NON-NLS-1$
	    IFile file= getArgument().getSourceFile();
	    //assert file!=null;

        // perform text-search
	    fMatches= new ArrayList();
	    ICRefactoringSearch txtSearch= getManager().getTextSearch();
        IStatus stat= txtSearch.searchWord(getSearchScope(), file, getSelectedWorkingSet(), 
                getManager().getCCppPatterns(), getArgument().getName(), 
                new SubProgressMonitor(monitor, 1), fMatches);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        result.merge(RefactoringStatus.create(stat));
        if (result.hasFatalError()) {
            return result;
        }
        selectMatchesByLocation(fMatches);        
        analyzeTextMatches(fMatches, new SubProgressMonitor(monitor, 1), result);
        if (result.hasFatalError()) {
            return result;
        }
        
        HashSet fileset= new HashSet();
        for (Iterator iter = fMatches.iterator(); iter.hasNext();) {
            CRefactoringMatch tm = (CRefactoringMatch) iter.next();
            if (tm.getAstInformation() == CRefactoringMatch.AST_REFERENCE_OTHER) {
                iter.remove();
            }
            else {
                fileset.add(tm.getFile());
            }
        }
        IFile[] files= (IFile[]) fileset.toArray(new IFile[fileset.size()]);
        if (context != null) {
            ValidateEditChecker editChecker= 
                (ValidateEditChecker) context.getChecker(ValidateEditChecker.class);
            editChecker.addFiles(files);
        }
        monitor.done();
        return result;
    }

    protected void analyzeTextMatches(ArrayList matches, IProgressMonitor monitor, 
            RefactoringStatus status) {
        CRefactoringArgument argument= getArgument();
        IBinding[] renameBindings= getBindingsToBeRenamed(status);
        if (renameBindings != null && renameBindings.length > 0 && 
                argument.getArgumentKind() != CRefactory.ARGUMENT_UNKNOWN) {
            ASTManager mngr= getAstManager();
            mngr.setValidBindings(renameBindings);
            mngr.setRenameTo(getReplacementText());
            mngr.analyzeTextMatches(matches, monitor, status);
        }
    }
    
    private void selectMatchesByLocation(ArrayList matches) {
        int acceptTextLocation= getAcceptedLocations(getSelectedOptions());
        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = (CRefactoringMatch) iter.next();
            int location= match.getLocation();
            if (location != 0 && ((location & acceptTextLocation) == 0)) {
                iter.remove();
            }
        }
    }

    protected int getAcceptedLocations(int selectedOptions) {
        return selectedOptions;
    }

    // overrider
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (fMatches.size() == 0) {
            return null;
        }
        pm.beginTask(Messages.getString("CRenameTextProcessor.task.createChange"), fMatches.size()); //$NON-NLS-1$
        final String identifier= getArgument().getName();
        final String replacement= getReplacementText();
        CompositeChange change= new CompositeChange(getProcessorName()); 
        IFile file= null;
        TextFileChange fileChange= null;
        MultiTextEdit fileEdit= null;
        for (Iterator iter = fMatches.iterator(); iter.hasNext();) {
            CRefactoringMatch match= (CRefactoringMatch) iter.next();
            switch(match.getAstInformation()) {
            case CRefactoringMatch.AST_REFERENCE_OTHER:
                continue;
            case CRefactoringMatch.IN_COMMENT:
            case CRefactoringMatch.POTENTIAL:
                if (getManager().getDisablePotentialMatches()) {
                    continue;
                }
                break;
            case CRefactoringMatch.AST_REFERENCE:
                break;
            }
            if (match.getAstInformation() != CRefactoringMatch.AST_REFERENCE_OTHER) {
                IFile mfile= match.getFile();
                if (file==null || !file.equals(mfile)) {
                    file= mfile;
                    fileChange= new TextFileChange(file.getName(), file); 
                    change.add(fileChange);
                    fileEdit= new MultiTextEdit();
                    fileChange.setEdit(fileEdit);
                }
                
                ReplaceEdit replaceEdit= new ReplaceEdit(match.getOffset(), 
                        identifier.length(), replacement);
                fileEdit.addChild(replaceEdit);
                fileChange.addTextEditGroup(new TextEditGroup(match.getLabel(), replaceEdit));
            }
            pm.worked(1);
        }
        return change;
    }

    /**
     * Returns the array of bindings that must be renamed
     */
    protected IBinding[] getBindingsToBeRenamed(RefactoringStatus status) {
        return new IBinding[] {getArgument().getBinding()};
    }
}
