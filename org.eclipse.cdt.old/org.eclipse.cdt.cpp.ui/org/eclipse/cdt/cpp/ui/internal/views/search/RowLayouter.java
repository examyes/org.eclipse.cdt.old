package com.ibm.cpp.ui.internal.views.search;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.util.Assert;

public class RowLayouter {

	public int spanHorizontalAlignment= -1;
	public int spanGrabExcessHorizontalSpace= -1;
	public int spanHorizontalSpan= -1;
	public int spanHorizontalIndent= -1;
	public int spanWidthHint= -1;
		
	public int spanVerticalAlignment= -1;
	public int spanGrabExcessVerticalSpace= -1;
	public int spanVerticalSpan= -1;
	public int spanHeightHint= -1;
	
	private int fNumColumns;
	private boolean fOrder;
	private Control fLastControl;
	private GridData[] fDefaultGridDatas= new GridData[4];
		
	public RowLayouter(int numColumns) {
		this(numColumns, false);
	}
	public RowLayouter(int numColumns, boolean order) {
		fNumColumns= numColumns;
		fOrder= order;
	}
	private void applyDelta(GridData gd) {
		if (spanHorizontalAlignment != -1)
			gd.horizontalAlignment= spanHorizontalAlignment;
			
		if (spanGrabExcessHorizontalSpace != -1) {
			if (spanGrabExcessHorizontalSpace == 0)
				gd.grabExcessHorizontalSpace= false;
			else
				gd.grabExcessHorizontalSpace= true;
		}		
				
			
		if (spanHorizontalSpan != -1)
			gd.horizontalSpan= spanHorizontalSpan;
			
		if (spanHorizontalIndent != -1)
			gd.horizontalIndent= spanHorizontalIndent;
		
		if (spanWidthHint != -1)
			gd.widthHint= spanWidthHint;
			
		if (spanVerticalAlignment != -1)
			gd.verticalAlignment= spanVerticalAlignment;
			
		if (spanGrabExcessVerticalSpace != -1) {
			if (spanGrabExcessVerticalSpace == 0)
				gd.grabExcessVerticalSpace= false;
			else
				gd.grabExcessVerticalSpace= true;
		}		
			
		if (spanVerticalSpan != -1)
			gd.verticalSpan= spanVerticalSpan;
			
		if (spanHeightHint != -1)
			gd.heightHint= spanHeightHint;
	}
	protected GridData cloneGridData(GridData gd) {
		if (gd == null)
			return null;
			
		GridData result= new GridData();
		result.horizontalAlignment= gd.horizontalAlignment;
		result.grabExcessHorizontalSpace= gd.grabExcessHorizontalSpace;
		result.horizontalSpan= gd.horizontalSpan;
		result.horizontalIndent= gd.horizontalIndent;
		result.widthHint= gd.widthHint;
		
		result.verticalAlignment= gd.verticalAlignment;
		result.grabExcessVerticalSpace= gd.grabExcessVerticalSpace;
		result.verticalSpan= gd.verticalSpan;
		result.heightHint= gd.heightHint;
		return result;
	}
	public GridData getGridData(int index) {
		if (index > fDefaultGridDatas.length)
			return null;
			
		return cloneGridData(fDefaultGridDatas[index]);
	}
	public int numColumns() {
		return fNumColumns;
	}
	protected void order(Control[] controls) {
		if (!fOrder)
			return;
			
		for (int i= 0; i < controls.length; i++) {
			Control control= controls[i];
			control.moveBelow(fLastControl);
			fLastControl= control;
		}
	}
	public void perform(Control[] controls, int spanColumn) {
		int numColumns= numColumns();
		Assert.isTrue(controls.length <= numColumns);
		order(controls);
		int gridIndex= 0;
		for (int i= 0; i < controls.length; i++) {
			Control control= controls[i];
			GridData gd= (GridData)control.getLayoutData();
			if (gd == null)
				gd= getGridData(gridIndex);
				
			if (i == spanColumn) {
				int span= numColumns - (controls.length - 1);
				gridIndex+= span;
				if (gd == null)
					gd= new GridData();
				applyDelta(gd);
				gd.horizontalSpan= span;
			} else {
				gridIndex++;
			}
			control.setLayoutData(gd);
		}
	}
	public void perform(Control c1) {
		perform(new Control[] {c1}, 0);
	}
	public void perform(Control c1, Control c2, int span) {
		perform(new Control[] {c1, c2}, span);
	}
	public void perform(Control c1, Control c2, Control c3, int span) {
		perform(new Control[] {c1, c2, c3}, span);
	}
	public void setDefaultGridData(GridData gd, int index) {
		if (index >= fDefaultGridDatas.length) {
			GridData[] newDatas= new GridData[index + 4];
			System.arraycopy(fDefaultGridDatas, 0, newDatas, 0, fDefaultGridDatas.length);
			fDefaultGridDatas= newDatas;
		}
		fDefaultGridDatas[index]= gd;
	}
public void setDefaultSpan() {
	spanHorizontalAlignment= GridData.FILL;
	spanGrabExcessHorizontalSpace= 1;
}
}
