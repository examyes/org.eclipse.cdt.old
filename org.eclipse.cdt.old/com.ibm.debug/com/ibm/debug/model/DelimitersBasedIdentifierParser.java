package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1999, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DelimitersBasedIdentifierParser.java, java-model, eclipse-dev, 20011128
// Version 1.1.2.2 (last modified 11/28/01 16:14:05)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import com.ibm.debug.model.*;
import java.util.*;


/**
 * This abstruct class finds identifiers in strings for languages which
 * can clearly determine tocken delimiters. It must be subclassed
 * and the getDelimiters() method must be overriden.
 */
public abstract class DelimitersBasedIdentifierParser extends IdentifierParser
{

   protected Hashtable table = new Hashtable();


   /**
    * Set up the finder
    */
   public DelimitersBasedIdentifierParser()
   {
       char [] chars = getDelimiters();

       for (int i = 0; i < chars.length ; i++)
       {
           table.put(new Character(chars[i]), new Character(chars[i]));
       }
   } // end constructor


   /**
    * This abstract method must be overriden to return the delimiters for that language
    */
   abstract public char [] getDelimiters();

   /**
    * Given a string and index within that string the positions within the string for the encompassing
    * identifier
    */
   public int [] identifierInString(String input, int index)
   {
       if (Model.TRACE.DBG)
           Model.TRACE.dbg(3, "DelimitersBasedIdentifierParser.identifierInString(" + input + "," + index + ")");
       if (input == null || index+1 > input.length())
       {
         return null;
       }

       int [] ans = new int [2];
       int left = 0;
       int right = input.length() - 1;

       char [] buffer = input.toCharArray();

       // Look left

       for (int i = index; i >= 0; i--)
       {
           if (table.containsKey(new Character(buffer[i])))
           {
              left = i + 1;
              break;
           }
       }
       //
       // The position is itself a delimiter.  Return -1, -1
       //
       if (left > index)
       {
          ans[0] = -1;
          ans[1] = -1;
          return ans;
       }

       // Look right

       for (int i = index + 1; i < buffer.length; i++)
       {
           if (table.containsKey(new Character(buffer[i])))
           {
              right = i - 1;
              break;
           }
       }

       ans[0] = left;
       ans[1] = right;
       return ans;
   } // end identifierInString()


   /**
    * Given a string and index return the string identifier for the contained idenfifier
    */
   public String findIdentifier(String input, int index)
   {
       if (Model.TRACE.DBG)
           Model.TRACE.dbg(3, "DelimitersBasedIdentifierParser.findIdentifier(" + input + "," + index + ")");

       int [] pos = identifierInString(input, index);

       if (pos == null || pos[0] == -1) return null;

       return input.substring(pos[0], pos[1]+1);
   } // findIdentifier()

} // end class DelimitersBasedIdentifierParser()




