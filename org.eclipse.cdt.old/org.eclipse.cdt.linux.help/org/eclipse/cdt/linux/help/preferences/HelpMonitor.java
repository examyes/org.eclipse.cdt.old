package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;

import org.eclipse.cdt.dstore.core.model.*;

public class HelpMonitor implements IRunnableWithProgress
{
    String _message;
    //boolean _running = false;

    DataElement _status;

    public HelpMonitor(String message,DataElement status)
    {
	if(message !=null)
	    _message = message;
	else
	    _message = "";

	_status = status;
    }

    public void run(IProgressMonitor monitor)
    {
	//	_running=true;
	monitor.beginTask(_message,IProgressMonitor.UNKNOWN);
	while (/*_running &&*/ !_status.getName().equals("done"))
	    {
		monitor.worked(1);	
		//monitor.subTask("Processing: "+theFile.getName());
		try
		    {
				Thread.sleep(1000);
			Thread.yield();
		    }
		catch(Exception e)
		    {
			e.printStackTrace();
		    }
	    }
	monitor.done();
	
    }
    /*
    public void terminate()
    {
	_running = false;
    }
    */

}
