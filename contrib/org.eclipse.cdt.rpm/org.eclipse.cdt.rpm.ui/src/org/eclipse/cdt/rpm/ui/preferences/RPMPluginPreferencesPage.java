/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.rpm.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.rpm.core.RPMCorePlugin;


/**
 * This class implements a sample preference page that is
 * added to the preference dialog based on the registration.
 */
public class RPMPluginPreferencesPage extends PreferencePage
	implements IWorkbenchPreferencePage, SelectionListener, ModifyListener {
    
	private Button dateFormat;
	private Text rpmMacrosFileNameField;
	private Text rpmResourceFileNameField;
	private Text rpmWorkAreaField;
	private Text rpmLogNameField;
	private Text displayedRpmLogNameField;
	private Text specFilePrefixField;
	private Text srpmInfoFileNameField;
	private Text rpmShellScriptFileNameField;
	private Text emailField;
	private Text nameField;
	private Text WorkAreaField;
	
	private Text makeField;
	private Text rpmField;
	private Text rpmbuildField;
	private Text chmodField;
	private Text cpField;
	private Text diffField;
	private Text tarField;
	
	
	static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
    
	private Button createCheckBox(Composite group, String label) {
	   Button button = new Button(group, SWT.CHECK | SWT.LEFT);
	   button.setText(label);
	   button.addSelectionListener(this);
	   GridData data = new GridData();
	   button.setLayoutData(data);
	   return button;
   }
   
   private Composite createComposite(Composite parent, int numColumns) {
	   Composite composite = new Composite(parent, SWT.NULL);

	   //GridLayout
	   GridLayout layout = new GridLayout();
	   layout.numColumns = numColumns;
	   composite.setLayout(layout);

	   //GridData
	   GridData data = new GridData();
	   data.verticalAlignment = GridData.FILL;
	   data.horizontalAlignment = GridData.FILL;
	   composite.setLayoutData(data);
	   return composite;
   }
   
   private Label createLabel(Composite parent, String text) {
	   Label label = new Label(parent, SWT.LEFT);
	   label.setText(text);
	   GridData data = new GridData();
	   data.horizontalSpan = 1;
	   data.horizontalAlignment = GridData.FILL;
	   label.setLayoutData(data);
	   return label;
   }
  
   private Button createPushButton(Composite parent, String label) {
	   Button button = new Button(parent, SWT.PUSH);
	   button.setText(label);
	   button.addSelectionListener(this);
	   GridData data = new GridData();
	   data.horizontalAlignment = GridData.FILL;
	   button.setLayoutData(data);
	   return button;
   }
   
   private Button createBrowseButton(Composite parent, Text field, String command) {
	   Button button = new Button(parent, SWT.PUSH);
	   button.setText("Browse..."); //$NON-NLS-1$
	   button.addSelectionListener(new BrowseSelectionListener(getShell(), 
	   								"Select '" + command + //$NON-NLS-1$
									"' Command", field)); //$NON-NLS-1$
	   GridData data = new GridData();
	   data.horizontalAlignment = GridData.FILL;
	   button.setLayoutData(data);
	   return button;
   }
 
   private Button createRadioButton(Composite parent, String label) {
	   Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
	   button.setText(label);
	   button.addSelectionListener(this);
	   GridData data = new GridData();
	   button.setLayoutData(data);
	   return button;
   }
 
   private Text createTextField(Composite parent) {
	   Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
	   text.addModifyListener(this);
	   GridData data = new GridData();
	   data.horizontalAlignment = GridData.FILL;
	   data.grabExcessHorizontalSpace = true;
	   data.verticalAlignment = GridData.CENTER;
	   data.grabExcessVerticalSpace = false;
	   text.setLayoutData(data);
	   return text;
   }
   
   protected void createSpacer(Composite composite, int columnSpan) {
   	   Label label = new Label(composite, SWT.NONE);
   	   GridData gd = new GridData();
   	   gd.horizontalSpan = columnSpan;
   	   label.setLayoutData(gd);
   }
   
   private void tabForward(Composite parent) {
	   Label vfiller = new Label(parent, SWT.LEFT);
	   GridData gridData = new GridData();
	   gridData = new GridData();
	   gridData.horizontalAlignment = GridData.BEGINNING;
	   gridData.grabExcessHorizontalSpace = false;
	   gridData.verticalAlignment = GridData.CENTER;
	   gridData.grabExcessVerticalSpace = false;
	   vfiller.setLayoutData(gridData);
   }
   protected IPreferenceStore doGetPreferenceStore() {
	   return RPMCorePlugin.getDefault().getPreferenceStore();
   }
   
   public void init(IWorkbench workbench){
		initializeDefaultPreferences(getPreferenceStore());
   }
   
   private String getUserName()
   {
		return System.getProperty ( "user.name" ); //$NON-NLS-1$
	
   }

   protected void initializeDefaultPreferences(IPreferenceStore store)
   {
	store.setDefault("IRpmConstants.RPM_WORK_AREA","/var/tmp"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.USER_WORK_AREA",file_sep+"rpm_workarea"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPM_DISPLAYED_LOG_NAME",".logfilename_" +  //$NON-NLS-1$ //$NON-NLS-2$
			getUserName());
	store.setDefault("IRpmConstants.SPEC_FILE_PREFIX","eclipse_"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.SRPM_INFO_FILE",file_sep+".srpminfo"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPM_SHELL SCRIPT","rpmshell.sh"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPM_LOG_NAME","rpmbuild.log"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPM_RESOURCE_FILE",".rpmrc"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPM_MACROS_FILE",".rpm_macros"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.AUTHOR_NAME",getUserName()); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.AUTHOR_EMAIL",getUserName()+"@" + RPMCorePlugin.getHostName()); //$NON-NLS-1$ //$NON-NLS-2$
   
	store.setDefault("IRpmConstants.MAKE_CMD", "/usr/bin/make"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPM_CMD", "/bin/rpm"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.RPMBUILD_CMD", "/usr/bin/rpmbuild"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.CHMOD_CMD", "/bin/chmod"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.CP_CMD", "/bin/cp"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.DIFF_CMD", "diff"); //$NON-NLS-1$ //$NON-NLS-2$
	store.setDefault("IRpmConstants.TAR_CMD", "/bin/tar"); //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   private void initializeDefaults()
   {
		IPreferenceStore store = getPreferenceStore();
		
		emailField.setText(store.getDefaultString("IRpmConstants.AUTHOR_EMAIL")); //$NON-NLS-1$
		nameField.setText(store.getDefaultString("IRpmConstants.AUTHOR_NAME")); //$NON-NLS-1$
		rpmWorkAreaField.setText(store.getDefaultString("IRpmConstants.RPM_WORK_AREA")); //$NON-NLS-1$
		makeField.setText(store.getDefaultString("IRpmConstants.MAKE_CMD")); //$NON-NLS-1$
		rpmField.setText(store.getDefaultString("IRpmConstants.RPM_CMD")); //$NON-NLS-1$
		rpmbuildField.setText(store.getDefaultString("IRpmConstants.RPMBUILD_CMD")); //$NON-NLS-1$
		chmodField.setText(store.getDefaultString("IRpmConstants.CHMOD_CMD")); //$NON-NLS-1$
		cpField.setText(store.getDefaultString("IRpmConstants.CP_CMD")); //$NON-NLS-1$
		diffField.setText(store.getDefaultString("IRpmConstants.DIFF_CMD")); //$NON-NLS-1$
		tarField.setText(store.getDefaultString("IRpmConstants.TAR_CMD")); //$NON-NLS-1$
		
		storeValues();
   }
   
   private void initializeValues()
	 {
		   IPreferenceStore store = getPreferenceStore();
		
			rpmWorkAreaField.setText(store.getString("IRpmConstants.RPM_WORK_AREA")); //$NON-NLS-1$
			emailField.setText(store.getString("IRpmConstants.AUTHOR_EMAIL")); //$NON-NLS-1$
			nameField.setText(store.getString("IRpmConstants.AUTHOR_NAME")); //$NON-NLS-1$
			makeField.setText(store.getString("IRpmConstants.MAKE_CMD")); //$NON-NLS-1$
			rpmField.setText(store.getString("IRpmConstants.RPM_CMD")); //$NON-NLS-1$
			rpmbuildField.setText(store.getString("IRpmConstants.RPMBUILD_CMD")); //$NON-NLS-1$
			chmodField.setText(store.getString("IRpmConstants.CHMOD_CMD")); //$NON-NLS-1$
			cpField.setText(store.getString("IRpmConstants.CP_CMD")); //$NON-NLS-1$
			diffField.setText(store.getString("IRpmConstants.DIFF_CMD")); //$NON-NLS-1$
			tarField.setText(store.getString("IRpmConstants.TAR_CMD")); //$NON-NLS-1$
	 }

	
   private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
	   	
		store.setValue("IRpmConstants.RPM_WORK_AREA",rpmWorkAreaField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.AUTHOR_NAME",nameField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.AUTHOR_EMAIL",emailField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.RPM_CMD", rpmField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.RPMBUILD_CMD", rpmbuildField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.TAR_CMD", tarField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.MAKE_CMD", makeField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.CHMOD_CMD", chmodField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.CP_CMD", cpField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.DIFF_CMD", diffField.getText()); //$NON-NLS-1$
   	}
    
	public void modifyText(ModifyEvent event) {
		//Do nothing on a modification in this example
	}
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
		
	}
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	public boolean performOk() {
		storeValues();
		RPMCorePlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	protected Control createContents(Composite parent)
	{
		//mainComposite << parent
		Composite mainComposite = createComposite(parent, 1);
		mainComposite.setLayout(new GridLayout());
		
		Group userPrefs = new Group(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		userPrefs.setLayout(layout);
		userPrefs.setText("RPM Preferences"); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		userPrefs.setLayoutData(gd);
				
		createLabel(userPrefs, "Author Name: ");	 //$NON-NLS-1$
		nameField = createTextField(userPrefs);
		
		createLabel(userPrefs, "Author Email: ");	 //$NON-NLS-1$
		emailField = createTextField(userPrefs);
		
		createLabel(userPrefs, "RPM Work Area: ");	 //$NON-NLS-1$
		rpmWorkAreaField = createTextField(userPrefs);
		
		createSpacer(mainComposite, 2);
		
		Group shellPrefs = new Group(mainComposite, SWT.NONE);
		shellPrefs.setText("Shell Commands"); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 3;
		shellPrefs.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		shellPrefs.setLayoutData(gd);
		
		String spacer = ": "; //$NON-NLS-1$
		String title = "rpm"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);	 
		rpmField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, rpmField, title);
		
		title = "rpmbuild"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		rpmbuildField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, rpmbuildField, title);

		title = "make"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		makeField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, makeField, title);
		
		title = "diff"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		diffField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, diffField, title);
		
		title = "tar"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		tarField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, tarField, title);
		
		title = "chmod";	//$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		chmodField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, chmodField, title);
		
		title = "cp"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		cpField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, cpField, title);

		initializeValues();

		return new Composite(parent, SWT.NULL);
	}
	

   public void widgetDefaultSelected(SelectionEvent event) {
	   //Handle a default selection. Do nothing in this example
   }
   /** (non-Javadoc)
	* Method declared on SelectionListener
	*/
   public void widgetSelected(SelectionEvent event) {
	   //Do nothing on selection in this example;
   }
   
   private class BrowseSelectionListener implements SelectionListener {
   		private Text text;
   		private String title;
   		private Shell parent;
   		
   		public BrowseSelectionListener(Shell disp, String title, Text text) {
   			this.text = text;
   			this.title = title;
   			this.parent = disp;
   		}
   		
   		public void widgetDefaultSelected(SelectionEvent event) {
   			// no action
   		}
   	  
   		public void widgetSelected(SelectionEvent event) {
   			
			FileDialog fd = new FileDialog(parent, SWT.OPEN | SWT.APPLICATION_MODAL);
   			fd.setText(title); 
   			fd.setFileName(text.getText());
   			String result;
   			if( (result = fd.open()) != null )
   				text.setText(result);
   		}
   	}
 }
