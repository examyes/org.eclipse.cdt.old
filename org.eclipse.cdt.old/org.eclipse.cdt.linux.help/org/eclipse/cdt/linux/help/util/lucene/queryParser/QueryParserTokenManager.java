/* Generated By:JavaCC: Do not edit this line. QueryParserTokenManager.java */
package com.ibm.linux.help.util.lucene.queryParser;
import java.util.Vector;
import java.io.*;
import com.ibm.linux.help.util.lucene.index.Term;
import com.ibm.linux.help.util.lucene.analysis.*;
import com.ibm.linux.help.util.lucene.search.*;

public class QueryParserTokenManager implements QueryParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 40:
         return jjStopAtPos(0, 15);
      case 41:
         return jjStopAtPos(0, 16);
      case 42:
         return jjStopAtPos(0, 19);
      case 43:
         return jjStopAtPos(0, 13);
      case 45:
         return jjStopAtPos(0, 14);
      case 58:
         return jjStopAtPos(0, 17);
      case 94:
         return jjStopAtPos(0, 18);
      case 126:
         return jjStopAtPos(0, 23);
      default :
         return jjMoveNfa_0(2, 0);
   }
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 30;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddStates(0, 2);
                  }
                  else if ((0x100000200L & l) != 0L)
                  {
                     if (kind > 27)
                        kind = 27;
                  }
                  else if (curChar == 34)
                     jjCheckNAdd(14);
                  else if (curChar == 33)
                  {
                     if (kind > 12)
                        kind = 12;
                  }
                  else if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 3;
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(16, 17);
                  break;
               case 3:
                  if (curChar == 38 && kind > 10)
                     kind = 10;
                  break;
               case 4:
                  if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 12:
                  if (curChar == 33 && kind > 12)
                     kind = 12;
                  break;
               case 13:
                  if (curChar == 34)
                     jjCheckNAdd(14);
                  break;
               case 14:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(14, 15);
                  break;
               case 15:
                  if (curChar == 34 && kind > 20)
                     kind = 20;
                  break;
               case 16:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(16, 17);
                  break;
               case 17:
                  if (curChar == 46)
                     jjCheckNAdd(18);
                  break;
               case 18:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(18);
                  break;
               case 20:
                  jjAddStates(3, 4);
                  break;
               case 23:
                  jjAddStates(5, 6);
                  break;
               case 25:
                  if ((0x100000200L & l) != 0L && kind > 27)
                     kind = 27;
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddStates(0, 2);
                  break;
               case 27:
                  if ((0x7bfff8bafffffdffL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(27);
                  break;
               case 28:
                  if ((0xfbfffcbafffffdffL & l) != 0L)
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 29:
                  if ((0x3ff000000000000L & l) != 0L && kind > 24)
                     kind = 24;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddStates(0, 2);
                  }
                  else if (curChar == 123)
                     jjCheckNAdd(23);
                  else if (curChar == 91)
                     jjCheckNAdd(20);
                  else if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 7;
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 10;
                  else if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 5;
                  else if (curChar == 65)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 0:
                  if (curChar == 68 && kind > 10)
                     kind = 10;
                  break;
               case 1:
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 0;
                  break;
               case 5:
                  if (curChar == 82 && kind > 11)
                     kind = 11;
                  break;
               case 6:
                  if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 7:
                  if (curChar == 124 && kind > 11)
                     kind = 11;
                  break;
               case 8:
                  if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 9:
                  if (curChar == 84 && kind > 12)
                     kind = 12;
                  break;
               case 10:
                  if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 14:
                  jjAddStates(7, 8);
                  break;
               case 19:
                  if (curChar == 91)
                     jjCheckNAdd(20);
                  break;
               case 20:
                  if ((0xffffffffdfffffffL & l) != 0L)
                     jjCheckNAddTwoStates(20, 21);
                  break;
               case 21:
                  if (curChar == 93 && kind > 25)
                     kind = 25;
                  break;
               case 22:
                  if (curChar == 123)
                     jjCheckNAdd(23);
                  break;
               case 23:
                  if ((0xdfffffffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 24:
                  if (curChar == 125 && kind > 26)
                     kind = 26;
                  break;
               case 26:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddStates(0, 2);
                  break;
               case 27:
                  if ((0x87ffffff97ffffffL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(27);
                  break;
               case 28:
                  if ((0x87ffffff97ffffffL & l) != 0L)
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 29:
                  if ((0x7fffffe87fffffeL & l) != 0L && kind > 24)
                     kind = 24;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 14:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(7, 8);
                  break;
               case 20:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(3, 4);
                  break;
               case 23:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(5, 6);
                  break;
               case 27:
                  if ((jjbitVec0[i2] & l2) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 28:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(9, 10);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 30 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   27, 28, 29, 20, 21, 23, 24, 14, 15, 28, 29, 
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
"\53", "\55", "\50", "\51", "\72", "\136", "\52", null, null, null, "\176", null, 
null, null, null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x7fffc01L, 
};
static final long[] jjtoSkip = {
   0x8000000L, 
};
private SimpleCharStream input_stream;
private final int[] jjrounds = new int[30];
private final int[] jjstateSet = new int[60];
protected char curChar;
public QueryParserTokenManager(SimpleCharStream stream)
{
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public QueryParserTokenManager(SimpleCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 30; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

private final Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public final Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
