package org.eclipse.cdt.cpp.ui.builder;

import java.util.*;

import java.io.*;
import java.util.*;

import org.eclipse.cdt.cpp.ui.api.*;
import org.eclipse.cdt.cpp.ui.dialogs.*;
import com.ibm.eclipse.internal.ui.dialogs.*;
import org.eclipse.cdt.cpp.ui.*;

import com.ibm.eclipse.core.resources.*;
import com.ibm.eclipse.core.runtime.*;
import com.ibm.eclipse.ui.*;
import com.ibm.eclipse.ui.part.*;
import com.ibm.eclipse.internal.ui.*;
import com.ibm.eclipse.internal.core.events.*;

import com.ibm.jface.window.*;
import com.ibm.jface.action.*;
import com.ibm.jface.dialogs.*;
import com.ibm.swt.widgets.*;

import com.ibm.jface.*;
import com.ibm.jface.viewers.*;
import com.ibm.jface.preference.PreferenceManager;


public class CppBuilder extends BaseBuilder
{
  public class InvocationError implements Runnable
  {
    private IProject _project;

    public InvocationError(IProject project)
    {
      _project = project;      
    }
    
    public void run()
    {    
      String msg = "Project \"" + 
	_project.getName() + 
	"\" has no specified build invocation.  Please set the build invocation in the \"C++ Build Invocation\" property page for this project.";

      MessageDialog explainD = new MessageDialog("No Specified Build Invocation", null, msg, 
						 MessageDialog.INFORMATION,
						 new String[]  { "OK" },
						 0);
      
      explainD.openInformation(new Shell(), "NoBuildInvocation", msg);          
    }  
  }
  
  private String _invocation;

  public CppBuilder()
      {
      }

  public void build(int i, Map m, IResourceDelta change, IProgressMonitor monitor) throws CoreException
      {
        IProject project = getProject();	

        if ((project != null) && project.isOpen() &&
	    (CppPlugin.getDefault().isCppProject(project)))
	  {
	    if ((change != null) && ((ResourceDelta)change).hasAffectedChildren())
	      {		
		monitor.beginTask("Building " + project.getName(), 100);
		
		String path = new String(project.getLocation().toOSString());	
		_invocation = getInvocation();
		
		if ((_invocation == null) || (_invocation.length() == 0))
		  {	
		    InvocationError action = new InvocationError(project);
		    
		    IWorkbench aWorkbench = WorkbenchPlugin.getDefault().getWorkbench();
		    Window win= (Window)aWorkbench.getActiveWorkbenchWindow();
		    if (win != null)
		      {
			Shell shell = win.getShell();
			if (shell != null)
			  {			
			    Display d= shell.getDisplay();
			    if (d != null)
			      {		    
				d.asyncExec(action);
			      }	
			  }		    
		      }	
		  }
		else
		  {		
		    ModelInterface api = CppPlugin.getModelInterface();	
		    api.command(project, _invocation, false);
		  }	    
	      }
	  }	
      }
  
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
      {
        super.setInitializationData(config, propertyName, data);
      }

  private String getInvocation()
      {
        IProject project = getProject();
        if (project != null)
        {
          ArrayList history = CppPlugin.readProperty(project, "Build History");
          if ((history != null) && (history.size() > 0))
            return new String((String)history.get(0));
        }

        return null;      
      }
}
