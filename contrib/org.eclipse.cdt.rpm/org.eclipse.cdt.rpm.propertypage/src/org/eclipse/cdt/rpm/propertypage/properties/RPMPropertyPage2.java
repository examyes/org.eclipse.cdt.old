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

public class RPMPropertyPage2 extends PropertyPage {

	private static final String PATH_TITLE = "Path:"; //$NON-NLS-1$

	private static final String RPM_NAME = "Name:"; //$NON-NLS-1$

	private static final String RPM_CHANGELOG_ENTRIES = "Spec File\nChangelog Entries:"; //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int CL_ENTRIES_FIELD_WIDTH = 80;

	private static final int CL_ENTRIES_FIELD_HEIGHT = 50;

	private Text rpm_nameText;

	private Text rpm_ChangelogEntriesText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public RPMPropertyPage2() {
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

		Label rpmChangelogEntriesLabel = new Label(composite, SWT.NONE);
		rpmChangelogEntriesLabel.setText(RPM_CHANGELOG_ENTRIES);
		rpm_ChangelogEntriesText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdEntries = new GridData();
		gdEntries.widthHint = convertWidthInCharsToPixels(CL_ENTRIES_FIELD_WIDTH);
		gdEntries.heightHint = convertWidthInCharsToPixels(CL_ENTRIES_FIELD_HEIGHT);
		rpm_ChangelogEntriesText.setLayoutData(gdEntries);

		// Populate RPM text fields
		String rpm_path = ((IResource) getElement()).getRawLocation()
				.toString();

		String rpm_ChangelogEntries = LinuxShellCmds
				.getInfo("/bin/rpm --changelog -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_ChangelogEntriesText.setText(rpm_ChangelogEntries);

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