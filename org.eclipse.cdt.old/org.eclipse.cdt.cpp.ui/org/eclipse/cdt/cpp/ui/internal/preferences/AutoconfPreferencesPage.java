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
		ArrayList autoUpdateRun = plugin.readProperty("Auto Update Run");
		if (autoUpdateRun.isEmpty())
		{
			autoUpdateRun.add("Yes");
			_autoconfControl.setAutoRunUpdateSelection(true);
			plugin.writeProperty("Auto Update Run",autoUpdateRun);	
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

		ArrayList autoUpdateCreate = plugin.readProperty("Auto Update Create");
		if (autoUpdateCreate.isEmpty())
		{
			autoUpdateCreate.add("Yes");
			_autoconfControl.setAutoCreateUpdateSelection(true);	
			plugin.writeProperty("Auto Update Create",autoUpdateCreate);
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

		ArrayList showDialogRun = plugin.readProperty("Show Dialog Run");
		if (showDialogRun.isEmpty())
		{
			showDialogRun.add("Yes");
			_autoconfControl.setShowRunDialogSelection(true);
			plugin.writeProperty("Show Dialog Run", showDialogRun);
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

		ArrayList showDialogCreate = plugin.readProperty("Show Dialog Create");
		if (showDialogCreate.isEmpty())
		{
			showDialogCreate.add("Yes");
			_autoconfControl.setShowCreateDialogSelection(true);
			plugin.writeProperty("Show Dialog Create",showDialogCreate);
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


		ArrayList list = plugin.readProperty("Show_Update_All_Dialog");
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setUpdateAllButtonSelection(true);
			plugin.writeProperty("Show_Update_All_Dialog",list);	
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


		ArrayList updateConflist = plugin.readProperty("Show_Update_ConfigureIn_Dialog");
		if (updateConflist.isEmpty())
		{
			updateConflist.add("Yes");
			_autoconfControl.setUpdateConfigureInButtonSelection(true);
			plugin.writeProperty("Show_Update_ConfigureIn_Dialog",list);	
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



		ArrayList makefileAmlist = plugin.readProperty("Show_Update_MakefileAm_Dialog");
		if (makefileAmlist.isEmpty())
		{
			makefileAmlist.add("Yes");
			_autoconfControl.setUpdateMakefileAmButtonSelection(true);
			plugin.writeProperty("Show_Update_MakefileAm_Dialog",list);	
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
	
		CppPlugin plugin      = CppPlugin.getDefault();
		plugin.writeProperty("Auto Update Run", autoRunUpdate);			


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

		plugin.writeProperty("Auto Update Create", autoCreateUpdate);	
		
	
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

		plugin      = CppPlugin.getDefault();
		plugin.writeProperty("Show Dialog Run", showRunDialog);	


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

		plugin.writeProperty("Show Dialog Create", showCreateDialog);	
		
		
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

		plugin.writeProperty("Show_Update_All_Dialog", udateAllDialog);
		
		// update configureIn dialog
		ArrayList udateConfigureInDialog = new ArrayList();
		if (_autoconfControl.getUpdateConfigureInButtonSelection())
		{
			udateConfigureInDialog.add("Yes");		
		}
		else
		{
			udateConfigureInDialog.add("No");		
		}	

		plugin.writeProperty("Show_Update_ConfigureIn_Dialog", udateConfigureInDialog);		
		
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

		plugin.writeProperty("Show_Update_MakefileAm_Dialog", updateMakefileAmDialog);		
						
		return true;
   	}

}

