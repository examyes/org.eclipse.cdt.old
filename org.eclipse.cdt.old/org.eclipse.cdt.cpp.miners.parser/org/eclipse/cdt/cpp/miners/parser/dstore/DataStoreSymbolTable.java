package org.eclipse.cdt.cpp.miners.parser.dstore;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.parser.namelookup.*;
import java.lang.*;
import java.util.*;
import java.io.*;

//This is an implementation of the SymbolTable interface.  The SymbolTable interface contains methods
//that are used by Parser.jj, since Parser.jj can feed either the CommandLineSymbolTable or the 
//DataStoreSymbolTable.
public class DataStoreSymbolTable implements SymbolTable
{
 private SymbolObject  _curObj          = null;
 private DataElement   _root            = null;
 private DataStore     _dataStore       = null;
 private DataElement   _parsedFiles     = null;
 private Hashtable     _builtinTypes    = null;
 private Hashtable     _unresolvedTypes = null;
 private NameLookup    _nameLookup      = null;
 private Stack         _rootStack       = null; 
 private String        _currentSource   = null;
 private int           _quality;
 private int           _idCounter       = 1;
 private boolean       _isTypedef       = false;
 private DataElement   _currentDeclaration = null;
 
 public DataStoreSymbolTable()
 {
  super();
  _curObj          = new SymbolObject();
  _rootStack       = new Stack();
  _builtinTypes    = new Hashtable();
  _unresolvedTypes = new Hashtable();
  _nameLookup      = new NameLookup();
  _quality         = 3;
 }
 
 public void setParseQuality(DataElement quality)
 {
  try
  { 
   _quality = Integer.parseInt(quality.getValue()); 
  }
  catch (NumberFormatException e) 
  { 
   _quality = 3; 
  }
 }
 
 public void setRoot (DataElement theRoot)
 {
  _root          = theRoot;
  _dataStore     = theRoot.getDataStore();
  _currentSource = (String)theRoot.getElementProperty(DE.P_SOURCE_NAME);
  _curObj.object = null;
 }
 

 public void setParsedFiles(DataElement parsedFiles)
 {
  _parsedFiles = parsedFiles;
  _dataStore = _parsedFiles.getDataStore();
  _nameLookup.setProject(parsedFiles.getParent());
  if (!_builtinTypes.isEmpty())
   return;
  
  DataElement builtins = _dataStore.createObject(_parsedFiles, "Built In Types", "Built-In Types");
  _unresolvedTypes.clear();
  DataElement unresolvedTypesRoot = _dataStore.createObject(_parsedFiles, "Unresolved Types", "Unresolved Types");
  _unresolvedTypes.put("UNRESOLVEDROOT", unresolvedTypesRoot);
  builtins.setDepth(0);
  unresolvedTypesRoot.setDepth(0); 
  
  //Storage Class Specifiers
  _builtinTypes.put("auto",       _dataStore.createObject(builtins, ParserSchema.Types, "auto"));
  _builtinTypes.put("register",   _dataStore.createObject(builtins, ParserSchema.Types, "register"));
  _builtinTypes.put("static",     _dataStore.createObject(builtins, ParserSchema.Types, "static")); 
  _builtinTypes.put("extern",     _dataStore.createObject(builtins, ParserSchema.Types, "extern"));
  _builtinTypes.put("mutable",    _dataStore.createObject(builtins, ParserSchema.Types, "mutable"));

  //Simple Type Specifiers
  _builtinTypes.put("char",       _dataStore.createObject(builtins, ParserSchema.Types, "char"));
  _builtinTypes.put("wchar_t",    _dataStore.createObject(builtins, ParserSchema.Types, "wchar_t")); 
  _builtinTypes.put("bool",       _dataStore.createObject(builtins, ParserSchema.Types, "bool")); 
  _builtinTypes.put("short",      _dataStore.createObject(builtins, ParserSchema.Types, "short")); 
  _builtinTypes.put("int",        _dataStore.createObject(builtins, ParserSchema.Types, "int")); 
  _builtinTypes.put("long",       _dataStore.createObject(builtins, ParserSchema.Types, "long"));
  _builtinTypes.put("signed",     _dataStore.createObject(builtins, ParserSchema.Types, "signed")); 
  _builtinTypes.put("unsigned",   _dataStore.createObject(builtins, ParserSchema.Types, "unsigned"));
  _builtinTypes.put("float",      _dataStore.createObject(builtins, ParserSchema.Types, "float"));
  _builtinTypes.put("double",     _dataStore.createObject(builtins, ParserSchema.Types, "double"));
  _builtinTypes.put("void",       _dataStore.createObject(builtins, ParserSchema.Types, "void"));
  
  //Class/Enum/Namespace/Template and Elaborated Type Specifiers
  _builtinTypes.put("class",      _dataStore.createObject(builtins, ParserSchema.Types, "class"));
  _builtinTypes.put("struct",     _dataStore.createObject(builtins, ParserSchema.Types, "struct"));
  _builtinTypes.put("union",      _dataStore.createObject(builtins, ParserSchema.Types, "union"));
  _builtinTypes.put("enum",       _dataStore.createObject(builtins, ParserSchema.Types, "enum"));
  _builtinTypes.put("typename",   _dataStore.createObject(builtins, ParserSchema.Types, "typename"));
  _builtinTypes.put("namespace",  _dataStore.createObject(builtins, ParserSchema.Types, "namespace"));
  _builtinTypes.put("template",   _dataStore.createObject(builtins, ParserSchema.Types, "template"));
  _builtinTypes.put("asm",        _dataStore.createObject(builtins, ParserSchema.Types, "asm"));
   
  //CV Qualifiers
  _builtinTypes.put("const",      _dataStore.createObject(builtins, ParserSchema.Types, "const"));
  _builtinTypes.put("volatile",   _dataStore.createObject(builtins, ParserSchema.Types, "volatile")); 

  //Function Specifiers
  _builtinTypes.put("inline",     _dataStore.createObject(builtins, ParserSchema.Types, "inline"));
  _builtinTypes.put("virtual",    _dataStore.createObject(builtins, ParserSchema.Types, "virtual"));
  _builtinTypes.put("explicit",   _dataStore.createObject(builtins, ParserSchema.Types, "explicit"));
  
  //Other Decl Specifers
  _builtinTypes.put("friend",     _dataStore.createObject(builtins, ParserSchema.Types, "friend"));
  _builtinTypes.put("typedef",    _dataStore.createObject(builtins, ParserSchema.Types, "typedef"));    

  //Ptr Operators
  _builtinTypes.put("operator",   _dataStore.createObject(builtins, ParserSchema.Types, "operator"));
  _builtinTypes.put("*",          _dataStore.createObject(builtins, ParserSchema.Types, "* (Pointer To)"));
  _builtinTypes.put("&",          _dataStore.createObject(builtins, ParserSchema.Types, "& (Reference To)"));
 }
 
 public NameLookup nameLookup()
 {
  return _nameLookup;
 }

 public void setObjectTypedef(boolean isTypedef)
 {
  _isTypedef = isTypedef;
 }
 
 public void setCurrentDeclaration(DataElement type)
 {
  _currentDeclaration = type;
 }
 
 public boolean isObjectTypedef()
 {
  return _isTypedef;
 }
 
 public boolean doBodies()
 {
  return isSet(BODIES);
 }
 
 public void pushRoot()
 {
  if (_root != null)
   _rootStack.push(_root);
 }

 public void popRoot()
 {
  if (!_rootStack.empty())
  {
    DataElement newRoot = (DataElement)_rootStack.peek();
    setRoot(newRoot);
    _rootStack.pop();
  }
 }
  
 public boolean isInitialized()
 {
  return ((_parsedFiles != null) && (_dataStore != null));
 }
  
 public DataElement getRoot()
 {
  return _root;
 }
 
 public String getCurrentSource() 
 {
  return _currentSource;
 }

 public void setBuffer(StringBuffer s)
 {
  DataElement theObj = _curObj.object;
  if (theObj == null)
   return;
  theObj.setBuffer(s);
  theObj.setDepth(2);
  theObj.expandChildren();
 }
 public DataElement addObject(String objType, String objName, int beginLine, boolean isScope)
 {
  System.out.println("Bad Call to DataStoreSymbolTable.addObject...use the new interface that uses a DataElement for the descriptor");
  Thread.currentThread().dumpStack();
  return null;
 }
 
 public DataElement addObject(DataElement objType, String objName, String objValue, int beginLine, boolean isScope)
 {
  DataElement theObject = null;
  try
   {
   theObject = addObject(objType, objName, beginLine, isScope);
   theObject.setAttribute(DE.A_VALUE, objValue);
   }
  catch (Throwable e) 
  {
   e.printStackTrace();
  }
  return theObject;
 }
 
 public DataElement addObject(DataElement objType, String objName, int beginLine, boolean isScope)
 {
  if ((objName == null) || (objName.length() == 0))
   return null;
  
  if (objType.getName().equals(ParserSchema.Variable)) 
   objName = parseObjectName(objName.trim());
    
  if (_isTypedef && objType == ParserSchema.dVariable)
  {
   objType = ParserSchema.dTypedef;
  }  
  
  DataElement theObject = _dataStore.createObject(_root, objType, objName, _currentSource + ":" + beginLine);//, "" + _idCounter++);
  theObject.setAttribute(DE.A_VALUE,objName);
  
  if (isScope) 
   _root = theObject;
  
  _curObj.object = theObject;

  if ( isSet(USES) && !objType.getName().equals("error"))
   _curObj.createUseReferences();
  
  _curObj.createTypeReferences();
  
  if (objType == ParserSchema.dVariable && _currentDeclaration != null)
   _dataStore.createReference(_currentDeclaration, _curObj.object, ParserSchema.DeclVariables);
  
  if (    objType.getName().equals(ParserSchema.Function) 
      || objType.getName().equals(ParserSchema.MainFunction)
      || objType.getName().equals(ParserSchema.Constructor) 
      || objType.getName().equals(ParserSchema.Destructor)
    )
   _curObj.createFunctionReferences();
 
   _curObj.reset();
  
  return theObject;
 } 
 
 
 public String parseObjectName(String name)
 {
  //Find a [ character as in const char *c [3] [5], and remove everything after it.
  int firstIndex = 0;
  int lastIndex = name.length() - 1;
  int suffixes = name.indexOf("[");
  if (suffixes > 0)
  {
   lastIndex = suffixes - 1;
   while(lastIndex >=0 && Character.isWhitespace(name.charAt(lastIndex)))
    lastIndex--;
  }
   
  if (name.charAt(lastIndex) == ')')
  {
   suffixes = name.lastIndexOf("(", lastIndex);
   if (suffixes > 0)
   { 
    lastIndex = suffixes - 1;
    while(lastIndex >=0 && Character.isWhitespace(name.charAt(lastIndex))) 
     lastIndex--;    
   }
  }
   
  //Now find the last name
  int lastSpace = name.lastIndexOf(" ", lastIndex);
  if (lastSpace > 0)
  {
   firstIndex = lastSpace + 1;
   //Find the bitfield declarator ":"
   int lastColon = name.indexOf(':', lastSpace);
   if (lastColon > 0 && lastColon < lastIndex)
   {    
    lastIndex = lastColon - 1;
   }
  }

  String nameString = name.substring(firstIndex, lastIndex+1);
  
  String typeString = name.substring(0, firstIndex) + name.substring(lastIndex+1);
  
  //In the case of a function pointer, we want to represent the type as 
  // a single string.
  if (typeString.indexOf("(*)") >= 0 || typeString.indexOf("()") >= 0)
  {
   _curObj.addVariableType(lookupTypeElement(typeString));
   return nameString;
  }
  
  
  StringTokenizer tokenizer = new StringTokenizer(typeString);
  while (tokenizer.hasMoreTokens())
  {
    _curObj.addVariableType(lookupTypeElement(tokenizer.nextToken()));  
    // For now I just return after adding the first type...Fix this later!!!
    return nameString;
  }
   
  return nameString;
 }
 

 public void addObjectUse(String theUse)
 {
  if (!isSet(USES)) 
   return;
  
  _curObj.addUse(_nameLookup.nameLookup(theUse, _curObj.object));
 }

 public void addFunctionCall(String name, String params)
 {
  if (!isSet(TYPES)) return;
  
  name += "(";
  
  ArrayList theFuncs = _nameLookup.fuzzyNameLookup(name, _root);
  if (theFuncs.size() > 0)
  {
   //If we get here, then we know we have at least found a match for the function name...Now we will
   //Count the number of parameters in params by counting commas, and find the function call that
   //has the same number of parameters.
   
   _dataStore.createReference(_root, (DataElement)theFuncs.get(0), "Callees", "Callers");
 
  //Count the number of parameters by counting the commas:
  //int numParams = 1;
  //if (params.length() == 0)
  // numParams = 0;
  //int nextComma = 0;
  //int startFrom = 0;
  //while ( (nextComma = params.indexOf(",",startFrom)) > -1)
  // {
  // numParams++;
  //  
   // }
  }
  

 }
 
 private int countParameters(String name)
 {
  //Simply count the number of commas in name...the number of parameters is 1 greater.
  if (name.length() == 0) return 0;
  int params = 1;
  int nextComma=0;
  int startFrom=0;
  while ((startFrom < name.length()) && ((nextComma = name.indexOf(",",startFrom)) >= 0))
  {
   params++;
   startFrom = nextComma+1;
  }
  return params;
 }
   
 public void addFunctionParameter(String name)
 {
  if (!isSet(TYPES)) return;
  _curObj.addArgument(lookupTypeElement(name));
 }
 
 public void addFunctionReturnTypes(String name)
 {
  if (!isSet(TYPES) || (name == null) || (name.length() == 0)) 
   return;
  name = name.trim();
  
  int nextSpace = 0;
  while ( (nextSpace = name.indexOf(" ")) > 0)
  {
   _curObj.addReturnType(lookupTypeElement(name.substring(0,nextSpace)));
   name = name.substring(nextSpace + 1, name.length()).trim();
  }
  _curObj.addReturnType(lookupTypeElement(name));
 }
 
 
 private DataElement lookupTypeElement(String theType)
 {
  DataElement theObj = null;
  
  //First lookup types in the builtins...
  if ( (theObj = (DataElement)_builtinTypes.get(theType)) != null) 
   return theObj;
  
  //Now do a regular name lookup...
  if ( (theObj = _nameLookup.valueLookup(theType, _curObj.object)) != null)
   return theObj;

  //Now do a lookup in the unresolved Types
  if ( (theObj = (DataElement)_unresolvedTypes.get(theType)) != null)
   return theObj;

  //If we get here, we haven't found the type anywhere, so create it as an unresolved type.
  
  theObj = _dataStore.createObject((DataElement)_unresolvedTypes.get("UNRESOLVEDTYPESROOT"), ParserSchema.Types, theType);
  
  
  _unresolvedTypes.put(theType, theObj);
  return theObj;
 }
 
 public void addIncludeFile(String fileName)
 {
  if (!isSet(INHERITANCE)) 
   return;
  try
  {
   fileName = (new File (fileName)).getCanonicalPath();
  }
  catch (IOException e) {}
  DataElement incFile = _dataStore.find(_parsedFiles, DE.A_VALUE, fileName,1);
   if (incFile != null)
  {
   //Check this!!!!Root could be pointing at something other than a source object
   DataElement theRef = _dataStore.createReference(_root, incFile, ParserSchema.Includes, ParserSchema.IncludedBy);
   theRef.setAttribute(DE.A_VALUE, "#include \"" + incFile.getName() + "\"");
  }
 }
 
 public DataElement addPreprocessorError(String errorText, String fileName, int lineNumber)
 {
  return _dataStore.createObject(_root,"error", "PREPROCESSING ERROR " + errorText, 
                                 fileName + ":" + lineNumber, "" + _idCounter++);
 }
 
 public void closeScope ()
 {
  if ((_root == null) || (_root.getType().equals(ParserSchema.ParsedSource)) || (_root.getType().equals(ParserSchema.IncludedSource)))
   return;
  _root = _root.getParent();
 }
 
 public void gotoGlobalScope()
 {
  while( (!_root.getType().equals(ParserSchema.ParsedSource)) && (_root.getParent() != null))
   _root = _root.getParent();
 }
 
 public void addClassBase(String baseName)
 {
  if (!isSet(INHERITANCE)) 
   return;
  
  DataElement base = _nameLookup.nameLookup(baseName, ParserSchema.Class, _curObj.object);
  if (base != null)
   {
    _dataStore.createReference(_curObj.object,base,ParserSchema.BaseClasses);
    _dataStore.createReference(base,_curObj.object,ParserSchema.DerivedClasses);
   }
 }
 
 public void addClassMember(String name)
 {
  /*
  if ( (theClass == null) || (theMember == null) ) return;
  switch (access)
  {
   case 0: _dataStore.createReference(theMember, theClass, "public members");    break;
   case 1: _dataStore.createReference(theMember, theClass, "private members");   break;
   case 2: _dataStore.createReference(theMember, theClass, "protected members"); break;
  }
  */
 }
 
 //Preference Settings:
 private final int BODIES          = 1;
 private final int INHERITANCE     = 2;
 private final int USES            = 3;
 private final int TYPES           = 4;
 
 private boolean isSet(int setting)
 {
  switch (_quality)
  {
   //Fastest..no bodies or relationships
   case 0 :
   {
    return false;
   }
   
   //Some relationships (inheritance and type info)...no Uses though.
   case 1:
   {
    switch (setting)
    {
     case INHERITANCE: return true;
     case TYPES:       return true;
    }
    return false;
   }

   //Bodies and all relationships except for Uses.
   case 2:
   {
    switch (setting)
    {
     case USES: return false;
    }
    return true;
   }

   //Everything
   case 3:
   {
    return true;
   }
  }
  return false;
 }
}




;
