/**
 * 
 */
package org.eclipse.cdt.android.internal.ui;

import org.eclipse.cdt.managedbuilder.ui.wizards.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author dschaefer
 *
 */
public class AddNativeProjectPage extends WizardPage {

	private final IProject project;
	private Text libraryName;
	
	public AddNativeProjectPage(IProject project) {
		super("projectPage");
		setTitle("Project");
		setDescription("Settings for adding native support to the project");
		
		this.project = project;
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		addLibraryName(comp);
		
		setControl(comp);
	}

	private void addLibraryName(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText("Library name (lib*.so will be added)");
		
		libraryName = new Text(group, SWT.BORDER);
		libraryName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		libraryName.setText(project.getName());
	}

	public String getLibraryName() {
		return libraryName.getText();
	}
	
}
