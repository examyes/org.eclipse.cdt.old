package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;

import com.ibm.cpp.ui.internal.vcm.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.ui.ILinkable;
import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.*;

import org.eclipse.ui.help.*;
import org.eclipse.help.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.*;

import org.eclipse.vcm.internal.core.base.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

public class RemoteProjectViewPart extends ViewPart 
    implements ISetSelectionTarget, IMenuListener, ICppProjectListener
{
    public class ResourceElementAction extends Action
    {
	private String _name;
	private String _command;

	public ResourceElementAction(String name, String command)
	{
	    super(name);
	    _name = name;
	    _command = command;
	}

	public void run()
	{
	    DataElement selected = _viewer.getSelected();
	    DataStore dataStore = selected.getDataStore();
	    DataElement descriptor = dataStore.localDescriptorQuery(selected.getDescriptor(), _command);
	    if (descriptor != null)
		{
		    UICommandAction cmdAction = new UICommandAction(selected, _name, descriptor, dataStore);
		    cmdAction.run();
		}
	}	
    }
    

    private RemoteProjectNavigator _viewer;
    
    private AddBookmarkAction addBookmarkAction;
    private BuildAction buildAction;
    private BuildAction rebuildAllAction;
    private CloseResourceAction closeResourceAction;
    private CopyResourceAction copyResourceAction;
    
    private ResourceElementAction createFolderAction;
    private ResourceElementAction createFileAction;
    private ResourceElementAction renameResourceAction;

    private DeleteResourceAction deleteResourceAction;
    
    
    private OpenFileAction openFileAction;
    private OpenResourceAction openResourceAction;
    private OpenSystemEditorAction openSystemEditorAction; 
    private PropertyDialogAction propertyDialogAction;
    private RefreshAction localRefreshAction;
    
    private CopyProjectAction copyProjectAction;
    private MoveProjectAction moveProjectAction;
    private MoveResourceAction moveResourceAction;
    private NewWizardAction newWizardAction;
    
    private RemoteProjectAdapter _root;
    
    static private RemoteProjectViewPart _instance;

    private CppPlugin _plugin;

    public RemoteProjectViewPart() 
    {
	super(); 
	_instance = this;
    }

    static public RemoteProjectViewPart getInstance()
    {
	return _instance;
    }
 
  public void createPartControl(Composite container)
  {
      _plugin = CppPlugin.getPlugin();
    Tree tree = new Tree(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    //WorkbenchHelp.setHelp(tree, new ViewContextComputer(this, "project_objects_view_context"));
    _viewer = new RemoteProjectNavigator(tree);    
    updateViewBackground();
    updateViewForeground();
    updateViewFont();

    _plugin.getDataStore().getDomainNotifier().addDomainListener(_viewer);

    makeActions();
    

    getSite().setSelectionProvider(_viewer);

    _viewer.addDoubleClickListener(new IDoubleClickListener() 
	{
	    public void doubleClick(DoubleClickEvent event) 
	    {
		handleDoubleClick(event);
	    }
	});

    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.addMenuListener(this);
    Menu menu = menuMgr.createContextMenu(tree);
    tree.setMenu(menu); 
    getSite().registerContextMenu(menuMgr, _viewer);

    CppProjectNotifier notifier = ModelInterface.getInstance().getProjectNotifier();
    notifier.addProjectListener(this);

    startup();
  }

  public void selectReveal(ISelection selection) 
      {
	  _viewer.setSelection(selection, true);
      }

    public Shell getShell() 
  {
    return _viewer.getTree().getShell();
  }

  public void setFocus()
  {    
    _viewer.getTree().setFocus();    
  }


  public void resetView()
  {
      Tree tree = _viewer.getTree();
      if (!tree.isDisposed())
	  {
	      Display d= _viewer.getTree().getDisplay();
	      d.asyncExec(new Runnable()
		  {
		      public void run()
		      {
			  startup();
		      }
		  });
	  }
  }
  
    public TreeViewer getViewer()
    {
	return _viewer;
    }

  public void setInput(Object object)
  {    
    _viewer.setInput(object);     
  }
  
  public void startup()
  {
    DataStore dataStore = _plugin.getDataStore();
    _root = RemoteProjectAdapter.getInstance();
    setInput(_root);    
  }

  public void menuAboutToShow(IMenuManager menu)
      {
	menu.removeAll();
	
	fillContextMenu(menu);
      }


void makeActions() 
{ 
  Shell shell = getShell();
  openResourceAction = new OpenResourceAction(shell);
  openFileAction = new OpenFileAction(getSite().getPage());
  openSystemEditorAction = new OpenSystemEditorAction(getSite().getPage());
  closeResourceAction = new CloseResourceAction(shell);
  localRefreshAction = new RefreshAction(shell);
  buildAction = new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
  rebuildAllAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);

  moveProjectAction = new MoveProjectAction(shell);
  copyProjectAction = new CopyProjectAction(shell);	

  copyResourceAction = new CopyResourceAction(shell);
  moveResourceAction = new MoveResourceAction(shell);
  deleteResourceAction = new DeleteResourceAction(shell);
  renameResourceAction = new ResourceElementAction("Rename", "C_RENAME");

  addBookmarkAction = new AddBookmarkAction(shell);
  propertyDialogAction = new PropertyDialogAction(getShell(), _viewer);
  newWizardAction = new NewWizardAction();
  
  //we know these will be in a sub-folder called "New" so we can shorten the name
  createFolderAction = new ResourceElementAction("Folder...", "C_CREATE_DIR");
  createFileAction = new ResourceElementAction("File...", "C_CREATE_FILE");

  IActionBars actionBars = getViewSite().getActionBars();
  actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, deleteResourceAction);
  actionBars.setGlobalActionHandler(IWorkbenchActionConstants.BOOKMARK, addBookmarkAction);
}
  
void fillContextMenu(IMenuManager menu) {
	IStructuredSelection selection = (IStructuredSelection) _viewer.getSelection();

	updateActions(selection);
	
	fillFileMenu(menu, selection);
	menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));
	if (propertyDialogAction.isApplicableForSelection())
	   menu.add(propertyDialogAction);	
}
/**
 * Add file / resource actions to the context sensitive menu.
 * @param menu the context sensitive menu
 * @param selection the current selection in the project explorer
 */
void fillFileMenu(IMenuManager menu, IStructuredSelection selection) 
  {
	boolean anyResourceSelected = !selection.isEmpty() && SelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER | IResource.FILE);
	boolean onlyFilesSelected = !selection.isEmpty() && SelectionUtil.allResourcesAreOfType(selection, IResource.FILE);
	boolean onlyFoldersOrFilesSelected = !selection.isEmpty() && SelectionUtil.allResourcesAreOfType(selection, IResource.FOLDER | IResource.FILE);
	boolean onlyProjectsSelected = !selection.isEmpty() && SelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);
	
	if (onlyFilesSelected)
		menu.add(openFileAction);

	if (onlyProjectsSelected) {
	    menu.add(openResourceAction);
		menu.add(closeResourceAction);
		// Allow manual incremental build only if auto build is off.
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
			menu.add(buildAction);
		menu.add(rebuildAllAction);
	}

	if (anyResourceSelected) 
	    {
		fillOpenWithMenu(menu, selection);
		fillOpenToMenu(menu, selection);
		menu.add(new Separator());
	    }

	MenuManager newMenu = new MenuManager("Ne&w");
	menu.add(newMenu);

	if (anyResourceSelected) 
	{
		newMenu.add(createFolderAction);
		newMenu.add(createFileAction);
		}

	/*
	newMenu.add(new Separator());
	newMenu.add(newWizardAction);
	*/
	
	if (anyResourceSelected) {
		menu.add(new Separator());
		fillManageMenu(menu, selection);
	}
	
	menu.add(new Separator());

	/***
	if (onlyFoldersOrFilesSelected) {
		menu.add(copyResourceAction);
	} 
	else if (onlyProjectsSelected) 
	    {
		menu.add(copyProjectAction);
		menu.add(moveProjectAction);
	    }
	***/
	if (anyResourceSelected) 
	    {
		 menu.add(renameResourceAction);
		 menu.add(deleteResourceAction);
	    }
	/*
	if (onlyFilesSelected)
	    {
		menu.add(addBookmarkAction);
	    }
	*/
}
/**
 * Add view specific actions to the context sensitive menu.
 * @param menu the context sensitive menu
 * @param selection the current selection in the project explorer
 */
void fillManageMenu(IMenuManager menu, IStructuredSelection selection) {
	if (selection != null && !selection.isEmpty()) {
		menu.add(localRefreshAction);
	}
}
/**
 * Add "open to" actions to the context sensitive menu.
 * @param menu the context sensitive menu
 * @param selection the current selection in the project explorer
 */
void fillOpenToMenu(IMenuManager menu, IStructuredSelection selection) 
{
	// If one file is selected get it.
	// Otherwise, do not show the "open with" menu.
	if (selection.size() != 1)
		return;
	IAdaptable element = (IAdaptable) selection.getFirstElement();
	if (!(element instanceof IContainer))
		return;

	// Create a menu flyout.
	MenuManager submenu = new MenuManager("Open In Same Window");
	submenu.add(new OpenNewPageMenu(getSite().getWorkbenchWindow(), element));
	menu.add(submenu);

	// Create a menu flyout.
	submenu = new MenuManager("Open In New Window");
	submenu.add(new OpenNewWindowMenu(getSite().getWorkbenchWindow(), element));
	menu.add(submenu);
}
/**
 * Add "open with" actions to the context sensitive menu.
 * @param menu the context sensitive menu
 * @param selection the current selection in the project explorer
 */
void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {

	// If one file is selected get it.
	// Otherwise, do not show the "open with" menu.
	if (selection.size() != 1)
		return;

	Object element = selection.getFirstElement();
	if (!(element instanceof IFile))
		return;

	// Create a menu flyout.
	MenuManager submenu = new MenuManager("Open Wit&h");
	submenu.add(new OpenWithMenu(getSite().getPage(), (IFile) element));
		
	// Add the submenu.
	menu.add(submenu);
}

void updateActions(IStructuredSelection selection) {
	buildAction.selectionChanged(selection);
	rebuildAllAction.selectionChanged(selection);
	closeResourceAction.selectionChanged(selection);
	copyResourceAction.selectionChanged(selection);
	localRefreshAction.selectionChanged(selection);
	openResourceAction.selectionChanged(selection);
	openFileAction.selectionChanged(selection);
	openSystemEditorAction.selectionChanged(selection);
	propertyDialogAction.selectionChanged(selection);
	copyProjectAction.selectionChanged(selection);
	moveProjectAction.selectionChanged(selection);

	updateGlobalActions(selection);
}

void updateGlobalActions(IStructuredSelection selection) {
	deleteResourceAction.selectionChanged(selection);
	addBookmarkAction.selectionChanged(selection);

	// Ensure Copy global action targets correct action,
	// either copyProjectAction or copyResourceAction,
	// depending on selection.
	copyProjectAction.selectionChanged(selection);
	copyResourceAction.selectionChanged(selection);
	IActionBars actionBars = getViewSite().getActionBars();
	if (copyProjectAction.isEnabled())
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyProjectAction);
	else
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyResourceAction);
	actionBars.updateActionBars();
}

void handleDoubleClick(DoubleClickEvent event) 
{

    IStructuredSelection s = (IStructuredSelection)event.getSelection();
    Object element = s.getFirstElement();
    if (element instanceof IFile) 
	{
	    openFileAction.selectionChanged(s);
	    openFileAction.run();
	}
    else {
	// 1GBZIA0: ITPUI:WIN2000 - Double-clicking in navigator should expand/collapse containers
	if (_viewer.isExpandable(element)) 
	    {
		_viewer.setExpandedState(element, !_viewer.getExpandedState(element));
	    }
    }	
}
    
    public void projectChanged(CppProjectEvent event)
	{
	    int type = event.getType();
	    IProject project = event.getProject();
	    switch (type)
		{
		case CppProjectEvent.CLOSE:
		case CppProjectEvent.DELETE:
		    break;
		    
		case CppProjectEvent.COMMAND:
		    break;
		    
		case CppProjectEvent.VIEW_CHANGE:
		    {
			updateViewBackground();		
			updateViewForeground();		
			updateViewFont();
		    }
		    break;
		    
		default:
		    break;
		}
	}
    
    public void updateViewForeground()
	{
	    ArrayList colours = _plugin.readProperty("ViewForeground");
	    if (colours.size() == 3)
		{
		    int r = new Integer((String)colours.get(0)).intValue();
		    int g = new Integer((String)colours.get(1)).intValue();
		    int b = new Integer((String)colours.get(2)).intValue();
		    
		    _viewer.setForeground(r, g, b);	      
		}    
	}
    
    public void updateViewBackground()
	{
	    ArrayList colours = _plugin.readProperty("ViewBackground");
	    if (colours.size() == 3)
		{
		    int r = new Integer((String)colours.get(0)).intValue();
		    int g = new Integer((String)colours.get(1)).intValue();
		    int b = new Integer((String)colours.get(2)).intValue();
		    
		    _viewer.setBackground(r, g, b);	      
		}    
	}
    
    public void updateViewFont()
	{
	    ArrayList fontArray = _plugin.readProperty("ViewFont");
	    if (fontArray.size() > 0)
		{
		    String fontStr = (String)fontArray.get(0);
		    fontStr = fontStr.replace(',', '|');
		    FontData fontData = new FontData(fontStr);
		    _viewer.setFont(fontData);
		}
	}

    public void dispose()
    {
	_plugin.getDataStore().getDomainNotifier().removeDomainListener(_viewer);	
	CppProjectNotifier notifier = ModelInterface.getInstance().getProjectNotifier();
	notifier.removeProjectListener(this);
        super.dispose();	
    }
    
}
