package com.ibm.linux.help.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.linux.help.*;
import com.ibm.linux.help.search.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FilterControl extends Composite implements Listener
{
    private Group group;
    
    private Button _addButton;
    private Button _removeButton;
   
    private Text   _pathEntry;
    private List   _pathList;      
    
    public FilterControl(Composite cnr, int style)
    {
	super(cnr, style);
	
	group = new Group(this, SWT.NULL);
	group.setText("Patterns to filter out");
	group.setLayout(new GridLayout());
	group.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
	
	// entry cmp
	Composite entryCmp = new Composite(group, SWT.NULL);

	//
	GridLayout entryCmpLayout = new GridLayout();
	entryCmpLayout.numColumns=4;
	entryCmpLayout.makeColumnsEqualWidth=true;
	entryCmp.setLayout(entryCmpLayout);
	entryCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	_pathEntry      = new Text(entryCmp, SWT.BORDER);
	_pathEntry.addListener(SWT.Modify, this);
	_pathEntry.addListener(SWT.FocusIn, this);
	GridData dp0 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);	
	dp0.horizontalSpan=3;
	_pathEntry.setLayoutData(dp0);	
	
	_addButton      = new Button(entryCmp, SWT.PUSH);
	_addButton.setText("Add");
	_addButton.addListener(SWT.Selection, this);
	GridData dp3 = new GridData(GridData.FILL_HORIZONTAL);	
	_addButton.setLayoutData(dp3);

	// p1
	Composite p1    = new Composite(group, SWT.NULL);
	GridLayout p1layout = new GridLayout();
	p1layout.numColumns = 4;
	p1layout.makeColumnsEqualWidth=true;
	p1.setLayout(p1layout);
	p1.setLayoutData(new GridData(GridData.FILL_BOTH));

	_pathList       = new List(p1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	_pathList.addListener(SWT.FocusIn, this);
	_pathList.addListener(SWT.Selection, this);
	GridData dp2 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
	dp2.heightHint = 100;
	dp2.horizontalSpan=3;
	_pathList.setLayoutData(dp2);			
		
	_removeButton   = new Button(p1, SWT.PUSH);	
	_removeButton.setText("Remove");
	_removeButton.addListener(SWT.Selection, this);
	GridData dp4 = new GridData(GridData.FILL_HORIZONTAL);
	dp4.verticalAlignment=GridData.BEGINNING;
	_removeButton.setLayoutData(dp4);
		
	if (_pathList.getItemCount() == 0)
	    {
		_addButton.setEnabled(false);
		_removeButton.setEnabled(false);       		
	    }   
	
	//load the list
	loadSettingsToWidget();    
    }   
    
    public void handleEvent(Event e)
    {
	Widget source = e.widget;
		
	if (source == _pathEntry)
	    {
		_removeButton.setEnabled(false);
		
		if (_pathEntry.getText().length()==0)
		    {
			_addButton.setEnabled(false);
		    }
		else
		    {
			_addButton.setEnabled(true);
			// deselect all entries in the list
			for (int i = 0; i < _pathList.getItemCount(); i++)
			    {
				if (_pathList.isSelected(i))
				    {
					_pathList.deselect(i);
					break;
				    }
			    }
		    }
	    }
	else if (source == _pathList)
	    {
		_addButton.setEnabled(false);
		
		//enable remove if an element in the list is selected
		for (int i = 0; i < _pathList.getItemCount(); i++)
		    {
			if (_pathList.isSelected(i))
			    {
				_removeButton.setEnabled(true);
				
				break;
			    }
		    }
	    }
	else if (source == _addButton)
	    {
		String text = _pathEntry.getText();     
		_pathEntry.setFocus();
		
		//avoid adding duplicates
		boolean duplicate=false;
		ArrayList pathList = getFilters();     
		for(int i=0;i<pathList.size();i++)
		    {
			String thePath= (String)pathList.get(i);
			if(text.equals(thePath))
			    duplicate=true;
		    }
		if(!duplicate)
		    {
			addFilter(text);
		    }				
	    }
	else if (source == _removeButton)
	    {
		for (int i = 0; i < _pathList.getItemCount(); i++)
		    {
			if (_pathList.isSelected(i))
			    {
				_pathList.remove(i);
			    }
		    }			
	    }	
    }
    
    public void storeSettings()
    {
	//write the paths to a dialog setting
	saveFilters(getFilters());		
		
    }
    
    private ArrayList readFilters()
    {
	ArrayList pathList = new ArrayList();
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();	
	String paths = settings.get(IHelpSearchConstants.HELP_FILTER_PATTERNS);	

	//remember the initial paths
	//originalPaths = paths;

	if(paths==null) return null;
	StringTokenizer tokenizer = new StringTokenizer(paths,"|");
	while(tokenizer.hasMoreTokens())
	    {
		pathList.add(tokenizer.nextToken());
	    }
	return pathList;
    }   

    private void loadSettingsToWidget()
    {
	ArrayList pathList = readFilters();
	if (pathList!=null)
	    setFilters(pathList);
    }

    private String getStringToSave(ArrayList list)
    {
	StringBuffer listToSave = new StringBuffer();
	for(int i=0;i<list.size();i++)
	    {
		listToSave.append((String)list.get(i) + "|");		
	    }
	return listToSave.toString();
    }

    private void saveFilters(ArrayList list)
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();		
	settings.put(IHelpSearchConstants.HELP_FILTER_PATTERNS, getStringToSave(list));
    }   

    private void addFilter(String path)
    {
        _pathList.add(path);
    }
    
    private void setFilters(ArrayList paths)
    {
        for (int i = 0; i < paths.size(); i++)
	    {
		addFilter((String)paths.get(i));
	    }
      }
    
    private ArrayList getFilters()
    {
        ArrayList result = new ArrayList();
        for (int i = 0; i < _pathList.getItemCount(); i++)
	    {
		result.add(_pathList.getItem(i));
	    }
        return result;
    }

    public boolean isEmpty()
    {
	if(_pathList.getItemCount()==0)
	    return true;
	else
	    return false;
    }

}
