package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2000, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/GdbView.java, eclipse, eclipse-dev, 20011129
// Version 1.4 (last modified 11/29/01 14:16:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.net.URL;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;

import org.eclipse.debug.internal.ui.*;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.ibm.debug.internal.util.EclipseLogger;
import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLStackFrame;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.DebugEngineEventListener;
import com.ibm.debug.model.*;

/**
 * View for Gdb Engine
 */
public class GdbView
	extends ViewPart
	implements IMenuListener, IDebugEventListener, ILaunchListener {

	private ResourceBundle _messagesBundle;
	private ILog log;
	private EclipseLogger LOG = PICLDebugPlugin.LOG;
	private TextViewer responses;
	private StyledText commands;
	private Label label;
	private String lastLabel = "";
	private Composite parent;
	private Control ctrl = null;
	private Display display = null;
	private CopyAction copyAction;
	private DeleteAction deleteAction;
	private final static int MAX_TEXT = 5000;
	private final static int AUTOCUT_TEXT = MAX_TEXT / 5;
	private String lastCommand = "";

	//	protected final static String PREFIX= "gdbView.";
	protected DebugEngine debugEngine = null;
	protected IDebugTarget iDebugTarget = null;
	protected String debuggeeName = "";

	/**
	 * GdbView constructor
	 */
	public GdbView() {
		super();
	}
	/**
	 * Returns the title tooltip for the View icon of this view part.
	 */
	protected final static String TITLE_TOOLTIPTEXT = "title_toolTipText";
	protected String getTitleToolTipText(String prefix) {
		return getResourceString("GdbView.titleTooltip");
	}

	/**
	 * Creates the Gdb Console View
	 */
	public void createPartControl(Composite _parent) {

		//   init_Initialization();

		LOG = PICLDebugPlugin.LOG;
		if (LOG.EVT)
			LOG.evt(1, "GdbView created");
		//if(EclipseLogger.EVT)
		//   EclipseLogger.STATIC.evt(1,"createPartControl EclipseLogger.EVT="+EclipseLogger.EVT+" EclipseLogger.evt="+EclipseLogger.STATIC.getEventLevel() );

		initializeMessagesOnce();

		parent = _parent;

		GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 2;
		parent.setLayout(parentLayout);

		// ################ command responses ########################
		responses = new TextViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		responses.getTextWidget().setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData responsesGrid = new GridData(GridData.FILL_BOTH);
		responsesGrid.horizontalSpan = 2;
		responses.getTextWidget().setLayoutData(responsesGrid);
		responses.setDocument(new Document(""));
		responses.setEditable(false);
		responses.getTextWidget().setTextLimit(MAX_TEXT);

		ctrl = responses.getControl();
		if (ctrl != null)
			display = ctrl.getDisplay();
		responses.getTextWidget().setFont(new Font(display, "Courier", 10, SWT.NORMAL));

		// ################ command label ########################
		label = new Label(parent, SWT.SINGLE);
		GridData labelGrid =
			new GridData(GridData.VERTICAL_ALIGN_END | GridData.HORIZONTAL_ALIGN_BEGINNING);
		label.setLayoutData(labelGrid);
		label.setText("");

		// ################ command entry-field ########################
		commands = new StyledText(parent, SWT.SINGLE | SWT.BORDER);
		GridData commandsGrid =
			new GridData(GridData.VERTICAL_ALIGN_END | GridData.HORIZONTAL_ALIGN_FILL);
		commandsGrid.grabExcessHorizontalSpace = true;
		commands.setLayoutData(commandsGrid);
		commands.setText("");

		commands.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event e) {
				if (e.character == '\r') // "enter" key
					{
					lastCommand = commands.getText();
					performCommand(commands.getText());
					commands.setText("");
				} else
					if (e.keyCode == 16777217) // "up" arrow
						{
						commands.setText(lastCommand);
						commands.setCaretOffset(commands.getCharCount());
					} else
						if (e.keyCode == 16777218) // "down" arrow
							{
							commands.setText("");
						}
			}
		});
		commands.setFont(new Font(display, "Courier", 10, SWT.NORMAL));

		// ################ menus ########################
		MenuManager menuMgr = new MenuManager("#BaseActionPopUp");
		Menu menu = menuMgr.createContextMenu(responses.getControl());
		menuMgr.addMenuListener(this);
		menuMgr.setRemoveAllWhenShown(true);
		responses.getControl().setMenu(menu);
		makeActions();

		fillLocalToolBar();

		// ################ ADD DebugEvent+Launch Listeners ########################

		boolean initialized = false;

		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null) {
			if (LOG.DBG)
				LOG.dbg(1, "DebugPlugin ADDING_I_DEBUG_EVENT_LISTENER");
			debugPlugin.addDebugEventListener((IDebugEventListener) this);

			ILaunchManager launchManager = debugPlugin.getLaunchManager();
			if (launchManager != null) {
				if (LOG.DBG)
					LOG.dbg(1, "ILaunchManager ADDING_LAUNCH_LISTENER");
				launchManager.addLaunchListener((ILaunchListener) this);

				// Search for a picldebug target
				IDebugTarget[] targets = launchManager.getDebugTargets();
				PICLDebugTarget pt = null;
				for (int i=0; i < targets.length; i++) {
					if (targets[i] instanceof PICLDebugTarget) {
						pt = (PICLDebugTarget)targets[i];
						if (pt.isInitialized()) {
							initialize(pt);
							initialized = true;
							break;
						}
					}
				}
			}
		}

		if (initialized) {
			addItem(GDBPICL_ACTIVE);
			setLabel(GDBPICL_ACTIVE);
		} else {
			addItem(GDBPICL_INACTIVE);
			setLabel(GDBPICL_INACTIVE);
		}
		debuggeeName = "";
	}

	public void dispose() {
		super.dispose();
		if (LOG.EVT)
			LOG.evt(1, "GdbView disposed");
		debugEngine = null;
		iDebugTarget = null;

		// ################ REMOVED DebugEvent+Launch Listeners ########################
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.removeDebugEventListener((IDebugEventListener) this);
			ILaunchManager launchManager = debugPlugin.getLaunchManager();
			if (launchManager != null)
				launchManager.removeLaunchListener((ILaunchListener) this);
		}

		label.dispose();
		label = null;
		commands.dispose();
		commands = null;
		if (responses.getControl() != null)
			responses.getControl().dispose();
		responses = null;
	}
	public void fillContextMenu(IMenuManager menu) {
		menu.add(copyAction);
		menu.add(deleteAction);
		//	menu.add(new Separator());
		//	menu.add(showAllAction);
	}
	public void fillLocalToolBar() {
		IToolBarManager toolBarManager =
			getViewSite().getActionBars().getToolBarManager();
		//	showAllAction.setEnabled(true);
		//	showAllAction.setChecked(true); //start out showing all
		//	showAllAction.setImageDescriptor(GdbViewPlugin.getDefault().getImageDescriptor("newreadme_wiz.gif"));
		//	showAllAction.setText("Show All");
		//	showAllAction.setToolTipText("Show All Items");
		//	toolBarManager.add(showAllAction);
	}
	private void makeActions() {
		// add item
		copyAction = new CopyAction(this, "copyItem");
//		copyAction.setImageDescriptor(GdbViewPlugin.getDefault().getImageDescriptor("newreadme_wiz.gif"));
		copyAction.setText("&Copy Selection");
		copyAction.setToolTipText("Copy Selection");
		// delete item
		deleteAction = new DeleteAction(this, "deleteItem");
//		deleteAction.setImageDescriptor(GdbViewPlugin.getDefault().getImageDescriptor("newreadme_wiz.gif"));
		deleteAction.setText("&Delete Selection");
		deleteAction.setToolTipText("Delete Selection");
	}
	/**
	 * The context menu is about to appear.  Populate it.
	 */
	public void menuAboutToShow(IMenuManager mgr) {
		fillContextMenu(mgr);
	}
	public void setFocus() {
		commands.setFocus();
	}

	/**
	 * @see IDebugEventListener
	 */
	public void handleDebugEvent(final DebugEvent event) {
		if (LOG.DBG)
			LOG.dbg(
				3,
				"handleDebugEvent Source="
					+ event.getSource().getClass()
					+ " Kind="
					+ event.getKind()
					+ " Detail="
					+ event.getDetail());
		Object source = event.getSource();

		initialize(source);

		doHandleDebugEvent(event);
		return;
	}

	/**
	 * Initialize
	 */
	
	private void initialize(Object source) {
		
		if (debugEngine == null && source instanceof PICLDebugTarget) {
			PICLDebugTarget tgt = (PICLDebugTarget)source;
			DebugEngine newDebugEngine = tgt.getDebugEngine();
			if (debugEngine == null && newDebugEngine != null) {
				if (tgt.isInitialized() && !(tgt.isTerminated() || tgt.isDisconnected())) {

					EngineCapabilities capabilities = newDebugEngine.getCapabilities();
					boolean cmdLog = capabilities.getWindowCapabilities().commandLogSupported();
					if (cmdLog) {
						if (debugEngine != null) {
							if (LOG.ERR)
								LOG.err(1, GDBPICL_IS_OVERWRITING_EXISTING_DEBUGENGINE);
						}
						debugEngine = newDebugEngine;
						debugEngine.addEventListener(new GdbEngineEventListener());
						debuggeeName = "";
						String targetLabel = tgt.getLabel(false);
						int quote = targetLabel.indexOf("\"");
						if (quote > 0)
							debuggeeName = targetLabel.substring(quote);

					} else {
						;
					} // non-GdbPicl
				}
			} else if (
				debugEngine != null
					&& debugEngine == newDebugEngine
					&& (tgt.isTerminated()
						|| tgt.isDisconnected())) {
				if (LOG.ERR)
					LOG.err(2, GDBPICL_IS_TERMINATED_OR_DISCONNECTED);
			}
		}
	}

	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	public void doHandleDebugEvent(DebugEvent event) {
		Object element = event.getSource();
		if (element instanceof PICLStackFrame) {
			return; // the GdbView does not care about StackFrames
		}
		if (element instanceof IDebugElement
			&& ((IDebugElement) element).getElementType() == IDebugElement.VARIABLE) {
			return; // the GdbView does not care about variables
		}
		switch (event.getKind()) {
			case DebugEvent.CREATE :
				if (element instanceof IDebugTarget) {
					if (iDebugTarget == null && debugEngine != null) {
						if (LOG.EVT)
							LOG.evt(1, "CREATE_TARGET=" + element);
						iDebugTarget = (IDebugTarget) element;
						addItem(GDBPICL_ACTIVE + " (" + debuggeeName + ")");
						setLabel(GDBPICL_COMMAND);
					}
				} else
					if (element instanceof IProcess) {
						if (LOG.EVT)
							LOG.evt(2, "CREATE_PROCESS");
					} else
						if (element instanceof IThread) {
							if (LOG.EVT)
								LOG.evt(2, "CREATE_THREAD");
						} else {
							if (LOG.DBG)
								LOG.dbg(1, "CREATE " + element.getClass() + " detail=" + event.getDetail());
						}
				break;
			case DebugEvent.TERMINATE :
				if (element instanceof IDebugTarget) {
					if (iDebugTarget != null && (iDebugTarget == (IDebugTarget) element));
					{
						if (LOG.EVT)
							LOG.evt(1, "GdbView CURRENT IDebugTarget TERMINATED");
						debugEngine = null;
						iDebugTarget = null;
						addItem("\rGdbPicl inactive...");
						setLabel("GdbPicl inactive... ");
						debuggeeName = "";
					}
				} else
					if (element instanceof IProcess) {
						if (LOG.EVT)
							LOG.evt(2, "TERMINATE_PROCESS");
					} else
						if (element instanceof IThread) {
							if (LOG.EVT)
								LOG.evt(2, "TERMINATE_THREAD");
						} else {
							if (LOG.DBG)
								LOG.dbg(1, "TERMINATE " + element.getClass() + " detail=" + event.getDetail());
						}
				break;
			case DebugEvent.RESUME :
				if (LOG.DBG)
					LOG.dbg(3, "RESUME " + element.getClass() + " detail=" + event.getDetail());
				break;
			case DebugEvent.SUSPEND :
				if (LOG.DBG)
					LOG.dbg(3, "SUSPEND " + element.getClass() + " detail=" + event.getDetail());
				break;
			case DebugEvent.CHANGE :
				if (LOG.DBG)
					LOG.dbg(3, "CHANGE " + element.getClass() + " detail=" + event.getDetail());
				break;
			default :
				if (LOG.DBG)
					LOG.dbg(1, "UNKNOWN " + element.getClass() + " detail=" + event.getDetail());
				break;
		}
	}

	/**
	 * @see ILaunchListener
	 */
	public void launchRegistered(final ILaunch launch) {
		IDebugTarget debugTarget = launch.getDebugTarget();
		if (LOG.DBG)
			LOG.dbg(1, "launchRegistered IDebugTarget=" + debugTarget);
	}
	/**
	 * @see ILaunchListener
	 */
	public void launchDeregistered(final ILaunch launch) {
		IDebugTarget debugTarget = launch.getDebugTarget();
		if (iDebugTarget != null && iDebugTarget == debugTarget) {
			if (LOG.EVT)
				LOG.evt(1, "launch_DE_Registered of CURRENT GdbPicl, will 'quit' it");
			if (debugEngine != null) {
				performCommand("quit");
			}
		}
	}

	/**
	 * @see DebugEngineEventListener
	 */
	public class GdbEngineEventListener implements DebugEngineEventListener {
		public GdbEngineEventListener() {
		}
		public void commandLogResponse(DebugEngineCommandLogResponseEvent event) {
			String[] responseLines = event.getResponseLines();
			if (responseLines == null || responseLines.length == 0) {
				//addItem("");
			} else {
				for (int i = 0; i < responseLines.length; i++) {
					if (responseLines.length == 1 && responseLines[0].equals(""))
						break;
					addItem(responseLines[i]);
				}
			}
		}
		public void debugEngineTerminated(DebugEngineTerminatedEvent event) {
			if (LOG.EVT)
				LOG.evt(1, "GdbEngineEventListener debugEngineTerminated");
			debugEngine = null;
			iDebugTarget = null;
			addItem("\r" + GDBPICL_INACTIVE);
			setLabel(GDBPICL_INACTIVE);
			debuggeeName = "";
		}
		public void errorOccurred(ErrorOccurredEvent event) {
			if (LOG.ERR)
				LOG.err(1, GDBPICL_SENT_ERROR + event.getMessage());
			addItem("\r" + GDBPICL_SENT_ERROR + event.getMessage());
		}
		public void messageReceived(MessageReceivedEvent event) {
			if (LOG.ERR)
				LOG.err(2, GDBPICL_SENT_MESSAGE + event.getMessage());
			addItem("\r" + GDBPICL_SENT_MESSAGE + event.getMessage());
		}
		public void engineCapabilitiesChanged(EngineCapabilitiesChangedEvent event) {
			;
		}
		public void modelStateChanged(ModelStateChangedEvent event) {
			;
		}
		public void processAdded(ProcessAddedEvent event) {
			//      System.out.println("#### GdbView.GdbEngineEventListener.processAdded" );
			//      DebuggeeProcess process = event.getProcess();
			//      process.addEventListener(new GdbProcessEventListener());
		}
	}

	private void performCommand(String cmd) {
		if (debugEngine == null) {
			addItem(GDBPICL_INACTIVE_REJECTED_COMMAND + " " + cmd);
			return;
		}
		try {
			addItem("\r" + COMMAND + " " + cmd);
			if (LOG.DBG)
				LOG.dbg(1, "COMMAND: " + cmd);
			debugEngine.commandLogExecute(cmd, DebugEngine.sendReceiveSynchronously);
		} catch (java.io.IOException exc) {
			if (LOG.ERR)
				LOG.err(1, "GDBPICL_COMMAND_IO_EXCEPTION=" + exc, exc);
			debugEngine = null;
			iDebugTarget = null;
			addItem("\r" + GDBPICL_INACTIVE);
			setLabel(GDBPICL_INACTIVE);
			debuggeeName = "";
		}
	}
	private void addItem(final String str) {
		display.asyncExec(new Runnable() {
			public void run() {
				if (responses == null || responses.getTextWidget() == null) {
					return;
				}
				StyledText textArea = responses.getTextWidget();
				int newChars = str.length() + 2;
				int max = textArea.getTextLimit();
				int current = textArea.getCharCount();
				if ((current + newChars) >= max) {
					int endChar = AUTOCUT_TEXT + 200;
					if (endChar >= current)
						endChar = current - 1;
					String s = textArea.getText(AUTOCUT_TEXT, endChar);
					int endLine = AUTOCUT_TEXT;
					int length = s.length();
					for (int i = 0; i < length; i++) {
						if (s.charAt(i) == '\r') {
							endLine = endLine + i + 1;
							break;
						}
					}

					textArea.setSelection(0, endLine);
					deleteSelection();
				}

				textArea.append("\r" + str);
				textArea.setSelection(textArea.getCharCount());
				textArea.setHorizontalIndex(0);
				String msg = str;
				if (msg.startsWith("\r"))
					msg = msg.substring(1);
				if (msg.startsWith(GDBPICL_COMMAND))
					return;
				if (msg.endsWith(lastLabel))
					return;
				if (LOG.DBG)
					LOG.dbg(1, "GdbPicl response=" + msg);
			}
		});
	}
	private void setLabel(final String str) {
		if (LOG.DBG)
			LOG.dbg(1, "CommandLabel=" + str);
		lastLabel = str;
		display.asyncExec(new Runnable() {
			public void run() {
				if (label != null && !label.isDisposed())
					label.setText(str);
				if (parent != null && !parent.isDisposed())
					parent.layout();
			}
		});
	}

	public void deleteSelection() {
		StyledText textArea = responses.getTextWidget();
		textArea.setEditable(true);
		responses.doOperation(org.eclipse.jface.text.ITextOperationTarget.DELETE);
		//    textArea.cut();   // cut also copies to clipboard
		textArea.setEditable(false);
	}
	public void copySelection() {
		StyledText textArea = responses.getTextWidget();
		textArea.setEditable(true);
		textArea.copy();
		textArea.setEditable(false);
	}

	// ***************************************************************************
	// Internationalization Initialization
	// ***************************************************************************
	private void init_Initialization() {
		// ***************************************************************************
		// Try to use the system's default locale, if we fail we will use en_US
		// ***************************************************************************
		if (!setMessageLocale(Locale.getDefault())) {
			// ***************************************************************************
			// We do not support the specified locale, we will try "en_US" instead
			// ***************************************************************************
			if (setMessageLocale(new Locale("en", "US", ""))) {
				if (LOG.ERR)
					LOG.err(1, getResourceString("GdbView.defaultLocaleNotSupported"));
			} else {
				if (LOG.ERR)
					LOG.STATIC.err(1, "GdbViewMessages_en.properties resource file missing.");
			}
		}
	}

	// ***************************************************************************
	// Attempt to set the locale for messages.
	// ***************************************************************************
	private boolean setMessageLocale(Locale locale) {
		try {
			_messagesBundle =
				ResourceBundle.getBundle("com.ibm.debug.internal.gdb.GdbViewMessages", locale);
		} catch (MissingResourceException e) {
			return false;
		}

		if (_messagesBundle == null)
			return false;
		else
			return true;
	}

	/**
	 * Get a resource string from the Messages ResourceBundle object
	 */
	public String getResourceString(String key) {
		if (_messagesBundle == null) {
			return key;
		}
		try {
			return _messagesBundle.getString(key);
		} catch (MissingResourceException e) {
			if (LOG.ERR)
				LOG.err(2, "(" + key + ") " + getResourceString("GdbView.missingResourceString"));
			return key + " ";
		}
	}

	private void initializeMessagesOnce() {
		GDBPICL_INACTIVE = PICLUtils.getResourceString(GDBPICL_INACTIVE);
		GDBPICL_ACTIVE = PICLUtils.getResourceString(GDBPICL_ACTIVE);
		GDBPICL_COMMAND = PICLUtils.getResourceString(GDBPICL_COMMAND);
		COMMAND = PICLUtils.getResourceString(COMMAND);
		GDBPICL_INACTIVE_REJECTED_COMMAND =
			PICLUtils.getResourceString(GDBPICL_INACTIVE_REJECTED_COMMAND);
		GDBPICL_COMMAND_IO_EXCEPTION =
			PICLUtils.getResourceString(GDBPICL_COMMAND_IO_EXCEPTION);
		GDBPICL_SENT_ERROR = PICLUtils.getResourceString(GDBPICL_SENT_ERROR);
		GDBPICL_SENT_MESSAGE = PICLUtils.getResourceString(GDBPICL_SENT_MESSAGE);
		GDBPICL_IS_TERMINATED_OR_DISCONNECTED =
			PICLUtils.getResourceString(GDBPICL_IS_TERMINATED_OR_DISCONNECTED);
		GDBPICL_IS_OVERWRITING_EXISTING_DEBUGENGINE =
			PICLUtils.getResourceString(GDBPICL_IS_OVERWRITING_EXISTING_DEBUGENGINE);
	}
	String GDBPICL_INACTIVE = "GdbView.inactive";
	String GDBPICL_ACTIVE = "GdbView.active";
	String GDBPICL_COMMAND = "GdbView.gdbPiclCommand";
	String COMMAND = "GdbView.command";
	String GDBPICL_INACTIVE_REJECTED_COMMAND = "GdbView.inactiveRejected";
	String GDBPICL_COMMAND_IO_EXCEPTION = "GdbView.commandIOException";
	String GDBPICL_SENT_ERROR = "GdbView.sentError";
	String GDBPICL_SENT_MESSAGE = "GdbView.sentMessage";
	String GDBPICL_IS_TERMINATED_OR_DISCONNECTED =
		"GdbView.terminatedOrDisconnected";
	String GDBPICL_IS_OVERWRITING_EXISTING_DEBUGENGINE =
		"GdbView.overwritingExistingEngine";
}