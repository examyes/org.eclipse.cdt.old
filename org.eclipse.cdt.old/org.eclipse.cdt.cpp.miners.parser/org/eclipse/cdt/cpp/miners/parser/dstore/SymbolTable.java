package org.eclipse.cdt.cpp.miners.parser.dstore;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.parser.namelookup.*;
import java.lang.*;
import java.util.*;

//This is the base class for the DataStoreSymbolTable
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
 public abstract boolean isObjectTypedef();
 
 //Setters...GET RID OF THESE!!!!
 public abstract boolean doBodies();
 public abstract void setObjectTypedef(boolean isTypedef);
 public abstract void setCurrentDeclaration(DataElement type);
 
}


