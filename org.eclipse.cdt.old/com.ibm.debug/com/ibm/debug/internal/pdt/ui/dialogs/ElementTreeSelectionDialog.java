package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/ElementTreeSelectionDialog.java, eclipse, eclipse-dev, 20011129
// Version 1.4 (last modified 11/29/01 14:15:54)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ISelectionValidator;
import com.ibm.debug.internal.pdt.ui.util.SelectionUtil;
import com.ibm.debug.internal.pdt.ui.util.StatusInfo;
import com.ibm.debug.internal.picl.PICLUtils;

public class ElementTreeSelectionDialog extends SelectionStatusDialog {

	private static final String TREE_EMPTY= "ElementTreeSelectionDialog.error.treeempty";

	private String fEmptyListMessage;

	private TreeViewer fViewer;
	private ILabelProvider fLabelProvider;
	private ITreeContentProvider fContentProvider;
	private boolean fAllowMultiple;
	private boolean fDoubleClickSelects;
	private ISelectionValidator fValidator;

	private int fInitialCharWidth= 40;
	private int fInitialCharHeight= 18;

	private StatusInfo fCurrStatus;

	private ViewerSorter fSorter;
	private List fFilters;

	private Object fInput;

	private boolean fIsEmpty;

	/**
	 * Constructor for the ElementTreeSelectionDialog.
	 * @param labelProvider The label provider to render the entries
	 * @param contentProvider The content provider to evaluate the tree structure
	 */
	public ElementTreeSelectionDialog(Shell parent, String title, Image image, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
		this(parent, title, image, labelProvider, contentProvider, true, true);
	}
	/**
	 * Constructor for the ElementTreeSelectionDialog.
	 * @param labelProvider The label provider to render the entries
	 * @param contentProvider The content provider to evaluate the tree structure
	 * @param allowMultiple Specify the selection behaviour of the tree widget. Allows multiple selection or not
	 */
	public ElementTreeSelectionDialog(Shell parent, String title, Image image, ILabelProvider labelProvider, ITreeContentProvider contentProvider, boolean allowMultiple, boolean doubleClickSelects) {
		super(parent);
		setTitle(title);
		setImage(image);
		fLabelProvider= labelProvider;
		fContentProvider= contentProvider;
		fAllowMultiple= allowMultiple;
		fDoubleClickSelects= doubleClickSelects;

		fValidator= null;
		setResult(new ArrayList(0));
		fCurrStatus= new StatusInfo();
		fEmptyListMessage= PICLUtils.getResourceString(TREE_EMPTY);
		setStatusLineAboveButtons(true);
	}
	/**
	 * Constructor for the ElementTreeSelectionDialog.
	 * @param labelProvider The label provider to render the entries
	 * @param contentProvider The content provider to evaluate the tree structure
	 */
	public ElementTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
		this(parent, "", null, labelProvider, contentProvider, true, true);
	}
	/**
	 * Constructor for the ElementTreeSelectionDialog.
	 * @param labelProvider The label provider to render the entries
	 * @param contentProvider The content provider to evaluate the tree structure
	 * @param allowMultiple Specify the selection behaviour of the tree widget. Allows multiple selection or not
	 */
	public ElementTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider, boolean allowMultiple, boolean doubleClickSelects) {
		this(parent, "", null, labelProvider, contentProvider, allowMultiple, doubleClickSelects);
	}

	protected void access$setResult(List result) {
		super.setResult(result);
	}
	protected void access$superButtonPressed(int id) {
		super.buttonPressed(id);
	}
	/* workaround for VA-Java */
	private void access$superOpen() {
		super.open();
	}

	/**
	 * Adds the given filter to the tree viewer.
	 */
	public void addFilter(ViewerFilter filter) {
		if (fFilters == null) {
			fFilters= new ArrayList(4);
		}
		fFilters.add(filter);
	}
	/*
	 * @private
	 */
	protected void cancelPressed() {
		setResult(null);
		super.cancelPressed();
	}
	/*
	 * @private
	 */
	protected void computeResult() {
		setResult(SelectionUtil.toList(fViewer.getSelection()));
	}
	/*
	 * @private
	 */
	public void create() {
		super.create();
		List initialSelections= getInitialSelections();
		if (initialSelections != null) {
			fViewer.setSelection(new StructuredSelection(initialSelections), true);
		}
		updateOKStatus();
	}
	/*
	 * @private
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite)super.createDialogArea(parent);

		Label messageLabel= createMessageArea(composite);

		Control treeWidget= createTreeViewer(composite);

		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(fInitialCharWidth);
		gd.heightHint= convertHeightInCharsToPixels(fInitialCharHeight);
		treeWidget.setLayoutData(gd);

		if (fIsEmpty) {
			messageLabel.setEnabled(false);
			treeWidget.setEnabled(false);
		}

		return composite;
	}
	private Tree createTreeViewer(Composite parent) {
		int selectionBehaviour= fAllowMultiple ? SWT.MULTI : SWT.SINGLE;
		fViewer= new TreeViewer(new Tree(parent, selectionBehaviour | SWT.BORDER));
		fViewer.setContentProvider(fContentProvider);
		fViewer.setLabelProvider(fLabelProvider);
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				access$setResult(SelectionUtil.toList(event.getSelection()));
				updateOKStatus();
			}
		});
		fViewer.setSorter(fSorter);
		if (fFilters != null) {
			for (int i= 0; i < fFilters.size(); i++) {
				fViewer.addFilter((ViewerFilter)fFilters.get(i));
			}
		}

		if (fDoubleClickSelects) {
			Tree tree= fViewer.getTree();
			tree.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent e) {
					updateOKStatus();
					if (fCurrStatus.isOK()) {
						access$superButtonPressed(IDialogConstants.OK_ID);
					}
				}
			});
		}

		fViewer.setInput(fInput);

		return fViewer.getTree();
	}
	private boolean evaluateIfTreeEmpty(Object input) {
		Object[] elements= fContentProvider.getElements(input);
		if (elements.length > 0) {
			if (fFilters != null) {
				for (int i= 0; i < fFilters.size(); i++) {
					ViewerFilter curr= (ViewerFilter)fFilters.get(i);
					elements= curr.filter(fViewer, input, elements);
				}
			}
		}
		return elements.length == 0;
	}
	/**
	 * Returns the selected element. If no element is selected or more
	 * than one element is selected then <code>null</code> is returned.
	 */
	public Object getSelectedElement() {
		Object[] result= getResult();
		if (result != null && result.length > 0) {
			return result[0];
		}
		return null;
	}
	/**
	 * @deprecated Use SelectionDialog.getResult() instead.
	 */
	public Object[] getSelectedElements() {
		return getResult();
	}
	/*
	 * @private
	 */
	public int open() {
		fIsEmpty= evaluateIfTreeEmpty(fInput);
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				access$superOpen();
			}
		});

		return getReturnCode();
	}
	/**
	 * Opens the dialog on the given input element.
	 */
	public int open(Object input) {
		return open(input, null);
	}
	/**
	 * Opens the dialog on the given input element and the given
	 * initial selection.
	 */
	public int open(Object input, Object selected) {
		setInitialSelection(selected);
		setInput(input);
		return open();
	}
	/**
	 * This message is shown when the tree has no entries at all
	 * Must be set before widget creation
	 */
	public void setEmptyListMessage(String message) {
		fEmptyListMessage= message;
	}
	public void setInitialSizeInCharacters(int width, int height) {
		fInitialCharWidth= width;
		fInitialCharHeight= height;
	}
	/**
	 * Sets the dialog's input to the given value.
	 * @param input the dialog's input.
	 */
	public void setInput(Object input) {
		fInput= input;
	}
	/**
	 * Sets the sorter used by the tree viewer.
	 */
	public void setSorter(ViewerSorter s) {
		fSorter= s;
	}
	/**
	 * A validator can be set to check if the current selection
	 * is valid
	 */
	public void setValidator(ISelectionValidator validator) {
		fValidator= validator;
	}
	/*
	 * @private
	 */
	protected void updateOKStatus() {
		if (!fIsEmpty) {
			if (fValidator != null) {
				// jdt overrode to add extra parameter. Why?
				//fValidator.isValid(getResult(), fCurrStatus);
				fValidator.isValid(getResult());
				updateStatus(fCurrStatus);
			} else {
				fCurrStatus.setOK();
			}
		} else {
			fCurrStatus.setError(fEmptyListMessage);
		}
		updateStatus(fCurrStatus);
	}
}
