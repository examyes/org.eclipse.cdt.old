/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.cdt.rpm.core;

import org.eclipse.cdt.rpm.core.RPMCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.net.UnknownHostException;

/**
  *This class is used to manipulate RPM spec files for Linux systems
  */
public class SpecFileOps {

//	When debug is set to true, lots of debug statements are printed.
	private static final boolean debug = false;
	private static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	private static final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String Error = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
	private static int[] line_ptr = { 0, 0, 0, 0 };
	private int pat_ctr = 1;
	private String prev_ver_no = ""; //$NON-NLS-1$
	private String new_rpm_version = ""; //$NON-NLS-1$
	private String proj_path;
	private String rpmdirs_path;
	private String wksp_path;
	private String proj_dir;
	 
	public SpecFileOps(String c_proj_path, String c_rpmdirs_path, 
		String c_wksp_path, String c_proj_dir) 
				throws CoreException {
		proj_path = c_proj_path;
		rpmdirs_path = c_rpmdirs_path;
		wksp_path = c_wksp_path;
		proj_dir = c_wksp_path;
	}

	/**
	 * This method modifies a previous spec file from a source RPM install
	 * adding the appropriate statements to it so the changes made to the
	 * C/C++ project via a patch file that will be generated.
	 * @param String ver_no is the version number from the GUI to place into the spec file
	 * @param rel_no is the release number from the GUI to place into the spec file
	 * @param rpm_version is the current version of the rpm
	 * @param rpm_release is the current release of the rpm
	 * @param patch_tag is the name associated with the patch, it is used to
	 *      create a unique name for the patch of the form:
	 *       packagename-ver_no-patch_tag.patch
	 * @param changelog_entry - string from the GUI to place in the %changelog section
	 * @param rpm_name is the name of the rpm we are working with
	 * @param path_to_specfile is the path to the specfile to change with the new info
	 * @ return returns true if successful, throws CoreException if not
	 */
	public String changeRPMspecfile(String ver_no, String rel_no, String rpm_version,
		String rpm_release, String patch_tag, String changelog_entry, String rpm_name,
		String path_to_specfile)
		throws CoreException, FileNotFoundException {
		if (debug) {
			System.out.println("changeRPMspecfile" + line_sep + "---ver_no = " + ver_no + //$NON-NLS-1$ //$NON-NLS-2$
				line_sep + "---rel_no = " + rel_no +  //$NON-NLS-1$ 
				line_sep + "---patch_tag = " + patch_tag  + //$NON-NLS-1$
				line_sep + "---path_to_specfile = " + path_to_specfile); //$NON-NLS-1$
		}
		String patch_name;
		// If the versions have changed, we must later recreate the source 
		// tarball with the new version
		if (!ver_no.equals(rpm_version)) {
			prev_ver_no = rpm_version;
			new_rpm_version = ver_no;
		}
		if (!patch_tag.equals("")) { //$NON-NLS-1$
		   patch_name = rpm_name + "-" + ver_no + "-" +  //$NON-NLS-1$ //$NON-NLS-2
			 patch_tag + ".patch";  //$NON-NLS-1$
		} else {
			patch_name = ""; //$NON-NLS-1$
		}

		// Check to make sure the patch name is unique
		if (!checkPatch(patch_name) & !patch_name.equals("")) { //$NON-NLS-1$
			String throw_message = Messages.getString(
					"RPMCore.The_patch_name__109") + patch_name + //$NON-NLS-1$
				Messages.getString("RPMCore._is_not_unique._110") + //$NON-NLS-1$
				Messages.getString(
					"RPMCore._nPlease_modify_the___Patch_tag___field_and_try_again._111"); //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		try {
			parseSpecfile(path_to_specfile);
		} catch (CoreException e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		// Figure out the format of the lines to add
		String[] add_line = {
			"Patch" + pat_ctr + ": " + patch_name + line_sep, //$NON-NLS-1$ //$NON-NLS-2$
			"%patch" + pat_ctr + " -p1 -b ." + patch_tag + line_sep //$NON-NLS-1$ //$NON-NLS-2$ 
		};
        
		// If there were %defines for Version and/or Release, setup replacement statements

		/* The logic here is if we did not find a "Patchx:" statement(line_ptr[1]=0) then
		 * this is the first patch for this source RPM and the "Patch1:" statement will be
		 * added after the last "Sourcex:" statement(line_ptr[0] and the "%patch1"
		 * statement will be added after the "%setup" statement where line_ptr[2] is
		 * pointing.  If a "Patchx" statement  was found, (line_ptr[1] != 0), add the new
		 * "Patchx:"line after the last "Patchx:" statement pointed to by line_ptr[1] and
		 * add the "%patchx" statement after the last "%patchx" statement indicated by
		 * line_ptr[3].
		 */
		if (line_ptr[1] == 0) {
			line_ptr[1] = line_ptr[2];
		} else {
			line_ptr[0] = line_ptr[1];
			line_ptr[1] = line_ptr[3];
		}

		// Now read the spec file line by line and write it to the final 
		// spec file adding in the lines to perform the patching.
		String path_to_newspecfile = path_to_specfile + ".new"; //$NON-NLS-1$

		FileReader fr = new FileReader(path_to_specfile);
		BufferedReader br = new BufferedReader(fr);
		FileWriter fw;

		try {
			fw = new FileWriter(path_to_newspecfile);
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Failed_to_open_the_output_spec_file_at__123") + //$NON-NLS-1$
					path_to_newspecfile;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		int line_ctr = 0;
		int line_indx = 0;
		String input_line;
		boolean found_changelog = false;

		// Setup the lines that set the version and release numbers
		String new_version_line = "Version: " + ver_no; //$NON-NLS-1$
		String new_release_line = "Release: " + rel_no; //$NON-NLS-1$

		try {
			while ((input_line = br.readLine()) != null) {
				if (input_line.length() > 8) {
					if (input_line.startsWith("Version")) { //$NON-NLS-1$
						input_line = new_version_line;
					} else if (input_line.startsWith("Release")) { //$NON-NLS-1$
						input_line = new_release_line;
					}
				}

				fw.write(input_line + line_sep);

				// See if this was the "%changelog" line just written, if it was, write out the new entry
				if (input_line.length() == 10 && !patch_name.equals("")) { //$NON-NLS-1$
					if (input_line.startsWith("%changelog")) { //$NON-NLS-1$
						fw.write(changelog_entry);
						found_changelog = true;
					}
				}

				line_ctr++;

				// Check to see if this is one of the lines I should add something after
				if (!patch_name.equals("")) { //$NON-NLS-1$
				   if (line_ctr == line_ptr[line_indx]) {
					   fw.write(add_line[line_indx]);
					   line_indx++;
				   }
				}
			}

			// if there was not a "%changelog" section, make one
			if (!found_changelog && !patch_name.equals("")) { //$NON-NLS-1$
				fw.write("%changelog" + line_sep + changelog_entry); //$NON-NLS-1$
			}

			fw.close();
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_trying_to_modify__132") + //$NON-NLS-1$
				path_to_specfile;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		return path_to_newspecfile;
	}

	/**
	 * Method parseSpecfile.
	 * This method takes a spec file path and parses it for various information.
	 * @param String containing the path to the spec file to be parsed
	 * @return simply return if successful, throw CoreEception if not
	 */
	public void parseSpecfile(String path_to_specfile) throws CoreException {
		if (debug) {
			System.out.println("--parseSpecfile: " + path_to_specfile); //$NON-NLS-1$
		}
		for (int i = 0; i < line_ptr.length; i++) {
			line_ptr[i] = 0;
		}
		/* The following logic determines where in the spec file the "Patchx:" and
		 * %patchx -p1" lines will need to be added to accomodate the patch we
		 * are fixing to generate.  If this is the first patch to ever be added to this
		 * source RPM then the "Patchx: statement will have to be added after the
		 * last "Sourcex:" statement and the "%patch -p1" statement will need to be
		 * added after the "%setup" statement.  If this is not the first patch for this
		 * source rpm, the "Patchx:" statement will be added after the last "Patchx:"
		 * statement and the "%patchx -p1" will be added after the last "%patch -p1"
		 * statement.  So, we keep track of where the line numbers for all of these
		 * eventualities are so when we mod the file we will know where to insert
		 * the necessary new lines.
		 */
		ArrayList patchlist = new ArrayList();
		boolean found_source_line = false;
		boolean found_patch = false;
		boolean found_define = false;
		boolean found_define_name = false;
		boolean found_version = false;
		boolean found_release = false;
		int define_ctr = 0;
		int define_line_ctr = 0;
		int lines = 1;

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
            
			String new_word;
			boolean check_ifs = false;
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
					
/* The following commented out logic addresses bugzilla 110452 where the version and
 * release numbers for spec files are stored in "%define" variables at the top of the file.  It
 * has been decided to put this change on hold until it can be determined how pervasive
 * the use of this practice is.  The code is incomplete for the time being and may be deleted
 * entirely in future releases.
 */                   
/*					if (found_version) {
						found_version = false;
						if (new_word.startsWith("%{")) {  //$NON-NLS-1$
							version_param = true;
							define_info.add(0,new_word.substring(2,new_word.length()-1));
						}
						break;
					}
                    
					if (found_release) {
						found_release = false;
						if (new_word.startsWith("%{")) {  //$NON-NLS-1$
//							release_param = true;
							define_info.add(1,new_word.substring(2,new_word.length()-1));
						}
						break;
					}  */
                    
					// See if we have found the Version: line
					if (new_word.equals("Version:")) {  //$NON-NLS-1$
						found_version = true;
						break;
					}
                    
					// See if we have found the Release: line
					if (new_word.equals("Release:")) {  //$NON-NLS-1$
						found_release = true;
						break;
					}

						// Record where the last line of the form "Sourcex:" is
						if (new_word.startsWith("Source") &  //$NON-NLS-1$
							 new_word.endsWith(":")) { //$NON-NLS-1$
							line_ptr[0] = lines;
							found_source_line = true;
							break;
						}

						/* Record where the last line of the form "Patchx:" is and count how many there were.
						 * Also, record the statement so when we generate our new "Patchx:" statement
						 * we don't duplicate a "Patch" statement.  This has to be done because a lot of
						 * spec files have "Patchx:" statements that are non-sequential
						 */
						if (new_word.startsWith("Patch") &  //$NON-NLS-1$
							   new_word.endsWith(":")) { //$NON-NLS-1$
							line_ptr[1] = lines;
							pat_ctr++;
							patchlist.add(new_word);

							break;
						}

						// Record where the "%setup line is
						if (new_word.equals("%setup")) { //$NON-NLS-1$

							// set the "check for if" constructs switch
							check_ifs = true;
							line_ptr[2] = lines;

							break;
						}

						if (new_word.equals("%build")) { //$NON-NLS-1$
							check_ifs = false;
                            
							break;
						}

						// Record where the last (if any) "%patchx" line is
						if (new_word.startsWith("%patch")) { //$NON-NLS-1$
							line_ptr[3] = lines;
							found_patch = true;

							break;
						}
                        
						// See if we have found a %define statement, if so save it as some
						// source RPMs use %define statements to "define" version/release #'s
/* See the comment several lines above regarding bugzilla 110452 as it also pertains to this code */
/*						if (new_word.equals("%define")) {  //$NON-NLS-1$
							found_define = true;
							define_line_ptr[define_line_ctr] = lines;
							define_line_ctr++;
                        	
							break;
						}  */
                        
					if (found_define) {
						found_define = false;
//						define_info.add(define_ctr,new_word);
						define_ctr++;
						found_define_name = true;
						break;
					}
                    
					if (found_define_name) {
						found_define_name = false;
//						define_info.add(define_ctr,new_word);
						define_ctr++;
						break;
					}

						// Set the found %if/%ifarch/%ifnarch/%ifos/%ifnos switch
						if (check_ifs) {
							if (new_word.startsWith("%if")) { //$NON-NLS-1$
								if_ctr++;

								break;
							}

							// Reset the found %if/%ifarch switch
							if (new_word.equals("%endif")) { //$NON-NLS-1$

								if ((if_ctr > 0) & found_patch) {
									if_ctr--;
									line_ptr[3] = lines;
									found_patch = false;

									break;
								}
							}

							break;
						}
                        
						break;

				default:
					break;
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

		if (pat_ctr > 1) {
			int patch_num = parsePatchArray(patchlist, pat_ctr);
			pat_ctr = patch_num;
		}

		return;
	}
	
	/**
		* Method checkPatch is used to check for the existence of a previous
		* patch name in the "SOURCES" directory.
		* @param patch name formed from the patch tag from the GUI
		* @return true if the name unique, false if name already exists
		*/
	   private boolean checkPatch(String patch_name) {
		   if (debug) {
			   System.out.println("--checkPatch"); //$NON-NLS-1$
		   }
                                                                                                
		   File f = new File(rpmdirs_path + file_sep + "SOURCES" + file_sep + patch_name); //$NON-NLS-1$
                                                                                                
		   if (f.exists()) {
			   return false;
		   }
                                                                                                
		   return true;
	   }

	/**
	 * Method parseArrayList is used to peruse a list of the "Patchx:"
	 * statements gleaned from a spec file and determine what would
	 * be unique "Patchx:"/"%patchx" statements to add to the spec file.
	 * @param patchlist is an ArrayList containing the "Patchx:" statements
	 *     in the spec file
	 * @param patch_ctr is the number of "Patchx:" statements found in the spec file
	 * @return a number to replace "x" in "Patchx:" and "%patchx" which
	 *     is unique for this spec file
	 *
	 */
	private static int parsePatchArray(ArrayList patchlist, int patch_ctr) {
		if (debug) {
			System.out.println("--parsePatchArrayList"); //$NON-NLS-1$
		}

		int patch_array_size = patchlist.size();
		String num_string;
		int patch_num;
		String last_patch = (String) patchlist.get(patch_array_size - 1);
		int indx = 5;

		while (last_patch.charAt(indx) != ':') {
			indx++;
		}

		// Allow for the fact that there could only be one patch statement of the
		// form "Patch:", that is, there is no number
		if (indx == 5) {
			return 0;
		}

		String num = last_patch.substring(5, indx);

		try {
			patch_num = Integer.parseInt(num, 10);
		} catch (NumberFormatException e) {
			return -1;
		}

		return patch_num + 1;
	}
	
	/**
			 * Method createRPMspec.
			 * Create a *very* generic spec file since no spec file was specfified and
			 * this is the first time this project has been exported.
			 * @param rpm_shell is the name to use when creating a shell script
			 * @param rpmbuild_logname is the name of the file to send log errors to
			 * @param tar_path is the path to the tarball to be used in this specfile
			 * @param rpm_version is the verison number to be placed in the spec file
			 * @param rpm_release is the release number to be placed in the spec file
			 * @param path_to_specfile is the path to write the new spec file to
			 * @param proj_dir is the name of the Eclipse project directory, it will be used
			 *        to create the name of the tarball along with the version number
			 * @return - true if successful, throw CoreException if not
			 */
			/******************************************************************************/
			public boolean createRPMspec(String rpm_shell, String rpmbuild_logname, 
			  String tar_path, String rpm_version, String rpm_release, String path_to_specfile,
			  String proj_dir)
			  throws CoreException {
				if (debug) {
					System.out.println("createRPMspec "); //$NON-NLS-1$
					System.out.println(rpm_shell + "  " + rpmbuild_logname + "  " + tar_path + line_sep + //$NON-NLS-1$
					rpm_version + "  " + rpm_release + "  " + path_to_specfile + line_sep + //$NON-NLS-1$ //$NON-NLS-2$
					proj_dir);
				}
				String author_name = RPMCorePlugin.getDefault().getPreferenceStore()
						.getString("IRpmConstants.AUTHOR_NAME"); //$NON-NLS-1$
				String author_email = RPMCorePlugin.getDefault().getPreferenceStore()
						.getString("IRpmConstants.AUTHOR_EMAIL"); //$NON-NLS-1$
				String user_name = System.getProperty("user.name"); //$NON-NLS-1$
				String user_wksp = wksp_path + file_sep + user_name; 
				ArrayList file_list = new ArrayList();
				String mkdir_cmds = "/usr/bin/make install"; //$NON-NLS-1$

				String make_cmd = "(cd " + tar_path + line_sep + "/usr/bin/make" + line_sep; //$NON-NLS-1$ //$NON-NLS-2$
				
				// Build the project to generate the binaries
				try {
					LinuxShellCmds.createLinuxShellScript(make_cmd, rpmbuild_logname, rpm_shell);
					LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
				} catch (CoreException e) {
					String throw_message = Messages.getString(
							"RPMCore.Problem_running_the___make___file_to_create_the_executables._nView_the_log_file_at__249") + //$NON-NLS-1$
							rpmbuild_logname;
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
							null);
					throw new CoreException(error);
				}
				String user_work_area = RPMCorePlugin.getDefault().getPreferenceStore()
					.getString("IRpmConstants.USER_WORK_AREA"); //$NON_NLS-1$
				String build_root_path = user_wksp + user_work_area + file_sep + 
					proj_dir + "-root"; //$NON-NLS-1$

				/* Now run the 'make install' to install the files into the directory structure
				 * so we can get a list of them and the directories they go into.
				 */

				String make_inst_cmd = "export RPM_BUILD_ROOT=" + //$NON-NLS-1$
					build_root_path + line_sep + mkdir_cmds;

				try {
					LinuxShellCmds.createLinuxShellScript("cd " + tar_path + line_sep + make_inst_cmd, rpmbuild_logname, //$NON-NLS-1$
						rpm_shell);
					LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
				} catch (CoreException e) {
					String throw_message = Messages.getString(
							"RPMCore.Problem_running_the___make_install___shell_script_--__273") + //$NON-NLS-1$
						rpm_shell +
						Messages.getString(
							"RPMCore._nThere_may_be_a_problem_in_the_M/makefile._274"); //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
							null);
					throw new CoreException(error);
				}

				/* Now traverse the "build root" directory to find all of the files installed by
				 * the "make install" above.  This file list will be used to populate the "%files"
				 * section of the spec file.
				 */
				traverse(new File(build_root_path), build_root_path, file_list);

				/* See if there are any files in file_lst, if not, we probably have an error in the install: section
				 * of the Makefile/makefile.
				 */
				if (file_list.size() == 0) {
					String throw_message = Messages.getString(
							"RPMCore.No_files_were_found_under_build_root_--__276") + //$NON-NLS-1$
						build_root_path +
						Messages.getString(
							"RPMCore._n_--_Problem_with_the___install____section_of_the_spec_file__277"); //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
							null);
					throw new CoreException(error);
				}

				// If there is a configure script, put in a %configure
				String configure = ""; //$NON-NLS-1$

				if (LinuxShellCmds.checkForConfigure(proj_path)) {
					configure = "%configure" + line_sep; //$NON-NLS-1$
				}

				// Set up date for "%changelog" entry
				SimpleDateFormat df = new SimpleDateFormat("E MMM dd yyyy"); //$NON-NLS-1$

				//	PatchChangeLogStamp.setText("* " + df.format(today)+ " -- YourName" +" <your@mail.com>");
				// Now set up to build the RPM spec file
				String is = 
					"%define _unpackaged_files_terminate_build 0" + line_sep +  //$NON-NLS-1$
					"Summary: None - Eclipse-generated spec file" + line_sep +  "Name: " + //$NON-NLS-1$ //$NON-NLS-2$
					proj_dir + "" + line_sep +  "Version: " + rpm_version + "" + line_sep +  "Release: " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					rpm_release + "" + line_sep +  "License: GPL" + line_sep +  //$NON-NLS-1$ //$NON-NLS-2$
					"Group: Applications/Internet" + line_sep +  "Source: " + proj_dir + "-" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"%{version}.tar.bz2" + line_sep +  "Requires: tar" + line_sep +  "BuildRoot: " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"%{_tmppath}/%{name}-root" + line_sep +  //$NON-NLS-1$
					"%description" + line_sep + line_sep + //$NON-NLS-1$
					"Basic spec file for rpm build in Eclipse for " + proj_dir + //$NON-NLS-1$
					line_sep + line_sep +  "%prep" + line_sep +  "%setup -q" + line_sep +  "%build" + line_sep + line_sep +  configure + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"make" + line_sep + line_sep +  "%install rm -rf $RPM_BUILD_ROOT" + line_sep + 
					"%makeinstall RPM_BUILD_ROOT=$RPM_BUILD_ROOT" + line_sep +  "%clean" + line_sep +  //$NON-NLS-1$ //$NON-NLS-2$
					"rm -rf $RPM_BUILD_ROOT" + line_sep +  "%files" + line_sep +  //$NON-NLS-1$ //$NON-NLS-2$
					"%defattr(-,root,root)" + line_sep; //$NON-NLS-1$

				// Convert the ArrayList file_list to an array of strings and append to the spec file 
				String[] lines = new String[file_list.size()];
				file_list.toArray(lines);

				for (int i = 0; i < lines.length; i++) {
					/* The following marked code is a hack to get around a problem of the ".gz" being
					 * dropped from some document file names that belong in /usr/share/man.  I'm not sure
					 * why docs whose names are of the form "xxxx.8.gz" are returned from the "traverse
					 * method as "xxxx.8".  Is there a problem with having two periods in the name with java
					 * for some reason?  Using the find command from a shell returns the correct names.
					 * When the exact same find command is embedded in java code and fired off as a shell
					 * script, again the ".gz" gets lost.  I was hoping to solve the problem with the "traverse"
					 * method, but it has the same problem which leads me to believe it is a java-related
					 * problem.  I must move on now, but this hack can be removed if this mystery is solved.
					 *
					 * ************* Beginning of hack ************************************/
					int k = lines[i].length() - 2;
					char last_char = lines[i].charAt(k);

					if (Character.isDigit(last_char) &
							(lines[i].lastIndexOf(file_sep + "man" + file_sep) != -1)) { //$NON-NLS-1$
						lines[i] = lines[i].substring(0, k + 1) + ".gz" + line_sep; //$NON-NLS-1$
					}

					/* ************ End of hack  *****************************************/
					is = is + lines[i];
				}
				Date today = new Date();
				is = is + "%changelog" + line_sep + "* " + returnDate() +
					 " " + author_name + " <" + //$NON-NLS-1$ //$NON-NLS-2$
				  	author_email + ">" + line_sep + "- Original" + line_sep; //$NON-NLS-1$ //$NON-NLS-2$

				byte[] buf = is.getBytes();

				/* Read the input stream and try to create the spec file to be used by
				 *  the rpmbuild process                 */
				try {
					BufferedOutputStream os = 
						new BufferedOutputStream(new FileOutputStream(path_to_specfile));

					for (int i = 0; i < buf.length; i++) {
						os.write(buf[i]);
					}

					os.close();
				} catch (Exception e) {
					String throw_message = Messages.getString(
						"RPMCore.Problem_creating_spec_file.__Check_permissions_in__324") + //$NON-NLS-1$
						rpmdirs_path;
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
							null);
					throw new CoreException(error);
				}

				return true;
			}

			/**
			 * Method traverse.
			 * This method traverses a directory tree beginning at the path passed
			 * to it and builds an array of file names contained in that tree.  This list
			 * of files is used to populate the "%files" section of the spec file.
			 * @param dir - A file that points to the directory where the traversing
			 * is to begin
			 * @param build_root_path is the path to the rpm build root directory
			 * @param file_list is an ArrayList of strings containing paths to what files were
			 *  installed by the "make install" command
			 * @return - If the file_lst[[] array is empty upon return there is a problem
			 * probably in the "install" section of the M/makefile
			 */
			private void traverse(File dir, String build_root_path, ArrayList file_list) {
				if (debug) {
					System.out.println("--traverse"); //$NON-NLS-1$
				}

				int file_ctr;

				if (dir.isDirectory()) {
					String[] children = dir.list();

					for (int i = 0; i < children.length; i++) {
						File temp = new File(dir, children[i]);

						if (temp.isDirectory()) {
							traverse(temp, build_root_path, file_list);
						} else {
							String tmp_name = temp.getAbsolutePath();
							file_list.add(tmp_name.substring(build_root_path.length()) + line_sep);

							if (debug) {
								file_ctr = file_list.size() - 1;
								System.out.println(" file_lst[" + file_ctr + "] = " + //$NON-NLS-1$ //$NON-NLS-2$
									file_list.get(file_ctr));
							}
						}
					}
				}
			}
	
	/** 
	 * Method getHostName gets the name of the host to use as part of the
	 * e-mail address for the chnagelog.
	 * @return String containing the name of the host, "" if error
	 */
	private String getHostName()
	 {
		String hostname;
		  try {
			  hostname = java.net.InetAddress.getLocalHost().getHostName();
		  } catch (UnknownHostException e) {
			  return "";
		  }
		  // Trim off superflous stuff from the hostname
		  int firstdot = hostname.indexOf(".");
		  int lastdot = hostname.lastIndexOf(".");
		  // If the two are equal, no need to trim name
		  if (firstdot == lastdot) {
			return hostname;
		  }
		  String hosttemp = "";
		  String hosttemp2 = hostname;
		  while (firstdot != lastdot) {
			hosttemp = hosttemp2.substring(lastdot) + hosttemp;
			hosttemp2 = hostname.substring(0,lastdot);
			lastdot = hosttemp2.lastIndexOf(".");
		  }
		  return hosttemp.substring(1);
	 }
	 
	/** 
	 * Method returnDate returns the type of date requested by the user.
	 * @return String containing the date in the requested format
	 */
	private String returnDate() {
		 SimpleDateFormat date_Format;
		 date_Format = new SimpleDateFormat("E MMM dd yyyy"); //$NON-NLS-1$
		 return date_Format.format(new Date());
	 }

   
}