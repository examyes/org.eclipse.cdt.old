package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.ILinkable;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.extra.internal.extra.*;

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

public class ModelInterface implements IDomainListener, IResourceChangeListener
{
    public class MonitorStatusThread extends Handler
    {
	private DataElement _status;
	
	public MonitorStatusThread(DataElement status)
	{
	    _status = status;
	    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.COMMAND, 
								    CppProjectEvent.START, 
								    _status,
								    _plugin.getCurrentProject()));
	}
	
	public void handle()
	{	  
	    if (_status.getName().equals(_status.getDataStore().getLocalizedString("model.done")))
		{
		    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.COMMAND, 
									    CppProjectEvent.DONE,
									    _status,
									    _plugin.getCurrentProject()));
		    finish();
		}
	    else
		{
		    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.COMMAND, 
									    CppProjectEvent.WORKING,
									    _status,
									    _plugin.getCurrentProject()));
		}
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
	if (resource == null || !(resource instanceof IFile))
	    return;

	IWorkbenchPage perspective= SearchPlugin.getActivePage();
	try {
	    perspective.openEditor(marker);
	}
	catch (PartInitException e) {
	    SearchPlugin.beep();
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
	    IFile file = null;
	    for (int i = 0; i < _results.size(); i++)
		{		
		    DataElement output = ((DataElement)_results.get(i)).dereference();

		    String fileName  = (String)(output.getElementProperty(DE.P_SOURCE_NAME));
		    Integer location = (Integer)(output.getElementProperty(DE.P_SOURCE_LOCATION));
		    int loc = location.intValue();
		
		    file = getNewFile(fileName);
		    
		    if (file == null)
			{
			    DataElement fileElement = output.getDataStore().createObject(null, "file", fileName, fileName);
			    file = new FileResourceElement(fileElement, (IProject)CppPlugin.getCurrentProject());
			}
		    if (file != null)
			{
			    try
				{
				    IMarker searchMarker = file.createMarker(SearchUI.SEARCH_MARKER);		
				    String message = output.getName();				
				    HashMap attributes = new HashMap(5);
				    attributes.put(IMarker.MESSAGE, message);
				    attributes.put(IMarker.CHAR_START, new Integer(0));
				    attributes.put(IMarker.CHAR_END, new Integer(message.length()));
				    attributes.put(IMarker.LINE_NUMBER, new Integer(loc));
				    attributes.put("DataElementID", output);
				    searchMarker.setAttributes(attributes);
				
				    SearchUI.getSearchResultView().addMatch(message, message, file, searchMarker);

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
  private IWorkspace     _workbench;

  private String         _workbenchDirectory;

  private ArrayList      _statuses;
  private ArrayList      _viewers;

  private ArrayList      _markedFiles;
  private DataElement    _status;

  private ISearchResultView _searchResultsView;
  private Shell             _dummyShell;

  private static CppProjectNotifier _projectNotifier;

  private static ModelInterface _instance;

  public ModelInterface(DataStore dataStore)
  {
    _workbench = WorkbenchPlugin.getPluginWorkspace();
    _workbench.addResourceChangeListener(this);

    Path workbenchPath = (Path)Platform.getLocation();
    _workbenchDirectory = workbenchPath.toString();

    _plugin = CppPlugin.getDefault();

    _statuses = new ArrayList();
    _viewers = new ArrayList();
    _markedFiles = new ArrayList();

    _projectNotifier = new CppProjectNotifier();
    _projectNotifier.enable(true);
    
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

  public void loadSchema()
      {
	  if (_plugin == null)
	      _plugin = CppPlugin.getDefault();

	  DataStore dataStore = _plugin.getDataStore(); 
	  DataElement schemaRoot    = dataStore.getDescriptorRoot();
	  dataStore.showTicket(dataStore.getTicket().getName());
	  dataStore.getSchema();
	  dataStore.initMiners();
	  
	  // extend schema from UI side
	  extendSchema(dataStore.getDescriptorRoot());
	  
	  _markersDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "markers", 1);
	  
	  dataStore.getDomainNotifier().addDomainListener(this);	
      }

  public Shell getShell()
  {
      if (_dummyShell != null)
	  {
	      return _dummyShell;
	  }

      Shell shell = new Shell();
      return shell;
  }

  public Shell getDummyShell()
  {
    if (_dummyShell == null)
      {	
	_plugin.getDataStore().getDomainNotifier().enable(true);
	IWorkbenchWindow win = _plugin.getActiveWorkbenchWindow();

	if (win != null)
	  {	
	    _dummyShell = win.getShell();
	  }
	else
	  {
	    _dummyShell = new Shell();	
	  }
	
	_plugin.getDataStore().getDomainNotifier().setShell(_dummyShell);
      }

    return _dummyShell;
  }

  public void showView(String id, DataElement input)
      {
	Display d= getDummyShell().getDisplay();
	
	ShowViewAction action = new ShowViewAction(id, input);
	d.asyncExec(action);
      }

    public void monitorStatus(DataElement status)
    {
	if (!_statuses.contains(status))
	    {
		_statuses.add(status);

		MonitorStatusThread monitor = new MonitorStatusThread(status);
		monitor.setWaitTime(1000);
		monitor.start();
	    }
	else if (status.getName().equals("done"))
	    {
		_statuses.remove(status);
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
    DataElement pathElement = _plugin.getCurrentDataStore().createObject(null, "directory", path, path);
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
	args.add(invocationObj);
	args.add(findProjectElement(CppPlugin.getCurrentProject()));
	
	try
	    {
		_workbench.getRoot().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);	
	    }
	catch (CoreException e)
	    {
	    }

        
	
	DataElement commandDescriptor = dataStore.localDescriptorQuery(pathElement.getDescriptor(), "C_COMMAND");
	DataElement status = dataStore.command(commandDescriptor, args, pathElement, true);		
	invocationObj.setParent(status.getParent());
	_status = status;

	monitorStatus(status);
	
	showView("com.ibm.dstore.hosts.views.OutputViewPart", _status);
	return status;	
      }

    return null;
  }

    public void openProjects()
    {
	// open all local projects
	IProject[] projects = _workbench.getRoot().getProjects();
	for (int i = 0; i < projects.length; i++)
	    {	
		if (projects[i].isOpen())
		    {
			openProject(projects[i]);
		    }
	    }			    	
    }

    public void openProject(IProject project)
    {
	if (project.isOpen())
	    {
		OpenProjectAction openAction = new OpenProjectAction(project);
		Display d= getShell().getDisplay();
		d.asyncExec(openAction);		
	    }   
    }

  private class OpenProjectAction implements Runnable
  {
   private IProject _project;
   public OpenProjectAction(IProject project)
   {
    _project = project;
   }
   
   public void run()
    {
     DataStore dataStore = _plugin.getDataStore();
     
     if (_project instanceof Repository)
      dataStore = ((Repository)_project).getDataStore();	

     DataElement projectMinerProject =  findProjectElement(_project);
     if (projectMinerProject == null)
       return;
     DataElement oDescriptor = dataStore.localDescriptorQuery(projectMinerProject.getDescriptor(), "C_OPEN", 4);
     if (oDescriptor != null)
	 {
	     dataStore.synchronizedCommand(oDescriptor, projectMinerProject);
	 }
     setParseIncludePath(_project);	
     setParseQuality(_project);	
     setEnvironment(_project);
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

    private void closeEditors()
    {
	IProject[] projects = _workbench.getRoot().getProjects();

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
						
						if (file instanceof ResourceElement)
						    {
							if (getProjectFor(file) == project)
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
    }

    public void shutdown()
    {
	_projectNotifier.enable(false);

	DataStore dataStore = _plugin.getCurrentDataStore();

	_workbench.removeResourceChangeListener(this);

        dataStore.getDomainNotifier().removeDomainListener(this);		
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
    DataElement commandDescriptor = dataStore.localDescriptorQuery(solutionObj.getDescriptor(), "C_CLOSE_PROJECTS");
    if (commandDescriptor != null)
      {		
	  dataStore.cancelAllCommands();
	  dataStore.synchronizedCommand(commandDescriptor, solutionObj);	
	  dataStore.cleanBadReferences(dataStore.getLogRoot());
      }
  }

  public void closeProject(IProject project)
  {
    closeEditors(project);

    DataStore dataStore = _plugin.getDataStore();	
    if (project instanceof Repository)
	{
	    dataStore = ((Repository)project).getDataStore();		    
	}
    
    // cancel all pending commands
    dataStore.cancelAllCommands();

    DataElement workspace = findWorkspaceElement(dataStore);	    
    DataElement cprojectObj = dataStore.find(workspace, DE.A_NAME, project.getName(), 1);

    if (cprojectObj != null)
	{
	    DataElement closeD = dataStore.localDescriptorQuery(cprojectObj.getDescriptor(), "C_CLOSE_PROJECT");
	    
	    if (closeD != null)
		{
		    dataStore.synchronizedCommand(closeD, cprojectObj);
		}
	}

    // for parser
    DataElement projectObj = findProjectElement(project);
    if (projectObj != null)
	{
	    DataElement commandDescriptor = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_CLOSE_PROJECT");
	    
	    if (commandDescriptor != null)
		{		
		    dataStore.synchronizedCommand(commandDescriptor, projectObj);	
		}
	}
  }

    public void saveProject(IProject project)
    {
	DataStore dataStore = _plugin.getDataStore();	
	if (project instanceof Repository)
	    dataStore = ((Repository)project).getDataStore();	
	
	DataElement projectObj = findProjectElement(project);
	DataElement commandDescriptor = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_SAVE_PROJECT");
	
	if (commandDescriptor != null)
	    {		
		dataStore.command(commandDescriptor, projectObj, false, true);	
	    }	
    }

    public void clearProject(IProject project)
    {
	DataStore dataStore = _plugin.getDataStore();	
	if (project instanceof Repository)
	    dataStore = ((Repository)project).getDataStore();	
	
	DataElement projectObj = findProjectElement(project);
	DataElement commandDescriptor = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_REMOVE_PARSE");
			
	if (commandDescriptor != null)
	    {		
		dataStore.command(commandDescriptor, projectObj, false, true);	
	    }	
    }

  public void setEnvironment(IProject project)
  {
      DataStore dataStore = _plugin.getDataStore();	
      if (project instanceof Repository)
	  dataStore = ((Repository)project).getDataStore();	

      DataElement projectObj = findProjectElement(project);

      if (projectObj != null)
	  {	
	      DataElement environmentElement = dataStore.createObject(null, "Environment Variable", project.getName() + "." + "Environment");	
	      ArrayList envVars = _plugin.readProperty(project, "Environment");
	      for (int i = 0; i < envVars.size(); i++)
		  {
		      dataStore.createObject(environmentElement, "Environment Variable", (String)envVars.get(i), (String)envVars.get(i));
		  }

	      DataElement setD = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_SET_ENVIRONMENT_VARIABLES");
	      if (setD != null)
		  {  
		     
		      ArrayList args = new ArrayList();
		      args.add(environmentElement);	
		      
		      DataElement theObject = dataStore.find(projectObj, DE.A_NAME, "Preferences",1);
		      if (theObject != null)
			  {
			      dataStore.command(setD, environmentElement, theObject, true);
			  }
		  }
	  }
  }

  public void setParseIncludePath(IProject project)
  {
   DataStore dataStore = _plugin.getDataStore();	
   if (project instanceof Repository)
     dataStore = ((Repository)project).getDataStore();	

   DataElement projectObj = findProjectElement(project);
   
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

  public void setParseQuality(IProject project)
  {
   DataStore dataStore = _plugin.getDataStore();	
   if (project instanceof Repository)
     dataStore = ((Repository)project).getDataStore();	

   DataElement projectObj = findProjectElement(project);
   DataElement qualityElement = dataStore.createObject(null, "quality", "2");
   ArrayList quality = _plugin.readProperty(project, "ParseQuality");

   if (!quality.isEmpty())
   {
    String qualityStr = (String)quality.get(0);
    qualityElement.setAttribute(DE.A_NAME, qualityStr);
   }
   
   DataElement setD = dataStore.localDescriptorQuery(projectObj.getDescriptor(), "C_SET_PREFERENCES");
   if (setD != null)
   {
    ArrayList args = new ArrayList();
    args.add(qualityElement);			
    dataStore.command(setD, qualityElement, projectObj, true);
   }
  }

    public DataElement findWorkspaceElement(DataStore dataStore)
    {
	DataElement projectInfo = dataStore.findMinerInformation("com.ibm.cpp.miners.project.ProjectMiner");
	DataElement workspaceObj = dataStore.find(projectInfo, DE.A_TYPE, "Workspace", 1);
	return workspaceObj;
    }  

  public DataElement findProjectElement(IProject project)
  {
   if (project == null)
    return null;
   
   DataStore dataStore = _plugin.getDataStore();	
   if (project instanceof Repository)
    dataStore = ((Repository)project).getDataStore();	
	
   DataElement workspace = findWorkspaceElement(dataStore);
   if (workspace == null)
    return null;
   
   DataElement projectObj = dataStore.find(workspace, DE.A_NAME, project.getName(), 1);
   if (projectObj == null)
    projectObj = dataStore.createObject(workspace, "Project", project.getName(), project.getLocation().toString());
   return projectObj;
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
   DataStore dataStore = null;
   DataElement pathElement = null;
	
   if (resource instanceof ResourceElement)
   {
    ResourceElement myResource = (ResourceElement)resource;	
    dataStore = myResource.getDataStore();	
    pathElement = myResource.getElement();	
   }
   else if (resource instanceof Repository)
   {
    Repository myRes = (Repository)resource;	
    dataStore = myRes.getDataStore();	
    pathElement = myRes.getElement();	    	    	
   }
   else
   {
    dataStore = _plugin.getCurrentDataStore();	
    pathElement = ConvertUtility.convert(dataStore, dataStore.getHostRoot(), resource);
   }
    
   IProject theProject = getProjectFor(resource);
   
   if (theProject != null)
   {	    
    String name = new String(theProject.getName());
    DataElement commandDescriptor = dataStore.localDescriptorQuery(pathElement.getDescriptor(), "C_PARSE");

    DataElement projectsRoot = findWorkspaceElement(dataStore);		
    DataElement projectRoot = dataStore.find(projectsRoot, DE.A_NAME, name, 1);

    if (commandDescriptor != null && projectRoot != null)
    {	
     projectRoot.setAttribute(DE.A_SOURCE, theProject.getLocation().toString());	
     if (theProject != null)
     {
      boolean changed = false;
      DataElement includeElement = dataStore.find(projectRoot, DE.A_NAME, "Include Path", 1);
      if (includeElement == null)
      {
       includeElement = dataStore.createObject(projectRoot, "environment", "Include Path");
       changed = true;
      }
      ArrayList includePaths = _plugin.readProperty(theProject, "Include Path");
      for (int i = 0; i < includePaths.size(); i++)
      {
       DataElement aPath = dataStore.find(includeElement, DE.A_NAME, (String)includePaths.get(i), 1);
       if (aPath == null)
       {		
	dataStore.createObject(includeElement, "directory", (String)includePaths.get(i), (String)includePaths.get(i));
	changed = true;
       }
      }

      // refresh remote
      if (changed)
      {
	  dataStore.setObject(projectRoot);
	  dataStore.setObject(includeElement);	
      }
     }
     ArrayList args = new ArrayList();	
     args.add(projectRoot);
     dataStore.getDomainNotifier().addDomainListener(this);	

     DataElement status = null;
     if (isSynchronized == false)
     {	
      if (pathElement.getSource().equals(projectRoot.getSource()))
      {
       status = dataStore.command(commandDescriptor, args, projectRoot, false);		
      }
      else
      {
       status = dataStore.command(commandDescriptor, args, pathElement, false);		
      }
      monitorStatus(status);
     }
     else
     {
      status = dataStore.synchronizedCommand(commandDescriptor, args, pathElement);			
     }
     _status = status;
    if (showView)
    {		
     showView("com.ibm.cpp.ui.internal.views.ParsedSourceViewPart", null);
     showView("com.ibm.cpp.ui.internal.views.DetailsViewPart", null);
    }		
   }
  }	
 }


  public synchronized void search(String pattern, ArrayList types, ArrayList relations, 
				  boolean ignoreCase, boolean regex)
  {
    DataElement subject = findWorkspaceElement(_plugin.getCurrentDataStore());
    search(subject, pattern, types, relations, ignoreCase, regex);
  }

  public synchronized void search(Object input, String pattern, ArrayList types, ArrayList relations, 
				  boolean ignoreCase, boolean regex)
  {
    if (input instanceof DataElement)
      {
	search((DataElement)input, pattern, types, relations, ignoreCase, regex);	
      }
    else
      {
	search(pattern, types, relations, ignoreCase, regex);	
      }
  }

  public synchronized void search(DataElement subject, String pattern, ArrayList types, ArrayList relations, 
				  boolean ignoreCase, boolean regex)
  {
      DataStore dataStore = subject.getDataStore();
      DataElement searchDescriptor = null;

      if (regex)
	  {
	      searchDescriptor = dataStore.localDescriptorQuery(subject.getDescriptor(), "C_SEARCH");
	  }
      else
	  {
	      searchDescriptor = dataStore.localDescriptorQuery(subject.getDescriptor(), "C_SEARCH_REGEX");
	  }

    if (searchDescriptor == null)
    {
      searchDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Search");
    }
    if (searchDescriptor != null)
    {
      ArrayList args = new ArrayList();
      DataElement patternElement = dataStore.createObject(null, "pattern", pattern);

      patternElement.setAttribute(DE.A_VALUE, ignoreCase ? "ignore_case" : "check_case" );
      args.add(patternElement);

      for (int i = 0; i < types.size(); i++)
      {
	String type = (String)types.get(i);
	DataElement objDescriptor = dataStore.findObjectDescriptor(type);
	if (objDescriptor != null)
	    {
		dataStore.createReference(patternElement, objDescriptor);
	    }
      }


      if (dataStore == _plugin.getDataStore())
	  {
	      _searchResultsView = SearchUI.getSearchResultView();
	      
	      StringBuffer patternStr = new StringBuffer("Pattern: " + pattern + " Types: ");
	      for (int i = 0; i < types.size(); i++)
		  {
		      patternStr.append((String)types.get(0));
		      if (i < types.size()) patternStr.append(", ");
		  }

	      _searchResultsView.searchStarted(
					       "com.ibm.cpp.ui.internal.views.CppSearchPage",
					       patternStr.toString(),
					       CppPlugin.getDefault().getImageDescriptor("details.gif"),//null,
					       null,
					       SearchLabelProvider.getInstance(),
					       new ShowMarkerAction(),
					       new GroupByKeyComputer(),
					       null);		
	      
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

      // needed because eclipse doesn't support remote files
      if (dataStore != _plugin.getDataStore())
	  {
	      showView("com.ibm.dstore.hosts.views.OutputViewPart", _status);
	  }
    }
  }


  public void cancel(DataElement command)
  {
      DataStore dataStore = command.getDataStore();
      DataElement commandDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Cancel");
      if (commandDescriptor != null)
	  {	
	      dataStore.command(commandDescriptor, command, false, true);
	  }
  }



  public IFile getNewFile(String fileName)
  {
    IFile file = null;

    if (fileName != null)
      {		  
	  IWorkspace ws = _plugin.getPluginWorkspace();
	  IWorkspaceRoot root = ws.getRoot();

	  file = findFile(root, fileName);
	  if (file == null)
	      {		  
		  RemoteProjectAdapter rmt = RemoteProjectAdapter.getInstance();		  
		  if (rmt != null)
		      file = findFile(rmt, fileName);
	      }
      }

    return file;
  }

    public IFile findFile(RemoteProjectAdapter root, String fileName)
    {
	IProject projects[] = root.getProjects();
	if (projects != null)
	    {
		for (int i = 0; i < projects.length; i++)
		    {
			IFile result = findFile(projects[i], fileName);
			if (result != null)
			    return result;
		    }
	    }
	
	return null;
    }

    public IFile findFile(IWorkspaceRoot root, String fileName)
    {
	IProject projects[] = root.getProjects();
	for (int i = 0; i < projects.length; i++)
	    {
		IFile result = findFile(projects[i], fileName);
		if (result != null)
		    return result;
	    }
	
	return null;
    }

    public IFile findFile(IContainer root, String fileName)
    {
	try
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
					return (IFile)resource;
				    }
				
				if (fileName.startsWith(path) && resource instanceof IContainer)
				    {
					IFile result = findFile((IContainer)resource, fileName);
					if (result != null)
					    return result;
				    }
			    }
		    }
	    }
	catch (CoreException e)
	    {
	    }
	
	return null;
    }

    private boolean compareFileNames(String file1, String file2)
    {
	String f1 = file1.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
	String f2 = file2.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
	return f1.equalsIgnoreCase(f2);
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
    // filename is fully qualified
    // need to map to eclipse!
    String newFileName = null;
    IResource fileHandle = null;

    if ((fileName != null) && (fileName.startsWith(_workbenchDirectory)))
      {	
	int len = _workbenchDirectory.length();
	newFileName = fileName.substring(len, fileName.length());
	IPath path = new Path(newFileName);    	

	if (path != null)
	  {	
	    if (path.segmentCount() == 1)
	      {		
		fileHandle = (IResource)_workbench.getRoot().getProject(path.toString());
	      }
	    else
	      {
		fileHandle = (IResource)_workbench.getRoot().getFolder(path);
		if (fileHandle == null)
		  {
		    fileHandle = (IResource)_workbench.getRoot().getFile(path);
		  }
	      }
	  }
      }

    return fileHandle;
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
		  IFile  file        = null;
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
					  if (object.getDataStore() == _plugin.getDataStore())
					      {
						  Display d = getDummyShell().getDisplay();
						  d.asyncExec(new CreateSearchMarkersAction(children));
						  if (_searchResultsView != null)
						      {
							  _searchResultsView.searchFinished();
						      }
					      }
				      }
				  else
				      {
					  for (int i = 0; i < size; i++)
					      {		
						  DataElement output = (DataElement)children.get(i);
						  if (commandName.equals("C_COMMAND")) // handle batch command markers
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
									  
									  file = getNewFile(fileName);
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


    private void resourceChanged(IResource resource)
    {
	IProject project = _plugin.getCurrentProject();
	if (resource != null)
	    {
		project = getProjectFor(resource);
	    }

	DataElement cProject = findProjectElement(project);
	if (cProject != null)
	    {
		DataStore dataStore = cProject.getDataStore();
		DataElement refreshD = dataStore.localDescriptorQuery(cProject.getDescriptor(), "C_REFRESH_PROJECT");
		if (refreshD != null)
		    dataStore.command(refreshD, cProject);
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
		    openProjects();
		    if (resource == null)
			{
			    resourceChanged(resource);
			}
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
			    _plugin.setCurrentDataStore(_plugin.getDataStore());
			    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.DELETE, (IProject)resource));
			}

		    resourceChanged(resource);
		}
		break;
	    default:
		break;
	    }
    }


    // temporary place for extending schema from UI side
    public void extendSchema(DataElement schemaRoot)
    {
	DataStore   dataStore = schemaRoot.getDataStore();
	DataElement dirD = dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement rootD = dataStore.find(schemaRoot, DE.A_NAME, "root", 1);

	DataElement statement = dataStore.find(schemaRoot, DE.A_NAME, "statement", 1);
	DataElement function  = dataStore.find(schemaRoot, DE.A_NAME, "function", 1);
	DataElement classD    = dataStore.find(schemaRoot, DE.A_NAME, "class", 1);

	/*
	  // example of adding an action to a element
	DataElement statementBreakpoint  = dataStore.createObject(statement, DE.T_UI_COMMAND_DESCRIPTOR, 
								  _plugin.getLocalizedString("model.Add_Breakpoint"), 
								  "com.ibm.cpp.ui.interanl.actions.AddStatementBreakpoint");	 
	statementBreakpoint.setAttribute(DE.A_VALUE, "C_ADD_STATEMENT_BREAKPOINT");	
	
	DataElement functionBreakpoint  = dataStore.createObject(function, DE.T_UI_COMMAND_DESCRIPTOR, 
								 _plugin.getLocalizedString("model.Add_Breakpoint"), 
								 "com.ibm.cpp.ui.internal.actions.AddFunctionBreakpoint");	 
	functionBreakpoint.setAttribute(DE.A_VALUE, "C_ADD_FUNCTION_BREAKPOINT");	
	
	DataElement classBreakpoint  = dataStore.createObject(classD, DE.T_UI_COMMAND_DESCRIPTOR, 
							      _plugin.getLocalizedString("model.Add_Breakpoint"), 
							      "com.ibm.cpp.ui.internal.actions.AddClassBreakpoint");	 
	classBreakpoint.setAttribute(DE.A_VALUE, "C_ADD_CLASS_BREAKPOINT");		
	*/
	
	
	DataElement connect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						     dataStore.getLocalizedString("model.Connect_to"), 
						     "com.ibm.dstore.ui.connections.ConnectAction");
        connect.setAttribute(DE.A_VALUE, "C_CONNECT");
	
	DataElement disconnect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
							dataStore.getLocalizedString("model.Disconnect_from"), 
							"com.ibm.dstore.ui.connections.DisconnectAction");	 
        disconnect.setAttribute(DE.A_VALUE, "C_DISCONNECT");
	
	DataElement editConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						  "Edit Connection", 
						  "com.ibm.dstore.ui.connections.EditConnectionAction");	 
        editConnection.setAttribute(DE.A_VALUE, "C_EDIT");

	DataElement removeConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						    dataStore.getLocalizedString("model.Delete_Connection"), 
						    "com.ibm.dstore.ui.connections.DeleteAction");	 
        removeConnection.setAttribute(DE.A_VALUE, "C_DELETE");

	
    }


    // methods for debugger team
    public static ArrayList getFunctions(IFile resource)
    {
	ArrayList results = new ArrayList();
	DataElementAdapter adapter = DataElementAdapter.getInstance();
	DataElement fileElement = adapter.parse(resource);
	if (fileElement != null)
	    {
		DataStore dataStore = fileElement.getDataStore();
		DataElement functionDescriptor = dataStore.findObjectDescriptor("Function");		
		ArrayList functionList = dataStore.findObjectsOfType(fileElement, functionDescriptor);

		for (int i = 0; i < functionList.size(); i++)
		    { 
			DataElement function = (DataElement)functionList.get(i);
			String name = function.getName();
			String type = function.getType();
			results.add(name);
		    }
	    }
	
	return results;
    }
}




