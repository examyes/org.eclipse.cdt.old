package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1999, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/IdentifierParserForFreeFormRPG.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:14:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This class finds identifiers in free form RPG strings.
 */
//This class was added to support Free Form RPG parsing.
public class IdentifierParserForFreeFormRPG extends DelimitersBasedIdentifierParser
{

   //Don't include '(' or ')', so we can tokenize a(x)
   static final private char [] delims = {'*', '<', '>', ':', '\'', '\"',
                                          ';', '+', '-', '=', '/', ' ',
                                          ',', '\\' };

   /**
    * Setup
    */
   public IdentifierParserForFreeFormRPG() {}

   /**
    * This method must be overriden to return the delimiters for that language
    */
   public char [] getDelimiters()
   {
      return delims;
   }

   public int [] identifierInString(String input, int index)
   {

     if (input == null || index+1 > input.length())
     {
         return null;
     }


     //Do the recursive call...
     char [] buffer = input.toCharArray();

     return identifierInSubString (buffer, index, 0, buffer.length-1);
   }//end identifierInString

   /**
    * This recursive function will get the token depending on the cursion postion and
    * the context of the line.
    * If it detects the cursor is with a pair of enclosing bracket, it will call
    * itself again.
    */
   public int [] identifierInSubString(char[] buffer, int index, int start, int end)
   {
       int [] ans = new int [2];

       int left = start;
       int right = end;

       // Look left

       for (int i = index; i >= start; i--)
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

       for (int i = index + 1; i <= end; i++)
       {
           if (table.containsKey(new Character(buffer[i])))
           {
              right = i - 1;
              break;
           }
       }

       //Look for bracket mismatching
       int l_paren = 0;
       int r_paren = 0;
       int first_l_paren = 0;
       int last_r_paren  = 0;
       int match_r_paren = 0;
       int match_l_parne = 0;

       //First case, looking for the left bracket from left to right.
       //This cover the most common cases.
       for(int i=left; i<=right; i++)
       {
         if('(' == buffer[i])
         {
           if (first_l_paren == 0)
           {
             first_l_paren = i;
           }
           ++l_paren;
         }//end if
         if(')' == buffer[i])
         {
           --l_paren;
           if(l_paren == 0)
           {
             //find the matching paren
             right=i;
             match_r_paren = i;
             break;
           }//end if
         }//end if
       }//end for

       //If we did not find any left bracket within [left, right], we will skip white space and
       //peek the first non-white space to see if it is a left bracket.
       //This cover the case like  "Array (a)", and the cursor position is on 'r'.
       if (0 == first_l_paren)
       {
         int i = right+1;
         while(true)
         {
           if(i > end)
           {
             break;
           }//end if

           if(' ' != buffer[i])
           {
             if('(' == buffer[i])
             {
               ++l_paren;
               first_l_paren = i;
               right = i;
             }//end if
             break;
           }//end if

           ++i;  //Otherwise, just skip to next.
         }//end while
       }//end if

       //If we cannot find the matching bracket within [left, right], then keep on looking;
       if(l_paren > 0)
       {
         for (int i=right+1; i<=end; i++)
         {
           if('(' == buffer[i])
           {
             ++l_paren;
           }//end if
           if(')' == buffer[i])
           {
             --l_paren;
             if(l_paren == 0)
             {
               //find the matching paren
               right=i;
               match_r_paren = i;
               break;
             }//end if
           }//end if
         }//end for
       }//end if
       else if (l_paren < 0)  //Right bracket is more than left bracket
       {
         //Look back to see its matching
         for (int i=right; i>=start; --i)
         {
           if (')' == buffer[i])
           {
             if (last_r_paren == 0)
             {
               last_r_paren = i;
             }
             ++r_paren;
           }//end if
           if('(' == buffer[i])
           {
             --r_paren;
             if(r_paren == 0)
             {
               //find the matching paren
               first_l_paren = i;
               match_r_paren = last_r_paren;
               left = i;
               break;
             }//end if
           }//end if
         }//end for
       }//end if

       //if we still cannot find the matching parens, something wrong
       if (l_paren > 0 || r_paren > 0)
       {
         ans[0] = -1;
         ans[1] = -1;
         return ans;
       }

       else
       {
         //We found the matching bracket.
         //But we need to consider the case arry (abc * edf).fgh.efg.eab
         //Note, . is not a delimiter for us
         if (end > match_r_paren)
         {
           //See if the next character is '.'
           if('.' == buffer[match_r_paren+1])
           {
             //Keep on looking until we found a delimiter
             for (int i = match_r_paren + 1; i <= end; i++)
             {
               if (table.containsKey(new Character(buffer[i])) ||
                   '(' == buffer[i]                            ||
                   ')' == buffer[i] )
               {
                  right = i - 1;
                  break;
               }//end if
             }//end for

           }//end if
         }//end if

       }//end else


     // If the position is inside the opening and closing parens
     if (index > first_l_paren && index < match_r_paren)
     {
       //Do recursive call here
       return identifierInSubString(buffer, index, first_l_paren+1, match_r_paren-1);

     }

     else if (index == first_l_paren || index == match_r_paren)
     {
          ans[0] = -1;
          ans[1] = -1;
          return ans;
     }
     else
     {
       if (left == first_l_paren)
       {
         //Righ now it highlights the bracket.  We need to find its array name or procedure
         //name
         //first, skip the white space.
         int k = left - 1;
         while(true)
         {
           if(k < start)
           {
             break;
           }//end if

           if(' ' != buffer[k])
           {
             break;
           }//end if

           --k;  //Otherwise, just skip to next.
         }//end while

         //It cannot be only the bracket.  So error condition...
         if (k < start)
         {
           ans[0] = -1;
           ans[1] = -1;
           return ans;
         }

         //Now we looking for the delimiter.  Now space is also a delimiter.
         for (int i = k - 1; i >= start; --i)
         {
           if ((table.containsKey(new Character(buffer[i])) && ' ' != buffer[i]) ||
                ' ' == buffer[i]                                                 ||
                '(' == buffer[i]                                                 ||
                ')' == buffer[i] )
           {
             left = i+1;
             break;
           }
         }
       }

       //Do not support buildin function.
       if ('%' == buffer[left])
       {
          ans[0] = -1;
          ans[1] = -1;
          return ans;
       }


       //Do some language specific processing for indicator.

       if (('I' == buffer[left] || 'i' == buffer[left])  &&
         ('N' == buffer[left+1] || 'n' == buffer[left+1]) )
       {
         if(left>=1 && buffer[left-1] == '*')
         {
           //this is the *IN or *in case, we need to get the *
           left = left - 1;
         }
       }


       ans[0] = left;
       ans[1] = right;
       return ans;
     }
   }//end identifierInSubString

} // end class IdentifierParserForFreeFormRPG




