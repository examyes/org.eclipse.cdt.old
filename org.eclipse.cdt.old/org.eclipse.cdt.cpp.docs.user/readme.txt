This document is intended to point out issues or problems with the current
release of the product.(You may also want to see the accompanying "todo.txt")

This list is not complete, please refer to the CDT Bugzilla database for
a comprehensive and up-to-date list of known defects.



Project Issues (Local and Remote)
---------------------------------

1.  Sometimes with small projects (no subdirectories), the C/C++ Projects View
    will not show the files contained in the project immediately after creating
    the project.  To get the files to appear, right-click on the Project and 
    click "Refresh".
2.  C/C++ files contained in non-C/C++ projects will not get parsed in the
    current driver.
3.  When deleting a project created with files found in "Local directory" using the 
    "Delete Project" action, you are prompted asking if you want to delete 
    all the source files as well. If you instead try to delete the project with the 
    "Delete" action then you are not prompted, and all your source files will 
    be deleted without warning.    
4.  If you have two files with the same name in the same directory but different case 
    then only one will show up in the C/C++ Projects View


Appearance on Linux
-------------------

1.  Eclipse on Linux currently uses Motif widgets.  CDT will support other 
    widget sets as Eclipse adds them. 


Parsing Issues
--------------

The parser has been written to handle both C and C++ and is really a work
in progress.  There are constructs or features of both languages that the
parser does not currently handle gracefully or at all. When the parser
encounters some code that it can't handle, it tries to skip over the
problem and continue parsing.  When it does this, it creates Parse 
Exception objects that will appear intermingled in with the other C/C++ 
objects when looking at the Details view of a Parsed File...
Here are some of the most common known problems:

- There are several known issues with UI responsiveness during a large parse job.  
  For now, once a large parse has been started, it is best to wait until the status
  bar has disappeared before trying to access the parse information in the UI.
 
- When parsing a large project, there is a potential to use a large amount 
  of memory. If you encounter memory problems create a batch\script 
  file that starts eclipse (so that we can have control over the command-line 
  parameters sent to the JVM.  A typical script would look like this:

  java -Xms100m -Xmx200m -verify -cp startup.jar org.eclipse.core.launcher.Main  
  
  The -Xms and -Xmx set the minimum and maximum memory size that the JVM is invoked with.
  (We will be looking into memory issues in upcoming releases)

- Known language limitations:
    - Some names of enums and structs are being shown incorrectly
    - Operator Overloading
    - Some macro replacements will not behave correctly
    - Expression Evaluation is broken in the preprocessor, so #if, #ifdef etc,
      will not behave correctly: Essentially every conditional directive is ignored, 
      so all code will be included.  This can cause duplicate objects or affect the 
      accuracy of parse information.  This will be fixed soon.
    - If a function is forward declared, both the definition and declaration
      will appear as separate objects
    - Functions that take as parameters or return, arrays
    - Some casts are not parsing properly  
    - The type information displayed for most objects contains duplicates
    - C++ has not well been tested so there are likely problems with templates,
      namespaces, etc.

Command Launcher View
--------------------

1. The Command Launcher View requires an object in the C/C++ Projects view to be selected.
   If you find that the Run Button in the Command View doesn't appear to work, 
   goto the C/C++ Projects view and select a Project or File and try again.


Command Specifications View
---------------------------

1. Users may need to click on the project to set the focus in the navigator
   before adding/removing command specifications within that project scope
2. When creating a command specification using the wizard, the selected project will be
   highlighted and selected automatically. Changing it to another project
   from within the wizard will not cause the command specification to belong to the
   selected project. This will be fixed in the next release
3. This feature does not support remote projects.


Searching
---------

1. Search results only appear for objects contained in the files of local projects.
   Objects contained in included files or remote projects do not show up in the 
   search results view. For remote projects use the Output view to see the results.


General View Issues
-------------------

1. We have not finalized any icons.  The current set of icons is really a collection
   of assorted images we grabbed from anywhere we could.  Very shortly our
   graphic design team will be creating a consistent, professional set of icons.
2. Several views such as the Command Specifications and Command Launcher view may require 
   a project to be selected before the view can operate properly.  If you find a view not working 
   as expected, goto the C/C++ Projects view and select your project and try again.
3. For large projects, parse view refreshing is quite slow.
4. The C/C++ Projects view and the Navigator are not always in sync. The solution is to
   do a refresh in the view that appears not up to date.
5. If you display a large amounts of information in the Output view, then painting problems 
   have been known to occur.
6. Markers are not be displayed in the Editor for remote projects.e.g adding a breakpoint
    will succedd even though the marker is not visible in the Editor left margin.

Platform issues
---------------
1. The C/C++ Plugin is intended for Linux, although some of its features will work 
   with Windows(with cygwin and some even without it.)
   - With remote Linux projects, Windows Eclipse should support most features.

Debugger issues
---------------
For debugger issues, see the accompanying "todo.txt"

Project Management Issues - autoconf and automake
-------------------------------------------------

1- When you use the automake/autoconf support to create the control files for
   a project, it assumes by convention that no build targets are created in
   the top level directory.  Instead, it is assumed that all real targets are
   in subdirectories.   These assumptions can be overridden by manually 
   editing the Makefile.am files, or by using the Autoconf->Advanced actions
   to generate specific desired kinds of Makefile.am in each directory.
   
   In particular, if the project contains no subdirectories then using 
   the Generate configure action will not produce a useful result as it 
   will generate a single Makefile.am with no target. Instead, create a 
   Makefile.am which include a Target- a PROGRAMS for example- by 
   selecting the action
      Autoconf -> Advanced->create Programs Makefile.am 

2- When invoking the Run Configure action, a popup dialog 
   will warn you of outdated "configure" file.  If you know that your
   configure file is valid and you don't want to regenerate it,
   press the cancel button.  This cancels the regeneration, but proceeds
   to run configure.
