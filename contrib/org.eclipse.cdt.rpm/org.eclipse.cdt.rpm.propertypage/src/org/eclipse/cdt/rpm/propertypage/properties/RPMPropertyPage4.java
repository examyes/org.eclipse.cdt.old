package org.eclipse.cdt.rpm.propertypage.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.rpm.core.LinuxShellCmds;

public class RPMPropertyPage4 extends PropertyPage {

	private static final String PATH_TITLE = "Path:"; //$NON-NLS-1$

	private static final String RPM_NAME = "Name:"; //$NON-NLS-1$

	private static final String RPM_PRE_INSTALL = "Pre-install script:\n(if any)"; //$NON-NLS-1$

	private static final String RPM_POST_INSTALL = "Post-install script:\n(if any)"; //$NON-NLS-1$

	private static final String RPM_PRE_UNINSTALL = "Pre-uninstall script:\n(if any)"; //$NON-NLS-1$

	private static final String RPM_POST_UNINSTALL = "Post-uninstall script:\n(if any)"; //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int SCRIPT_ENTRIES_FIELD_WIDTH = 80;

	private static final int SCRIPT_ENTRIES_FIELD_HEIGHT = 20;

	private Text rpm_nameText;

	private Text rpm_PreInstallText;

	private Text rpm_PostInstallText;

	private Text rpm_PreUnInstallText;

	private Text rpm_PostUnInstallText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public RPMPropertyPage4() {
		super();
	}

	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		//Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(PATH_TITLE);

		// Path text field
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setText(((IResource) getElement()).getFullPath()
				.toString());
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for owner field
		Label rpmNameLabel = new Label(composite, SWT.NONE);
		rpmNameLabel.setText(RPM_NAME);

		// Populate RPM name text field
		rpm_nameText = new Text(composite, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		GridData gdName = new GridData();
		gdName.widthHint = convertWidthInCharsToPixels(NAME_FIELD_WIDTH);
		rpm_nameText.setLayoutData(gdName);
		String rpm_path = ((IResource) getElement()).getRawLocation()
				.toString();
		String rpm_name = LinuxShellCmds.getInfo("/bin/rpm --qf %{NAME} -qp " + //$NON-NLS-1$
				rpm_path);
		rpm_nameText.setText(rpm_name);
	}

	private void addThirdSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmPreInstallLabel = new Label(composite, SWT.NONE);
		rpmPreInstallLabel.setText(RPM_PRE_INSTALL);
		rpm_PreInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPreInst = new GridData();
		gdPreInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPreInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PreInstallText.setLayoutData(gdPreInst);

		Label rpmPostInstallLabel = new Label(composite, SWT.NONE);
		rpmPostInstallLabel.setText(RPM_POST_INSTALL);
		rpm_PostInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPostInst = new GridData();
		gdPostInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPostInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PostInstallText.setLayoutData(gdPostInst);

		Label rpmPreUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPreUnInstallLabel.setText(RPM_PRE_UNINSTALL);
		rpm_PreUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPreUnInst = new GridData();
		gdPreUnInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPreUnInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PreUnInstallText.setLayoutData(gdPreUnInst);

		Label rpmPostUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPostUnInstallLabel.setText(RPM_POST_UNINSTALL);
		rpm_PostUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPostUnInst = new GridData();
		gdPostUnInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPostUnInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PostUnInstallText.setLayoutData(gdPostUnInst);

		// Populate RPM text fields
		String rpm_path = ((IResource) getElement()).getRawLocation()
				.toString();

		String rpm_PreInstall = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{PREIN} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_PreInstallText.setText(rpm_PreInstall);

		String rpm_PostInstall = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{POSTIN} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_PostInstallText.setText(rpm_PostInstall);

		String rpm_PreUnInstall = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{PREUN} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_PreUnInstallText.setText(rpm_PreUnInstall);

		String rpm_PostUnInstall = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{POSTUN} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_PostUnInstallText.setText(rpm_PostUnInstall);

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		addSeparator(composite);
		addThirdSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {

	}

	public boolean performOk() {

		return true;
	}

}