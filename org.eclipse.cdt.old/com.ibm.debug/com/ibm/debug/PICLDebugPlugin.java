package com.ibm.debug;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/PICLDebugPlugin.java, eclipse, eclipse-dev, 20011129
// Version 1.24 (last modified 11/29/01 14:15:44)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.BreakpointsView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.debug.connection.ConnectionInfo;
import com.ibm.debug.internal.pdt.ui.actions.DebugViewMenuListener;
import com.ibm.debug.internal.pdt.ui.editor.DebuggerDocumentProvider;
import com.ibm.debug.internal.picl.PICLDebugUIDaemon;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.util.EclipseLogger;
import com.ibm.debug.launch.PICLDaemonInfo;
import com.ibm.debug.launch.PICLStartupInfo;

import java.util.Vector;

/**
 * Represents the compiled PICL plugin
 */
public class PICLDebugPlugin extends AbstractUIPlugin implements ISelectionListener {

	public final static String DAEMON_PORT = "DaemonPort";
	private final static String PLUGIN = "PICLDebugPlugin";

	private static PICLDebugPlugin instance;
	private boolean breakpointActionsForced = false;
	private boolean debugViewMenuListenerAdded = false;
	private static PICLDebugUIDaemon daemon = null;
	private static Vector infoList = new Vector();
	private static Vector freeInfoKeyList = new Vector();
	private DebuggerDocumentProvider editorDocumentProvider = null;
	public static EclipseLogger LOG;

	/**
	 * Constructor for PICLDebugPlugin
	 */
	public PICLDebugPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		instance = this;
		DebugUIPlugin.getDefault().addSelectionListener(this);
		PICLUtils.logText("PICLDebugPlugin loaded");

		// Code copied from GDBViewPlugin
		IPluginDescriptor pluginDescriptor = getDescriptor();
		String componentName = pluginDescriptor.getResourceString("%GdbViewLoggerName");
		LOG = new EclipseLogger(this,componentName);
	}

	public static PICLDebugPlugin getInstance() {
		return instance;
	}

	public static PICLDebugPlugin getDefault() {
		return instance;
	}

	public void startup() throws CoreException {
		PICLUtils.logText("In startup()");
		super.startup();
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		PICLUtils.logText("In shutdown()");
		super.shutdown();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the active workbench shell.
	 * Note: This method should only be called from a UI thread.
	 * When running on a non-UI thread, use getShell() method.
	 */
	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Returns the current debug target to use for model support.
	 * Returns the first <code>IDebugTarget</code> that has a launch and is associated with a debug target.
	 * If no debug targets, returns null.
	 */
	public static IDebugTarget determineCurrentDebugTarget() {

		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] debugTargets= launchManager.getDebugTargets();
		IProcess currentProcess = DebugUIPlugin.getDefault().getCurrentProcess();
		if(debugTargets == null || debugTargets.length==0)
			return null;
		if(currentProcess == null)
			return debugTargets[0];
		for (int i = 0; i < debugTargets.length; i++) {
			IDebugTarget target= debugTargets[i];
			IProcess targetProcess= target.getProcess();
			if (targetProcess != null && targetProcess.getLaunch() != null
				&& currentProcess == targetProcess) {
				return target;
			}
		}

		return debugTargets[0];
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	public void initializeDefaultPreferences(IPreferenceStore store)
	{
		store.setDefault(DAEMON_PORT, "8001");
	}

	/**
	 * Allows access to the protected method super.saveDialogSettings().
	 */
	public void saveDialogSettings()
	{
		super.saveDialogSettings();  //protected method
	}

	/**
	 * Sends a selection event to the breakpoints view to force creation of
	 * Exception/Breakpoint actions. This is necessary to ensure correct enablement of
	 * the actions when a picl debug element is selected in the stack frame.
	 * This will not disrupt the current selection, if any.
	 * This may fail if the breakpoint view is not showing in the debug perspective.
	 */
	private boolean activateBPViewActions()
	{
		IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) 	return false;
		BreakpointsView view= (BreakpointsView) p.findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		if (view != null)
		{
			view.getViewer().setSelection(view.getViewer().getSelection());
			return true;
		}
		return false;

	}
	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if(!breakpointActionsForced)
		{
			breakpointActionsForced=activateBPViewActions();
		}

		if (!debugViewMenuListenerAdded) {
			if (part instanceof DebugView) {
				DebugView dv = (DebugView) part;
				DebugViewMenuListener dmListener = new DebugViewMenuListener(dv);
				IMenuManager mm = dv.getViewSite().getActionBars().getMenuManager();
				mm.addMenuListener(dmListener);

				// Look for the PreferredView group that was added in
				// our plugin.xml for the DebugView
				IContributionItem preferred = mm.find("PreferredView");
				if (preferred != null)
					mm.insertBefore(preferred.getId(), new Separator("ViewSwitching"));
				else
					mm.add(new Separator("ViewSwitching"));

				debugViewMenuListenerAdded = true;
			}
		}

		// Question: If DebugView is closed and reopened, how do we
		// get the menu listener added again?

		//check if listener still needed
		if(breakpointActionsForced && debugViewMenuListenerAdded)
				DebugUIPlugin.getDefault().removeSelectionListener(this);

	}

	/**
	 * Launch a PICL UI Daemon.  The daemon listens on the port specified
	 * in the PICL Debug preferences (default is 8001) for an engine
	 * connection.  A key is generated for the startup info so it can
	 * be retrieved later using the key.
	 * @param startupInfo The startup info to associate with this launch
	 * @returns A PICLDaemonInfo object which contains the key and the
	 *         port the daemon is listening on.
	 */
	public static PICLDaemonInfo launchDaemon(PICLStartupInfo startupInfo) {
		if(!launchDaemon(startupInfo.getLauncher(), startupInfo.getResource()))
			return null;
		return generateDaemonInfo(startupInfo);
	}

	/**
	 * Launch a debug session using the given startup info and the
	 * connection key.
	 * @param startupInfo The info used to start debugging.  May be
	 *                    null to indicate that the launch was cancelled
	 *	              and the debug engine should be released.
	 * @param connectionKey Key for retrieving the engine connection info.
	 *		        Must be the same key that was passed to
	 *                      either the wizard or launcher.  If connectionKey
         *                      is not valid, launchDebugSession does nothing.
	 */
	public static void launchDebugSession(PICLStartupInfo startupInfo, Object connectionKey) {
		if(connectionKey instanceof ConnectionInfo) {
			PICLDebugUIDaemon.launchDebugSession(startupInfo, (ConnectionInfo)connectionKey);
		}
	}

        /**
         * Terminate the debug engine.  This is used when the engine was
         * started by the user but hit cancel in the startup wizard.
         * @param connectionKey Key for retrieving the engine connection info.
	 *		        Must be the same key that was passed to
	 *                      either the wizard or launcher.  If connectionKey
         *                      is not valid, terminateEngine does nothing.
	 */
	public static void terminateEngine(Object connectionKey) {
		if(connectionKey instanceof ConnectionInfo) {
			PICLDebugUIDaemon.terminateEngine((ConnectionInfo)connectionKey);
		}
	}

	/**
	 * Launch a PICL UI Daemon.  This is for launching the daemon
	 * independant of an engine launch.  This is for PICL Debug Plugin
	 * internal use only.
	 */
	public static boolean launchDaemon(ILauncher launcher, Object resource) {
		if(daemon == null || daemon.isTerminated()) {
	         	daemon = new PICLDebugUIDaemon(launcher, resource);
		 	if(daemon.isTerminated()) {
				displayError(PLUGIN+".errorTitle", PLUGIN+".daemonLaunchError");
				return false;
			}
			if(!(resource instanceof IResource)) {
				IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				if(projects.length > 0)
					resource = projects[0];
			}
			ILaunch launch = new Launch(launcher, ILaunchManager.DEBUG_MODE, resource, null, null, daemon);
	        	DebugPlugin.getDefault().getLaunchManager().registerLaunch(launch);
		}
		return true;
	}

	protected static synchronized PICLDaemonInfo generateDaemonInfo(PICLStartupInfo startupInfo) {
		int key;
		if(freeInfoKeyList.size() == 0) {
			key = infoList.size();
			infoList.addElement(startupInfo);
		}
		else {
			key = ((Integer)freeInfoKeyList.elementAt(0)).intValue();
			infoList.setElementAt(startupInfo, key);
			freeInfoKeyList.removeElementAt(0);
		}

		PICLDaemonInfo daemonInfo = new PICLDaemonInfo(key, daemon.getPort());
		return daemonInfo;
	}

	/**
	 * Given a key, retrieve the startup info associated with it or
	 * null if not found.
	 * @returns The startup info associated with the key or null if not found.
	 */
	public static synchronized PICLStartupInfo retrieveStartupInfo(int key) {
		PICLStartupInfo startupInfo = null;

		if(key >= 0 && key < infoList.size()) {
			startupInfo = (PICLStartupInfo)infoList.elementAt(key);
			infoList.setElementAt(null, key);
			freeInfoKeyList.addElement(new Integer(key));
		}

		return startupInfo;
	}

	/**
	 * Returns whether the given launcher should be visible in the UI.
	 * If a launcher is not visible, it will not appear
	 * in the UI - i.e. not as a default launcher, not in the run/debug
	 * drop downs, and not in the launch history.
	 * Based on the public attribute.
	 */
	public boolean isVisible(ILauncher launcher) {
		IConfigurationElement e = launcher.getConfigurationElement();
		String publc=  e.getAttribute("public");
		if (publc == null || publc.equals("true")) {
			return true;
		}
		return false;
	}


	/**
 	* Debug ui thread safe access to a shell
 	*/
	public Shell getShell() {
		IWorkbench workbench= getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
			Display display= null;
			if (windows != null && windows.length > 0) {
				Shell shell= windows[0].getShell();
				if (!shell.isDisposed()) {
					return shell;
				}
			}
		}
		return null;
	}

	/**
 	* Debug ui thread safe access to a display
 	*/
	public Display getDisplay() {
		Shell shell = getShell();
		if (shell != null)
			return shell.getDisplay();

		return null;
	}



	/**
	 * Returns whether the given launcher specifies a wizard.
	 */
	public boolean hasWizard(ILauncher launcher) {
		IConfigurationElement e = launcher.getConfigurationElement();
		return e.getAttribute("wizard") != null;
	}

	/**
 	* Creates an extension.  If the extension plugin has not
 	* been loaded a busy cursor will be activated during the duration of
 	* the load.
 	*
 	* @param element the config element defining the extension
 	* @param classAttribute the name of the attribute carrying the class
 	* @returns the extension object
 	*/
	public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		if (plugin.isPluginActivated()) {
			return element.createExecutableExtension(classAttribute);
		} else {
			final Object [] ret = new Object[1];
			final CoreException [] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					try {
						ret[0] = element.createExecutableExtension(classAttribute);
					} catch (CoreException e) {
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null) {
				throw exc[0];
			}
			else {
				return ret[0];
			}
		}
	}

	protected static void displayError(String titleCode, String msgCode) {
		String title = PICLUtils.getResourceString(titleCode);
		String msg = PICLUtils.getResourceString(msgCode);
		MessageDialog.openError(PICLDebugPlugin.getActiveWorkbenchShell(), title, msg);
	}

	/**
	 * Returns the document provider used for the debugger's editor
	 */
	public DebuggerDocumentProvider getDebuggerEditorDocumentProvider() {
		if (editorDocumentProvider == null)
			editorDocumentProvider = new DebuggerDocumentProvider();
		return editorDocumentProvider;
	}
}
