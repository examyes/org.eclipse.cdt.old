/**
 * 
 */
package org.eclipse.cdt.pdom.ui.popup.actions;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.pdom.ui.views.BindingsView;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

/**
 * @author dschaefer
 *
 */
public class ShowInBindingsView implements IViewActionDelegate {

	IViewPart view;
	IProject project;
	
	public void init(IViewPart view) {
		this.view = view;
	}

	public void run(IAction action) {
		try { 
			IViewPart bindingsView = view.getSite().getPage().showView(BindingsView.ID);
			if (bindingsView instanceof BindingsView) {
				((BindingsView)bindingsView).showProject(project);
			}
		} catch (PartInitException e) {
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection &&
                ((IStructuredSelection)selection).getFirstElement() instanceof ICProject) {
                project = ((ICProject)((IStructuredSelection)selection).getFirstElement()).getProject();
            }
	}

}
