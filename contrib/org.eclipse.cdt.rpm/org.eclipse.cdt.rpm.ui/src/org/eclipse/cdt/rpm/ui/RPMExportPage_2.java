/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

/**
 * @author pmuldoon
 * @version 1.0
 *
 * S/RPM  export page 2. Defines the patch page that is shown to the user when they choose
 * to export to an SRPM and patch. Defines the UI elements shown, and the basic validation (need to add to
 * this)
 */
package org.eclipse.cdt.rpm.ui;

import org.eclipse.cdt.rpm.core.RPMCorePlugin;

import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import java.text.SimpleDateFormat;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.ArrayList;
import java.io.*;

public class RPMExportPage_2 extends WizardPage implements Listener {
	// Composite file/browse control
	// Core RPM build class
	private RPMExportOperation rpmExport;

	// Checkbox Buttons
	private Button generatePatch;

	// Patch Fields
	private Text patchTag;
	private Text patchChangeLog;
	private Text patchChangeLogstamp;
	private boolean first_spec = true;
	private String path_to_specfile_save = null;
	private String last_gettext = ""; //$NON-NLS-1$
	private ArrayList patch_names;
	private boolean firstTag = true;
	private boolean patchTagError;
	static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	
	/**
	 * @see java.lang.Object#Object()
	 *
	 * Constructor for RPMExportPage class
	 */
	public RPMExportPage_2() {
		super(Messages.getString("RPMExportPage.Export_SRPM"), //$NON-NLS-1$
		Messages.getString("RPMExportPage.Export_SRPM_from_project"), null); //$NON-NLS-1$ //$NON-NLS-2$
		setDescription(Messages.getString("RPMExportPage_2.0")); //$NON-NLS-1$
		patch_names = new ArrayList();
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 *
	 * Parent control. Creates the listbox, Destination box, and options box
	 *
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		createPatchFields(composite);
		populatePatchInfo();
	}


	/**
	 * Method populatePatchInfo
	 *
	 * Populate the patch widgets with data
	 */
	protected void populatePatchInfo() {

		String userName = RPMCorePlugin.getDefault().getPreferenceStore()
				.getString("IRpmConstants.AUTHOR_NAME"); //$NON-NLS-1$
		String userEmail = RPMCorePlugin.getDefault().getPreferenceStore()
				.getString("IRpmConstants.AUTHOR_EMAIL"); //$NON-NLS-1$

		// Populate the changeLog
		Date today = new Date();
		SimpleDateFormat df = new SimpleDateFormat("E MMM dd yyyy"); //$NON-NLS-1$

		patchChangeLogstamp.setText("* " + df.format(today) + //$NON-NLS-1$
			" -- " + userName + " <" + userEmail + ">");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		patchChangeLog.setText("- "); //$NON-NLS-1$
	}

	/**
	 * Method createGenPatchFields
	 *
	 * Create the patch generation widgets
	 */
	protected void createPatchFields(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMExportPage.Patch_Options")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite composite = new Composite(group, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));
		
		// Listen for changes to the patch tag field so we can make sure
		// this tag has not been used before
		ModifyListener trapTag = new ModifyListener() {
			public void modifyText(ModifyEvent e)  {
				if (firstTag) {
					patch_names = getPatchNames(RPMExportPage.getSpecFilePath());
					firstTag = false;
				}
				
				patchTagError = false;
				// Does it have any spaces
				if (patchTag.getText().lastIndexOf(" ") != -1) { //$NON-NLS-1$
					setErrorMessage(Messages.getString("RPMExportPage_2.1")); //$NON-NLS-1$
					patchTagError = true;
					handleEvent(null);
					return;
				}
				// Is the patch tag unique? (That is, if there are any patch tags.)
				if (!patch_names.isEmpty()) {
					for (int i = 0; i < patch_names.size(); i++) {
						if (patchTag.getText().equals((String) patch_names.get(i))) {
							patchTagError = true;
							setErrorMessage(Messages.getString("RPMExportPage_2.3")); //$NON-NLS-1$
							handleEvent(null);
							return;
						}
					}
					setErrorMessage(null);
					setDescription(Messages.getString("RPMExportPage_2.0")); //$NON-NLS-1$
					handleEvent(null);
				}
			}
		} ;  

		ModifyListener trapPatch = new ModifyListener() {
		
				public void modifyText(ModifyEvent e) {
					handleEvent(null);
				}
			};

		GridData patchTagGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
			GridData.GRAB_HORIZONTAL);
		new Label(composite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Patch_Tag")); //$NON-NLS-1$
		patchTag = new Text(composite, SWT.BORDER);
		patchTag.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Patch_Tag")); //$NON-NLS-1$

		patchTag.setLayoutData(patchTagGridData);

		GridData pChangelogStampGridData = new GridData(
			GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		new Label(composite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Patch_Changelog_Stamp")); //$NON-NLS-1$

		patchChangeLogstamp = new Text(composite, SWT.BORDER);
		patchChangeLogstamp.setLayoutData(pChangelogStampGridData);
		patchTag.addModifyListener(trapTag);
		patchChangeLogstamp.addModifyListener(trapPatch);
		patchChangeLogstamp.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Changelog_Stamp")); //$NON-NLS-1$

		

		new Label(composite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Patch_Changelog")); //$NON-NLS-1$

		KeyListener patchChangelogListener = new KeyListener() {
				public void keyPressed(KeyEvent e) {
					handleEvent(null);
				}
				public void keyReleased(KeyEvent e) {
					handleEvent(null);
					if (e.keyCode == 13) {
						if (patchChangeLog.getCaretPosition() == patchChangeLog.getCharCount())
							patchChangeLog.append("- "); //$NON-NLS-1$
						else
							if (patchChangeLog.getText(patchChangeLog.getCaretPosition()-1,
								patchChangeLog.getCaretPosition()-1).equals("\n")) //$NON-NLS-1$
									patchChangeLog.insert("- "); //$NON-NLS-1$
					}
				}
			};

		GridData pChangelogGridData = new GridData(
			GridData.HORIZONTAL_ALIGN_FILL |
			GridData.GRAB_HORIZONTAL);
			
		patchChangeLog = new Text(composite, SWT.BORDER | SWT.MULTI);
		pChangelogGridData.heightHint = 7 * patchChangeLog.getLineHeight();
		patchChangeLog.setLayoutData(pChangelogGridData);
		patchChangeLog.addKeyListener(patchChangelogListener);
		patchChangeLog.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Changelog")); //$NON-NLS-1$

	
	}

	/**
		 * canFinish()
		 * 
		 * Hot validation. Called to determine whether Finish
		 * button can be set to true
		 * @return boolean. true if finish can be activated
		 */
	public boolean canFinish() {
		
		// Is the patch tag empty
		if (patchTag.getText().equals("")) { //$NON-NLS-1$
			return false;
		}
		
//		 Is tag a duplicate or have spaces?
		if (patchTagError) {
			return false;
		}
		
			else		{
				setErrorMessage(null);
				setDescription(Messages.getString("RPMExportPage_2.2")); //$NON-NLS-1$
			}

		// Is the Changelog fields empty?
		if (patchChangeLog.getText().equals("- ")) { //$NON-NLS-1$

			return false;
		}

		// Is the time stamp empty?
		if (patchChangeLogstamp.getText().equals("")) { //$NON-NLS-1$

			return false;
		}
		
		return true;
	}

	public String[] patchData() {
		String[] patchDataList = new String[3];

		patchDataList[0] = patchTag.getText();
		patchDataList[1] = patchChangeLogstamp.getText();
		patchDataList[2] = patchChangeLog.getText();

		return patchDataList;
	}

	public void handleEvent(Event e) {
		setPageComplete(canFinish());
	}
	
	private String getHostName()
	 {
	 	String hostname;
		  try {
			  hostname = java.net.InetAddress.getLocalHost().getHostName();
		  } catch (UnknownHostException e) {
			  return ""; //$NON-NLS-1$
		  }
		  // Trim off superflous stuff from the hostname
		  int firstdot = hostname.indexOf("."); //$NON-NLS-1$
		  int lastdot = hostname.lastIndexOf("."); //$NON-NLS-1$
		  // If the two are equal, no need to trim name
		  if (firstdot == lastdot) {
		  	return hostname;
		  }
		  String hosttemp = ""; //$NON-NLS-1$
		  String hosttemp2 = hostname;
		  while (firstdot != lastdot) {
		  	hosttemp = hosttemp2.substring(lastdot) + hosttemp;
		  	hosttemp2 = hostname.substring(0,lastdot);
		  	lastdot = hosttemp2.lastIndexOf("."); //$NON-NLS-1$
		  }
		  return hosttemp.substring(1);
	 }
	 /*
	  * This method gets the path to the spec file that will be used to create
	  * the rpm and gleans the patch names from it.
	  * @returns populated ArrayList if spec file found, null ArrayList if not
	  */ 
	 
	 private ArrayList getPatchNames(String path_to_specfile) {
	
	 	File f = new File(path_to_specfile);
	 	if (!f.exists()) {
	 		return null;
	 	}
		ArrayList patch_names = new ArrayList();
		try {
					FileReader sp_file = new FileReader(path_to_specfile);
					StreamTokenizer st = new StreamTokenizer(sp_file);

					// Make sure numbers, colons and percent signs are considered valid
					st.wordChars('a','z');
					st.wordChars('A','Z');
					st.wordChars(':', ':');
					st.wordChars('0', '9');
					st.wordChars('%', '%');
					st.wordChars('{', '}');
					st.wordChars('-', '-');
					st.wordChars('/', '/');
					st.wordChars('=','=');
					st.wordChars('.','.');
					st.wordChars('_','_');
					st.eolIsSignificant(true);
					boolean found_patch = false;
            
					String new_word;
					int token = st.nextToken();
						while (token != StreamTokenizer.TT_EOF) {
							token = st.nextToken();

							switch (token) {

							case StreamTokenizer.TT_WORD:
								new_word = st.sval;
								if (new_word.startsWith("Patch")) { //$NON-NLS-1$
									found_patch = true;
									break;
								}
								if (new_word.endsWith(".patch") && found_patch) {  //$NON-NLS-1$
									int i = new_word.lastIndexOf("-"); //$NON-NLS-1$
									int j = new_word.lastIndexOf(".patch"); //$NON-NLS-1$
									patch_names.add(new_word.substring(i+1,j));
									found_patch = false;
									break;
								}
								default:
								  found_patch = false;
								  break;
							}
						}
					sp_file.close();
				} catch (IOException e) {
					return null;
				}
			return patch_names;
	 }
}
