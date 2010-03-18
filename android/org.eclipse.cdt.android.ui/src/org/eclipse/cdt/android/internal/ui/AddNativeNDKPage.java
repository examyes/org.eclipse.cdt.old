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
public class AddNativeNDKPage extends WizardPage {

	public AddNativeNDKPage() {
		super("ndkPage");
		setTitle("Build");
		setDescription("Settings for adjusting the build");
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
	}

}
