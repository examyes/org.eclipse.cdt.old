package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.ILinkable;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.search.ui.*;

public class SearchLabelProvider extends LabelProvider implements ITableLabelProvider
    {
	static private SearchLabelProvider _instance = new SearchLabelProvider();

	public SearchLabelProvider()
	{
	    super();
	}

	static public SearchLabelProvider getInstance()
	{
	    return _instance;
	}

	public Image getImage(Object obj)
	{
	    if (obj instanceof ISearchResultViewEntry)
		{	
		    ISearchResultViewEntry entry = (ISearchResultViewEntry)obj;
		    IMarker marker = entry.getSelectedMarker();
		    DataElement element = getElement(marker);
		    if (element != null)
			{
			    String imageStr = com.ibm.dstore.ui.widgets.DataElementLabelProvider.getImageString(element);
			    return CppPlugin.getDefault().getImage(imageStr);
			}
		    else
			{
			    return null;
			}
		}

	    return null;
	}

	public String getText(Object obj)
	{
	    if (obj instanceof ISearchResultViewEntry)
		{	
		    ISearchResultViewEntry entry = (ISearchResultViewEntry)obj;
		    IMarker marker = entry.getSelectedMarker();
		    DataElement element = getElement(marker);
		    if (element != null)
			{
			    return (String)element.getElementProperty(DE.P_VALUE);
			}
		    else
			{
			    return "null";
			}
		}

	    return "null";
	}

	private DataElement getElement(IMarker marker)
	{
	    DataElement result = null;
	    try
		{
		    result = (DataElement)marker.getAttribute("DataElementID");
		}
	    catch (CoreException e)
		{
		    System.out.println(e);
		}
	
	    return result;	
	}

	public Image getColumnImage(Object element, int columnIndex)
	{
	    return getImage(element);
	}
	
	
	public String getColumnText(Object element, int columnIndex)
	{
	    return getText(element);
	}
    }
