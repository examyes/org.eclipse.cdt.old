package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.views.*;

import org.eclipse.cdt.dstore.ui.ILinkable;
import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;

import org.eclipse.ui.*;
import org.eclipse.core.resources.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;
import java.util.*;

public class CppOutputViewPart extends OutputViewPart 
    implements ICppProjectListener, SelectionListener, 
    		IDomainListener, ISelectionListener

{
    
  public class CancelAction extends Action
  {
    public CancelAction(String name, ImageDescriptor image)
    {
      super(name, image);
  	  setToolTipText(name);
    }

      public void run()
      {
	  DataElement input = _viewer.getCurrentInput();
	  if (input != null)
	      {
		  DataElement command = input.getParent();
		  cancel(command);
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
  }

  public class ShellAction extends Action
  {
  	public ShellAction(String name, ImageDescriptor image)
  	{
  		super(name, image);
  		setToolTipText(name);
  	}
  	
  	public void run()
  	{
  		CppPlugin plugin = CppPlugin.getDefault();
  		ModelInterface api = plugin.getModelInterface();
  		IProject project = plugin.getCurrentProject();
  		if (project != null)
  		{
  			DataElement projectElement = api.findProjectElement(project);			
  			if (projectElement != null)
  			{
  				DataStore dataStore = projectElement.getDataStore();
  				DataElement shellD = dataStore.localDescriptorQuery(projectElement.getDescriptor(), "C_SHELL", 2);
  				if (shellD != null)
				{
	  				DataElement status = dataStore.command(shellD, projectElement);
	  				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				}
  			}
  		}
  	}
  }	

  public class HistoryAction extends Action
  {
    private DataElement _status;
    private int         _increment;

    public HistoryAction(String name, ImageDescriptor image, int increment)
    {
      super(name, image);
      _increment = increment;
       setToolTipText(name);
    }

      public void run()
    {
      DataElement status = _viewer.getCurrentInput();
      if (status != null)
      {
        DataElement command = status.getParent();

        DataStore dataStore = command.getDataStore();
        DataElement logRoot = dataStore.getLogRoot();

        ArrayList commands = logRoot.getNestedData();
        int thisIndex = commands.indexOf(command);

        DataElement newStatus = null;
        int newIndex = thisIndex + _increment;
        boolean found = false;
        while (!found && (newIndex > -1) && (newIndex < commands.size()))
        {
          DataElement newCommand = (DataElement)commands.get(newIndex);	
	  String commandName = newCommand.getName();

	  if (commandName.equals("C_COMMAND") ||
	      commandName.equals("C_SEARCH") ||
	      commandName.equals("C_SEARCH_REGEX"))
	    {	
	      newStatus  = newCommand.get(newCommand.getNestedSize() - 1);
	      if (newStatus != null)
		{	
		  found = (newStatus.getNestedSize() > 1);
		}	
	    }
	  newIndex += _increment;
	}
	
        if (found && newStatus != null)
        {
          setInput(newStatus);
        }
      }
    }
  }

    protected CppPlugin         _plugin;  
	private Combo 				_inputEntry;
	private Button 				_sendButton;
    private CancelAction 	 	  _cancelAction;
    private ShellAction			  _shellAction;
    private HistoryAction	      _backAction;
    private HistoryAction         _forwardAction;

	public CppOutputViewPart()
	{
		super();

	}

    public void createPartControl(Composite container)
    {
	_plugin = CppPlugin.getDefault();
	
	GridLayout gridLayout = new GridLayout();
	gridLayout.numColumns=1;
	container.setLayout(gridLayout);
	
	Table table = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	
	_viewer = new CppOutputViewer(table);
	TableLayout layout = new TableLayout();
	table.setLayout(layout);
	table.setHeaderVisible(true);
	
	layout.addColumnData(new ColumnWeightData(256));
	TableColumn tc = new TableColumn(table, SWT.NONE, 0);
	tc.setText("Output");
	
	GridData gridData = new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL);
	table.setLayoutData(gridData);
	
	
	Composite inputContainer = new Composite(container, SWT.NONE);
	GridLayout ilayout = new GridLayout();
	ilayout.numColumns = 4;
	
	Label label = new Label(inputContainer, SWT.NONE);
	label.setText("Standard Input");
	
	_inputEntry = new Combo(inputContainer, SWT.SINGLE|SWT.BORDER);
	_inputEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	_inputEntry.addModifyListener(
                                       new ModifyListener()
                                       {
                                         public void modifyText(ModifyEvent e)
                                             {
                                             	DataElement input = _viewer.getCurrentInput();
                                             	
                                                if (input != null && !input.getName().equals("done"))
                                                {
                                               		if (_inputEntry.getText().length() > 0)
                                               		{
                                                 	_sendButton.setEnabled(true);						
                                               		}
                                               		else
                                               		{
                                                	 _sendButton.setEnabled(false);
                                               		}
                                                }
                                                else
                                                {
                                                	_inputEntry.setEnabled(false);
                                                }
                                             }
                                       }
                                       );
	
	_sendButton = new Button(inputContainer, SWT.PUSH);
	_sendButton.setText("Send");
	_sendButton.addSelectionListener(this);

	_sendButton.setEnabled(false);
	_inputEntry.setEnabled(false);

 	GridData gridData1= new GridData(GridData.FILL_HORIZONTAL);
    inputContainer.setLayout(ilayout);
    inputContainer.setLayoutData(gridData1);
	
 	
	CppProjectNotifier notifier = _plugin.getModelInterface().getProjectNotifier();
	notifier.addProjectListener(this);

	updateViewBackground();
	updateViewForeground();
	updateViewFont();
	
	getSite().setSelectionProvider(_viewer);

	ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
	selectionService.addSelectionListener(this);

	
	fillLocalToolBar();
	
    }
    
    public void fillLocalToolBar()
    {
	IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	fillLocalToolBar(toolBarManager);
    }

    public void updateViewForeground()
    {
      ArrayList colours = _plugin.readProperty("OutputViewForeground");
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
      ArrayList colours = _plugin.readProperty("OutputViewBackground");
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
      ArrayList fontArray = _plugin.readProperty("OutputViewFont");
      if (fontArray.size() > 0)
	  {
	      String fontStr = (String)fontArray.get(0);
	      fontStr = fontStr.replace(',', '|');
	      FontData fontData = new FontData(fontStr);
	      _viewer.setFont(fontData);
	  }
      else
	  {
	      Font font = JFaceResources.getTextFont();
	      _viewer.setFont(font);
	      
	  }
    }
    
    public void selectionChanged(IWorkbenchPart p, ISelection s)
    {
    	enableActions();
    }

    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	switch (type)
	    {
	    case CppProjectEvent.VIEW_CHANGE:
		{
		    updateViewBackground();		
		    updateViewForeground();		
		    updateViewFont();
		}
		break;
		
		
	    default:
	    {
	    	_viewer.enableActions();
	    }
		break;
	    }
    }
    
    public void dispose()
    {
        IWorkbench aWorkbench = _plugin.getWorkbench();
        IWorkbenchWindow win= aWorkbench.getActiveWorkbenchWindow();
        win.getSelectionService().removeSelectionListener(this);
    }
	
 	public void widgetDefaultSelected(SelectionEvent e) 
    {
	  widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
  	 Widget source = e.widget;
	
	  if (source == _sendButton)
	    {
			sendInput();
	    }	    
    }
       
    public void sendInput()
    {
      DataElement command = _viewer.getCurrentInput().getParent();
	  DataStore dataStore = command.getDataStore();
	  DataElement commandDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Send Input");
	  if (commandDescriptor != null) 
	  {	
	  	 String stdIn = new String(_inputEntry.getText());
	  	 _inputEntry.setText("");
	  	 DataElement in = dataStore.createObject(null, "input", stdIn);
	  	 
	  	 ArrayList args = new ArrayList();
	  	 args.add(in);
	  	
		 dataStore.command(commandDescriptor, args, command);
		 _inputEntry.add(stdIn);
	  }
   }
   
    public boolean listeningTo(DomainEvent ev)
    {
      DataElement input = _viewer.getCurrentInput();
      if (input != null)
      {
      	DataElement parent = (DataElement)ev.getParent();
      	if (input == parent)
      	{
      		return true;
      	}	
      }
      
      return false; 
    }

    public void domainChanged(DomainEvent ev)
    {
		enableActions();
    }
    
    private void enableActions()
    {
    	DataElement input = _viewer.getCurrentInput();
      	if (input != null && !input.getName().equals("done"))
      	{
      		_inputEntry.setEnabled(true);
			if (_inputEntry.getText().length() > 0)
			{
				_sendButton.setEnabled(true);
			} 
			
			enableToolActions();
      	}
      	else
      	{
      		_inputEntry.setEnabled(false);
			_sendButton.setEnabled(false);
			enableToolActions();	
      	}
    }
    
    public void setInput(DataElement element)
    { 
    	DomainNotifier notifier = element.getDataStore().getDomainNotifier();
		notifier.addDomainListener(_viewer);	
		notifier.addDomainListener(this);
		_viewer.setInput(element);		
		enableActions();
    }
    
    public Shell getShell()
    {
	return _sendButton.getShell();
    }
    
  public void fillLocalToolBar(IToolBarManager toolBarManager)
  {
  	CppPlugin plugin = CppPlugin.getDefault();
  	
  	_shellAction = new ShellAction("Launch Shell",plugin.getImageDescriptor("command"));
    toolBarManager.add(_shellAction);
 
  	_backAction  = new HistoryAction(plugin.getLocalizedString("OutputViewer.back"),
					 plugin.getImageDescriptor("back"), -1);
    toolBarManager.add(_backAction);
    
    _forwardAction = new HistoryAction(plugin.getLocalizedString("OutputViewer.forward"),
					 plugin.getImageDescriptor("forward"), 1);
    toolBarManager.add(_forwardAction);
    
    _cancelAction = new CancelAction(plugin.getLocalizedString("OutputViewer.Cancel"),
					plugin.getImageDescriptor("cancel"));
    toolBarManager.add(_cancelAction);


    enableActions();
  } 
    
  private void enableToolActions()
  {
  	if (_cancelAction == null)
  	{
  		fillLocalToolBar();	
  	}
  	
  	DataElement input = _viewer.getCurrentInput();
  	if (input != null && !input.getName().equals("done"))
		{		
			_cancelAction.setEnabled(true);  	
		}
	else
		{
			_cancelAction.setEnabled(false);
		}
	
	DataElement currentInput = _viewer.getCurrentInput();
	_backAction.setEnabled(currentInput != null);
	_forwardAction.setEnabled(currentInput != null);
	
	CppPlugin plugin = CppPlugin.getDefault();
	IProject project = plugin.getCurrentProject();
	if (project != null && project.isOpen())
	{
		_shellAction.setEnabled(true);
	}
	else
	{
		_shellAction.setEnabled(false);
	}
  }
    
}
