package org.eclipse.cdt.cpp.ui.internal.views.search;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.dialogs.*;
import java.util.*;
import java.util.List;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.search.ui.*;
import org.eclipse.ui.model.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.util.Assert;

public class CppSearchPage extends DialogPage  implements ISearchPage, ICppSearchConstants{

	public static final String EXTENSION_POINT_ID= "org.eclipse.cdt.cpp.ui.internal.views.search.CppSearchPage";

	private static List fgPreviousSearchPatterns= new ArrayList(20);

	private Combo fPattern;
	private Combo scopeField;
	private Button browseButton;
	private String fInitialPattern;
	private boolean fFirstTime= true;

	protected Object scopeInput; 

	// NL enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static  String SEARCH_FOR_VARIABLE = "SearchViewer.Search_For.Variable";
	private static  String SEARCH_FOR_METHOD = "SearchViewer.Search_For.Method";
	private static  String SEARCH_FOR_CONSTUCT = "SearchViewer.Search_For.Construct";
	private static  String SEARCH_FOR_CLASS = "SearchViewer.Search_For.Class";
	private static  String SEARCH_FOR_TYPE = "SearchViewer.Search_For.Type";
	private static  String SEARCH_FOR_ALL = "SearchViewer.Search_For.All";
	
	private static  String LIMIT_TO_REF = "SearchViewer.Limit_To.References";
	private static  String LIMIT_TO_DECL = "SearchViewer.Limit_To.Declarations";
	private static  String LIMIT_TO_BOTH = "SearchViewer.Limit_To.Both";
	
	private static  String FILTER_CASE = "SearchViewer.Filter.Case_Sensitive";
	private static  String FILTER_REGULAR = "SearchViewer.Filter.Regular_Expressions";
	
	private   String SELEC_SCOPE_TITLE = "SearchViewer.Scope.Selection.Title";
	private  String EXP_TITLE ="SearchViewer.Expression.Title";
	private  String TEXT_TITLE="SearchViewer.Text_Field.Text";
	private String LIMIT_TO_TITLE ="SearchViewer.Limit_To.Title";
	
	private  String SCOPE_TITLE="SearchViewer.Scope.Title";
	private  String WORKSPACE_TITLE="SearchViewer.Workspace.Title";
	private  String SELECTION_TITLE="SearchViewer.Selection.Title";
	private  String BROWSE_BUTTON_TITLE="SearchViewer.Browse_Button.Title";
	
	private String SEARCH_FOR_TITLE = "SearchViewer.Search_For.Title";
	private String ERROR_MESSAGE="SearchViewer.Error_Message";
	
	private Button[] fSearchFor;
	private String[] fSearchForText= { pluginInstance.getLocalizedString(SEARCH_FOR_VARIABLE),
		pluginInstance.getLocalizedString(SEARCH_FOR_METHOD), 
		pluginInstance.getLocalizedString(SEARCH_FOR_CONSTUCT),
		pluginInstance.getLocalizedString(SEARCH_FOR_CLASS),
		pluginInstance.getLocalizedString(SEARCH_FOR_TYPE),
		pluginInstance.getLocalizedString(SEARCH_FOR_ALL) };

	private Button[] fLimitTo;
	private Button[] fFilters;
	private String[] fLimitToText= { pluginInstance.getLocalizedString(LIMIT_TO_REF),
		pluginInstance.getLocalizedString(LIMIT_TO_DECL),
		pluginInstance.getLocalizedString(LIMIT_TO_BOTH) };
	private String[] fFiltersText= { pluginInstance.getLocalizedString(FILTER_CASE),

		pluginInstance.getLocalizedString(FILTER_REGULAR) };
	private static class SearchPatternData {
		public SearchPatternData(int s, int l, String p){
			searchFor= s;
			limitTo= l;
			pattern= p;
		}
		int			searchFor;
		int			limitTo;
		String		pattern;
	}
// user selection - from the navigator
	private IStructuredSelection selection;
	private ISearchPageContainer fContainer;

/**
 * Attaches the given layout specification to the <code>component</code>
 */
private void browseButtonSelected(){
	
	ContainerSelectionDialog dialog = new ContainerSelectionDialog(scopeField.getShell(),null,false,pluginInstance.getLocalizedString(SELEC_SCOPE_TITLE));
	dialog.open();
	Object[] result =dialog.getResult();
	if(result!=null)
	{
		for(int i = 0; i < result.length ; i++)
		{
			if(result[i]!=null)
			{
				scopeInput = result[i];
				scopeField.setText(scopeInput.toString().substring(1));
			}
		}
	}
}
//---- Widget creation ------------------------------------------------
/**
* Creates the page's content.
*/
public void createControl(Composite parent) {
	GridData gd;
	Composite result= new Composite(parent, SWT.NONE);
	GridLayout layout= new GridLayout();
	layout.numColumns= 2; 
	layout.makeColumnsEqualWidth= true;
	
	layout.horizontalSpacing= 10;
	result.setLayout(layout);
	
	RowLayouter layouter= new RowLayouter(layout.numColumns);
	gd= new GridData();
	gd.horizontalAlignment= gd.FILL;
	layouter.setDefaultGridData(gd, 0);
	layouter.setDefaultGridData(gd, 1);

	layouter.setDefaultSpan();
	
	layouter.perform(createExpression(result));
	layouter.perform(createSearchFor(result), createLimitTo(result), -1);
	// new code
	//layouter.perform(createScope(result));
	layouter.perform(createFilters(result));
	
	// end new code
	
	setControl(result);
}

private Control createExpression(Composite parent) 
{
	Group result= new Group(parent, SWT.NONE);
	result.setText(pluginInstance.getLocalizedString(EXP_TITLE));
	GridLayout layout= new GridLayout();
	layout.numColumns= 1;
	result.setLayout(layout);
	RowLayouter layouter= new RowLayouter(2);
	layouter.setDefaultSpan();
	
	// Pattern combo
	fPattern= new Combo(result, SWT.SINGLE | SWT.BORDER);
	// Not done here to prevent page from resizing
	// fPattern.setItems(getPreviousSearchPatterns());
	fPattern.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (fPattern.getSelectionIndex() < 0)
				return;
			int index= fgPreviousSearchPatterns.size() - 1 - fPattern.getSelectionIndex();
			SearchPatternData values= (SearchPatternData) fgPreviousSearchPatterns.get(index);
			for (int i= 0; i < fSearchFor.length; i++)
				fSearchFor[i].setSelection(false);
			for (int i= 0; i < fLimitTo.length; i++)
				fLimitTo[i].setSelection(false);
			fSearchFor[values.searchFor].setSelection(true);
			fLimitTo[values.limitTo].setSelection(true);
			fLimitTo[IMPLEMENTORS].setEnabled((values.searchFor == TYPE));
			fInitialPattern= values.pattern;
			fPattern.setText(fInitialPattern);
			//fJavaElement= values.javaElement;
		}
	});
	fPattern.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			getContainer().setPerformActionEnabled(fPattern.getText().length() > 0);
		}
	});
	fPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	// Pattern info
	Label label= new Label(result, SWT.LEFT);
	label.setText(pluginInstance.getLocalizedString(TEXT_TITLE));
	return result;
}
private Control createFilters(Composite parent) {
	Composite result= new Composite(parent, SWT.NONE);
	GridLayout layout= new GridLayout();
	layout.numColumns= 2;
	result.setLayout(layout);
		fFilters= new Button[fFiltersText.length];
	for (int i= 0; i < fFiltersText.length; i++) {
		Button button= new Button(result, SWT.CHECK);
		button.setText(fFiltersText[i]);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if(i==1)
		    {
			button.setSelection(true);
		    }
		 
		fFilters[i]= button;
	}
	return result;		
}
private Control createLimitTo(Composite parent) {
	Group result= new Group(parent, SWT.NONE);
	result.setText(pluginInstance.getLocalizedString(LIMIT_TO_TITLE));
	GridLayout layout= new GridLayout();
	layout.numColumns= 2;
	result.setLayout(layout);
		fLimitTo= new Button[fLimitToText.length];
	for (int i= 0; i < fLimitToText.length; i++) {
		Button button= new Button(result, SWT.RADIO);
		button.setText(fLimitToText[i]);
		button.setEnabled(false);
		fLimitTo[i]= button;
	}
	return result;		
}
/*private Control createScope(Composite parent) {
	Group result= new Group(parent, SWT.NONE);
	result.setText(pluginInstance.getLocalizedString(SCOPE_TITLE));
	GridLayout layout= new GridLayout();
	layout.numColumns= 3;
	result.setLayout(layout);

	// controls
	Button workspace = new Button(result,SWT.RADIO);
	workspace.setText(pluginInstance.getLocalizedString(WORKSPACE_TITLE));
	workspace.setSelection(true);
	new Label(result,SWT.NONE);
	new Label(result,SWT.NONE);
	
	Button selection = new Button(result,SWT.RADIO);
	selection.setText(pluginInstance.getLocalizedString(SELECTION_TITLE));
	
	scopeField = new Combo(result,SWT.DROP_DOWN);
	scopeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	scopeField.setEnabled(false);
	browseButton = new Button(result,SWT.PUSH);
	browseButton.setText(pluginInstance.getLocalizedString(BROWSE_BUTTON_TITLE));
	browseButton.setEnabled(false);

	// add listener
	browseButton.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e) {
			browseButtonSelected();
		}
	});

	selection.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			boolean state= ((Button)event.widget).getSelection();
			if(state)
			{
				scopeField.setEnabled(true);
				browseButton.setEnabled(true);
				
			}
		}
	});
	workspace.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			boolean state= ((Button)event.widget).getSelection();
			if(state)
			{
				scopeField.setEnabled(false);
				browseButton.setEnabled(false);
			}
		}
	});
		
	return result;		
}*/
private Control createSearchFor(Composite parent) {
	Group result= new Group(parent, SWT.NONE);
	result.setText(pluginInstance.getLocalizedString(SEARCH_FOR_TITLE));
	GridLayout layout= new GridLayout();
	layout.numColumns= 3;
	result.setLayout(layout);
		fSearchFor= new Button[fSearchForText.length];
	for (int i= 0; i < fSearchForText.length; i++) {
		Button button= new Button(result, SWT.RADIO);
		button.setText(fSearchForText[i]);
		fSearchFor[i]= button;
	}
	return result;		
}
/**
 * Returns the search page's container.
 */
private ISearchPageContainer getContainer() {
	return fContainer;
}
private SearchPatternData getDefaultInitValues() {
	//return new SearchPatternData(TYPE, REFERENCES, "", null);
	return new SearchPatternData(TYPE, REFERENCES, "");
}
/**
 * Returns the current active editor part.
 */
private IEditorPart getEditorPart() {
	IWorkbenchWindow window= CppPlugin.getActiveWorkbenchWindow();
	if (window != null) {
		IWorkbenchPage page= window.getActivePage();
		if (page != null)
			return page.getActiveEditor();
	}
	return null;
}
private int getLimitTo() {
	for (int i= 0; i < fLimitTo.length; i++) {
		if (fLimitTo[i].getSelection())
			return i;
	}
	Assert.isTrue(false, pluginInstance.getLocalizedString(ERROR_MESSAGE));
	return -1;
}

private Object getNavigatorSelection() {
	IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	if (window != null)
	  {	    
	    ISelection	selection =  window.getSelectionService().getSelection();
	    StructuredSelection ss = new StructuredSelection(selection); 
	    Object root = ss.getFirstElement();
	    
	    if(root instanceof IResource)
		{
		    return root;
		}
	    else if (root instanceof DataElement)
		{
		    return root;
		}
	  }
	
	return null;

}
private String getPattern() {
	return fPattern.getText();
}
/**
* Return search pattern data and update previous searches.
* An existing entry will be updated.
*/
private SearchPatternData getPatternData() {
	String pattern= getPattern();
	SearchPatternData match= null;
	int i= 0;
	int size= fgPreviousSearchPatterns.size();
	while (match == null && i < size) {
		match= (SearchPatternData) fgPreviousSearchPatterns.get(i);
		i++;
		if (!pattern.equals(match.pattern))
			match= null;
	};
	if (match == null) {
		match= new SearchPatternData(getSearchFor(), getLimitTo(), getPattern());
		fgPreviousSearchPatterns.add(match);
	}
	else {
		match.searchFor= getSearchFor();
		match.limitTo= getLimitTo();
	};
	return match;
}
private String[] getPreviousSearchPatterns() 
{
	// Search results are not persistent
	int patternCount= fgPreviousSearchPatterns.size();
	String [] patterns= new String[patternCount];
	for (int i= 0; i < patternCount; i++)
		patterns[i]= ((SearchPatternData) fgPreviousSearchPatterns.get(patternCount - 1 - i)).pattern;
	return patterns;
}
/**
 * Attaches the given layout specification to the <code>component</code>
 */
protected Object  getScopeInput()
{
	return scopeInput;
}
  private int getSearchFor() {
		for (int i= 0; i < fSearchFor.length; i++) {
			if (fSearchFor[i].getSelection())
				return i;
		}
		Assert.isTrue(false, pluginInstance.getLocalizedString(ERROR_MESSAGE));
		return -1;
	}
 private ArrayList getSearchRelations(){
	 
	ArrayList relations = new ArrayList();
	
	if(fLimitTo[0].getSelection())
	  relations.add(new String("uses"));
	
	if(fLimitTo[1].getSelection())
	  relations.add(new String("declarations"));
	
	if(fLimitTo[2].getSelection())
	  relations.add(new String("all"));
	
	return relations;    
}
/*****/
private ArrayList getSearchTypes(){

	ArrayList types = new ArrayList();
	 	 
	if(fSearchFor[0].getSelection())
	  types.add(new String("variable"));
	
	if(fSearchFor[1].getSelection())
	  types.add(new String("function"));
	
	if(fSearchFor[2].getSelection())
	  types.add(new String("constructor"));
	
	if(fSearchFor[3].getSelection())
	  types.add(new String("class"));

	if(fSearchFor[4].getSelection())
	  types.add(new String("type"));
	  
	if(fSearchFor[5].getSelection())
	  types.add(new String("all"));
			
	return types; 
}
/**
 * Returns the current active selection.
 */
private ISelection getSelection() {
	return fContainer.getSelection();
}
  private void initSelections() {
	  
		ISelection selection= getSelection();
		SearchPatternData values= null;
		values= tryTypedTextSelection(selection);
		if (values == null)
			values= trySelection(selection);
		if (values == null)
			values= trySimpleTextSelection(selection);
		if (values == null)
			values= getDefaultInitValues();
					
		fSearchFor[values.searchFor].setSelection(true);
		fLimitTo[values.limitTo].setSelection(true);
		if (values.searchFor != TYPE)
			fLimitTo[IMPLEMENTORS].setEnabled(false);

		fInitialPattern= values.pattern;
		if(fInitialPattern==""||fInitialPattern == null)
			fPattern.setText("");
		else
			fPattern.setText(fInitialPattern);
  }  
public boolean isValid(){
	return true;
}
	//---- Action Handling ------------------------------------------------
public boolean performAction() {
	SearchPatternData data= getPatternData();
	String searchText = data.pattern;
	ModelInterface api = CppPlugin.getModelInterface();
	api.search(getScopeInput(), searchText, getSearchTypes(), getSearchRelations(),
		   !fFilters[0].getSelection(), !fFilters[1].getSelection());	
	return true;  // disposes the dialog
}
/*
 * Implements method from ISearchPage
 */
public void setContainer(ISearchPageContainer container) {
	fContainer= container;
}
/**
 * Attaches the given layout specification to the <code>component</code>
 */
protected void  setScopeInput(Object input)
  {
	scopeInput = input;
  }  
/**
 * This method is called whenever this page becomes visible (e.g. is selected). 
 */
public void setVisible(boolean visible) {
	if (visible && fPattern != null) {
		if (fFirstTime) {
			fFirstTime= false;
			// Set item and text here to prevent page from resizing
			fPattern.setItems(getPreviousSearchPatterns());
			initSelections();
		}
		fPattern.setFocus();
		getContainer().setPerformActionEnabled(fPattern.getText().length() > 0);
	}
	super.setVisible(visible);
}
private SearchPatternData trySelection(ISelection selection) 
{
	SearchPatternData result= null;
	
	if (selection == null)
		return result;
	
	Object o = null;
	if(selection instanceof IStructuredSelection)
		o = ((IStructuredSelection)selection).getFirstElement();

	if (o instanceof IMarker) 
	{
		IMarker marker= (IMarker) o;
		try
		{
			String handleId= (String)marker.getAttribute(ICppSearchUIConstants.ATT_JE_HANDLE_ID);
		}catch(Exception ex){}
	} 
	else if (o instanceof IAdaptable) 
	{	
	    if (o instanceof DataElement)
		{
		    result= new SearchPatternData(TYPE, REFERENCES, ((DataElement)o).getName());		    
		}
	    else
		{
		    IWorkbenchAdapter element= (IWorkbenchAdapter)((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
		    result= new SearchPatternData(TYPE, REFERENCES, element.getLabel(o));
		}
	}
	return result;
}
  private SearchPatternData trySimpleTextSelection(ISelection selection) 
  {
	SearchPatternData result= null;
	if (selection instanceof ITextSelection) {
	  ITextSelection ts= (ITextSelection)selection;
	  result= new SearchPatternData(TYPE, REFERENCES, ts.getText());
	}
	return result;
  }  
  private SearchPatternData tryTypedTextSelection(ISelection selection) 
  {
	SearchPatternData result= null;
	if (selection instanceof ITextSelection) 
	  {
	IEditorPart e= getEditorPart();
	if (e != null) 
	  {
	    ITextSelection ts= (ITextSelection)selection;
	  }
	return result;
	  }

	return result;
	
  }  
}
