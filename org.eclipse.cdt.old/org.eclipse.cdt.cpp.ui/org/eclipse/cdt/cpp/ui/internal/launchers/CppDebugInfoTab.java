package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppProjectAttributes;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;

import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.dialogs.*;
//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.*;

import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.actions.*;

//  from java

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.ILabelProvider;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.jface.viewers.StructuredSelection;


/**
 * This tab appears in the LaunchConfigurationDialog for launch configurations that
 * require C/C++ specific launching information such as executable name, parameters, etc.
 */
public class CppDebugInfoTab extends CppLaunchConfigurationTab
 {
	

    // initial value stores
    private String initialProgramFieldValue;
    private String initialParametersFieldValue;

    // widgets
    private Text                             _programNameField;
//    private Text                             programParametersField;
    private   Combo                          programParametersField;
    protected Combo	                        sourceNameField;
    protected Combo		                     _workingDirectoryField;
    protected Button                         workingDirectoryBrowseButton;

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 300;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    protected ModelInterface	_api = ModelInterface.getInstance();
    protected ArrayList    _history;

    private  String        _programName;
    private  DataElement   _directory;
    private  String        _parameters;

    private static DataElement _executable;

				
	/**
	 * @see ILaunchConfigurationTab#createControl(TabItem)
	 */
    public void createControl(Composite parent)
    {
   	Composite composite = new Composite(parent, SWT.NULL);
   	
   	
   	composite.addHelpListener(new HelpListener()
	     {
   		 public void helpRequested(HelpEvent event)
   	    {
   		    performHelp();
       	 }
        }
      );
   	
   	composite.setLayout(new GridLayout());
   	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
   	
   	createProgramNameGroup(composite);
   	createProgramParametersGroup(composite);

   	createSpacer(composite);

   	createWorkingDirectoryGroup(composite);

   	programParametersField.setFocus();
   	
   	setControl(composite);
    }

    /**
     * Field for entering the name of the program to execute.
     * We populate it with the user's current selection in the navigator,
     * and make it read only.  Essentially forcing user to preselect its executable
     * in the navigator before invoking the debug action.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProgramNameGroup(Composite parent)
    {
   	// project specification group
   	Composite programGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 2;
   	programGroup.setLayout(layout);
   	programGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// new program label
   	Label programLabel = new Label(programGroup,SWT.NONE);
   	programLabel.setText(_plugin.getLocalizedString("debugLauncherMain.ProgramName"));
   	
   	// new program name entry field
   	_programNameField = new Text(programGroup, SWT.BORDER);
   	GridData data = new GridData(GridData.FILL_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
//      _programNameField.setText(_programName);
   	_programNameField.setLayoutData(data);

		_programNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
   	_programNameField.setEnabled(true);
   	
    }

    /**
     * Field for entering the parameters of the program to execute.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProgramParametersGroup(Composite parent)
    {
    	// project specification group
    	Composite parametersGroup = new Composite(parent,SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	parametersGroup.setLayout(layout);
    	parametersGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
    	
    	// new parameters label
    	Label parametersLabel = new Label(parametersGroup,SWT.NONE);
    	parametersLabel.setText(_plugin.getLocalizedString("debugLauncherMain.ProgramParameters"));
    	
    	// new parameters name entry field
    	programParametersField = new Combo(parametersGroup, SWT.BORDER);
    	//programParametersField.addListener(SWT.Modify, this);
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    	programParametersField.setLayoutData(data);

		programParametersField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
    	programParametersField.setEnabled(true);
     /*
   	// source name entry field
   	sourceNameField = new Combo(sourceContainerGroup,SWT.BORDER);
   	sourceNameField.addListener(SWT.Modify,this);
   	sourceNameField.addListener(SWT.Selection,this);
   	_history = _plugin.readProperty(CppProjectAttributes.LOCATION_LOCAL);
   	int size = _history.size();
   	for (int i = 0; i < size; i++)
   	    {
   		String item = (String)_history.get(size - i - 1);
   		if (item != null)
   		    {
   			sourceNameField.add(item, i);
   		    }
   		
   		if (i == 0)
   		    {	
   			sourceNameField.setText(item);
   		    }
   	    }

   	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
   	sourceNameField.setLayoutData(data);
    */


    	
    	if (initialParametersFieldValue != null)
    	    programParametersField.setText(initialParametersFieldValue);



    }

    /**
     * Field for entering the working directory from which to invoke the program to execute.
     * By default, we populate the field with the directory where the program is found, but the user
     * can change it by manualy entering the path, or using the "Browse" button to select it.
     *
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createWorkingDirectoryGroup(Composite parent)
    {
   	// working directory specification group
   	Composite workingDirectoryGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 3;
   	workingDirectoryGroup.setLayout(layout);
   	workingDirectoryGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// working directory name label
   	Label directoryLabel = new Label(workingDirectoryGroup,SWT.NONE);
   	directoryLabel.setText(_plugin.getLocalizedString("debugLauncherMain.WorkingDirectory"));

      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	//data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
   	directoryLabel.setLayoutData(data);


   	// Directory name entry field
   	_workingDirectoryField = new Combo(workingDirectoryGroup,SWT.BORDER);
   	//workingDirectoryField.addListener(SWT.Modify,this);
   	//workingDirectoryField.addListener(SWT.Selection,this);
   	//workingDirectoryField.setText(_directory.getSource());
   	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
   	data.horizontalSpan = 1;
   	_workingDirectoryField.setLayoutData(data);

		_workingDirectoryField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

   	// browse button
   	workingDirectoryBrowseButton = new Button(workingDirectoryGroup, SWT.PUSH);
   	workingDirectoryBrowseButton.setText(_plugin.getLocalizedString("debugLauncherMain.Browse"));

		workingDirectoryBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkingDirectoryBrowseButtonPressed();
			}
		});

   	workingDirectoryBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));	

   	
   	
    }
	
	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
   {
    /*
    		IJavaElement je = getContext();
		if (je == null) {
			initializeHardCodedDefaults(config);
		} else {
			initializeDefaults(je, config);
		}
		config.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, (String)null);
		config.setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, (String)null);
    */

     IProject project;
     System.out.println("CppDebugInfoTab:setDefaults");
     IStructuredSelection selection = getSelection();
     if(selection == null)
     {
        displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.noSelection"));
        return;
     }

     Object element = selection.getFirstElement();

     if (element instanceof DataElement)
     {	
   		_executable = (DataElement)element;
	   	if (!_executable.getType().equals("file"))
	      {
		   	_executable = null;
			   _directory = null;
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notExecutable"));
   			return;
	      }

         DataElement projectElement = _api.getProjectFor(_executable);
         project = _api.findProjectResource(projectElement);
         if (!project.isOpen())
         {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
            return;
         }
	   	_directory = _executable.getParent();
      }	
      else if (element instanceof IProject || element instanceof IResource)
      {
         project = ((IResource)element).getProject();
         if (!project.isOpen())
         {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
            return;
         }

		   _executable = _api.findResourceElement((IResource)element);
   		if (_executable == null)
	      {
            if (_plugin.isCppProject(project))
            {
      			IResource resource = (IResource)element;
	      		IResource parentRes = resource.getParent();
			
      			DataStore dataStore = _plugin.getCurrentDataStore();
	      		_directory = dataStore.createObject(null, "directory", parentRes.getName(),
 	  	   					    parentRes.getLocation().toString());

	   	   	_executable = dataStore.createObject(_directory, "file", resource.getName(),
				   			     resource.getLocation().toString());
            }
            else
            {
               displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notCppProject"));
               return;
            }

	      }
      	else
         {
    			_directory = _executable.getParent();
  	      }
   	}
	   else
   	{
         displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notExecutable"));
   		_executable = null;
	   	_directory = null;
   		return;
	   }
      System.out.println("CppDebugInfoTab:setDefaults _executable = " +_executable.getSource());
      //_programNameField.setText(_executable.getSource());
      //_workingDirectoryField.setText(_directory.getSource());

	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
    System.out.println("CppDebugInfoTab:initializeFrom");
		updateExecutableFromConfig(config);
	}

	protected void updateExecutableFromConfig(ILaunchConfiguration config) {
		String executableName = "";
		String workingDirectory = "";
		try
      {
			executableName = config.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
         if (executableName == "")
         {
            if (_executable != null)
              _programNameField.setText(_executable.getSource());
         }
         else
         {
      		_programNameField.setText(executableName);		
         }

			workingDirectory = config.getAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, "");
         if (workingDirectory == "")
         {
            if (_directory != null)
               _workingDirectoryField.setText(_directory.getSource());
         }
         else
         {
      		_workingDirectoryField.setText(workingDirectory);		
         }


		}
      catch (CoreException ce)
      {			
        displayMessageDialog(_plugin.getLocalizedString("CppDebugInfoTab.Exception") + ce.toString());
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config)
   {
      System.out.println("CppDebugInfoTab:performApply() ");
		//config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)fProjText.getText());
		//config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)fTestText.getText());
		config.setAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, (String)_programNameField.getText());
		config.setAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, (String)_workingDirectoryField.getText());
	}

	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
      System.out.println("CppDebugInfoTab:dispose() ");
	}

	/**
	 * Create some empty space
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}
	
	/**
	 * Show a dialog that lists all main types
	 */
		/*
	protected void handleSearchButtonSelected() {
		Shell shell = getShell();
		IWorkbenchWindow workbenchWindow = CppPlugin.getActiveWorkbenchWindow();
		IJavaProject javaProject = getJavaProject();
		
		SelectionDialog dialog = new TestSelectionDialog(shell, getLaunchConfigurationDialog(), javaProject);
		dialog.setTitle("Test Selection");
		dialog.setMessage("Choose a test case or test suite:");
		if (dialog.open() == dialog.CANCEL) {
			return;
		}
		
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length < 1)) {
			return;
		}		
		IType type = (IType)results[0];
		
		if (type != null) {
			fTestText.setText(type.getFullyQualifiedName());
			javaProject = type.getJavaProject();
			fProjText.setText(javaProject.getElementName());
		}
	}
      */
		
	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
//	protected void handleProjectButtonSelected() {
    /*
		IJavaProject project = chooseJavaProject();
		if (project == null) {
			return;
		}
		
		String projectName = project.getElementName();
		fProjText.setText(projectName);		
     */
//	}

    protected void handleWorkingDirectoryBrowseButtonPressed()
    {
      if (_directory != null)
      {
         DataElement directory = _directory.getDataStore().getHostRoot().get(0).dereference();
   		directory = directory.getParent();
	   	DataElementFileDialog dialog = new DataElementFileDialog("Select Directory", /*_directory*/directory, true);
   		dialog.open();
	   	if (dialog.getReturnCode() == dialog.OK)
   	   {
      		DataElement selected = dialog.getSelected();
	  	   	if (selected != null)
   	      {
      	      _workingDirectoryField.setText(selected.getSource());
  		      }
   	   }
      }
    }	

	
	/**
	 * Realize a Java Project selection dialog and return the first selected project,
	 * or null if there was none.
	 */
   /*
	protected IJavaProject chooseJavaProject() {
		IJavaProject[] projects;
		try {
			projects= JavaCore.create(getWorkspaceRoot()).getJavaProjects();
		} catch (JavaModelException e) {
			JUnitPlugin.log(e.getStatus());
			projects= new IJavaProject[0];
		}
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project Selection");
		dialog.setMessage("Choose a project to constrain the search for main types:");
		dialog.setElements(projects);
		
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == dialog.OK) {			
			return (IJavaProject) dialog.getFirstResult();
		}			
		return null;		
	}
   */
	
	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {

      System.out.println("CppDebugInfoTab:isValid()");

		setErrorMessage(null);
		setMessage(null);
		
    /*		
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage("Project does not exist.");
				return false;
			}
		}

		name = fTestText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage("Test not specified.");
			return false;
		}
    */
		// TO DO should verify that test exists
		return true;
	}
	
	/**
	 * Initialize default attribute values based on the
	 * given Java element.
	 */
/*
	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
//		initializeJavaProject(javaElement, config);
//		initializeTestTypeAndName(javaElement, config);
//		initializeHardCodedDefaults(config);
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "&DebugCpp";
	}

    /**
     * Creates a horizontal spacer line that fills the width of its container.
     */
    protected void createSpacer(Composite parent)
    {
   	//	Label spacer = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
   	Label spacer = new Label(parent, SWT.NONE);
   	GridData data = new GridData();
   	data.horizontalAlignment = GridData.FILL;
   	data.verticalAlignment = GridData.BEGINNING;
   	spacer.setLayoutData(data);
    }


    public void performHelp()
    {
       System.out.println("HELP");
    }

    /**
     *	Display an error dialog with the specified message.
     *
     *	@param message java.lang.String
     */
    protected void displayMessageDialog(String message)
    {
	     MessageDialog.openError(CppPlugin.getActiveWorkbenchWindow().getShell(),_plugin.getLocalizedString("loadLauncher.Error.Title"),message);
    }

	
}
