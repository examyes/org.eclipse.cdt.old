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

package org.eclipse.cdt.refactoring;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.*;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cdt.internal.refactoring.*;
import org.eclipse.cdt.internal.refactoring.ui.CRenameRefactoringWizard;
import org.eclipse.core.resources.*;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Serves to launch the various refactorings.
 */
public class CRefactory {
    public static final int OPTION_ASK_SCOPE 					= 0x01;
    public static final int OPTION_IN_COMMENT 					= 0x02;
    public static final int OPTION_IN_STRING_LITERAL 			= 0x04;
    public static final int OPTION_IN_INCLUDE_DIRECTIVE 		= 0x08;
    public static final int OPTION_IN_MACRO_DEFINITION 			= 0x10;
    public static final int OPTION_IN_PREPROCESSOR_DIRECTIVE 	= 0x20;
    public static final int OPTION_IN_INACTIVE_CODE 			= 0x40;
    public static final int OPTION_IN_CODE 						= 0x80;

    public static final int ARGUMENT_UNKNOWN				=  0;
    public static final int ARGUMENT_LOCAL_VAR				=  1;
    public static final int ARGUMENT_PARAMETER 				=  2;
    public static final int ARGUMENT_FILE_LOCAL_VAR 		=  3;
    public static final int ARGUMENT_GLOBAL_VAR 			=  4;
    public static final int ARGUMENT_FIELD					=  5;
    public static final int ARGUMENT_FILE_LOCAL_FUNCTION	=  6;
    public static final int ARGUMENT_GLOBAL_FUNCTION 		=  7;
    public static final int ARGUMENT_VIRTUAL_METHOD 		=  8;
    public static final int ARGUMENT_NON_VIRTUAL_METHOD 	=  9;
    public static final int ARGUMENT_TYPE 					= 10;
    public static final int ARGUMENT_MACRO 					= 11;
    public static final int ARGUMENT_INCLUDE_DIRECTIVE 		= 12;
    public static final int ARGUMENT_ENUMERATOR             = 13;
    public static final int ARGUMENT_CLASS_TYPE             = 14;
    public static final int ARGUMENT_NAMESPACE              = 15;
    
    private static CRefactory sInstance= new CRefactory();
    private HashSet fEditorIDs= new HashSet();
    private boolean fDisablePotentialMatches= false;
    private ICRefactoringSearch fTextSearch;
    private String[] fAffectedProjectNatures;
    private IParserConfigurationProvider[] fParserConfigurationProviders= new IParserConfigurationProvider[0];
    
    public static CRefactory getInstance() {
        return sInstance;
    }
    
    public static ICRefactoringArgument createArgument(ITextEditor editor, 
            ITextSelection selection) {
        return new CRefactoringArgument(editor, selection);
    }

    public static ICRefactoringArgument createArgument(IFile file, int offset, String text) {
        return new CRefactoringArgument(file, offset, text);
    }

    private CRefactory() {
        fEditorIDs.add("org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        fEditorIDs.add("com.windriver.ide.editor.cpp"); //$NON-NLS-1$
        fEditorIDs.add("com.windriver.ide.editor.c"); //$NON-NLS-1$
        
        fAffectedProjectNatures= new String[] {
                CProjectNature.C_NATURE_ID, 
                CCProjectNature.CC_NATURE_ID
        };
    }
    
    // runs the rename refactoring
    public void rename(Shell shell, ICRefactoringArgument arg) {
        if (!IDE.saveAllEditors(
                new IResource[] {ResourcesPlugin.getWorkspace().getRoot()},
                false)) {
            return;
        }
        CRefactoringArgument iarg= (CRefactoringArgument) arg;
        CRenameRefactoring r= new CRenameRefactoring(new CRenameProcessor(this, iarg));
		RefactoringWizardOpenOperation op= 
		    new RefactoringWizardOpenOperation(new CRenameRefactoringWizard(r));
		try {
            op.run(shell, Messages.getString("CRefactory.title.rename"));  //$NON-NLS-1$
        } catch (InterruptedException e) {
            // operation was cancelled
        }
    }

    public ICRefactoringSearch getTextSearch() {
        if (fTextSearch == null) {
            return new TextSearchWrapper();
        }
        return fTextSearch;
    }
    
    public void setTextSearch(ICRefactoringSearch txtSearch) {
        fTextSearch= txtSearch;
    }

    public String[] getCCppPatterns() {
        IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
        HashSet patterns= new HashSet();
        IFileEditorMapping[] mappings= registry.getFileEditorMappings();
        for (int i = 0; i < mappings.length; i++) {
            IFileEditorMapping mapping = mappings[i];
            IEditorDescriptor[] editors= mapping.getEditors();
            for (int j = 0; j < editors.length; j++) {
                IEditorDescriptor editor = editors[j];
                if (fEditorIDs.contains(editor.getId())) {
                    String name= mapping.getName();
                    String ext= mapping.getExtension();
                    if (ext.length() > 0) {
                        patterns.add(name+'.'+ext);
                    }
                    else {
                        patterns.add(name);
                    }
                    break;
                }
            }
        }
        return (String[]) patterns.toArray(new String[patterns.size()]);
    }

    public IASTTranslationUnit getTranslationUnit(IFile sourceFile, 
            RefactoringStatus status) {
        IParserConfiguration pcfg= null;
        ICodeReaderFactory codeReader= SavedCodeReaderFactory.getInstance();
        
        for (int i = 0; i < fParserConfigurationProviders.length; i++) {
            IParserConfigurationProvider pcp = fParserConfigurationProviders[i];
            pcfg= pcp.getParserConfiguration(sourceFile);
            if (pcfg != null) {
                break;
            }
        }
        
        try {
            return CDOM.getInstance().getTranslationUnit(sourceFile, codeReader, pcfg);
        } catch (UnsupportedDialectException e) {
            status.addError(e.getMessage());
        }
        return null;
    }

    public void addAffectedProjectNatures(String nature) {
        HashSet natures= new HashSet();
        natures.addAll(Arrays.asList(fAffectedProjectNatures));
        natures.add(nature);
        fAffectedProjectNatures= (String[]) natures.toArray(new String[natures.size()]);
    }
    
    public String[] getAffectedProjectNatures() {
        return fAffectedProjectNatures;
    }

    public void addEditorDefiningExtension(String editorID) {
        if (!fEditorIDs.contains(editorID)) {
            fEditorIDs.add(editorID);
        }
    }

    public void addParserConfigurationProvider(IParserConfigurationProvider pcp) {
        HashSet now= new HashSet();
        now.addAll(Arrays.asList(fParserConfigurationProviders));
        now.add(pcp);
        fParserConfigurationProviders= 
            (IParserConfigurationProvider[]) now.toArray(new String[now.size()]);
    }

    public void setDisablePotentialMatches(boolean val) {
        fDisablePotentialMatches= val;
    }
    
    public boolean getDisablePotentialMatches() {
        return fDisablePotentialMatches;
    }    
}
