package help.helpwebapp.servlet;

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

import help.helpwebapp.servlet.doctype.*;

public class HelpWebApp extends HttpServlet 
{

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
		    ManPage manProcess = new ManPage();
		    manProcess.process(out,invocation,section);
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
		
		response.setContentType("text/html");
		out = response.getWriter();	
		
		try
		    {	
			InfoPage infoProcess = new InfoPage(invocation);
			infoProcess.process(out);
		    }
		catch(Exception e)
		    {
			return;
		    }
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
    
    

    



}
