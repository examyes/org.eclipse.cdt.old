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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
  *This class is the core class that is extended by RPMExport and SRPMExport.
  * It conatins the main methods for exporting either a binary or source RPM.
  */
public class RPMExportCore extends RPMCore {

	protected String ui_spec_file = ""; //$NON-NLS-1$
	protected String patch_name;
	protected String patch_tag;
	protected String changelog_entry;
	
	private String which_rpm;
	private SpecFileOps specfileops;
	
	private static final String src_exp = "-bs"; //$NON-NLS-1$
	private static final String bin_exp = "-bb"; //$NON-NLS-1$
	private static final boolean debug = false;
	
	/**
	 * This is the constructor called to instantiate this class.
	 * @param c_proj_path is the path to the Eclipse project to be exported.
	 * @param c_which_rpm is which rpm to export; "-bs" = source, "-bb" = binary
	 * @throws CoreException
	 */
	
	public RPMExportCore (String c_proj_path, String c_which_rpm) 
   	    throws CoreException {
		super(c_proj_path, ""); //$NON-NLS-1$

		which_rpm = c_which_rpm;

		int j = proj_path.lastIndexOf(file_sep);
		proj_dir = proj_path.substring(j + 1);
 	}
 	
	/**
		 * Method run will call the proper methods in the proper sequence
		 * to create the appropriate RPM.  This is called from the GUI to
		 * segregate the implementation from the user interface.
		 * @return if successful, throws CoreException if not
		 */
	public void run() throws CoreException {
		if (debug) {
					System.out.println("*************** RPMExportCore run **********************"); //$NON-NLS-1$
		}
		// Create the work area for rpm to do its thing
		try {
			setRpmbuild_logname();
			createRPMdirectories(rpmdirs_path);
			createRPMmacros(rpm_macros);
			createRPMrpmrc(rpmrc);
			createRPMLogFile();
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 
		}
		// If we are exporting a binary RPM and the source RPM is already built and contains
		// the same version/release numbers and the checksums are the same
		//  just build the RPM from the source
		if (ui_ver_no.equals(getRpm_version()) & ui_rel_no.equals(getRpm_release()) &
			patch_tag == null & which_rpm.equals("-bb") & !isChk_sum_diff()) { //$NON-NLS-1$
				try {
					buildBinaryFromSourceRpm();
				} catch (Exception e) {
					String throw_message = e.getMessage();
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
					throw new CoreException(error); 
				}
				return;
			}
		// Was this project previously exported (does a .srpminfo file exist)
		if (firstSRPM(proj_path)) {
			// if this is the first time this project has been exported, branch off to another method
			try {
				createSRPM();
			} catch (CoreException e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
				throw new CoreException(error);
			}
			return;
		}
		String path_to_newspecfile;
		/** During an export we install the source rpm this project was imported from
		 * and then copy the Eclipse project itself over to the to allow us to create patches
		 * by executing the "diff" command.  In the future we should change this behaviour
		 * to only occur when a source RPM is being exported, but for now we do it the same
		 * for both exports.
		 */
		// Install the source into the rpm workarea 
		try {
			installRPMsource(getPath_to_rpm());  // "rpm -i ...." command
			executeRPMbuildprep();               // "rpmbuild -bp ...." command
			// If the user specfied a spec file, use it instead of modifying the one in the project
			if (ui_spec_file.startsWith(spec_file_prefix)) { 
				specfileops = new SpecFileOps(proj_path, rpmdirs_path, wksp_path, proj_dir);
				path_to_newspecfile =
			   		specfileops.changeRPMspecfile(ui_ver_no, ui_rel_no, rpm_version, 
					rpm_release,patch_tag,changelog_entry,rpm_name,getPath_to_specfile());
				// Now generate patches by doing a diff between the old source RPM and
				// the current project directory
				generateSRPMpatch(ui_ver_no, patch_tag);
			} else {
				path_to_newspecfile = ui_spec_file;
			}
			/* If the version number changed, we must change the tarball name if we are exporting
			 * a source rpm export, the source tarball name format is:
			 * rpmname-version.tar.zz where zz is usually either bz2 or gz
			 */
			 if (which_rpm.equals(src_exp) & !ui_ver_no.equals(getRpm_version())) {
			 	TarOps tarops = new TarOps(proj_path, rpmdirs_path);
			 	try {
			 		tarops.renameRPMtarfile(getRpm_name(), getRpmbuild_logname(),
			 	   		getRpm_shell(), ui_ver_no, getRpm_version());
			 	} catch (Exception e) {
					String throw_message = e.getMessage();
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
					throw new CoreException(error);
			 	}
			 }
			// Replace the spec file in the work area with the new one we just created
			String spec_path = replaceSpecFile(path_to_newspecfile);
			// Now build us an RPM....which_rpm governs which one
			executeRpmBuild(which_rpm, spec_path);
			// Now copy the newly created RPM(s) back to the Eclipse project
			String new_src_rpm = copyRpms(which_rpm);
			// Only create a new spec file/.srpminfo file if a source RPM was exported
			if (which_rpm.equals("-bs")) { //$NON-NLS-1$
				String newspecpath = renameSpecFile(path_to_newspecfile, spec_path);
				int j = new_src_rpm.lastIndexOf(file_sep);
				String srpm_full_name = new_src_rpm.substring(j + 1);
				createSRPMinfo(newspecpath, srpm_full_name);
				// Delete the newly created spec file if we exported a binary RPM
				} else {
					try {
						File f = new File(path_to_newspecfile);
						f.delete();
					} catch (Exception e) {
						String throw_message = "Error trying to delete newly created spec file at: " //$NON-NLS-1$
							+ path_to_newspecfile;
						IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
						throw new CoreException(error);
					}
				}

			deleteRPMresources(rpmdirs_path);
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
	  return;
	}
	
	/**
			 * Method renameSpecFile will delete the previous "eclipse_...spec"
			 * and the new "eclipse_...spec.new" files from the project and copy the
			 * spec file that was used to perform the building of the source rpm from
			 * the work area to the project renaming it to "eclipse_...spec"
			 * @param path is a String containing the path to the spec file either created 
			 * by changeRPMspec which was created from the original spec file but has
			 * the version and/or release numbers changed along with the addition of
			 * Patch:/%patch/Changelog statements or is a user-specified spec file
			 * @param path2 is a String containing the path to the spec file in the rpm 
			 * workarea so we can copy it back to the project for future use
			 * @return the path to the new spec file so this information can be included
			 * in the .srpminfo file so the next time we export we'll know which spec file
			 * to use; throws CoreException if there is an error
			 */
	public String renameSpecFile(String path, String path2) throws CoreException {
		if (debug) {
					System.out.println("renameSpecFile" + line_sep + " path = " + path + //$NON-NLS-1$ //$NON-NLS-2$
						line_sep + "  path2 = " + path2); //$NON-NLS-1$
		}
		int j = path2.lastIndexOf(file_sep);
		String spec_name;
		if (j != -1) {
			spec_name = path2.substring(j+1);
		} else {
			spec_name = path2;
		}
		// Only delete the spec file is the user did not specify their own spec file
		if (ui_spec_file.startsWith(spec_file_prefix)) {
		  File f = new File(path);
		  File f1 = new File(getPath_to_specfile());
		  if (!f.delete() | !f1.delete()) {
			String throw_message = "Error trying to delete " + f + " or " + f1; //$NON-NLS-1$ //$NON-NLS-2$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		  }
		}

			try {
				copyFile(path2, proj_path + file_sep + getSpec_file_prefix() + spec_name);
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (CoreException e) {
				
				e.printStackTrace();
			}
		return proj_path + file_sep + spec_name;
	}
	
	/**
			 * Method replaceSpecFile takes the new spec file created by changeRPMspec
			 * with the new information and copies it to the rpm workarea replacing the spec
			 * file that was a part of original source RPM.
			 * @param contains a String with is the path to the newly created spec file
			 * which resides in the project for now.
			 * @return if successful, throws CoreException if not
			 */
	
	public String replaceSpecFile(String path) throws CoreException {
		if (debug) {
					System.out.println("replaceSpecFile"); //$NON-NLS-1$
		}
		String temp = rpmdirs_path + file_sep + "SPECS" + file_sep; //$NON-NLS-1$
		File f = new File(temp);
		String[] subdir = f.list();
		if (subdir.length != 1) {
			String throw_message = Messages.getString("RPMExportCore.Too_many_spec_files_in__4") + temp;  //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
		File f1 = new File(temp + subdir[0]);
		if (!f1.delete()) {
			String throw_message = Messages.getString("RPMExportCore.Error_trying_to_delete__5") + f1;  //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
		 try {
			copyFile(path, temp + subdir[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return temp + subdir[0];
	}
	
	/**
			 * Method createSRPM is called when a project has never been
			 * exported as a project before.
			 * @return if successful, throws CoreException if not
			 */
	public void createSRPM() throws CoreException {
		if (debug) {
			System.out.println("createSRPM"); //$NON-NLS-1$
		}
		String spec_dir = file_sep + "SPECS" + file_sep;
		// Since this is the first time this project has been exported as an RPM,
		// use the version/release number entered by the user
		rpm_version = ui_ver_no;
		rpm_release = ui_rel_no;
		// Copy the project to the work area to preserve the Eclipse project
		String proj_work_dir = rpmdirs_path + file_sep + "BUILD" + file_sep + proj_dir; //$NON-NLS-1$
		// Copy the project to the rpm workarea so we can work on it
		try {
			LinuxShellCmds.linuxCopy(proj_path, proj_work_dir, getRpmbuild_logname(), 
			   getRpm_shell());
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
		String spec_file_name = ui_spec_file;
		String spec_file_path = ui_spec_file;
		// Invoke the specfile ops class to create a new spec file if the user has not specified one
		// If the user chose a spec file inside the Eclipse project, there will not be any
		// '/'s in the name, if there are '/'s, figure out the name
		if (!ui_spec_file.equals("")) { //$NON-NLS-1$
			int i = ui_spec_file.lastIndexOf(file_sep);
			if (i != -1) {
				spec_file_name = ui_spec_file.substring(i+1);
				spec_file_path = ui_spec_file;
			}
		} else {
			spec_file_name = proj_dir + ".spec"; //$NON-NLS-1$
			spec_file_path = rpmdirs_path + spec_dir + spec_file_name;
			SpecFileOps specfileops = new SpecFileOps(proj_path, rpmdirs_path, wksp_path, 
		   		proj_dir);
			specfileops.createRPMspec(getRpm_shell(), getRpmbuild_logname(),
		     	proj_work_dir, rpm_version, rpm_release, rpmdirs_path + spec_dir + 
		     		spec_file_name,
		     	rpm_name);
		}
		
		// Now create a tarball for the source if the user has indicated that a source RPM be built
		if (which_rpm.equals("-bs")) { //$NON-NLS-1$
		// Clean up after the spec file creation is done in preparation for creating a tarball
			try {
				executeMakeClean(proj_work_dir);
			} catch (Exception e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
				throw new CoreException(error);
			}
		}		
			try {
				TarOps tarops = new TarOps(proj_path, rpmdirs_path);
				tarops.createRPMtarfile(rpm_name, rpm_version, spec_file_path);
			} catch (Exception e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
				throw new CoreException(error);
			}
		// Copy the spec file to be used by rpmbuild to the work area if it isn't already there
		if (!spec_file_path.equals(rpmdirs_path + spec_dir + spec_file_name)) { 
			try {
				 copyFile(spec_file_path, rpmdirs_path + spec_dir + spec_file_name);
			  	  } catch (Exception e) {
				  	String throw_message = e.getMessage();
				  	IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
				  	throw new CoreException(error); 
			  	  }
		}
		// Now, build the RPM's and copy them to the project
		try {
			executeRpmBuild(which_rpm, spec_file_path);
			String path_to_src_rpm = copyRpms(which_rpm);
			copyFile(rpmdirs_path + spec_dir + spec_file_name, proj_path + file_sep + 
			getSpec_file_prefix() + spec_file_name);
			if (which_rpm.equals("-bs")){ //$NON-NLS-1$
				int j = path_to_src_rpm.lastIndexOf(file_sep);
				String src_name = path_to_src_rpm.substring(j+1);
				createSRPMinfo(getSpec_file_prefix() + spec_file_name, src_name);
			}
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
		// Now clean up the work area
		try {
			deleteRPMresources(rpmdirs_path);
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
	}
	
	/**
			 * Method createSRPMinfo is used as an interface to the same method
			 * inthe RPMCore class.  We just put try/catch around it
			 * @param file1 is a string containing the Eclipce spec file name in the Eclipse project
			 * @param file2 is a string containing the name of the new source rpm that resides
			 * in the project
			 * @return if successful, throws CoreException if not
			 */
	public void createSRPMinfo(String specfile, String src_name)
		throws CoreException {
			int j = specfile.lastIndexOf(file_sep);
			if (j != -1) {
				specfile = getSpec_file_prefix() + specfile.substring(j+1);
			}
			// Now, recreate the .srpminfo file in the project to point to the new source RPM
			try {
				super.createSRPMinfo(specfile, src_name);
			} catch (Exception e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}
	}
	
	/**
			 * Method buildBinaryFromSource builds a binary RPM from a source RPM
			 * that has previously been created in the project.
			 * @return if successful, throws CoreException if not
			 */
	public void buildBinaryFromSourceRpm() throws CoreException {
		if (debug) {
			System.out.println("buildBinaryFromSourceRpm"); //$NON-NLS-1$
		}
		try {
			installRPMsource(getPath_to_rpm());  // "rpm -i ...." command
			//	Now build us an RPM....which_rpm governs which one
			String spec_path = findSpecFileName();
			executeRpmBuild(which_rpm, spec_path);
			copyRpms(which_rpm);
			deleteRPMresources(rpmdirs_path);
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
			throw new CoreException(error);
		}
		return;
		}
	
	/**
	 * Method findSpecFileName will return the name of the spec file in the 
	 * SPECS directory of the RPM work area.
	 * @return if successful, throws CoreException if not
	 */
	public String findSpecFileName() throws CoreException {
		if (debug) {
			System.out.println("findSpecFileName"); //$NON-NLS-1$
		}
		File f = new File(rpmdirs_path + file_sep + "SPECS" + file_sep); //$NON-NLS-1$
		String[] subdir = f.list();
		if (subdir.length != 1) {
			String throw_message = "Error in spec file directory:" + f; //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error);
		}
		return rpmdirs_path + file_sep + "SPECS" + file_sep + subdir[0]; //$NON-NLS-1$
	}

	/**
	 * @param string
	 */
	public void setPatch_name(String string) {
		patch_name = string;
	}

	/**
	 * @param string
	 */
	public void setPatch_tag(String string) {
		patch_tag = string;
	}

	/**
	 * @param string containing the changelog entry the user entered
	 * 	from the GUI
	 */
	public void setChangelog_entry(String string) {
		changelog_entry = string;
	}

	/**
	 * @param string
	 */
	public void setUi_spec_file(String string) {
		ui_spec_file = string;
		if (!string.equals("") & string != null) { //$NON-NLS-1$
			int i = spec_file_name.lastIndexOf(file_sep);
			if (i == -1) {
				setPath_to_specfile(proj_path + file_sep + ui_spec_file);
			} else {
				setPath_to_specfile(ui_spec_file);
			}
		}
	}

}
