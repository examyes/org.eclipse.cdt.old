package org.eclipse.cdt.cpp.miners.help.search;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.miners.help.preferences.*;

import org.eclipse.cdt.dstore.core.util.regex.text.regex.*;

import java.lang.String;
import java.util.ArrayList;
import java.io.*;
import java.lang.*;

public class HelpSearch
{
    private static ArrayList _manList;
    private static int _mansize=0;
    private static boolean _mandone=false;
   
    private static ArrayList _infoList;
    private static int _infosize=0;
    private static boolean _infodone=false;

    private static ArrayList _result;

    private static final int INFO_TYPE=0;
    private static final int MAN_TYPE=1;   

    static public int getManSize()
    {
	return _manList.size();
    }

    public static HelpSearch _helpSearch = null;
    public static HelpSearch getDefault()
    {
	if(_helpSearch==null)
	    {
		_helpSearch = new HelpSearch();
	    }
	return _helpSearch;
    }

    private HelpSearch()
    {	
	long begin=System.currentTimeMillis();

	_manList=new ArrayList();
	_infoList=new ArrayList();
	if(!isWindows())
	    {
		HelpSearch.loadMan();
		HelpSearch.loadInfo();
	    }
	long end=System.currentTimeMillis();	
    }
    
    public static void loadMan()
    {
	Thread workerThread = new HelpSearchLoadThread(_manList,MAN_TYPE);
	workerThread.start();
    }

    public static void loadInfo()
    {
	Thread workerThread = new HelpSearchLoadThread(_infoList,INFO_TYPE);
	workerThread.start();
    }
    public static void reloadMan()
    {
	_mandone=false;
	Thread workerThread = new HelpSearchLoadThread(_manList,MAN_TYPE);
	workerThread.start();
    }

    public static void reloadInfo()
    {
	_infodone=false;
	Thread workerThread = new HelpSearchLoadThread(_infoList,INFO_TYPE);
	workerThread.start();
    }

       
    public static  void load(ArrayList list, int type)
    {
	list.clear(); //empty the list	

	if (type==MAN_TYPE)
	    {
		_mandone=false; // indicate load about to start
		_mansize=0;
		File filepath = getWhatIsPath();

		// DKM - need to check if this exists before trying to read
		if (filepath != null && filepath.exists())
		    {
			try
			    {
				BufferedReader input = new BufferedReader(new FileReader(filepath));
				
				String line;
				while((line=input.readLine())!=null )
				    {
					int limit= line.indexOf('-',line.indexOf(')'));	

					if(limit ==-1)
					    continue; //skip lines with no description
					String name = line.substring(0,limit); 
					String content = line.substring(limit+1,line.length());
					String key = getManKey(name);
					String invocation= getManInvocation(name);
					String section = getManSection(name);
					
					if(key ==null || key.trim().length()==0)
					    continue;//skip entry
					
					ItemElement i= new ItemElement(key,name,content,invocation,section,
								       ItemElement.MAN_TYPE);
					
					list.add(i);
					_mansize++;
				    }				
			    }
			catch(Exception e)
			    {
				
				//FIXME: filenotfoundexception
				e.printStackTrace();
			    }
			_mandone=true;	// indicate done
		    }			
	    }
	else if (type==INFO_TYPE)
	    {
		_infodone=false; //indicate about to start
		_infosize=0;
		File path = getInfoPath();
		if (path != null && path.exists())
		    {
			try
			    {
				BufferedReader input = new BufferedReader(new FileReader(path));
				
				String line;
				while(true)
				    {				
					input.mark(200);
					line = input.readLine();
					if(line == null) break;
					if(line.startsWith("*"))
					    {
						input.reset();
						try{
						    processEntry(input,list);
						}catch(Exception e){
						}
					    }
				    }
			    }
			catch(Exception e)
			    {
				//FIXME: filenotfoundexception
				e.printStackTrace();
			    }
			_infodone=true; //indicate done.
		    }			
	    }
    }

    private static void processEntry(BufferedReader in, ArrayList list) throws IOException
    {
	// Format is :
	// * keyname: (filename)nodename. content
	//              content
	//              content

	String line;
	line = in.readLine();
	
	int keyEnd = line.indexOf(':');
	int fileBegin= line.indexOf('(');
	int fileEnd= line.indexOf(')');
	int nodeEnd = line.indexOf('.');

	if(keyEnd ==-1||fileBegin==-1 ||fileEnd==-1||fileBegin>fileEnd||nodeEnd==-1||nodeEnd<fileEnd)
	    return;

	//KEY
	String key = line.substring(1,keyEnd).trim();

	//INVOCATION
	StringBuffer invocation = new StringBuffer();
	invocation.append("info:/");	
	String filename= line.substring(fileBegin+1,fileEnd);
	if(filename.startsWith("/"))
	    return;//Absolute paths to info pages are not handled by gnome-help-browser.
	invocation.append(filename).append("/");
	String nodename= line.substring(fileEnd+1,nodeEnd).trim();
	if(nodename.equals(""))
	    invocation.append("Top");
	else
	    invocation.append(nodename);
	
	//CONTENT
	StringBuffer content = new StringBuffer();
	if(nodeEnd!=line.length())
		content.append(line.substring(nodeEnd+1).trim());
	//keep processing content
	while(true)
	    {
		in.mark(200);		
		line = in.readLine();
		if(line.startsWith("*")|| line.trim().equals(""))
		    {
			in.reset();
			break;
		    }
		else
		    {
			// keep adding to 'content'
			content.append(" "+line.trim());
		    }
	    }
	
	ItemElement i= new ItemElement(key,null,content.toString().trim(),invocation.toString(),null,
				       ItemElement.INFO_TYPE);	
	list.add(i);
	_infosize++;	
    }

    static private File getInfoPath()
    {
	HelpSettings settings = HelpSettings.getDefault();
	String filename=settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_DIR);
		
	if(filename == null)
	    {
		filename = HelpSearchUtil.getDefaultDirPath();
		if(filename!=null)
		    settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_DIR,filename);
	    }

	if(filename!=null)
	    {
		//FIXME
		File path= new File(filename);
		if (path.exists())
		    return path;
		else
		    return null;
	    }
	else
	    {
		return null;
	    }
    }

    static private String getManKey(String name)
    {
	int end=name.indexOf(' ');
	if (end!= -1)
	    return name.substring(0,end);
	else 
	    return null;
    }

    static public String getManInvocation(String name)
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

    static public String getInvocation(int index)
    {
	return ((ItemElement)_result.get(index)).getInvocation();
    }

    static public String getManSection(String name)
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


    static private File getWhatIsPath()
    {
	HelpSettings settings = HelpSettings.getDefault();
	String filename=settings.get(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS);

	if(filename==null)
	    {
		filename = HelpSearchUtil.getDefaultWhatisPath();
		if(filename!=null)
		    settings.put(IHelpSearchConstants.HELP_SEARCH_PATH_WHATIS,filename);
	    }

	if(filename!=null)
	    {
		File path= new File(filename);
		if (path.exists())
		    return path;
		else
		    return null;		
	    }
	else
	    {		
		return null;
	    }	
    }
    
    static public boolean finishLoading()
    {      	
	return (_mandone && _infodone);
    }

    // check whether 'key' is a valid one.(i.e. neither 'null' nor "")
    private boolean checkValidKey(String key)
    {
	if(key==null)
	    return false;
	else if(key.trim().length()==0)
	    return false;
	else
	    return true;
    }

    // Execute a search and return the results in an ArrayList. 
    // Returns:
    //   'null' is 'key' is invalid.
    //    empty ArrayList if 'key' is not found.
    public ArrayList FindListOfMatches(String key, String optSearchType)
    {	
	HelpSettings settings = HelpSettings.getDefault();

	if (!checkValidKey(key))
	    {
		// maybe display a dialog box here.
		return null;
	    }
	
	long begin=System.currentTimeMillis();
	ArrayList result=new ArrayList();		

	if((settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT)&& optSearchType==null) ||
		(optSearchType!=null && optSearchType.equals(IHelpSearchConstants.HELP_SEARCH_TYPE_EXACT)) )
	    {
		// HTML available only with EXACT search type
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL))
		    {
			result.addAll(FindManExact(key));
			result.addAll(FindInfoExact(key));
			result.addAll(findHtml(key)); //HTML only with EXACT type
		    }
		else
		    {
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN))		   
			    {
				result.addAll(FindManExact(key));
			    }
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO))
			    {
				result.addAll(FindInfoExact(key));
			    }
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_HTML))
			    {
				result.addAll(findHtml(key));// HTML only with EXACT type
			    }
		    }
	    }
	else if((settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS)&& optSearchType==null) ||
		(optSearchType!=null && optSearchType.equals(IHelpSearchConstants.HELP_SEARCH_TYPE_CONTAINS)) )
	    {
		//No HTML with SUBSTRING search type
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL))
		    {
			result.addAll(FindManContains(key));
			result.addAll(FindInfoContains(key));
		    }
		else
		    {
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN))		   
			    {
				result.addAll(FindManContains(key));
			    }
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO))
			    {
				result.addAll(FindInfoContains(key));
			    }
		    }				
	    }
	else if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_TYPE_REGEXP))
	    {
		//No HTML with REGULAR EXPRESSION search type
		if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_ALL))
		    {
			result.addAll(FindManRegExp(key));
			result.addAll(FindInfoRegExp(key));
		    }
		else
		    {
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_MAN))		   
			    {
				result.addAll(FindManRegExp(key));
			    }
			if(settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_SCOPE_INFO))
			    {
				result.addAll(FindInfoRegExp(key));
			    }
		    }	
	    }	
			
	_result=result;

	long end=System.currentTimeMillis();	

	return result;
    }

    //------------------- man --------------------
    private ArrayList FindManContains(String key)
    {
	ArrayList result=new ArrayList();	
	for(int i=0;i<_mansize;i++)
	    {
		ItemElement e=(ItemElement)_manList.get(i);
		if(e.getName().toLowerCase().indexOf(key.toLowerCase())!= -1)
		    {			
			result.add(e);
		    }
	    }
	return result;
    }

    private ArrayList FindManExact(String key)
    {
	ArrayList result=new ArrayList();
	for(int i=0;i<_mansize;i++)
	    {
		ItemElement e=(ItemElement)_manList.get(i);
		if(e.getKey().compareToIgnoreCase(key)== 0)
		    {			
			result.add(e);
		    }
	    }
	return result;
    }

    private ArrayList FindManRegExp(String keyPattern)
    {
	ArrayList result = new ArrayList();

	 PatternMatcher matcher   = new Perl5Matcher();
	 PatternCompiler compiler = new Perl5Compiler();
	 
	 Pattern pattern = null;
	 try 
	     {
		 pattern = compiler.compile(keyPattern);
	     } 
	 catch(MalformedPatternException e) 
	     {
		 System.err.println(e.getMessage());
		 e.printStackTrace();
	     }
	 
	for(int i=0;i<_mansize;i++)
	    {
		ItemElement e=(ItemElement)_manList.get(i);
		if(matcher.matches(e.getName(),pattern))
		    {
			result.add(e);
		    }
	    }
	return result;
    }

    //------------------- info ------------------
    private ArrayList FindInfoContains(String key)
    {
	ArrayList result=new ArrayList();
	for(int i=0;i<_infosize;i++)
	    {
		ItemElement e=(ItemElement)_infoList.get(i);
		if(e.getKey().toLowerCase().indexOf(key.toLowerCase()) != -1)
		    {
			result.add(e);
		    }
	    }
	return result;
    }
    private ArrayList FindInfoExact(String key)
    {
	ArrayList result=new ArrayList();
	for(int i=0;i<_infosize;i++)
	    {
		ItemElement e=(ItemElement)_infoList.get(i);
		if(e.getKey().compareToIgnoreCase(key)== 0)
		    {
			result.add(e);
		    }
	    }
	return result;
    }

    private ArrayList FindInfoRegExp(String keyPattern)
    {
	ArrayList result = new ArrayList();

	 PatternMatcher matcher   = new Perl5Matcher();
	 PatternCompiler compiler = new Perl5Compiler();
	 
	 Pattern pattern = null;
	 try 
	     {
		 pattern = compiler.compile(keyPattern);
	     } 
	 catch(MalformedPatternException e) 
	     {
		 System.err.println(e.getMessage());
		 e.printStackTrace();
	     }
	 
	for(int i=0;i<_infosize;i++)
	    {
		ItemElement e=(ItemElement)_infoList.get(i);
		if(matcher.matches(e.getKey(),pattern))
		    {
			result.add(e);
		    }
	    }
	return result;
    }

    //--------------------html -------------------------- 
    private ArrayList findHtml(String key)
    {
	ArrayList results;
	String indexPathName;

	
	//File statePath = HelpPlugin.getDefault().getStateLocation().toFile();
	
	/////////////////////FIXME: get path to the project's ".metadata"///////
	File statePath = new File("/tmp");

	File indexPath = new File(statePath,IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION);
	
	HelpSettings settings = HelpSettings.getDefault();

	if (indexPath.exists() && settings.getBoolean(IHelpSearchConstants.HELP_SETTINGS_INDEXEXISTS))
	    {
		try{
		    indexPathName = indexPath.getCanonicalPath();
		}catch(Exception e){e.printStackTrace();return new ArrayList();}
		    
		SearchHtml searchBox = new SearchHtml();
		results = searchBox.search(indexPathName,key);		
		return results;
	    }
	else
	    {
		return new ArrayList();
	    }
    }

    public static void setList(ArrayList list)
    {
	_result.clear();
	_result.addAll(list);
    }
    
    public static ArrayList getList()
    {
	return _result;
    }

    public static ItemElement getItemElement(int index)
    {
	return (ItemElement)_result.get(index);
    }

    private boolean isWindows()
    {
	return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
    
}
