package com.ibm.cpp.miners.parser.dstore;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.lang.*;
import java.io.*;
import java.util.*;

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.miners.parser.dstore.*;

public class ParserSchema
{

 //Commands
 public static String Query                  = getLocalizedString("parser.Query");
 public static String Parse                  = getLocalizedString("parser.Parse");
 public static String RemoveParseInformation = getLocalizedString("parser.RemoveParseInformation");
 public static String SaveParseInformation   = getLocalizedString("parser.SaveParseInformation");
 public static String CodeAssist             = getLocalizedString("parser.CodeAssist");
 public static String FindDeclaration        = getLocalizedString("parser.FindDeclaration");
 public static String SetIncludePath         = getLocalizedString("parser.SetIncludePath");
 public static String SetPreferences         = getLocalizedString("parser.SetPreferences");
 
 //Relationships
 public static String Uses               = getLocalizedString("parser.Uses");
 public static String ReturnType         = getLocalizedString("parser.ReturnType");
 public static String Parameters         = getLocalizedString("parser.Parameters");
 public static String Callees            = getLocalizedString("parser.Callees");
 public static String Callers            = getLocalizedString("parser.Callers");
 public static String BaseClasses        = getLocalizedString("parser.BaseClasses");
 public static String DerivedClasses     = getLocalizedString("parser.DerivedClasses");
 public static String ParseReference     = getLocalizedString("parser.ParseReference");
 public static String Includes           = getLocalizedString("parser.Includes");
 public static String IncludedBy         = getLocalizedString("parser.IncludedBy");
 
 //C/C++ Objects
 public static String ParsedSource       = getLocalizedString("parser.ParsedSource");
 public static String IncludedSource     = getLocalizedString("parser.IncludedSource");
 public static String Statement          = getLocalizedString("parser.Statement");
 public static String Constructor        = getLocalizedString("parser.Constructor");
 public static String Destructor         = getLocalizedString("parser.Destructor");
 public static String Function           = getLocalizedString("parser.Function");
 public static String Class              = getLocalizedString("parser.Class");
 public static String Struct             = getLocalizedString("parser.Struct");
 public static String Union              = getLocalizedString("parser.Union");
 public static String Namespace          = getLocalizedString("parser.Namespace");
 public static String Enum               = getLocalizedString("parser.Enum");
 public static String Typedef            = getLocalizedString("parser.Typedef");
 public static String Variable           = getLocalizedString("parser.Variable");
 public static String Macro              = getLocalizedString("parser.Macro");
 
 
 //Other Object Descriptors (those defined in other schemas, among other things)
 public static String Project            = getLocalizedString("parser.Project");
 public static String ParsedFiles        = getLocalizedString("parser.ParsedFiles");
 public static String SourceFiles        = getLocalizedString("parser.SourceFiles");
 public static String ProjectObjects     = getLocalizedString("parser.ProjectObjects");
 public static String SystemObjects      = getLocalizedString("parser.SystemObjects");
 public static String Preferences        = getLocalizedString("parser.Preferences");
 public static String Types              = getLocalizedString("parser.Types");
 public static String FileSystemObjects  = getLocalizedString("parser.FileSystemObjects");
 public static String ContainerObject    = getLocalizedString("parser.ContainerObject");
 public static String CppObject          = getLocalizedString("parser.CppObject");
 public static String UsableCppObject    = getLocalizedString("parser.UsableCppObject");
 public static String Functions          = getLocalizedString("parser.Functions");
 public static String ClassesStructs     = getLocalizedString("parser.ClassesStructs");
 public static String Variables          = getLocalizedString("parser.Variables");
 public static String TimeStamp          = getLocalizedString("parser.TimeStamp"); 
 public static String IncludePath        = getLocalizedString("parser.IncludePath");
 public static String ParseQuality       = getLocalizedString("parser.ParseQuality");
 public static String Error              = getLocalizedString("parser.Error");
 public static String Contents           = getLocalizedString("parser.Contents");
 

 public static DataElement dC_QUERY;
 public static DataElement dC_PARSE;
 public static DataElement dC_REMOVE_PARSE;
 public static DataElement dC_SAVE_PARSE;
 public static DataElement dC_CODE_ASSIST;
 public static DataElement dC_FIND_DECLARATION;
 public static DataElement dC_SET_INCLUDE_PATH;
 public static DataElement dC_SET_PREFERENCES;
 public static DataElement dUses;
 public static DataElement dTypes;
 public static DataElement dReturnType;
 public static DataElement dParameters;
 public static DataElement dCallees;
 public static DataElement dCallers;
 public static DataElement dBaseClasses;
 public static DataElement dDerivedClasses;
 public static DataElement dParseReference;
 public static DataElement dIncludes;
 public static DataElement dIncludedBy;
 public static DataElement dParsedSource;
 public static DataElement dIncludedSource;
 public static DataElement dStatement;
 public static DataElement dConstructor;
 public static DataElement dDestructor;
 public static DataElement dFunction;
 public static DataElement dClass;
 public static DataElement dStruct;
 public static DataElement dUnion;
 public static DataElement dNamespace;
 public static DataElement dEnum;
 public static DataElement dTypedef;
 public static DataElement dVariable;
 public static DataElement dMacro;
 public static DataElement dProject;
 public static DataElement dParsedFiles;
 public static DataElement dSourceFiles;
 public static DataElement dProjectObjects;
 public static DataElement dPreferences;
 public static DataElement dCppObject;
 public static DataElement dUsableCppObject;
 public static DataElement dClassesStructs;
 public static DataElement dVariables;
 public static DataElement dFunctions;
 public static DataElement dFsObjects;
 public static DataElement dContObject;
 public static DataElement dError;
 public static DataElement dTimeStamp;
 public static DataElement dContents;
 public static DataElement dAll;
 
 
 public ParserSchema(DataElement schemaRoot)
 {
  dFsObjects  = findDescriptor(FileSystemObjects,schemaRoot.getDataStore());
  dContObject = findDescriptor(ContainerObject, schemaRoot.getDataStore());  	
  dContObject.setDepth(0);
  dContents   = findDescriptor(Contents, schemaRoot.getDataStore());
  dContents.setDepth(100);
  dAll        = findDescriptor("all", schemaRoot.getDataStore());

  
  
  //Set up the abstract object descriptors:
  dCppObject          = createAbstractDerivativeDescriptor(dContObject,      CppObject);
  dUsableCppObject    = createAbstractDerivativeDescriptor(dCppObject,       UsableCppObject);
  dClassesStructs     = createAbstractDerivativeDescriptor(dCppObject,       ClassesStructs);
  dVariables          = createAbstractDerivativeDescriptor(dUsableCppObject, Variables);
  dFunctions          = createAbstractDerivativeDescriptor(dCppObject,       Functions);


  //Set up the relations and commands for the above objects:
  dC_QUERY            = findDescriptor(Query, schemaRoot.getDataStore());
  dC_PARSE            = createCommandDescriptor(dFsObjects, Parse,                  "C_PARSE", false);
  dC_REMOVE_PARSE     = createCommandDescriptor(dFsObjects, RemoveParseInformation, "C_REMOVE_PARSE", false);
  dC_SAVE_PARSE       = createCommandDescriptor(dFsObjects, SaveParseInformation,   "C_SAVE_PARSE", false);
  dC_CODE_ASSIST      = createCommandDescriptor(dFsObjects, CodeAssist,             "C_CODE_ASSIST");
  dC_CODE_ASSIST.setDepth(0);
  dC_FIND_DECLARATION = createCommandDescriptor(dFsObjects, FindDeclaration,        "C_FIND_DECLARATION");
  dC_FIND_DECLARATION.setDepth(0);
  dC_SET_INCLUDE_PATH = createCommandDescriptor(dFsObjects, SetIncludePath,         "C_SET_INCLUDE_PATH");
  dC_SET_INCLUDE_PATH.setDepth(0);
  dC_SET_PREFERENCES  = createCommandDescriptor(dFsObjects, SetPreferences,         "C_SET_PREFERENCES");
  dC_SET_PREFERENCES.setDepth(0);
  
  DataElement dFile   = findDescriptor("file", schemaRoot.getDataStore());
  createCommandDescriptor(dFile, Parse, "C_PARSE").setDepth(0);
  createCommandDescriptor(dFile, RemoveParseInformation, "C_REMOVE_PARSE").setDepth(0);
  
  dUses            = createRelationDescriptor(dUsableCppObject, Uses);
  dReturnType      = createRelationDescriptor(dFunctions,       ReturnType);
  dParameters      = createRelationDescriptor(dFunctions,       Parameters);
  dCallees         = createRelationDescriptor(dFunctions,       Callees);
  dCallers         = createRelationDescriptor(dFunctions,       Callers);
  dBaseClasses     = createRelationDescriptor(dClassesStructs,  BaseClasses);  
  dDerivedClasses  = createRelationDescriptor(dClassesStructs,  DerivedClasses);
  dTypes           = createRelationDescriptor(dVariables,       Types);
  

  //Create the actual Cpp Objects (non-abstract): 
  dStatement      = createDerivativeDescriptor(dUsableCppObject,      Statement);
  dMacro          = createDerivativeDescriptor(dUsableCppObject,      Macro);
  dConstructor    = createDerivativeDescriptor(dFunctions,      Constructor);
  dDestructor     = createDerivativeDescriptor(dFunctions,      Destructor);
  dFunction       = createDerivativeDescriptor(dFunctions,      Function);
  dClass          = createDerivativeDescriptor(dClassesStructs, Class);
  dStruct         = createDerivativeDescriptor(dClassesStructs, Struct);
  dUnion          = createDerivativeDescriptor(dClassesStructs, Union);
  dNamespace      = createDerivativeDescriptor(dClassesStructs, Namespace);
  dEnum           = createDerivativeDescriptor(dVariables,      Enum);
  dTypedef        = createDerivativeDescriptor(dVariables,      Typedef);
  dVariable       = createDerivativeDescriptor(dVariables,      Variable);
  
  //Create others
  dProject           = findDescriptor(Project, schemaRoot.getDataStore());
  if (dProject == null)
   dProject = createDerivativeDescriptor(dFsObjects, Project);
  
  dParseReference    = createRelationDescriptor(dProject, ParseReference);
  dParseReference.setDepth(0); //make it invisible 
  dParsedFiles       = createDerivativeDescriptor(dContObject, ParsedFiles); 
  dProjectObjects    = createDerivativeDescriptor(dContObject, ProjectObjects);  
  dPreferences       = createDerivativeDescriptor(dContObject, Preferences);
  dError             = createDerivativeDescriptor(dContObject, Error);
  dTimeStamp         = createDerivativeDescriptor(dContObject, TimeStamp);
  
  //Set up the DataElements representing files:
  dSourceFiles      = createAbstractDerivativeDescriptor(dFsObjects, SourceFiles);
  dIncludes         = createRelationDescriptor(dSourceFiles,         Includes);
  dIncludedBy       = createRelationDescriptor(dSourceFiles,         IncludedBy);
  dIncludedSource   = createDerivativeDescriptor(dSourceFiles,       IncludedSource);  
  dParsedSource     = createDerivativeDescriptor(dSourceFiles,       ParsedSource);
  
  
  //Set up some contents relationships
  createReference(dParsedFiles, dSourceFiles);
  createReference(dParsedFiles, dParsedSource);
  createReference(dParsedFiles, dIncludedSource);
  
  createReference(dProjectObjects, dClassesStructs);
  createReference(dProjectObjects, dFunctions);
  createReference(dProjectObjects, dVariables);
  createReference(dProjectObjects, dMacro);
 
  createReference(dSourceFiles, dClassesStructs);
  createReference(dSourceFiles, dFunctions);
  createReference(dSourceFiles, dVariables);
  createReference(dSourceFiles, dMacro);

  createReference(dIncludedSource, dClassesStructs);
  createReference(dIncludedSource, dFunctions);
  createReference(dIncludedSource, dVariables);
  createReference(dIncludedSource, dMacro);

  createReference(dFunctions,    dVariables); 
  createReference(dFunctions,    dStatement);

  createReference(dClassesStructs, dFunctions);
  createReference(dClassesStructs, dVariables);
  createReference(dUnion,       dVariables);
 
  

  dVariables.setDepth(dMacro.depth() + 1);
  dFunctions.setDepth(dVariables.depth() + 1);
  dClassesStructs.setDepth(dFunctions.depth() + 1);
 
  dUses.setDepth(dContents.depth() + 100);
  
  dSourceFiles.setDepth(dAll.depth() + 4);
  dParsedSource.setDepth(dAll.depth() +3);
  dIncludedSource.setDepth(dAll.depth() +2);
  


 }
 
 private static ResourceBundle _resourceBundle = null;
 
 public static String getLocalizedString(String key)
 {
  try
  {
   if (key == null)
    return "";
   if (_resourceBundle == null)
    _resourceBundle = ResourceBundle.getBundle("com.ibm.cpp.miners.parser.dstore.ParserSchema"); 
   String value = _resourceBundle.getString(key);
   if ((value == null) || (value.length() == 0))
    System.out.println("ParserSchema problem finding " + key);
   return value;
   
  }
  catch (MissingResourceException mre) {}
  System.out.println("ParserSchema problem finding " + key);
  return "";
 }

 //Find an object under the schema Root.
 private DataElement findDescriptor(String descName, DataStore dataStore)
 {
  return dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, descName, 2);
 }

 private DataElement createDerivativeDescriptor(DataElement base, String derivedName)
 {
  DataElement theDerivative = createObjectDescriptor(base.getDataStore().getDescriptorRoot(), derivedName);
  createAbstractRelationship(base, theDerivative);
  return theDerivative;
 }

 private DataElement createAbstractDerivativeDescriptor(DataElement base, String derivedName)
 {
  DataElement theDerivative = createAbstractObjectDescriptor(base.getDataStore().getDescriptorRoot(), derivedName);
  createAbstractRelationship(base, theDerivative);
  return theDerivative;
 }

 private DataElement createCommandDescriptor(DataElement descriptor, String name, String value)
 {
  return createCommandDescriptor(descriptor, name, value, true);
 }
 
 private DataElement createCommandDescriptor(DataElement descriptor, String name, String value, boolean visible)
 {
  DataElement cmdD = descriptor.getDataStore().createCommandDescriptor(descriptor, name, "com.ibm.cpp.miners.parser.ParseMiner", value);
  if (!visible)
   cmdD.setDepth(0);
  return cmdD;
 }

 private DataElement createRelationDescriptor(DataElement descriptor, String name)
 {
  return descriptor.getDataStore().createRelationDescriptor(descriptor, name);
 }
 
 private DataElement createAbstractRelationship(DataElement from, DataElement to)
 {
  return from.getDataStore().createReference(from, to, "abstracts", "abstracted by");
 }
 
 private DataElement createAbstractObjectDescriptor(DataElement descriptor, String name)
 {
  return descriptor.getDataStore().createAbstractObjectDescriptor(descriptor, name);
 }
 
 private DataElement createObjectDescriptor(DataElement descriptor, String name)
 {
  return descriptor.getDataStore().createObjectDescriptor(descriptor, name);
 }

 private DataElement createReference(DataElement from, DataElement to)
 {
  return from.getDataStore().createReference(from, to);
 }

 private DataElement createReference(DataElement from, DataElement to, String rel)
 {
  return from.getDataStore().createReference(from, to, rel);
 }
}



