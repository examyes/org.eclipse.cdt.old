package org.eclipse.cdt.cpp.ui.internal.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.builder.ParsePathControl;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AutoconfPreferencesPage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
	private AutoconfControl _autoconfControl;
    
    String configureDialogKey = "Show_Configure_Dialog";
	String configureUpdateKey = "Update_When_Configure";
    
    String createDialogKey = "Show_Create_Dialog";
	String createUpdateKey = "Update_When_Create";
	
	String runDialogKey = "Show_Run_Dialg";
	String runUpdateKey = "Update_When_Run";
	
	String updateAllDialogKey = "Show_Update_All_Dialog";
	String updateMakefileAmKey = "Show_Update_MakefileAm_Dialog";
	String updateConfigureInKey = "Show_Update_ConfigureIn_Dialog";
  
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);

		_autoconfControl = new AutoconfControl(control, SWT.NONE);

		performDefaults();
	
		control.setLayout(new GridLayout());

		return control;
	}

    public void init(IWorkbench workbench) 
    {	
    }
	public void performDefaults() 
    {
		CppPlugin plugin      = CppPlugin.getDefault();
		ArrayList autoUpdateConfigure = plugin.readProperty(configureUpdateKey);
		if (autoUpdateConfigure.isEmpty())
		{
			autoUpdateConfigure.add("Yes");
			_autoconfControl.setAutoConfigureUpdateSelection(true);
			plugin.writeProperty(configureUpdateKey,autoUpdateConfigure);	
		}
		else
		{
			String preference = (String)autoUpdateConfigure.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setAutoConfigureUpdateSelection(true);
			}
			else
			{
				_autoconfControl.setAutoConfigureUpdateSelection(false);
			}
		}
		
		ArrayList autoUpdateRun = plugin.readProperty(runUpdateKey);
		if (autoUpdateRun.isEmpty())
		{
			autoUpdateRun.add("Yes");
			_autoconfControl.setAutoRunUpdateSelection(true);
			plugin.writeProperty(runUpdateKey,autoUpdateRun);	
		}
		else
		{
			String preference = (String)autoUpdateRun.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setAutoRunUpdateSelection(true);
			}
			else
			{
				_autoconfControl.setAutoRunUpdateSelection(false);
			}
		}

		ArrayList autoUpdateCreate = plugin.readProperty(createUpdateKey);
		if (autoUpdateCreate.isEmpty())
		{
			autoUpdateCreate.add("Yes");
			_autoconfControl.setAutoCreateUpdateSelection(true);	
			plugin.writeProperty(createUpdateKey,autoUpdateCreate);
		}
		else
		{
			String preference = (String)autoUpdateCreate.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setAutoCreateUpdateSelection(true);
			}
			else
			{
				_autoconfControl.setAutoCreateUpdateSelection(false);
			}
		}

		ArrayList showDialogConfigure = plugin.readProperty(configureDialogKey);
		if (showDialogConfigure.isEmpty())
		{
			showDialogConfigure.add("Yes");
			_autoconfControl.setShowConfigureDialogSelection(true);
			plugin.writeProperty(configureDialogKey,showDialogConfigure);
		}
		else
		{
			String preference = (String)showDialogConfigure.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setShowConfigureDialogSelection(true);
			}
			else
			{
				_autoconfControl.setShowConfigureDialogSelection(false);
			}
		}

		ArrayList showDialogRun = plugin.readProperty(runDialogKey);
		if (showDialogRun.isEmpty())
		{
			showDialogRun.add("Yes");
			_autoconfControl.setShowRunDialogSelection(true);
			plugin.writeProperty(runDialogKey, showDialogRun);
		}
		else
		{
			String preference = (String)showDialogRun.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setShowRunDialogSelection(true);
			}
			else
			{
				_autoconfControl.setShowRunDialogSelection(false);
			}
		}

		ArrayList showDialogCreate = plugin.readProperty(createDialogKey);
		if (showDialogCreate.isEmpty())
		{
			showDialogCreate.add("Yes");
			_autoconfControl.setShowCreateDialogSelection(true);
			plugin.writeProperty(createDialogKey,showDialogCreate);
		}
		else
		{
			String preference = (String)showDialogCreate.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setShowCreateDialogSelection(true);
			}
			else
			{
				_autoconfControl.setShowCreateDialogSelection(false);
			}
		}


		ArrayList list = plugin.readProperty(updateAllDialogKey);
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setUpdateAllButtonSelection(true);
			plugin.writeProperty(updateAllDialogKey,list);	
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setUpdateAllButtonSelection(true);
			}
			else
			{
				_autoconfControl.setUpdateAllButtonSelection(false);
			}
		}


		ArrayList updateConflist = plugin.readProperty(updateConfigureInKey);
		if (updateConflist.isEmpty())
		{
			updateConflist.add("Yes");
			_autoconfControl.setUpdateConfigureInButtonSelection(true);
			plugin.writeProperty(updateConfigureInKey,list);	
		}
		else
		{
			String preference = (String)updateConflist.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setUpdateConfigureInButtonSelection(true);
			}
			else
			{
				_autoconfControl.setUpdateConfigureInButtonSelection(false);
			}
		}



		ArrayList makefileAmlist = plugin.readProperty(updateMakefileAmKey);
		if (makefileAmlist.isEmpty())
		{
			makefileAmlist.add("Yes");
			_autoconfControl.setUpdateMakefileAmButtonSelection(true);
			plugin.writeProperty(updateMakefileAmKey,list);	
		}
		else
		{
			String preference = (String)makefileAmlist.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setUpdateMakefileAmButtonSelection(true);
			}
			else
			{
				_autoconfControl.setUpdateMakefileAmButtonSelection(false);
			}
		}



    }

    public boolean performOk()
	{
		CppPlugin plugin      = CppPlugin.getDefault();
		
		// auto update when configure
		ArrayList autoConfigureUpdate = new ArrayList();
		if (_autoconfControl.getAutoConfigureUpdateSelection())
		{
			autoConfigureUpdate.add("Yes");		
		}
		else
		{
			autoConfigureUpdate.add("No");		
		}		

		plugin.writeProperty(configureUpdateKey, autoConfigureUpdate);	
		
		
		// auto update when run configure
		ArrayList autoRunUpdate = new ArrayList();
		if (_autoconfControl.getAutoRunUpdateSelection())
		{
			autoRunUpdate.add("Yes");		
		}
		else
		{
			autoRunUpdate.add("No");		
		}
	
		
		plugin.writeProperty(runUpdateKey, autoRunUpdate);			

		// auto update when create configure
		ArrayList autoCreateUpdate = new ArrayList();
		if (_autoconfControl.getAutoCreateUpdateSelection())
		{
			autoCreateUpdate.add("Yes");		
		}
		else
		{
			autoCreateUpdate.add("No");		
		}		
		plugin.writeProperty(createUpdateKey, autoCreateUpdate);	
		
			// show dialog when configure
		ArrayList showConfigureDialog = new ArrayList();
		if (_autoconfControl.getShowConfigureDialogSelection())
		{
			showConfigureDialog.add("Yes");		
		}
		else
		{
			showConfigureDialog.add("No");		
		}	

		//plugin      = CppPlugin.getDefault();
		plugin.writeProperty(configureDialogKey, showConfigureDialog);	
	
		// show dialog when run configure
		ArrayList showRunDialog = new ArrayList();
		if (_autoconfControl.getShowRunDialogSelection())
		{
			showRunDialog.add("Yes");		
		}
		else
		{
			showRunDialog.add("No");		
		}	

		//plugin      = CppPlugin.getDefault();
		plugin.writeProperty(runDialogKey, showRunDialog);	


		// show dialog when create configure
		ArrayList showCreateDialog = new ArrayList();
		if (_autoconfControl.getShowCreateDialogSelection())
		{
			showCreateDialog.add("Yes");		
		}
		else
		{
			showCreateDialog.add("No");		
		}	

		plugin.writeProperty(createDialogKey, showCreateDialog);	
		
		
		// update all dialog
		ArrayList udateAllDialog = new ArrayList();
		if (_autoconfControl.getUpdateAllButtonSelection())
		{
			udateAllDialog.add("Yes");		
		}
		else
		{
			udateAllDialog.add("No");		
		}	

		plugin.writeProperty(updateAllDialogKey, udateAllDialog);
		
		// update configureIn dialog
		ArrayList updateConfigureInDialog = new ArrayList();
		if (_autoconfControl.getUpdateConfigureInButtonSelection())
		{
			updateConfigureInDialog.add("Yes");		
		}
		else
		{
			updateConfigureInDialog.add("No");		
		}	

		plugin.writeProperty(updateConfigureInKey, updateConfigureInDialog);		
		
		// update makefileAm dialog
		ArrayList updateMakefileAmDialog = new ArrayList();
		if (_autoconfControl.getUpdateMakefileAmButtonSelection())
		{
			updateMakefileAmDialog.add("Yes");		
		}
		else
		{
			updateMakefileAmDialog.add("No");		
		}	

		plugin.writeProperty(updateMakefileAmKey, updateMakefileAmDialog);		
						
		return true;
   	}

}

