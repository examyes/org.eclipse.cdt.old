package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1999, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/IdentifierParserForOPMRPG.java, java-model, eclipse-dev, 20011128
// Version 1.2.3.2 (last modified 11/28/01 16:14:13)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class IdentifierParserForOPMRPG extends ColumnBasedIdentifierParser
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
   public IdentifierParserForOPMRPG(boolean stringIndexIs0Based)
   {
      _stringIndexIs0Based = stringIndexIs0Based;
   }


   /******************************************************************************/
   /*                                                                            */
   /*  parseOPMRPGwords -  do parsing for OPM RPG debuggers                      */
   /*                                                                            */
   /******************************************************************************/
   private char _specifier;
   private boolean  _is_comment = false;
   private boolean  _is_indicator = false;
   private int  _start_pos = 0;
   private int  _end_pos = 0;

   public int [] identifierInString(String line, int startChar)
   {
      // this function uses 1-based index
      if (_stringIndexIs0Based)
      {
         startChar++;
      } // end if()

      // global variables
      int src_prefixlen = SRCCLIENT.src_prefixlen;


      int char_idx = startChar - src_prefixlen - 1;
      final int linelen = line.length();

      if( !sourceOPMRPGLineInfo(line, char_idx) )
      {
         return null;
      } // end if()

      int wordstart = _start_pos;
      int wordend = _end_pos;

      /* CMVC 8434 SEB
      if ((wordstart+src_prefixlen+1 > linelen) ||
         (wordend+src_prefixlen+1 > linelen)) {
         return false;
      } */

      // Adjust wordend for short lines CMVC 7655
      if(wordend + src_prefixlen + 1 > linelen)
        wordend = linelen - 1 - src_prefixlen;

      boolean colon_found = false;
      int i;

      /*------------------------------------------------------------------*/
      /* First we search for the location of a colon to the right or      */
      /* left of the starting character;                                  */
      /*------------------------------------------------------------------*/
      for( i = char_idx; i <= Min(wordend, linelen - 1); i++ )                  // @recordLen
      {
         if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) == ':' )  // @recordLen
         {
            wordend = i - 1;
            colon_found = true;
            break;
         } // end if(is ':')
      } // end for()

      if( !colon_found )
      {
         for( i = Min(char_idx, linelen - 1); i >= wordstart; i-- )               // @recordLen
         {
            if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) == ':' ) // @recordLen
            {
               wordstart = i + 1;
               colon_found = true;
               break;
            } // end if(is ':')
         } // end for()
      } // end if(no column found)


      int bend_old = wordend;
      int bstart_old = wordstart;
      boolean right_paren_found = false;
      boolean left_paren_found = false;

      /*--------------------------------------------------------------------*/
      /* Search for a '(' && ')' select what's within the parens            */
      /*--------------------------------------------------------------------*/
      for( i = char_idx; i <= Min(bend_old, linelen - 1); i++ )                 // @recordLen
      {
         if (line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) == ')' )  // @recordLen
         {
            wordend = i - 1;
            right_paren_found = true;
            break;
         } // end if(is ')')
      } // end for()

      if( right_paren_found )
      {
         for( i = Min(char_idx, linelen - 1); i >= bstart_old; i-- )               // @recordLen
         {
            if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) == '(' )  // @recordLen
            {
               wordstart = i + 1;
               left_paren_found = true;
               break;
            } // end if(is '(')
         } // end for()
      } // end if(found right parenthesis)

      if( !(right_paren_found && left_paren_found &&
            (char_idx >= wordstart) && (char_idx <= wordend)) )
      {
         wordstart = bstart_old;
         wordend = bend_old;
      } // end if()

      boolean nonblank = false;
      for( i = Min(wordend, linelen - 1); i >= wordstart; i-- )                 // @recordLen
      {
         if( line.charAt(Min(i + src_prefixlen + 1 - 1, linelen - 1)) != ' ' )  // @recordLen
         {
            nonblank = true;
            break;
         } // end if(is ' ')
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

      return ans;
   } // end identifierInString()




   /**
    * Given a highlighted token a line where this toake is found and starting
    * position of the token within this line, update the token if required by
    * the specific language parser (e.g. RPG indicators)
    *
    * NOTE: For OPM RPG we tack '*IN' in front of indicators
    *       For OPM RPG we convert Array,Index to Array(Index)     cmvc14098A
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
        if( sourceOPMRPGLineInfo(line, start_index) ) // parse the line for token at start_index
        {
          if( _is_indicator )                         // if it happens to be an indicator
            updatedToken = new String("*IN" + token); // add '*IN' in front of it
        } // end if()
      } // end if(token length is <= 2)
      else                                            // this could be an arrray,index
      {                                               // convert it to array(index)
        for( int i = 0; i < token.length(); i++ )     // for every character of token
        {
          if(token.charAt(i) == ',')                  // look for ','
          {                                           // then replace array,index with array(index)
            updatedToken = new String(token.substring(0, i - 1) + '(' + token.substring(i + 1, token.length() - 1) + ')');
            break;                                    // and get out of the loop
          } // end if(we found ',')
        } // end for(every character in the token)
      } // end else(token length > 2)

      if( null == updatedToken )                      // if updatedToken is still null
        updatedToken = new String(token);             // return the original token


      // Restor original values of private members that might have changed
      _specifier    = specifier;
      _is_comment   = is_comment;
      _is_indicator = is_indicator;
      _start_pos    = start_pos;
      _end_pos      = end_pos;

      return updatedToken;
   } // end doLanguageSpecifics()



   /******************************************************************************/
   /*                                                                            */
   /*  sourceOPMRPGLineInfo - Take the current line and gather all the RPG info. */
   /*                                                                            */
   /******************************************************************************/
   private boolean sourceOPMRPGLineInfo(String line, int char_idx)
   {
      // this function uses 1-based index
      // and returns 1-based index. - rlin

      // global variables
      int src_prefixlen = SRCCLIENT.src_prefixlen;


      int bstart = 0; // 1-based index
      int bend = 0; // 1-based index
      boolean is_listing = false;                                       // cmvc 7655-1

      int len = line.length();
      len = len - src_prefixlen - 1;
      if( len <= 0 ) {
         return false;
      }

      _start_pos = 0;
      _end_pos = 0;
      _is_comment = false;
      _is_indicator = false;

      // CMVC 8434 Check if we are in a listing view
      if( ((line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) == ' ') &&   // @recordLen
           (line.charAt(Min(7 + src_prefixlen + 1 - 1, len)) != '*')  )   // @recordLen
                                       ||
          ((line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) >= '0') &&   // @recordLen
           (line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) <= '9')  ) ) // @recordLen
        is_listing = true; // found a listing view

      /*------------------------------------------------------------------*/
      /*                                                                  */
      /* Spec    Columns for OPM RPG                                      */
      /*   H       None                                                   */
      /*   F       33-34 71-72                                            */
      /*   E       27-32 46-51                                            */
      /*   L       None                                                   */
      /*   I  Pgm Files:  19-20 53-58 59-60 61-62 63-64 65-66 67-68 69-70 */
      /*   I  Ext Files:  7-14 19-20 21-30 53-58 59-60 61-62 65-66 67-68  */
      /*                  69-70                                           */
      /*   I  Data Struc: 21-30 53-58                                     */
      /*   C       7-8 10-11 13-14 16-17 18-27 33-42 43-48                */
      /*           54-55 56-57 58-59                                      */
      /*   O  Pgm Files:  24-25 27-28 30-31 32-37                         */
      /*      Ext Files:  7-14 24-25 27-28 30-31 32-37                    */
      /*                                                                  */
      /*   For listing views, add 6 to account for view displacement      */
      /*                                                                  */
      /*------------------------------------------------------------------*/

      if( len >= 7 )
      {
         if( !is_listing )             // check for listing view   CMVC 8434
         {
            if( line.charAt(Min(7 + src_prefixlen + 1 - 1, len)) == '*' )  // @recordLen
            {
               _is_comment = true;
               return true;
            } // end if(is '*')

            _specifier = line.charAt(Min(6 + src_prefixlen + 1 - 1, len)); // @recordLen

            switch( line.charAt(Min(6 + src_prefixlen + 1 - 1, len)) )     // @recordLen
            {
               case 'F':
                  switch( char_idx )
                  {
                     case 33:
                     case 34:
                        bstart = 33;
                        bend   = 34;
                        _is_indicator = true;
                        break;
                     case 71:
                     case 72:
                        bstart = 71;
                        bend   = 72;
                        _is_indicator = true;
                        break;
                     default:
                        return false;
                  } // end switch()
                  break;

               case 'E':
                  if((char_idx >= 27) && (char_idx <= 32))
                  {
                     bstart = 27;
                     bend   = 32;
                  } // end if()
                  else if((char_idx >=46) && (char_idx <= 51))
                  {
                     bstart = 46;
                     bend   = 51;
                  } // end if()
                  else
                     return false;
                  break;

               case 'I':
                  if((char_idx >= 7) && (char_idx <= 14))
                  {
                     bstart = 7;
                     bend   = 14;
                  } // end if()
                  else if((char_idx >= 21) && (char_idx <= 30))
                  {
                     bstart = 21;
                     bend   = 30;
                  } // end else if()
                  else if((char_idx >= 53) && (char_idx <= 58))
                  {
                     bstart = 53;
                     bend   = 58;
                  } // end else if()
                  else
                  {
                     switch( char_idx )
                     {
                        case 19:
                        case 59:
                        case 61:
                        case 63:
                        case 65:
                        case 67:
                        case 69:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;

                        case 20:
                        case 60:
                        case 62:
                        case 64:
                        case 66:
                        case 68:
                        case 70:
                           bstart = char_idx - 1;
                           bend   = char_idx;
                           _is_indicator = true;
                           break;

                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;

               case 'C':
                  if((char_idx >= 18) && (char_idx <= 27))
                  {
                     bstart = 18;
                     bend   = 27;
                  } // end if()
                  else if((char_idx >=33) && (char_idx <= 42))
                  {
                     bstart = 33;
                     bend   = 42;
                  } // end else if()
                  else
                  if((char_idx >= 43) && (char_idx <= 48))
                  {
                     bstart = 43;
                     bend   = 48;
                  } // end if()
                  else
                  {
                     switch( char_idx )
                     {
                        case  7:
                        case 10:
                        case 13:
                        case 16:
                        case 54:
                        case 56:
                        case 58:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;

                        case  8:
                        case 11:
                        case 14:
                        case 17:
                        case 55:
                        case 57:
                        case 59:
                           bstart = char_idx -1;
                           bend   = char_idx;
                           _is_indicator = true;
                           break;

                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;

               case 'O':
                  if((char_idx >= 7) && (char_idx <= 14))
                  {
                     bstart = 7;
                     bend   = 14;
                  } // end if()
                  else if((char_idx >= 32) && (char_idx <= 37))
                  {
                     bstart = 32;
                     bend   = 37;
                  } // end else if()
                  else {
                     switch( char_idx )
                     {
                        case 24:
                        case 27:
                        case 30:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;

                        case 25:
                        case 28:
                        case 31:
                           bstart = char_idx -1;
                           bend   = char_idx;
                           _is_indicator = true;
                           break;

                        default:
                           return false;
                     } // end swtich()
                  } // end else()
                  break;

               default :
                  return false;
            } // end switch()
         } // end if(source view)
         else  // listing view CMVC 8434
         {
            if( line.charAt(Min(13 + src_prefixlen + 1 - 1, len)) == '*' )  // @recordLen
            {
               _is_comment = true;
               return true;
            }
            _specifier = line.charAt(Min(6 + src_prefixlen + 1 - 1, len));  // @recordLen

            switch( line.charAt(Min(12 + src_prefixlen + 1 - 1, len)) )     // @recordLen
            {
               case 'F':
                  switch( char_idx )
                  {
                     case 39:
                     case 40:
                        bstart = 39;
                        bend   = 40;
                        _is_indicator = true;
                        break;
                     case 77:
                     case 78:
                        bstart = 77;
                        bend   = 78;
                        _is_indicator = true;
                        break;
                     default:
                        return false;
                  } // end switch()
                  break;

               case 'E':
                  if((char_idx >= 33) && (char_idx <= 38))
                  {
                     bstart = 33;
                     bend   = 38;
                  } // end if()
                  else if((char_idx >= 52) && (char_idx <= 57))
                  {
                     bstart = 52;
                     bend   = 57;
                  } // end else if()
                  else
                     return false;
                  break;

               case 'I':
                  if((char_idx >= 13) && (char_idx <= 20))
                  {
                     bstart = 13;
                     bend   = 20;
                  } // end if()
                  else if((char_idx >= 27) && (char_idx <= 36))
                  {
                     bstart = 27;
                     bend   = 36;
                  } // end else if()
                  else if((char_idx >= 59) && (char_idx <= 64))
                  {
                     bstart = 59;
                     bend   = 64;
                  } // end else if()
                  else
                  {
                     switch( char_idx )
                     {
                        case 25:
                        case 65:
                        case 67:
                        case 69:
                        case 71:
                        case 73:
                        case 75:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;

                        case 26:
                        case 66:
                        case 68:
                        case 70:
                        case 72:
                        case 74:
                        case 76:
                           bstart = char_idx -1;
                           bend   = char_idx;
                           _is_indicator = true;
                           break;

                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;

               case 'C':
                  if((char_idx >= 24) && (char_idx <= 33))
                  {
                     bstart = 24;
                     bend   = 33;
                  } // end if()
                  else if((char_idx >= 39) && (char_idx <= 48))
                  {
                     bstart = 39;
                     bend   = 48;
                  } // end else if()
                  else
                  if((char_idx >= 49) && (char_idx <= 54))
                  {
                     bstart = 49;
                     bend   = 54;
                  } // end if()
                  else
                  {
                     switch( char_idx )
                     {
                        case 13:
                        case 16:
                        case 19:
                        case 22:
                        case 60:
                        case 62:
                        case 64:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;


                        case 14:
                        case 17:
                        case 20:
                        case 23:
                        case 61:
                        case 63:
                        case 65:
                           bstart = char_idx -1;
                           bend   = char_idx;
                           _is_indicator = true;
                           break;

                        default:
                           return false;
                     } // end switch()
                  } // end else()
                  break;

               case 'O':
                  if((char_idx >= 13) && (char_idx <= 20))
                  {
                     bstart = 13;
                     bend   = 20;
                  } // end if()
                  else if((char_idx >= 38) && (char_idx <= 43))
                  {
                     bstart = 38;
                     bend   = 43;
                  } // end else if()
                  else
                  {
                     switch( char_idx )
                     {
                        case 30:
                        case 31:
                           bstart = 24;
                           bend   = 25;
                           _is_indicator = true;
                           break;

                        case 33:
                        case 36:
                           bstart = char_idx;
                           bend   = char_idx + 1;
                           _is_indicator = true;
                           break;

                        case 34:
                        case 37:
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
            } // end switch()
         } // end else()
      } // end if( len > 7 )

      _start_pos = bstart;
      _end_pos = bend;

      return true;
   } // end sourceOPMRPGLineInfo()


   /**
    * Returns minimal of two integers
    */
   private int Min(int a, int b)
   {
      return (a > b) ? b : a;
   } // end Min

} // end class IdentifierParserForOPMRPG

