package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/StorageViewTab.java, eclipse, eclipse-dev
// Version 1.16 (last modified 11/28/01 16:00:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import com.ibm.debug.internal.pdt.ui.actions.MonitorExpressionAction;
import com.ibm.debug.internal.pdt.ui.actions.RemoveStorageMonitorAction;
//import com.ibm.debug.internal.pdt.ui.actions.DisableStorageMonitorAction;
import com.ibm.debug.internal.pdt.ui.actions.ResetStorageMonitorAction;
import com.ibm.debug.internal.pdt.ui.actions.CopyTableViewToClipboardAction;
import com.ibm.debug.internal.pdt.ui.actions.PrintTableViewAction;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLStorage;
import com.ibm.debug.internal.picl.PICLUtils;


public class StorageViewTab extends AbstractDebugView implements IDoubleClickListener, SelectionListener, ControlListener {

	protected final static String PREFIX= "StorageViewTab.";
	public static final String[] fColumnPROPERTIES = new String[]{"address","1stByte","2ndByte","3rdByte","4thByte","translated"};
	public int TABLE_PREBUFFER = 20;
	public int TABLE_POSTBUFFER = 20;
	public int TABLE_DEFAULTBUFFER = 20;

	private Composite tabFolderPage;
	private TableViewer fTableViewer = null;
	private CellEditor fEditors[];
	private StorageViewContentProvider contentProvider;

	private MonitorExpressionAction fMonitorExpressionAction;
	private RemoveStorageMonitorAction fRemoveStorageMonitorAction;
//	private DisableStorageMonitorAction fDisableStorageMonitorAction;
	private ResetStorageMonitorAction fResetStorageMonitorAction;
	
	private CopyTableViewToClipboardAction fCopyTableViewToClipboardAction;
	private PrintTableViewAction fPrintTableViewAction;



	public StorageViewTab(PICLStorage newStorage, TabItem newTab) {
		newTab.setData(this);
		contentProvider = new StorageViewContentProvider(newStorage, newTab);
	}

	public void dispose() {
		try {
			if (fTableViewer != null)
				fTableViewer.removeDoubleClickListener(this);
			if (contentProvider != null)
				contentProvider.dispose();
			ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
			scroll.removeSelectionListener(this);
		} catch (Exception e) {}
	}

	public void partClosed(IWorkbenchPart part) {
		if (!(part instanceof StorageViewTab))
			return;
		try {
			if (fTableViewer != null)
				fTableViewer.removeDoubleClickListener(this);
			if (contentProvider != null)
				contentProvider.dispose();
			ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
			scroll.removeSelectionListener(this);
		} catch (Exception e) {}
		super.partClosed(part);
	}

	/**
	 * @see WorkbenchPart#createPartControl(Composite)
	 * not used - see createFolderPage
	 */
	public void createPartControl(Composite arg0) {	}

	public Control createFolderPage(TabFolder tabFolder) {

		tabFolderPage = new Composite (tabFolder, SWT.NULL);
		fTableViewer= new TableViewer(tabFolder, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION | SWT.BORDER);
		fTableViewer.setContentProvider(contentProvider);
		fTableViewer.setLabelProvider(new StorageViewLabelProvider());

		ScrollBar scroll = ((Table)fTableViewer.getControl()).getVerticalBar();
		scroll.addSelectionListener(this);
		scroll.setMinimum(-100);
		scroll.setMaximum(200);
		fTableViewer.addDoubleClickListener(this);
		fTableViewer.getControl().addControlListener(this);
		fTableViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});


		initializeActions(fTableViewer, tabFolder);
		//force selectionChanged() on MonitorExpressionAction in case the user immediately uses the context menu
		IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) { return null; }
		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (view == null) {
			try {
				IWorkbenchPart activePart= p.getActivePart();
				view= (DebugView) p.showView(IDebugUIConstants.ID_DEBUG_VIEW);
				p.activate(activePart);
			} catch (PartInitException e) {}
		}
		fMonitorExpressionAction.selectionChanged(view, view.getViewer().getSelection());


		MenuManager menuMgr= new MenuManager("#PopUp");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Control menuControl = fTableViewer.getControl();
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		fTableViewer.getTable().setHeaderVisible(true);
		fTableViewer.getTable().setLinesVisible(true);
		// set the font to a proportional one here
		fTableViewer.getTable().setFont(new Font(tabFolder.getDisplay(),"Courier New",10,SWT.NORMAL));

		TableColumn column0 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,0);
		TableColumn column1 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,1);
		TableColumn column2 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,2);
		TableColumn column3 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,3);
		TableColumn column4 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,4);
		TableColumn column5 = new TableColumn(fTableViewer.getTable(),SWT.LEFT,5);
		column0.setText(PICLUtils.getResourceString(PREFIX+"address"));
		column1.setText("0 - 3");
		column2.setText("4 - 7");
		column3.setText("8 - B");
		column4.setText("C - F");
		column5.setText(PICLUtils.getResourceString(PREFIX+"text"));

		TableLayout layout = new TableLayout();
		ColumnLayoutData c0Data = new ColumnWeightData(100,80,true);
		ColumnLayoutData c1Data = new ColumnWeightData(100,80,true);
		ColumnLayoutData c2Data = new ColumnWeightData(100,80,true);
		ColumnLayoutData c3Data = new ColumnWeightData(100,80,true);
		ColumnLayoutData c4Data = new ColumnWeightData(100,80,true);
		ColumnLayoutData c5Data = new ColumnWeightData(200,160,true);
		layout.addColumnData(c0Data);
		layout.addColumnData(c1Data);
		layout.addColumnData(c2Data);
		layout.addColumnData(c3Data);
		layout.addColumnData(c4Data);
		layout.addColumnData(c5Data);
		fTableViewer.getTable().setLayout(layout);

		fTableViewer.setCellEditors(getCellEditors());
		fTableViewer.setCellModifier(new StorageViewCellModifier());
		fTableViewer.setColumnProperties(fColumnPROPERTIES);
		fTableViewer.setInput(contentProvider.getStorage());

		return fTableViewer.getControl();
	}

	/**
	 * Initializes the actions of this view
	 */
	private void initializeActions(TableViewer viewer, TabFolder tabFolder) {
		fMonitorExpressionAction = new MonitorExpressionAction(true);
		fMonitorExpressionAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setEnabled(true);

		fRemoveStorageMonitorAction = new RemoveStorageMonitorAction();
		fRemoveStorageMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_STORAGE));
		fRemoveStorageMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_STORAGE));
		fRemoveStorageMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_STORAGE));
		fRemoveStorageMonitorAction.setEnabled(false);

//		fDisableStorageMonitorAction = new DisableStorageMonitorAction(tabFolder);
//		fDisableStorageMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_DISABLE_STORAGE));
//		fDisableStorageMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_DISABLE_STORAGE));
//		fDisableStorageMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_DISABLE_STORAGE));
//		fDisableStorageMonitorAction.setEnabled(true);

		fResetStorageMonitorAction = new ResetStorageMonitorAction();
		fResetStorageMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_STORAGE_RESET));
		fResetStorageMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_STORAGE_RESET));
		fResetStorageMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_STORAGE_RESET));
		fResetStorageMonitorAction.setEnabled(true);

		fCopyTableViewToClipboardAction= new CopyTableViewToClipboardAction(fTableViewer);
		fCopyTableViewToClipboardAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyTableViewToClipboardAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyTableViewToClipboardAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD));
		
		fPrintTableViewAction= new PrintTableViewAction(fTableViewer, PICLUtils.getResourceString(PREFIX+"printjobtitle"));
		fPrintTableViewAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_PRINT_VIEW));
		fPrintTableViewAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_PRINT_VIEW));
		fPrintTableViewAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_PRINT_VIEW));

	}


	private CellEditor[] getCellEditors() {
		Table table = fTableViewer.getTable();
		fEditors = new CellEditor[table.getColumnCount()];
		fEditors[0] = new TextCellEditor(table);
		fEditors[1] = new TextCellEditor(table);
		fEditors[2] = new TextCellEditor(table);
		fEditors[3] = new TextCellEditor(table);
		fEditors[4] = new TextCellEditor(table);
		fEditors[5] = new TextCellEditor(table);

		// combine the listener/validator interfaces so we can handle
		// "editing" an address, which really skips the table to that address
		class CellValidatorListener implements ICellEditorListener, ICellEditorValidator {
			TextCellEditor textEditor;
			boolean isAddressValidator;

			public CellValidatorListener(CellEditor cellEditor, boolean isAddress) {
				textEditor = (TextCellEditor)cellEditor;
				isAddressValidator = isAddress;
			}

			public void editorValueChanged(boolean oldValidState, boolean newValidState) {
				if (!newValidState) {
					MessageDialog.openError(null, PICLUtils.getResourceString(PREFIX+"inputerror"), textEditor.getErrorMessage());
				}
			}

			public void cancelEditor() { }

			public void applyEditorValue() {
				if (isAddressValidator) {
					reloadTable((String)textEditor.getValue(), true);
				}
			}

			public String isValid(Object value) {
				if ((value instanceof String)) {
					// make sure the character is 0-9ABCDEF only
					try {
						Long.parseLong((String)value, 16);
					} catch (NumberFormatException ne) {
						return PICLUtils.getResourceString(PREFIX+"invalidformat");
					}
					if (((String)value).length() > 8) {
						return PICLUtils.getResourceString(PREFIX+"invalidformat");
					}
				}
				return null;
			}

		}

		//"editing" an address skips the table to that address
		fEditors[0].setValidator(new CellValidatorListener(fEditors[0], true));
		fEditors[0].addListener((CellValidatorListener)fEditors[0].getValidator());
		fEditors[1].setValidator(new CellValidatorListener(fEditors[1], false));
		fEditors[1].addListener((CellValidatorListener)fEditors[1].getValidator());
		fEditors[2].setValidator(new CellValidatorListener(fEditors[2], false));
		fEditors[2].addListener((CellValidatorListener)fEditors[2].getValidator());
		fEditors[3].setValidator(new CellValidatorListener(fEditors[3], false));
		fEditors[3].addListener((CellValidatorListener)fEditors[3].getValidator());
		fEditors[4].setValidator(new CellValidatorListener(fEditors[4], false));
		fEditors[4].addListener((CellValidatorListener)fEditors[4].getValidator());
		fEditors[5].addListener(new CellValidatorListener(fEditors[2], false));  // no validation for translated input
		return fEditors;
	}

	/**
	 * @see AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(this.getClass().getName()));
		menu.add(fMonitorExpressionAction);
		menu.add(fRemoveStorageMonitorAction);
//		menu.add(fDisableStorageMonitorAction);
		menu.add(fResetStorageMonitorAction);
		menu.add(new Separator(this.getClass().getName()));
		menu.add(fCopyTableViewToClipboardAction);
		menu.add(fPrintTableViewAction);
		menu.add(new Separator(this.getClass().getName()));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) { }

	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		//System.out.println("double click");
	}

	/**
	 * Handles key events in viewer.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.stateMask != 0) { return; }
		ScrollBar scrollbar = ((Table)fTableViewer.getControl()).getVerticalBar();
		Table table  = (Table)fTableViewer.getControl();

		//remove this functionality - too easy to delete storage monitor when trying to edit storage
		//if (event.character == SWT.DEL) {
		//	contentProvider.getStorage().delete();
		//	return;
		//}

		switch (event.keyCode) {

 			case SWT.HOME:
			case SWT.PAGE_UP:
			case SWT.ARROW_UP:
			//intentional fall-through

 			case SWT.END:
			case SWT.PAGE_DOWN:
			case SWT.ARROW_DOWN:
			//intentional fall-through

				if ((scrollbar.getSelection() <= 3) || (TABLE_PREBUFFER+TABLE_POSTBUFFER - scrollbar.getSelection() <= 3))
					reloadTable(table.getTopIndex(), false);
				break;
		}
	}


	/**
	 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent event) {}
	/**
	 * @see SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {
		Table table = (Table)fTableViewer.getControl();
		ScrollBar scrollbar = (ScrollBar)event.getSource();
		switch (event.detail) {
			case 0:	//the end of a drag
				break;

 			case SWT.END:
			case SWT.PAGE_DOWN:
			case SWT.ARROW_DOWN:
				//intentional fall-through

 			case SWT.HOME:
			case SWT.PAGE_UP:
			case SWT.ARROW_UP:
				//intentional fall-through

  			case SWT.DRAG:
				if ((scrollbar.getSelection() <= 3) || (TABLE_PREBUFFER+TABLE_POSTBUFFER - scrollbar.getSelection() <= 3))
  					reloadTable(table.getTopIndex(), false);
				break;
		}
	}


	public PICLStorage getStorage() {
		return contentProvider.getStorage();
	}

	public StructuredViewer getViewer() {
		return fTableViewer;
	}

	public String getTopVisibleAddress() {
		Table table = fTableViewer.getTable();
		int topIndex = table.getTopIndex();
		if (topIndex < 1) { topIndex = 1; }
		StorageViewLine topItem = (StorageViewLine)table.getItem(topIndex).getData();
		String calculatedAddress = topItem.getAddress();
		if ( Long.parseLong(calculatedAddress, 16) <= 16)
			return "00000000";
		return calculatedAddress;
	}

	public int getNumberOfVisibleLines() {
		Table table = fTableViewer.getTable();
		int numOfVisibleLines = (int)java.lang.Math.ceil(table.getClientArea().height/table.getItemHeight());
		return numOfVisibleLines;
	}


	private void reloadTable(int currentTopIndex, boolean setSelectionToTop) {
		Table table = (Table)fTableViewer.getControl();
		StorageViewLine line = (StorageViewLine)table.getItem(currentTopIndex).getData();
		String calculatedAddress = line.getAddress();
		reloadTable(calculatedAddress, setSelectionToTop);
	}

	public void reloadTable(String calculatedAddress, boolean setSelectionToTop) {
		Table table = (Table)fTableViewer.getControl();

		//handle '0x' address strings
		if (calculatedAddress.toUpperCase().startsWith("0X")) {
			calculatedAddress = calculatedAddress.substring(2);
		}
		if ( Long.parseLong(calculatedAddress, 16) <= 32) {
			TABLE_PREBUFFER = 0;
		} else {
			TABLE_PREBUFFER = (int)java.lang.Math.min(Long.parseLong(calculatedAddress, 16)/32, (long)TABLE_DEFAULTBUFFER);
		}

		calculatedAddress = Long.toHexString(Long.parseLong(calculatedAddress, 16) - 16*TABLE_PREBUFFER);

		if (calculatedAddress.length() < 8) {
			for (int j=0; calculatedAddress.length()<8; j++) {
				calculatedAddress = "0" + calculatedAddress;
			}
		}
		calculatedAddress = calculatedAddress.toUpperCase();
		contentProvider.getStorageToFitTable(calculatedAddress, getNumberOfVisibleLines()+TABLE_PREBUFFER+TABLE_POSTBUFFER);
		contentProvider.forceRefresh();
		table.setTopIndex(TABLE_PREBUFFER);
		if (setSelectionToTop) {
			table.setFocus();
			table.setSelection(TABLE_PREBUFFER); //this does not change the focus of the selected item in eclipse 1.0, but s/b fixed in 2.0
		} else {
			table.setFocus();
			table.deselectAll(); //this does not change the focus of the selected item in eclipse 1.0, but s/b fixed in 2.0
		}
	}

	public void resizeTable() {
		//if new window size exceeds the number of lines available in the table, reload the table
		Table table = fTableViewer.getTable();
		int topIndex = table.getTopIndex();
		if (topIndex < 0) { return; }

		if ( Long.parseLong(getTopVisibleAddress(), 16) <= 32) {
			TABLE_PREBUFFER = 0;
		} else {
			TABLE_PREBUFFER = (int)java.lang.Math.min(Long.parseLong(getTopVisibleAddress(), 16)/32, (long)TABLE_DEFAULTBUFFER);
		}

		int linesLeftInTable = table.getItemCount() - table.indexOf(table.getItem(topIndex));
		if (getNumberOfVisibleLines() >= linesLeftInTable-TABLE_POSTBUFFER) {
			reloadTable(topIndex, false);
		}
	}

	/**
	 * @see ControlListener#controlMoved(ControlEvent)
	 */
	public void controlMoved(ControlEvent event) { }
	/**
	 * @see ControlListener#controlResized(ControlEvent)
	 */
	public void controlResized(ControlEvent event) {
		//this method gets called many times as the user drags the window to a new size
		//TODO: only refresh the data at the end of the resize, if possible
		resizeTable();
	}
}

