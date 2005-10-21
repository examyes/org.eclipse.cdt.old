package org.eclipse.cdt.pdom.ui.popup.actions;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.pdom.core.SQLPDOMUpdaterJob;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class PDOMUpdateProjectAction implements IObjectActionDelegate {

	private ISelection selection;
	
	/**
	 * Constructor for Action1.
	 */
	public PDOMUpdateProjectAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (!(selection instanceof IStructuredSelection))
			return;
		
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i) {
			if (!(objs[i] instanceof ICProject))
				continue;
			
			SQLPDOMUpdaterJob job = new SQLPDOMUpdaterJob((ICProject)objs[i], null);
			job.schedule();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
