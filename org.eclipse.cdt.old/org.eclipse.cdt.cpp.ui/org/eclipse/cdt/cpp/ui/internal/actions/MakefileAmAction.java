package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.ArrayList;

import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStore;
import org.eclipse.cdt.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.cdt.dstore.extra.internal.extra.IDomainListener;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;
import org.eclipse.swt.widgets.Shell;

public class MakefileAmAction extends CustomAction implements IDomainListener 
{
	
	// to identify Makefile.am type
	private final int TOPLEVEL = 1;
	private final int PROGRAMS = 2;
	private final int STATICLIB = 3;
	private final int SHAREDLIB = 4;

    private DataElement _cmdStatus;
	
	public MakefileAmAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
	
		String value = _command.getValue();
			
		if(value.equals("INSERT_CONFIGURE_IN")|| value.equals("TOPLEVEL_MAKEFILE_AM"))
		{
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
			return;
		}
						
		if(value.equals("INSERT_CONFIGURE_IN") && doesFileExist("configure.in"))	
		{
			setEnabled(false);
			return;
		}

		if(value.equals("TOPLEVEL_MAKEFILE_AM") || value.equals("PROGRAMS_MAKEFILE_AM") ||
		 	value.equals("STATICLIB_MAKEFILE_AM") || value.equals("SHAREDLIB_MAKEFILE_AM"))
		{
			DataElement cmdD = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CLASSIFY_MAKEFILE_AM");
			if (cmdD != null)
			{

				DataElement classifier = null;
				ArrayList updated = _subject.getAssociated("class type");
				if (updated.size() > 0)
				{
					classifier = (DataElement)updated.get(0);			
				}
				
				String type = null;		
				if (classifier != null)
				{
					type = classifier.getName();		
				} 				
				else 
				{	

				    // DKM
				    // ---
				    // Yasser, I've made this class impelment domain listener so that it doesn't have to
				    // use synchronized commands.  In the remote case, that is a problem.
				    
				    /***
					DataElement status = _dataStore.synchronizedCommand(cmdD, _subject);								
								
					updated = _subject.getAssociated("class type");		
					if (updated != null && updated.size() > 0)
					{						
					 classifier = (DataElement)updated.get(0);
					 type = classifier.getName();
					}
					else
					{
					  type = "0";	
					}
				    ***/
				    
				    _cmdStatus = _dataStore.command(cmdD, _subject);
				    _dataStore.getDomainNotifier().addDomainListener(this);
				    type = "0";
				}

				setEnabledState(type);
				
				return;
			}
		}
	}

    private void setEnabledState(String type)
    {
	int classification = (new Integer(type)).intValue();
	
	switch (classification)
	    {
	    case (TOPLEVEL):
		if(_command.getValue().equals("TOPLEVEL_MAKEFILE_AM"))
		    setEnabled(false);
		break;
		
	    case (PROGRAMS):
		if(_command.getValue().equals("PROGRAMS_MAKEFILE_AM"))
		    setEnabled(false);
		break;
		
	    case (STATICLIB):
		if(_command.getValue().equals("STATICLIB_MAKEFILE_AM"))
		    setEnabled(false);
		break;
		
	    case (SHAREDLIB):
		if(_command.getValue().equals("SHAREDLIB_MAKEFILE_AM"))
		    setEnabled(false);
		break;
		
	    default:
		break;
	    }
    }
    
    public void run()
	{
		
		DataElement makefileAmCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());
		DataElement status = _dataStore.command(makefileAmCmd, _subject);
		ModelInterface api = ModelInterface.getInstance();
		api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
		api.monitorStatus(status);
    }

 	private boolean doesFileExist(String fileName)
	{
		for (int i = 0; i < _subject.getNestedSize(); i++)
		    {
			DataElement child = _subject.get(i).dereference();
			if (!child.isDeleted() && child.getName().equals(fileName))
			    {
					return true;
			    }
		    }
		return false;
	}


    // DKM
    // ----
    // We implement domain notifier her so that we don't have to synchronize commands
    // to handle enable state
    public Shell getShell()
    {
		ModelInterface api = ModelInterface.getInstance();
		return api.getDummyShell();
    }
    

    public boolean listeningTo(DomainEvent e)
    {
		DataElement parent = (DataElement)e.getParent();
		if (parent == _cmdStatus)
	    {
			if (parent.getName().equals("done"))
			{
				return true;
			}
		}

		return false;
    }

    public void domainChanged(DomainEvent e)
    {
		DataElement classifier = null;
		ArrayList updated = _subject.getAssociated("class type");
		if (updated.size() > 0)
		{
			classifier = (DataElement)updated.get(0);			
		}
	
		String type = null;		
		if (classifier != null)
	    {
			type = classifier.getName();		
	    } 				
		else
	    {
			type = "0";	
	    }

		_cmdStatus = null;
		_dataStore.getDomainNotifier().removeDomainListener(this);
		setEnabledState(type);
    }
}
