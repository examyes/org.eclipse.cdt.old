package org.eclipse.cdt.linux.help.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.swt.events.*;

import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

import java.util.*;
import java.lang.*;
import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.DialogSettings;

import org.eclipse.cdt.linux.help.display.*;
import org.eclipse.cdt.linux.help.preferences.*;

public class ResultsViewPart extends ViewPart
{
    private void showItem()    
    {
	/******
	Rectangle tablesize=_table.getClientArea();	
	TableColumn tc=_table.getColumn(0);
	System.out.println("showItem:width="+tablesize.width);
	tc.setWidth(tablesize.width-250);
	*****/

	int index=_table.getSelectionIndex();	

	IDialogSettings settings = HelpPlugin.getDefault().getDialogSettings();	

	//Pick a default Browser if none selected
	boolean kdeBrowserExists=HelpBrowserUtil.existsCommand("konqueror");
	boolean gnomeBrowserExists=HelpBrowserUtil.existsCommand("gnome-help-browser");		
	if (!(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR) ||
	     settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER)))
	    {		
		if(kdeBrowserExists)
		    {
			settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR,true);
		    }
		else if(gnomeBrowserExists)
		    {			
			settings.put(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER,true);
		    }				
	    }
	
	//ItemElement element = HelpSearch.getItemElement(index);
	ItemElement element = _plugin.getFilter().getItem(index);
	

	if(element.getType().equals(ItemElement.MAN_TYPE))
	    {		
		String name= element.getName();
		String section= HelpSearch.getManSection(name);
		StringBuffer theCommand = new StringBuffer(HelpSearch.getManInvocation(name));

		if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER) &&
		    HelpBrowserUtil.existsCommand("gnome-help-browser"))
		    {
			try
			    {				
				theCommand.append("("+section+")");
			       				
				runHelpBrowser gbrowser = new runHelpBrowser("man:"+theCommand);
				gbrowser.start();			    			    
			    }
			catch(Exception e)
			    {e.printStackTrace();}
		    }
		else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR) &&
			 HelpBrowserUtil.existsCommand("konqueror"))
		    {
			try
			    {	
				//konqueror doesn't like man sections with letters in it.(eg."3thr","3pm","n")
				int begin=0;
				int end=section.length();
				int current=begin;
				for(int i=0;i<end;i++)
				    {
					if("123456789".indexOf(section.charAt(i))!=-1)
					    current++;
					else
					    break;						
				    }
				if(current!=begin)
				    theCommand.append("("+section.substring(begin,current)+")"); 
				runKonq konq = new runKonq("man:"+theCommand);
				konq.start();			    			    
			    }
			catch(Exception e)
			    {e.printStackTrace();}
		    }
		else
		    {
			//Bring a dialog box telling the user he needs to have either KDE or GNOME installed
		    }
	    }
	else if (element.getType().equals(ItemElement.INFO_TYPE))
	    {
		// info page
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER) &&
		    HelpBrowserUtil.existsCommand("gnome-help-browser"))
		    {
			try
			    {				
				//String invocation=HelpSearch.getInvocation(index);
				String invocation = element.getInvocation();
				StringBuffer thecommand = new StringBuffer(invocation);
				int i=invocation.lastIndexOf('/');
				thecommand.replace(i,i+1,"#");
				
				runHelpBrowser gbrowser = new runHelpBrowser(thecommand.toString().replace(' ','_'));
				gbrowser.start();			    			   
			    }
			catch(Exception e)
			    {e.printStackTrace();}

		    }
		else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR) &&
			 HelpBrowserUtil.existsCommand("konqueror"))
		    {
			try
			    {			
				runKonq konq = new runKonq(element.getInvocation());
				konq.start();			    			   
			    }
			catch(Exception e)
			    {e.printStackTrace();}
		    }
		else
		    {
			//Bring a dialog box telling the user he needs to have either KDE or GNOME installed
		    }
		
	    }
	else if(element.getType().equals(ItemElement.HTML_TYPE))
	    {
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER) &&
		    HelpBrowserUtil.existsCommand("gnome-help-browser"))
		    {
			try
			    {				
				runHelpBrowser gbrowser = new runHelpBrowser("file:"+element.getName());
				gbrowser.start();			    			   
			    }
			catch(Exception e)
			    {e.printStackTrace();}

		    }
		else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_KONQUEROR) &&
			 HelpBrowserUtil.existsCommand("konqueror"))
		    {
			try
			    {				
				runKonq konq = new runKonq("file:"+element.getName());
				konq.start();			    			   
			    }
			catch(Exception e)
			    {e.printStackTrace();}
		    }
		else
		    {
			//Bring a dialog box telling the user he needs to have either KDE or GNOME installed
		    }

	    }
    }
    
    
    public class ResultsViewListener extends MouseAdapter
    {
	public void mouseDoubleClick(MouseEvent me)
	{
	    showItem();
	    /*
	    Rectangle tablesize=_table.getClientArea();
	    System.out.println("table width="+tablesize.width);
	    */	    
	}
	
    }
    
    //  private ResultsViewer _viewer;
    private HelpPlugin _plugin;
    private Table _table;
    private Color _tablecolor;

    private Text _expression;
    private Color _expressionColor;
    private Button _findButton;
    
    private Label _settings;

    //--------------    
    private Button addFilterAction;


    public ResultsViewPart()
    {
	super();
	_plugin = HelpPlugin.getDefault();
    }
    

   
    public void setExpression(String key)
    {
	_expression.setText(key);
    }
    public void setLabelSettings(String content)
    {
	_settings.setText(content);
    }

    public void createPartControl(Composite container)
    {
	ArrayList _itemList = null;

	GridLayout gridLayout = new GridLayout();
	gridLayout.numColumns=1;
	container.setLayout(gridLayout);
	
	
	Group helpInfo = new Group(container,SWT.NULL);	
	helpInfo.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_FINDDOCS_TITLE));
       	gridLayout = new GridLayout();
	gridLayout.numColumns = 4;//FIXME
	helpInfo.setLayout(gridLayout);
	GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
	helpInfo.setLayoutData(gridData);
       	
	new Label(helpInfo,SWT.NULL).setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_FINDDOCS_PROMPT));

	_expression = new Text(helpInfo,SWT.SINGLE|SWT.BORDER);
	_expressionColor=new Color(_expression.getDisplay(),255,255,255);
	_expression.setBackground(_expressionColor);
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.horizontalSpan=2;
	_expression.setLayoutData(gridData);
	
	_findButton = new Button(helpInfo,SWT.PUSH);	
	_findButton.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_FINDDOCS_FIND));
	_findButton.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e)
		{
		    String theKey = _expression.getText();
		    _plugin.showMatches(theKey); 		    
		}
	    });
      	

	_table = new Table(container,SWT.SINGLE|SWT.FULL_SELECTION);
	gridData = new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL);
	_table.setLayoutData(gridData);
		//	gridData.horizontalSpan=3;
	//_table = new Table(bottomHalf_container,SWT.SINGLE|SWT.FULL_SELECTION);
	

	TableColumn tc1= new TableColumn(_table,SWT.NULL);
	tc1.setWidth(20);	
	tc1.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN));

	TableColumn tc2= new TableColumn(_table,SWT.NULL);
	tc2.setWidth(100);	
	tc2.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_NAMECOLUMN));
	TableColumn tc3= new TableColumn(_table,SWT.NULL);
	tc3.setWidth(250);	
	tc3.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_DESCRIPTIONCOLUMN));

	//Point tablesize=_table.getSize();
	//System.out.println("size="+tablesize.x);
	//tc2.setWidth(tablesize.x-250);
	

	//change the default 'grey' background color of the table to 'white'
	_tablecolor=new Color(_table.getDisplay(),255,255,255);
	_table.setBackground(_tablecolor);

	_table.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e)
		{
		    //showItem();
		}
	    });

	_table.addMouseListener(new ResultsViewListener());
	_table.setHeaderVisible(true);

	_settings=new Label(container,SWT.NULL);
	_settings.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

	HelpDialogSettingUtil.setDefaultSettings();
	HelpDialogSettingUtil.updateDisplayedSettings(); //show some default settings

	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	IWorkbenchPage persp= win.getActivePage();
	
	try{
	    persp.showView("org.eclipse.cdt.linux.help.views.ResultsViewPart"); //FIXME hardcoded view id
	}catch(PartInitException e){
	    e.printStackTrace();
	}

    }   

    public void populate(ArrayList list)
    {
	//clean the old results if any
	_table.deselectAll();	
	_table.removeAll();
	
	String manType = _plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN_MAN);
	String infoType = _plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN_INFO);
	String htmlType = _plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN_HTML);

	//Add the new results
	for(int i=0;i<list.size();i++)
	    {
		TableItem ti = new TableItem(_table,SWT.NULL);
		ItemElement theItem=(ItemElement)list.get(i);		
		String type = theItem.getType();
		
		if (type == ItemElement.MAN_TYPE)
		    {
			ti.setImage(0,_plugin.getImage("full/obj16/manual_obj.gif"));
       			//ti.setText(0,manType);
			ti.setText(1,theItem.getName());			
		    }
		else if (type == ItemElement.INFO_TYPE)
		    {	
			ti.setImage(0,_plugin.getImage("full/obj16/infopage_obj.gif"));
			//ti.setText(0,infoType);
			ti.setText(1,theItem.getKey());
		    }
		else if (type == ItemElement.HTML_TYPE)
		    {
			ti.setImage(0,_plugin.getImage("full/obj16/htmlpage_obj.gif"));
			//ti.setText(0,htmlType);
			String filename = theItem.getName();
			ti.setText(1,filename.substring(filename.lastIndexOf(File.separator)+1));
		    }

		ti.setText(2, theItem.getContent());
	    }
	
	//Default:show the first item
	if (list.size()>=1)
	    {
		_table.deselectAll();
		_table.select(0);
		showItem();		
	    }

    }

    public void setFocus()
    {
	//_viewer.getTable().setFocus();
	_expression.setFocus();
    }

    public void dispose()
    {
	super.dispose();
	if(_expressionColor!=null)
	    _expressionColor.dispose();
	if(_tablecolor!=null)
	    _tablecolor.dispose();
	if (_table!=null)
	    _table.dispose();
	if(_expression!=null)
	    _expression.dispose();
	if(_findButton!=null)
	    _findButton.dispose();
	if(_settings!=null)
	    _settings.dispose();
    }

} 


 
