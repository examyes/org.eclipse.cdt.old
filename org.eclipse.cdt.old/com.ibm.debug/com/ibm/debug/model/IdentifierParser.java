package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1999, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/IdentifierParser.java, java-model, eclipse-dev, 20011128
// Version 1.1.3.2 (last modified 11/28/01 16:14:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This interface finds identifiers in strings
 */
public abstract class IdentifierParser
{
   /**
    * Given a string and index within that string the positions within the string for the encompassing
    * identifier
    */
   abstract public int [] identifierInString(String input, int index);

   /**
    * Given a string and index return the string identifier for the contained idenfifier
    */
   public String findIdentifier(String input, int index)
   {
       int [] pos = identifierInString(input, index);

       if (pos == null || pos[0] == -1) return null;

       return input.substring(pos[0], pos[1]+1);
   } // end findIdentifier()



   /**
    * Given a highlighted token a line where this toake is found and starting
    * position of the token within this line, update the token if required by
    * the specific language parser (e.g. RPG indicators)
    *
    * NOTE: default implementation returns the original token      cmvc14098A
    */
   public String doLanguageSpecifics(String token, String line, int index)
   {
      return token;
   } // end doLanguageSpecifics()

} // end class IdentifierParser()


