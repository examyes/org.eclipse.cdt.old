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

import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.dstore.core.model.*;

/**
 * This tab appears in the LaunchConfigurationDialog for launch configurations that
 * require C/C++ specific launching information such as executable name, process id, etc.
 */
public class CppAttachInfoTab extends CppLaunchConfigurationTab
 {
	

    // initial value stores
    private String initialProgramFieldValue;

    // widgets
    private Text                             _programNameField;
    private Text                             _processIDField;

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 300;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    protected ModelInterface	_api = ModelInterface.getInstance();

    private  DataElement   _directory;
				
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
   	createProcessIDGroup(composite);

   	createSpacer(composite);

   	_processIDField.setFocus();
   	
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
   	programLabel.setText(_plugin.getLocalizedString("debugAttachLauncherMain.ProgramName"));
   	GridData data = new GridData(GridData.FILL_HORIZONTAL);
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


   	// browse executable/library name button
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
     * Field for entering the processID of the program to attach to in debug mode.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProcessIDGroup(Composite parent)
    {
    	// new processID label
    	Label processIDLabel = new Label(parent,SWT.NONE);
    	processIDLabel.setText(_plugin.getLocalizedString("debugAttachLauncherMain.ProcessID"));
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	processIDLabel.setLayoutData(data);
    	
    	// project specification group
    	Composite processIDGroup = new Composite(parent,SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	processIDGroup.setLayout(layout);
    	processIDGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
    	
    	// new processID name entry field
    	_processIDField = new Text(processIDGroup, SWT.BORDER);
   	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.horizontalSpan = 1;
    	_processIDField.setLayoutData(data);

		_processIDField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
    	_processIDField.setEnabled(true);


   	// browse process ids button
   	Button processIDBrowseButton = new Button(processIDGroup, SWT.PUSH);
   	processIDBrowseButton.setText(_plugin.getLocalizedString("debugLauncherMain.Browse"));

		processIDBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProcessIDBrowseButtonPressed();
			}
		});

   	processIDBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));	
    }


	
	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
   {

     IProject project;
     DataElement selectedElement, directory;
     String programName = "";
     String processID = "?";

     IStructuredSelection selection = getSelection();
     if(selection == null)
     {
        displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.noSelection"));
        return;
     }

     Object element = selection.getFirstElement();

     if (element instanceof DataElement)
     {	
        selectedElement = (DataElement)element;

        DataElement projectElement = _api.getProjectFor(selectedElement);
        project = _api.findProjectResource(projectElement);
        if (!project.isOpen())
        {
           displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
           return;
        }
	     directory = selectedElement.getParent();
      }	
      else if (element instanceof IProject || element instanceof IResource)
      {
         project = ((IResource)element).getProject();
         if (!project.isOpen())
         {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
            return;
         }

         selectedElement = _api.findResourceElement((IResource)element);
         if (selectedElement == null)
         {
            if (_plugin.isCppProject(project))
            {
      			IResource resource = (IResource)element;
	      		IResource parentRes = resource.getParent();
			
      			DataStore dataStore = _plugin.getCurrentDataStore();
	      		directory = dataStore.createObject(null, "directory", parentRes.getName(),
 	  	   					    parentRes.getLocation().toString());

	   	   	selectedElement = dataStore.createObject(directory, "executable", resource.getName(),
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
        	  directory = selectedElement.getParent();
      	}
      }
      else
   	{
           displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notExecutable"));
           return;
	   }

      programName = selectedElement.getSource();
      config.setAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, programName);
      config.setAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, processID);

   }
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
              		updateExecutableFromConfig(config);
	}

   protected void updateExecutableFromConfig(ILaunchConfiguration config)
   {
	  String executableName = "";
	  String processID = "";

	  try
	  {
		 executableName = config.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
		 if (executableName.length() != 0)
		 {
			 _programNameField.setText(executableName);		
       }
       processID = config.getAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, "");
       if (processID.length() != 0)
       {
         	_processIDField.setText(processID);		
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
      _directory = CppPlugin.getDefault().getCurrentDataStore().getHostRoot().get(0);
      setErrorMessage(null);
   	setMessage(null);
      String programName = _programNameField.getText();
      String processID = _processIDField.getText();

      if (programName.length() == 0)
      {
         setErrorMessage(_plugin.getLocalizedString("attachLauncher.Error.missingProgramName"));
      }
      if (processID.length() == 0)
      {
         setErrorMessage(_plugin.getLocalizedString("attachLauncher.Error.missingProcessID"));
      }
      else
      {
         if (!isValidProcessID(processID))
         {
            setErrorMessage(_plugin.getLocalizedString("attachLauncher.Error.invalidProcessID"));
         }
      }

      config.setAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, programName);
      config.setAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, processID);
   }

	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Create some empty space
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}

	/**
	 * Show a dialog that lets the user select a program name.
	 */

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

	/**
	 * Show a dialog that lets the user select a process ID.
	 * Equivalent to output from a "ps" command on Unix
	 */

    protected void handleProcessIDBrowseButtonPressed()
    {
         BrowseProcessesDialog dialog = new BrowseProcessesDialog("Select process id", _directory);
   		dialog.open();
	   	if (dialog.getReturnCode() == dialog.OK)
   	   {
            DataElement selected = dialog.getSelected();
   	      if (selected != null)
  	         {
               _processIDField.setText(selected.getName());
  	         }
   	   }
    }	

	 protected boolean isValidProcessID(String processID)
    {
       DataStore dataStore = _directory.getDataStore();
       DataElement hostRoot = dataStore.getHostRoot();	
       DataElement processRoot = dataStore.find(hostRoot, DE.A_TYPE, "Processes", 1);
       if (processRoot.getNestedSize() > 0)
       {
          DataElement process = dataStore.find(processRoot, DE.A_NAME, processID, 1);
          return (process != null);
       }
       else
          return true;
    }
	
	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName()
        {
           String Name = "&" + _plugin.getLocalizedString("attachLaunchTab.Title");
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
