package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/BreakpointWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 15:58:49)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.ContainerContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.debug.WorkspaceSourceLocator;
import com.ibm.debug.internal.pdt.ui.util.DialogField;
import com.ibm.debug.internal.pdt.ui.util.IStringButtonAdapter;
import com.ibm.debug.internal.pdt.ui.util.MGridLayout;
import com.ibm.debug.internal.pdt.ui.util.StringButtonDialogField;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLUtils;

public abstract class BreakpointWizardPage extends WizardPage implements IStringButtonAdapter{


	protected StringButtonDialogField projectField;
	protected IProject selectedProject;
	protected IWorkspaceRoot root;
	protected Composite composite;

	boolean editing = false;  //is the page in "edit mode"?
	boolean flipped = false; //flag: page only flips the first time dialog is visible
	IMarker existingBP;  //breakpoint user is editing

	/** Constructor */
	protected BreakpointWizardPage(String pageName, String title, ImageDescriptor titleImage)
	{
		super(pageName, title, titleImage);
		setTitle(title);
		if(titleImage !=null)
			setImageDescriptor(titleImage);
	}
	/** Constructor when dialog used for editing*/
	protected BreakpointWizardPage(String pageName, String title, ImageDescriptor titleImage, IMarker breakpoint)
	{
		super(pageName, title, titleImage);
		setTitle(title);
		if(titleImage !=null)
			setImageDescriptor(titleImage);

		editing = true;
		existingBP = breakpoint;
	}


	protected abstract void createRequiredFields();

	/**
	 * We want to start on the second page when editing. This is an awkward place to change the page, but the only
	 * spot that works. It will only flip the first time the dialog is setVisible.
	 */
	public void setVisible(boolean visible)
	{
		if(getNextPage() == null)
			return;
		if(!flipped && visible && editing && getNextPage() instanceof ConditionalBreakpointWizardPage)
		{
			((ConditionalBreakpointWizardPage)getNextPage()).flipToMe();
			flipped = true;
		}
		else super.setVisible(visible);
	}


	/**
	 * @see DialogPage#createControl(Composite)
	 */

	public void createControl(Composite parent) {
		createRequiredFields();

		composite= new Composite(parent, SWT.NONE);

		MGridLayout layout= new MGridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		layout.minimumWidth= 400;
		layout.minimumHeight= 200;
		layout.numColumns= 3;
		composite.setLayout(layout);

		setControl(composite);

		projectField.postSetFocusOnDialogField(parent.getDisplay());

	}



	/**
	 * @see IStringButtonAdapter#changeControlPressed(DialogField)
	 *  The browse button was pressed. May need to be overridden if dialog has
	 *  more than one browse button.
	 */
	public void changeControlPressed(DialogField field){
		if (field == projectField)
		{
			selectedProject = chooseProject();
			if (selectedProject!=null && selectedProject.getName()!=null)
				projectField.setText(selectedProject.getName());
		}
	}


	/**
	 * Returns the WorkspaceRoot
	 */
	public IWorkspaceRoot getWorkspaceRoot() {
		if(root==null)
			return root = WorkbenchPlugin.getPluginWorkspace().getRoot();
		else return root;
	}

	public IProject getProjectResource()
	{
		String projectName = projectField.getText();
		IProject projects[] = getWorkspaceRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if(projects[i].getName().equals(projectName))
				return projects[i];
		}
		return null;
	}

	/**
	 * Returns the name of the project associated with the currently selected debug target.
	 * Intended for use in prefilling the project text field.
	 */
	public String getNameOfCurrentSelectedProject()
	{
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null)
			return "";
		IWorkbenchPage p= window.getActivePage();
		if (p == null)
			return "";

		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);

		if (view != null)
		{
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null)
			{
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection)
				{
					Object firstElement = ((IStructuredSelection)selection).getFirstElement();
					if(firstElement != null && firstElement instanceof PICLDebugElement)
					{
						ISourceLocator locator = ((PICLDebugElement)firstElement).getDebugTarget().getLaunch().getSourceLocator();
						if(locator instanceof WorkspaceSourceLocator)
						{
							IProject project=((WorkspaceSourceLocator)locator).getHomeProject();

							if (project !=null)
								return project.getName();
						}
					}
				}


			}
		}

		return "";
	}


	protected IProject chooseProject() {

		/*Class[] acceptedClasses= new Class[] { IPackageFragmentRoot.class, IJavaProject.class };
		ISelectionValidator validator= new TypedElementSelectionValidator(acceptedClasses, false) {
			public boolean isSelectedValid(Object element) {
				try {
					if (element instanceof IJavaProject) {
						IJavaProject jproject= (IJavaProject)element;
						IPath path= jproject.getProject().getFullPath();
						return (jproject.findPackageFragmentRoot(path) != null);
					} else if (element instanceof IPackageFragmentRoot) {
						return (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE);
					}
					return true;
				} catch (JavaModelException e) {
					ErrorDialog.openError(getShell(), "Error", null, e.getStatus());
				}
				return false;
			}
		};*/

		/*acceptedClasses= new Class[] { IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
		ViewerFilter filter= new TypedViewerFilter(acceptedClasses) {
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (element instanceof IPackageFragmentRoot) {
					try {
						return (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE);
					} catch (JavaModelException e) {
						ErrorDialog.openError(getShell(), "Error", null, e.getStatus());
						return false;
					}
				}
				return super.select(viewer, parent, element);
			}
		};		*/


		ILabelProvider labelProvider = new WorkbenchLabelProvider();
		ContainerContentProvider contentProvider = new ContainerContentProvider();
		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(),
			PICLUtils.getResourceString( "ElementTreeSelectionDialog.title"),null,
			labelProvider, contentProvider, false, true);

		//dialog.setValidator(validator);
		//dialog.setSorter(new PackageViewerSorter());
		dialog.setMessage(PICLUtils.getResourceString("ElementTreeSelectionDialog.description"));
		//dialog.addFilter(filter);

		if(dialog.open(getWorkspaceRoot()) == dialog.OK){
			Object element= dialog.getPrimaryResult();
			if (element instanceof IProject) {
				return (IProject)element;
			}
		}
		//IJavaModel root= JavaCore.create(fWorkspaceRoot);
		/*if (dialog.open(root, initElement) == dialog.OK) {
			Object element= dialog.getPrimaryResult();
			if (element instanceof IJavaProject) {
				IJavaProject jproject= (IJavaProject)element;
				return jproject.getPackageFragmentRoot(jproject.getProject());
			} else if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot)element;
			}
			return null;
		}*/
		return null;
	}

	/**
	 * Restores the values to the fields from the profiles on startup.
	 * The values should be stored using ISettingsWriter#writeSettings.
	 */
	private void restoreSettings(){};

	/**
	 * This method initializes the dialog fields with the values of the existing
	 * breakpoint that the user is editing.
	 */
	private void initUsingOldBreakpoint(){};

} 