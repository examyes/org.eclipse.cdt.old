package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
//import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.*;
/**
 * Main class for the Property Sheet View.
 * <p>
 * This standard view has id <code>"com.ibm.eclipse.ui.views.PropertySheet"</code>.
 * </p>
 * <p>
 * Note that property <it>sheets</it> and property sheet pages are not the
 * same thing as property <it>dialogs</it> and their property pages (the property
 * pages extension point is for contributing property pages to property dialogs).
 * Within the property sheet view, all pages are <code>IPropertySheetPage</code>s.
 * </p>
 * <p>
 * Property sheet pages are discovered by the property sheet view automatically
 * when a part is first activated. The property sheet view asks the active part
 * for its property sheet page; this is done by invoking 
 * <code>getAdapter(IPropertySheetPage.class)</code> on the part. If the part 
 * returns a page, the property sheet view then creates the controls for that
 * property sheet page (using <code>createControl</code>), and adds the page to 
 * the property sheet view. Whenever this part becomes active, its corresponding
 * property sheet page is shown in the property sheet view (which may or may not
 * be visible at the time). A part's property sheet page is discarded when the
 * part closes. The property sheet view has a default page (an instance of 
 * <code>PropertySheetPage</code>) which services all parts without a property
 * sheet page of their own.
 * </p>
 * <p>
 * The workbench will automatically instantiates this class when a Property
 * Sheet view is needed for a workbench window. This class is not intended
 * to be instantiated or subclassed by clients.
 * </p>
 *
 * @see IPropertySheetPage
 * @see PropertySheetPage
 */
public class Targets extends PageBookView implements ISelectionListener {
	private TargetsPage page;
		/**
	 * Help context id used for the property sheet view
	 * (value <code>"com.ibm.eclipse.ui.general_help_context"</code>).
	 */
	public static final String HELP_CONTEXT_TARGETS_VIEW = "com.ibm.eclipse.ui.general_help_context";

	/**
	 * Register the adapters for the standard properties.
	 */
	static {
		registerAdapters();
	}

/**
 * Create a <code>ViewPart</code> for the <code>PropertySheet</code>
 * No other viewer should use this view.
 */
public Targets() {
	super();
}
/**
 * Returns the default page.
 *
 * @return the default targets page.
 */
protected IPage createDefaultPage(PageBook book) {
	
	//IToolBarManager localToolBarManager = getSite().getActionBars().getToolBarManager();
	page = new TargetsPage();
	page.createControl(book);

	return page;
}
/**
 * The <code>PropertySheet</code> implementation of this <code>IWorkbenchPart</code>
 * method creates a <code>PageBook</code> control with its default page showing.
 */
public void createPartControl(Composite parent) {
	super.createPartControl(parent);
//	WorkbenchHelp.setHelp(getPageBook(), new Object[] {HELP_CONTEXT_PROPERTY_SHEET_VIEW});
}
/**
 * Disposes this part and discards all part state.  From this point on
 * the part will not be referenced within the desktop.
 *
 * @see IDesktopPart
 */
public void dispose() {
	// run super.
	super.dispose();
	
	//persist targets for each project in the navigator
	if ((page != null) && (page.targetStore != null))
	    {
	java.util.Vector projects =  page.targetStore.projectList;
	for(int i = 0;i < projects.size(); i++)
	{
		java.util.ArrayList list = new java.util.ArrayList();
		int listCounter = 0;
		
		RootElement root = (RootElement)projects.elementAt(i);
		for (int y=0; y < root.getTargets().size(); y++)
		{
			TargetElement target = (TargetElement)root.getTargets().elementAt(y);
			list.add(listCounter++,target.getName());
			list.add(listCounter++,target.getWorkingDirectory());
			list.add(listCounter++,target.getMakeInvocation());
		}
		com.ibm.cpp.ui.internal.CppPlugin.writeProperty(root.getRoot(),root.getName(),list);

		// check if persistence has been worked properly 
		java.util.ArrayList savedList = com.ibm.cpp.ui.internal.CppPlugin.readProperty(root.getRoot(),root.getName());
		/*for(int z = 0; z < savedList.size(); z++)
		{
			System.out.println("\n Persisting......"+ savedList.get(z));
		}
	
		System.out.println("\n Persisting......");*/
	}
	    }
	// remove ourselves as a selection service listener
	getSite().getPage().removeSelectionListener(this);
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 */
protected PageRec doCreatePage(IWorkbenchPart part) { 
	// Try to get a custom property sheet page.
	IPropertySheetPage page = (IPropertySheetPage)part.getAdapter(IPropertySheetPage.class);
	if (page != null) {
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	// Use the default page		
	return null;
}
/**
 * Destroys a page in the pagebook.
 * <p>
 * Subclasses of PageBookView must implement the creation and
 * destruction of pages in the view.  This method should be implemented
 * by the subclass to destroy a page for the given part.
 * </p>
 * @param part the input part
 * @rec a page record for the part
 */
protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
	IPropertySheetPage page = (IPropertySheetPage) rec.page;
	page.dispose();
	rec.dispose();
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 * Returns the active editor on the same workbench page as this property 
 * sheet view.
 */
protected IWorkbenchPart getBootstrapPart() {
	IWorkbenchPage page = getSite().getPage(); 
	if (page != null)
		return page.getActiveEditor();
	else
		return null;
}
/**
 * Answer the active desktop part.
 */
protected TargetsPage getTargetsPage() {
	return this.page;
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void init(IViewSite site) throws PartInitException {
	site.getPage().addSelectionListener(this);
	super.init(site);
}
/**
 * Since the property sheet may show properties for any view we
 * are interested in any other view
 */
protected boolean isImportant(IWorkbenchPart part) {
	return part != this;
}
/**
 * The <code>PropertySheet</code> implementation of this <code>IPartListener</code>
 * method first sees if the active part is an <code>IContributedContentsView</code>
 * adapter and if so, asks it for its contributing part.
 */
public void partActivated(IWorkbenchPart part) {
	IContributedContentsView view = (IContributedContentsView)part.getAdapter(IContributedContentsView.class);
	IWorkbenchPart source = null;
	if (view != null)
		source = view.getContributingPart();
	if (source != null) 
		super.partActivated(source);
	else
		super.partActivated(part);
}
/**
 * Registers the adapters for the standard properties.
 */
static void registerAdapters() {
	IAdapterManager manager = Platform.getAdapterManager();
	IAdapterFactory factory = new StandardPropertiesAdapterFactory();
	manager.registerAdapters(factory, IWorkspace.class);
	manager.registerAdapters(factory, IWorkspaceRoot.class);
	manager.registerAdapters(factory, IProject.class);
	manager.registerAdapters(factory, IFolder.class);
	manager.registerAdapters(factory, IFile.class);
	manager.registerAdapters(factory, IMarker.class);
}
/* (non-Javadoc)
 * Method declared on ISelectionListener.
 * Notify the current page that the selection has changed.
 */
public void selectionChanged(IWorkbenchPart part, ISelection sel) {
	// we ignore our own selection or null selection
	if (part == this || sel == null)
		return;
	
	// pass the selection to the page		
	IPropertySheetPage page = (IPropertySheetPage)getCurrentPage();
	if(page != null)
		page.selectionChanged(part, sel);
}
}
