/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

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

public class RPMPropertyPage extends PropertyPage {

	private static final String PATH_TITLE = "Path:"; //$NON-NLS-1$

	private static final String RPM_NAME = "Name:"; //$NON-NLS-1$

	private static final String RPM_QI = "rpm -qi:"; //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int QI_FIELD_WIDTH = 80;

	private static final int QI_FIELD_HEIGHT = 40;

	private Text rpm_nameText;

	private Text rpm_qiText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public RPMPropertyPage() {
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

		Label rpmDescriptionLabel = new Label(composite, SWT.NONE);
		rpmDescriptionLabel.setText(RPM_QI);
		rpm_qiText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.WRAP);
		GridData gdQI = new GridData();
		gdQI.widthHint = convertWidthInCharsToPixels(QI_FIELD_WIDTH);
		gdQI.heightHint = convertWidthInCharsToPixels(QI_FIELD_HEIGHT);
		rpm_qiText.setLayoutData(gdQI);

		// Populate RPM text fields
		String rpm_path = ((IResource) getElement()).getRawLocation()
				.toString();

		String rpm_qi = LinuxShellCmds.getInfo("/bin/rpm -qi -qp " + //$NON-NLS-1$
				rpm_path);
		rpm_qiText.setText(rpm_qi);

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