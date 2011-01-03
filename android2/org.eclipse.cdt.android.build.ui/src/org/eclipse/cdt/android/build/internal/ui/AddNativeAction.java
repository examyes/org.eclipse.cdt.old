package org.eclipse.cdt.android.build.internal.ui;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class AddNativeAction implements IObjectActionDelegate {

	private IWorkbenchPart part;
	private ISelection selection;

	@Override
	public void run(IAction action) {
		IProject project = null;
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1) {
				Object obj = ss.getFirstElement();
				if (obj instanceof IProject) {
					project = (IProject)obj;
				} else if (obj instanceof PlatformObject) {
					project = (IProject)((PlatformObject)obj).getAdapter(IProject.class);
				}
			}
		}
		
		if (project != null) {
			AddNativeWizard wizard = new AddNativeWizard(project);
			WizardDialog dialog = new WizardDialog(part.getSite().getShell(), wizard);
			dialog.open();
		}
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection; 
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}

}
