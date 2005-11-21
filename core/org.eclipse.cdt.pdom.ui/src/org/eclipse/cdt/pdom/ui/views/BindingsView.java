package org.eclipse.cdt.pdom.ui.views;

import java.io.IOException;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.ILineLocatable;
import org.eclipse.cdt.core.search.IMatchLocatable;
import org.eclipse.cdt.core.search.IOffsetLocatable;
import org.eclipse.cdt.internal.pdom.core.PDOMDatabase;
import org.eclipse.cdt.internal.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.pdom.ui.PDOMUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class BindingsView extends ViewPart {
	
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	public static final String ID = "org.eclipse.cdt.pdom.ui.views.BindingsView";
	
	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements ILazyContentProvider {
		TableViewer viewer;
		PDOMDatabase pdom;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			this.viewer = (TableViewer)v;
			if (newInput != null && newInput instanceof IProject) {
				IPDOM pdom = PDOM.getPDOM((IProject)newInput);
				if (pdom instanceof PDOMDatabase) {
					try {
						this.pdom = (PDOMDatabase)pdom;
						BTreeCounter counter = new BTreeCounter();
						this.pdom.getBindingIndex().visit(counter);
						System.out.println("Binding count: " + counter.count);
						viewer.setItemCount(counter.count);
						return;
					} catch (IOException e) {
					}
				}
			}

			// If everything falls out, we have nothing to view
			pdom = null;
			viewer.setItemCount(0);
		}

		public void dispose() {
		}

		public void updateElement(final int index) {
			try {
				// find the binding at i
				BTreeIndex visitor = new BTreeIndex(index);
				pdom.getBindingIndex().visit(visitor);
				PDOMBinding binding = null;
				if (visitor.result != 0) {
					binding = new PDOMBinding(pdom, visitor.result);
				}
				viewer.replace(binding, index);
			} catch (IOException e) {
			}
		}
	}

	private static class BTreeCounter implements IBTreeVisitor {
		int count;
		public int compare(int record) throws IOException {
			return 1;
		}
		public boolean visit(int record) throws IOException {
			if (record != 0)
				++count;
			return true;
		}
	}
	
	private static class BTreeIndex implements IBTreeVisitor {
		final int index;
		int count;
		int result;
		public BTreeIndex(int index) {
			this.index = index;
		}
		public int compare(int record) throws IOException {
			return 1;
		};
		public boolean visit(int record) throws IOException {
			if (record == 0)
				return true;
			
			if (count++ == index) {
				result = record;
				return false;
			} else
				return true;
		};
	}
	
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj == null) {
				return "null :(";
			} else if (obj instanceof PDOMBinding) {
				return ((PDOMBinding)obj).getName();
			} else
				return obj.toString();
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BindingsView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.VIRTUAL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(null);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BindingsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				PDOMBinding binding = (PDOMBinding)((IStructuredSelection) selection)
						.getFirstElement();
				try {
					PDOMName name = binding.getFirstDeclaration();
					if (name == null)
						return;
					IASTFileLocation loc = name.getFileLocation();
					IPath path = new Path(loc.getFileName());
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
					IEditorPart part;
					if (files.length == 0)
						part = EditorUtility.openInEditor(new FileStorage(path));
					else
						// TODO what if length > 1?
						part = EditorUtility.openInEditor(files[0]);
					((AbstractTextEditor)part).selectAndReveal(loc.getNodeOffset(), loc.getNodeLength());
				} catch (IOException e) {
					PDOMUIPlugin.log(new CoreException(new Status(
							IStatus.ERROR, PDOMUIPlugin.ID, 0,
							"doubleClick", e)));
				} catch (CoreException e) {
					PDOMUIPlugin.log(e);
				}
			}
		};
	}

    protected void open( IResource resource, IMatchLocatable locatable ) throws CModelException, PartInitException {
        IEditorPart part= EditorUtility.openInEditor(resource);
        setSelectionAtOffset(part, locatable);
    }

    protected void setSelectionAtOffset(IEditorPart part, IMatchLocatable locatable) {
        if( part instanceof AbstractTextEditor )
        {
			int startOffset=0;
			int length=0;
		
			if (locatable instanceof IOffsetLocatable){
			    startOffset = ((IOffsetLocatable)locatable).getNameStartOffset();
			    length = ((IOffsetLocatable)locatable).getNameEndOffset() - startOffset;
			} else if (locatable instanceof ILineLocatable){
				int tempstartOffset = ((ILineLocatable)locatable).getStartLine();
				
				IDocument doc =  ((AbstractTextEditor) part).getDocumentProvider().getDocument(part.getEditorInput());
				try {
					//NOTE: Subtract 1 from the passed in line number because, even though the editor is 1 based, the line
					//resolver doesn't take this into account and is still 0 based
					startOffset = doc.getLineOffset(tempstartOffset-1);
					length=doc.getLineLength(tempstartOffset-1);
				} catch (BadLocationException e) {}			
				
				//Check to see if an end offset is provided
				int tempendOffset = ((ILineLocatable)locatable).getEndLine();
				//Make sure that there is a real value for the end line
				if (tempendOffset>0 && tempendOffset>tempstartOffset){
					try {
						//See NOTE above
						int endOffset = doc.getLineOffset(tempendOffset-1);
						length=endOffset - startOffset;
					} catch (BadLocationException e) {}		
				}
					
			}
            try {
            ((AbstractTextEditor) part).selectAndReveal(startOffset, length);
            } catch (Exception e) {}
        }
    }

    private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Bindings", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void showProject(IProject project) {
		viewer.setInput(project);
	}
}