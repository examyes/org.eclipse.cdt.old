/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.cdt.rpm.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.io.*;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
  *This class contains the core methods for manipulating rpm's.  Most othe classes
  *extend this one to get to these classes.
  */
public class RPMCore  {
	// When debug is set to true, lots of debug statements are printed.
	private static final boolean debug = false;
	protected static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	protected static final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
	protected static final String Error = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
	
	protected String spec_file_prefix = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.SPEC_FILE_PREFIX"); //$NON_NLS-1$
	protected String srpm_info_name = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.SRPM_INFO_FILE"); //$NON_NLS-1$
	protected String wksp_path = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.RPM_WORK_AREA"); //$NON-NLS-1$
	protected String rpmrc = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.RPM_RESOURCE_FILE"); //$NON_NLS-1$
	protected String rpm_macros = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.RPM_MACROS_FILE"); //$NON_NLS-1$
	protected String rpm_shell = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.RPM_SHELL_SCRIPT"); //$NON_NLS-1$
	protected String rpmbuild_logname = RPMCorePlugin.getDefault().getPreferenceStore()
		.getString("IRpmConstants.RPM_LOG_NAME"); //$NON_NLS-1$

	protected String proj_path;
	protected String path_to_rpm;
	protected String rpmdirs_path;
	protected String rpm_version = "1"; //$NON_NLS-1$
	protected String rpm_release = "0"; //$NON_NLS-1$
	protected String rpm_name;
	protected String path_to_specfile;
	protected String path_to_newspecfile;
	protected String spec_file_name;
	protected String proj_dir;
	protected String srpm_full_name;
	protected String ui_rel_no = ""; //$NON_NLS-1$
	protected String ui_ver_no = ""; //$NON_NLS-1$
	protected boolean chk_sum_diff = false;
	
	private boolean preserve = true;
	private boolean generate_patch;
	private String rpm_spec;
	private String checksum_string;
	private String diff_old_dir;
	private String diff_new_dir;
	private String copied_proj_path;
	private String orig_srpm_path;
	private String srpm_abbr_name;
	

	/**
		  * Constructor #1 for RPMCore - used for the RPM import to Eclipse Project sequence
		  * also used for exporting an Eclipse C/C++ project to Source RPM
		  * This method is called when a source rpm is to be imported into an Eclipse C/C++ project
		  * or exported from an Eclipse project.  It extends LinuxShellCmds and is extended by
		  * RPMExportCore and SRPMExport. 
		  * @param c_proj_path - is a string containing the full path to the workspace project of the form
		  *                                /home/xxxx/workspace/cproject
		  * @param c_path_to_rpm - is a string containing the path to the user-selected RPM 
		  * 			(only used if this is an import of a source RPM)
		  * @return - throws a CoreException if it cannot get to the .sprminfo file if this was a
		  * 			previously imported source RPM
		  */
	public RPMCore(String c_proj_path, String c_path_to_rpm) 
		throws CoreException {
		if (debug) {
			System.out.println(
				"RPMCore constructor**************************************************"); //$NON-NLS-1$
		}
		
		proj_path = c_proj_path;
		path_to_rpm = c_path_to_rpm;
		
		String user_wksp = wksp_path + file_sep + System.getProperty("user.name"); //$NON-NLS-1$ //$NON-NLS-2$
		rpmdirs_path = user_wksp + file_sep + "rpm_workarea"; //$NON-NLS-1$
		String srpm_abbr_name = ""; //$NON-NLS-1$
	 	
		int j = proj_path.lastIndexOf(file_sep); //$NON-NLS-1$
		if ( j == -1) {
			proj_dir = proj_path;
		} else {
			proj_dir = proj_path.substring( j + 1);
		}
		if (!firstSRPM(proj_path)) {
			ArrayList srpminfo = new ArrayList();
			try {
				srpminfo = getSRPMexportinfo(proj_path);
				checkSrpmExists((String) srpminfo.get(0));
			} catch (CoreException e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
				throw new CoreException(error); 
			}

			long cksum = generateChecksum(proj_path, 0);
			int i = spec_file_name.lastIndexOf(file_sep);
			if (i == -1) {
				path_to_specfile = proj_path + file_sep + spec_file_name;
			} else {
				path_to_specfile = spec_file_name;
			}
			if (Long.parseLong(checksum_string) != cksum) {
				chk_sum_diff = true;
			}
			
			if (!(path_to_rpm.equals(""))) { //$NON-NLS-1$
			  j = path_to_rpm.lastIndexOf(file_sep);
			  if (j != -1) {
			  	srpm_full_name = path_to_rpm.substring(j+1);	
			  }
			  j = srpm_full_name.lastIndexOf(".src.rpm"); //$NON-NLS-1$

			// Strip off the ".src.rpm" part of the name to get the abbreviated name
			  if (j!= -1) {
				srpm_abbr_name = srpm_full_name.substring(0, j);
			  } 
		//	If this is the first time this project has been exported into RPM
		// format, use the Eclipse project directory name as the RPM name
	 	   }
		}  else {
			rpm_name = proj_dir;
			srpm_abbr_name = proj_dir;
			srpm_full_name = proj_dir;
			spec_file_name = proj_dir + ".spec"; //$NON-NLS-1$
	 }

		rpm_spec = rpmdirs_path + file_sep + "SPECS" + file_sep + srpm_abbr_name + ".spec"; //$NON-NLS-1$ //$NON-NLS-2$
		rpm_shell = rpmdirs_path + file_sep + rpm_shell;
		rpm_macros = rpmdirs_path + file_sep + getRpm_macros();
		rpmrc = rpmdirs_path + file_sep + getRpmrc();
	}
	
	/**
		  * Constructor #2 for RPMCore - used by the GUI to access information stored in the
		  * project's .srpminfo file and the latest source RPM built from this project.  The GUI
		  * displays the RPM version/release info on the screen for the user to change if desired.
		  * This method is called when a source rpm is to be imported into an Eclipse C/C++ 
		  * project.  It is called to instantiate this class so the GUI can call getSRPMinfo
		  */
	public RPMCore() throws CoreException {
		
	}

	/**
	 * Method checkMakefileForClean.
	 * This method is used to check the M/makefile in the C/C++ project for a
	 * particular string.
	 * @param path - contains a string to the Makefile to be searched
	 * @param srch_string - contains a string to search the Makefile for
	 * @return boolean - return true if successful, false if not
	 * @exception - throw a CoreException if we have problems reading the 
	 * Makefile
	 */
	/******************************************************************************/
	public boolean checkMakefileForString(String path, String srch_string) 
	      throws CoreException {
		if (debug) {
			System.out.println("checkForMakefileClean: " + path); //$NON-NLS-1$
		}
		boolean makefile_found = false;
		boolean found_string = false;
		String sh_command;
		String line;
		String[] makefile_path = { "", "" }; //$NON-NLS-1$ //$NON-NLS-2$

		if (!path.equals("")) { //$NON-NLS-1$
			makefile_path[0] = path + file_sep + "Makefile"; //$NON-NLS-1$
			makefile_path[1] = path + file_sep + "makefile"; //$NON-NLS-1$
		}

		for (int i = 0; i < makefile_path.length; i++) {
			File f = new File(makefile_path[i]);

			// Now check for whether or not there are 'install:'/'clean:' sections in the Makefile/makefile
			if (f.exists()) {
				makefile_found = true;

				try {
					FileReader makefile = new FileReader(makefile_path[i]);
					StreamTokenizer st = new StreamTokenizer(makefile);
					st.wordChars(':', ':');
					st.wordChars('-', '-');

					int token = st.nextToken();

					while ((token != StreamTokenizer.TT_EOF) &
							(!found_string)) {
						token = st.nextToken();

						switch (token) {
						case StreamTokenizer.TT_WORD:

							String word = st.sval;

							if (word.equals(srch_string) | word.equals(srch_string + ":")) { //$NON-NLS-1$
								return true;
							}

							break;

						default:
							break;
						}
					}

					makefile.close();
				} catch (IOException e) {
					String throw_message = Messages.getString(
							"RPMCore.I/O_error_processing/reading_the_M/makefile__183") + //$NON-NLS-1$
						e;
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
					throw new CoreException(error);
				}
			}
		}
		return false;
	}
	/**
	 * Method checkForMakefile.
	 * This method is used to check the C/C++ project to be made into an RPM has a
	 * Makefile/makefile present in its top directory
	 * @param path - contains a string with the path of where to check for the existence
	 * of a Makefile or makefile  (this last check for "makefile" may be removed at a later 
	 * date if deemed unnecessary)
	 * @return boolean - return true if successful, throw CoreException if not
	 */
	/******************************************************************************/
	public boolean checkForMakefile(String path) throws CoreException {
		if (debug) {
			System.out.println("checkMakefile: " + path); //$NON-NLS-1$
		}
		boolean makefile_found = false;
		String sh_command;
		String line;
		String[] makefile_path = { "", "" }; //$NON-NLS-1$ //$NON-NLS-2$

		if (!path.equals("")) { //$NON-NLS-1$
			makefile_path[0] = path + file_sep + "Makefile"; //$NON-NLS-1$
			makefile_path[1] = path + file_sep + "makefile"; //$NON-NLS-1$
		}

		for (int i = 0; i < makefile_path.length; i++) {
			File f = new File(makefile_path[i]);

			// Now check for whether or not there are 'install:'/'clean:' sections in the Makefile/makefile
			if (f.exists()) {
				return true;
			}
		}
		// If no M/makefile is found, throw an error.
			String throw_message = Messages.getString(
					"RPMCore.Failed_to_find_a_M/makefile_in_the_project.___THIS_IS_REQUIRED_!_!_!_185"); //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
			
	}

	/**
			 * Method copyDirTree.
			 * This method copies one directory tree to another.
			 * @param dir - A file that points to the directory where the traversing
			 * is to begin
			 * @return - false if unsuccessful, true if successful
			 * ***NOTE*** this method is not currectly used, we use the 
			 * Linux "cp" command right now since Java provides not way
			 * to preserve file permissions, particularly the "execute" bit...
			 * when this deficiency is corrected, we will replace those "cp"
			 * commands with calls to this method
			 */
	private boolean copyDirTree(String dirin, String dirout) {
		if (debug) {
			System.out.println("--copyDirTree" + //$NON-NLS-1$
				line_sep + "----dirin = " + dirin + //$NON-NLS-1$
				line_sep + "----dirout = " + dirout); //$NON-NLS-1$
		}

		int file_ctr;
		String infile;
		String outfile;
		File newdirin = new File(dirin);
		File newdirout = new File(dirout);

		if (newdirin.isDirectory()) {
			// If the directory to output to already exists, 
			// skip trying to create it
			if (!newdirout.exists()) {
				if (!newdirout.mkdir()) {
					return false;
				}
			}

			// Get a list of all of the files in this directory
			String[] children = newdirin.list();

			// Walk the tree and copy files
			for (int i = 0; i < children.length; i++) {
				infile = dirin + file_sep + children[i]; 
				outfile = dirout + file_sep + children[i];

				File newfilein = new File(infile);
				File newfileout = new File(outfile);
				long modifiedTime = newfilein.lastModified();

				if (newfilein.isDirectory()) {
					copyDirTree(infile, outfile);
				} else {
					try {
						copyFile(infile, outfile);
					} catch (Exception e) {
						return false;
					}

					if (!newfileout.setLastModified(modifiedTime)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Method getConfigOpts.
	 * This method takes a spec file path and parses it to see if there are any options
	 * that need to be passed to the "configure" script when conmfiguring an RPM.
	 * @param path_to_specfile - contains a string with a path to the spec file to be
	 * searched to see if the "configure" command has any options to be applied
	 * @return a string containing the options to pass to configure if any were found
	 */
	public String getConfigOpts(String path_to_specfile) throws CoreException {
		if (debug) {
			System.out.println("--getConfigOpts: " + path_to_specfile); //$NON-NLS-1$
		}

	
		boolean found_config = false;
		int lines = 0;
		int config_line = 0;
		String config_opts = ""; //$NON-NLS-1$
		
		try {
			FileReader sp_file = new FileReader(path_to_specfile);
			StreamTokenizer st = new StreamTokenizer(sp_file);
//			  st.resetSyntax();

			// Make sure numbers, colons and percent signs are considered valid
			st.wordChars('a','z');
			st.wordChars('A','Z');
			st.wordChars(':', ':');
			st.wordChars('0', '9');
			st.wordChars('%', '%');
			st.wordChars('{', '}');
			st.wordChars('-', '-');
			st.wordChars('/','/');
			st.wordChars('=','=');
			st.wordChars('.','.');
			st.wordChars('_','_');
			st.eolIsSignificant(true);
            
			String new_word;
			int if_ctr = 0;
			int token = st.nextToken();
			while (token != StreamTokenizer.TT_EOF) {
				token = st.nextToken();

				switch (token) {
				case StreamTokenizer.TT_EOL:
				  lines++;	
				  break;
				case StreamTokenizer.TT_WORD:
					new_word = st.sval;
					// System.out.println("---- " + new_word + line_sep + "   line no = " + st.lineno());
					
					// If '%configure' was found, gather the options if there were any
					if (found_config & config_line == lines) {
						config_opts = config_opts + " --" + new_word; //$NON-NLS-1$
						break;
					}
					if (found_config & !(config_line == lines)) {
						found_config = false;
						break;
					}

						// See if there is a %configure section
						if (new_word.equals("%configure")) { //$NON-NLS-1$
							found_config = true;
							config_line = lines;
                        	
							break;
						}
				}
			}

			sp_file.close();
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_parsing_the_spec_file_in_the_project_--_157") + //$NON-NLS-1$
					path_to_specfile;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		return config_opts;
	}
	

	/**
	 * Method executeProjConfigure.
	 * See if there is a "configure" script to be run to set up the project with a properly
	 * configured Makefile and if there is, run it.
	 * @param orig_proj_path - contans a string with a path to the directory in which
	 * to run the "configure" script
	 * @return boolean - return true if successful, throw CoreException if not
	 */
	/***************************************************************************/
	public void executeProjConfigure(String orig_proj_path) throws CoreException {
		if (debug) {
			System.out.println("executeProjConfigure"); //$NON-NLS-1$
		}
		String config_opts;
        
		// Check to see if there is indeed a "configure" script
		File config = new File(orig_proj_path + file_sep + "configure"); //$NON-NLS-1$

		if (config.exists()) {
            
				// We need to parse the spec file to see if there are any "configure" options
				 File spec_path = new File(rpmdirs_path + file_sep + "SPECS" + file_sep); //$NON-NLS-1$
				 String[] children = spec_path.list();
				 String path_to_specfile = rpmdirs_path + file_sep + "SPECS" + file_sep + children[0]; //$NON-NLS-1$
				 try {
				   config_opts = getConfigOpts(path_to_specfile);
				 } catch (Exception e) {
					 String throw_message = Messages.getString("RPMCore.Error_parsing_spec_file_at__33") + path_to_specfile; //$NON-NLS-1$
					 IStatus error = new Status(IStatus.ERROR, Error, 1,
						 throw_message, null);
					 throw new CoreException(error);
				 }
			String conf_cmd = "cd " + orig_proj_path + line_sep + "." + file_sep + "configure " + //$NON-NLS-1$ //$NON-NLS-2$
						   config_opts + " >> " + rpmdirs_path + file_sep + "configure.log"; //$NON-NLS-1$ //$NON-NLS-2$
			try {
				LinuxShellCmds.createLinuxShellScript(conf_cmd, rpmbuild_logname, rpm_shell);
				LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
			} catch (Exception e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}
	}
	/**
	 * Method getSRPMexportinfo.
	 * This method is called to get the information from the .srpminfo
	 * file in the Eclipse project where information about an imported
	 * source RPM is kept.
	 * @param String containing the path to the Eclipse project
	 * @return ArrayList - contains the following if successful:
	 *     ArrayList[0] = previous source RPM used to create this Eclipse project
	 *     ArrayList[1] = version number
	 *     ArrayList[2] = release number
	 *     ArrayList[3] = rpm name
	 *     ArrayList[4] = date when SRPM was imported/exported
	 *     ArrayList[5] = spec file name
	 *     ArrayList[6] = String with the checksum
	 * 
	 * or throws CoreException if unsuccessful
	 */
	public ArrayList getSRPMexportinfo(String project_path)
		throws CoreException {
		if (debug) {
			System.out.println("getSRPMexportinfo -- " + project_path); //$NON-NLS-1$
		}

		String path_to_srpm_info_file = project_path + srpm_info_name; //$NON-NLS-1$
		String Error = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
		boolean found_chksum = false;

		// See if the srpminfo file exists
		ArrayList srpm_info = new ArrayList();

		// Read the first line of the file in, it is a warning about not deleting the file
		try {
			BufferedReader in = new BufferedReader(new FileReader(
						path_to_srpm_info_file));
			String str;

			while ((str = in.readLine()) != null) {
				if (str.lastIndexOf(".src.rpm") != -1) { //$NON-NLS-1$

					path_to_rpm = project_path + file_sep + str; //$NON-NLS-1$
					srpm_info.add(0, path_to_rpm);

					// save the source rpm path for the installRPMsource() method
					// path_to_rpm = path;
					// Now get the version/release number from the RPM
					rpm_version = LinuxShellCmds.getInfo(
							"/bin/rpm --qf %{VERSION} -qp " + //$NON-NLS-1$
							path_to_rpm);
					rpm_release = LinuxShellCmds.getInfo(
							"/bin/rpm --qf %{RELEASE} -qp " + //$NON-NLS-1$
							path_to_rpm);
					rpm_name = LinuxShellCmds.getInfo("/bin/rpm --qf %{NAME} -qp " + //$NON-NLS-1$
							path_to_rpm); 
					srpm_info.add(1, rpm_version);
					srpm_info.add(2, rpm_release);
					srpm_info.add(3, rpm_name);
				}

				if (str.startsWith("Date:")) { //$NON-NLS-1$

					String date_temp = str.substring(6, str.length());
					srpm_info.add(4, date_temp);
				}

				if (str.startsWith("Specfile:")) { //$NON-NLS-1$

					spec_file_name = str.substring(10, str.length());
					srpm_info.add(5, spec_file_name);
				}

				if (str.startsWith("Checksum:")) { //$NON-NLS-1$

					checksum_string = str.substring(10, str.length());
					srpm_info.add(6, checksum_string);
					found_chksum = true;
				}
			}

			in.close();
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_getting_info_from__93") + //$NON-NLS-1$
				path_to_srpm_info_file;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		if (!found_chksum) {
			srpm_info.add(6, "-1"); //$NON-NLS-1$
		}

		return srpm_info;
	}

  /** 
   * Method checkSrpmExists takes a path to the source rpm to be
   * checked and verifies that there is indeed still there.
   * If there is not, we cannot successfully export this project.
   * @param project - a string containing the path to the selected project
   * @throws CoreException if it does not exist
   */
	public void checkSrpmExists(String srpm_path) throws CoreException {
		
		File f = new File(srpm_path);
//		Make sure the source rpm is still where it was

		if (!f.exists()) {
			String throw_message = Messages.getString(
				 "RPMCore.There_is_no_longer_a_source_RPM_at__86") + //$NON-NLS-1$
				 srpm_path +
				 Messages.getString("RPMCore._nThis_RPM_*must*_be_restored_before_exporting_can_occur._1"); //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 2, //$NON-NLS-1$
				 throw_message, null);
			 throw new CoreException(error);
		}
		return;
	}
	
	/**
	 * Method copyFile with two file input strings
	 * Takes two input strings containing pathnames, one input file path and
	 * one output file path and does a copy
	 * @param inName - a string containing a path to the input file
	 * @param outName - a string containing a path to the output file
	 * @return - throws any of three exceptions if an error occurs
	 */
	public void copyFile(String inName, String outName)
		throws FileNotFoundException, IOException, CoreException {
		//        if (debug) {
		//            System.out.println("--copyFile - inName: " + inName + //$NON-NLS-1$
		//                line _sep + "      outName: " + outName); //$NON-NLS-1$
		//        }
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(
					inName));
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
					outName));

		try {
			copyFile(is, os, true);
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
					throw_message, null);
			throw new CoreException(error);
		}
	}

	/**
	 * Method copyFile which copies a file from an input stream to an output stream
	 * @param is - a file  InputStream
	 * @param os - a file OutputStream
	 * @param close - a boolean to tell if the OutputStream should be closed upon exit
	 * @return if successful, throw Exception if not
	 */
	public void copyFile(InputStream is, OutputStream os, boolean close)
		throws IOException, CoreException {
		int b;

		try {
			while ((b = is.read()) != -1) {
				os.write(b);
			}

			is.close();

			if (close) {
				os.close();
			}
		} catch (Exception e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_trying_to_write_to__8") + os; //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
					throw_message, null);
			throw new CoreException(error);
		}
	}

	/**
	 * Method getNameVerRel interrogates a spec file for the name, version and release
	 * of the RPM
	 * @param path_to_specfile contains a string pointing to the specfile to interrogate
	 * @return if successful, throw Exception if not
	 */

	public ArrayList getNameVerRel(String path_to_specfile)
		throws CoreException, FileNotFoundException {
		if (debug) {
			System.out.println("getNameVerRel"); //$NON-NLS-1$
		}

		ArrayList rpm_info = new ArrayList();
		ArrayList define_info = new ArrayList();

		// initialize version/release numbers to 0 in case none are found in the spec file
		rpm_info.add(0, "0"); //$NON-NLS-1$
		rpm_info.add(1, "0"); //$NON-NLS-1$
		rpm_info.add(2, " "); //$NON-NLS-1$

		boolean found_version = false;
		boolean found_release = false;
		boolean found_name = false;
		boolean found_ver_token = false;
		boolean found_rel_token = false;
		boolean found_name_token = false;
		boolean found_define = false;
		boolean found_define_name = false;
		int define_ctr = 0;
        
		File f = new File(path_to_specfile);

		if (!f.exists()) {
			String throw_message = Messages.getString(
					"RPMCore.Failed_to_find_a_the_spec_file_at") + //$NON-NLS-1$
				path_to_specfile;
			IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
					throw_message, null);
			throw new CoreException(error);
		}

		try {
			FileReader sp_file = new FileReader(path_to_specfile);
			StreamTokenizer st = new StreamTokenizer(sp_file);

			// Make sure numbers, colons and periods are considered valid characters
			st.resetSyntax();
			st.wordChars(':', ':');
			st.wordChars('0', '9');
			st.wordChars('.', '.');
			st.wordChars('A', 'z');
			st.wordChars('%','%');
			st.wordChars('{','{');
			st.wordChars('}','}');

			int token = 0;
			String new_word;
outer: 
			while (token != StreamTokenizer.TT_EOF) {
				token = st.nextToken();

				switch (token) {
				case StreamTokenizer.TT_WORD:
					new_word = st.sval;
                    
					if (found_define) {
						found_define = false;
						define_info.add(define_ctr,new_word);
						define_ctr++;
						found_define_name = true;
						break;
					}
                    
					if (found_define_name) {
						found_define_name = false;
						define_info.add(define_ctr,new_word);
						define_ctr++;
						break;
					}
                    
					if (found_version & !found_ver_token) {
						found_ver_token = true;
						if (new_word.startsWith("%")) { //$NON-NLS-1$
							try {
								rpm_info.set(0,parseDefine(new_word, define_info));
							} catch (Exception e) {
								String throw_message = Messages.getString("RPMCore.Error_using_parseDefine_to_get_the_version_no._41") + //$NON-NLS-1$
								  Messages.getString("RPMCore._from_the_spec_file_at___42") + path_to_specfile; //$NON-NLS-1$
								IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
													throw_message, null);
								throw new CoreException(error);
							}
						} else {
							 rpm_info.set(0, new_word);
						}

						// System.out.println("Found version = " + new_word);
						if (found_name_token & found_ver_token &
								found_rel_token) {
							break outer;
						}

						break;
					}

					if (found_release & !found_rel_token) {
						found_rel_token = true;
						if (new_word.startsWith("%")) {  //$NON-NLS-1$
							try {
								rpm_info.set(1,parseDefine(new_word, define_info));
							} catch (Exception e) {
							String throw_message = Messages.getString("RPMCore.Error_using_parseDefine_to_get_the_release_no._44") + //$NON-NLS-1$
							  Messages.getString("RPMCore._from_the_spec_file_at___45") + path_to_specfile; //$NON-NLS-1$
							IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
												throw_message, null);
							throw new CoreException(error);
						}
							break;
						} else {
							 rpm_info.set(1, new_word);
						  }

						// System.out.println("Found release = " + new_word);
						if (found_name_token & found_ver_token &
								found_rel_token) {
							break outer;
						}

						break;
					}

					if (found_name & !found_name_token) {
						found_name_token = true;
						rpm_info.set(2, new_word);

						// System.out.println("Found name = " + new_word);
						if (found_name_token & found_ver_token &
								found_rel_token) {
							break outer;
						}

						break;
					}

					// See if this is a "Version:" tag
					if (new_word.equals("Version:")) { //$NON-NLS-1$
						found_version = true;
						break;
					}

					// See if this is a "Release:" tag
					if (new_word.equals("Release:")) { //$NON-NLS-1$
						found_release = true;
						break;
					}

					// See if this is a "Name:" tag
					if (new_word.equals("Name:")) { //$NON-NLS-1$
						found_name = true;
						break;
					}
                    
					// See if this a "%define" statement
					// the version and release can sometimes be in a define stmt
					if (new_word.equals("%define")) {  //$NON-NLS-1$
						found_define = true;
						break;
					}

				default:
					break;
				}
			}
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_parsing_the_spec_file_at") + //$NON-NLS-1$
				path_to_specfile;
			IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
					throw_message, null);
			throw new CoreException(error);
		}

		return rpm_info;
	}

	/**
	  * Method parseDefine accepts a token from the parser and
	  * searches the ArrayList passed to it for the value of the
	  * token name.  This is crude at this point since this does not
	  * happen very often.
	  * @param token is a string containing the name found after the
	  *               "Version:" or "Release:" fields of a spec file and the
	  *               begining character is a "%"
	  * @param token_value ia an ArrayList containing the names and
	  *               values found in the "%define" statements usually found
	  *              at the top of the spec file
	  * @return a string with the correct version or release number
	  *               else throw a CoreException
	  */
	  public String parseDefine(String token, ArrayList token_value) 
		 throws CoreException {
		  if (debug) {
			  System.out.println("parseDefine - token = " + token); //$NON-NLS-1$
		  }
		  // See if there in anything in the ArrayList
		  if (token_value.isEmpty()) {
			  String throw_message = Messages.getString("RPMCore.No___%defines___were_found_in_the_spec_file_38"); //$NON-NLS-1$
			  IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
								  throw_message, null);
			  throw new CoreException(error);
		  }
		  // A token usually looks this: %{name}
		  String token_name = token.substring(2,token.length()-1);
		  int i = token_value.indexOf(token_name);
		  return (String) token_value.get(i+1);
		 }


	/**
	 * Method generateChecksum is used to calculate the size of the source
	 * files in a project.  This can then be used by the GUI to see if any files
	 * have changed since the last time this project was exported.
	 * @param project_path is a string containing the path to the project
	 * @param a long containing the initial value to begin  with (since this is
	 * a recursive function, the value is always passed into the function)
	 * @return long integer with total file size
	 */
	public long generateChecksum(String project_path, long proj_checksum) {
//		  if (debug) {
//			  System.out.println("generateChecksum"); //$NON-NLS-1$
//		  }

		File dir = new File(project_path);

		if (dir.isDirectory()) {
			String[] children = dir.list();

			for (int i = 0; i < children.length; i++) {
				// System.out.println("children[" + i + "] = " + children[i]); //$NON-NLS-1$ //$NON-NLS-2$

				File temp = new File(project_path + file_sep + children[i]);

				if (temp.isDirectory()) {
					proj_checksum = generateChecksum(project_path + file_sep + 
							children[i], proj_checksum);
				} else {
					if ((children[i].endsWith(".c") | //$NON-NLS-1$
							children[i].endsWith(".cpp") | //$NON-NLS-1$
							children[i].endsWith(".h") | //$NON-NLS-1$
							children[i].endsWith(".in") | //$NON-NLS-1$
							children[i].endsWith(".pl") | //$NON-NLS-1$
							children[i].endsWith(".s") | //$NON-NLS-1$
							children[i].endsWith(".log") | //$NON-NLS-1$
							children[i].endsWith(".m4") | //$NON-NLS-1$
							children[i].endsWith("-sh") | //$NON-NLS-1$
							children[i].endsWith(".mo") | //$NON-NLS-1$
							children[i].endsWith(".po") | //$NON-NLS-1$
							children[i].endsWith(".pot") | //$NON-NLS-1$
							children[i].endsWith(".sh")) & //$NON-NLS-1$
							(!children[i].equals("config.log") & //$NON-NLS-1$
							!children[i].equals("config.h"))) { //$NON-NLS-1$
						proj_checksum = proj_checksum + temp.length();
						// System.out.println("children[" + i + "] = " + children[i] + " ... " + temp.length() + " ... " + proj_checksum);
					}
					if (children[i].equals("Makefile") & !LinuxShellCmds.checkForConfigure(project_path)) { //$NON-NLS-1$
						proj_checksum = proj_checksum + temp.length();
					}
				}
			}
		}

		return proj_checksum;
	}
	/**
	 * Method executeMakeClean.
	 * Create a shell script to do a "make clean" in the project and run it.
	 * @param String containing a path where "make xxxxclean" needs to be run
	 * @return boolean - return true if successful, else return false
	 */
	/***************************************************************************/
	public boolean executeMakeClean(String mc_path) throws CoreException {
		if (debug) {
			System.out.println("executeMakeClean"); //$NON-NLS-1$
		}

		String make_cmd = ""; //$NON-NLS-1$
		String orig_srpm_path = ""; //$NON-NLS-1$
		// Create the shell script for the "make clean" command and execute it
		if (mc_path.equals("")) { //$NON-NLS-1$
			mc_path = orig_srpm_path;
		}

		File f = new File(mc_path + file_sep + "Makefile"); //$NON-NLS-1$
		// The different "cleans" are searched in a particular order.  Depending on the 
		// how the RPM package maintainer designed their Makefile;  some maintainers
		// use maintainer-clean, some use realclean and some use distclean and some use
		// all three, with maintainer clean being the best
		if (f.exists()) {
			if (checkMakefileForString(mc_path, "maintainer-clean:")) { //$NON-NLS-1$
				make_cmd = line_sep + "/usr/bin/make maintainer-clean"; //$NON-NLS-1$
			} else if (checkMakefileForString(mc_path, "realclean:")) { //$NON-NLS-1$
				make_cmd = line_sep + "/usr/bin/make realclean"; //$NON-NLS-1$
			} else if (checkMakefileForString(mc_path, "distclean:")) { //$NON-NLS-1$
				make_cmd = line_sep + "/usr/bin/make distclean"; //$NON-NLS-1$
			} else {
				make_cmd = line_sep + "/usr/bin/make clean"; //$NON-NLS-1$
			}

			String mc_cmd = "( cd " + mc_path + make_cmd; //$NON-NLS-1$

			try {
				LinuxShellCmds.createLinuxShellScript(mc_cmd, rpmbuild_logname, rpm_shell);
				LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
			} catch (CoreException e) {
				String throw_message = Messages.getString(
						"RPMCore.Problem_running_the___make_install___shell_script_--__518") + //$NON-NLS-1$
						rpm_shell +
					Messages.getString(
						"RPMCore._nThere_may_be_a_problem_in_the_M/makefile._519"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}
	  return true;
	}

		/**
		 * Method deleteSRPMextrafiles.
		 * This method deletes files the "make distclean" sometimes misses.
		 * @param path_to_start - the starting directory
		 * @return true if successful, false if not
		 */
		public boolean deleteSRPMextrafiles(File path_to_start)
			throws CoreException {
//			  if (debug) {
//				  System.out.println("--deleteSRPMextrafiles"); //$NON-NLS-1$
//			  }

			String[] files_to_del = { ".a", ".o", ".so", "~", ".rpm", ".spec.new" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

			if (path_to_start.isDirectory()) {
				String[] subfiles = path_to_start.list();

				for (int i = 0; i < subfiles.length; i++) {
					File f = new File(path_to_start, subfiles[i]);

					if (f.isDirectory()) {
						try {
							deleteSRPMextrafiles(f);
						} catch (CoreException e) {
							String throw_message = e.getMessage();
							IStatus error = new Status(IStatus.ERROR, Error, 1,
									throw_message, null);
							throw new CoreException(error);
						}
					}

					for (int j = 0; j < files_to_del.length; j++) {
						int ext_length = files_to_del[j].length();
						int file_length = subfiles[i].length();

						if (ext_length > file_length) {
							continue;
						}

						String file_ext = subfiles[i].substring(file_length -
								ext_length);

						if (!file_ext.equals(files_to_del[j])) {
							continue;
						}

						boolean del_file = f.delete();

						if (!del_file) {
							String throw_message = Messages.getString(
									"RPMCore.Error_deleting_files_in_deleteSRPMextrafiles_498"); //$NON-NLS-1$
							IStatus error = new Status(IStatus.ERROR, Error, 1,
									throw_message, null);
							throw new CoreException(error);
						}
					}
				}
			}
			return true;
		}
	
		/**
			 * Method deleteEclipseFiles will delete the files added to an SRPM that
			 * was imported by Eclipse and the import process.
			 * @param String containing the path where to start deleting
			 * @return true if successful, false if not
			 */
			public boolean deleteEclipseFiles(String path_to_delete, String rpm_name)
				throws CoreException {
				if (debug) {
					System.out.println("--deleteEclipseFiles"); //$NON-NLS-1$
				}

				// Remove the .srpminfo file used to store info about the SRPM
				String[] eclipse_files = {
					srpm_info_name, file_sep + ".project", file_sep + ".cdtproject", file_sep + ".cdtbuild", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					file_sep + spec_file_prefix + rpm_name + ".spec", //$NON-NLS-1$
					file_sep + spec_file_prefix + rpm_name + 
					".spec.new" + file_sep + //$NON-NLS-1$
					"Binaries" //$NON-NLS-1$
            
				};

				for (int i = 0; i < eclipse_files.length; i++) {
					File f = new File(path_to_delete + eclipse_files[i]);

					if (f.exists()) {
						if (!f.delete()) {
							String throw_message = Messages.getString(
									"RPMCore.Error_deleting_files_in_deleteEclipseFiles_616"); //$NON-NLS-1$
							IStatus error = new Status(IStatus.ERROR, Error, 1,
									throw_message, null);
							throw new CoreException(error);
						}
					}
				}

				return true;
			}
	
	/**
		 * Method createRPMdirectories creates the directories in the "path" passed to
		 * it necessary for the "rpm/rpmbuild" command to execute.
		 * @param path contains a string to the path of where to create the directories
		 * @return boolean - true is the operation was successful,
		 *      throw CoreException if not
		 */
		/******************************************************************************/

		// Create RPM Directories used for the rpmbuild process
		public boolean createRPMdirectories(String path) throws CoreException {
			if (debug) {
				System.out.println("createRPMdirectories: path = " + //$NON-NLS-1$
				path);
			}

			boolean cmd_stat;
			File f = new File(path);

			// If an old environment exists remove it
			if (f.exists()) {
				deleteRPMworkarea(f);
			}

			// Create the rpm temporary work area
			if (!f.mkdirs()) {
				String throw_message = Messages.getString(
						"RPMCore.Failed_to_create_RPM_directories,_check_file_permissions_in__195") + //$NON-NLS-1$
				wksp_path;
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}

			// Create the directories required by rpm/rpmbuild to perform their work
			String[] rpm_dirs = { "BUILD", "RPMS", "SOURCES", "SPECS", "SRPMS" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

			for (int i = 0; i < rpm_dirs.length; i++) {
				File f1 = new File(rpmdirs_path + file_sep + rpm_dirs[i]);

				if (!f1.mkdir()) {
					String throw_message = Messages.getString(
							"RPMCore.Failed_to_create_RPM_directories_in__203") + //$NON-NLS-1$
						f1 +
						Messages.getString(
							"RPMCore._--_check_file_permissions._204"); //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
					throw new CoreException(error);
				}
			}

			// Set the permissions of the work area so only the owner can access for 
			String chmodcommand = "/bin/chmod -R 744 " + rpmdirs_path + file_sep; //$NON-NLS-1$ //$NON-NLS-2$

			try {
				LinuxShellCmds.executeLinuxCommand(chmodcommand, 0);
			} catch (CoreException e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}
			return true;
		}
		/**
		 * Method createRPMLogFile.
		 * Create the file where the name of the logfile resides for this run.
		 * @return throw CoreException if unsuccessful
		 */
		public void createRPMLogFile() throws CoreException {
			if (debug) {
				System.out.println("createRPMLogFile: " + rpmbuild_logname);  //$NON-NLS-1$
			}

			String logfilename = wksp_path + file_sep + 
				RPMCorePlugin.getDefault().getPreferenceStore().
				getString("IRpmConstants.RPM_DISPLAYED_LOG_NAME");  //$NON-NLS-1$
			byte[] buf = rpmbuild_logname.getBytes();
			try {
						BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
									logfilename)); 

						for (int i = 0; i <buf.length; i++) {
							os.write(buf[i]);
						}

						os.close();
					} catch (Exception e) {
						String throw_message = Messages.getString("RPMCore.Error_creating__1") + //$NON-NLS-1$
						    logfilename + Messages.getString("RPMCore._nCheck_permissions__2"); //$NON-NLS-1$ 
						IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
								null);
						throw new CoreException(error);
					}
		}
		/**
		 * Method createRPMrpmrc.
		 * Create the RPM .rpmrc resource file for use by rpmbuild
		 * @param rpmrc contains a string of where the .rpmrc file should be written
		 * @return boolean - return true if able to create .rpmrc,
		 *       throw CoreException if not
		 */
		/******************************************************************************/
		public boolean createRPMrpmrc(String rpmrc) throws CoreException {
			if (debug) {
				System.out.println("createRPMrpmrc   " + rpmrc); //$NON-NLS-1$
			}

			String is = "include: /usr/lib/rpm/rpmrc" + line_sep + //$NON-NLS-1$
				"macrofiles:     /usr/lib/rpm/macros:/usr/lib/rpm/%{_target}/macros:" + //$NON-NLS-1$
				"/etc/rpm/macros.specspo:/etc/rpm/macros.db1:/etc/rpm/macros:" + //$NON-NLS-1$
				"/etc/rpm/%{_target}/macros:~/.rpm_macros:" + rpmdirs_path + //$NON-NLS-1$
				"/.rpm_macros" + line_sep; //$NON-NLS-1$

			byte[] buf = is.getBytes();

			try {
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
					rpmrc));

				for (int i = 0; i < buf.length; i++) {
					os.write(buf[i]);
				}

				os.close();
			} catch (IOException e) {
				String throw_message = Messages.getString(
						"RPMCore.Problem_creating_the_.rpmrc_file.__Check_file_permissions_in__217") + //$NON-NLS-1$
						rpmdirs_path;
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}

			return true;
		}

		/**
		 * Method createRPMmacros.
		 * Create the .rpm_macros resource file for use by rpmbuild
		 * @param rpm_macros contains a string with a path to where the
		 * macros file should be written
		 * @return boolean - return true if able to create .rpmmacros,
		 *        throw CoreException if not
		 */
		/******************************************************************************/
		public boolean createRPMmacros(String rpm_macros) throws CoreException {
			if (debug) {
				System.out.println("createRPMmacros: rpm_macros = " + rpm_macros); //$NON-NLS-1$
			}

			String is = "%_topdir               " + rpmdirs_path + line_sep + //$NON-NLS-1$
				"%_vendor             redhat" + line_sep + "%_dbpath             " + //$NON-NLS-1$ //$NON-NLS-2$
				rpmdirs_path + line_sep + "%_tmppath         " + rpmdirs_path + //$NON-NLS-1$
				line_sep + "%_unpackaged_files_terminate_build 0" + line_sep; //$NON-NLS-1$

			byte[] buf = is.getBytes();

			// Read the input stream and try to create the .rpm_macros file
			try {
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
							rpm_macros));

				for (int i = 0; i < buf.length; i++) {
					os.write(buf[i]);
				}

				os.close();
			} catch (FileNotFoundException e) {
				String throw_message = Messages.getString(
						"RPMCore.Problem_creating_the_.rpmmacros_file._nCheck_file_permissions_in__226") + //$NON-NLS-1$
						rpmdirs_path;
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			} catch (IOException e) {
				String throw_message = Messages.getString(
						"RPMCore.Problem_creating_the_.rpmmacros_file._nCheck_file_permissions_in__228") + //$NON-NLS-1$
						rpmdirs_path;
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}
			return true;
		}

		/**
		* Method deleteRPMworkarea.
		* This method deletes all files and subdirectories under path_to_delete.
		* @param path_to_delete - the starting directory
		* @return true if successful, false if not
		*/
		private boolean deleteRPMworkarea(File path_to_delete) {
			if (path_to_delete.isDirectory()) {
				String[] subfiles = path_to_delete.list();

				for (int i = 0; i < subfiles.length; i++) {
					boolean success = deleteRPMworkarea(new File(path_to_delete,
								subfiles[i]));

					if (!success) {
						return false;
					}
				}
			}

			// If we made it to here, the directory must be empty so we can delete it
			return path_to_delete.delete();
		}
		/**
			 * Method deleteRPMresources.
			 * Delete the directories created for the purpose of building an RPM
			 * @param path - contains a string with a path to where the resources are
			 * @return boolean - true if successful, false if not
			 */
			/***************************************************************************/
			public boolean deleteRPMresources(String path) throws CoreException {
				if (debug) {
					System.out.println("deleteRPMresources"); //$NON-NLS-1$
				}

				File path_to_delete = new File(path);

				// Call the recursive method to delete a directory tree
				if (debug) {
					System.out.println("--calling deleteRPMworkarea"); //$NON-NLS-1$
				}

				if (!deleteRPMworkarea(path_to_delete)) {
					String throw_message = Messages.getString(
							"RPMCore.Error_deleting_resources.__Check_file_permissions_in__483") + //$NON-NLS-1$
							rpmdirs_path;
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
							null);
					throw new CoreException(error);
				}

				return true;
			}

	
	/**
	 * @param string
	 */
	public void setRpm_shell(String string) {
		rpm_shell = string;
	}

	/**
		 * Method setRpmbuild_logname.
		 * Set the name of the RPM build log.  This gets a little tricky since the name
		 * of the log iis based on the version/release of the rpm.  If the developer has entered
		 * a version/release, then those values are used as part of the name.
		 */
		/******************************************************************************/
	public void setRpmbuild_logname() {
		SimpleDateFormat df2 = new SimpleDateFormat("MMdd_HHmm"); //$NON-NLS-1$
		Date today = new Date();
		String logname_version = rpm_version;
		String logname_release = rpm_release;
		if (!ui_ver_no.equals("")) {
			logname_version = ui_ver_no;
		}
		if (!ui_rel_no.equals("")) {
			logname_release = ui_rel_no;
		}
		rpmbuild_logname = wksp_path + file_sep + rpm_name + "-" + //$NON-NLS-1$
				logname_version + "-" + logname_release + "-" + //$NON-NLS-1$ //$NON-NLS-2$
				df2.format(today) + ".rpmbuild.log"; //$NON-NLS-1$
	}
	/**
	 * Method executeRPMbuildprep.
	 * This method creates a shell script with the 'rpmbuild -bp' command that takes the sources
	 * peviously installed in the RPM work area, untars them into the BUILD directory and applies
	 * all of the patches specified in the spec file.
	 * @return String - path to the original srpm that was copied into the work area
	 */
	/******************************************************************************/
	public String executeRPMbuildprep() throws CoreException {
		if (debug) {
			System.out.println("Export executeRPMbuildprep"); //$NON-NLS-1$
		}

		boolean cmd_stat;
		String orig_proj_path = proj_path;
		// Get the path to the spec file directory to use
		String specdir = rpmdirs_path + file_sep + "SPECS" + file_sep; //$NON-NLS-1$
		File f = new File(specdir);

		if (!f.isDirectory()) {
			String throw_message = Messages.getString(
					"RPMCore.There_is_not_a__360") + specdir + //$NON-NLS-1$
				Messages.getString(
					"RPMCore._directory._nCheck_permissions_in_the_path_directories._361"); //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		FilenameFilter only = new OnlyExt("spec"); //$NON-NLS-1$
		String[] s = f.list(only);

		/* Get ready to execute the rpmbuild command to do a "build prep" which
		 *  will untar the source into a directory under BUILD and apply all patches
		 *  specified in the "spec" file.
		 */
		String build_opt = "-bp"; //$NON-NLS-1$

		String rpmbuild_cmd = "/usr/bin/rpmbuild " + build_opt + //$NON-NLS-1$
			" -v --rcfile " + rpmrc + " --nodeps" + " " + specdir + s[0]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			LinuxShellCmds.createLinuxShellScript(rpmbuild_cmd, rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
		} catch (CoreException e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		// Need to figure out what the name of the directory under the BUILD dir is
		File build_dir = new File(rpmdirs_path + file_sep + "BUILD" + file_sep); //$NON-NLS-1$
		String[] build_dir_list = build_dir.list();
		diff_old_dir = build_dir_list[0];
		diff_new_dir = diff_old_dir + ".new"; //$NON-NLS-1$ 
		orig_srpm_path = build_dir + file_sep + build_dir_list[0];
		copied_proj_path = orig_srpm_path + ".new"; //$NON-NLS-1$

		String chmod_command = "/bin/chmod -R u+rw " + orig_srpm_path + file_sep; //$NON-NLS-1$ //$NON-NLS-2$
		String cp_cmd1 = "(cd " + orig_srpm_path + line_sep + "/bin/cp -rp . " + //$NON-NLS-1$ //$NON-NLS-2$
			copied_proj_path;
		String cp_cmd2 = "(cd " + proj_path + line_sep + "/bin/cp -rp . " + //$NON-NLS-1$ //$NON-NLS-2$
			copied_proj_path;
		String make_cmd = "(cd " + orig_srpm_path + line_sep + "/usr/bin/make "; //$NON-NLS-1$ //$NON-NLS-2$
			// Make a copy of the tree under BUILD for patch creation
		try {
			orig_proj_path = orig_srpm_path;
			executeProjConfigure(orig_proj_path);
			LinuxShellCmds.createLinuxShellScript(make_cmd,rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell,0);
//				  copyDirTree(orig_srpm_path, copied_proj_path);
//				  copyDirTree(proj_path, copied_proj_path);
			LinuxShellCmds.executeLinuxCommand(chmod_command, 0);
			LinuxShellCmds.createLinuxShellScript(cp_cmd1, rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell,0);
			LinuxShellCmds.createLinuxShellScript(cp_cmd2,rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell,0);
			} catch (Exception e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}

		return orig_srpm_path;
	}
	
	/**
	 * Method executeRPMBuild.
	 * This method execs the rpmbuild shell script to build either a binary or source
	 * RPM
	 * @param rpm_type - type of RPM to handle -	"-ba" for both source and binary
	 * 																			"-bs" for just a source rpm
	 * 																			"-bb" for just binary rpm(s)
	 * @param rpm_spec is a String with the path to the specfile
	 * @return boolean - returns if successful, throw CoreException if not
	 */
	/******************************************************************************/
	public void executeRpmBuild(String rpm_opt, String rpm_spec) throws CoreException {
		if (debug) {
			System.out.println("executeRpmBuild -- type = " + rpm_opt); //$NON-NLS-1$
		}

		String rpmbuild_cmd = "/usr/bin/rpmbuild " + rpm_opt + //$NON-NLS-1$
			" -v --nodeps --rcfile " + rpmrc + " " + rpm_spec; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			LinuxShellCmds.createLinuxShellScript(rpmbuild_cmd, rpmbuild_logname, rpm_shell);

			// Execute the rpmbuild shell script to try and create an RPM
			LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
		} catch (CoreException e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1,throw_message,null);
			preserve = true;;
			throw new CoreException(error);
		}
		return;
	}
	/**
		 * Method copyRpms.
		 * This method copies the binary and/or source rpm's created during the
		 * rpmbuild process back to the Eclipse project
		 * @param rpm_type - type of RPM to handle - "-ba", "-bs" or "-ba"
		 * @return - returns if successful, throw CoreException if not
		 */
		/******************************************************************************/
	public String copyRpms(String rpm_opt) throws CoreException {
		if (debug) {
			System.out.println("copyRpms"); //$NON-NLS-1$
		}
		String path_to_src_rpm = ""; //$NON-NLS-1$
		/* Copy the source rpm(s) back to the original project  */
		if (rpm_opt.equals("-bs") | rpm_opt.equals("-ba")) { //$NON-NLS-1$  //$NON-NLS-2$

			File dir = new File(rpmdirs_path + file_sep + "SRPMS"); //$NON-NLS-1$
			String[] dirlist = dir.list();
			String from_file = rpmdirs_path + file_sep + "SRPMS" + file_sep + dirlist[0]; //$NON-NLS-1$
			path_to_src_rpm = proj_path + file_sep + dirlist[0];
			rpm_name = dirlist[0];

			// If the destination source rpm file already exists, delete it
			File f1 = new File(path_to_src_rpm);

			if (f1.exists()) {
				if (!f1.delete()) {
					String throw_message = Messages.getString(
							"RPMCore.Unable_to_delete_file__582") + f1 + //$NON-NLS-1$
						Messages.getString(
							"RPMCore._nCheck_permissions_in_the_project._583"); //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
					preserve = true;;
					throw new CoreException(error);
				}
			}

			try {
				copyFile(from_file, path_to_src_rpm);
			} catch (Exception e) {
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						Messages.getString(
							"RPMCore.Error_trying_to_copy__") + //$NON-NLS-1$
						from_file +
						Messages.getString("RPMCore._to__") + //$NON-NLS-1$
						path_to_rpm, null);
				throw new CoreException(error);
			}
		}

		/* Get the name of the RPM out of the directory to copy
		* Need to figure out what the name of the directory under the RPMS dir is
		*so we can get a path so we can copy the created RPM
		*/
		if (rpm_opt.equals("-ba") | rpm_opt.equals("-bb")) {  //$NON-NLS-1$ //$NON-NLS-2$
			File dir = new File(rpmdirs_path + file_sep + "RPMS" + file_sep); //$NON-NLS-1$
			String[] dirlist = dir.list();
			File dir2 = new File(rpmdirs_path + file_sep + "RPMS" + file_sep + dirlist[0]); //$NON-NLS-1$
			String[] dirlist2 = dir2.list();

			// Copy all of the rpms back to the project
			for (int i = 0; i < dirlist2.length; i++) {
				String from_file = rpmdirs_path + file_sep + "RPMS" + file_sep + dirlist[0] + //$NON-NLS-1$
					file_sep + dirlist2[i];

				try {
					copyFile(from_file, proj_path + file_sep + dirlist2[i]);
				} catch (Exception e) {
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							Messages.getString(
								"RPMCore.Error_trying_to_copy__") + //$NON-NLS-1$
							from_file +
							Messages.getString("RPMCore._to__") + //$NON-NLS-1$
					proj_path + file_sep + dirlist2[i], null);
					preserve = true;;
					throw new CoreException(error);
				}
			}
		}

		return path_to_src_rpm;
	}
	
	/**
	 * Method to see if this is the first tme a project has been exported
	 * as a source RPM.
	 * @param projpath is a string containing the path to the project 
	 * @return true if this is the first time (.srpminfo does not exist), false if not
	 */
	public boolean firstSRPM(String projpath) {
		/* Check to see if there a .srpminfo file, if not
		 * this project was not imported from a source rpm. So, copy
		 * the spec file from the work area to the project.
		 */
		File f = new File(projpath + srpm_info_name);

		if (f.exists()) {
			return false;
		}

		return true;
	}
	
	/**
	  * Method createSRPMinfo.
	  * This method creates the ".srpminfo" file in the project to store information
	  * so that if this project is ever to be re-exported as a binary and/or source
	  * we will know where the original source RPM is so we can generate patches.
	  * @param specfile - String containing spec file name
	  * @param path_to_srpm - String containing the path to the source rpm
	  *                                     for this project
	  * @return if successful, throw CoreException if not
	  * @throws CoreException
	  */
	public void createSRPMinfo(String specfile, String path_to_rpm)
		throws CoreException {
		if (debug) {
			System.out.println("--createSRPMinfo"); //$NON-NLS-1$
		}

		SimpleDateFormat df = new SimpleDateFormat("E MMM dd yyyy  HH:mm"); //$NON-NLS-1$
		Date today = new Date();
		String srpm_info_file =
			"####  DO NOT DELETE THIS FILE IF YOU EVER WANT TO RE-EXPORT THIS " + //$NON-NLS-1$
			"PROJECT AS A BINARY OR SOURCE RPM  ####" + line_sep + path_to_rpm + //$NON-NLS-1$
			line_sep + "Date: " + df.format(today); //$NON-NLS-1$ 
		long file_size = generateChecksum(proj_path, 0);

		String new_srpm_info_file = proj_path + srpm_info_name;
		srpm_info_file = srpm_info_file + line_sep + "Specfile: " + specfile + //$NON-NLS-1$
			line_sep + Messages.getString("RPMCore.Checksum___32") + String.valueOf(file_size); //$NON-NLS-1$

		byte[] buf = srpm_info_file.getBytes();

		/* Read the input stream and try to create the spec file to be used by
		 *  the rpmbuild process                 */
		try {
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
						new_srpm_info_file));

			for (int i = 0; i < buf.length; i++) {
				os.write(buf[i]);
			}

			os.close();
		} catch (Exception e) {
			String throw_message = Messages.getString("RPMCore.Error_creating_srpminfo_file_in_the_project._9") + //$NON-NLS-1$
			  Messages.getString("RPMCore._nCheck_permissions_in__10") + proj_path; //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		return;
	}
	/**
	 * Method installRPMsource.
	 * This method installs the source RPM into the "buildroot" area so we can capture it
	 * for use in importing the project.
	 * @param path - contains a string to the source RPM to install
	 * @return boolean - returns true if successful, throws CoreException if not
	 */
	/******************************************************************************/
	public boolean installRPMsource(String path) throws CoreException {
		if (debug) {
			System.out.println("installRPMsource"); //$NON-NLS-1$
		}

		String rpm_cmd = "/bin/rpm -i -v --rcfile " + rpmrc + " " + //$NON-NLS-1$ //$NON-NLS-2$
			path;

		try {
			LinuxShellCmds.createLinuxShellScript(rpm_cmd, rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
		} catch (CoreException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_trying_to_install_the_source_with_this_command__355") + //$NON-NLS-1$
				rpm_cmd +
				Messages.getString("RPMCore._nCheck_the_log_at__356") + //$NON-NLS-1$
					rpmbuild_logname;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		return true;
	}
	
	/**
	 * Method generateSRPMpatch will take a path to an Eclipse C/C++
	 * project and copy it to a temp area, run "make clean" and install
	 *  the SRPM this project was created from and generate a patch
	 *  of the differences for inclusion for the new SRPM.
	 * @param ver_no String containing the version number of this SRPM
	 * @param patch_tag String containing a unique identifier for the patch
	 * @return a long integer indicating the size of the patch,
	 *       throw CoreException if there are any errors
	 */
	public long generateSRPMpatch(String ver_no,
		String patch_tag) throws CoreException {
		if (debug) {
			System.out.println("generateSRPMpatch"); //$NON-NLS-1$
			System.out.println("---patch tag: " + patch_tag); //$NON-NLS-1$
		}
		String patch_name = rpm_name + "-" + ver_no + "-" + //$NON-NLS-1$ //$NON-NLS-2$
			 patch_tag + ".patch";  //$NON-NLS-1$

		boolean cmd_stat;

		/* Now that the original source rpm has been installed in the rpm workarea,
		* The source has already been copied from the Eclipse project and has a
		* ".new" appended to the end.
		*/
		String chmodcommand = "/bin/chmod -R u+r " + rpmdirs_path +  //$NON-NLS-1$
				file_sep + "BUILD" + file_sep; //$NON-NLS-1$

		try {
			LinuxShellCmds.executeLinuxCommand(chmodcommand, 0);
		} catch (CoreException e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		try {
			checkForMakefile(copied_proj_path);
		} catch (CoreException e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		/*  Run a "make clean" and/or "make distclean" on the previously installed source
		 * A bit of explanation here.  There probably is a question of why we are doing an
		 * "rpmbuild -bc" (which does a build/compile) when we install the previously-
		 * installed source RPM instead of an "rpmbuild -bp" which just installs the source.
		 * We have found that a *lot* of "Makefiles" in the source RPMs do not properly
		 * "clean-up" the directory tree when a "make distclean" is done.  Sometimes files
		 * created by "autoconf" and "aclocal" get left behind.  So, we perform the same
		 * procedure on the previous source RPM as we do the source RPM we imported,
		 * "rpmbuild -bc" and then "make distclean".  This way the same files get created so
		 * when we run the "diff" command on the previous and the to-be-exported source
		 * RPM only the differences that were edited by the developer are picked up.
		 */
		 String make_cmd = "(cd " + copied_proj_path + line_sep + "/usr/bin/make "; //$NON-NLS-1$ //$NON-NLS-2$
		try {
			// need to make sure that "make" has been run on the Eclipse project at least
			// once before doing the "diff" command so we "compare apples to apples"
			LinuxShellCmds.createLinuxShellScript(make_cmd, rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell,0);
			executeMakeClean(copied_proj_path);
			deleteEclipseFiles(copied_proj_path, rpm_name);
			File f = new File(copied_proj_path);
			deleteSRPMextrafiles(f);
			executeMakeClean(orig_srpm_path);
			deleteEclipseFiles(orig_srpm_path, rpm_name);
			f = new File(orig_srpm_path);
			deleteSRPMextrafiles(f);
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		// Now, get the directory names under the "BUILD" directory and run the "diff"
		// command to generate the patch

		/* It makes a difference when using the "diff" command which directory comes first and
		 * which directory comes second in the command.  We want the original to come first
		 * and we have appended a ".new" to the Eclipse project that was copied over.
		 */
		String diff_cmd = "(cd " + rpmdirs_path + file_sep + "BUILD" + line_sep + "diff -uNr " + //$NON-NLS-1$ //$NON-NLS-2$
			"--ignore-matching-lines=POT-Creation-Date --exclude=autom4te.cache " + //$NON-NLS-1$ 
			// skip the fields that are dynamically filled in or created by autoconf
			diff_old_dir + " " + diff_new_dir + " > " + rpmdirs_path + //$NON-NLS-1$ //$NON-NLS-2$
			file_sep + "SOURCES" + file_sep + patch_name + " )" + line_sep; //$NON-NLS-1$ //$NON-NLS-2$
		File f = new File(rpmdirs_path + file_sep + "SOURCES" + file_sep + patch_name); //$NON-NLS-1$
		// Execute the diff_cmd shell script to create a patch file
		try {
			LinuxShellCmds.createLinuxShellScript(diff_cmd, rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell, 1);
		} catch (CoreException e) {
			
			if (!f.exists() | f.length() == 0) {
				return 0;
			} else {
				return -1;} 
			}
		return f.length();
	}
	
	/**
	   * This small class implements the FilenameFilter method.  It is
	   * used to return all files ending with ".spec" from the SPECS
	   * directory
	   */
	public class OnlyExt implements FilenameFilter {
		String ext;

		/**
		 * Method OnlyExt.
		 * This method accepts a string to be used as a filter.  Only files with names ending
		 * in .'ext' will be returned.
		 * @param ext - string containing the file extension to use as a filter
		 */
		public OnlyExt(String ext) {
			this.ext = "." + ext; //$NON-NLS-1$
		}

		/**
		 * @see java.io.FilenameFilter#accept(File, String)
		 */
		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}

	/**
	 * @return
	 */
	public static String getError() {
		return Error;
	}

	/**
	 * @return
	 */
	public boolean isGenerate_patch() {
		return generate_patch;
	}

	/**
	 * @return
	 */
	public String getPath_to_rpm() {
		return path_to_rpm;
	}

	/**
	 * @return
	 */
	public String getPath_to_specfile() {
		return path_to_specfile;
	}

	/**
	 * @return
	 */
	public String getProj_dir() {
		return proj_dir;
	}

	/**
	 * @return
	 */
	public String getProj_path() {
		return proj_path;
	}

	/**
	 * @return
	 */
	public String getRpm_macros() {
		return rpm_macros;
	}

	/**
	 * @return
	 */
	public String getRpm_name() {
		return rpm_name;
	}

	/**
	 * @return
	 */
	public String getRpm_release() {
		return rpm_release;
	}

	/**
	 * @return
	 */
	public String getRpm_spec() {
		return rpm_spec;
	}

	/**
	 * @return
	 */
	public String getRpm_version() {
		return rpm_version;
	}

	/**
	 * @return
	 */
	public String getRpmbuild_logname() {
		return rpmbuild_logname;
	}

	/**
	 * @return
	 */
	public String getRpmdirs_path() {
		return rpmdirs_path;
	}

	/**
	 * @return
	 */
	public String getRpmrc() {
		return rpmrc;
	}

	/**
	 * @return
	 */
	public String getSpec_file_name() {
		return spec_file_name;
	}

	/**
	 * @return
	 */
	public String getSpec_file_prefix() {
		return spec_file_prefix;
	}

	/**
	 * @return
	 */
	public String getSrpm_info_name() {
		return srpm_info_name;
	}

	/**
	 * @return
	 */
	public String getWksp_path() {
		return wksp_path;
	}

	/**
	 * @param string
	 */
	public void setRpm_release(String string) {
		rpm_release = string;
	}

	/**
	 * @param string
	 */
	public void setRpm_version(String string) {
		rpm_version = string;
	}

	/**
	 * @param string
	 */
	public void setSpec_file_name(String string) {
		spec_file_name = string;
	}

	/**
	 * @return
	 */
	public boolean isChk_sum_diff() {
		return chk_sum_diff;
	}

	/**
	 * @return
	 */
	public String getRpm_shell() {
		return rpm_shell;
	}

	/**
	 * @param string
	 */
	public void setPath_to_specfile(String string) {
		path_to_specfile = string;
	}

	/**
	 * @param string
	 */
	public void setUi_rel_no(String string) {
		ui_rel_no = string;
	}

	/**
	 * @param string
	 */
	public void setUi_ver_no(String string) {
		ui_ver_no = string;
	}

}
