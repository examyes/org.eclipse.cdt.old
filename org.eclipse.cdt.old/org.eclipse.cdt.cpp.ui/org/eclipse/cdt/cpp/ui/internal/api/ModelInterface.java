package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.cpp.ui.internal.actions.*;

import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.hosts.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.search.ui.*;
import org.eclipse.search.internal.ui.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import java.net.*;

public class ModelInterface implements IDomainListener, IResourceChangeListener
{
  private class OpenProjectAction extends Thread
  {
      private IProject _project;
      public OpenProjectAction(IProject project)
      {
	  _project = project;
      }

      public void run()
      {
	  DataStore dataStore = _plugin.getDataStore();

	  DataElement projectMinerProject = null;
	  if (_project instanceof Repository)
	      {
		  dataStore = ((Repository)_project).getDataStore();
		
		  DataElement workspace = findWorkspaceElement(dataStore);
		  if (workspace != null)
		      {
			  projectMinerProject = dataStore.createObject(workspace, "Project",
								       _project.getName(),
								       _project.getLocation().toString());
			  dataStore.setObject(workspace, false);
			  dataStore.setObject(projectMinerProject);
		      }	
	      }
	  else
	      {
		  projectMinerProject =  findProjectElement(_project, "Closed Project");
		  if (projectMinerProject == null)
		      {
			  //System.out.println("can't find project miner element for " + _project);
			  return;
		      }
		  else
		      {
			  projectMinerProject.setAttribute(DE.A_SOURCE, _project.getLocation().toString());
		      }
	      }
	
	  if (projectMinerProject != null)
	      {
		  DataElement oDescriptor = dataStore.localDescriptorQuery(projectMinerProject.getDescriptor(),
									   "C_OPEN", 4);
		  if (oDescriptor != null)
		      {
			  // open closed project
			  dataStore.synchronizedCommand(oDescriptor, projectMinerProject);

			  // open descriptor for FS object
			  oDescriptor = dataStore.localDescriptorQuery(projectMinerProject.getDescriptor(),
								       "C_OPEN", 4);
			  // open opened project - recursive query
			  dataStore.command(oDescriptor, projectMinerProject);
		      }
		
		  setPreferences(_project);
		
		  if (_project instanceof Repository)
		      {
			  DataElement rworkspace = findWorkspaceElement(dataStore);
			  projectMinerProject = dataStore.find(rworkspace, DE.A_NAME, _project.getName(), 1);
			
			  DataElement localWorkspace = findWorkspaceElement();		
			  if (localWorkspace != null && projectMinerProject != null)
			      {
				  DataStore localDataStore = _plugin.getDataStore();
				  DataElement localRemoteProject = ((Repository)_project).getClosedElement();
				  if (localRemoteProject != null)
				      {
					  localDataStore.deleteObject(localWorkspace, localRemoteProject);
					  ((Repository)_project).setClosedElement(null);
				      }
				
				  localDataStore.createReference(localWorkspace, projectMinerProject);		
				  projectMinerProject.setParent(rworkspace);
				
				  localDataStore.refresh(localWorkspace);
			      }
		      }
		
		  _plugin.setCurrentDataStore(projectMinerProject.getDataStore());

		  CppProjectNotifier notifier = getProjectNotifier();
		  notifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.OPEN, _project));
	      }
      }

      private synchronized void setPreferences(IProject project)
      {
	  Display d= getDummyShell().getDisplay();
	  d.asyncExec(new SetPreferencesAction(project));
      }
  }

  public class SetPreferencesAction implements Runnable
  {
      private IProject _project;

    public SetPreferencesAction(IProject project)
    {
      _project = project;
    }

      public void run()
      {
	  setParseIncludePath(_project);	
	  setParseQuality(_project);	
	  setEnvironment(_project);
	  setDistributionExtensions(_project,"init");
      }
  }

  public class ShowViewAction implements Runnable
  {
    private String      _id;
    private DataElement _input;

    public ShowViewAction(String id, DataElement input)
    {
      _id = id;
      _input = input;
    }

    public void run()
    {
      IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
      IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

      IWorkbenchPage persp= win.getActivePage();
      ILinkable viewPart = (ILinkable)persp.findView(_id);

      if (viewPart != null)
	{	
	  try
	    {
		if (_input != null)
		    viewPart.setInput(_input);	
		persp.showView(_id);
	    }
	  catch (PartInitException e)
	      {
	      }
	}
    }
  }

  public class SearchContextMenuContributor implements IContextMenuContributor
  {
      public void fill(IMenuManager menu, IInputSelectionProvider inputProvider)
      {
	  Object results = inputProvider.getInput();
	  if (results instanceof java.util.List)
	      {
		  Object input = ((java.util.List)results).get(0);
		  if (input instanceof ISearchResultViewEntry)
		      {
			  IMarker marker = ((ISearchResultViewEntry)input).getSelectedMarker();
			  try
			      {
				  Object de = marker.getAttribute("DataElementID");
				  if (de != null && de instanceof DataElement)
				      {
					  DataElement element = (DataElement)de;
					  _menuHandler.fillContextMenu(menu, null, element);
				      }
			      }
			  catch (CoreException e)
			      {
			      }
		      }
	    }
      }
  }

  public class ShowMarkerAction extends Action
  {
    public ShowMarkerAction()
    {
      super("SearchResultView.showMarker.text");
    }

    public void run()
      {
	  _searchResultsView = SearchUI.getSearchResultView();

	ISelection selection= _searchResultsView.getSelection();
	IStructuredSelection es= (IStructuredSelection) selection;
	Object element= es.getFirstElement();
	if (element instanceof ISearchResultViewEntry)
	    {
		show(((ISearchResultViewEntry)element).getSelectedMarker());
	    }
    }

    public void show(IMarker marker)
    {
	IResource resource= marker.getResource();
 	IWorkbenchPage perspective= SearchPlugin.getActivePage();
	if (resource == null || !(resource instanceof IFile))
	    {
		try
		    {
			Object de = marker.getAttribute("DataElementID");
			if (de != null && de instanceof DataElement)
			    {
				IOpenAction openA = CppActionLoader.getInstance().getOpenAction();
				openA.setSelected((DataElement)de);
				openA.run();
			    }
		    }
		catch (CoreException e)
		    {
		    }
	    }
	else
	    {
		try 
		    {
			perspective.openEditor(marker);
		    }
		catch (PartInitException e) 
		    {
			SearchPlugin.beep();
		    }	
	    }
    }
  }

    public class GroupByKeyComputer implements IGroupByKeyComputer
    {
	public Object computeGroupByKey(IMarker marker)
	{
	    if (marker == null)
		return null;
	
	    try
		{
		    return (String)marker.getAttribute("DataElementID");
		}
	    catch (CoreException e)
		{
		    System.out.println(e);
		}

	    return marker.getResource();
	}
    }

    public class CreateSearchMarkersAction implements Runnable
    {
	private ArrayList _results;

	public CreateSearchMarkersAction(ArrayList results)
	{
	    _results = results;
	}

	public void run()
	{
	    IResource file = null;
	    for (int i = 0; i < _results.size(); i++)
		{		
		    DataElement output = ((DataElement)_results.get(i)).dereference();

		    String fileName  = (String)(output.getElementProperty(DE.P_SOURCE_NAME));
		    Integer location = (Integer)(output.getElementProperty(DE.P_SOURCE_LOCATION));
		    int loc = location.intValue();
		
		    file = findFile(fileName);
	
		    if (file == null)
			{
			    DataElement fileElement = output.getDataStore().createObject(null, "file", fileName, fileName);
			    file = new FileResourceElement(fileElement, (IProject)CppPlugin.getCurrentProject());
			    addNewFile(file);
			}
		    if (file != null)
			{
			    try
				{
				    IMarker searchMarker = null;
				    if (file instanceof FileResourceElement)
					{
					    IWorkspaceRoot workspaceRoot = _workspace.getRoot();
					    searchMarker = workspaceRoot.createMarker(SearchUI.SEARCH_MARKER);
					}
				    else
					{
					    searchMarker = file.createMarker(SearchUI.SEARCH_MARKER);		
					}

				    String message = output.getName();				
				    HashMap attributes = new HashMap(5);
				    attributes.put(IMarker.MESSAGE, message);
				    attributes.put(IMarker.CHAR_START, new Integer(-1));
				    attributes.put(IMarker.CHAR_END, new Integer(-1));
				    attributes.put(IMarker.LINE_NUMBER, new Integer(loc));
				    attributes.put("DataElementID", output);
				    searchMarker.setAttributes(attributes);
				
					ISearchResultView sview = SearchUI.getSearchResultView();
				    if (sview != null)
				    {
					    sview.addMatch(message, message, file, searchMarker);
				    }
				}
			    catch (CoreException e)
				{
				    e.printStackTrace();
				    System.out.println(e);
				}
			}
		}
	}
    }

  private CppPlugin      _plugin;
  private DataElement    _markersDescriptor;
  private IWorkspace     _workspace;
    private DataElement  _workspaceElement = null;

  private String         _workspaceDirectory;

  private ArrayList      _statuses;
  private ArrayList      _viewers;

  private ArrayList      _markedFiles;
    private ArrayList    _tempFiles;

  private DataElement    _status;


  private ISearchResultView _searchResultsView;
  private Shell             _dummyShell;

  private static CppProjectNotifier _projectNotifier;

  private static ModelInterface _instance;

    private MenuHandler _menuHandler;

  public ModelInterface(DataStore dataStore)
  {
    _workspace = WorkbenchPlugin.getPluginWorkspace();
    _workspace.addResourceChangeListener(this);

    Path workbenchPath = (Path)Platform.getLocation();
    _workspaceDirectory = workbenchPath.toString();

    _plugin = CppPlugin.getDefault();

    _statuses = new ArrayList();
    _viewers = new ArrayList();
    _markedFiles = new ArrayList();
    _tempFiles = new ArrayList();

    _projectNotifier = new CppProjectNotifier(this);
    _projectNotifier.enable(true);
    
    _projectNotifier.addProjectListener(StatusLineUpdater.getInstance());

    _menuHandler = new MenuHandler(CppActionLoader.getInstance());

    _instance = this;
  }

  public static ModelInterface getInstance()
  {
    return _instance;
  }

  public static CppProjectNotifier getProjectNotifier()
  {
      return _projectNotifier;
  }

    public IWorkspace getWorkspace()
    {
	return _workspace;
    }

  public void initDataStore()
      {
	  if (_plugin == null)
	      _plugin = CppPlugin.getDefault();

	  DataStore dataStore = _plugin.getDataStore();
	  DataElement schemaRoot    = dataStore.getDescriptorRoot();
	  dataStore.showTicket(dataStore.getTicket().getName());
	  dataStore.getSchema();
	  dataStore.initMiners();
	
	  _markersDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "markers", 1);
	
	  dataStore.getDomainNotifier().addDomainListener(this);

      }

  public Shell getShell()
  {
      if (_dummyShell != null)
	  {
	      return _dummyShell;
	  }

      try
	  {
	      Shell shell = new Shell();
	      return shell;
	  }
      catch (Exception e)
	  {
	  }
      return null;
  }

  public Shell getDummyShell()
  {
    if (_dummyShell == null)
      {	
	  IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	  IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	
	  if (win != null)
	      {	
		  _dummyShell = win.getShell();
	      }
	  else
	      {
		  try
		      {
			  _dummyShell = new Shell();	
		      }
		  catch (Exception e)
		      {
			  return null;
		      }
	      }
	
	  _plugin.getDataStore().getDomainNotifier().setShell(_dummyShell);
      }

    _plugin.getDataStore().getDomainNotifier().enable(true);
    return _dummyShell;
  }

  public void showView(String id, DataElement input)
  {
  	Shell dummy = getDummyShell();
  	if (dummy != null && !dummy.isDisposed())
  	{
  		Display d= dummy.getDisplay();
		ShowViewAction action = new ShowViewAction(id, input);
		d.syncExec(action);
  	}
  }

    public void monitorStatus(DataElement status)
    {
	if (status != null)
	    {
		if (!_statuses.contains(status))
		    {
			_statuses.add(status);
			IProject project = _plugin.getCurrentProject();
			MonitorStatusThread monitor = new MonitorStatusThread(status, project);
			monitor.setWaitTime(1000);
			monitor.start();
		    }
		else if (status.getName().equals("done"))
		    {
			_statuses.remove(status);
		    }
	    }
    }

    public void debug(String pathStr, String port, String key)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();

	DataElement dirObject = null;
	IResource resource = findFile(pathStr);
	if (resource == null)
	    {
		dirObject = dataStore.createObject(null, "directory", pathStr, pathStr);	
	    }
	else
	    {
		dirObject = findResourceElement(resource);	
	    }

	dataStore.setObject(dirObject);
	debug(dirObject, port, key);
    }

    public void debug(DataElement dirObject, String port, String key)
    {
       if (dirObject == null)
          {
            DataStore ddataStore = _plugin.getCurrentDataStore();
            dirObject = ddataStore.getHostRoot().get(0);
          }

       DataStore dataStore = dirObject.getDataStore();


	String hostName = "localhost";
	try
	    {
          hostName = InetAddress.getLocalHost().getHostName();
       }
	catch (Exception e)
	    {
          System.out.println(e);
	    }

	DataElement hostObj  = dataStore.createObject(null, "hostname", hostName);
	DataElement portObj    = dataStore.createObject(null, "port", port);
	DataElement keyObj     = dataStore.createObject(null, "key", key);
	
	dataStore.setObject(hostObj);
	dataStore.setObject(portObj);
	dataStore.setObject(keyObj);


	DataElement debugDescriptor = dataStore.localDescriptorQuery(dirObject.getDescriptor(), "C_DEBUG");

	if (debugDescriptor != null)
	    {
		ArrayList args = new ArrayList();
		args.add(hostObj);
		args.add(portObj);
		args.add(keyObj);

		if (_plugin.getDataStore() == dataStore)
		    {			
			String jrePath = new String(_plugin.getPluginPath());
			int indexOfPlugins = jrePath.indexOf("plugins");
			if (indexOfPlugins > 0)
			    {
				jrePath = jrePath.substring(0, indexOfPlugins);
			    }
			jrePath = jrePath + "jre/bin/";
			DataElement javaPath = dataStore.createObject(null, "directory", jrePath);
			dataStore.setObject(javaPath);
			args.add(javaPath);
		    }

		_status = dataStore.command(debugDescriptor, args, dirObject);
		showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", _status);
	    }
    }


    public DataElement command(IResource resource, String invocation, boolean showProgress)
      {
	if (resource instanceof ResourceElement)
	  {
	    DataElement element = ((ResourceElement)resource).getElement();	
	    return invoke(element, invocation, showProgress);	
	  }
	else if (resource instanceof Repository)
	  {
	    DataElement element = ((Repository)resource).getElement();	
	    return invoke(element, invocation, showProgress);	
	  }
	else if (resource != null)
	  {	
	    IPath location = resource.getLocation();
	    if (location != null)
	      {		
		String path = new String(resource.getLocation().toString());
		return invoke(path, invocation, showProgress);
	      }
	  }	

	return null;	
      }

  public DataElement command(String path, String invocation)
      {
        return command(path, invocation, true);
      }

  public DataElement command(String path, String invocation, boolean showProgress)
      {
        IResource resource = getResource(path);
        return invoke(path, invocation, showProgress);
      }
 public DataElement invoke(String path, String invocation, boolean showProgress)
  {
  	// first try to find the path element
  	DataElement pathElement = findResourceElement(findWorkspaceElement(), path);
  	if (pathElement == null)
  	{
     pathElement = _plugin.getCurrentDataStore().createObject(null, "directory", path, path);
  	}
  	
    return invoke(pathElement, invocation, showProgress);
  }

  public DataElement invoke(DataElement pathElement, String invocation, boolean showProgress)
  {
   if (invocation != null)
      {	
	ArrayList args = new ArrayList();
	DataStore dataStore = pathElement.getDataStore();
	dataStore.getDomainNotifier().addDomainListener(this);	
	
	DataElement invocationObj = dataStore.createObject(null, "invocation", invocation, "");
	args.add (invocationObj);
	
	IProject currentProject = CppPlugin.getCurrentProject();
	args.add(findProjectElement(currentProject));
	

	deleteAssociatedMarkers(currentProject);

  	
	DataElement commandDescriptor = dataStore.localDescriptorQuery(pathElement.getDescriptor(), "C_COMMAND");
	DataElement status = dataStore.command(commandDescriptor, args, pathElement, true);
	if (status != null)
	    invocationObj.setParent(status.getParent());
	_status = status;

	if (showProgress)
	{
		monitorStatus(status);
	}
	
	showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", _status);
	return status;	
      }

    return null;
  }


    public void initializeProject(IProject project)
    {
	if (project != null && _plugin.isCppProject(project))
	    {
		DataStore dataStore = _plugin.getDataStore();
		DataElement workspace = findWorkspaceElement(dataStore);
		if (workspace != null)
		    {
			IPath location = project.getLocation();
			if (location != null)
			    {
				DataElement closedProject = dataStore.createObject(workspace, "Closed Project",
										   project.getName(),
										   location.toString());


				if (project instanceof Repository)
				    {
					// we need to distinguish it's host from others
					((Repository)project).setClosedElement(closedProject);
				    }
			

			    }
		    }
	    }
    }

    public void openProjects()
    {
	// open all local projects
	IProject[] projects = _workspace.getRoot().getProjects();
	for (int i = 0; i < projects.length; i++)
	    {	
		IProject project = projects[i];
		if (project.isOpen() && _plugin.isCppProject(project))
		    {
			openProject(project);
		    }
	    }			    	
    }

    public void openProject(IProject project)
    {
	openProject(project, getDummyShell());
    }

    public void openProject(IProject project, Shell shell)
    {
    	if (project.isOpen())
	    {	
		if (_plugin.isCppProject(project))
		    {
			if (project instanceof Repository)
			    {
			    }
			else
			    {
				findProjectElement(project, "Closed Project");
			    }
			
			OpenProjectAction openAction = new OpenProjectAction(project);
			openAction.start();
		    }
		else
		    {
			DataElement closedProjectElement = findProjectElement(project, "Closed Project");
			if (closedProjectElement != null)
			    {
				DataStore dataStore = closedProjectElement.getDataStore();
				dataStore.deleteObject(closedProjectElement.getParent(), closedProjectElement);
			    }
		    }
	    }
    }


    private class CloseEditorAction implements Runnable
    {
	private IEditorPart    _editor;
	private IWorkbenchPage _page;

	public CloseEditorAction(IWorkbenchPage page, IEditorPart editor)
	{
	    _page = page;
	    _editor = editor;
	}
	
	public void run()
	{
	    _page.closeEditor(_editor, false);
	}
    }

    public boolean isBeingEdited(IResource resource)
    {
	IWorkbench desktop = CppPlugin.getDefault().getWorkbench();
	IWorkbenchWindow[] windows = desktop.getWorkbenchWindows();
	for (int a = 0; a < windows.length; a++)
	    {		
		IWorkbenchWindow window = windows[a];
		IWorkbenchPage[] pages = window.getPages();
		for (int b = 0; b < pages.length; b++)
		    {
			IWorkbenchPage page = pages[b];
			IEditorPart[] editors = page.getEditors();
		        for (int c = 0; c < editors.length; c++)
			    {
				IEditorPart editor = editors[c];
				if (editor.getEditorInput() instanceof IFileEditorInput)
				    {
					IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
					if (input != null)
					    {
						IFile file = input.getFile();
						if (file.getLocation().toString().equals(resource.getLocation().toString()))
						    {
							return true;
						    }
					    }
				    }
			    }
		    }
	    }
	
	return false;
    }

    private void closeEditors()
    {
	IProject[] projects = _workspace.getRoot().getProjects();

	for (int i = 0; i < projects.length; i++)
	    {	
		if (_plugin.isCppProject(projects[i]))
		  {
		      closeEditors(projects[i]);
		  }
	    }

	RemoteProjectAdapter rmtAdapter = RemoteProjectAdapter.getInstance();
	if (rmtAdapter != null)
	    {
		IProject[] rprojects = rmtAdapter.getProjects();
		
		if (rprojects != null)
		    {
			for (int j = 0; j < rprojects.length; j++)
			    {	
				if (_plugin.isCppProject(rprojects[j]))
				    {
					closeEditors(rprojects[j]);
				    }
			    }
		    }
	    }
    }


    private void closeEditors(IProject project)
    {
	IWorkbench desktop = CppPlugin.getDefault().getWorkbench();

	IWorkbenchWindow[] windows = desktop.getWorkbenchWindows();
	for (int a = 0; a < windows.length; a++)
	    {	
		IWorkbenchWindow window = windows[a];
		IWorkbenchPage[] pages = window.getPages();
		for (int b = 0; b < pages.length; b++)
		    {
			IWorkbenchPage page = pages[b];
			IEditorPart[] editors = page.getEditors();
		        for (int c = 0; c < editors.length; c++)
			    {
				IEditorPart editor = editors[c];
				if (editor.getEditorInput() instanceof IFileEditorInput)
				  {
				    IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
				    if (input != null)
				      {
					IFile file = input.getFile();
					IProject fProject = file.getProject();					
					if (fProject == null ||
					    fProject == project ||
					    fProject.getLocation() == project.getLocation())
					    {
						Display d= getDummyShell().getDisplay();
						d.asyncExec(new CloseEditorAction(page, editor));
					    }
				      }
				  }
			    }
		    }	
	    }
    }

    public void shutdown()
    {
		_projectNotifier.enable(false);

		DataStore dataStore = _plugin.getCurrentDataStore();

		_workspace.removeResourceChangeListener(this);

		DomainNotifier domainNotifier = dataStore.getDomainNotifier();
		
        domainNotifier.removeDomainListener(this);		
	try
	    {
		SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
	    }
	catch (CoreException e)
	    {
	    }
	closeProjects(dataStore);
    }

	
  public void closeProjects(DataStore dataStore)
  {
    closeEditors();

    DataElement solutionObj = findWorkspaceElement(dataStore);
    if (solutionObj != null)
	{
	    DataElement commandDescriptor = dataStore.localDescriptorQuery(solutionObj.getDescriptor(), "C_CLOSE_PROJECTS");
	    if (commandDescriptor != null)
		{		
		    dataStore.cancelAllCommands();
		    dataStore.synchronizedCommand(commandDescriptor, solutionObj);	
		    dataStore.cleanBadReferences(dataStore.getLogRoot());
		}
	}
  }

  public void closeProject(IProject project)
  {
      // close editors
      closeEditors(project);

      // remote temp information
      for (int i = 0; i < _tempFiles.size(); i++)
	  {
	      IResource file = (IResource)_tempFiles.get(i);
	      if (file.getProject() == project)
		  {
		      _tempFiles.remove(file);
		      file = null;
		      i--;
		  }
	  }

    DataStore dataStore = _plugin.getDataStore();	
    if (project instanceof Repository)
	{
	    dataStore = ((Repository)project).getDataStore();		
	}

    // close project
    DataElement workspace = findWorkspaceElement(dataStore);	
    DataElement cprojectObj = dataStore.find(workspace, DE.A_NAME, project.getName(), 1);


    if (cprojectObj != null)
	{
	    DataElement closeD = dataStore.localDescriptorQuery(cprojectObj.getDescriptor(), "C_CLOSE_PROJECT");
	
	    if (closeD != null)
		{
		    dataStore.synchronizedCommand(closeD, cprojectObj);
		}

	    if (project instanceof Repository)
		{
		    initializeProject(project);
		}
	}

  }

    public void saveProject(IProject project)
    {
        DataStore dataStore = _plugin.getDataStore();	
	if (project instanceof Repository)
	    dataStore = ((Repository)project).getDataStore();	
	
	DataElement projectObj = findProjectElement(project);
	if (projectObj != null)
	    {
		DataElement commandDescriptor = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_SAVE_PARSE");
		if (commandDescriptor != null)
		    {		
			dataStore.command(commandDescriptor, projectObj, false, true);	
		    }	
	    }
    }

    public void clearProject(IProject project)
    {
	DataStore dataStore = _plugin.getDataStore();	
	if (project instanceof Repository)
	    dataStore = ((Repository)project).getDataStore();	
	
	DataElement projectObj = findProjectElement(project);
	if (projectObj != null)
	    {
		DataElement commandDescriptor = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_REMOVE_PARSE");
		
		if (commandDescriptor != null)
		    {		
			dataStore.command(commandDescriptor, projectObj, false, true);	
		    }	
	    }
    }

	public void setDistributionExtensions(IProject project, String init_set_Key)
	{
		DataStore dataStore = _plugin.getDataStore();	
		if (project instanceof Repository)
			dataStore = ((Repository)project).getDataStore();
				
		DataElement extensionElement = dataStore.createObject(null, "ExtraDist File Extensions", project.getName());
		ArrayList extensions = _plugin.readProperty(project, "Extra_Dist_Extensions");
		for (int i = 0; i < extensions.size(); i++)
			dataStore.createObject(extensionElement, "ExtraDist File Extensions", (String)extensions.get(i), (String)extensions.get(i));

		setDistributionExtensions(findProjectElement(project), extensionElement,init_set_Key);
	}
	public void setDistributionExtensions(DataElement theObject, DataElement theExtensions, String init_set_Key)
	{
		if ((theObject == null) || (theExtensions == null))
			return;
		theExtensions.setAttribute(DE.A_NAME, theObject.getId());
		DataStore dataStore = theObject.getDataStore();
		DataElement contObj = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Container Object", 1);
		DataElement setD;
		if(init_set_Key.equals("init"))
		{
			setD = dataStore.localDescriptorQuery(contObj, "C_INIT_EXTRA_DIST_EXTENSIONS");
		}
		else
		{
			setD = dataStore.localDescriptorQuery(contObj, "C_SET_EXTRA_DIST_EXTENSIONS");
		}
		if (setD != null)
		{
			dataStore.command(setD, theExtensions, theObject, true);
		}
	}

  public void setEnvironment(IProject project)
  {

   DataStore dataStore = _plugin.getDataStore();	
   if (project instanceof Repository)
    dataStore = ((Repository)project).getDataStore();	


   DataElement envElement = dataStore.createObject(null, "Environment Variable", project.getName());
   ArrayList envVars = _plugin.readProperty(project, "Environment");
   for (int i = 0; i < envVars.size(); i++)
    dataStore.createObject(envElement, "Environment Variable", (String)envVars.get(i), (String)envVars.get(i));

   setEnvironment(findProjectElement(project), envElement);
  }

 public void setEnvironment(DataElement theObject, DataElement theEnvironment)
 {
  if ((theObject == null) || (theEnvironment == null))
   return;
  theEnvironment.setAttribute(DE.A_NAME, theObject.getId());
  DataStore dataStore = theObject.getDataStore();
  DataElement contObj = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Container Object", 1);
  DataElement setD = dataStore.localDescriptorQuery(contObj, "C_SET_ENVIRONMENT_VARIABLES");
  if (setD != null)
  {
   dataStore.command(setD, theEnvironment, theObject, true);
  }
 }

 public void setParseIncludePath(IProject project)
 {
  DataStore dataStore = _plugin.getDataStore();	
  if (project instanceof Repository)
   dataStore = ((Repository)project).getDataStore();	


  DataElement projectObj = findProjectElement(project, "Project");
  if (projectObj != null)
      {
	  DataElement includeElement = dataStore.createObject(null, "environment", "Include Path");	
	  ArrayList includePaths = _plugin.readProperty(project, "Include Path");
	  for (int i = 0; i < includePaths.size(); i++)
	      dataStore.createObject(includeElement, "directory", (String)includePaths.get(i), (String)includePaths.get(i));
	
	  DataElement setD = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_SET_INCLUDE_PATH");
	  if (setD != null)
	      {
		  ArrayList args = new ArrayList();
		  args.add(includeElement);	
		  dataStore.command(setD, includeElement, projectObj, true);
	      }
      }
  }

  public void setParseQuality(IProject project)
  {
   DataStore dataStore = _plugin.getDataStore();	
   if (project instanceof Repository)
     dataStore = ((Repository)project).getDataStore();	

   DataElement projectObj = findProjectElement(project);
   if (projectObj != null)
       {

	   // parse quality
	   DataElement qualityElement = dataStore.createObject(null, "quality", "2");
	   ArrayList quality = _plugin.readProperty(project, "ParseQuality");
	
	   if (!quality.isEmpty())
	       {
		   String qualityStr = (String)quality.get(0);
		   qualityElement.setAttribute(DE.A_NAME, qualityStr);
	       }

	   // parse behaviour
	   DataElement autoParseElement = dataStore.createObject(null, "autoparse", "No");
	   ArrayList autoParse = _plugin.readProperty(project, "AutoParse");
	   	
	   if (!autoParse.isEmpty())
	       {
		   String autoParseStr = (String)autoParse.get(0);
		   autoParseElement.setAttribute(DE.A_NAME, autoParseStr);
	       }	

	   DataElement autoPersistElement = dataStore.createObject(null, "autopersist", "No");
	   ArrayList autoPersist = _plugin.readProperty(project, "AutoPersist");
	   	
	   if (!autoPersist.isEmpty())
	       {
		   String autoPersistStr = (String)autoPersist.get(0);
		   autoPersistElement.setAttribute(DE.A_NAME, autoPersistStr);
	       }	

	   DataElement setD = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_SET_PREFERENCES");
	   if (setD != null)
	       {
		   ArrayList args = new ArrayList();
		   args.add(qualityElement);			
		   args.add(autoParseElement);			
		   args.add(autoPersistElement);			
		   dataStore.command(setD, args, projectObj);
	       }
       }
  }

    public DataElement findWorkspaceElement()
    {
	return findWorkspaceElement(_plugin.getDataStore());
    }

    public DataElement findWorkspaceElement(DataStore dataStore)
    {

    	if (_workspaceElement != null && _workspaceElement.getDataStore() == dataStore)
    	{
    		return _workspaceElement;
    	}
    	
    	
	//if (_workspaceElement == null)
	    {
		DataElement workspaceObj = null;
		DataElement projectInfo = dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.project.ProjectMiner");
		if (projectInfo == null)
		    {
			//System.out.println("couldn't find project miner");
		    }
		else
		    {
			workspaceObj = dataStore.find(projectInfo, DE.A_TYPE, "Workspace", 1);
			if (workspaceObj == null)
			    {
				//System.out.println("couldn't find workspace");
			    }
		    }
		
		if (workspaceObj != null)
		    {
			if (dataStore == _plugin.getDataStore())
			    {
				workspaceObj.setAttribute(DE.A_SOURCE, _workspaceDirectory);	
			    }
		    }
		_workspaceElement = workspaceObj;
	    }

	return _workspaceElement;
    }

    public IResource findResource(DataElement resourceElement)
    {
	String type = resourceElement.getType();
	if (type.equals("Project") || type.equals("Closed Project"))
	    {
		return findProjectResource(resourceElement);
	    }
	else if (resourceElement.isOfType("file") || resourceElement.isOfType("directory"))
	    {
		return findFile(resourceElement.getSource());
	    }
	else
	    {
		String source = resourceElement.getSource();
		return getResource(source);
	    }	
    }

  public IProject findProjectResource(DataElement projectElement)
    {
    	if (projectElement == null)
    	  return null;
    	
	// first search local projects
	IProject[] projects = _workspace.getRoot().getProjects();
	DataStore ldataStore = _plugin.getDataStore();
	for (int i = 0; i < projects.length; i++)
	    {	
		IProject project = projects[i];
		if (_plugin.isCppProject(project))
		    {
			if (compareFileNames(project.getLocation().toString(), projectElement.getSource()))
			    {
				if (projectElement.getName().equals(project.getName()))
				    {
					if (ldataStore == projectElement.getDataStore())
					    {
						return project;
					    }
				    }
			    }
		    }
	    }			    	

	// next search remote projects
	RemoteProjectAdapter rmtAdapter = RemoteProjectAdapter.getInstance();
	if (rmtAdapter != null)
	    {
		IProject[] rprojects = rmtAdapter.getProjects();		
		if (rprojects != null)
		    {
			for (int j = 0; j < rprojects.length; j++)
			    {	
				IProject project = rprojects[j];
				if (compareFileNames(project.getLocation().toString(),
						     projectElement.getSource()))
				    {
					if (projectElement.getName().equals(project.getName()))
					    {
						if (project.isOpen())
						    {
							DataStore rdataStore = ((Repository)project).getDataStore();
							if (rdataStore == projectElement.getDataStore())
							    {
								return project;
							    }
						    }
						else
						    {
							DataElement root = ((Repository)project).getClosedElement();
							if (root == projectElement)
							    {
								return project;
							    }
						    }
					    }
				    }
			    }
		    }
	    }
	
	return null;
    }





    public DataElement findOrCreateResourceElement(IResource resource)
    {
	DataElement result = findResourceElement(resource);
	if (result == null)
	    {
		// create some temporary element to represent a resource
		DataElement projectElement = findProjectElement(resource.getProject());
		if (projectElement != null)
		    {
			DataStore dataStore = projectElement.getDataStore();
			if (resource instanceof IFile)
			    {			
				result = dataStore.createObject(null, "file", resource.getName(), resource.getLocation().toString());
			    }			
			else if (resource instanceof IFolder)
			    {
				result = dataStore.createObject(null, "directory", resource.getName(), resource.getLocation().toString());
				
			    }			
		    }
	    }

	return result;
    }

    public DataElement findResourceElement(IResource resource)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	DataElement workspace = findWorkspaceElement(dataStore);
	if (resource instanceof IWorkspace)
	    {
		return workspace;
	    }
	else if (resource instanceof IProject)
	    {
		return findProjectElement((IProject)resource);
	    }
	else if (resource instanceof ResourceElement)
	    {
		ResourceElement resElement = (ResourceElement)resource;
		return resElement.getElement();
	    }
	
	DataElement result = null;
	if (workspace != null)
	    {
		if (resource != null)
		    {
			String resString = resource.getLocation().toString();
			result = findResourceElement(workspace, resString);
		    }
	    }
	
	return result;
    }

    public DataElement findResourceElement(DataStore dataStore, String path)
    {
	return findResourceElement(findWorkspaceElement(dataStore),  path);
    }

    public DataElement findResourceElement(DataElement root, String path)
    {
	DataElement found = null;
	if (root == null)
	    {
		return null;
	    }

	if (compareFileNames(root.getSource(), path))
	    {
		found = root;
	    }
	else
	    {
	    	if (root.getType().equals("Workspace") || path.startsWith(root.getSource()))
	    	{
		    ArrayList children = root.getAssociated("contents");
		    for (int i = 0; i < children.size(); i++)
			{		    	
			    DataElement child = (DataElement)children.get(i);
			    if (child != null && !child.isDeleted())
			    {
				if (child.isOfType("file"))
				    {	
					
					found = findResourceElement(child, path);
					if (found != null)
					    {
						return found;
					    }
					
				    }
			    }
			}
	    	}
	    }
	
	return found;
    }





  public DataElement findProjectElement(IProject project)
    {
	return findProjectElement(project, "Project");
    }

  public DataElement findProjectElement(IProject project, String type)
  {
      if (!_plugin.isCppProject(project))
	  {
	      return null;
	  }
      if (project == null)
	  return null;

      DataStore dataStore = _plugin.getDataStore();	
      if (project instanceof Repository)
	  dataStore = ((Repository)project).getDataStore();	
	
      DataElement workspace = findWorkspaceElement(dataStore);
      if (workspace == null)
	  {
	      return null;
	  }

      DataElement projectObj = dataStore.find(workspace, DE.A_NAME, project.getName(), 1);
      if (projectObj == null)
	  {
	      projectObj = dataStore.createObject(workspace, type, project.getName(), project.getLocation().toString());

	      if (dataStore != _plugin.getDataStore())
		  {
		      dataStore.setObject(workspace);
		  }
	  }

      return projectObj;
  }


 private DataElement getPathElement(IResource resource)
 {
     return findResourceElement(resource);
 }

  public void parse(IResource resource)
  {
    parse(resource, false);
  }

  public void parse(IResource resource, boolean isSynchronized)
  {
    parse(resource, isSynchronized, false);
  }

    public void parse(IResource resource, boolean isSynchronized, boolean showView)
    {
	DataElement pathElement = getPathElement(resource);
	parse(pathElement, isSynchronized, showView);
    }

  public void parse(DataElement pathElement, boolean isSynchronized, boolean showView)
    {
   if (pathElement != null)
       {
	   DataStore   dataStore    = pathElement.getDataStore();
	   DataElement projectRoot  = getProjectFor(pathElement);
	
	   if (projectRoot != null)
	       {
		   DataElement commandDescriptor = dataStore.localDescriptorQuery(pathElement.getDescriptor(), "C_PARSE");
		   DataElement projectsRoot = findWorkspaceElement(dataStore);		
		
		   if ((commandDescriptor == null) || (projectRoot == null))
		       return;
	
		   ArrayList args = new ArrayList();	
		   args.add(projectRoot);
		   dataStore.getDomainNotifier().addDomainListener(this);
		
		   DataElement status = null;
		   status = dataStore.command(commandDescriptor, args, pathElement, false);		
		
		   if (showView)
		   {
			   monitorStatus(status);
		   }
		   _status = status;
	       }
       }
    }	

  public synchronized void search(String pattern, ArrayList types, ArrayList relations,
				  boolean regex, boolean respectCase)
  {
  	  // search of current project
      IProject project = _plugin.getCurrentProject();
      DataElement subject = findProjectElement(project);

      //DataElement subject = findWorkspaceElement(_plugin.getCurrentDataStore());
      search(subject, pattern, types, relations, regex, respectCase);
  }

  public synchronized void search(Object input, String pattern, ArrayList types, ArrayList relations,
				  boolean regex, boolean respectCase)
  {
    if (input instanceof DataElement)
      {
		search((DataElement)input, pattern, types, relations, regex, respectCase);	
      }
    else if (input instanceof IContainer)
    {
    	if (input instanceof IWorkspace)
    	{
    		DataElement subject = findWorkspaceElement();
    		search(subject, pattern, types, relations, regex, respectCase);
    	}
    	else if (input instanceof IProject)
    	{
    		DataElement subject = findProjectElement((IProject)input);
    		search(subject, pattern, types, relations, regex, respectCase);	
    	}
    }
    else
      {
		search(pattern, types, relations, regex, respectCase);	
      }
  }

  public synchronized void search(DataElement subject, String pattern, ArrayList types, ArrayList relations,
				  boolean regex, boolean respectCase)
  {
  	  if (subject == null)
  	  {
  	  	return;
  	  }
  	
      DataStore dataStore = subject.getDataStore();

      DataElement searchDescriptor = null;

      if (regex)
	  {
	      searchDescriptor = dataStore.localDescriptorQuery(subject.getDescriptor(), "C_SEARCH_REGEX");
	  }
      else
	  {
	      searchDescriptor = dataStore.localDescriptorQuery(subject.getDescriptor(), "C_SEARCH");
	  }

    if (searchDescriptor == null)
    {
      searchDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Search");
    }
    if (searchDescriptor != null)
    {
      ArrayList args = new ArrayList();
      DataElement patternElement = dataStore.createObject(null, "pattern", pattern);

      patternElement.setAttribute(DE.A_VALUE, respectCase ? "check_case" : "ignore_case");
      args.add(patternElement);

      for (int i = 0; i < types.size(); i++)
      {
		String type = (String)types.get(i);
                if (type.equals("type"))
                {
                        type = "class"; 
                } 
		DataElement objDescriptor = dataStore.findObjectDescriptor(type);
		if (objDescriptor != null)
		    {
			dataStore.createReference(patternElement, objDescriptor);
		    }		        
      }


	  {
	  			
		  SearchUI.activateSearchResultView();
		
	      _searchResultsView = SearchUI.getSearchResultView();
		  if (_searchResultsView != null)
		  {
		      StringBuffer patternStr = new StringBuffer("Pattern: " + pattern + " Types: ");
		      for (int i = 0; i < types.size(); i++)
			  {
			      patternStr.append((String)types.get(0));
			      if (i < types.size()) patternStr.append(", ");
			  }
                      
		      _searchResultsView.searchStarted(
					       "org.eclipse.cdt.cpp.ui.CppSearchPage",
					       patternStr.toString(),
					       patternStr.toString(),
					       CppPlugin.getDefault().getImageDescriptor("details.gif"),
					       new SearchContextMenuContributor(),
					       SearchLabelProvider.getInstance(),
					       new ShowMarkerAction(),
					       new GroupByKeyComputer(),
					       null);
		  }
	
	      Display d = getDummyShell().getDisplay();
	      d.syncExec(new Runnable()
		  {
		      public void run()
		      {
			  try
			      {
				  SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
			      }
			  catch (CoreException e)
			      {
			      }
		      }
		  });
	  }

      DataElement status = dataStore.command(searchDescriptor, args, subject, true);
      _status = status;
      monitorStatus(_status);
    }
  }


  public void cancel(DataElement command)
  {
      DataStore dataStore = command.getDataStore();
      DataElement cmdDescriptor = command.getDescriptor();
      DataElement cancelDescriptor = dataStore.localDescriptorQuery(cmdDescriptor, "C_CANCEL");
      if (cancelDescriptor != null)
	  {	
	      dataStore.command(cancelDescriptor, command);
	  }
  }


    public void addNewFile(IResource file)
    {
	_tempFiles.add(file);
    }

  public IResource findFile(String fileName)
    {
	IResource result = null;
	java.io.File theFile = new java.io.File(fileName);
	result = findFile(theFile);

	// search remote files
	if (result == null)
	    {		
		RemoteProjectAdapter rmt = RemoteProjectAdapter.getInstance();		
		if (rmt != null)
		    result = findFile(rmt, fileName);
	    }
	

	return result;
    }

  public IResource findFile(java.io.File fileName)
  {
    IResource file = null;
    if (fileName != null)
      {		
	  IWorkspace ws = _plugin.getPluginWorkspace();
	  IWorkspaceRoot root = ws.getRoot();

	  // search workspace files
	  file = findFile(root, fileName);

	  // search temporary files
	  if (file == null)
	      {
		  for (int i = 0; i < _tempFiles.size(); i++)
		      {
			  ResourceElement tempFile = (ResourceElement)_tempFiles.get(i);
			  String tempFileName = tempFile.getLocalLocation().toString();
			  if (compareFileNames(tempFileName, fileName))
			      {
				  return tempFile;
			      }
		      }
	      }
      }

    return file;
  }

    public IResource findFile(RemoteProjectAdapter root, String fileName)
    {
	IResource result = null;
	IProject projects[] = root.getProjects();
	if (projects != null)
	    {		
		for (int i = 0; i < projects.length; i++)
		    {
			IProject project = projects[i];
			if (project.isOpen())
			    {
				DataElement rootElement = findProjectElement(project);
				String source = rootElement.getSource();
				if (compareFileNames(source,fileName))
				    {
					return project;
				    }
				else
				    {
					result = findFile(project, rootElement, fileName);
					if (result != null)
					    {
						return result;
					    }
				    }
			    }
		    }
	    }
	
	return null;
    }

    public IResource findFile(IProject project, DataElement root, String fileName)
    {
	IResource result = null;
	ArrayList contents = root.getAssociated("contents");
	for (int i = 0; i < contents.size(); i++)
	    {
		DataElement child = (DataElement)contents.get(i);
		String source = child.getSource();

		// compare source to filename
		if (source.equals(fileName))
		    {
			if (child.isOfType("directory"))
			{
			    result = new org.eclipse.cdt.dstore.ui.resource.FolderResourceElement(child, project);	  	
			}
			else
			{
			    result = new org.eclipse.cdt.dstore.ui.resource.FileResourceElement(child, project);
			}
			addNewFile((IResource)result);
			return result;
		    }	
		else if (child.isOfType("directory"))
		    {
			if (fileName.startsWith(source))
			    {
				result = findFile(project, child, fileName);
				if (result != null)
				    {
					return result;
				    }
			    }
		    }
	    }

	return result;
    }


    public IResource findFile(IWorkspaceRoot root, java.io.File fileName)
    {
	IProject projects[] = root.getProjects();
	for (int i = 0; i < projects.length; i++)
	    {
		IProject project = projects[i];
		if (project.isOpen())
		    {
			IResource result = findFile(project, fileName);
			if (result != null)
			    return result;
		    }
	    }
	
	return null;
    }

    public IResource findFile(IContainer root, java.io.File fileName)
    {
	try
	    {
		if (root instanceof IResource)
		    {
			IResource container = (IResource)root;
			String rpath = container.getLocation().toString();
			if (compareFileNames(fileName, rpath))
			    {
				return container;
			    }
		    }
		
		    {
			IResource resources[] = root.members();
			if (resources != null)
			    {
				for (int i = 0; i < resources.length; i++)
				    {
					IResource resource = resources[i];
					
					String path = null;
					if (resource instanceof FileResourceElement)
					    {
						FileResourceElement res = (FileResourceElement)resource;
						path = res.getElement().getSource();
					    }
					else
					    {
						path = resource.getLocation().toString();
					    }
					
					
					if (compareFileNames(path, fileName))
					    {
						return resource;
					    }
					
					//if (fileName.startsWith(path) && resource instanceof IContainer)
					if (resource instanceof IContainer)
					    {
						IResource result = findFile((IContainer)resource, fileName);
						if (result != null)
						    return result;
					    }
				    }
			    }
		    }
	    }
	catch (CoreException e)
	    {
	    }
	
	return null;
    }

    public boolean compareFileNames(java.io.File f1, String file2)
    {
	java.io.File f2 = new java.io.File(file2);
	return (f1.compareTo(f2) == 0);
    }

    public boolean compareFileNames(String file1, java.io.File f2)
    {
	java.io.File f1 = new java.io.File(file1);
	return (f1.compareTo(f2) == 0);
    }

    public boolean compareFileNames(String file1, String file2)
    {
	java.io.File f1 = new java.io.File(file1);
	java.io.File f2 = new java.io.File(file2);
	return (f1.compareTo(f2) == 0);
    }

 public DataElement findParseFiles(DataElement theProjectFile)
    {
		DataStore dataStore = theProjectFile.getDataStore();
		DataElement parseProject = null;
		if (!theProjectFile.getType().equals("Namespace"))
		{
			ArrayList parseRef = theProjectFile.getAssociated("Parse Reference");
			if (parseRef != null && parseRef.size() > 0)
			{
				parseProject = ((DataElement)(parseRef.get(0))).dereference();
			}
		}
		else
		{
			parseProject = theProjectFile;
		}
		if (parseProject != null)
		{
			DataElement projectObjects = dataStore.find(parseProject, DE.A_NAME, "Project Objects", 1);
			return projectObjects;
		}
		else
	    {
			return null;
	    }
    }


 public DataElement findParseFile(DataElement theProjectFile)
 {
  String projectFileSource1 = theProjectFile.getSource().replace('\\','/');
  String projectFileSource2 = theProjectFile.getSource().replace('/','\\');
  DataStore dataStore = theProjectFile.getDataStore();


  while (!(theProjectFile = theProjectFile.getParent()).getType().equals("Project"))
      {
      }

     DataElement parseProject = ((DataElement)(theProjectFile.getAssociated("Parse Reference").get(0))).dereference();
     DataElement parsedFiles  = dataStore.find(parseProject, DE.A_NAME, "Parsed Files", 1);
     DataElement theParseFile = dataStore.find(parsedFiles, DE.A_SOURCE, projectFileSource1, 1);
     if (theParseFile == null)
	 {
	     theParseFile = dataStore.find(parsedFiles, DE.A_SOURCE, projectFileSource2, 1);
	 }

     if (theParseFile == null)
	 {
	     DataElement dummyInput = dataStore.find(dataStore.getTempRoot(), DE.A_NAME, "Non-Parsed File", 1);
	     if (dummyInput != null)
		 {
		     theParseFile = dummyInput;
		     DataElement theMessage = ((DataElement)(theParseFile.getNestedData().get(0)));
		     theMessage.setAttribute(DE.A_VALUE,projectFileSource1  + " has not been parsed.");
		     dataStore.refresh(theMessage);
		 }
	     else
		 {
		     theParseFile = dataStore.createObject(dataStore.getTempRoot(), "Output", "Non-Parsed File");
		     dataStore.createObject(theParseFile, "warning", projectFileSource1 + " has not been parsed.");
		 }
	 }


     return theParseFile;
 }

 	
    public DataElement getProjectFor(DataElement resource)
    {
	 if (resource != null)
	 {	
	     String rtype = resource.getType();
	     if (resource.isOfType("file") || rtype.equals("Closed Project"))
		 {
		     DataStore dataStore = resource.getDataStore();
		     if (!dataStore.isVirtual())
			 {
			     DataElement parent = resource;
			     String type = parent.getType();
			     while (parent != null &&			
				    !type.equals("Project") &&
				    !type.equals("Closed Project"))
				 {		
				     if (type.equals("temp") || type.equals("Root"))
					 {
					     parent = null;
					 }
				     else
					 {
					     parent = parent.getParent();
					     if (parent != null)
						 {
						     type = parent.getType();
						 }
					 }
				 }
			     return parent;
			 }
		     else
			 {
			     // virtual datastore
			     // -only one project per remote datastore
			     DataElement workspace = findWorkspaceElement(dataStore);
			     if (workspace != null)
				 {
				     DataElement project = workspace.get(0);
				     return project;
				 }
			 }
		 }
	     else
		 {
		     return null;
		 }
	 }
	 return null;
    }

  public IProject getProjectFor(IResource resource)
  {
    IProject project = null;
    IResource parent = resource;
    while ((project == null) && (parent != null))
      {	
	if (parent instanceof Repository)
	  {
	    project = (Repository)parent;
	    parent = null;
	  }	
	else if (parent instanceof IProject)
	  {
	    project = (IProject)parent;	
	    parent = null;
	  }
	else if (parent instanceof ResourceElement)
	    {
		project = parent.getProject();
		parent = null;
	    }
	else
	    {
		project = parent.getProject();
		parent = null;
	    }
      }

    return project;
  }


    public IResource getResource(String fileName)
    {
	return findFile(fileName);
    }


    private void synchronizeWithNavigator(IResource resource, DataElement parent)
    {
	if (resource instanceof IContainer)
	    {
		boolean needsRefresh = false;
		IContainer container = (IContainer)resource;
		
		// compare elements to resources	
		synchronized(parent)
		    {
			for (int i = 0; (i < parent.getNestedSize()); i++)
			    {
				DataElement child = parent.get(i);
				if (child != null && !child.isDeleted() && !child.isReference())
				    {
					IResource match = container.findMember(child.getName());
					
					needsRefresh = (match == null);
				    }			
			    }
		    }
		
		// compare resources to elements in case deleted
		if (!needsRefresh)
		    {
			try
			    {
				IResource[] members = container.members();
				for (int i = 0; (i < members.length) && !needsRefresh; i++)
				    {
						IResource member = members[i];
						DataStore dataStore = parent.getDataStore();
						DataElement match = dataStore.find(parent,
										       DE.A_NAME,
										       member.getName(),
										       1);
						needsRefresh = ((match == null) || match.isDeleted());					
						if (needsRefresh)
						{
												
						}
				    }
			    }
			catch (CoreException e)
			    {
				System.out.println(e);
			    }
		    }
		
		if (needsRefresh)
		    {
			try
			    {
				resource.refreshLocal(2, null);
			    }
			catch (CoreException e)
			    {
				System.out.println(e);
			    }					
		    }
		
	    }
    }
	
    public boolean listeningTo(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();

	if (_statuses.contains(parent))
	    {
		return true;
	    }	
	else
	    {		
		// This could cause slowdowns -- temporarily uncommenting for nav synchronization
		if (parent != null && !parent.getDataStore().isVirtual())
		    {		
			synchronized (parent)
			    {
				IResource resource = null;
				String type = parent.getType();
				
				if (type.equals("Project"))
				    {
					resource = findProjectResource(parent);
				    }
				else if (type.equals("directory"))
				    {	
					// is this part of a project?
					DataElement prj = getProjectFor(parent);
					if (prj != null)
					    {
						resource = findResource(parent);
					    }
				    }
			
				if (resource != null)
				    {
					synchronizeWithNavigator(resource, parent);
				    }
			    }
		    }
		return false;
	    }
    }

  public synchronized void domainChanged(DomainEvent ev)
  {
      DataElement object = (DataElement)ev.getParent();
      if ((object != null) && (object.getType().equals("status")))
      {	
	  monitorStatus(object);
	
	  DataElement commandInstance = object.getParent();
	  String commandName = (String)commandInstance.getElementProperty(DE.P_NAME);
	  String state = (String)object.getElementProperty(DE.P_NAME);	
	  if (state.equals("done"))
	      {               		
		  IResource  file        = null;
		  String fileName    = null;
		  String oldFileName = null;		
		  ArrayList children = ev.getChildren();

		  if (children != null)
		      {
			  int size = ev.getChildrenCount();
			
			  synchronized(children)
			      {
				  if (commandName.equals("C_SEARCH") || commandName.equals("C_SEARCH_REGEX"))
				      {
					      {
						  Display d = getDummyShell().getDisplay();
						  d.asyncExec(new CreateSearchMarkersAction(children));
						  if (_searchResultsView != null)
						      {
							  _searchResultsView.searchFinished();
						      }
					      }
				      }
				
				  else if (commandName.equals("C_COMMAND"))
				      {
					  for (int i = 0; i < size; i++)
					      {		
						  DataElement output = (DataElement)children.get(i);
						  	      {			
							  if (output.getDataStore().filter(_markersDescriptor, output))
							      {			
								  String type  = (String)(output.getElementProperty(DE.P_TYPE));
								  int priority;
								  if (type.equals("error"))
								      priority = IMarker.SEVERITY_ERROR;			
								  else if (type.equals("warning"))
								      priority = IMarker.SEVERITY_WARNING;
								  else
								      priority = IMarker.SEVERITY_INFO;	
								
								  fileName  = (String)(output.getElementProperty(DE.P_SOURCE_NAME));
								  Integer location = (Integer)(output.getElementProperty(DE.P_SOURCE_LOCATION));
								  int loc = location.intValue();
								
								  if (!fileName.equals(oldFileName))
								      {			    	
									  oldFileName = new String(fileName);
									
									  file = findFile(fileName);
								      }
								
								  if ( file != null)
								      {
									  try
									      {
										  IMarker errorMarker = file.createMarker(IMarker.PROBLEM);
										  errorMarker.setAttribute(IMarker.MESSAGE,
													   (String)output.getElementProperty(DE.P_VALUE));
										
										  errorMarker.setAttribute(IMarker.SEVERITY, priority);
										  errorMarker.setAttribute(IMarker.LINE_NUMBER, loc);
											
										  if (!_markedFiles.contains(file))	
											  _markedFiles.add(file);
									      }
									  catch (CoreException e)
									      {
									      }						
								      }		
							      }
						      }
						
					      }	
				      }
			      }
		      }
	      }
      }
  }


 private void deleteAssociatedMarkers(IProject project)
 {

 	for (int i = _markedFiles.size() - 1; i >= 0; i--)
 	{
 		IFile file = (IFile)_markedFiles.get(i);
 		if (file.getProject().equals(project))
 		{
 			try
	    	{
				file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);	
				
	    	}
			catch (CoreException e)
	    	{
	    	}
	    	
	    	_markedFiles.remove(file);
 		}
 	}

 }


    private void removeParseInfo(DataElement element)
    {
    	DataStore dataStore = element.getDataStore();
    	
    	if (element.isOfType("file") || element.isOfType("directory"))
	    {
    		DataElement notifyD = dataStore.localDescriptorQuery(dataStore.getRoot().getDescriptor(), "C_NOTIFICATION", 1);
		if (notifyD != null)
		    {
				ArrayList args = new ArrayList();
				args.add(element);
				DataElement delD = dataStore.createObject(null, "dummy command", "C_DELETE");
				dataStore.synchronizedCommand(notifyD, args, delD);
			}
    	}

		else
		{
    		DataElement parsedFile = null;
    		if (element.getType().equals("Parsed Source"))
    		{
    			parsedFile = element;
    		}
    	
			if (parsedFile != null)
			{
				DataElement rmParseInfo = dataStore.localDescriptorQuery(element.getDescriptor(), "C_REMOVE_PARSE", 3);
				if (rmParseInfo != null)
				{
					DataElement projectElement = getProjectFor(element);
					ArrayList args = new ArrayList();
					args.add(projectElement);
					DataElement status = dataStore.command(rmParseInfo, args, parsedFile);
					monitorStatus(status);				
				}									
			}    	
		}
    }








    private void parseFile(DataElement element)
    {
			DataStore dataStore = element.getDataStore();
					
			DataElement notifyD = dataStore.localDescriptorQuery(dataStore.getRoot().getDescriptor(), "C_NOTIFICATION", 1);
			if (notifyD != null)
			{
				ArrayList args = new ArrayList();
				args.add(element);
				DataElement addD = dataStore.createObject(null, "dummy command", "C_ADD");
				dataStore.command(notifyD, args, addD);
			}
    }

    private void resourceChanged(IResource resource)
    {
	if (resource != null)
	    {
		DataElement resourceElement = findResourceElement(resource);
		if (resourceElement != null &&
		    resourceElement.getType().equals("file"))
		    {
				resourceElement = resourceElement.getParent();
				resourceElement.doCommandOn("C_DATE", false);
		    }
		if (resourceElement != null)
		    {
			DataStore dataStore = resourceElement.getDataStore();
			
			
			DataElement refreshD = dataStore.localDescriptorQuery(resourceElement.getDescriptor(),
									      "C_REFRESH");
			if (refreshD != null)
			    {
				dataStore.command(refreshD, resourceElement);
			    }
		    }		
	    }
    }

    private void traverseDelta(IResourceDelta delta)
    {
	int kind  = delta.getKind();
	int flags = delta.getFlags();
	IResource resource = delta.getResource();


	if (resource != null)
	    {
		switch (kind)		
		    {
		    case IResourceDelta.CHANGED:			
			if ((flags & IResourceDelta.CONTENT) != 0)
			    {
				resourceChanged(resource);
				return;
			    }
			else if ((flags & IResourceDelta.OPEN) != 0)
			    {
				if (resource instanceof IProject)
				    {
					IProject project = (IProject)resource;
					if (_plugin.isCppProject(project))
					    {
						openProject(project);
					    }
					return;
				    }
			    }	
			else
			{
				// special case for rename
				IResourceDelta[] aff = delta.getAffectedChildren();
				if (aff.length >= 2)
				{
				
					IResourceDelta c1 = aff[0];
					IResourceDelta c2 = aff[1];
					
					IResource r1 = c1.getResource();
					IResource r2 = c2.getResource();
					
					int kind1 = c1.getKind();
					int kind2 = c2.getKind();
					
					IResource oldRes = null;
					IResource newRes = null;
					if (kind1 == IResourceDelta.REMOVED)
					{
						oldRes = c1.getResource();
						if (kind2 == IResourceDelta.ADDED)
						{
							newRes = c2.getResource();
						}
					}
					else if (kind1 == IResourceDelta.ADDED)
					{
						newRes = c1.getResource();
						if (kind2 == IResourceDelta.REMOVED)
						{
							oldRes = c2.getResource();
						}
					}
					
					if (oldRes != null && newRes != null)
						 {
						    // this is a rename
						 	if ((oldRes instanceof IResource) && (newRes instanceof IResource))
						 	{
						 		DataElement element = findResourceElement((IResource)oldRes);
						 		if (element != null)
						 		{						
						 			DataStore dataStore = element.getDataStore();
 						 	
									String oldName = element.getName();
									String oldSource = element.getSource();
									
									String newName = newRes.getLocation().toString();
									element.setAttribute(DE.A_NAME, newRes.getName());
						 			element.setAttribute(DE.A_SOURCE, newName);
						 								 													 		
						 			dataStore.refresh(element.getParent());
					
									DataElement notifyD = dataStore.localDescriptorQuery(dataStore.getRoot().getDescriptor(), "C_NOTIFICATION", 1);
									if (notifyD != null)
									{
										ArrayList args = new ArrayList();
										args.add(element);
										
										DataElement dummyOldName = dataStore.createObject(null, element.getType(), oldName, oldSource);
										args.add(dummyOldName);
										
										DataElement renameD = dataStore.createObject(null, "dummy command", "C_RENAME");
										dataStore.command(notifyD, args, renameD);
									}
								
								}
						 		else
						 		{
									String memberSource = oldRes.getLocation().toString().replace('\\', '/');
									String memberSource2 = oldRes.getLocation().toString().replace('/', '\\');
								
									IProject project = oldRes.getProject();
							
									DataElement projectElement = findProjectElement(project);	
									if (projectElement != null)
									{
										DataStore dataStore = projectElement.getDataStore(); 							
										DataElement parseProject = ((DataElement)(projectElement.getAssociated("Parse Reference").get(0))).dereference();
     									if (parseProject != null)
     									{
		     								DataElement parsedFiles  = dataStore.find(parseProject, DE.A_NAME, "Parsed Files", 1);
     										DataElement theParseFile = dataStore.find(parsedFiles, DE.A_SOURCE, memberSource, 1);
     										
     										if (theParseFile == null)
     										{
     											theParseFile = dataStore.find(parsedFiles, DE.A_SOURCE, memberSource2, 1);     											
     										}
     								
     										if (theParseFile != null)
     										{
      											removeParseInfo(theParseFile);
     										}
     									}	
									}	
     					 		}
						 	
						 		return;
						 	}
						 }
				
				}							
			}
		
			
			break;
		    case IResourceDelta.ADDED:
		    case IResourceDelta.REMOVED:
			{
			    if (resource instanceof IProject)
				{
					if (kind == IResourceDelta.ADDED)
					{
					    initializeProject((IProject)resource);
					}
				
				}
			    else
				{
					DataElement resourceElement = findResourceElement(resource);

					if (kind == IResourceDelta.ADDED)
					{
				    	if (resourceElement == null)
						{	
							// new file
						    IResource parent = resource.getParent();
						    DataElement parentElement = findResourceElement(parent);		
						    if (parentElement != null)
							{
							  parentElement.refresh(false);
							}
						}
					}
					else if (kind == IResourceDelta.REMOVED)
					{
						if (resourceElement !=  null)
						{
							removeParseInfo(resourceElement);	
							DataElement parentElement = resourceElement.getParent();
							parentElement.refresh(false);
						}
					}

 
				    /* THIS CAUSES A HANG WHEN IMPORTING LARGE NUMBERS OF FILES
				    System.out.println("added " + resource);
				    resourceChanged(resource.getParent());
				    resourceChanged(resource);
				    */
				}
			    return;
			}
		    default:
			break;
		    }

	    }

	IResourceDelta[] aff = delta.getAffectedChildren();
	for (int i = 0; i < aff.length; i++)
	    {
		traverseDelta(aff[i]);
	    }
    }

    public void resourceChanged(IResourceChangeEvent event)
    {
	int type = event.getType();
	IResourceDelta delta = event.getDelta();
  	IResource resource = event.getResource();

	switch (type)
	    {
	    case IResourceChangeEvent.POST_CHANGE:
		{		
		    traverseDelta(delta);
		}
		break;
	    case IResourceChangeEvent.PRE_CLOSE:
		{
		
		    if (resource instanceof IProject)
			{
			    closeProject((IProject)resource);
			    _plugin.setCurrentDataStore(_plugin.getDataStore());
			    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.CLOSE, (IProject)resource));
			}
			
		}
		break;
	    case IResourceChangeEvent.PRE_DELETE:
		{
		    if (resource instanceof IProject)
			{
			    closeProject((IProject)resource);
			    DataStore dataStore = _plugin.getCurrentDataStore();
			    if (dataStore != _plugin.getDataStore())
				{
				    _plugin.setCurrentDataStore(_plugin.getDataStore());
				}

			    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.DELETE, (IProject)resource));

			    // need to delete our representation of a project
			    DataElement cProject = findProjectElement((IProject)resource);
			    if (cProject != null)
				{
				    DataElement commandDescriptor = dataStore.localDescriptorQuery(cProject.getDescriptor(),
												   "C_DELETE_PROJECT");
				    if (commandDescriptor != null)
					{		
					    dataStore.command(commandDescriptor, cProject);	
					}				
				}
			}
		
		    resourceChanged(resource);
		}
		break;
	    default:
		break;
	    }
    }


    public void fireProjectChanged(int event, IProject project)
    {
	_projectNotifier.fireProjectChanged(new CppProjectEvent(event, project));
    }



}


