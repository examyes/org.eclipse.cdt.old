/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.parser.c99;

public interface C99LexerBaseexp {
    public final static int
      TK_EOF_TOKEN = 1,
      TK_identifier = 2,
      TK_integer = 3,
      TK_floating = 4,
      TK_charconst = 5,
      TK_stringlit = 6,
      TK_RightBracket = 7,
      TK_LeftBracket = 8,
      TK_RightParen = 9,
      TK_LeftParen = 10,
      TK_RightBrace = 11,
      TK_LeftBrace = 12,
      TK_Dot = 13,
      TK_Arrow = 14,
      TK_PlusPlus = 15,
      TK_MinusMinus = 16,
      TK_And = 17,
      TK_Star = 18,
      TK_Plus = 19,
      TK_Minus = 20,
      TK_Tilde = 21,
      TK_Bang = 22,
      TK_Slash = 23,
      TK_Percent = 24,
      TK_RightShift = 25,
      TK_LeftShift = 26,
      TK_LT = 27,
      TK_GT = 28,
      TK_LE = 29,
      TK_GE = 30,
      TK_EQ = 31,
      TK_NE = 32,
      TK_Caret = 33,
      TK_Or = 34,
      TK_AndAnd = 35,
      TK_OrOr = 36,
      TK_Question = 37,
      TK_Colon = 38,
      TK_SemiColon = 39,
      TK_DotDotDot = 40,
      TK_Assign = 41,
      TK_StarAssign = 42,
      TK_SlashAssign = 43,
      TK_PercentAssign = 44,
      TK_PlusAssign = 45,
      TK_MinusAssign = 46,
      TK_RightShiftAssign = 47,
      TK_LeftShiftAssign = 48,
      TK_AndAssign = 49,
      TK_CaretAssign = 50,
      TK_OrAssign = 51,
      TK_Comma = 52,
      TK_Hash = 53,
      TK_HashHash = 54,
      TK_NewLine = 55,
      TK_Invalid = 56,
      TK_PlaceMarker = 57,
      TK_Parameter = 58,
      TK_DisabledMacroName = 59,
      TK_Completion = 60,
      TK_EndOfCompletion = 61,
      TK_SingleLineComment = 62,
      TK_MultiLineComment = 63;

      public final static String orderedTerminalSymbols[] = {
                 "",//$NON-NLS-1$
                 "EOF_TOKEN",//$NON-NLS-1$
                 "identifier",//$NON-NLS-1$
                 "integer",//$NON-NLS-1$
                 "floating",//$NON-NLS-1$
                 "charconst",//$NON-NLS-1$
                 "stringlit",//$NON-NLS-1$
                 "RightBracket",//$NON-NLS-1$
                 "LeftBracket",//$NON-NLS-1$
                 "RightParen",//$NON-NLS-1$
                 "LeftParen",//$NON-NLS-1$
                 "RightBrace",//$NON-NLS-1$
                 "LeftBrace",//$NON-NLS-1$
                 "Dot",//$NON-NLS-1$
                 "Arrow",//$NON-NLS-1$
                 "PlusPlus",//$NON-NLS-1$
                 "MinusMinus",//$NON-NLS-1$
                 "And",//$NON-NLS-1$
                 "Star",//$NON-NLS-1$
                 "Plus",//$NON-NLS-1$
                 "Minus",//$NON-NLS-1$
                 "Tilde",//$NON-NLS-1$
                 "Bang",//$NON-NLS-1$
                 "Slash",//$NON-NLS-1$
                 "Percent",//$NON-NLS-1$
                 "RightShift",//$NON-NLS-1$
                 "LeftShift",//$NON-NLS-1$
                 "LT",//$NON-NLS-1$
                 "GT",//$NON-NLS-1$
                 "LE",//$NON-NLS-1$
                 "GE",//$NON-NLS-1$
                 "EQ",//$NON-NLS-1$
                 "NE",//$NON-NLS-1$
                 "Caret",//$NON-NLS-1$
                 "Or",//$NON-NLS-1$
                 "AndAnd",//$NON-NLS-1$
                 "OrOr",//$NON-NLS-1$
                 "Question",//$NON-NLS-1$
                 "Colon",//$NON-NLS-1$
                 "SemiColon",//$NON-NLS-1$
                 "DotDotDot",//$NON-NLS-1$
                 "Assign",//$NON-NLS-1$
                 "StarAssign",//$NON-NLS-1$
                 "SlashAssign",//$NON-NLS-1$
                 "PercentAssign",//$NON-NLS-1$
                 "PlusAssign",//$NON-NLS-1$
                 "MinusAssign",//$NON-NLS-1$
                 "RightShiftAssign",//$NON-NLS-1$
                 "LeftShiftAssign",//$NON-NLS-1$
                 "AndAssign",//$NON-NLS-1$
                 "CaretAssign",//$NON-NLS-1$
                 "OrAssign",//$NON-NLS-1$
                 "Comma",//$NON-NLS-1$
                 "Hash",//$NON-NLS-1$
                 "HashHash",//$NON-NLS-1$
                 "NewLine",//$NON-NLS-1$
                 "Invalid",//$NON-NLS-1$
                 "PlaceMarker",//$NON-NLS-1$
                 "Parameter",//$NON-NLS-1$
                 "DisabledMacroName",//$NON-NLS-1$
                 "Completion",//$NON-NLS-1$
                 "EndOfCompletion",//$NON-NLS-1$
                 "SingleLineComment",//$NON-NLS-1$
                 "MultiLineComment"//$NON-NLS-1$
             };


    public final static boolean isValidForParser = false;
}
