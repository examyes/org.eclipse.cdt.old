@echo off
rem ******************runCommand.bat******************************************************** 
rem  This batch file is the result of the limitation in Java 1.2 which prevents us from 
rem  programatically changing the working directory for a Runtime.exec() call...This is
rem  fixed in Java 1.3, so when we move up to that level of jdk, we won't need this hack ;-)

rem  This batch file accepts an unlimited number of arguments (up to some system limit of
rem  course...maybe 256?).   The first 2 arguments are the drive letter and absoluted path
rem  of the required directory respectively.  The third argument is the command to be issued
rem  once in the working directory, and the remaining arguments are parameters to send to the
rem  command.
rem **************************************************************************************** 


rem ****************************************************************************************
rem  Change drive letter and chdir(cd) to the working directory.   Then shift those 2 
rem  parameters...we are done with them.
rem ****************************************************************************************
%1:
shift
cd %1
shift


rem ****************************************************************************************
rem  Set the THECOMMAND variable to be the command, and shift that parameter.
rem ****************************************************************************************
set THECOMMAND=%1
shift


rem ****************************************************************************************
rem  This little loop just keeps appending the remaining parameters to the end of the 
rem  command with a space in between each.   When we find a parameter that is "empty" 
rem  we end.
rem ****************************************************************************************
:appendArgument
if "%1" == "" goto end 
set THECOMMAND=%THECOMMAND% %1
shift
goto appendArgument



rem ****************************************************************************************
rem  We are finished constructing the command, so now we just execute it  (we redirect 
rem  stderr to stdout so that the system doesn't have to listen to both streams.  Finally
rem  when the command has finished, echo a "BATCHCOMMANDISFINISHED" which tells the caller 
rem  of this file that it is done.
rem ****************************************************************************************
:end
%THECOMMAND% 2>&1
echo BATCHCOMMANDISFINISHED
