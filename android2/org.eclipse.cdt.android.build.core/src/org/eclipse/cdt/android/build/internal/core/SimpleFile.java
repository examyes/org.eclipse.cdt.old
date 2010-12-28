package org.eclipse.cdt.android.build.internal.core;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class SimpleFile extends ProcessRunner {

	private static final class FileOp {
		public String source;
		public String destination;
	}
	
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {

		// Fetch the args
		String projectName = null;
		List<FileOp> fileOps = new ArrayList<FileOp>();
		
		for (ProcessArgument arg : args) {
			if (arg.getName().equals("projectName"))
				projectName = arg.getSimpleValue();
			else if (arg.getName().equals("files")) {
				ProcessArgument[][] files = arg.getComplexArrayValue();
				for (ProcessArgument[] file : files) {
					FileOp op = new FileOp();
					for (ProcessArgument fileArg : file) {
						if (fileArg.getName().equals("source"))
							op.source = fileArg.getSimpleValue();
						else if (fileArg.getName().equals("destination"))
							op.destination = fileArg.getSimpleValue();
					}
					if (op.source == null || op.destination == null)
						throw new ProcessFailureException("bad file op");
					fileOps.add(op);
				}
			}
		}
		
		if (projectName == null)
			throw new ProcessFailureException("no project name");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
			throw new ProcessFailureException("project does not exist");
		
		// Find bundle to find source files
		String pluginId = template.getTemplateInfo().getPluginId();
		Bundle[] bundles = Activator.getContext().getBundles();
		Bundle templateBundle = null;
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(pluginId)) {
				templateBundle = bundle;
				break;
			}
		}
		
		if (templateBundle == null)
			throw new ProcessFailureException("bundle not found");
		
		try {
			for (FileOp op : fileOps) {
				IFile destFile = project.getFile(new Path(op.destination));
				if (destFile.exists())
					// don't overwrite files if they exist already
					continue;
				
				URL sourceURL = FileLocator.find(templateBundle, new Path(op.source), null);
				if (sourceURL == null)
					throw new ProcessFailureException("could not find source file: " + op.source);
				
				TemplatedInputStream in = new TemplatedInputStream(sourceURL.openStream(), template.getValueStore());
				destFile.create(in, true, monitor);
				in.close();
			}			
		} catch (IOException e) {
			throw new ProcessFailureException(e);
		} catch (CoreException e) {
			throw new ProcessFailureException(e);
		}

	}

}
