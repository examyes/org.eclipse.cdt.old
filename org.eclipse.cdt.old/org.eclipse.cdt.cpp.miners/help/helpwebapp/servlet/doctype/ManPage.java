package help.helpwebapp.servlet.doctype;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.util.*;

public class ManPage
{

    private String manNamePrefix=null; // used by man.

    public ManPage()
    {
    }

    public void process(PrintWriter out, String name, String section)
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
		    "pre {font-family:Terminal,Courier,monospace; font-size:12pt;}\n" +
		    "b{color:#3333CC;}\n"+
		    "i{color:#00BC00;}\n"+
		    "h2{color:#DC1436;font-size:100%;}\n"+
		    "h1{color:#DC1436;text-transform:uppercase;font-size:120%;}\n"+
		    "body {background-color:#FFFFE0;}\n"+
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


}
