package com.ibm.dstore.hosts.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.dialogs.*;

import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.*;

import java.util.*;
import java.io.*;

import org.eclipse.jface.viewers.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;

import org.eclipse.ui.dialogs.*;

public class CommandViewer extends Viewer implements SelectionListener
{  	
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
	    //	  try
	    {
		if (_input != null)
		    viewPart.setInput(_input);	
		//persp.showView(_id);	       
	    }
	    //	  catch (PartInitException e)
	      {
	      }
	} 
    }
  }



  private final static int	SIZING_SELECTION_WIDGET_HEIGHT = 150;
  private final static int	SIZING_SELECTION_WIDGET_WIDTH = 300;
  private static final int	SIZING_TEXT_FIELD_WIDTH = 100;
  private static final int	SIZING_BUTTON_WIDTH = 50;

  protected String             _command;

  private Combo              _commandText;
  private Text               _directoryText;

  protected Button             _runButton;
  protected Button             _browseButton;

  private boolean            _hasFocus;
    private String           _outputViewId;

  protected DataElement      _input;
  protected ArrayList        _history;
  private HostsPlugin        _plugin = HostsPlugin.getDefault();

    public CommandViewer(Composite parent)
    {
	super();
	doCreateControl(parent);
	_outputViewId = "com.ibm.dstore.hosts.views.OutputViewPart";
    }
    
    public void setOutputId(String outputId)
    {
	_outputViewId = outputId;
    }

    public Control getControl()
    {
	return null;
    }
       
    public ISelection getSelection()
    {
	return null;
    }
    
    public void setSelection(ISelection sel, boolean flag)
    {
    }
    
    public void refresh()
    {
    }

    
    
    public Object getInput()
    {
	return _input;
    }
    
    public void setInput(Object input)
    {
	if (input != null && input != _input)	
	    {
		if (input instanceof IResource)
		    {
			DataStore dataStore = DataStoreCorePlugin.getInstance().getCurrentDataStore();
			if (dataStore != null)
			    {
				IResource resource = (IResource)input;
				if (resource instanceof IFile)
				    {
					resource = resource.getParent();
				    }

				
				input = dataStore.createObject(null, "directory", 
							       resource.getName(), 
							       resource.getLocation().toString());
			    }
		    }
		if (input instanceof DataElement)
		    {
			setElementInput((DataElement)input);
		    }
	    }	

    }

    protected void setElementInput(DataElement input)
    {
	String type = input.getType();
	DataElement descriptor = input.getDescriptor();
	if (type.equals("file") || (descriptor != null && input.getDescriptor().isOfType("file")))
	{
		if ((descriptor != null) && descriptor.isOfType("Filesystem Objects"))
		 {
			_input = input;
			String directory = input.getSource();				
			if ((directory != null) && (_directoryText != null))
			    {		
				try
				    {		
					_directoryText.setText(directory);
				    }
				catch (SWTException e)
				    {
					System.out.print(e);		
				    }
			    }			
		    }
		else if (input.getType().equals("file"))
		    {
			setElementInput(input.getParent());
		    }
		if (_history != null)
		    {
			_history.removeAll(_history);
		    }
		
		updateCombo();	
	}		
    }

    protected void updateCombo()
    {
	_history = readHistory();
	if (_history != null)
	    {
		_commandText.removeAll();
		for (int i = 0; i < _history.size(); i++)
		    {
			String item = (String)_history.get(i);
			_commandText.add(item, i);
			if (i == 0)
			    {
				_commandText.setText(item);
			    }
		    }
	    }
    }
    
  protected Control doCreateControl(Composite parent)
    {
	Composite container = new Composite(parent, SWT.NULL);
	
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing=0;
	container.setLayout(layout);
	
	GridData data1 = new GridData();
	data1.verticalAlignment = GridData.FILL;
	data1.horizontalAlignment = GridData.FILL;
	data1.grabExcessHorizontalSpace = true;
	data1.grabExcessVerticalSpace = true;
	container.setLayoutData(data1);
	
        GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
                                         GridData.GRAB_HORIZONTAL);
	textData.widthHint = SIZING_TEXT_FIELD_WIDTH;
	
        GridData buttonData = new GridData();
	buttonData.widthHint = SIZING_BUTTON_WIDTH;
	
        // directory
        Composite directoryContainer = new Composite(container, SWT.NULL);
        Label directoryLabel = new Label(directoryContainer, SWT.NULL);
        directoryLabel.setText(_plugin.getLocalizedString("CommandViewer.Working_Directory"));
        _directoryText = new Text(directoryContainer, SWT.SINGLE | SWT.BORDER);
	_directoryText.setLayoutData(textData);
	
        _browseButton = new Button(directoryContainer, SWT.PUSH);
	_browseButton.setLayoutData(buttonData);
        _browseButton.setText(_plugin.getLocalizedString("CommandViewer.Browse..."));
	_browseButton.addSelectionListener(this);
	
	GridLayout layout2 = new GridLayout();
	layout2.numColumns = 3;
	layout2.marginHeight = 5;
	layout2.marginWidth = 5;
	layout2.verticalSpacing=0;
	directoryContainer.setLayout(layout2);

	GridData data2 = new GridData();
	data2.horizontalAlignment = GridData.FILL;
	data2.grabExcessHorizontalSpace = true;
        directoryContainer.setLayoutData(data2);

        // command
        Composite commandContainer = new Composite(container, SWT.NULL);
        Label commandLabel = new Label(commandContainer, SWT.NULL);
        commandLabel.setText(_plugin.getLocalizedString("CommandViewer.Command"));

        _commandText = new Combo(commandContainer, SWT.SINGLE | SWT.BORDER);
	_commandText.setLayoutData(textData);
        _commandText.addModifyListener(
                                       new ModifyListener()
                                       {
                                         public void modifyText(ModifyEvent e)
                                             {
                                               if (_commandText.getText().length() > 0)
                                               {
                                                 _runButton.setEnabled(true);						
                                               }
                                               else
                                               {
                                                 _runButton.setEnabled(false);
                                               }
                                             }
                                       }
                                       );
	

       	_runButton = new Button(commandContainer, SWT.PUSH);
	_runButton.setLayoutData(buttonData);
        _runButton.setText(_plugin.getLocalizedString("CommandViewer.Run"));
	_runButton.addSelectionListener(this);
	
	GridLayout layout3 = new GridLayout();
	layout3.numColumns = 3;
	layout3.marginHeight = 5;
	layout3.marginWidth = 5;
	layout3.verticalSpacing=0;
	commandContainer.setLayout(layout3);

	GridData data3 = new GridData();
	data3.horizontalAlignment = GridData.FILL;
	data3.grabExcessHorizontalSpace = true;
	commandContainer.setLayoutData(data3);

        _directoryText.setEditable(false);

	
	return container;
      }

  protected final Control createControl(Composite parent)
      {
        Control c= doCreateControl(parent);
        return c;
      }

    public void widgetDefaultSelected(SelectionEvent e)
    {
	widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
	Widget source = e.widget;
	
	if (source == _browseButton)
	    {
		IWorkspace workbench = _plugin.getPluginWorkspace();	
		
		DataElementFileDialog dialog = new DataElementFileDialog("Select Directory", _input);
		dialog.open();
		if (dialog.getReturnCode() == dialog.OK)
		    {
			DataElement selected = dialog.getSelected();
			if (selected != null)
			    {
				setInput(selected);
			    }
		    }		
	    }
	else if (source == _runButton)
	    {
		String command = _commandText.getText();
		setCommand(command);
		executeCommand();	
	    }	
    }
        
    public String getWorkingDirectory()
    {
        return _directoryText.getText();
    }
    
    public void setCommand(String command)
    {
        _command = command;
      }
    
    public void executeCommand()
    {
	if (_input != null)
	    {	
		if (_history == null)
		    {
			_history = new ArrayList();
		    }

		int index= _history.indexOf(_command);
		if (index != 0)
		    {
			if (index != -1)
			    {
				_history.remove(index);
			    }
		    }

		_history.add(0, _command);
		writeHistory();
		updateCombo();
		
		Display d = getShell().getDisplay();
    
		DataStore dataStore = _input.getDataStore();
		DataElement cmdD = dataStore.localDescriptorQuery(_input.getDescriptor(), "C_COMMAND");
		if (cmdD != null)

		    {				
			ArrayList args = new ArrayList();
			
			DataElement invocationObj = dataStore.createObject(null, "invocation", _command, "");
			args.add(invocationObj);
		 
     		DataElement cmdStatus = dataStore.command(cmdD, args, _input);
		ShowViewAction action = new ShowViewAction(_outputViewId, cmdStatus);
	        d.asyncExec(action);		
		    }

	    }
    }

    public Shell getShell()
    {
	return _browseButton.getShell();
    }
    
    
    public void enable(boolean on)
    {
    }

    public ArrayList readHistory()
    {
	return null;
    }
    
    public void writeHistory()
    {
    }
}
