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
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


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
    protected ArrayList    _history;

    private  String        _programName;
    private  String        _processID;

    private static DataElement _executable;
    private  DataElement   _directory;

				
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
   	// project specification group
   	Composite programGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 2;
   	programGroup.setLayout(layout);
   	programGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// new program label
   	Label programLabel = new Label(programGroup,SWT.NONE);
   	programLabel.setText(_plugin.getLocalizedString("debugAttachLauncherMain.ProgramName"));
   	
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
     * Field for entering the processID of the program to attach to in debug mode.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProcessIDGroup(Composite parent)
    {
    	// project specification group
    	Composite processIDGroup = new Composite(parent,SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	processIDGroup.setLayout(layout);
    	processIDGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
    	
    	// new processID label
    	Label processIDLabel = new Label(processIDGroup,SWT.NONE);
    	processIDLabel.setText(_plugin.getLocalizedString("debugAttachLauncherMain.ProcessID"));
    	
    	// new processID name entry field
    	_processIDField = new Text(processIDGroup, SWT.BORDER);
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    	_processIDField.setLayoutData(data);

		_processIDField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
    	_processIDField.setEnabled(true);
    }


	
	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
   {

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
		String processID = "";
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

			processID = config.getAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, "");
         if (processID == "")
         {
            if (_processID != null)
               _processIDField.setText(_processID);
         }
         else
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
      System.out.println("CppDebugInfoTab:performApply() ");
		config.setAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, (String)_programNameField.getText());
		config.setAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, (String)_processIDField.getText());
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
	 * Show a dialog that lets the user select a process ID.
	 * Equivalent to output from a "ps" command on Unix
	 */

    protected void handleProcessIDBrowseButtonPressed()
    {
      if (_processID != null)
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
      	      _processIDField.setText(selected.getSource());
  		      }
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
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {

      System.out.println("CppDebugInfoTab:isValid()");

		setErrorMessage(null);
		setMessage(null);
		
		// TO DO should verify that test exists
		return true;
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
