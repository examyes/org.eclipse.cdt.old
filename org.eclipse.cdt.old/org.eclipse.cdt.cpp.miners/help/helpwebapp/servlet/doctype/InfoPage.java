package help.helpwebapp.servlet.doctype;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class InfoPage
{
    private String _invocation;
    private String _filename;
    private String _nodename;
    private ArrayList _fileList = new ArrayList();
    
    public InfoPage(String invocation)
    {
	_invocation = invocation;
    }
    
    public void process(PrintWriter out) throws IOException
    {
	// invocation is in format: "info:/filename/nodename" 
	int left = _invocation.indexOf("/");
	int right = _invocation.lastIndexOf("/");
	if(left==-1 || right==-1 || left==right) 
	    return;
	
	_filename = _invocation.substring(left+1, right);
	_nodename = _invocation.substring(right+1);
	if(_filename==null || _filename.equals("") || _nodename==null || _nodename.equals(""))
	    return;	

	processInfo(out);
    }

    private void processInfo(PrintWriter out)throws IOException
    {
	String docType =
	    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
	    "Transitional//EN\">\n";
	
	out.println("<HTML>\n" +
		    "<HEAD>\n"+
		    "<TITLE>" + _invocation + "</TITLE>\n"+
		    "<style type=\"text/css\">\n"+
		    "<!--\n"+
		    "TABLE{background-color:white;}\n"+
		    "TD{font-size:120%;height:50pt;width:33%;}\n"+
		    "-->\n"+
		    "</style>\n"+
		    "</HEAD>\n" +
		    "<BODY BGCOLOR=\"#FDF5E6\">\n"+
		    "<pre>\n");
	
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

	out.println("</pre>\n"+
			    "</BODY>\n"+
			    "</HTML>\n");
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
	if(nodewanted.equals(nodename) && nodename!=null)
	    {
		out.println("<center><H2>"+nodename+"<H2></center>");
		out.println("<TABLE rules=\"all\" border=\"1\" align=\"center\" width=\"100%\">\n"+
			    "<TR>\n"+
			    "<TD>PREVIOUS</TD><TD>UP</TD><TD>NEXT</TD>\n"+
			    "</TR>\n"+
			    "<TR>");

		if(prev!=null && !prev.startsWith("("))
		    {
			out.println("<TD><a href=Help?type=I&invocation=info:/"+_filename+"/"+prev.replace(' ','+')+
				    "><H3>"+prev+"</H3></a></TD>");
		    }
		else
		    {
			out.println("<TD></TD>");
		    }

		if(up!=null && !up.startsWith("(") )
		    {
			out.println("<TD><a href=Help?type=I&invocation=info:/"+_filename+"/"+up.replace(' ','+')+
				    "><H3>"+up+"</H3></a></TD>");
		    }
		else
		    {
			out.println("<TD></TD>");
		    }

		
		if(next!=null && !next.startsWith("("))
		    {
			out.println("<TD><a href=Help?type=I&invocation=info:/"+_filename+"/"+next.replace(' ','+')+
				    "><H3>"+next+"</H3></a></TD>");
		    }
		else
		    {
			out.println("<TD></TD>");
		    }

		out.println("</TR>\n"+
			    "</TABLE>");
		
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
