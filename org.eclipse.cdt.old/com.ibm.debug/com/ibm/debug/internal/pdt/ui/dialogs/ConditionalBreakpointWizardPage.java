package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/ConditionalBreakpointWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.12 (last modified 11/28/01 15:58:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.pdt.ui.util.DialogField;
import com.ibm.debug.internal.pdt.ui.util.IDialogFieldListener;
import com.ibm.debug.internal.pdt.ui.util.MGridData;
import com.ibm.debug.internal.pdt.ui.util.MGridLayout;
import com.ibm.debug.internal.pdt.ui.util.Separator;
import com.ibm.debug.internal.pdt.ui.util.StringCombo;
import com.ibm.debug.internal.pdt.ui.util.StringDialogField;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;



/** The second page in the add breakpoint wizards, if conditional breakpoints are supported.*/
public class ConditionalBreakpointWizardPage extends WizardPage implements IDialogFieldListener{
	private static final String PAGE_NAME= "BreakpointWizard.optional";
	private StringCombo threadField;
	private StringDialogField fromField;
	private StringDialogField toField;
	private StringDialogField everyField;
	private StringDialogField expressionField;
	private Composite composite;
	private Group frequencyGroup;
	private boolean supportsExpressionField = false;
	private boolean supportsFrequencyFields = false;
	private boolean supportsThreadField = false;

	private static IDialogSettings section;
	private static final String EXPRESSION ="Expression"; //profile key
	private static final String TO = "To";
	private static final String FROM = "From";
	private static final String EVERY = "Every";
	private static final String THREAD = "Thread";

	IMarker existingBP;
	boolean editing = false;


	/**
	 * Constructor for BreakpointWizardOptionalPage
	 */
	protected ConditionalBreakpointWizardPage(String pageName, String title, ImageDescriptor titleImage,
				boolean supportsExpression, boolean supportsFrequency, boolean supportsThreads) {
		super(pageName, title, titleImage);
		setTitle(title);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		if(titleImage !=null)
			setImageDescriptor(titleImage);
		supportsExpressionField = supportsExpression;
		supportsFrequencyFields = supportsFrequency;
		supportsThreadField = supportsThreads;

	}

	/**
	 * Constructor for BreakpointWizardOptionalPage
	 */
	protected ConditionalBreakpointWizardPage(String pageName, String title, ImageDescriptor titleImage,
					boolean supportsExpression, boolean supportsFrequency, boolean supportsThreads, IMarker breakpoint) {
		super(pageName, title, titleImage);
		setTitle(title);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		if(titleImage !=null)
			setImageDescriptor(titleImage);
		supportsExpressionField = supportsExpression;
		supportsFrequencyFields = supportsFrequency;
		supportsThreadField = supportsThreads;
		editing=true;
		existingBP = breakpoint;
	}


	/**
	 * Used to give focus to the thread field after the page is visible.
	 */
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if(visible && supportsThreadField)
			threadField.setFocus();
		else if(visible && supportsFrequencyFields)
			fromField.setFocus();
		else if(supportsExpressionField)
			expressionField.setFocus();

	}

	private void createOptionalFields()
	{
		if(supportsThreadField)
		{
			threadField = new StringCombo();
			threadField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".threadLabel"));
		}

		if (supportsFrequencyFields)
		{
			fromField = new StringDialogField();
			fromField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".fromLabel"));
			fromField.setText("1");

			toField = new StringDialogField();
			toField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".toLabel"));
			toField.setText(PICLUtils.getResourceString(PAGE_NAME+".defaultInfinity"));

			everyField = new StringDialogField();
			everyField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".everyLabel"));
			everyField.setText("1");
		}

		if(supportsExpressionField)
		{
			expressionField = new StringDialogField();
			expressionField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".expressionLabel"));
		}
	}



	/**
	 * @see WizardPage#createControl
	 */
	public void createControl(Composite parent) {

		createOptionalFields();

		composite= new Composite(parent, SWT.NONE);
		int nColumns= 3;

		MGridLayout layout= new MGridLayout();
		layout.minimumWidth= 400;
		layout.minimumHeight= 200;
		layout.numColumns= nColumns;
		composite.setLayout(layout);

		if(supportsThreadField)
		{
			threadField.doFillIntoGrid(composite, 2 );
			//can't populate until doFillIntoGrid has been called (comboControl is null until then)
			threadField.setItems(getCurrentThreads());
			threadField.setText(PICLUtils.getResourceString(PAGE_NAME+".defaultEvery"));

			DialogField.createEmptySpace(composite,1, 200);
			new Separator().doFillIntoGrid(composite,nColumns,15);
		}

		if (supportsFrequencyFields)
		{
			frequencyGroup = new Group(composite, SWT.SHADOW_IN);
			frequencyGroup.setText(PICLUtils.getResourceString(PAGE_NAME+".frequencyLabel"));
			MGridLayout gridLayout = new MGridLayout();
			gridLayout.numColumns = nColumns;
			gridLayout.minimumWidth= 200;
			frequencyGroup.setLayout(gridLayout);
			MGridData gridData = new MGridData(MGridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.horizontalSpan = nColumns;
			//gridData.grabExcessHorizontalSpace =true;
			//gridData.widthHint=100;  //column width
			frequencyGroup.setLayoutData(gridData);

			fromField.doFillIntoGrid(frequencyGroup,2);
			DialogField.createEmptySpace(frequencyGroup,1);
			toField.doFillIntoGrid(frequencyGroup,2);
			DialogField.createEmptySpace(frequencyGroup,1);
			everyField.doFillIntoGrid(frequencyGroup,2);
			DialogField.createEmptySpace(frequencyGroup,1);
		}

		if(supportsExpressionField)
		{
			new Separator().doFillIntoGrid(composite,nColumns,15);
			expressionField.doFillIntoGrid(composite, nColumns );
		}


		setControl(composite);

		String pageHelpID = PICLUtils.getHelpResourceString("ConditionalBPWizardPage");
		//sets the help for any helpless widget on the page
		WorkbenchHelp.setHelp(getShell(), new DialogPageContextComputer(this, pageHelpID));
		//set widget specific help, with page help as backup
		if(supportsThreadField)
			WorkbenchHelp.setHelp(threadField.getComboControl(composite) , new Object[] {PICLUtils.getHelpResourceString("ConditionalBPWizardPage.threadField") , pageHelpID });
		if(supportsFrequencyFields)
		{
			WorkbenchHelp.setHelp(toField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("ConditionalBPWizardPage.toField") , pageHelpID });
			WorkbenchHelp.setHelp(fromField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("ConditionalBPWizardPage.fromField") , pageHelpID});
			WorkbenchHelp.setHelp(everyField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("ConditionalBPWizardPage.everyField") , pageHelpID});
		}
		if(supportsExpressionField)
			WorkbenchHelp.setHelp(expressionField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("ConditionalBPWizardPage.expressionField") , pageHelpID});

		//postSetFocusOnDialogField(parent.getDisplay());
		if(editing) //defaults are better when not editing
			restoreSettings();
	}

	/**
	 * This method will flip the wizard pages to place itself on top.
	 */
	public void flipToMe()
	{
		getWizard().getContainer().showPage(this);
	}

	/**
	 *	Always sets the page complete after text is typed.
	 *  May be used to do some error checking in the future.
	 */
	public void dialogFieldChanged(DialogField field)
	{
		setPageComplete(true);
	}

	private void restoreSettings()
	{
		if(section == null)
		{
			IDialogSettings dialogSettings = getDialogSettings();
			if((section=dialogSettings.getSection(PAGE_NAME)) == null)
			{
				section=dialogSettings.addNewSection(PAGE_NAME);
			}
		}

		if(editing)
		{
			initUsingOldBreakpoint();
			return;
		}
		String text = "";
		if(supportsThreadField)
		{
  			text = section.get(THREAD);
  			if( text != null)
  			{
  				if(text.equals("0"))
  					threadField.setText(PICLUtils.getResourceString(PAGE_NAME+".defaultEvery"));
  				threadField.setText(text);
  			}
		}
	  	if(supportsFrequencyFields)
	  	{
	  		text = section.get(TO);
  			if( text != null)
		  		toField.setText(text);

  			text = section.get(FROM);
  			if( text != null)
		  		fromField.setText(text);

			text = section.get(EVERY);
  			if( text != null)
	  			everyField.setText(text);
	  	}
	  	if (supportsExpressionField)
	  	{
	  		text = section.get(EXPRESSION);
	  		if( text != null)
	  			expressionField.setText(text);
	  	}
  	}

	/** Returns the value currently in the thread field. */
	public String getThreadValue()
	{
		if(supportsThreadField)
			return threadField.getText();
		else return PICLUtils.getResourceString(PAGE_NAME+".defaultEvery");
	}
	/** Returns the value currently in the from field. */
	public Integer getFromValue()
	{
		if(!supportsFrequencyFields)
			return new Integer(1);  //default to every time
		try{
			return Integer.valueOf(fromField.getText());
		}
		catch(NumberFormatException e){
			return new Integer(1);
		}
	}
	/** Returns the value currently in the to field. */
	public Integer getToValue(){
		if (!supportsFrequencyFields)
			return new Integer(0);	//default to infinity
		if(toField.getText().equals(PICLUtils.getResourceString(PAGE_NAME+".defaultInfinity")))
			return new Integer(0);
		try{
			return Integer.valueOf(toField.getText());
		}
		catch(NumberFormatException e){
			// todo: display error
			return new Integer(0);
		}
	}
	/** Returns the value currently in the every field. */
	public Integer getEveryValue(){
		if(!supportsFrequencyFields)
			return new Integer(1); //default to every time
		try{
			return Integer.valueOf(everyField.getText());
		}
		catch(NumberFormatException e){
			// todo: display error
			return new Integer(1);
		}
	}
	/** Returns true is expression field is enabled/supported. */
	public boolean isExpressionFieldSupported(){
		return supportsExpressionField;
	}

	/** Returns the value currently in the expression field. */
	public String getExpression(){
		if(isExpressionFieldSupported())
			return expressionField.getText();
		else return null;
	}

	/**
	 * This method initializes the dialog fields with the values of the existing
	 * breakpoint that the user is editing.
	 */
	public void initUsingOldBreakpoint()
	{
		try{
			if(supportsThreadField)
			{
				String thread = (String)existingBP.getAttribute(IPICLDebugConstants.THREAD);
				if(thread != null)
				{
					if(thread.equals("0"))
  						threadField.setText(PICLUtils.getResourceString(PAGE_NAME+".defaultEvery"));
					else threadField.setText(thread);
				}
			}
			if(supportsFrequencyFields)
			{
				if(existingBP.getAttribute(IPICLDebugConstants.TO_VALUE) !=null)
				{
					if( (existingBP.getAttribute(IPICLDebugConstants.TO_VALUE)).toString().equals("0") )
						toField.setText(PICLUtils.getResourceString(PAGE_NAME+".defaultInfinity"));
					else toField.setText( (existingBP.getAttribute(IPICLDebugConstants.TO_VALUE)).toString());
				}
				if(existingBP.getAttribute(IPICLDebugConstants.FROM_VALUE) !=null)
					fromField.setText(existingBP.getAttribute(IPICLDebugConstants.FROM_VALUE).toString());
				if(existingBP.getAttribute(IPICLDebugConstants.EVERY_VALUE) !=null)
					everyField.setText(existingBP.getAttribute(IPICLDebugConstants.EVERY_VALUE).toString());
			}

			if(	supportsExpressionField && existingBP.getAttribute(IPICLDebugConstants.CONDITIONAL_EXPRESSION)!=null)
				expressionField.setText((String)existingBP.getAttribute(IPICLDebugConstants.CONDITIONAL_EXPRESSION));

		}catch(CoreException e){}


	}

	/**
	 * Returns an array of thread names. Used to populate the thread combobox.
	 */
	private String[] getCurrentThreads()
	{
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null)
		{
			return new String[] {PICLUtils.getResourceString(PAGE_NAME+".defaultEvery")};
		}

		IWorkbenchPage p = window.getActivePage();
		if(p == null)
			return new String[] {PICLUtils.getResourceString(PAGE_NAME+".defaultEvery")};

		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);

		if (view != null)
		{
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null)
			{
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection)
				{
					Object firstElement = ((IStructuredSelection)selection).getFirstElement();
					if(firstElement != null && firstElement instanceof PICLDebugElement)
					{
						IDebugTarget target = ((PICLDebugElement)firstElement).getDebugTarget();
						if(target != null && !target.isTerminated())
						{
						//	DebuggeeThread[] threads = ((PICLDebugTarget)target).getDebuggeeProcess().getThreadsArray();
							IDebugElement[] threads;
							try{
								threads = ((PICLDebugTarget)target).getChildren();
							}catch(Exception e){return new String[] {PICLUtils.getResourceString(PAGE_NAME+".defaultEvery")};}
							String[] names = new String[threads.length+1];
							//always add "every" to list of choices. Therefore, names never null.
							names[0]=(PICLUtils.getResourceString(PAGE_NAME+".defaultEvery"));
							for(int i=0; i < threads.length; i++)
							{
								if(threads[i] instanceof PICLThread)
									//names.addElement(threads[i].getNameOrTID().getName());
									names[i+1] = ((PICLThread)threads[i]).getName();
							}
							return names;


						}

					}
				}


			}
		}

		return new String[] {PICLUtils.getResourceString(PAGE_NAME+".defaultEvery")};
	}


	/**
	 * @see ISettingsWriter#writeSettings
	 */
	public void writeSettings()
	{	if(supportsThreadField)
			section.put(THREAD, threadField.getText());
		else //avoid losing values that were set previously by dialogs that supported it
			section.put(THREAD, section.get(THREAD));
		if(supportsFrequencyFields)
		{
			section.put(TO, toField.getText());
			section.put(FROM, fromField.getText());
			section.put(EVERY, everyField.getText());
		}
		else  //avoid losing values that were set previously by dialogs that supported it
		{
			section.put(TO, section.get(TO));
			section.put(FROM,section.get(FROM));
			section.put(EVERY,section.get(EVERY));
		}
		if(	supportsExpressionField)
			section.put(EXPRESSION, expressionField.getText());
		else //avoid losing values that were set previously by dialogs that supported it
			section.put(EXPRESSION, section.get(EXPRESSION));
	}

}

