/**
 * 
 */
package org.eclipse.cdt.android.internal.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author dschaefer
 *
 */
public class AddNativeProjectPage extends WizardPage {

	public AddNativeProjectPage() {
		super("projectPage");
		setTitle("Project");
		setDescription("Settings for adding native support to the project");
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
	}

}
