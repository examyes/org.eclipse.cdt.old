package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.ArrayList;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStore;
import org.eclipse.cdt.dstore.hosts.dialogs.DataElementFileDialog;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


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
    private   Text                           _parametersField;
    protected Text		                     _workingDirectoryField;

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 300;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    protected ModelInterface	_api = ModelInterface.getInstance();
    protected ArrayList    _history;

    private  String        _parameters;
				
	/**
	 * @see ILaunchConfigurationTab#createControl(TabItem)
	 */
    public void createControl(Composite parent)
    {
   	Composite composite = new Composite(parent, SWT.NULL);
   	   	   	   	
   	composite.setLayout(new GridLayout());
   	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
   	
   	createSpacer(composite);
   	createProgramNameGroup(composite);

   	createSpacer(composite);
   	createProgramParametersGroup(composite);

   	createSpacer(composite);
   	createWorkingDirectoryGroup(composite);

   	_parametersField.setFocus();
   	
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
   	// new program label
   	Label programLabel = new Label(parent,SWT.NONE);
   	programLabel.setText(_plugin.getLocalizedString("debugLauncherMain.ProgramName"));
      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
   	programLabel.setLayoutData(data);
   	
   	// project specification group
   	Composite programGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 2;
   	programGroup.setLayout(layout);
   	programGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// new program name entry field
   	_programNameField = new Text(programGroup, SWT.BORDER);
   	data = new GridData(GridData.FILL_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
   	_programNameField.setLayoutData(data);

		_programNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
   	_programNameField.setEnabled(true);
   	
   	// browse button
   	Button programBrowseButton = new Button(programGroup, SWT.PUSH);
   	programBrowseButton.setText(_plugin.getLocalizedString("debugLauncherMain.Browse"));

		programBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProgramBrowseButtonPressed();
			}
		});

   	programBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));	

    }

    /**
     * Field for entering the parameters of the program to execute.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProgramParametersGroup(Composite parent)
    {
    	// new parameters label
    	Label parametersLabel = new Label(parent,SWT.NONE);
    	parametersLabel.setText(_plugin.getLocalizedString("debugLauncherMain.ProgramParameters"));
      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
   	parametersLabel.setLayoutData(data);

    	// project specification group
    	Composite parametersGroup = new Composite(parent,SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	parametersGroup.setLayout(layout);
    	parametersGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
    	
    	
    	// new parameters name entry field
    	_parametersField = new Text(parametersGroup, SWT.BORDER);
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    	_parametersField.setLayoutData(data);

		_parametersField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
    	_parametersField.setEnabled(true);
         	
    	if (initialParametersFieldValue != null)
    	    _parametersField.setText(initialParametersFieldValue);
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
   	// working directory name label
   	Label directoryLabel = new Label(parent,SWT.NONE);
   	directoryLabel.setText(_plugin.getLocalizedString("debugLauncherMain.WorkingDirectory"));
      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
   	directoryLabel.setLayoutData(data);

   	// working directory specification group
   	Composite workingDirectoryGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 2;
   	workingDirectoryGroup.setLayout(layout);
   	workingDirectoryGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));

   	// Directory name entry field
   	_workingDirectoryField = new Text(workingDirectoryGroup,SWT.BORDER);
   	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.horizontalSpan = 1;
   	_workingDirectoryField.setLayoutData(data);

		_workingDirectoryField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

   	// browse button
   	Button workingDirectoryBrowseButton = new Button(workingDirectoryGroup, SWT.PUSH);
   	workingDirectoryBrowseButton.setText(_plugin.getLocalizedString("debugLauncherMain.Browse"));

		workingDirectoryBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkingDirectoryBrowseButtonPressed();
			}
		});

   	workingDirectoryBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));	
    }

	/**
	 * Create some empty space
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}
	
	
	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
   {
     IProject project;
     DataElement executable, directory;
     IStructuredSelection selection = getSelection();
     if(selection == null)
     {
        displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.noSelection"));
        return;
     }

     Object element = selection.getFirstElement();

     if (element instanceof DataElement)
     {
   		executable = (DataElement)element;
	   	if (!executable.isOfType("executable"))
	      {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notExecutable"));
   			return;
	      }

         DataElement projectElement = _api.getProjectFor(executable);
         project = _api.findProjectResource(projectElement);
         if (!project.isOpen())
         {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
            return;
         }
	   	directory = executable.getParent();
      }	
      else if (element instanceof IProject || element instanceof IResource)
      {
         project = ((IResource)element).getProject();
         if (!project.isOpen())
         {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
            return;
         }

		   executable = _api.findResourceElement((IResource)element);
   		if (executable == null)
	      {
            if (_plugin.isCppProject(project))
            {
      			IResource resource = (IResource)element;
	      		IResource parentRes = resource.getParent();
			
      			DataStore dataStore = _plugin.getCurrentDataStore();
	      		directory = dataStore.createObject(null, "directory", parentRes.getName(),
 	  	   					    parentRes.getLocation().toString());

	   	   	executable = dataStore.createObject(directory, "executable", resource.getName(),
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
    			directory = executable.getParent();
  	      }
   	}
	   else
   	{
         displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notExecutable"));
   		return;
	   }

      config.setAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, executable.getSource());
      config.setAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, directory.getSource());
	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateExecutableFromConfig(config);
	}

	protected void updateExecutableFromConfig(ILaunchConfiguration config) {
		String executableName = "";
		String workingDirectory = "";
		String parameters = "";

		try
      {
       	executableName = config.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
         if (executableName.length() != 0)
         {
      		_programNameField.setText(executableName);		
         }

			parameters = config.getAttribute(CppLaunchConfigConstants.ATTR_PARAMETERS, "");
         if (parameters.length() != 0)
         {
      		_parametersField.setText(parameters);		
         }

			workingDirectory = config.getAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, "");
         if (workingDirectory.length() != 0)
         {
      		_workingDirectoryField.setText(workingDirectory);		
         }
		}
      catch (CoreException ce)
      {			
        displayMessageDialog(_plugin.getLocalizedString("CppDebugInfoTab.Exception") + ce.toString());
		}
	}

    protected void handleProgramBrowseButtonPressed()
    {
      DataElement rootDirectory = CppPlugin.getDefault().getCurrentDataStore().getHostRoot().get(0);
      DataElement directory = rootDirectory.getDataStore().getHostRoot().get(0).dereference();
  		directory = directory.getParent();
     	DataElementFileDialog dialog = new DataElementFileDialog("Select executable program", directory, true);
     	dialog.setActionLoader(org.eclipse.cdt.cpp.ui.internal.views.CppActionLoader.getInstance());
  		dialog.open();
     	if (dialog.getReturnCode() == dialog.OK)
  	   {
     		DataElement selected = dialog.getSelected();
    	   	if (selected != null)
  	      {
     	      _programNameField.setText(selected.getSource());
 		   }
  	   }
    }	

    protected void handleWorkingDirectoryBrowseButtonPressed()
    {
      DataElement rootDirectory = CppPlugin.getDefault().getCurrentDataStore().getHostRoot().get(0);
      DataElement directory = rootDirectory.getDataStore().getHostRoot().get(0).dereference();
  		directory = directory.getParent();
     	DataElementFileDialog dialog = new DataElementFileDialog("Select directory", directory, true);
     	dialog.setActionLoader(org.eclipse.cdt.cpp.ui.internal.views.CppActionLoader.getInstance());
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

	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config)
   {
      setErrorMessage(null);
	   setMessage(null);

      if (_programNameField.getText().length() == 0)
      {
         setErrorMessage(_plugin.getLocalizedString("loadLauncher.Error.missingProgramName"));
      }

      if (_workingDirectoryField.getText().length() == 0)
      {
         setErrorMessage(_plugin.getLocalizedString("loadLauncher.Error.missingWorkingDirectory"));
      }

		config.setAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, (String)_programNameField.getText());
		config.setAttribute(CppLaunchConfigConstants.ATTR_PARAMETERS, (String)_parametersField.getText());
		config.setAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, (String)_workingDirectoryField.getText());
	}

	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	

	/**
	 * @see ILaunchConfigurationTab#canSave()
	 */

	public boolean canSave()
   {
      return true;   //??
   }
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName()
   {
      String Name = "&" + _plugin.getLocalizedString("debugLaunchTab.Title");
      return Name;
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
