
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.util.*;

class GenerateMakefiles
{
 private static String _pluginsDir;
 private static String _theRulesMakefile;
 
 public static void main(String args[])
 {
  _pluginsDir = getPluginsDirectory();
  if (args.length == 0)
  {
   System.out.println(_pluginsDir);
   return;
  }
  _theRulesMakefile = _pluginsDir + "/com.ibm.dstore.core/build/rules.mk";
  for (int i=0; i<args.length; i++)
   generateBuildMakefilesFor(args[i]);
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
  writeFile(theMakefile, fileContents);
 
  File[] subdirs = theDirectory.listFiles();
  for (int i=0; i<subdirs.length; i++)
  {
   File theSubdir = subdirs[i];
   if ( theSubdir.isDirectory() && (!theSubdir.getName().equals("CVS")) && (!theSubdir.getName().equals("build")))
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
  if (thePlugin.indexOf("extra") >= 0)
   thePlugin = thePlugin + "_client";
  int firstDot  = thePlugin.indexOf(".");
  int secondDot = thePlugin.indexOf(".",firstDot+1);
  return thePlugin.substring(secondDot+1, thePlugin.length()).replace('.','_') + ".jar";
 }
 
 private static String getEnvironmentInfo(String thePlugin)
 {
  StringBuffer env = new StringBuffer();
  env.append("pluginsDirectory:=" + _pluginsDir +"\n");
  env.append("pluginName:=" + thePlugin +"\n");
  env.append("jarFile:=" + getJarFileName(thePlugin) +"\n");

  File pluginXML = new File(_pluginsDir + "/" + thePlugin + "/plugin.xml");
  if (!pluginXML.exists())
   pluginXML = new File(_pluginsDir + "/" + thePlugin + "/Imports.make");
  
  ArrayList classpaths = getClassPaths(pluginXML);
  env.append("cp:=" + _pluginsDir + "/" + thePlugin + "\\\n");
  for (int i=0; i<classpaths.size(); i++)
  {
   ArrayList theJars = getJars((String)classpaths.get(i));
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
 
 private static ArrayList getJars(String theClassPath)
 {
  ArrayList jarNames = new ArrayList();
  try
  {
   File pluginXML = new File (_pluginsDir + "/" + theClassPath + "/plugin.xml");
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
        jarNames.add(_pluginsDir + "/" + theClassPath);
       else
        jarNames.add(_pluginsDir + "/" + theClassPath + "/" + theJar);        
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
  String plugins = new String("plugins");
  String pwd = System.getProperty("user.dir");
  try
  { 
   pwd = new File(pwd).getCanonicalPath();
  }
  catch (IOException e) {}
  return pwd.substring(0, pwd.indexOf(plugins) + plugins.length()).replace('\\','/');
 } 
}

 


