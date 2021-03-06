package org.eclipse.cdt.cpp.miners.parser.dstore;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.lang.*;
import java.io.*;
import java.util.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.parser.dstore.*;

public class ParserSchema
{

 //Commands
 public static String Query                  = getLocalizedString("parser.Query");
 public static String Parse                  = getLocalizedString("parser.Parse");
 public static String RemoveParseInformation = getLocalizedString("parser.RemoveParseInformation");
 public static String SaveParseInformation   = getLocalizedString("parser.SaveParseInformation");
 public static String CodeAssist             = getLocalizedString("parser.CodeAssist");
 public static String ProvideSourceFor       = getLocalizedString("parser.ProvideSourceFor");
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
 public static String All                = getLocalizedString("parser.All");

 //C/C++ Objects
 public static String ParsedSource       = getLocalizedString("parser.ParsedSource");
 public static String IncludedSource     = getLocalizedString("parser.IncludedSource");
 public static String Statement          = getLocalizedString("parser.Statement");
 public static String CompoundStatement  = getLocalizedString("parser.CompoundStatement");
 public static String Constructor        = getLocalizedString("parser.Constructor");
 public static String Destructor         = getLocalizedString("parser.Destructor");
 public static String MainFunction       = getLocalizedString("parser.MainFunction");
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
 public static String DeclVariables      = getLocalizedString("parser.DeclarationVariables");
 public static String FileSystemObjects  = getLocalizedString("parser.FileSystemObjects");
 public static String ContainerObject    = getLocalizedString("parser.ContainerObject");
 public static String ObjectDescriptor   = getLocalizedString("parser.ObjectDescriptor");
 public static String CppObject          = getLocalizedString("parser.CppObject");
 public static String ContCppObject      = getLocalizedString("parser.ContCppObject");
 public static String UsableCppObject    = getLocalizedString("parser.UsableCppObject");
 public static String Functions          = getLocalizedString("parser.Functions");
 public static String Statements         = getLocalizedString("parser.Statements");
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
 public static DataElement dC_PROVIDE_SOURCE_FOR;
 public static DataElement dC_FIND_DECLARATION;
 public static DataElement dC_SET_INCLUDE_PATH;
 public static DataElement dC_SET_PREFERENCES;
 public static DataElement dAll;
 public static DataElement dUses;
 public static DataElement dTypes;
 public static DataElement dDeclVariables;
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
 public static DataElement dCompoundStatement;
 public static DataElement dStatement;
 public static DataElement dConstructor;
 public static DataElement dDestructor;
 public static DataElement dMainFunction;
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
 public static DataElement dContCppObject;
 public static DataElement dUsableCppObject;
 public static DataElement dClassesStructs;
 public static DataElement dVariables;
 public static DataElement dFunctions;
 public static DataElement dStatements;
 public static DataElement dFsObjects;
 public static DataElement dContObject;
 public static DataElement dError;
 public static DataElement dTimeStamp;
 public static DataElement dContents;
 public static DataElement dObject;

 public ParserSchema(DataElement schemaRoot)
 {
  dObject     = findDescriptor(ObjectDescriptor, schemaRoot.getDataStore());
  dFsObjects  = findDescriptor(FileSystemObjects,schemaRoot.getDataStore());
  dContObject = findDescriptor(ContainerObject, schemaRoot.getDataStore());  	
  dContObject.setDepth(0);
  dContents   = findDescriptor(Contents, schemaRoot.getDataStore());
  dContents.setDepth(100);
  
  //Set up the abstract object descriptors:
	//  dCppObject          = createAbstractDerivativeDescriptor(dObject,          CppObject);
  dCppObject		  = createObjectDescriptor(schemaRoot, CppObject);
  dContCppObject      = createAbstractDerivativeDescriptor(dContObject,      ContCppObject);
  schemaRoot.getDataStore().createReference(dCppObject, dContCppObject, "abstracts", "abstracted by");
  dUsableCppObject    = createAbstractDerivativeDescriptor(dCppObject,       UsableCppObject);
  dClassesStructs     = createAbstractDerivativeDescriptor(dContCppObject,   ClassesStructs);
  dFunctions          = createAbstractDerivativeDescriptor(dContCppObject,   Functions);
  dVariables          = createAbstractDerivativeDescriptor(dUsableCppObject, Variables);
  dStatements         = createAbstractDerivativeDescriptor(dCppObject,   Statements);
  dAll                = createAbstractDerivativeDescriptor(dContCppObject,   All);
  dAll.setDepth(200); 

  //Set up the relations and commands for the above objects:
  //  dC_QUERY            = findDescriptor(Query, schemaRoot.getDataStore());
/***** DKM ****/
  /***** cont cpp overrides container object, whilc cpp object doesnt */
  //dC_QUERY            = createCommandDescriptor(dCppObject, "Query", "C_QUERY", false);
  dC_QUERY            = createCommandDescriptor(dContCppObject, "Query", "C_QUERY", false);
/**** DKM ****/
 
  dC_REMOVE_PARSE     = createCommandDescriptor(dFsObjects, RemoveParseInformation, "C_REMOVE_PARSE", false);
  dC_SAVE_PARSE       = createCommandDescriptor(dFsObjects, SaveParseInformation,   "C_SAVE_PARSE", false);
  dC_CODE_ASSIST      = createCommandDescriptor(dFsObjects, CodeAssist,             "C_CODE_ASSIST");
  dC_CODE_ASSIST.setDepth(0);
  dC_PROVIDE_SOURCE_FOR = createCommandDescriptor(dCppObject, ProvideSourceFor,     "C_PROVIDE_SOURCE_FOR");
  dC_PROVIDE_SOURCE_FOR.setDepth(0);
  
  dC_FIND_DECLARATION = createCommandDescriptor(dFsObjects, FindDeclaration,        "C_FIND_DECLARATION");
  dC_FIND_DECLARATION.setDepth(0);
  dC_SET_INCLUDE_PATH = createCommandDescriptor(dFsObjects, SetIncludePath,         "C_SET_INCLUDE_PATH");
  dC_SET_INCLUDE_PATH.setDepth(0);
  dC_SET_PREFERENCES  = createCommandDescriptor(dFsObjects, SetPreferences,         "C_SET_PREFERENCES");
  dC_SET_PREFERENCES.setDepth(0);

  DataElement dFile   = findDescriptor("file", schemaRoot.getDataStore());
  dC_PARSE = createCommandDescriptor(dFile, Parse, "C_PARSE");
  dC_PARSE.setDepth(0);
  createCommandDescriptor(dFile, RemoveParseInformation, "C_REMOVE_PARSE").setDepth(0);
  DataElement cancellable = findDescriptor("Cancellable",schemaRoot.getDataStore());
  schemaRoot.getDataStore().createReference(cancellable, dC_PARSE, "abstracts","abstracted by");
   
  dUses            = createRelationDescriptor(dUsableCppObject, Uses);
  dReturnType      = createRelationDescriptor(dFunctions,       ReturnType);
  dParameters      = createRelationDescriptor(dFunctions,       Parameters);
  dCallees         = createRelationDescriptor(dFunctions,       Callees);
  dCallers         = createRelationDescriptor(dFunctions,       Callers);
  dBaseClasses     = createRelationDescriptor(dClassesStructs,  BaseClasses);
  dDerivedClasses  = createRelationDescriptor(dClassesStructs,  DerivedClasses);
  dDeclVariables   = createRelationDescriptor(dClassesStructs,  DeclVariables);
  dTypes           = createRelationDescriptor(dVariables,       Types);
  
  //Make a few of these relationships invisible:
  dReturnType.setDepth(0);
  dParameters.setDepth(0);
  dDeclVariables.setDepth(0);
  dTypes.setDepth(0);
  /*
  DataElement dC_QUERY = crevateCommandDescriptor(dFunctions, "Query", "C_QUERY");
  createReference(dClassesStructs, dC_QUERY);
  createReference(dUsableCppObject, dC_QUERY);
*/

  //Create the actual Cpp Objects (non-abstract):
  dStatement         = createDerivativeDescriptor(dStatements,      Statement);
  dCompoundStatement = createDerivativeDescriptor(dStatements,      CompoundStatement);
  schemaRoot.getDataStore().createReference(dContObject, dCompoundStatement, "abstracts", "abstracted by");
  dMacro             = createDerivativeDescriptor(dUsableCppObject, Macro);
  dConstructor       = createDerivativeDescriptor(dFunctions,       Constructor);
  dDestructor        = createDerivativeDescriptor(dFunctions,       Destructor);
  dMainFunction      = createDerivativeDescriptor(dFunctions,       MainFunction);
  dFunction          = createDerivativeDescriptor(dFunctions,       Function);
  dClass             = createDerivativeDescriptor(dClassesStructs,  Class);
  dStruct            = createDerivativeDescriptor(dClassesStructs,  Struct);
  dUnion             = createDerivativeDescriptor(dClassesStructs,  Union);
  dNamespace         = createDerivativeDescriptor(dClassesStructs,  Namespace);
  dEnum              = createDerivativeDescriptor(dVariables,       Enum);
  dTypedef           = createDerivativeDescriptor(dVariables,       Typedef);
  dVariable          = createDerivativeDescriptor(dVariables,       Variable);
 
 
  //Create others
  dProject = createDerivativeDescriptor(dContObject, Project);

  dParseReference    = createRelationDescriptor(dProject, ParseReference);
  dParseReference.setDepth(0); //make it invisible
  dParsedFiles       = createDerivativeDescriptor(dContObject, ParsedFiles);
  dProjectObjects    = createDerivativeDescriptor(dContObject, ProjectObjects);
  dPreferences       = createDerivativeDescriptor(dContObject, Preferences);
  dError             = createDerivativeDescriptor(dContObject, Error);
  dTimeStamp         = createDerivativeDescriptor(dContObject, TimeStamp);

  //Set up the DataElements representing files:
  /**** DKM ****/
  ///dSourceFiles      = createAbstractDerivativeDescriptor(dFsObjects, SourceFiles);
  dSourceFiles      = createAbstractDerivativeDescriptor(dContCppObject, SourceFiles);
 
   /**** DKM ****/
   
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

  createReference(dFunctions,    dAll);
  createReference(dFunctions,    dVariables);
  createReference(dFunctions,    dStatements);
 
  createReference(dClassesStructs, dAll);
  createReference(dClassesStructs, dFunctions);
  createReference(dClassesStructs, dVariables);
  createReference(dUnion,          dVariables);


  dSourceFiles.setDepth(0);
  dVariables.setDepth(dMacro.depth() + 1);
  dClassesStructs.setDepth(dVariables.depth() + 1);
  dFunctions.setDepth(dClassesStructs.depth() + 1);
  dUses.setDepth(dContents.depth() + 100);
 }

 private static ResourceBundle _resourceBundle = null;

 public static String getLocalizedString(String key)
 {
  try
  {
   if (key == null)
    return "";
   if (_resourceBundle == null)
    _resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.cpp.miners.parser.dstore.ParserSchema");
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
  DataElement cmdD = descriptor.getDataStore().createCommandDescriptor(descriptor, name, "org.eclipse.cdt.cpp.miners.parser.ParseMiner", value);
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



