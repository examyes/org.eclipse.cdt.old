package com.ibm.cpp.miners.parser.dstore;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.miners.parser.namelookup.*;
import java.lang.*;
import java.util.*;

//This is the base class for the DataStoreSymbolTable and CommandLineSymbolTable.  Whenever a new 
//SymbolTable method is needed by Parser.jj, make sure you put it here, so that both child classes
//are required to implement it.
public interface SymbolTable
{ 
 //Methods that actually create or deal with DataElement in the dataStore.
 public abstract DataElement addObject(DataElement type, String name, int line, boolean hasChildren);
 public abstract DataElement addObject(DataElement type, String name, String value, int line, boolean hasChildren);
 public abstract DataElement addObject(String type, String name, int line, boolean hasChildren);
 public abstract void        closeScope();
 public abstract void        gotoGlobalScope();
 
 //Methods that add reference information
 public abstract void addObjectUse(String name);
 public abstract void addFunctionParameter(String name);
 public abstract void addFunctionReturnTypes(String name);
 public abstract void addFunctionCall(String name, String params);
 public abstract void addClassBase(String name);
 public abstract void addClassMember(String name);
 public abstract void addIncludeFile(String name);

 //Getters
 public abstract DataElement getRoot();
 
 //Setters...GET RID OF THESE!!!!
 public abstract boolean doBodies();
 public abstract void objectIsTypedef();
 
}


