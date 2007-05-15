/*******************************************************************************
 * Copyright (c) 2004, 2007 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 

package org.eclipse.cdt.internal.refactoring.ui;

import org.eclipse.cdt.internal.refactoring.*;
import org.eclipse.cdt.refactoring.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Input page added to the standard refactoring wizard.
 */
public class CRenameRefactoringInputPage extends UserInputWizardPage {

    public static final String PAGE_NAME = "RenameRefactoringPage"; //$NON-NLS-1$
    
    private static final String KEY_REFERENCES_INV = "references_inv"; //$NON-NLS-1$
    private static final String KEY_COMMENT = "comment"; //$NON-NLS-1$
    private static final String KEY_STRING = "string"; //$NON-NLS-1$
    private static final String KEY_INACTIVE = "inactive"; //$NON-NLS-1$
    private static final String KEY_SCOPE = "scope"; //$NON-NLS-1$
    private static final String KEY_WORKING_SET_NAME = "workingset"; //$NON-NLS-1$

    private static final String KEY_INCLUDE = "include"; //$NON-NLS-1$
    private static final String KEY_MACRO_DEFINITION = "macroDefinition"; //$NON-NLS-1$
    private static final String KEY_PREPROCESSOR = "preprocessor"; //$NON-NLS-1$

    private IDialogSettings fDialogSettings;
    private String fSearchString;
    private int fOptions;
    private int fForcePreviewOptions= 0;
    private int fEnableScopeOptions;

    private Text fNewName;
    private Button fWorkspace;
    private Button fDependent;
    private Button fInComment;
    private Button fInString;
    private Button fInInclude;
    private Button fInInactiveCode;
    private Button fReferences;
    private Button fSingle;
    private Button fWorkingSet;
    private Text fWorkingSetSpec;
    private Button fWorkingSetButton;
    private Button fInMacro;
    private Button fInPreprocessor;

    public CRenameRefactoringInputPage() {
        super(PAGE_NAME);
        String key= "CRenameRefactoringInputPage"; //$NON-NLS-1$
        IDialogSettings ds= CRefactoringPlugin.getDefault().getDialogSettings();
        fDialogSettings= ds.getSection(key);
        if (fDialogSettings == null) {
            fDialogSettings= ds.addNewSection(key);
        }
    }
 
    private boolean hasOption(int options) {
        return (fOptions & options) == options;
    }

    // overrider
    public void createControl(Composite parent) {
        CRenameProcessor processor= getRenameProcessor();
        fSearchString= processor.getArgument().getName();
        fOptions= processor.getAvailableOptions();
        fForcePreviewOptions= processor.getOptionsForcingPreview();
        fEnableScopeOptions= processor.getOptionsEnablingScope();
        
        Composite top= new Composite(parent, SWT.NONE);
        initializeDialogUnits(top);
        setControl(top);

        top.setLayout(new GridLayout(2, false));

        // new name
        Composite group= top;
        GridData gd;

        Label l= new Label(group, SWT.NONE);
        l.setText(Messages.getString("CRenameRefactoringInputPage.newIdentifier.label")); //$NON-NLS-1$
        fNewName= new Text(group, SWT.BORDER);
        fNewName.setText(fSearchString);
        fNewName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));        
        fNewName.selectAll();

        boolean skippedLine= false;
        // specify the scope
        if (hasOption(CRefactory.OPTION_ASK_SCOPE)) {          
            skipLine(top);
            new Label(top, SWT.NONE).setText(Messages.getString("CRenameRefactoringInputPage.label.scope")); //$NON-NLS-1$
            skippedLine= true;
            
            group= new Composite(top, SWT.NONE);
            group.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
            gd.horizontalSpan= 2;
            group.setLayout(new GridLayout(3, false));

            fWorkspace= new Button(group, SWT.RADIO);
            fWorkspace.setText(Messages.getString("CRenameRefactoringInputPage.button.scope.workspace")); //$NON-NLS-1$
            fWorkspace.setLayoutData(gd= new GridData());
            gd.horizontalSpan= 3;
            
            fDependent= new Button(group, SWT.RADIO);
            fDependent.setText(Messages.getString("CRenameRefactoringInputPage.button.scope.releatedprojects"));         //$NON-NLS-1$
            fDependent.setLayoutData(gd= new GridData());
            gd.horizontalSpan= 3;

            fSingle= new Button(group, SWT.RADIO);
            fSingle.setText(Messages.getString("CRenameRefactoringInputPage.button.singleProject"));       //$NON-NLS-1$
            fSingle.setLayoutData(gd= new GridData());
            gd.horizontalSpan= 3;

            fWorkingSet= new Button(group, SWT.RADIO);
            fWorkingSet.setText(Messages.getString("CRenameRefactoringInputPage.button.workingSet")); //$NON-NLS-1$

            fWorkingSetSpec= new Text(group, SWT.BORDER|SWT.READ_ONLY);
            fWorkingSetSpec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fWorkingSetButton= new Button(group, SWT.PUSH);
            fWorkingSetButton.setText(Messages.getString("CRenameRefactoringInputPage.button.chooseWorkingSet")); //$NON-NLS-1$
            setButtonLayoutData(fWorkingSetButton);
        }

        group= null;
        if (hasOption(CRefactory.OPTION_IN_CODE)) {
            group= createLabelAndGroup(group, skippedLine, top);
        	fReferences= new Button(group, SWT.CHECK);
        	fReferences.setText(Messages.getString("CRenameRefactoringInputPage.update.label")); //$NON-NLS-1$
        }
    	if (hasOption(CRefactory.OPTION_IN_INACTIVE_CODE)) {
            group= createLabelAndGroup(group, skippedLine, top);
    	    fInInactiveCode= new Button(group, SWT.CHECK);
    	    fInInactiveCode.setText(Messages.getString("CRenameRefactoringInputPage.button.inactiveCode")); //$NON-NLS-1$
    	}
        if (hasOption(CRefactory.OPTION_IN_COMMENT)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInComment= new Button(group, SWT.CHECK);
            fInComment.setText(Messages.getString("CRenameRefactoringInputPage.button.comments")); //$NON-NLS-1$
        }
        if (hasOption(CRefactory.OPTION_IN_STRING_LITERAL)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInString= new Button(group, SWT.CHECK);
            fInString.setText(Messages.getString("CRenameRefactoringInputPage.button.strings")); //$NON-NLS-1$
        }
        
        if (hasOption(CRefactory.OPTION_IN_MACRO_DEFINITION)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInMacro= new Button(group, SWT.CHECK);
            fInMacro.setText(Messages.getString("CRenameRefactoringInputPage.button.macrodefinitions")); //$NON-NLS-1$
        }
        if (hasOption(CRefactory.OPTION_IN_INCLUDE_DIRECTIVE)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInInclude= new Button(group, SWT.CHECK);
            fInInclude.setText(Messages.getString("CRenameRefactoringInputPage.button.includes")); //$NON-NLS-1$
        }
        if (hasOption(CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInPreprocessor= new Button(group, SWT.CHECK);
            fInPreprocessor.setText(Messages.getString("CRenameRefactoringInputPage.button.preprocessor")); //$NON-NLS-1$
        }
        Dialog.applyDialogFont(top);
        hookSelectionListeners();
        readPreferences();
        onSelectOption();	// transfers the option to the refactoring/ enablement
        updatePageComplete();
    }

    private Composite createLabelAndGroup(Composite group, boolean skippedLine, 
            Composite top) {
        if (group != null) {
            return group;
        }
        if (!skippedLine) {
            skipLine(top);
        }
        GridData gd;
        new Label(top, SWT.NONE).setText(Messages.getString("CRenameRefactoringInputPage.label.updateWithin")); //$NON-NLS-1$
        group= new Composite(top, SWT.NONE);
        group.setLayoutData(gd= new GridData());
        gd.horizontalSpan= 2;
        group.setLayout(new GridLayout(1, true));
        return group;
    }

    private void skipLine(Composite top) {
        new Label(top, SWT.NONE);
        new Label(top, SWT.NONE);
    }

    private void hookSelectionListeners() {
        fNewName.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }
            public void keyReleased(KeyEvent e) {
                onKeyReleaseInNameField();
            }
        });

        registerScopeListener(fWorkspace, ICRefactoringSearch.SCOPE_WORKSPACE);
        registerScopeListener(fDependent, ICRefactoringSearch.SCOPE_RELATED_PROJECTS);
        registerScopeListener(fSingle, ICRefactoringSearch.SCOPE_SINGLE_PROJECT);
        registerScopeListener(fWorkingSet, ICRefactoringSearch.SCOPE_WORKING_SET);
            
        if (fWorkingSetButton != null) {    
            fWorkingSetButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    onSelectWorkingSet();
                }
            });
        }
        SelectionListener listenOption= new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onSelectOption();
            }
        };
        registerOptionListener(fReferences, listenOption);
        registerOptionListener(fInComment, listenOption);
        registerOptionListener(fInInactiveCode, listenOption);
        registerOptionListener(fInInclude, listenOption);
        registerOptionListener(fInMacro, listenOption);
        registerOptionListener(fInString, listenOption);
        registerOptionListener(fInPreprocessor, listenOption);
    }

    private void registerScopeListener(Button button, final int scope) {
        if (button != null) {
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    onSelectedScope(scope);
                }
            });
        }
    }

    private void registerOptionListener(Button button, SelectionListener listenOption) {
        if (button != null) {
            button.addSelectionListener(listenOption);
        }
    }

    protected void onSelectedScope(int scope) {
        getRenameProcessor().setScope(scope);
        updateEnablement();
    }

    private void onSelectOption() {
        int selectedOptions= computeSelectedOptions();
        boolean forcePreview= fForcePreviewOptions==-1 ||
       			(selectedOptions & fForcePreviewOptions) != 0;
        getRenameProcessor().setSelectedOptions(selectedOptions);
        getRefactoringWizard().setForcePreviewReview(forcePreview);
        updateEnablement();
    }            

    protected void onKeyReleaseInNameField() {
        getRenameProcessor().setReplacementText(fNewName.getText());
        updatePageComplete();
    }

    // overrider
    public void dispose() {
        storePreferences();
        super.dispose();
    }
    
    private void readPreferences() {
        CRenameProcessor processor= getRenameProcessor();
        
        if (fWorkspace != null) {
            int choice;
            try {
                choice= fDialogSettings.getInt(KEY_SCOPE);
            }
            catch (Exception e) {
                choice= ICRefactoringSearch.SCOPE_RELATED_PROJECTS;
            }
            
            switch(choice) {
            case ICRefactoringSearch.SCOPE_WORKSPACE:
                fWorkspace.setSelection(true);
                break;
            case ICRefactoringSearch.SCOPE_SINGLE_PROJECT:
                fSingle.setSelection(true);
                break;
            case ICRefactoringSearch.SCOPE_WORKING_SET:
                fWorkingSet.setSelection(true);
                break;
            default:
                choice= ICRefactoringSearch.SCOPE_RELATED_PROJECTS;
                fDependent.setSelection(true);
                break;
            }
            processor.setScope(choice);
       
            String workingSet= checkWorkingSet(fDialogSettings.get(KEY_WORKING_SET_NAME));
            fWorkingSetSpec.setText(workingSet);
            processor.setWorkingSet(workingSet);
        }
        
        if (fReferences != null) {
            boolean val= !fDialogSettings.getBoolean(KEY_REFERENCES_INV);
            fReferences.setSelection(val);
        }
        initOption(fInComment, KEY_COMMENT);
        initOption(fInString, KEY_STRING);
        initOption(fInInclude, KEY_INCLUDE);
        initOption(fInMacro, KEY_MACRO_DEFINITION);
        initOption(fInPreprocessor, KEY_PREPROCESSOR);
        initOption(fInInactiveCode, KEY_INACTIVE);
    }

    private int computeSelectedOptions() {
        int options= 0;
        options |= computeOption(fReferences, CRefactory.OPTION_IN_CODE);
        options |= computeOption(fInComment, CRefactory.OPTION_IN_COMMENT);
        options |= computeOption(fInString, CRefactory.OPTION_IN_STRING_LITERAL);
        options |= computeOption(fInInclude, CRefactory.OPTION_IN_INCLUDE_DIRECTIVE);
        options |= computeOption(fInPreprocessor, CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE);
        options |= computeOption(fInMacro, CRefactory.OPTION_IN_MACRO_DEFINITION);
        options |= computeOption(fInInactiveCode, CRefactory.OPTION_IN_INACTIVE_CODE);
        return options;
    }

    private int computeOption(Button button, int option) {
        if (button != null && button.getSelection()) {
            return option;
        }
        return 0;
    }

    private void initOption(Button button, String key) {
        boolean val= false;
        if (button != null) {
            val= fDialogSettings.getBoolean(key);
            button.setSelection(val);
        }
    }

    private String checkWorkingSet(String ws) {
		if (ws != null && ws.length() > 0) {
		    IWorkingSetManager wsManager= PlatformUI.getWorkbench().getWorkingSetManager();
		    if (wsManager.getWorkingSet(ws)!=null) {
		        return ws;
		    }
		}
	    return ""; //$NON-NLS-1$
    }

    private void storePreferences() {
        if (fWorkspace != null) {
            int choice= ICRefactoringSearch.SCOPE_RELATED_PROJECTS;
            if (fWorkspace.getSelection()) {
                choice= ICRefactoringSearch.SCOPE_WORKSPACE;
            }
            else if (fSingle.getSelection()) {
                choice= ICRefactoringSearch.SCOPE_SINGLE_PROJECT;
            }
            else if (fWorkingSet.getSelection()) {
                choice= ICRefactoringSearch.SCOPE_WORKING_SET;	
            }
            fDialogSettings.put(KEY_SCOPE, choice);
            fDialogSettings.put(KEY_WORKING_SET_NAME, fWorkingSetSpec.getText());
        }
        if (fReferences != null) {
            fDialogSettings.put(KEY_REFERENCES_INV, !fReferences.getSelection());
        }
        if (fInComment != null) {
            fDialogSettings.put(KEY_COMMENT, fInComment.getSelection());
        }
        if (fInString != null) {
            fDialogSettings.put(KEY_STRING, fInString.getSelection());
        }
        if (fInInclude != null) {
            fDialogSettings.put(KEY_INCLUDE, fInInclude.getSelection());
        }
        if (fInPreprocessor != null) {
            fDialogSettings.put(KEY_PREPROCESSOR, fInPreprocessor.getSelection());
        }
        if (fInMacro != null) {
            fDialogSettings.put(KEY_MACRO_DEFINITION, fInMacro.getSelection());
        }
        if (fInInactiveCode != null) {
            fDialogSettings.put(KEY_INACTIVE, fInInactiveCode.getSelection());
        }
    }

    protected void onSelectWorkingSet() {
		CRenameProcessor processor= getRenameProcessor();
        String wsName= checkWorkingSet(fWorkingSetSpec.getText());
		IWorkingSetManager wsManager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dlg= 
		    wsManager.createWorkingSetSelectionDialog(getShell(), false);
		IWorkingSet currentWorkingSet= wsManager.getWorkingSet(wsName);
		if (currentWorkingSet != null) {
			dlg.setSelection(new IWorkingSet[] {currentWorkingSet});
		}
		IWorkingSet ws= null;
		if (dlg.open() == Window.OK) {
			IWorkingSet wsa[]= dlg.getSelection();
			if (wsa != null && wsa.length > 0) {
				ws= wsa[0];
			}
			if (ws != null) {
			    fWorkspace.setSelection(false);
			    fDependent.setSelection(false);
			    fSingle.setSelection(false);
			    fWorkingSet.setSelection(true);
			    processor.setScope(ICRefactoringSearch.SCOPE_WORKING_SET);
			    wsName= ws.getName();
			}
		}
	    
		fWorkingSetSpec.setText(wsName);
	    processor.setWorkingSet(wsName);
	    updateEnablement();
    }

    protected void updatePageComplete() {
        String txt= fNewName.getText();
        setPageComplete(txt.length() > 0 && !txt.equals(fSearchString));
    }

    protected void updateEnablement() {
        boolean enable= fEnableScopeOptions==-1 ||
        	(computeSelectedOptions() & fEnableScopeOptions) != 0;
        
        if (fWorkspace != null) {
            fWorkspace.setEnabled(enable);
            fDependent.setEnabled(enable);
            fSingle.setEnabled(enable);

            boolean enableSpec= false;
            fWorkingSet.setEnabled(enable);
            if (enable && fWorkingSet.getSelection()) {
                enableSpec= true;
            }
            fWorkingSetSpec.setEnabled(enableSpec);
            fWorkingSetButton.setEnabled(enable);
        }
    }
    
    private CRenameProcessor getRenameProcessor() {
        return (CRenameProcessor) ((CRenameRefactoring) getRefactoring()).getProcessor();
    }
}
