package org.eclipse.cdt.cpp.miners.help.helpwebapp;


/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.io.*;
import java.util.zip.*;

public class HelpWebApp extends HttpServlet 
{
    String _filename;
    String _nodename;
    ArrayList _fileList;

    private String manNamePrefix=null; // used by man.

    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws ServletException, IOException 
    {
	
	PrintWriter out;
	
	String type = request.getParameter("type"); 
	
	if (type == null || type.equals(""))
	    {
		HttpSession session = request.getSession(true);
		String baseurl = (String)session.getAttribute("baseurl");

		if (baseurl == null)
		    return;
		
		String requesturl = request.getRequestURI();
		String filename = baseurl+requesturl.substring("/cdthelp".length());
		
		if(requesturl.endsWith(".html")|| requesturl.endsWith(".htm"))
		    {
			response.setContentType("text/html");
			out = response.getWriter();
		
			File file = new File(filename);
			if(file.exists())
			    {
				String line;
				BufferedReader in = new BufferedReader(new FileReader(filename));
				while((line=in.readLine())!=null)
				    {
					out.println(line);
				    }
			    }
		    }
		else if(requesturl.endsWith(".png"))
		    {
			response.setContentType("image/png");
			OutputStream outB = response.getOutputStream();
			
			File file = new File(filename);
			if(file.exists())
			    {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				int theByte;
				while((theByte=in.read())!=-1)
				    {
					outB.write(theByte);
				    }
			    }

		    }
		else if(requesturl.endsWith(".gif"))
		    {
			response.setContentType("image/gif");
			OutputStream outB = response.getOutputStream();
			
			File file = new File(filename);
			if(file.exists())
			    {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				int theByte;
				while((theByte=in.read())!=-1)
				    {
					outB.write(theByte);
				    }
			    }
		    }	

		return;
	    }
	else if (type.equals("H"))
	    {
		String name = request.getParameter("name");

		HttpSession session = request.getSession(true);
		String thebase = name.substring(0,name.lastIndexOf("/"));
		session.setAttribute("baseurl",thebase );

		response.setContentType("text/html");
		out = response.getWriter();

		File theFile = new File(name);
		if(theFile.exists())
		    { 
			String line;
			BufferedReader in = new BufferedReader(new FileReader(name));
			while((line=in.readLine())!=null)
			    {
				out.println(line);
			    }
		    }		

		return;
	    }
	else if(type.equals("M"))
	    {
		String invocation = request.getParameter("invocation");
		if(invocation==null || invocation.equals(""))
		    return;
		String section = request.getParameter("section");
		//		if(section==null || section.equals(""))
		//    return;

		response.setContentType("text/html");
		out = response.getWriter();
		try{
		    processManPage(out,invocation,section);
		}catch(Exception e){
		    //e.printStackTrace();
		}
		return;
	    }
	else if(type.equals("I"))
	    {
		String invocation = request.getParameter("invocation");
		if(invocation==null || invocation.equals(""))
		    return;

		// invocation is in format: "info:/filename/nodename" 
		int left=invocation.indexOf("/");
		int right=invocation.lastIndexOf("/");
		if(left==-1 || right==-1 || left==right) 
		    return;

		_filename= invocation.substring(left+1, right);
		_nodename=invocation.substring(right+1);
		if(_filename==null || _filename.equals("") || _nodename==null || _nodename.equals(""))
		    return;	
 
	        _fileList = new ArrayList();
		
		response.setContentType("text/html");
		out = response.getWriter();
		String docType =
		    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		    "Transitional//EN\">\n";
	       
		out.println("<HTML>\n" +
			    "<HEAD><TITLE>" + invocation + "</TITLE></HEAD>\n" +
			    "<BODY BGCOLOR=\"#FDF5E6\">\n"+
			    "<pre>\n");
		
		try
		    {	
			processInfo(out);
		    }
		catch(Exception e)
		    {
			return;
		    }

		out.println("</pre>\n"+
			    "</BODY>\n"+
			    "</HTML>\n");
		
	    }
	else
	    {
		
		/*
		HttpSession session = request.getSession(true);
		String baseurl = (String)session.getAttribute("baseurl");

		String documentType =
		    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		    "Transitional//EN\">\n";
		String title = "Title Redirected";
		out.println(documentType +
			    "<HTML>\n" +
			    "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
			    "<BODY BGCOLOR=\"#FDF5E6\">\n" +
			    "<H1>" + title+"***"+baseurl+"***" + "</H1>\n" );
		out.println("<p>the request url="+request.getRequestURI());
		if(baseurl!=null)
		    out.println("<p>baseurl="+baseurl);
		out.println("</BODY></HTML>");
		*/
		return;
	    }
	
    }
    
    //---------------Man Pages ----------------
    public void processManPage(PrintWriter out, String name, String section)
    {
	Runtime runtime = Runtime.getRuntime();
 
	printHeader(out,name,section);
      
	try
	    {
		Process p;
		
		if(section ==null)
		    {
			String arg[] = new String[2];
			arg[0]="man";
			arg[1]=name;
			p=runtime.exec(arg);
		    }
		else
		    {
			String args[] = new String[3];
			args[0]="man";
			args[1]=section;
			args[2]=name;		
			p =runtime.exec(args);
		    }
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		String newline;
		boolean reachedSeeAlso=false;
		while((line=in.readLine())!=null)
		    {
			newline=processLine(line);

			if(reachedSeeAlso)
			    {
				if(newline.trim().length()!=0)
				    {
					String lineWithLinks = addLinks(newline);
					out.println(lineWithLinks);
				    }
				else
				    {
					reachedSeeAlso=false;
					out.println(newline);
				    }
			    }
			else
			    {
				if(newline.startsWith("<b>SEE</b>") && newline.endsWith("<b>ALSO</b>"))
				    {
					reachedSeeAlso=true; 
				    }
				out.println(getSectionHeader(newline));
			    }
		    }
		in.close();
		p.destroy();
	    }
	catch(Exception e)
	    {
		e.printStackTrace();

	    }
	out.println("</PRE>\n"+
		    "</BODY>\n"+
		    "</HTML>");

    }

    private String getSectionHeader(String line)
    {
        //if(line.startsWith("<b>") && line.endsWith("</b>"))
	if(isHeader(line))
	    {
		StringBuffer newline = new StringBuffer("</pre><h2>"+
							line.substring("<b>".length(),
								       line.length()-"</b>".length())+
							"</h2><pre>");
		String templine1 = newline.toString();
		int i=templine1.length();
		while((i=templine1.lastIndexOf("<b>",i-1))!=-1)
		    {
			newline.delete(i,i+"<b>".length());
		    }

		String templine = newline.toString();
		i=templine.length();
		while((i=templine.lastIndexOf("</b>",i-1))!=-1)
		    {
			newline.delete(i,i+"</b>".length());
		    }
		return newline.toString();
	    }
	else
	    return line;
    }

    private boolean isHeader(String line)
    {
	if(line.startsWith("<b>") && line.endsWith("</b>"))
	    {
		char linechar[]= line.toCharArray();
		boolean inTag=false;
		for(int i=0;i<linechar.length;i++)
		    {
			if(inTag)
			    {
				if(linechar[i]=='>')
				    {
					inTag=false;
				    }
				continue;
			    }
			else if(linechar[i]=='<')
			    {
				inTag=true;
				continue;
			    }
			
			if (Character.isLetter(linechar[i]) && Character.isLowerCase(linechar[i]))
			    return false;
		    }
		return true;
	    }
	return false;

    }

    private void printHeader(PrintWriter out,String name, String section)
    {
	String title;
	if(section!=null)
	    title=name+"("+section+")";
	else
	    title=name;

	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"+
			   "<HTML>\n"+
			   "<HEAD>\n"+
			   "<TITLE>"+ title+"</TITLE>\n");
	printStyle(out);
	out.println("</HEAD>\n"+
		    "<BODY>\n"+
		    "<h1 align=center>"+title+"</h1>\n"+
		    "<PRE>");
    }

    private void printStyle(PrintWriter out)
    {
	out.println("<style type=\"text/css\">\n" +
		    "<!--\n"+
		    "pre {font-family:Courier,monospace; font-size:12pt;}\n" +
		    "b{color:#006699;}\n"+
		    "i{color:#339900;font-weight:bold;}\n"+
		    "h2{color:#FF0033;font-size:120%;}\n"+
		    "h1{color:#FF0033;text-transform:uppercase;}\n"+
		    "body {background-color:#CCCCCC;}\n"+
		    "-->\n"+
		    "</style>\n"
		    );
    }

    private String addLinks(String line)
    {
	StringBuffer result = new StringBuffer();
	StringTokenizer st = new StringTokenizer(line,",");
	int numLeadingSpaces=0;

	for(int i=0;i<line.length();i++)
	    {
		if(Character.isWhitespace(line.charAt(i)))
		    numLeadingSpaces++;
		else
		    break;
	    }

	while(st.hasMoreTokens())
	    {
		String token = st.nextToken();
		String manName=null;
		String manSection=null;
		int sectbegin = token.indexOf('(');
		int sectend = token.indexOf(')');
		
		if(sectbegin!=-1 && sectend!=-1)
		    {
			manSection = token.substring(sectbegin+1,sectend); 
		    }
		
		try{
		    int bb=token.indexOf("<b>");
		    int be=token.indexOf("</b>");
		    int ib=token.indexOf("<i>");
		    int ie=token.indexOf("</i>");
		    String manNameSuffix=null;

		    if(bb!=-1 && be!=-1)
			{
			    if(sectbegin!=-1 && sectbegin<be)
				manNameSuffix =token.substring(bb+3,sectbegin);
			    else
				manNameSuffix = token.substring(bb+3,be);
			}
		    else if(ib!=-1 && ie!=-1)
			{
			    if(sectbegin!=-1 && sectbegin<ie)
				manNameSuffix = token.substring(ib+3,sectbegin);
			    else
				manNameSuffix = token.substring(ib+3,ie);
			}
		    else if(sectbegin!=-1)
			{
			    manNameSuffix = token.substring(0,sectbegin).trim();
			}
		    else
			{
			    manNameSuffix = token.trim();
			}
		    
		     if(manNamePrefix!=null)
			 {
			     manName = manNamePrefix+manNameSuffix;
			     manNamePrefix=null;
			 }
		     else
			 {
			     manName = manNameSuffix;
			 }
		     
		}catch(Exception e){
		    e.printStackTrace();		   
		}

		 if(manName!=null && manName.length()>0)
		     {
			 if(!st.hasMoreTokens() && 173==(int)manName.charAt(manName.length()-1))
			     {
				 manNamePrefix=manName.substring(0,manName.length()-1);
				 continue;
			     }
			 
			 if(manSection!=null)
			     result.append("<a href=Help?type=M&invocation="+ manName+
					   "&section="+manSection+
					   "><b>"+manName+"</b></a>("+manSection+"), ");
			 else
			     result.append("<a href=Help?type=M&invocation="+ manName+
					   "><b>"+manName+"</b></a>, ");
		     }
		 else
		     {
			 result.append(token.trim()+", ");
		     }
	    }
	for(int i=0;i<numLeadingSpaces;i++)
	    result.insert(0,' ');
	return result.toString();
    }


    private String processLine(String line)
    {
	char in[]=line.toCharArray();
	StringBuffer result = new StringBuffer();
	char format[] = new char[line.length()];

	boolean p= false;

	for(int i=0;i<line.length();i++)
	    {
		if(8==(int)in[i])
		    {
			p=true;			
		    }
		else
		    {
			if(p==true)
			    {
				if(in[i]==result.charAt(result.length()-1))
				    {
					format[result.length()-1]='b';
				    }
				else if('_'==result.charAt(result.length()-1))
				    {
					format[result.length()-1]='u';
					result.deleteCharAt(result.length()-1);
					result.append(in[i]);
				    }
			    }
			else
			    {
				format[result.length()]='n';
				result.append(in[i]);
			    }
			p=false;
		    }
	    }

	StringBuffer result2 = new StringBuffer();
	StringBuffer format2 = new StringBuffer();
	//encode special characters
	for(int i=0;i<result.length();i++)
	    {
		char c = result.charAt(i);
		char f = format[i];
		if(c=='<')
		    {
			result2.append("&lt;");
			format2.append(f).append(f).append(f).append(f);
		    }
		else if(c=='>')
		    {
			result2.append("&gt;");
			format2.append(f).append(f).append(f).append(f);
		    }
		else if(c=='&')
		    {
			result2.append("&amp;");
			format2.append(f).append(f).append(f).append(f).append(f);
		    }
		else if(c=='"')
		    {
			result2.append("&quot;");
			format2.append(f).append(f).append(f).append(f).append(f).append(f);
		    }
		else
		    {
			result2.append(c);
			format2.append(f);
		    }
	    }

	boolean inb=false;
	boolean inu=false;
	StringBuffer result3=new StringBuffer();
	char format3[]= format2.toString().toCharArray();

	for(int i=0;i<result2.length();i++)
	    {
		if(format3[i]=='b')
		    {
			if(inu)
			    {
				result3.append("</i>");
				inu=false;
			    }
			if(!inb)
			    {
				result3.append("<b>");
				inb=true;
			    }
		    }
		else if(format3[i]=='u')
		    {
			if(inb)
			    {
				result3.append("</b>");
				inb=false;
			    }
			if(!inu)
			    {
				result3.append("<i>");
				inu= true;
			    }
		    }
		else
		    {
			if(inb)
			    {
				result3.append("</b>");
				inb=false;
			    }
			else if(inu)
			    {
				result3.append("</i>");
				inu=false;
			    }
		    }

		result3.append(result2.charAt(i));
	    }
 

	if(inb)
	    result3.append("</b>");
	if(inu)
	    result3.append("</i>");


	return result3.toString();
    }


    /*********
    private void processManPage(PrintWriter out, String invocation, String section)
    {
	ArrayList manList = getManByName(invocation);
	String filename = getManBySection(manList, section);
	if(filename != null)
	    {
		if(filename.endsWith(".gz"))
		{
			ArrayList content = getHtmlContent(filename);
			
			for(int i=0;i<content.size();i++)
			    {
				out.println((String)content.get(i));
			    }
		    }		    
	    }
    }
    
    private ArrayList getManByName(String name) 
    {
	Runtime runtime = Runtime.getRuntime();
	String args[]=new String[3];
	args[0]="man";
	args[1]="-aw"; // "aW" also exists in RH but not in Suse
	args[2]=name;

	ArrayList results = new ArrayList();

	 try
	    {
		Process p = runtime.exec(args);
		BufferedReader in= new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		String line;
		int spacePos;
		while ((line=in.readLine())!=null)
		    {
			spacePos = line.indexOf(' ');
			if(spacePos==-1)
			    {
				results.add(line);
			    }
			else
			    {
				results.add(line.substring(0,spacePos));
			    }
		    }
	       	//	in.close();
		//	p.destroy();
	    }
	 catch(Exception e)
	     {
		 //e.printStackTrace();
	     }

	 return results;	
    }

    private String getManBySection(ArrayList manList, String section)	
    {
	for(int i=0;i<manList.size();i++)
	    {
		String candidate = (String)manList.get(i);
		String candidateName = candidate.substring(candidate.lastIndexOf("/")+1);
		String candidateSection = candidateName.substring(candidateName.indexOf(".")+1,
								  candidateName.lastIndexOf("."));
		if(candidateSection != null && candidateSection!="" && candidateSection.equals(section))
		    {
			return candidate;
		    }
	    }
	return null;
    }
    
    private ArrayList getHtmlContent(String filename)
    {
	Runtime runtime = Runtime.getRuntime();

	String args2[]=new String[3];
	args2[0]="groff";
	args2[1]="-Thtml";
	args2[2]="-mandoc";

	ArrayList results = new ArrayList();

	 try
	    {
		BufferedReader input = new BufferedReader(new InputStreamReader(new GZIPInputStream (new FileInputStream(filename))));

		Process p2 = runtime.exec(args2);
		BufferedWriter out =new BufferedWriter(new OutputStreamWriter(p2.getOutputStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(p2.getInputStream()));

		String line;
		while ((line=input.readLine())!=null)
		    {
			out.write(line,0,line.length());
			out.newLine();
		    }
		
		input.close();

		out.flush();
		out.close();

		while ((line=in.readLine())!=null)
		    {
			results.add(line);
			
		    }
	       
		//    	in.close();
		//	p.destroy();
	    }
	 catch(Exception e)
	     {
		 //e.printStackTrace();
	     }

	 return results;	
    }
    **************/


    //-------------------Info Pages ---------
    private void processInfo(PrintWriter out)throws IOException
    {
	boolean nodeFound;
	nodeFound = processInfoFile(_filename+".info", out);
	
	if(!nodeFound)
	    {
		boolean success=false;
		for (int i=0;i<_fileList.size();i++)
		    {
			success = processInfoFile((String)_fileList.get(i), out);
			if(success) break;
		    }
	    }
    }

    private boolean processInfoFile(String filename, PrintWriter out) throws IOException
    {
	String infofilename = "/usr/share/info/" + filename + ".gz";
	File infofile = new File(infofilename);
	boolean nodeFound = false;

	if(!infofile.exists())
	    return false; //FIXME: "/usr/share/info" as argument?
	try{
	    BufferedReader  in = new BufferedReader(new InputStreamReader(new GZIPInputStream (new FileInputStream(infofile))));
	    
	    String line;
	   
	    while(true)
		{
		    line = in.readLine();
		    if (line==null) break;
		    if (line.length()==1)
			{
			    int theChar=(int)line.charAt(0);
			    if(theChar==31)
				{
				    line=in.readLine();
				    if (line==null) break;
				    
				    if(line.startsWith("Indirect:"))
					{
					    processIndirect(in);
					}
				    else if(line.startsWith("Tag Table:"))
					{
					    processTagTable(in);
					}
				    else if(line.startsWith("File:"))
					{
					    nodeFound = processNodeHeader(line,_nodename,out);
					    processNode(in,nodeFound,out);   
					    if (nodeFound)
						break;
						
					}   


				}
		      
			}
		}

	    in.close();
	}catch(Exception e)
	    {
		e.printStackTrace();
	    }
	return nodeFound;
	
    }

    private boolean processNodeHeader(String header,String nodewanted, PrintWriter out)
    {
	String filename=null;
	String nodename=null;
	String next=null;
	String prev=null;
	String up=null;

	StringTokenizer st = new StringTokenizer(header,",");
	while (st.hasMoreTokens())
	    {
		String field = st.nextToken().trim();
		if(field.startsWith("File:"))
		    {
			filename = field.substring("File:".length()).trim();
		    }
		else if(field.startsWith("Node:"))
		    {
			nodename = field.substring("Node:".length()).trim();
		    }
		else if(field.startsWith("Next:"))
		    {
			next = field.substring("Next:".length()).trim();
		    }
		else if(field.startsWith("Prev:"))
		    {
			prev = field.substring("Prev:".length()).trim();
		    }
		else if(field.startsWith("Up:"))
		    {
			up = field.substring("Up:".length()).trim();
		    }
	    }
	if(nodewanted.equals(nodename))
	    {
		StringBuffer link = new StringBuffer();
		if(nodename!=null)
		    {
			if(prev!=null && !prev.startsWith("("))
			    link.append("<a href=Help?type=I&invocation=info:/"+_filename+"/"+prev.replace(' ','+')+
					"><H3>"+"Previous:"+prev+"</H3></a>");
			if(up!=null && !up.startsWith("(") )
			    link.append("<a href=Help?type=I&invocation=info:/"+_filename+"/"+up.replace(' ','+')+
					"><H3>"+"Up:"+up+"</H3></a>");
			if(next!=null && !next.startsWith("("))
			    link.append("<a href=Help?type=I&invocation=info:/"+_filename+"/"+next.replace(' ','+')+
					"><H3>"+"Next:"+next+"</H3></a>");
		   
			out.println(link.toString());			
			out.println("<center><H2>"+nodename+"</H2></center>");
		    }
      		return true;
	    }
	return false;
    }

    private void processNode(BufferedReader in, boolean nodeFound, PrintWriter out) throws IOException
    {
	String line;

	while(true)
	    {
		in.mark(200);
		line = in.readLine();
		if(line == null) break;
		if(line.length()==1)
		    {
			int theChar=(int)line.charAt(0);
			if(theChar==31)
			    {
				//reached the begining of another node
				in.reset();
				return;
			    }
		    }

		//process
		if(nodeFound)
		    {
			if(line.startsWith("*") && line.indexOf("::")!= -1)
			    {
				StringBuffer newLine = new StringBuffer();
				int begin=line.indexOf('*')+1;
				int end=line.indexOf("::");
				if(begin<end)
				    {
					String destnode = line.substring(begin,end).trim();					
					newLine.append("<a href=Help?type=I&invocation=info:/"+_filename+"/"+destnode.replace(' ','+')+
						       ">"+destnode+"</a>"+line.substring(end+"::".length()));
					out.println(newLine.toString());
				    }
			    }
			else if(line.startsWith("*")&& line.indexOf(":")!=-1)
			    {
				StringBuffer newLine = new StringBuffer();
				int begin=line.indexOf('*')+1;
				int end=line.indexOf(":");
				if(begin<end)
				    {
					String destnode = line.substring(end+":".length()).replace('.',' ').trim();
					if(destnode!=null && !destnode.equals(""))
					    {
						newLine.append("<a href=Help?type=I&invocation=info:/"+_filename+"/"+destnode.replace(' ','+')+
							       ">"+line.substring(begin,end).trim()+"</a>"+line.substring(end+":".length()));
						out.println(newLine.toString());
					    }
				    }
			    }
			else
			    {
				out.println(filterHtmlTags(line));
			    }
		    }
	    }
	
    }
    
    private void processTagTable(BufferedReader in/*,PrintWriter out*/) throws IOException
    {
	String line;
	while(true)
	    {
		in.mark(200);
		line=in.readLine();
		if(line==null) break;
		if(line.length()==1)
		    {
			int theChar=(int)line.charAt(0);
			if(theChar==31)
			    {
				//reached the begining of another node
				in.reset();
				return;
			    }		
		    }
		    
		//process
		//don't do anything. i.e skip this block
		
		/**
		if(!line.toLowerCase().startsWith("node:"))
		    continue; //skip non-nodes e.g "Ref:"
		
		
		int begin,end;
		begin= "Node:".length();
		end=line.indexOf(127);
		if(end==-1)
		    continue;// should never get here
		String nodename = line.substring(begin,end);
		
		***/
	    }
	
    }

    private void processIndirect(BufferedReader in/*,PrintWriter out*/) throws IOException
    {
	String line;
	while(true)
	    {
		in.mark(200);
		line=in.readLine();
		if(line==null) break;
		if(line.length()==1)
		    {
			int theChar=(int)line.charAt(0);
			if(theChar==31)
			    {
				//reached the begining of another node
				in.reset();
				return;
			    }
		    }
		    
		//process
		if(!line.toLowerCase().startsWith(_filename))
		    {
			continue;
		    }
		else
		    {
			int end = line.indexOf(":");
			if (end==-1)
			    continue;
			String filename = line.substring(0,end);
			_fileList.add(filename);
		    }
	    }
    }

    private String filterHtmlTags(String line)
    {
	StringBuffer result = new StringBuffer();
	char currChar;
	for(int i =0;i<line.length();i++)
	    {
		currChar = line.charAt(i);
		if(currChar=='<') result.append("&lt;");
		else if (currChar=='>') result.append("&gt;");
		else if (currChar=='&') result.append("&amp;");
		else if (currChar=='"') result.append("&quot;");
		/*else if*/
		else result.append(currChar);
	    }
	return result.toString();
    }

}
