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

/**
  *This class is used to interface to Linux commands
  */
public class LinuxShellCmds {
	private static final boolean debug = false;
	private static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	private static final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String Error = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
	/**
		 * Method createLinuxShellScript.
		 * This method is necessary because of problems trying to execute the
		 *  "rpmbuild -bb....." command directly from the executeRPMbuild method.
		 *  The command did not work the same as it did from the command line.
		 *  It appears that the java command line parser does not work very well
		 *  on very sophisticated or lengthy command lines. Hence, we build a shell
		 *  script to eliminate the java command line parser and all works fine.
		 * @param shellString - string containing the command to be executed as a shell script
		 * @param rpmbuild_logname - boolean variable which determines whether or 
		 * not the output of the shell script to be created should be placed in the 
		 * rpmbuild logfile; true means do not output to the logfile, false means output to the logfile
		 * @param rpm_shell is the name of the shell to create
		 * @return boolean - return true if successful, else throw CoreException
		 */
		/******************************************************************************/
		public static boolean createLinuxShellScript(String shellString,
			 String rpmbuild_logname, String rpm_shell)
			throws CoreException {
			if (debug) {
				System.out.println("--createLinuxShellScript: " + shellString); //$NON-NLS-1$
			}

			// if there was a parenthesis at the beginning of the script, put one at the end
			String tail;
			String first = shellString.substring(0, 1);

			if (first.equals("(")) { //$NON-NLS-1$
				tail = " 2>&1 )" + line_sep; //$NON-NLS-1$
			} else {
				tail = " 2>&1" + line_sep; //$NON-NLS-1$
			}

			String is = "#!/bin/sh" + line_sep + shellString + " >> " + rpmbuild_logname + //$NON-NLS-1$ //$NON-NLS-2$
					tail;

			byte[] buf = is.getBytes();

			/* Read the input stream and try to create the shell script to be used by
			 * the rpmbuild process   */
			try {
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
					rpm_shell));

				for (int i = 0; i < buf.length; i++) {
					os.write(buf[i]);
				}

				os.close();
			} catch (Exception e) {
				String throw_message = Messages.getString(
						"RPMCore.Problem_creating_a_shell_script_--__342") + //$NON-NLS-1$
					rpm_shell +
					Messages.getString(
						"RPMCore._nThere_may_be_a_problem_in_the_M/makefile._343"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}

			/* Change the file attributes so it is exectable, there is no method that I can
			   *  find in java that performs this function   */
			String chmodcommand = "/bin/chmod 744 " + rpm_shell; //$NON-NLS-1$

			try {
				executeLinuxCommand(chmodcommand, 0);
			} catch (CoreException e) {
				String throw_message = Messages.getString(
						"RPMCore.Problem_running_this_command___346") + //$NON-NLS-1$
					chmodcommand +
					Messages.getString("RPMCore._nCheck_permissions._347"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}

			return true;
		}
		
	/**
	 * Method executeLinuxCommand.
	 * This method executes a Linux command passed to it from other methods.  It executes
	 * the command, reads the output from the command and passes back a status.  This method
	 * is used when several output lines is expected from a command.  If one line or less is
	 * expected and the developer wants the output of the command, use the getInfo method.
	 * @param linux_command - a string containing a Linux command
	 * @param status - what the successful status value from the command should be (normally 0)
	 * @return - throws a CoreException if an error is encountered
	 */
	/****************************************************************************/
	public static void executeLinuxCommand(String linux_command, int status)
		throws CoreException {
		if (debug) {
			System.out.println("--executeLinuxCommand: " + //$NON-NLS-1$
				linux_command);
		}

		Runtime r = Runtime.getRuntime();
		Process p = null;
		String line = ""; //$NON-NLS-1$

		try {
			p = r.exec(linux_command);
		} catch (Exception e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_executing__97") + linux_command; //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		// Wait for the completion of the Linux command and check the return status
		try {
			p.waitFor();

			int completionStatus = p.exitValue();

			if (completionStatus != status) {
				String throw_message = Messages.getString(
						"RPMCore.Error_waiting_for__99") + //$NON-NLS-1$
					linux_command +
					Messages.getString("RPMCore._to_complete._100"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}

			// Catch the exception if it was interrupted during execution
		} catch (InterruptedException e) {
			String throw_message = Messages.getString(
					"RPMCore.Command__102") + linux_command + //$NON-NLS-1$
				Messages.getString("RPMCore._was_interrupted._103"); //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}
	}
	/**
	 * Method checkCompression.
	 * This method takes a spec file path and parses it to see what compression
	 * will be required to untar the source file.  We assume that the compression
	 * will either be gzip or bzip2 since those are the only 2 currently used for
	 * RPM's that we have run into.
	 * @param path to the spec file to be searched
	 * @return return the tar file suffix of either ".gz" or ".bz2" if successful, 
	 *             return "" if not.
	 */
	public static String checkCompression(String path_to_specfile) throws CoreException {
		if (debug) {
			System.out.println("--parseSpecfile: " + path_to_specfile); //$NON-NLS-1$
		}

		boolean found_source_line = false;
		
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
				  break;
				case StreamTokenizer.TT_WORD:
					new_word = st.sval;
					// System.out.println("---- " + new_word + "\n   line no = " + st.lineno());

					if (found_source_line) {
						if (new_word.endsWith(".gz")) { //$NON-NLS-1$
							return ".gz"; //$NON-NLS-1$
						} else {
							return ".bz2"; //$NON-NLS-1$
						}
					}
					
						// Record where the last line of the form "Sourcex:" is
						if (new_word.startsWith("Source") &  //$NON-NLS-1$
							 new_word.endsWith(":")) { //$NON-NLS-1$
							found_source_line = true;
							break;
						}
						
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
	// If we got here, we could not determine the compression for the tar file
		return "";  //$NON-NLS-1$
	}
	/**
	 * Method linuxCopy
	 * This method takes two strings containing paths and uses the Linux cp
	 * command to copy from the first string path to the second string path.  The
	 * reason we sue this instead of native Java techniques is that Java copies
	 * do not preserve all of the file attributes, particularly the executable bit and
	 * also does not preserve the modification date/time without special handling.
	 * In the future when this is taken care of, we will replace this method.
	 * @author Red Hat, Inc.
	 *@param from_path is a string containing the from path
	 *@param to_path is a string containing the to path
	 *@return if successful, throw CoreException if not
	 */
	public static void linuxCopy(String from_path, String to_path, String rpmbuild_logname,
			String rpm_shell) throws CoreException {
		// If we are doing a directory to directory copy, make sure the to_path
		// exists and is a directory
		File f = new File(to_path);
		File f1 = new File(from_path);
		if (f1.isDirectory()) {
			if (!f.exists()) {
				if (!f.mkdir()) {
					String throw_message = Messages.getString("LinuxShellCmds.Error_attempting_to_create___1") + to_path; //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
					throw new CoreException(error); 
				}
			}
			if (f.exists() & !f.isDirectory()) {
				String throw_message = Messages.getString("LinuxShellCmds.Cannot_copy_a_directory___2") + from_path + //$NON-NLS-1$
				  Messages.getString("LinuxShellCmds._to_a_file___3") + to_path; //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
				throw new CoreException(error);  
			}
		}
		String cp_cmd = "(cd " + from_path + line_sep + "/bin/cp -rp . " + to_path; //$NON-NLS-1$ //$NON-NLS-2$
				try {
					createLinuxShellScript(cp_cmd, rpmbuild_logname, rpm_shell);
					executeLinuxCommand(rpm_shell,0);
				} catch (CoreException e) {
					String throw_message = Messages.getString("LinuxShellCmds.Error_attempting_to_copy_source_from___4") + from_path + //$NON-NLS-1$
					Messages.getString("LinuxShellCmds._to__5") + to_path; //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
					throw new CoreException(error); 
				}
	}
	
	/**
		 * Method getInfo.
		 * This method takes a Linux/shell command, executes it and passes the output line back
		 * as the information string.
		 * @param sh_command - a string containing the command to execute
		 * @return String - the output of the executed command, maybe null if there is an error
		 */
		/******************************************************************************/
		public static String getInfo(String sh_command) {
			if (debug) {
				System.out.println("getInfo: " + sh_command); //$NON-NLS-1$
			}

			Runtime r = Runtime.getRuntime();
			Process p = null;
			String line = ""; //$NON-NLS-1$

			try {
				p = r.exec(sh_command);

				// Set up and capture the stdout messages from the Linux/shell command
				BufferedReader is = new BufferedReader(new InputStreamReader(
							p.getInputStream()));
				line = is.readLine();
				p.waitFor();

				if (debug) {
					System.out.println(sh_command + " =  " + line); //$NON-NLS-1$
				}
			} catch (Exception e) {
				System.out.println(Messages.getString(
						"RPMCore.Error_during__191") + sh_command + //$NON-NLS-1$
					Messages.getString("RPMCore._execution..error____192") + //$NON-NLS-1$
					e.getMessage());

				return ""; //$NON-NLS-1$
			}

			return line;
		}
	
	/**
	 * Method checkForConfigure checks a project for the presence of a 
	 * "configure" script that creates various parts of a project including
	 * "Makefile".
	 * @return true if "configure" was found, false if not
	 */

	public static boolean checkForConfigure(String proj_path) {
//		Check to see if there is a "configure" script for use when creating a spec file
			 File f = new File(proj_path + file_sep + "configure"); //$NON-NLS-1$

			 return f.exists();
	}

}