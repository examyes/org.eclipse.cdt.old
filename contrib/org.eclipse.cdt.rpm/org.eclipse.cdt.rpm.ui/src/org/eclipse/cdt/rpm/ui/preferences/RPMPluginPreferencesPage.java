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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
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
	   data.horizontalSpan = 2;
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
		return System.getProperty ( "user.name" );
	
   }

   protected void initializeDefaultPreferences(IPreferenceStore store)
   {
//	store.setDefault("IRpmConstants.DATE_FORMAT","true"); //$NON-NLS-1$ //$NON-NLS-2$
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
   }
   
   private void initializeDefaults()
   {
		IPreferenceStore store = getPreferenceStore();
		
//		dateFormat.setSelection(store.getDefaultBoolean("IRpmConstants.DATE_FORMAT"));  //$NON-NLS-1$
		emailField.setText(store.getDefaultString("IRpmConstants.AUTHOR_EMAIL")); //$NON-NLS-1$
		nameField.setText(store.getDefaultString("IRpmConstants.AUTHOR_NAME")); //$NON-NLS-1$
		rpmWorkAreaField.setText(store.getDefaultString("IRpmConstants.RPM_WORK_AREA")); //$NON-NLS-1$
//	These fields are commented out because of a decision not to let the user change these values at this time
//		nameField.setText(store.getDefaultString("IRpmConstants.USER_WORK_AREA")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.RPM_LOG_NAME")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.RPM_DISPLAYED_LOG_NAME")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.SPEC_FILE_PREFIX")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.SRPM_INFO_FILE")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.RPM_SHELL_SCRIPT")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.RPM_RESOURCE_FILE")); //$NON-NLS-1$
//		nameField.setText(store.getDefaultString("IRpmConstants.RPM_MACROS_FILE")); //$NON-NLS-1$
		storeValues();
   }
   
   private void initializeValues()
	 {
		   IPreferenceStore store = getPreferenceStore();
		
//			dateFormat.setSelection(store.getBoolean("IRpmConstants.DATE_FORMAT"));	     //$NON-NLS-1$
			rpmWorkAreaField.setText(store.getString("IRpmConstants.RPM_WORK_AREA")); //$NON-NLS-1$
//		These fields are commented out because of a decision not to let the user change these values at this time
//			userWorkAreaField.setText(store.getString("IRpmConstants.USER_WORK_AREA")); //$NON-NLS-1$
//			rpmLogNameField.setText(store.getString("IRpmConstants.RPM_LOG_NAME")); //$NON-NLS-1$
//			displayedRpmLogNameField.setText(store.getString("IRpmConstants.RPM_DISPLAYED_LOG_NAME")); //$NON-NLS-1$
//			specFilePrefixField.setText(store.getString("IRpmConstants.SPEC_FILE_PREFIX"));  //$NON-NLS-1$
//			srpmInfoFileNameField.setText(store.getString("IRpmConstants.SRPM_INFO_FILE"));  //$NON-NLS-1$
//			rpmShellScriptFileNameField.setText(store.getString("IRpmConstants.RPM_SHELL_SCRIPT")); //$NON-NLS-1$
//			rpmResourceFileNameField.setText(store.getString("IRpmConstants.RPM_RESOURCE_FILE")); //$NON-NLS-1$
//			rpmMacrosFileNameField.setText(store.getString("IRpmConstants.RPM_MACROS_FILE"));  //$NON-NLS-1$
			emailField.setText(store.getString("IRpmConstants.AUTHOR_EMAIL")); //$NON-NLS-1$
			nameField.setText(store.getString("IRpmConstants.AUTHOR_NAME")); //$NON-NLS-1$
	  }

	
   private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
	   	
//		store.setValue("IRpmConstants.DATE_FORMAT",dateFormat.getSelection()); //$NON-NLS-1$
		store.setValue("IRpmConstants.RPM_WORK_AREA",rpmWorkAreaField.getText()); //$NON-NLS-1$
// These fields are commented out because of a decision not to let the user change these values at this time
//		store.setValue("IRpmConstants.RPM_WORK_AREA",userWorkAreaField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.RPM_LOG_NAME",rpmLogNameField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.RPM_DISPLAYED_LOG_NAME",displayedRpmLogNameField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.SPEC_FILE_PREFIX",specFilePrefixField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.SRPM_INFO_FILE",srpmInfoFileNameField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.RPM_SHELL_SCRIPT",rpmShellScriptFileNameField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.RPM_RESOURCE_FILE",rpmResourceFileNameField.getText()); //$NON-NLS-1$
//		store.setValue("IRpmConstants.RPM_MACROS_FILE",rpmMacrosFileNameField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.AUTHOR_NAME",nameField.getText()); //$NON-NLS-1$
		store.setValue("IRpmConstants.AUTHOR_EMAIL",emailField.getText()); //$NON-NLS-1$
		
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
		
		
	
		//composite_textField << parent
		Composite composite_textField = createComposite(parent, 2);
		createLabel(composite_textField, "Author Name: ");	 //$NON-NLS-1$
		nameField = createTextField(composite_textField);
		
		//composite_textField << parent

		createLabel(composite_textField, "Author Email: ");	 //$NON-NLS-1$
		emailField = createTextField(composite_textField);
		
		createLabel(composite_textField, "RPM Work Area ");	 //$NON-NLS-1$
		rpmWorkAreaField = createTextField(composite_textField);

//		These fields are commented out because of a decision not to let the user change these values at this time
/*		
		createLabel(composite_textField, "User Work Area ");	 //$NON-NLS-1$
		userWorkAreaField = createTextField(composite_textField);
		
		createLabel(composite_textField, "RPM Log Name prefix ");	 //$NON-NLS-1$
		rpmLogNameField = createTextField(composite_textField);
		
		createLabel(composite_textField, "File in Work Area to Store RPM Log to be Displayed ");	 //$NON-NLS-1$
		displayedRpmLogNameField = createTextField(composite_textField);
		
		createLabel(composite_textField, "Spec File Prefix ");	 //$NON-NLS-1$
		specFilePrefixField = createTextField(composite_textField);
		
		createLabel(composite_textField, "Source RPM Info File Name ");	 //$NON-NLS-1$
		srpmInfoFileNameField = createTextField(composite_textField);
		
		createLabel(composite_textField, "RPM Shell Script Name ");	 //$NON-NLS-1$
		rpmShellScriptFileNameField = createTextField(composite_textField);
		
		createLabel(composite_textField, "RPM Resource File Name ");	 //$NON-NLS-1$
		rpmResourceFileNameField = createTextField(composite_textField); 
		
		createLabel(composite_textField, "RPM Macros File ");	 //$NON-NLS-1$
		rpmMacrosFileNameField = createTextField(composite_textField); */
		
		
		//composite_tab << parent
//		Composite composite_tab = createComposite(parent, 2);
//		createLabel(composite_tab,"Miscellaneous"); //$NON-NLS-1$

//		tabForward(composite_tab);

	
//		Composite composite_checkBox = createComposite(composite_tab, 1);
	
//		dateFormat = createCheckBox(composite_checkBox, "Use ISO Date? ");    //$NON-NLS-1$ 

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
 }