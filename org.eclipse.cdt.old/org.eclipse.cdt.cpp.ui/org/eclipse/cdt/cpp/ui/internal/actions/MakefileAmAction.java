package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerAction;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;

import com.ibm.cpp.miners.managedproject.MakefileAmClassifier;

public class MakefileAmAction extends CustomAction {
	
	// to identify Makefile.am identity
	private static MakefileAmClassifier classifier = new MakefileAmClassifier();
	private final int TOPLEVEL = 1;
	private final int PROGRAMS = 2;
	private final int STATICLIB = 3;
	private final int SHAREDLIB = 4;
	
	public MakefileAmAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		if(_command.getValue().equals("INSERT_CONFIGURE_IN")||_command.getValue().equals("TOPLEVEL_MAKEFILE_AM"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
		if(_command.getValue().equals("COMPILER_FLAGS")&&!doesFileExist("Makefile.am"))	
			setEnabled(false);
		//	
		if(_command.getValue().equals("INSERT_CONFIGURE_IN")&&doesFileExist("configure.in"))	
				setEnabled(false);
		// classifying Makefile.am
		if(doesFileExist("Makefile.am"))
		{
			File Makefile_am = new File(_subject.getSource(),"Makefile.am");
			int classification = classifier.classify(Makefile_am);
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
	}
	public void run()
	{
		/*
		// open dialog to set Makefile.am compiler flags
		if(_command.getValue().equals("COMPILER_FLAGS"))
		{
			com.ibm.dstore.hosts.dialogs.CompilerFlagDialog cfd = new com.ibm.dstore.hosts.dialogs.CompilerFlagDialog(_subject);
			// populate if found any C or CXX flag def
			String existingDef = getExistingDef(_subject);
			//cfd.setDefinition(existingDef);
			cfd.open();
			System.out.println("\n ysuuhsa = "+cfd._definitionArea.getText());
			//get the user input
			//String newDef = cfd.getDefinitionInput();
			// insert this def in Makefile.am
			// modify Makefile.am
		}
		*/
		DataElement makefileAmCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());
		DataElement status = _dataStore.command(makefileAmCmd, _subject);
		ModelInterface api = ModelInterface.getInstance();
		api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
		api.monitorStatus(status);
    }
    private String getExistingDef(DataElement parent)
    {
    	File MakefileAm = new File(_subject.getSource(),"Makefile.am");
		String line;
		boolean found = false;
		try
		{
			// searching for FLAGS line
			BufferedReader in = new BufferedReader(new FileReader(MakefileAm));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf("CFLAGS")!=-1||line.indexOf("CXXFLAGS")!=-1
					||line.indexOf("CPPFLAGS")!=-1)
					return line;
			}
			in.close();
		}catch(IOException e){System.out.println(e);}  	
    	return "FLAG DEF";
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
}
