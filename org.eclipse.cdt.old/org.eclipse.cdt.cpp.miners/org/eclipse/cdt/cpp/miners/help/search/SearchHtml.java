package org.eclipse.cdt.cpp.miners.help.search;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.miners.help.preferences.*;

import java.io.*;
import java.util.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;

//import org.apache.lucene.HTMLParser.*;
import org.apache.lucene.demo.html.*;

import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;

public class SearchHtml
{
    private static final String FIELD_PATH = "Filename";
    private static final String FIELD_CONTENT = "Content";
    private static final String FIELD_TITLE = "Title";

    private IndexWriter writer; 

    public boolean createIndex(String indexDestination,ArrayList paths)
    {	
	boolean success = true;
	//always recreate an index
	try
	    {
		writer = new IndexWriter(indexDestination,new SimpleAnalyzer(),true);
		
		for(int i=0;i<paths.size();i++)
		    {
			File file = new File((String)paths.get(i));
			if(!processFile(file))
			    success = false;
		    }	

		writer.close();
	    }
	catch(Exception e)
	    {
		e.printStackTrace();
		return false;
	    }
	return success;
    }
    
    private boolean processFile(File file)
    {
	boolean success = true;
	if(!file.exists())
	    return false;
	if(file.isDirectory())
	    {
		File[] fileList=file.listFiles();
		for(int i=0;i<fileList.length;i++)
		    {
			if(!processFile(fileList[i]))
			    success = false;
		    }
	    }
	else
	    {
		String filename = file.getName();
		// only html files are indexed... for now
		if(!(filename.endsWith(".html")||
		     filename.endsWith(".htm")))
		    return true;
		if(!addFileToDocument(file))
		    success = false;
	    }
	return success;
    }

    private boolean addFileToDocument(File theFile)
    {
	boolean success = true;
	try{

	    HTMLParser parser = new HTMLParser(theFile);
	    Document document = new Document();
	    document.add(Field.UnIndexed(FIELD_PATH,theFile.getCanonicalPath()));	    	    
	    document.add(Field.UnIndexed(FIELD_TITLE,parser.getTitle()));
	    document.add(Field.Text(FIELD_CONTENT,parser.getReader()));
	    // document.add(Field.Text(FIELD_CONTENT,
	    //		    (Reader)new InputStreamReader(new FileInputStream(theFile))));

	    writer.addDocument(document);
	}catch(Exception e)
	    {
		e.printStackTrace();
		success = false;
	    }
	return success;
	
    }

    public ArrayList search(String indexSource, String key)
    {
	ArrayList results = new ArrayList();
	try {
	    
	    Searcher engine = new IndexSearcher(indexSource);
	    Query query = QueryParser.parse(key,FIELD_CONTENT, new SimpleAnalyzer());
	    Hits hits = engine.search(query);
	    for(int i=0;i<hits.length();i++)
		{
		   
		    results.add(new ItemElement(null,hits.doc(i).get(FIELD_PATH),
						hits.doc(i).get(FIELD_TITLE),null,null,
						ItemElement.HTML_TYPE));
		    /**
		     results.add(new ItemElement(null,hits.doc(i).get(FIELD_PATH),
						"",null,null,
						ItemElement.HTML_TYPE));
		    **/
		}
	}catch(Exception e)
	    {
		return null;
	    }
	return results;
	
    }    

}
