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

public class RPMPropertyPage3 extends PropertyPage {

	private static final String PATH_TITLE = "Path:"; //$NON-NLS-1$

	private static final String RPM_NAME = "Name:"; //$NON-NLS-1$

	private static final String RPM_ARCH = "Architecture:"; //$NON-NLS-1$

	private static final String RPM_PLATFORM = "Platform:"; //$NON-NLS-1$

	private static final String RPM_OS = "OS built for:"; //$NON-NLS-1$

	private static final String RPM_HOST = "Host package was built on:"; //$NON-NLS-1$

	private static final String RPM_TIME = "Time package was built:"; //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int ARCH_FIELD_WIDTH = 8;

	private static final int PLATFORM_FIELD_WIDTH = 20;

	private static final int OS_FIELD_WIDTH = 10;

	private static final int HOST_FIELD_WIDTH = 40;

	private static final int TIME_FIELD_WIDTH = 35;

	private Text rpm_nameText;

	private Text rpm_archText;

	private Text rpm_platformText;

	private Text rpm_osText;

	private Text rpm_hostText;

	private Text rpm_timeText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public RPMPropertyPage3() {
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

		Label rpmArchLabel = new Label(composite, SWT.NONE);
		rpmArchLabel.setText(RPM_ARCH);
		rpm_archText = new Text(composite, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		GridData gdArch = new GridData();
		gdArch.widthHint = convertWidthInCharsToPixels(ARCH_FIELD_WIDTH);
		rpm_archText.setLayoutData(gdArch);

		Label rpmPlatformLabel = new Label(composite, SWT.NONE);
		rpmPlatformLabel.setText(RPM_PLATFORM);
		rpm_platformText = new Text(composite, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		GridData gdPlatform = new GridData();
		gdPlatform.widthHint = convertWidthInCharsToPixels(PLATFORM_FIELD_WIDTH);
		rpm_platformText.setLayoutData(gdPlatform);

		Label rpmOSLabel = new Label(composite, SWT.NONE);
		rpmOSLabel.setText(RPM_OS);
		rpm_osText = new Text(composite, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		GridData gdOS = new GridData();
		gdOS.widthHint = convertWidthInCharsToPixels(OS_FIELD_WIDTH);
		rpm_osText.setLayoutData(gdOS);

		Label rpmHostLabel = new Label(composite, SWT.NONE);
		rpmHostLabel.setText(RPM_HOST);
		rpm_hostText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.WRAP);
		GridData gdHost = new GridData();
		gdHost.widthHint = convertWidthInCharsToPixels(HOST_FIELD_WIDTH);
		rpm_hostText.setLayoutData(gdHost);

		Label rpmTimeLabel = new Label(composite, SWT.NONE);
		rpmTimeLabel.setText(RPM_TIME);
		rpm_timeText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.WRAP);
		GridData gdTime = new GridData();
		gdTime.widthHint = convertWidthInCharsToPixels(TIME_FIELD_WIDTH);
		rpm_timeText.setLayoutData(gdTime);

		// Populate RPM text fields
		String rpm_path = ((IResource) getElement()).getRawLocation()
				.toString();
		String rpm_arch = LinuxShellCmds.getInfo("/bin/rpm --qf %{ARCH} -qp " + //$NON-NLS-1$
				rpm_path);
		rpm_archText.setText(rpm_arch);
		String rpm_platform = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{PLATFORM} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_platformText.setText(rpm_platform);
		String rpm_os = LinuxShellCmds.getInfo("/bin/rpm --qf %{OS} -qp " + //$NON-NLS-1$
				rpm_path);
		rpm_osText.setText(rpm_os);
		String rpm_host = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{BUILDHOST} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_hostText.setText(rpm_host);
		String rpm_time = LinuxShellCmds
				.getInfo("/bin/rpm --qf %{BUILDTIME:date} -qp " + //$NON-NLS-1$
						rpm_path);
		rpm_timeText.setText(rpm_time);

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