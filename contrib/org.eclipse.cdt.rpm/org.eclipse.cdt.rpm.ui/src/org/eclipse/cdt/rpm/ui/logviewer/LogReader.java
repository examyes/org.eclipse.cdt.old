/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.rpm.ui.logviewer;

import org.eclipse.cdt.rpm.core.RPMCorePlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Display;

/**
 * A Runnable to read rpm's logfile and stuff it into the daemon view's TextViewer.
 * @author Keith Seitz  <keiths@redhat.com> (oringinally for oprofile)
 * 	modified to be used in the Rpm plugin to view the RPM import/export log
 */
public class LogReader implements Runnable {
	private static String log_name = ""; //$NON-NLS-1$
	private static TextViewer _viewer = null;
	private static String _logfile = null;
	private static long last_modified = 0;
	static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	static final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
	/**
	 * Constructor
	 * @param viewer	the TextViewer in which to display log file
	 */
	public LogReader(TextViewer viewer) {
		_viewer = viewer;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		// Don't do anything if the logviewer was disposed since the last time
		if (_viewer != null) {
			String logfile_name = getLogFile();
		   if (logfile_name == null) {
		   	return;
		   }
			File log_file = new File(logfile_name);
			  // If the file does not exist, do not do anything
			if (log_file.exists()) {
			  long size = log_file.length();
			  if (!logfile_name.equals(log_name) | size != last_modified) {
					/* Log file has changed read it in and stuff it into the
						log viewer. */
					log_name = logfile_name;
					last_modified = size;
				
					try {
						BufferedReader reader = new BufferedReader(new FileReader(log_file));
						String contents = new String();
						String line;
						while ((line = reader.readLine()) != null) {
							contents += line + line_sep;
						}
						
					// Content changed, notify logviewer
						_viewer.setDocument(new Document(contents));
					} catch (FileNotFoundException e) {
					// The file doesn't exist or was erased. Try again next time.
					} catch (IOException e) {
					// Error reading log. Try again next time.
					}
			}
			
			// Re-register ourselves to be run after the timeout
			Display.getCurrent().timerExec(1000, this);
		}
	  } else {
	  		last_modified = 0;
	  		log_name = "";
		}
	}
	
	/**
	 * Stop updating log file view because viewer was disposed
	 */
	public void dispose() {
		_viewer = null;
	}
	
	public String getLogFile() {
		String wksp_path = RPMCorePlugin.getDefault().getPreferenceStore()
				.getString("IRpmConstants.RPM_WORK_AREA"); //$NON-NLS-1$
		String log_name = RPMCorePlugin.getDefault().getPreferenceStore()
				.getString("IRpmConstants.RPM_DISPLAYED_LOG_NAME"); //$NON-NLS-1$
		String log_file_name = wksp_path + file_sep + log_name + file_sep;
		File f = new File(log_file_name);
		if (!f.exists()) {
			return null;
		}
		try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				return reader.readLine();
				} catch (FileNotFoundException e) {
		// The file doesn't exist or was erased. Try again next time.
				} catch (IOException e) {
		// Error reading log. Try again next time.
	}
	return null;
  }
}