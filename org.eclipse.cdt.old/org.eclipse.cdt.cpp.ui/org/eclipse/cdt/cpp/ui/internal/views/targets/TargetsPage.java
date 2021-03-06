package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySheetEntryListener;
import org.eclipse.ui.views.properties.IPropertySheetPage; 
import org.eclipse.ui.views.properties.IPropertySourceProvider;
 
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.core.runtime.Platform;
import java.net.*;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.*;
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;

public class TargetsPage extends Page implements IPropertySheetPage {
	
	private TargetsViewer viewer;
	private IPropertySheetEntry rootEntry;
	private IPropertySourceProvider provider;
	
	public static final String HELP_CONTEXT_TARGETS_PAGE = "com.ibm.eclipse.ui.target_page_help_context";

	protected ActionBuildTarget buildAction;
	protected ActionNewTarget newAction;
	protected ActionRemoveAllTarget removeAllAction;
	protected ActionRemoveTarget removeAction;

	// persistance
	public TargetsStore targetStore;	
	//NL Enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	private final String NEW_ACTION_KEY = "TargetsViewer.Action.New_Target";
	private final String BUILD_ACTION_KEY = "TargetsViewer.Action.Build_Target";
	private final String REMOVE_ACTION_KEY = "TargetsViewer.Action.Remove_Target";
	private final String REMOVE_ALL_ACTION_KEY = "TargetsViewer.Action.Remove_All_Targets";
	private final String HOVER_NEW_ACTION_KEY = "TargetsViewer.Hover.New_Target";
	private final String HOVER_BUILD_ACTION_KEY = "TargetsViewer.Hover.Build_Target";
	private final String HOVER_REMOVE_ACTION_KEY = "TargetsViewer.Hover.Remove_Target";
	private final String HOVER_REMOVE_ALL_ACTION_KEY = "TargetsViewer.Hover.Remove_All_Targets";

    public TargetsPage() 
    {
	super();
	targetStore = TargetsStore.getInstance();	
    }

    /**
     * Creates and configures the TargetsViewer 
     * for this page
     **/
    public void createControl(Composite parent) 
    {
	viewer = new TargetsViewer(parent,this);
	
	// set the model for the viewer
	if (rootEntry == null) 
	    {
		// create a new root
		TargetsEntry root = new TargetsEntry();
		if (provider != null)
		    // set the property source provider
		    root.setPropertySourceProvider(provider);
		rootEntry = root;
	    }
	viewer.setRootEntry(rootEntry);
	
	// each time you start up targets view, initialize root list
	initTargetsStore(targetStore);
	
	//create the actions
	newAction = new ActionNewTarget(this);
	removeAction = new ActionRemoveTarget(this);
	buildAction = new ActionBuildTarget(this);
	removeAllAction = new ActionRemoveAllTarget(this);
	
	
	// navigatorSelection is a global variable - use set/get  instead
	//Object root = getNavigatorSelection();
	Object root  = getProjectInput();
	if(root!=null && (root instanceof IProject))
	    {
		newAction.setEnabled(true);
	    }
    }

    /**
     * Returns the SWT control for this page
     *
     * @return the SWT control for this page
     */
    public Control getControl() 
    {
	if(viewer == null)
	    return null;
	return viewer.getControl();
    }
    
    /**
     * Returns the image descriptor with the given relative path.
     */
    private ImageDescriptor getImageDescriptor(String relativePath) 
    {
	try {
	    AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	    URL installURL = plugin.getDescriptor().getInstallURL();
		URL url = new URL(installURL, "icons/basic/" + relativePath);
		return ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
	    // Should not happen
	    return null;
	}
    }
    
    protected int getRootIndex(IProject root)  
    {
	for(int i = 0; i< targetStore.projectList.size(); i++)
	    {
		RootElement obj = (RootElement)targetStore.projectList.elementAt(i);
		IProject project = obj.getRoot();

		if(root.getName().equals(project.getName()))
		    {
			return i;
		    }
	    }

	return -1;
    }

    private Vector getTargetsList(Object root) 
    {
 	return null;
    }

    /**
     * Returns the SWT control for this page
     *
     * @return the SWT control for this page
     */
    public TargetsViewer getViewer() 
    {
	return this.viewer;
    }


    /*
     */
    private void initTargetsStore(TargetsStore store) 
    {
	store.projectList.removeAllElements();

	// local projects
	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	if(projects.length > 0)
	    {
 		for(int i = 0; i < projects.length; i++)
		    {			
			store.projectList.add(new RootElement(projects[i], null));
		    }
	    }

	// remote projects
	RemoteProjectAdapter rmtAdapter = RemoteProjectAdapter.getInstance();
	if (rmtAdapter != null)
	    {
		IProject[] rprojects = rmtAdapter.getProjects();
		
		if (rprojects != null)
		    {
			for (int j = 0; j < rprojects.length; j++)
			    {	
				store.projectList.add(new RootElement(rprojects[j], null));
			    }
		    }
	    }

	// updating root elements
	for(int y = 0; y < store.projectList.size(); y++)
	{
		RootElement root = (RootElement)store.projectList.elementAt(y);
		IProject project = root.getRoot();

		// look for that root in the persistence store and if found do update
		java.util.ArrayList savedList = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty(project, root.getName());
		for(int z = 0; z < savedList.size(); z++)
		{
			TargetElement target = new TargetElement(savedList.get(z++).toString(),
			savedList.get(z++).toString(), savedList.get(z).toString(),root);
			root.setProprtyDescriptor();
			target.setParent(root);
			root.add(target);
		}
	}
}
/**
 * Make action objects.
 */
private void makeActions() {
		
	// new
	newAction.setText(pluginInstance.getLocalizedString(NEW_ACTION_KEY));
	newAction.setToolTipText(pluginInstance.getLocalizedString(HOVER_NEW_ACTION_KEY));
	newAction.setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_ADD);
	newAction.setEnabled(false);

	// remove

	removeAction.setText(pluginInstance.getLocalizedString(REMOVE_ACTION_KEY));
	removeAction.setToolTipText(pluginInstance.getLocalizedString(HOVER_REMOVE_ACTION_KEY));
	removeAction.setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_REM);
	removeAction.setEnabled(false);

	
	// build

	buildAction.setText(pluginInstance.getLocalizedString(BUILD_ACTION_KEY));
	buildAction.setToolTipText(pluginInstance.getLocalizedString(HOVER_BUILD_ACTION_KEY));
	buildAction.setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_BUILD);
	buildAction.setEnabled(false);

		
	// remove All

	removeAllAction.setText(pluginInstance.getLocalizedString(REMOVE_ALL_ACTION_KEY));
	removeAllAction.setToolTipText(pluginInstance.getLocalizedString(HOVER_REMOVE_ALL_ACTION_KEY));
	removeAllAction.setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_REMALL);
	removeAllAction.setEnabled(false);

}
/* (non-Javadoc)
 * Method declared on IPage (and Page).
 */
public void makeContributions(
	IMenuManager menuManager,
	IToolBarManager toolBarManager,
	IStatusLineManager statusLineManager) {

	// make the actions
	makeActions();

	// add actions to the menu manager
	menuManager.add(newAction);
	menuManager.add(buildAction);
	menuManager.add(removeAction);
	menuManager.add(new Separator());
	menuManager.add(removeAllAction);
	
	// add actions to the tool bar
	toolBarManager.add(newAction);
	toolBarManager.add(buildAction);
	toolBarManager.add(removeAction);
	toolBarManager.add(new Separator());
	toolBarManager.add(removeAllAction);

	// set status line manager into the viewer
	viewer.setStatusLineManager(statusLineManager);

	// setting the context menu
	MenuManager mgr = new MenuManager();
	Menu menu = mgr.createContextMenu(viewer.tableTree.getTable());
	viewer.tableTree.getTable().setMenu(menu);

	mgr.removeAll();
	mgr.add(newAction);
	mgr.add(buildAction);
	mgr.add(removeAction);
	mgr.add(new Separator());
	mgr.add(removeAllAction);
	

}

    /**
     * Updates the model for the viewer.
     * <p>
     * Note that this means ensuring that the model reflects the state
     * of the current viewer input. 
     * </p>
     */
    public void refresh() 
    {
	if (viewer == null)
	    return;
	// calling setInput on the viewer will cause the model to refresh
	viewer.setInput(viewer.getInput());
    }

    public IProject getProjectInput()
    {
	return pluginInstance.getCurrentProject();
    }

    protected boolean rootExists(Object root)  
    {
	for(int i = 0; i< targetStore.projectList.size(); i++)
	    {
		RootElement obj = (RootElement)targetStore.projectList.elementAt(i);
		String name = new String(obj.getName());
		if(name.equals(((IProject)root).getName()))
		    return true;
	    }
	return false;
    }

    /**
     * Notify the viewer that selection has changed.
     */
    public final void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {	
	if (part instanceof org.eclipse.cdt.cpp.ui.internal.views.CppProjectsViewPart || 
	    part instanceof org.eclipse.ui.views.navigator.ResourceNavigator)
	    {
		if (viewer == null)
		    return;
		
		IStructuredSelection es = (IStructuredSelection)sel;
		Object object = es.getFirstElement();
		
		if (object != null)
		    {
			IProject project = null;
			if (object instanceof IResource)
			    {
				project = ((IResource)object).getProject();
			    }
			else if (object instanceof DataElement)
			    {
				ModelInterface api = ModelInterface.getInstance();
				DataElement projectElement = api.getProjectFor((DataElement)object);
				if (projectElement != null)
				    {
					project = api.findProjectResource(projectElement);
				    }
			    }
			
		
			// populate the viewer with existing projects
			// this code is for populating the viewer with existing targets
			// to be moved in a separte method
			updateTargetsStore(targetStore); // new
						
			if (project != null)	
			    {
				// the following statement has been put in the TargetsPage.createPage mas well
				newAction.setEnabled(true);
				buildAction.setEnabled(false);
				removeAction.setEnabled(false);
				removeAllAction.setEnabled(false);
			    }
		
			// set the input to be the targets associated witht the navigator selection
			// I could use the selection object from the function header instead
			if(project != null)
			    {
				java.util.List list = new ArrayList();
				int index = getRootIndex(project);
				if(index>-1)
				    {
					RootElement root = (RootElement)targetStore.projectList.elementAt(index);
					list.add(root);
					// enabling the relevant target actions
					if(root.getTargets().size()>0)
					    removeAllAction.setEnabled(true);
					// end
					viewer.setInput(list.toArray());
				    }
				else
				    {
					viewer.setInput(list.toArray()); // empty list
				    }
			    }
		    }
	    }
    }

    /**
     * Sets focus to a part in the page.
     */
    public void setFocus() 
    {
	viewer.getControl().setFocus();
    }

/**
 * Sets the given property source provider as
 * the property source provider
 * <p>
 * Calling this method is only valid if you are using
 * this page's defualt root entry
 * </p>
 * @param newProvider the property source provider
 */
public void setPropertySourceProvider(IPropertySourceProvider newProvider) {
	provider = newProvider;
	if (rootEntry instanceof TargetsEntry) {
		((TargetsEntry)rootEntry).setPropertySourceProvider(provider);
		// the following will trigger an update
		viewer.setRootEntry(rootEntry);
	}
}
/**
 * Sets the given entry as the model for the page.
 *
 * @param entry the root entry
 */
public void setRootEntry(IPropertySheetEntry entry) {
	rootEntry = entry;
	if (viewer != null)
		// the following will trigger an update
		viewer.setRootEntry(rootEntry); 
}
/*
*/
private void updateTargetsStore(TargetsStore oldStore) {

	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

	RemoteProjectAdapter rmtAdapter = RemoteProjectAdapter.getInstance();
	IProject[] rprojects = rmtAdapter.getProjects();		

	// removing project list if the project has been deleted
	if(projects.length==0 && rprojects.length==0)
	{
		oldStore.projectList.removeAllElements();
		viewer.setInput(new ArrayList().toArray());
		removeAction.setEnabled(false);
		buildAction.setEnabled(false);
		removeAllAction.setEnabled(false);
		newAction.setEnabled(false);
		return;
	}
		
	for (int i = 0; i < oldStore.projectList.size(); i++) 
	{
		boolean found = false;
		// compare locals
		for (int y = 0; y < projects.length; y++) 
		{
			RootElement root = (RootElement) oldStore.projectList.elementAt(i);
			if (root.getName().equals(projects[y].getName())) 
				found = true;
		}
		// compare remotes
		for (int z = 0; z < rprojects.length; z++) 
		{
		    RootElement root = (RootElement) oldStore.projectList.elementAt(i);
		    if (root.getName().equals(rprojects[z].getName())) 
			found = true;
		}
		if (!found ) 
		{
			// remove the project from the list
			oldStore.projectList.removeElementAt(i);
			if(oldStore.projectList.size()==0)
				oldStore.projectList.removeAllElements();
		}
	}
}
}
