package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.*;

import org.eclipse.cdt.linux.help.preferences.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.DataStoreCorePlugin;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.vcm.Repository;
import org.eclipse.cdt.dstore.hosts.dialogs.DataElementFileDialog;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.ui.internal.*;
import org.eclipse.core.internal.plugins.*;

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

    private HelpPlugin plugin;
    private boolean _isRemote;

    DataElement _input;

    HelpSettings _settings = null;

    public IndexPathControl(Composite cnr, int style, boolean isRemote)
    {
	super(cnr, style);
	plugin = HelpPlugin.getDefault();
	_isRemote = isRemote;

	_settings = new HelpSettings(_isRemote);
	_settings.read();

	group = new Group(this, SWT.NULL);
	group.setText(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_TITLE));
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
	_browseButton.setText(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_BROWSE));
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
	_addButton.setText(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_ADD));
	_addButton.addListener(SWT.Selection, this);
	GridData dp3 = new GridData(GridData.HORIZONTAL_ALIGN_END);
	dp3.widthHint = 80;
	_addButton.setLayoutData(dp3);
	
	_removeButton   = new Button(p2, SWT.PUSH);	
	_removeButton.setText(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_REMOVE));
	_removeButton.addListener(SWT.Selection, this);
	GridData dp4 = new GridData(GridData.HORIZONTAL_ALIGN_END);
	dp4.widthHint = 80;
	_removeButton.setLayoutData(dp4);
	
	_indexButton = new Button(p2,SWT.PUSH);
	_indexButton.setText(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_CREATE));
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
		//FIXME : need to connect to server for a remote  project
		if(_isRemote)		
		    {
			IProject project = CppPlugin.getCurrentProject();
			if(project!=null && project instanceof Repository)
			    {
				DataElement dirInput = ((Repository)project).getRemoteElement();
				if(dirInput!=null)
				    {
					DataElementFileDialog dialog = new DataElementFileDialog(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_BROWSE_FILESYSTEMMESSAGE), dirInput);
					dialog.open();
					if (dialog.getReturnCode() == dialog.OK)
					    {
						DataElement selected = dialog.getSelected();
						if (selected != null)
						    {
							selectedDirectory = selected.getSource();
						    }
					    }
				    }

				else
				    {//the project is closed
					Shell shell = WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openInformation(shell,
								      plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INFORMATION),
								      plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INFORMATION_REMOTEBROWSEMESSAGE));
					
				    }
			    }
		    }
		else
		    {
			DirectoryDialog dialog = new DirectoryDialog(this.getShell(), SWT.SAVE);
			dialog.setMessage(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_BROWSEDIRMESSAGE));
			dialog.setFilterPath("*.*");
			
			selectedDirectory = dialog.open();
		    }

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
		Shell shell = WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		boolean confirmation =  MessageDialog.openConfirm(shell, plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_CONFIRMATION),plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_CONFIRMATIONMESSAGE));
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

	needIndexing = _settings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED);
	if(needIndexing) // do we need to update the index?
	    {
		Shell shell = WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		boolean createIndex = MessageDialog.openQuestion(shell, plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_QUESTION),plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_QUESTIONMESSAGE));
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

	//	if(HelpPlugin.getDefault().isRemote())
	    {
		DataStore dataStore;
		if(_isRemote)
		    dataStore= DataStoreCorePlugin.getDefault().getCurrentDataStore();
		else
		    dataStore= DataStoreCorePlugin.getDefault().getRootDataStore();

		DataElement indexObject = dataStore.createObject(null,"Project","linuxhelp_command");
	   
		DataStore ids = indexObject.getDataStore();

		String helpSettings = _settings.settingsToString();
		DataElement argSettings = dataStore.createObject(null,"help_settings", helpSettings);
		
		DataElement descriptor = dataStore.localDescriptorQuery(indexObject.getDescriptor(), 	
								       "C_HELPCREATEINDEX");
		
		DataElement status;
		if(descriptor!=null)
		    {
			ArrayList args = new ArrayList();
			args.add(argSettings);
			status = dataStore.command(descriptor,args,indexObject);

			HelpMonitor helpMonitor = new HelpMonitor(plugin.getLocalizedString(IHelpNLConstants.SETTINGS_INDEX_MONITORMESSAGE),status);
			Shell shell = WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
			progressDialog.setCancelable(false);	
			try 
			    {		
				progressDialog.run(true,false,helpMonitor);
			    }
			catch(Exception e)
			    {
				e.printStackTrace();
			    }
			

			while(!status.getName().equals("done"))
			    {
				try{Thread.sleep(200);}catch(Exception e){e.printStackTrace();}
      				Thread.yield();
			    }

			//indicate an index was created 
			_settings.read();
			_settings.put(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS,true);
			_settings.write();//commit 
		    }
		return true;
	    }
    }
    
    private ArrayList readPathsToIndex()
    {
	ArrayList pathList = new ArrayList();
		
	String paths = _settings.get(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX);	

	//remember the initial paths
	originalPaths = paths;

	if(paths==null) return null;
	StringTokenizer tokenizer = new StringTokenizer(paths,"##");
	while(tokenizer.hasMoreTokens())
	    {
		pathList.add(tokenizer.nextToken());
	    }
	return pathList;
    }   

    private void setPathsModifiedFlag(boolean flag)
    {
	_settings.read();
	_settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSMODIFIED,flag);
	_settings.write();
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
		listToSave.append((String)list.get(i) + "##");		
	    }
	return listToSave.toString();
    }

    private void savePaths(ArrayList list)
    {
	_settings.read();
	String paths=getStringToSave(list);
	if(paths.equals(""))
	    {
		_settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX, null);
	    }
	else
	    {
		_settings.put(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX, paths);
	    }
	_settings.write();
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
