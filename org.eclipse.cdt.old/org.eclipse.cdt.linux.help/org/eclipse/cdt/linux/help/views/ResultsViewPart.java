package org.eclipse.cdt.linux.help.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.*;
import org.eclipse.cdt.linux.help.convert.*;

import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.custom.StyledText;

import org.eclipse.swt.events.*;

import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.DialogSettings;

import org.eclipse.cdt.linux.help.display.*;
import org.eclipse.cdt.linux.help.preferences.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.help.ui.internal.WorkbenchHelpPlugin;
import org.eclipse.help.ui.browser.IBrowser;

import org.eclipse.cdt.dstore.core.*;

import org.eclipse.cdt.cpp.ui.internal.help.IHelpInput;
import org.eclipse.cdt.cpp.ui.internal.help.LaunchSearch;
import org.eclipse.help.AppServer;

public class ResultsViewPart extends ViewPart implements IDomainListener , IHelpInput  
{
    private void showItem()    
    {
	int index=_table.getSelectionIndex();	

	HelpSettings settings = new HelpSettings(_remoteContentsInView);
	settings.read();	

	ItemElement element = _plugin.getFilter().getItem(index);

	if(element.getType().equals(ItemElement.MAN_TYPE))
	    {		
		String name= element.getName();
		String section= getManSection(name);
		StringBuffer theCommand = new StringBuffer(getManInvocation(name));

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
		else if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT))
		    {
			try{
			    IBrowser browser = WorkbenchHelpPlugin.getDefault().getHelpBrowser();
			    String url = getLocalUrl(element);
			    _plugin.getLaunchSearch().registerHelpWebApp();
			    browser.displayURL(url);
			}
			catch(Exception e)
			    {e.printStackTrace();}
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
		else if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT))
		    {
			try{
			    IBrowser browser = WorkbenchHelpPlugin.getDefault().getHelpBrowser();
			    String url = getLocalUrl(element);
			    _plugin.getLaunchSearch().registerHelpWebApp();
			    browser.displayURL(url);
			}
			catch(Exception e)
			    {e.printStackTrace();}
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
		else if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT))
		    {
			try{
			    IBrowser browser = WorkbenchHelpPlugin.getDefault().getHelpBrowser();
			    String url = getLocalUrl(element);
			    _plugin.getLaunchSearch().registerHelpWebApp();
			    browser.displayURL(url);
			}
			catch(Exception e)
			    {e.printStackTrace();}
		    }
	    }
    }

    private void showRemoteItem()
    {      
	int index=_table.getSelectionIndex();	
	ItemElement element = _plugin.getFilter().getItem(index);
	
	HelpSettings settings = new HelpSettings(_remoteContentsInView);
	settings.read();
	int port = Integer.parseInt(settings.get(IHelpSearchConstants.HELP_TOMCAT_PORT));
	String url = getRemoteUrl(_hostname,port,element);	
	/**	
	if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_GNOMEHELPBROWSER) &&
	   HelpBrowserUtil.existsCommand("gnome-help-browser"))
	    {
		try
		    {				
			runHelpBrowser gbrowser = new runHelpBrowser(url);
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
			runKonq konq = new runKonq(url);
			konq.start();			    			   
		    }
		catch(Exception e)
		    {e.printStackTrace();}
	    }
	    else if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT))
	**/
	    {
		try{
		    IBrowser browser = WorkbenchHelpPlugin.getDefault().getHelpBrowser();
		    browser.displayURL(url);
		}
		catch(Exception e)
		    {e.printStackTrace();}
	    }
    }

    private String getRemoteUrl(String hostname,int port, ItemElement element)
    {
	StringBuffer url = new StringBuffer("http://");
	url.append(hostname);
	url.append(":"+port);
	url.append("/cdthelp/Help");

	String type = element.getType();
	url.append("?type="+element.getType());
	
	if(type.equals(ItemElement.MAN_TYPE))
	    {
		url.append("&section="+element.getSection());
		url.append("&invocation="+element.getInvocation());
	    }
	else if(type.equals(ItemElement.INFO_TYPE))
	    {//FIXME: may need to convert other non-alphanumeric characters to %XX (i.e XX is ascii value in hex)
		url.append("&invocation="+element.getInvocation().replace(' ','+')); 
	    }
	else if(type.equals(ItemElement.HTML_TYPE))
	    {
		url.append("&name="+element.getName().trim());
	    }
	return url.toString();
    }

    private String getLocalUrl(ItemElement element)
    {
	StringBuffer url=new StringBuffer("http://");
	url.append(AppServer.getHost());
	url.append(":"+AppServer.getPort());
	url.append("/cdthelp/Help");
	url.append("?type="+element.getType());

	String type = element.getType();
	if(type.equals(ItemElement.MAN_TYPE))
	    {
		url.append("&section="+element.getSection());
		url.append("&invocation="+element.getInvocation());
	    }
	else if(type.equals(ItemElement.INFO_TYPE))
	    {//FIXME: may need to convert other non-alphanumeric characters to %XX (i.e XX is ascii value in hex)
		url.append("&invocation="+element.getInvocation().replace(' ','+')); 
	    }
	else if(type.equals(ItemElement.HTML_TYPE))
	    {
		url.append("&name="+element.getName().trim());
	    }
	return url.toString();
	
    }
    
    public class ResultsViewListener extends MouseAdapter
    {
	public void mouseDoubleClick(MouseEvent me)
	{
	    HelpSettings settings = new HelpSettings(_remoteContentsInView);
	    settings.read();
	    
	    if(_remoteContentsInView)
		{
		    showRemoteItem();
		}
	    else
		{
		    showItem();
		}
	}
    }
    
    private HelpPlugin _plugin;
    private Table _table;
    private Color _tablecolor;

//    private Text _expression; 
    private StyledText _expression;//needed to get <Enter> key event
    private Color _expressionColor;
    private Button _findButton;
    
    private Label _settings;

    private Button addFilterAction;

    private DataElementMapper _mapper;
    private DataElement _input;
    private boolean _remoteContentsInView=false;
    private String _hostname="localhost";

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

//	_expression = new Text(helpInfo,SWT.SINGLE|SWT.BORDER);
	_expression = new StyledText(helpInfo,SWT.SINGLE|SWT.BORDER);
	_expressionColor=new Color(_expression.getDisplay(),255,255,255);
	_expression.setBackground(_expressionColor);
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.horizontalSpan=2;
	_expression.setLayoutData(gridData);
	_expression.addKeyListener(new KeyAdapter(){
		public void keyReleased(KeyEvent e)
		    {
			if(e.character=='\r')
			{
			    String theKey = _expression.getText();
			    LaunchSearch.getDefault().doSearch(theKey,null);
			}
		    }
	    });


	_findButton = new Button(helpInfo,SWT.PUSH);	
	_findButton.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_FINDDOCS_FIND));
	_findButton.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e)
		{
		    String theKey = _expression.getText();			
		    LaunchSearch.getDefault().doSearch(theKey,null);
		}
	    });

	_table = new Table(container,SWT.SINGLE|SWT.FULL_SELECTION);
	gridData = new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL);
	_table.setLayoutData(gridData);

	TableColumn tc1= new TableColumn(_table,SWT.NULL);
	tc1.setWidth(20);	
	tc1.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN));

	TableColumn tc2= new TableColumn(_table,SWT.NULL);
	tc2.setWidth(100);	
	tc2.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_NAMECOLUMN));
	TableColumn tc3= new TableColumn(_table,SWT.NULL);
	tc3.setWidth(250);	
	tc3.setText(_plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_DESCRIPTIONCOLUMN));

	//change the default 'grey' background color of the table to 'white'
	_tablecolor=new Color(_table.getDisplay(),255,255,255);
	_table.setBackground(_tablecolor);

	/*
	_table.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e)
		{
		    //showItem();
		}
	    });
	*/
	_table.addMouseListener(new ResultsViewListener());
	_table.setHeaderVisible(true);

	/*
	_settings=new Label(container,SWT.NULL);
	_settings.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	_settings.setText(getSettingsToDisplay());       
	*/
    }   

    public void populate(ArrayList list)
    {
	//clean the old results if any
	_table.deselectAll();	
	_table.removeAll();
	
	String manType = _plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN_MAN);
	String infoType = _plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN_INFO);
	String htmlType = _plugin.getLocalizedString(IHelpNLConstants.VIEW_TABLE_TYPECOLUMN_HTML);
	
	boolean isOsWindows = System.getProperty("os.name").toLowerCase().startsWith("window");

	//Add the new results
	for(int i=0;i<list.size();i++)
	    {
		TableItem ti = new TableItem(_table,SWT.NULL);
		ItemElement theItem=(ItemElement)list.get(i);		
		String type = theItem.getType();
		
		if (type.equals(ItemElement.MAN_TYPE))
		    {
			ti.setImage(0,_plugin.getImage("full/obj16/manual_obj.gif"));
			ti.setText(1,theItem.getName());			
		    }
		else if (type.equals(ItemElement.INFO_TYPE))
		    {	
			ti.setImage(0,_plugin.getImage("full/obj16/infopage_obj.gif"));
			ti.setText(1,theItem.getKey());
		    }
		else if (type.equals(ItemElement.HTML_TYPE))
		    {
			ti.setImage(0,_plugin.getImage("full/obj16/htmlpage_obj.gif"));
			String filename = theItem.getName();
			if(isOsWindows && filename.startsWith("/"))
			    {
				//windows client, linux server
				ti.setText(1,filename.substring(filename.lastIndexOf("/")+1));
			    }
			else
			    {
				ti.setText(1,filename.substring(filename.lastIndexOf(File.separator)+1));
			    }
		    }

		ti.setText(2, theItem.getContent());
	    }
	
	//Default:show the first item
	/*	if (list.size()>=1)
	    {
		_table.deselectAll();
		_table.select(0);
		
		if(_remoteContentsInView)
		    {
			showRemoteItem();
		    }
		else
		    {
			showItem();
		    }
	    }
	*/
    }

    public void setFocus()
    {
	_expression.setFocus();
    }

    public void dispose()
    {
	super.dispose();

	if(_input!=null)
	    {
		DomainNotifier notifier= _input.getDataStore().getDomainNotifier();
		notifier.removeDomainListener(this);
	    }

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



    ////////////methods for IDomainListener////////    
    public void domainChanged(DomainEvent ev)
    {
	DataElement parent =(DataElement)ev.getParent();
	
	if(_input==null)
	    {
		return;
	    }

	if(!_input.getName().equals("done"))
	    {
		//FIXME:display some progress dialog?
	    }
	else
	    {
		//FIXME: new Thread?
		ArrayList dataElementList = _input.getAssociated("contents");
	
		DataElement DminerKey = (DataElement)dataElementList.get(dataElementList.size()-1);
		String keyword;
		if(DminerKey.getType().equals("key"))
		    keyword = DminerKey.getValue();
		else
		    keyword = "";//should never get here
		dataElementList.remove(dataElementList.size()-1);
		setExpression(keyword);

		DataElement DminerHostname = (DataElement)dataElementList.get(dataElementList.size()-1);
      		String minerHostname;
		if(DminerHostname.getType().equals("hostname"))
		    minerHostname= DminerHostname.getName();
		else
		    minerHostname="localhost";// should never get here
		dataElementList.remove(dataElementList.size()-1);

		_hostname = minerHostname;

		ArrayList itemElementList = DataElementMapper.convertToItemElement(dataElementList);

		//FIXME: wait until get complete list of results
   
		HelpPlugin.getDefault().setList(itemElementList);

		//filter unwanted elements
		HelpPlugin.getDefault().getFilter().updateIndexList(itemElementList);
		ArrayList filteredResults = HelpPlugin.getDefault().getFilter().getFilteredResults();
		
		String hostname;
		try
		    {
			hostname= InetAddress.getLocalHost().getHostName();
			boolean isRemote = !hostname.equals(minerHostname);
			setRemoteContents(isRemote);
		    }
		catch(UnknownHostException e)
		    {
			e.printStackTrace();
			setRemoteContents(false); // mark it as LOCAL project
		    }

		//display in view
		populate(filteredResults);
	    }
    }

    public boolean listeningTo(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();
	if(_input == parent)
	    {
		return true;
	    }
	return false;
    }

    public Shell getShell()
    {
	return getViewSite().getShell();
    }

    public void setInput(DataElement status)
    {
	DomainNotifier notifier = status.getDataStore().getDomainNotifier();
	notifier.addDomainListener(this);
	_input = status;
    }

    public void setRemoteContents(boolean remoteContents)
    {
	_remoteContentsInView = remoteContents;
    }
    
    private String getSettingsToDisplay()
    {
	StringBuffer displayedSettings= new StringBuffer();
	HelpSettings settings = new HelpSettings(_remoteContentsInView);
	settings.read();

	displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_TITLE)+":");

	if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT))
	    {
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_EXACT));
	    }
	else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS))
	    {
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_SUBSTRING));
	    }
	else if (settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP))
	    {		
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_LOOKUPMODE_REGEXP));
	    }
	displayedSettings.append("  "+HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_TITLE)+":");
	if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL))
	    {
		displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_ALL));
	    }
	else
	    {
		boolean scopeSelected = false;
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN))
		    {		 
			displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_MAN));
			scopeSelected = true;
		    }
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO))
		    {
			if(scopeSelected) displayedSettings.append(","); 
			displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_INFO));
			scopeSelected = true;
		    }
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML))
		    {
			if(scopeSelected) displayedSettings.append(","); 
			displayedSettings.append(HelpPlugin.getDefault().getLocalizedString(IHelpNLConstants.SETTINGS_SCOPE_HTML));
		    }
	    }
	return displayedSettings.toString();
    }

    private String getManSection(String name)
    {
	int begin,end;
	begin=name.lastIndexOf('(');
	end=name.lastIndexOf(')');
	if (begin==-1 || end==-1)
	    {
		return null; // no section
	    }
	else	    
	    {		
		return name.substring(begin+1,end);
	    }
    }
    
    private String getManInvocation(String name)
    {	
	int keybegin,keyend;
	keybegin=name.indexOf('[');
	keyend= name.indexOf(']');	
	if (keybegin==-1 || keyend==-1)
	    {
		keyend=name.indexOf(" ");
		if (keyend == -1)
		    return name;
		else
		    return name.substring(0,keyend);		
	    }
	else
	    return name.substring(keybegin+1,keyend);	    
	    
    }

} 


 
