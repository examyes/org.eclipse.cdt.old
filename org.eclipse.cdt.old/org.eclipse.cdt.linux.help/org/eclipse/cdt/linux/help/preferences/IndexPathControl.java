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

public class IndexPathControl extends Composite implements Listener
{
    private Group group;
    
    private Button _addButton;
    private Button _removeButton;
    private Button _browseButton;
    
    private Text   _pathEntry;
    private List   _pathList;    
    
    private Button _indexButton;

    private String originalPaths;
    
    public IndexPathControl(Composite cnr, int style)
    {
	super(cnr, style);
	
	group = new Group(this, SWT.NULL);
	group.setText("Html directories to index");
	group.setLayout(new GridLayout());
	group.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
	
	// entry cmp
	Composite entryCmp = new Composite(group, SWT.NULL);
	_pathEntry      = new Text(entryCmp, SWT.BORDER);
	_pathEntry.addListener(SWT.Modify, this);
	_pathEntry.addListener(SWT.FocusIn, this);
	GridData dp0 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
	_pathEntry.setLayoutData(dp0);
	
	_browseButton      = new Button(entryCmp, SWT.PUSH);
	_browseButton.setText("Browse");
	_browseButton.addListener(SWT.Selection, this);
	GridData dp1 = new GridData(GridData.HORIZONTAL_ALIGN_END);
	dp1.widthHint = 80;
	_browseButton.setLayoutData(dp1);
	
	// p1
	Composite p1    = new Composite(group, SWT.NULL);
	GridLayout p1layout = new GridLayout();
	p1layout.numColumns = 2;
	p1.setLayout(p1layout);
	
	_pathList       = new List(p1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	_pathList.addListener(SWT.FocusIn, this);
	_pathList.addListener(SWT.Selection, this);
	GridData dp2 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
	dp2.heightHint = 100;
	_pathList.setLayoutData(dp2);
	
	// p2
	Composite p2    = new Composite(p1, SWT.NULL);
	
	_addButton      = new Button(p2, SWT.PUSH);
	_addButton.setText("Add");
	_addButton.addListener(SWT.Selection, this);
	GridData dp3 = new GridData(GridData.HORIZONTAL_ALIGN_END);
	dp3.widthHint = 80;
	_addButton.setLayoutData(dp3);
	
	_removeButton   = new Button(p2, SWT.PUSH);	
	_removeButton.setText("Remove");
	_removeButton.addListener(SWT.Selection, this);
	GridData dp4 = new GridData(GridData.HORIZONTAL_ALIGN_END);
	dp4.widthHint = 80;
	_removeButton.setLayoutData(dp4);
	
	_indexButton = new Button(p2,SWT.PUSH);
	_indexButton.setText("Create Index");
	_indexButton.addListener(SWT.Selection,this);
	GridData dp5 = new GridData(GridData.HORIZONTAL_ALIGN_END);
	dp5.widthHint = 80;
	_indexButton.setLayoutData(dp5);
	
	// layouts
	GridLayout l1 = new GridLayout();
	l1.numColumns = 3;
	entryCmp.setLayout(l1);
	entryCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	GridLayout l2 = new GridLayout();
	l2.numColumns = 2;
	p1.setLayout(l2);
	p1.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	GridLayout l3 = new GridLayout();
	l3.numColumns = 1;
	p2.setLayout(l3);
	p2.setLayoutData(new GridData(GridData.FILL_VERTICAL));
	
	if (_pathList.getItemCount() == 0)
	    {
		_addButton.setEnabled(false);
		_removeButton.setEnabled(false);       
		_indexButton.setEnabled(false);
	    }   
	
	//load the list
	loadSettingsToWidget();    
    }   
    
    public void handleEvent(Event e)
    {
	Widget source = e.widget;
	
	if (source == _browseButton)
	    {
		String selectedDirectory = null;
		
		DirectoryDialog dialog = new DirectoryDialog(this.getShell(), SWT.SAVE);
		dialog.setMessage("Select the source directory of files to index.");
		dialog.setFilterPath("*.*");
		
		selectedDirectory = dialog.open();
		

		if (selectedDirectory != null)
		    {
			_pathEntry.setText(selectedDirectory);	
			_addButton.setEnabled(true);
		    }
	    }
	else if (source == _pathEntry)
	    {
		_removeButton.setEnabled(false);
		
		if (_pathEntry.getText().length()==0)
		    {
			_addButton.setEnabled(false);
		    }
		else
		    {
			_addButton.setEnabled(true);
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
		ArrayList pathList = getPaths();     
		for(int i=0;i<pathList.size();i++)
		    {
			String thePath= (String)pathList.get(i);
			if(text.equals(thePath))
			    duplicate=true;
		    }
		if(!duplicate)
		    {
			addPath(text);
		    }
		
		_indexButton.setEnabled(true);				
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
		if(_pathList.getItemCount()==0)
		    {
			_indexButton.setEnabled(false);
		    }		
	    }
	else if(source == _indexButton)
	    {
		storeSettings();
		
		// Attempt to create an index
		doCheckIndexCreation();
	    }
    }
    
    public void storeSettings()
    {
	//write the paths to a dialog setting
	savePaths(getPaths());		
	
	if (originalPaths==null)
	    {  
		if(getStringToSave(getPaths()).length() != 0)
		    {
			//indicate paths were modified
			setPathsModifiedFlag(true);
		    }
	    }
	else
	    {
		if (!originalPaths.equals(getStringToSave(getPaths())))
		    {
			//indicate paths were modified
			setPathsModifiedFlag(true);
		    }
	    }
    }

    public void setEnabled(boolean enabled)
    {
	group.setEnabled(enabled);
	super.setEnabled(enabled);
    }

    public  void doCheckIndexCreation()
    {
	if(!checkIndexCreation())
	    {
		boolean confirmation = MessageDialog.openConfirm(HelpPlugin.getDefault().getView().getSite().getShell(), "Confirmation","The 'Directories to Index' have not changed\nDo you still want to recreate the index?");
		if(confirmation)
		    {
			if(createIndex()) //attempt to create the index
			    {				
				setPathsModifiedFlag(false);//indicate indexing was successful.	
			    }	
		    }
	    }
    }

    public boolean checkIndexCreation()
    {
	boolean needIndexing;
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();
	needIndexing = settings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED);
	if(needIndexing) // do we need to update the index?
	    {
		// ask user confirmation to create index
		boolean createIndex = MessageDialog.openQuestion(HelpPlugin.getDefault().getView().getSite().getShell(),"question","The 'Directories to Index' have changed\nDo you want to recreate the index?");
		if(createIndex)
		    {			
			if(createIndex()) //attempt to create the index
			    {				
				setPathsModifiedFlag(false);//indicate indexing was successful.
			    }			
		    }
		
	    }
	return needIndexing; 
    }    

    public boolean createIndex()
    {	
	boolean success=false;
	String indexPathName;
	File statePath = HelpPlugin.getDefault().getStateLocation().toFile();
	File indexPath = new File(statePath,IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION);
	if(!indexPath.exists())
	    {
		if(!indexPath.mkdir())
		    return false;
	    }	
	
	try{
	    indexPathName = indexPath.getCanonicalPath();
	}catch(Exception e){e.printStackTrace();return false;}

	ArrayList pathList = readPathsToIndex();
	//FIXME:add check that pathList is valid list of directories/files 
	if(pathList==null || pathList.size()==0)return false;
	//SearchHtml indexBox = new SearchHtml();
	//success = indexBox.createIndex(indexPathName,pathList);
	
	IRunnableWithProgress searchWithProgress = new SearchHtmlWithProgress(indexPathName,pathList);
	ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(HelpPlugin.getDefault().getView().getSite().getShell());
	progressDialog.setCancelable(false);	
	try 
	    {		
		progressDialog.run(true,false,searchWithProgress);
	    }
	catch(Exception e)
	    {
		e.printStackTrace();
	    }
	success=true;////

	if(success)
	    {   
		//indicate an index has been created
		IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();	
		settings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,"true");
	    }
	return success;
    }

    
    private ArrayList readPathsToIndex()
    {
	ArrayList pathList = new ArrayList();
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();	
	String paths = settings.get(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX);	

	//remember the initial paths
	originalPaths = paths;

	if(paths==null) return null;
	StringTokenizer tokenizer = new StringTokenizer(paths,"|");
	while(tokenizer.hasMoreTokens())
	    {
		pathList.add(tokenizer.nextToken());
	    }
	return pathList;
    }   

    private void setPathsModifiedFlag(boolean flag)
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();	
	settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,flag);
    }

    private void loadSettingsToWidget()
    {
	ArrayList pathList = readPathsToIndex();
	if (pathList!=null)
	    setPaths(pathList);
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

    private void savePaths(ArrayList list)
    {
	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();		
	settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX, getStringToSave(list));
    }   

    public void addPath(String path)
    {
        _pathList.add(path);
    }
    
    public void setPaths(ArrayList paths)
    {
        for (int i = 0; i < paths.size(); i++)
	    {
		addPath((String)paths.get(i));
	    }
      }
    
    public ArrayList getPaths()
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
