package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.core.resources.*;


public class MonitorStatusThread extends Handler
{
	private DataElement _status;
	private IProject    _project;
	private CppProjectNotifier _projectNotifier;
	private int         _threshold;
	private int         _timesHandled;

	public MonitorStatusThread(DataElement status, IProject project)
	{
	    super();
	    setWaitTime(500);
	    _threshold = 100;
	    _timesHandled = 0;
	    _status = status;
	    _project = project;
	    _projectNotifier = ModelInterface.getInstance().getProjectNotifier();
	    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.COMMAND,
								    CppProjectEvent.START,
								    _status,
								    _project));
	}
	
	public void handle()
	{	
	    String statusValue = _status.getName();
	    _status.getDataStore().trace("monitor status = " + _status.getId() + " " + _status.getName());
	    if (_timesHandled > _threshold)
		{
		    if ((_project == null) ||  (!_project.isOpen()))
			{
			    statusValue = "done";
			}
		}
	
	    if (statusValue.equals("done") || statusValue.equals("timeout"))
		{
		    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.COMMAND,
									    CppProjectEvent.DONE,
									    _status,
									    _project));
		    finish();
		}
	    else
		{
		    _projectNotifier.fireProjectChanged(new CppProjectEvent(CppProjectEvent.COMMAND,
									    CppProjectEvent.WORKING,
									    _status,
									    _project));
		    _timesHandled++;
		}
	}
 }