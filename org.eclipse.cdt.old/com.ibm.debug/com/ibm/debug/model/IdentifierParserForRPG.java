package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1999, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/IdentifierParserForRPG.java, java-model, eclipse-dev, 20011128
// Version 1.2.3.2 (last modified 11/28/01 16:14:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class IdentifierParserForRPG extends ColumnBasedIdentifierParser
{
   /**
    * The original code is based on IString, whose index is 1 based.
    * In Java, String's index is 0 based.
    * I do not know which one will be used. So the following variable
    * is used to dynamically select 1 or 0 based index.
    * This flag can only be set by constructor.              -- rlin
    */
   private boolean _stringIndexIs0Based = false;

   /**
    * constructor
    */
   public IdentifierParserForRPG(boolean stringIndexIs0Based)
   {
      _stringIndexIs0Based = stringIndexIs0Based;
   }

   /******************************************************************************/
   /*                                                                            */
   /*  parseRPGwords -  do parsing for RPG debuggers                             */
   /*                                                                            */
   /******************************************************************************/
   private char _specifier;
   private boolean  _is_comment = false;
   private boolean  _is_indicator = false;
   private int _start_pos = 0;
   private int _end_pos = 0;
   private IdentifierParser freeFormParser = null; //cmvc18024A FreeForm RPG

   public int [] identifierInString(String line, int startChar)
   {
      // this function uses 1-based index
      if (_stringIndexIs0Based) {
         startChar++;
      }

      // global variables
      int src_prefixlen = SRCCLIENT.src_prefixlen;

      final int linelen = line.length();
      int char_idx = startChar - src_prefixlen - 1;

      //cmvc18024A Check if it is a free from RPG
      if (true == ifFreeForm(line, char_idx))
      {
        //then we use the free form parser, which is a DelimitersBasedIdentifierParser
        if (null == freeFormParser)
        {
          freeFormParser = new IdentifierParserForFreeFormRPG();
        }
        return freeFormParser.identifierInString(line, char_idx);
      }
      //end cmvc18024

      if (!sourceRPGLineInfo(line, char_idx))
      {
         return null;
      } // end if()

      int wordstart = _start_pos;
      int wordend = _end_pos;

      // cmvc 8434: Adjust wordend for short lines:
      //if ((wordstart+src_prefixlen+1 > linelen) ||
      //    (wordend+src_prefixlen+1 > linelen)) {
      //  return false;
      //}

      if( wordend + src_prefixlen + 1 > linelen)
        wordend = linelen - 1 - src_prefixlen;

      /*----------------------------------------------------------------*/
      /* Do the following parsing for special delimiters     cmvc 8434  */
      /*----------------------------------------------------------------*/
      boolean colon_found = false;
      int i;

      /*------------------------------------------------------------------*/
      /* First we search for the location of a colon to the right or      */
      /* left of the starting character;                                  */
      /*------------------------------------------------------------------*/
      for( i = char_idx; i <= Min(wordend, linelen - 1); i++)                   // @recordLen
      {
         if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) == ':' )  // @recondLen
         {
            wordend=i-1;
            colon_found = true;
            break;
         } // end if()
      } // end for()

      if( !colon_found )
      {
         for(i = Min(char_idx, linelen - 1); i >= wordstart; i--)                // @recordLen
         {
            if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) == ':' )  // @recordLen
            {
               wordstart=i+1;
               colon_found = true;
               break;
            } // end if()
         } // end for()
      } // end if()

      int bend_old = wordend;
      int bstart_old = wordstart;
      boolean right_paren_found = false;
      boolean left_paren_found = false;

      /*--------------------------------------------------------------------*/
      /* Search for a '(' && ')', select what's in the parens               */
      /*--------------------------------------------------------------------*/
      for( i = char_idx; i <= Min(bend_old, linelen - 1); i++)                // @recordLen
      {
         if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1))==')' )  // @recordLen
         {
            wordend = i - 1;
            right_paren_found = true;
            break;
         } // end if()
      } // end for()

      if( right_paren_found )
      {
         for( i = Min(char_idx, linelen - 1); i >= bstart_old; i-- )            // @recordLen
         {
            if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1))=='(')  // @recordLen
            {
               wordstart= i + 1;
               left_paren_found = true;
               break;
            } // end if()
         } // end for()
      } // end if()

      if( !(right_paren_found && left_paren_found &&
            (char_idx >= wordstart) && (char_idx <= wordend)) )
      {
         wordstart = bstart_old;
         wordend = bend_old;
      }


      boolean nonblank = false;
      for( i = Min(wordend, linelen - 1); i >= wordstart; i-- )                 // @recordLen
      {
         if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) != ' ' )  // @recordLen
         {
            nonblank = true;
            break;
         } // end if()
      } // end for()

      if( !nonblank )
        return null;

      wordstart = wordstart     + src_prefixlen + 1;   /* take care of starting blank */
      wordend   =                 src_prefixlen + i;   /* fragment is not inclusive  @recordLen */

      // If we found a left parenthesis on the left of the cursor and
      // the last character of the word is a ')', then do not select the last character
      if( !(left_paren_found && line.charAt(Min(i, linelen - 1)) == ')') )       // @recordLen
         wordend++;                                                              // @recordLen

      int [] ans = new int [2];
      ans[0] = wordstart - (_stringIndexIs0Based ? 1 : 0);
      ans[1] = wordend - (_stringIndexIs0Based ? 1 : 0);


      //cmvc18095A - For indicator (INxx or inxx), we need to put the '*' in front
      //Do some language specific processing

       if (('I' == line.charAt(ans[0]) || 'i' == line.charAt(ans[0]))  &&
           (ans[0]+1 < ans[1])                                         &&
           ('N' == line.charAt(ans[0]+1) || 'n' == line.charAt(ans[0]+1)) )
       {
         if(ans[0]>=1 && line.charAt(ans[0]-1) == '*')
         {
           //this is the *IN or *in case, we need to get the *
           ans[0] = ans[0] - 1;
         }
       }

      //end cmvc18095A

      return ans;

   } // end identifierInString()




   /**
    * Given a highlighted token a line where this toake is found and starting
    * position of the token within this line, update the token if required by
    * the specific language parser (e.g. RPG indicators)
    *
    * NOTE: For ILE RPG we tack '*IN' in front of indicators       cmvc14098A
    */
   public String doLanguageSpecifics(String token, String line, int index)
   {
      if( null == token )  // in case we get an invalid token
        return null;       // do no extra work - just return null

      String updatedToken   = null;

      // Remember old values of private members that may change
      char     specifier    = _specifier;
      boolean  is_comment   = _is_comment;
      boolean  is_indicator = _is_indicator;
      int      start_pos    = _start_pos;
      int      end_pos      = _end_pos;
      int      start_index  = index;

      // this function uses 1-based index
      if( _stringIndexIs0Based )
        start_index++;


      _is_comment   = false; // not a comment
      _is_indicator = false; // not an indicator

      if( token.length() <= 2 )                       // length of indicators is <= 2
      {
        if( sourceRPGLineInfo(line, start_index) )    // parse the line for token at start_index
        {
          if( _is_indicator )                         // if it happens to be an indicator
            updatedToken = new String("*IN" + token); // add '*IN' in front of it
        } // end if()
      } // end if(token length is <= 2)


      if( null == updatedToken )                      // if updatedToken is still null cmvc14559A
        updatedToken = new String(token);             // return the original token     cmvc14559A


      // Restor original values of private members that might have changed
      _specifier    = specifier;
      _is_comment   = is_comment;
      _is_indicator = is_indicator;
      _start_pos    = start_pos;
      _end_pos      = end_pos;

      return updatedToken;                          // return the updated token
   } // end doLanguageSpecifics()




   /******************************************************************************/
   /*                                                                            */
   /*  sourceRPGLineInfon - Take the current line and gather all the RPG info.   */
   /*                                                                            */
   /******************************************************************************/
   private boolean sourceRPGLineInfo(String line, int char_idx)
   {
      // this function uses 1-based index
      // and returns 1-based index. - rlin

      // global variables
      int src_prefixlen = SRCCLIENT.src_prefixlen;
      int bstart = 0; // 1-based index
      int bend = 0; // 1-based index
      boolean is_listing=false;  //cmvc 8434

      int len = line.length();
      len = len - src_prefixlen - 1;
      if( len <= 0 ) {
         return false;
      }

      _start_pos = 0;
      _end_pos = 0;
      _is_comment = false;
      _is_indicator = false;

      // CMVC 7655-1 Check if we are in a listing view
      if( ((line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) == ' ') &&   // @recordLen
           (line.charAt(Min(7 + src_prefixlen + 1 - 1, len)) != '*')  )   // @recordLen
                                       ||
          ((line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) >= '0') &&   // @recordLen
           (line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) <= '9')  ) ) // @recordLen
        is_listing = true; // found a listing view


      /*------------------------------------------------------------------*/
      /*                                                                  */
      /* Spec    Columns                                                  */
      /*   H       None                                                   */
      /*   F       None                                                   */
      /*   D       7-21                                                   */
      /*   I       21-30 53-58 69-70 71-72 73-74                          */
      /*   C       7-8 9-22 33-46 47-60 68-69 70-71 72-73                 */
      /*   O       22-23 25-26 28-29 30-43                                */
      /*                                                                  */
      /* Additionally, in the Cspec for columns 9-22 and 33-46 it is      */
      /* possible that we need to parse out an item within () or to the   */
      /* left or right of a : (depending on where the user clicks).       */
      /*                                                                  */
      /* Note: for listing view, we'll add 5 to these columns.            */
      /*------------------------------------------------------------------*/

      if( len >= 7)
      {
         if( !is_listing )
         {
            if( line.charAt(Min(7 + src_prefixlen + 1 - 1, len)) == '*' )  // @recordLen
            {
               _is_comment = true;
               return true;
            } // end if()
            _specifier = line.charAt(Min(6 + src_prefixlen + 1 - 1, len)); // @recordLen

            switch( line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) )     // @recordLen
            {
               case 'D':
               case 'd':
                  if((char_idx >=7) && (char_idx <= 21))
                  {
                     bstart = 7;
                     bend   = 21;
                  }
                  else
                     return false;
                  break;
               case 'I':
               case 'i':
                  if((char_idx >=21) && (char_idx <= 30))
                  {
                     bstart = 21;
                     bend   = 30;
                  }
                  else
//        if((char_idx >=53) && (char_idx <= 58))    // 10/18/95
                  if((char_idx >=49) && (char_idx <= 62))
                  {
                     bstart = 49;
                     bend   = 62;
                  }
                  else
                  {
                     switch( char_idx )
                     {
                        case 69:
                        case 70:
                           bstart = 69;
                           bend   = 70;
                           _is_indicator = true;
                           break;
                        case 71:
                        case 72:
                           bstart = 71;
                           bend   = 72;
                           _is_indicator = true;
                           break;
                        case 73:
                        case 74:
                           bstart = 73;
                           bend   = 74;
                           _is_indicator = true;
                           break;
                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;
               case 'O':
               case 'o':
                  if((char_idx >=30) && (char_idx <= 43))
                  {
                     bstart = 30;
                     bend   = 43;
                  }
                  else
                  {
                     switch( char_idx )
                     {
                        case 22:
                        case 23:
                           bstart = 22;
                           bend   = 23;
                           _is_indicator = true;
                           break;
                        case 25:
                        case 26:
                           bstart = 25;
                           bend   = 26;
                           _is_indicator = true;
                           break;
                        case 28:
                        case 29:
                           bstart = 28;
                           bend   = 29;
                           _is_indicator = true;
                           break;
                        default:
                           return false;
                     } // end swtich()
                  } // end else()
                  break;
               case 'C':
               case 'c':
                  if( (char_idx >=36) && (char_idx <= Min(80, len)) )  // @recordLen
                  {  // ext factor 2 cmvc 8434
                     // check for free form opcode
                     String freeFormOpcode = new String("CALLPDOUDOWEVALIFRETURNWHEN");
                     int begin_index = Min(src_prefixlen + 1 + 26 - 1, len);                 // @recordLen
                     String opcode = line.substring(begin_index, Min(6 + begin_index, len)); // @recordLen
                     int index = opcode.indexOf('(');

                     if( index >= 0 )
                     {
                        opcode = opcode.substring(0, Min(index, len)); // strip off any opcode extender
                     }

                     opcode = opcode.trim();
                     opcode = opcode.toUpperCase();

                     if( freeFormOpcode.indexOf(opcode) >= 0 )
                     {
                        int i;

                        // check if we are pointing to parens
                        // special checking is needed for supporting ar(x) as a token
                        char c =  line.charAt(Min(char_idx + src_prefixlen + 1 - 1, len)); // @recordLen
                        if ((c == '(') || (c == ')'))
                           return false;

                        bend = 80;
                        for(i = char_idx; i <= len; i++)                          // @recordLen
                        {
                           c = line.charAt(Min(i + src_prefixlen + 1 - 1, len));  // @recordLen
                           // search for operators > = < : / - + * or space
                           // do not include '(' or ')' so we can tokenize a(x)
                           if ( ((c >= ':') && (c <= '>'))  ||
                                ((c >= '*') && (c <= '/'))  ||
                                (c == ' ') || (c == '\'') )
                           {
                              bend = i-1;
                              break;
                           } // end if()
                        } // end for()


                        bstart = 36;

                        for(i = Min(char_idx, len); i >= 36; i--)                 // @recordLen
                        {
                           c = line.charAt(Min(i + src_prefixlen + 1 - 1, len));  // @recordLen
                           // search for operators > = < : / - + * or space
                           if ( ((c >= ':') && (c <= '>'))  ||
                                ((c >= '\'') && (c <= '/')) ||
                                (c == ' ') )
                           {
                              bstart = i+1;
                              break;
                           } // enf if()
                        } // end for()

                        if( line.charAt(Min(bstart + src_prefixlen + 1 - 1, len)) == '%' )  // @recordLen
                           return false;    // do not select built-ins
                        else
                        {
                           // look for '(' ')' mismatches
                           int l_paren = 0;

                           for(i = char_idx; i <= Min(bend, len); i++)               // @recordLen
                           {
                              c = line.charAt(Min(i + src_prefixlen + 1 - 1, len));  // @recordLen
                              if (c == '(') ++l_paren;
                              if (c == ')')
                              {
                                 if (--l_paren < 0)
                                 {
                                    bend = i-1;
                                    break;
                                 } // end if()
                              } // end if()
                           } // end for()

                           if (l_paren > 0) return false; //no closing paren, e.g. proc(a:
                        } // end else()
                        break; // break from this case only if we had a free form opcode
                     } // end of free form expression processing
                  } // end of 36 <-> 80

                  if((char_idx >=12) && (char_idx <= 25))
                  {
                     bstart = 12;
                     bend   = 25;
                  }
                  else
                  if((char_idx >=36) && (char_idx <= 49))
                  {
                     bstart = 36;
                     bend   = 49;
                  }
                  else
                  if((char_idx >=50) && (char_idx <= 63))
                  {
                     bstart = 50;
                     bend   = 63;
                  }
                  else
                  {
                     switch( char_idx )
                     {
                        case  7:
                        case 10:
                        case 71:
                        case 73:
                        case 75:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;

                        case  8:
                        case 11:
                        case 72:
                        case 74:
                        case 76:
                           bstart = char_idx - 1;
                           bend   = char_idx;
                           _is_indicator = true;
                           break;

                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;
               default :
                  return false;
            } // end swtich()
         } // end if(not listing view)
         else   // listing view CMVC 8434
         {
            if( line.charAt(Min(7 + src_prefixlen + 1 + 5 - 1, len))=='*' )  // @recordLen
            {
               _is_comment = true;
               return true;
            }
            _specifier = line.charAt(Min(11 + src_prefixlen + 1 - 1, len));  // @recordLen

            switch( line.charAt(Min(11 + src_prefixlen + 1 - 1, len)) )      // @recordLen
            {
               case 'D':
               case 'd':
                  if((char_idx >=12) && (char_idx <= 26))
                  {
                     bstart = 12;
                     bend   = 26;
                  }
                  else
                     return false;
                  break;
               case 'I':
               case 'i':
                  if((char_idx >=26) && (char_idx <= 35))
                  {
                     bstart = 26;
                     bend   = 35;
                  }
                  else
                  if((char_idx >=54) && (char_idx <= 67))
                  {
                     bstart = 54;
                     bend   = 67;
                  }
                  else
                  {
                     switch( char_idx )
                     {
                        case 74:
                        case 75:
                           bstart = 74;
                           bend   = 75;
                           _is_indicator = true;
                           break;
                        case 76:
                        case 77:
                           bstart = 76;
                           bend   = 77;
                           _is_indicator = true;
                           break;
                        case 78:
                        case 79:
                           bstart = 78;
                           bend   = 79;
                           _is_indicator = true;
                           break;
                        default:
                           return false;
                     } // end swtich()
                  } // end else()
                  break;
               case 'O':
               case 'o':
                  if((char_idx >=35) && (char_idx <= 48))
                  {
                     bstart = 35;
                     bend   = 48;
                  }
                  else
                  {
                     switch( char_idx )
                     {
                        case 27:
                        case 28:
                           bstart = 27;
                           bend   = 28;
                           _is_indicator = true;
                           break;
                        case 30:
                        case 31:
                           bstart = 30;
                           bend   = 31;
                           _is_indicator = true;
                           break;
                        case 33:
                        case 34:
                           bstart = 33;
                           bend   = 34;
                           _is_indicator = true;
                           break;
                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;
               case 'C':
               case 'c':
                  if((char_idx >=41) && (char_idx <= 85)) // ext factor 2 cmvc 8434
                  {
                     // check for free form opcode
                     String freeFormOpcode = new String("CALLPDOUDOWEVALIFRETURNWHEN");
                     int begin_index = Min(src_prefixlen + 1 + 31 - 1, len);                 // @recordLen
                     String opcode = line.substring(begin_index, Min(6 + begin_index, len)); // @recordLen
                     int index = opcode.indexOf('(');

                     if( index >= 0 )
                     {
                        opcode = opcode.substring(0, Min(index, len)); // strip off any opcode extender
                     }
                     opcode = opcode.trim();
                     opcode = opcode.toUpperCase();
                     if (freeFormOpcode.indexOf(opcode) >= 0)
                     {
                        int i;

                        // check if we are pointing to parens
                        // special checking is needed for supporting ar(x) as a token
                        char c = line.charAt(Min(char_idx + src_prefixlen + 1 - 1, len));    // @recordLen
                        if ((c == '(') || (c == ')'))
                           return false;

                        bend = 85;
                        for(i = char_idx; i <= Min(85, len); i++)                 // @recordLen
                        {
                           c = line.charAt(Min(i + src_prefixlen + 1 - 1, len));  // @recordLen
                           // search for operators > = < : / - + * or space
                           // do not include '(' or ')' so we can tokenize a(x)
                           if ( ((c >= ':') && (c <= '>'))  ||
                                ((c >= '*') && (c <= '/'))  ||
                                (c == ' ') || (c == '\'') )
                           {
                              bend = i-1;
                              break;
                           } // end if()
                        } // end for()

                        bstart = 41;

                        for(i = Min(char_idx, len); i >= 41; i--)                 // @recordLen
                        {
                           c = line.charAt(Min(i + src_prefixlen + 1 - 1, len));  // @recordLen
                           // search for operators > = < : / - + * or space
                           if ( ((c >= ':') && (c <= '>'))  ||
                                ((c >= '\'') && (c <= '/'))  ||
                                (c == ' ') )
                           {
                              bstart = i+1;
                              break;
                           } // end if()
                        } // end for()

                        if( line.charAt(Min(bstart + src_prefixlen + 1 - 1, len)) == '%') // @recordLen
                           return false;    // do not select built-ins
                        else
                        {
                           // look for '(' ')' mismatches
                           int l_paren = 0;
                           for( i = char_idx; i <= Min(bend, len); i++)                   // @recordLen
                           {
                              c = line.charAt(Min(i + src_prefixlen + 1 - 1, len));       // @recordLen
                              if (c == '(') ++l_paren;
                              if (c == ')')
                              {
                                 if (--l_paren < 0)
                                 {
                                    bend = i-1;
                                    break;
                                 } // end if()
                              } // end if()
                           } // end for()
                           if( l_paren > 0 )
                             return false; //no closing paren, e.g. proc(a:
                        } // end else()
                        break; // break from this case only if we had a free form opcode
                     } // end of free form expression processing
                  } // end of 41 <-> 85    (36 <-> 80 on source view)

                  if((char_idx >=17) && (char_idx <= 30))
                  {
                     bstart = 17;
                     bend   = 30;
                  }
                  else
                  if((char_idx >=41) && (char_idx <= 54))
                  {
                     bstart = 41;
                     bend   = 54;
                  }
                  else
                  if((char_idx >=55) && (char_idx <= 68))
                  {
                     bstart = 55;
                     bend   = 68;
                  }
                  else
                  {
                     switch( char_idx )
                     {
                        case 12:
                        case 13:
                           bstart = 12;
                           bend   = 13;
                           _is_indicator = true;
                           break;
                        case 15:
                        case 16:
                           bstart = 15;
                           bend   = 16;
                           _is_indicator = true;
                           break;
                        case 76:
                        case 77:
                           bstart = 76;
                           bend   = 77;
                           _is_indicator = true;
                           break;
                        case 78:
                        case 79:
                           bstart = 78;
                           bend   = 79;
                           _is_indicator = true;
                           break;
                        case 80:
                        case 81:
                           bstart = 80;
                           bend   = 81;
                           _is_indicator = true;
                           break;
                        default:
                           return false;
                     } // end swtich()
                  } // end else()
                  break;
               default :
                  return false;
            } // end swtich()
         } // end else()
      } // end if(len > 7)

      _start_pos = bstart;
      _end_pos = bend;

      return true;
   } // end sourceRPGLineInfo()


   /**
    * Returns minimal of two integers
    */
   private int Min(int a, int b)
   {
      return (a > b) ? b : a;
   } // end Min

   /**
    *  This function will determine if this line is a free form RPG line.
    *  cmvc18024A
    */
   private boolean ifFreeForm(String line, int char_idx)
   {
      // this function uses 1-based index
      // and returns 1-based index. - rlin

      // global variables
      int src_prefixlen = SRCCLIENT.src_prefixlen;

      boolean is_listing=false;  //cmvc 8434

      int len = line.length();
      len = len - src_prefixlen - 1;
      if( len <= 0 ) {
         return false;
      }

      //First, determine if it is a listing view.
      if( ((line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) == ' ') &&   // @recordLen
           (line.charAt(Min(7 + src_prefixlen + 1 - 1, len)) != '*')  )   // @recordLen
                                       &&
          ((line.charAt(Min(9 + src_prefixlen + 1 - 1, len)) >= '0') &&   // @recordLen
           (line.charAt(Min(9 + src_prefixlen + 1 - 1, len)) <= '9')  ) ) // @recordLen
        is_listing = true; // found a listing view

      //Now, check if it is freeform depending on which view it is
      if(true != is_listing)
      {
        //we check position 6 and 7
        if((line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) == ' ') &&
           (line.charAt(Min(7 + src_prefixlen + 1 - 1, len)) == ' ') )
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      else
      {
        //we check position 6+5 and 7+5
        if((line.charAt(Min(11 + src_prefixlen + 1 - 1, len)) == ' ') &&
           (line.charAt(Min(12 + src_prefixlen + 1 - 1, len)) == ' ') )
        {
          return true;
        }
        else
        {
          return false;
        }
      }

   } //end ifFreeForm

} // end class IdentifierParserForRPG

