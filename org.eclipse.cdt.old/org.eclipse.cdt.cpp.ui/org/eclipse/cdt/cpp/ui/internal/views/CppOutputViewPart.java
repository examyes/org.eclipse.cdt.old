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


    protected CppPlugin         _plugin;  
	private Combo 				_inputEntry;
	private Button 				_sendButton;

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
                                               if (_inputEntry.getText().length() > 0)
                                               {
                                                 _sendButton.setEnabled(true);						
                                               }
                                               else
                                               {
                                                 _sendButton.setEnabled(false);
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
    	_viewer.enableActions();
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
		DataElement parent = (DataElement)ev.getParent();
		if (!parent.getName().equals("done"))
		{
			_inputEntry.setEnabled(true);
			if (_inputEntry.getText().length() > 0)
			{
				_sendButton.setEnabled(true);
			} 
			
			_viewer.enableActions();
		}
		else
		{
			_inputEntry.setEnabled(false);
			_sendButton.setEnabled(false);
			_viewer.enableActions();
		}
    }
    
    public void setInput(DataElement element)
    {
    	DomainNotifier notifier = element.getDataStore().getDomainNotifier();
		notifier.addDomainListener(_viewer);	
		notifier.addDomainListener(this);
		_viewer.setInput(element);		
		_viewer.enableActions();
    }
    
    public Shell getShell()
    {
	return _sendButton.getShell();
    }
    
}
