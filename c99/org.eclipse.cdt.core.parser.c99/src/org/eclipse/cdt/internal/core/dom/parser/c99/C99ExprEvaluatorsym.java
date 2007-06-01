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

public interface C99ExprEvaluatorsym {
    public final static int
      TK_RightBracket = 29,
      TK_LeftBracket = 30,
      TK_RightParen = 18,
      TK_LeftParen = 19,
      TK_RightBrace = 31,
      TK_LeftBrace = 32,
      TK_Dot = 33,
      TK_Arrow = 34,
      TK_PlusPlus = 35,
      TK_MinusMinus = 36,
      TK_And = 12,
      TK_Star = 5,
      TK_Plus = 3,
      TK_Minus = 4,
      TK_Tilde = 20,
      TK_Bang = 21,
      TK_Slash = 6,
      TK_Percent = 7,
      TK_RightShift = 1,
      TK_LeftShift = 2,
      TK_LT = 8,
      TK_GT = 9,
      TK_LE = 10,
      TK_GE = 11,
      TK_EQ = 13,
      TK_NE = 14,
      TK_Caret = 15,
      TK_Or = 16,
      TK_AndAnd = 17,
      TK_OrOr = 22,
      TK_Question = 23,
      TK_Colon = 24,
      TK_SemiColon = 37,
      TK_DotDotDot = 38,
      TK_Assign = 39,
      TK_StarAssign = 40,
      TK_SlashAssign = 41,
      TK_PercentAssign = 42,
      TK_PlusAssign = 43,
      TK_MinusAssign = 44,
      TK_RightShiftAssign = 45,
      TK_LeftShiftAssign = 46,
      TK_AndAssign = 47,
      TK_CaretAssign = 48,
      TK_OrAssign = 49,
      TK_Comma = 50,
      TK_Hash = 51,
      TK_HashHash = 52,
      TK_NewLine = 53,
      TK_EOF_TOKEN = 25,
      TK_identifier = 26,
      TK_integer = 27,
      TK_floating = 54,
      TK_charconst = 28,
      TK_stringlit = 55,
      TK_Invalid = 56,
      TK_PlaceMarker = 57,
      TK_Parameter = 58,
      TK_DisabledMacroName = 59,
      TK_Completion = 60,
      TK_EndOfCompletion = 61,
      TK_SingleLineComment = 62,
      TK_MultiLineComment = 63,
      TK_ERROR_TOKEN = 64;

      public final static String orderedTerminalSymbols[] = {
                 "",//$NON-NLS-1$
                 "RightShift",//$NON-NLS-1$
                 "LeftShift",//$NON-NLS-1$
                 "Plus",//$NON-NLS-1$
                 "Minus",//$NON-NLS-1$
                 "Star",//$NON-NLS-1$
                 "Slash",//$NON-NLS-1$
                 "Percent",//$NON-NLS-1$
                 "LT",//$NON-NLS-1$
                 "GT",//$NON-NLS-1$
                 "LE",//$NON-NLS-1$
                 "GE",//$NON-NLS-1$
                 "And",//$NON-NLS-1$
                 "EQ",//$NON-NLS-1$
                 "NE",//$NON-NLS-1$
                 "Caret",//$NON-NLS-1$
                 "Or",//$NON-NLS-1$
                 "AndAnd",//$NON-NLS-1$
                 "RightParen",//$NON-NLS-1$
                 "LeftParen",//$NON-NLS-1$
                 "Tilde",//$NON-NLS-1$
                 "Bang",//$NON-NLS-1$
                 "OrOr",//$NON-NLS-1$
                 "Question",//$NON-NLS-1$
                 "Colon",//$NON-NLS-1$
                 "EOF_TOKEN",//$NON-NLS-1$
                 "identifier",//$NON-NLS-1$
                 "integer",//$NON-NLS-1$
                 "charconst",//$NON-NLS-1$
                 "RightBracket",//$NON-NLS-1$
                 "LeftBracket",//$NON-NLS-1$
                 "RightBrace",//$NON-NLS-1$
                 "LeftBrace",//$NON-NLS-1$
                 "Dot",//$NON-NLS-1$
                 "Arrow",//$NON-NLS-1$
                 "PlusPlus",//$NON-NLS-1$
                 "MinusMinus",//$NON-NLS-1$
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
                 "floating",//$NON-NLS-1$
                 "stringlit",//$NON-NLS-1$
                 "Invalid",//$NON-NLS-1$
                 "PlaceMarker",//$NON-NLS-1$
                 "Parameter",//$NON-NLS-1$
                 "DisabledMacroName",//$NON-NLS-1$
                 "Completion",//$NON-NLS-1$
                 "EndOfCompletion",//$NON-NLS-1$
                 "SingleLineComment",//$NON-NLS-1$
                 "MultiLineComment",//$NON-NLS-1$
                 "ERROR_TOKEN"//$NON-NLS-1$
             };

    public final static boolean isValidForParser = true;
}
