/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.cdt.rpm.ui.propertypage;

import org.eclipse.cdt.rpm.core.utils.RPMQuery;
import org.eclipse.cdt.rpm.ui.util.ExceptionHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class ChangelogPage extends PropertyPage {

	private static final String RPM_CHANGELOG_ENTRIES = 
		Messages.getString("ChangelogPage.entries"); //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int CL_ENTRIES_FIELD_WIDTH = 80;

	private static final int CL_ENTRIES_FIELD_HEIGHT = 50;

	private Text rpm_nameText;

	private Text rpm_ChangelogEntriesText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public ChangelogPage() {
		super();
	}

	private void addChangelogField(Composite parent) {
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

		try {
			String rpm_ChangelogEntries = RPMQuery.getChangelog((IFile) getElement());
			rpm_ChangelogEntriesText.setText(rpm_ChangelogEntries);
		} catch(CoreException e) {
			ExceptionHandler.handle(e, getShell(),
					Messages.getString("ErrorDialog.title"), e.getMessage());
		}

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

		addChangelogField(composite);
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