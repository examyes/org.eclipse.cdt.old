package org.eclipse.cdt.linux.help.search;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;

public class SearchHtmlWithProgress implements IRunnableWithProgress
{
    String _indexPath;
    ArrayList _paths;

    public SearchHtmlWithProgress (String indexPathName, ArrayList paths)
    {
	_indexPath=indexPathName;
	_paths=paths;
    }

    public void run(IProgressMonitor monitor)
    {
	boolean success = false;
	SearchHtml searchBox = new SearchHtml();
	success = searchBox.createIndex(_indexPath, _paths, monitor);

	//FIXME:save success flag 
    }

}
