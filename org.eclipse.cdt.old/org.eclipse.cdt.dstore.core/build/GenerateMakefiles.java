
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.util.*;

class GenerateMakefiles
{
 private static String _pluginsDir;	// Location of the cpp plugins
 private static String _basePluginsDir; // Location of the Eclipse base plugins
 private static String _theRulesMakefile;
 
 public static void main(String args[])
 {
  _pluginsDir = getPluginsDirectory();
  if (args.length == 0)
  {
   System.out.println(_pluginsDir);
   System.exit(0);
  }
  _basePluginsDir = getBasePluginsDirectory();
  _theRulesMakefile = _pluginsDir + "/com.ibm.dstore.core/build/rules.mk";
  for (int i=0; i<args.length; i++)
   generateBuildMakefilesFor(args[i]);
  System.exit(0);
 }
 
 
 private static void generateBuildMakefilesFor(String thePlugin)
 {
  String envMakefile = generateEnvironmentMakefile(thePlugin);
  File srcDirectory = new File (_pluginsDir + "/" + thePlugin);
  if (srcDirectory.exists())
   recursiveCreateMakefiles(srcDirectory, envMakefile);
 }

 private static void recursiveCreateMakefiles(File theDirectory, String theEnvMakefile)
 {
  String theMakefile = theDirectory + "/makefile";
  String fileContents = "include " + theEnvMakefile + "\ninclude " + _theRulesMakefile;
  if (!theDirectory.getName().equals("com.ibm.dstore.core"))
   writeFile(theMakefile, fileContents);
 
  File[] subdirs = theDirectory.listFiles();
  for (int i=0; i<subdirs.length; i++)
  {
    
   File theSubdir = subdirs[i];
   if ( theSubdir.isDirectory() &&
	(!theSubdir.getName().equals("CVS")) &&
	(!theSubdir.getName().equals("build")) &&
	(!theSubdir.getName().equals("icons")))
    recursiveCreateMakefiles(theSubdir, theEnvMakefile);
  }
 }
 
 private static String generateEnvironmentMakefile(String thePlugin)
 {
  String setenvMakefile = _pluginsDir + "/" + thePlugin + "/setenv.mk";
  writeFile(setenvMakefile, getEnvironmentInfo(thePlugin));
  return setenvMakefile;
 }

 private static void writeFile(String fileName, String contents)
 {
  try
  {
   File theFile = new File (fileName);
   if (theFile.exists())
    theFile.delete();
   theFile.createNewFile();
   PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(theFile)));
   out.println("# Copyright (c) 2001 International Business Machines Corporation. All rights reserved.");
   out.println("# This program and the accompanying materials are made available under the terms of the");
   out.println("# Common Public License which accompanies this distribution.");
   out.println("SHELL=sh");
   out.println(contents);
   out.flush();
   out.close();
  }
  catch (IOException e) {}
 }
 
 /* Very specific method to generate a jar file name...
  * Currently we just take the last 2 parts of the plugin name and separate them by a _,
  * so com.ibm.dstore.core becomes dstore_core.jar
  */
 private static String getJarFileName(String thePlugin)
 {
  StringTokenizer strtok = new StringTokenizer(thePlugin,".");
  ArrayList namePieces = new ArrayList();
  while (strtok.hasMoreTokens())
   namePieces.add(strtok.nextToken());
    
  int names = namePieces.size()-1;
  if(names <= 0)
	return "";
  String jarName = (String)namePieces.get(names-1) + "_" + (String)namePieces.get(names) + ".jar";
  return jarName;
 }
 
 private static String getEnvironmentInfo(String thePlugin)
 {
  StringBuffer env = new StringBuffer();
  env.append("pluginsDirectory:=" + _pluginsDir +"\n");
  env.append("pluginName:=" + thePlugin +"\n");
  env.append("jarFile:=" + getJarFileName(thePlugin) +"\n");

  File pluginXML = new File(_pluginsDir + "/" + thePlugin + "/Imports.make");
  if (!pluginXML.exists())
	pluginXML = new File(_pluginsDir + "/" + thePlugin + "/plugin.xml");
  
  ArrayList classpaths = getClassPaths(pluginXML);
  env.append("cp:=" + _pluginsDir + "/" + thePlugin + "\\\n");
  for (int i=0; i<classpaths.size(); i++)
  {
   ArrayList theJars = getJars((String)classpaths.get(i), _pluginsDir);
   if(theJars == null || theJars.size() == 0 && _basePluginsDir != null)
	theJars = getJars((String)classpaths.get(i), _basePluginsDir);
   if(theJars == null || theJars.size() == 0) {
	System.out.println("Cannot find jars from "+(String)classpaths.get(i));
	System.exit(1);
   }

   for (int j=0; j<theJars.size(); j++)
    env.append(((String)theJars.get(j))+"\\\n");
  }
  env.append("\n\n");
  return env.toString();
 }
 
 private static ArrayList getClassPaths(File pluginXML)
 {
  ArrayList theClassPaths = new ArrayList();
  try
  {
   BufferedReader br = new BufferedReader(new FileReader(pluginXML));
   String nextLine;
   while ( (nextLine = br.readLine()) != null)
   {
    if (nextLine.indexOf("import plugin=") > -1)
    {
     int firstQuote = nextLine.indexOf("\"");
     if (firstQuote > -1)
     {
      int secondQuote = nextLine.indexOf("\"", firstQuote+1);
      if (secondQuote > firstQuote)
       theClassPaths.add(nextLine.substring(firstQuote+1, secondQuote));
     }
    }
   }
  }
  catch (IOException e) { }
  return theClassPaths;
 }
 
 private static ArrayList getJars(String theClassPath, String pluginsDir)
 {
  ArrayList jarNames = new ArrayList();
  try
  {
   File pluginXML = new File (pluginsDir + "/" + theClassPath + "/plugin.xml");
   if(!pluginXML.exists() || !pluginXML.isFile() || !pluginXML.canRead())
	return null;

   BufferedReader br = new BufferedReader(new FileReader(pluginXML));
   String nextLine;

   while ( (nextLine = br.readLine()) != null)
   {
    if (nextLine.indexOf("library name=") > -1)
    {
     int firstQuote = nextLine.indexOf("\"");
     if (firstQuote > -1)
     {
      int secondQuote = nextLine.indexOf("\"", firstQuote+1);
      if (secondQuote > firstQuote)
      { 
       String theJar = nextLine.substring(firstQuote+1, secondQuote);
       if (theJar.equals("."))
        jarNames.add(pluginsDir + "/" + theClassPath);
       else
        jarNames.add(pluginsDir + "/" + theClassPath + "/" + theJar);        
      }
     }      
    }
   }
  }
  catch (IOException e) {}
  return jarNames;
 }
 
 private static String getPluginsDirectory()
 {
  String pwd = System.getProperty("user.dir");
  try
  { 
   pwd = new File(pwd).getCanonicalPath();
  }
  catch (IOException e) { return ""; }
  pwd = pwd.replace('\\','/');
  int last = pwd.lastIndexOf('/');
  if(last == pwd.length()-1)
  	last = pwd.lastIndexOf('/', last-1);
  last = pwd.lastIndexOf('/', last-1);
  return pwd.substring(0, last);
 } 

 private static String getBasePluginsDirectory()
 {
  String pwd = System.getProperty("ECLIPSE");

  if(pwd == null || pwd.equals(""))
     return _pluginsDir;		// Our best guess

  try {
    pwd = new File(pwd).getCanonicalPath();
  } catch(IOException e) { return null; }
  if(pwd.indexOf("plugins") < 0)
	pwd += File.separator + "plugins";

  return pwd;
 } 
}

 


