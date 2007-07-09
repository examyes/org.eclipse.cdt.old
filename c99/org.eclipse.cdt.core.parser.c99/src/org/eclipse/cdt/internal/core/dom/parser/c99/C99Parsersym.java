/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.parser.c99;

public interface C99Parsersym {
    public final static int
      TK_auto = 10,
      TK_break = 53,
      TK_case = 54,
      TK_char = 31,
      TK_const = 5,
      TK_continue = 55,
      TK_default = 56,
      TK_do = 57,
      TK_double = 32,
      TK_else = 79,
      TK_enum = 44,
      TK_extern = 11,
      TK_float = 33,
      TK_for = 58,
      TK_goto = 59,
      TK_if = 60,
      TK_inline = 12,
      TK_int = 34,
      TK_long = 35,
      TK_register = 13,
      TK_restrict = 6,
      TK_return = 61,
      TK_short = 36,
      TK_signed = 37,
      TK_sizeof = 20,
      TK_static = 9,
      TK_struct = 45,
      TK_switch = 62,
      TK_typedef = 14,
      TK_union = 46,
      TK_unsigned = 38,
      TK_void = 39,
      TK_volatile = 7,
      TK_while = 50,
      TK__Bool = 40,
      TK__Complex = 41,
      TK__Imaginary = 42,
      TK_LeftBracket = 28,
      TK_LeftParen = 1,
      TK_LeftBrace = 21,
      TK_Dot = 66,
      TK_Arrow = 80,
      TK_PlusPlus = 18,
      TK_MinusMinus = 19,
      TK_And = 17,
      TK_Star = 3,
      TK_Plus = 15,
      TK_Minus = 16,
      TK_Tilde = 22,
      TK_Bang = 23,
      TK_Slash = 67,
      TK_Percent = 68,
      TK_RightShift = 51,
      TK_LeftShift = 52,
      TK_LT = 69,
      TK_GT = 70,
      TK_LE = 71,
      TK_GE = 72,
      TK_EQ = 74,
      TK_NE = 75,
      TK_Caret = 76,
      TK_Or = 77,
      TK_AndAnd = 78,
      TK_OrOr = 81,
      TK_Question = 82,
      TK_Colon = 47,
      TK_DotDotDot = 63,
      TK_Assign = 64,
      TK_StarAssign = 83,
      TK_SlashAssign = 84,
      TK_PercentAssign = 85,
      TK_PlusAssign = 86,
      TK_MinusAssign = 87,
      TK_RightShiftAssign = 88,
      TK_LeftShiftAssign = 89,
      TK_AndAssign = 90,
      TK_CaretAssign = 91,
      TK_OrAssign = 92,
      TK_Comma = 29,
      TK_Hash = 93,
      TK_HashHash = 94,
      TK_NewLine = 95,
      TK_EOF_TOKEN = 73,
      TK_identifier = 2,
      TK_integer = 24,
      TK_floating = 25,
      TK_charconst = 26,
      TK_stringlit = 27,
      TK_RightBracket = 65,
      TK_RightParen = 48,
      TK_RightBrace = 43,
      TK_SemiColon = 30,
      TK_Invalid = 96,
      TK_Completion = 8,
      TK_EndOfCompletion = 4,
      TK_SingleLineComment = 97,
      TK_MultiLineComment = 98,
      TK_ERROR_TOKEN = 49;

      public final static String orderedTerminalSymbols[] = {
                 "",//$NON-NLS-1$
                 "LeftParen",//$NON-NLS-1$
                 "identifier",//$NON-NLS-1$
                 "Star",//$NON-NLS-1$
                 "EndOfCompletion",//$NON-NLS-1$
                 "const",//$NON-NLS-1$
                 "restrict",//$NON-NLS-1$
                 "volatile",//$NON-NLS-1$
                 "Completion",//$NON-NLS-1$
                 "static",//$NON-NLS-1$
                 "auto",//$NON-NLS-1$
                 "extern",//$NON-NLS-1$
                 "inline",//$NON-NLS-1$
                 "register",//$NON-NLS-1$
                 "typedef",//$NON-NLS-1$
                 "Plus",//$NON-NLS-1$
                 "Minus",//$NON-NLS-1$
                 "And",//$NON-NLS-1$
                 "PlusPlus",//$NON-NLS-1$
                 "MinusMinus",//$NON-NLS-1$
                 "sizeof",//$NON-NLS-1$
                 "LeftBrace",//$NON-NLS-1$
                 "Tilde",//$NON-NLS-1$
                 "Bang",//$NON-NLS-1$
                 "integer",//$NON-NLS-1$
                 "floating",//$NON-NLS-1$
                 "charconst",//$NON-NLS-1$
                 "stringlit",//$NON-NLS-1$
                 "LeftBracket",//$NON-NLS-1$
                 "Comma",//$NON-NLS-1$
                 "SemiColon",//$NON-NLS-1$
                 "char",//$NON-NLS-1$
                 "double",//$NON-NLS-1$
                 "float",//$NON-NLS-1$
                 "int",//$NON-NLS-1$
                 "long",//$NON-NLS-1$
                 "short",//$NON-NLS-1$
                 "signed",//$NON-NLS-1$
                 "unsigned",//$NON-NLS-1$
                 "void",//$NON-NLS-1$
                 "_Bool",//$NON-NLS-1$
                 "_Complex",//$NON-NLS-1$
                 "_Imaginary",//$NON-NLS-1$
                 "RightBrace",//$NON-NLS-1$
                 "enum",//$NON-NLS-1$
                 "struct",//$NON-NLS-1$
                 "union",//$NON-NLS-1$
                 "Colon",//$NON-NLS-1$
                 "RightParen",//$NON-NLS-1$
                 "ERROR_TOKEN",//$NON-NLS-1$
                 "while",//$NON-NLS-1$
                 "RightShift",//$NON-NLS-1$
                 "LeftShift",//$NON-NLS-1$
                 "break",//$NON-NLS-1$
                 "case",//$NON-NLS-1$
                 "continue",//$NON-NLS-1$
                 "default",//$NON-NLS-1$
                 "do",//$NON-NLS-1$
                 "for",//$NON-NLS-1$
                 "goto",//$NON-NLS-1$
                 "if",//$NON-NLS-1$
                 "return",//$NON-NLS-1$
                 "switch",//$NON-NLS-1$
                 "DotDotDot",//$NON-NLS-1$
                 "Assign",//$NON-NLS-1$
                 "RightBracket",//$NON-NLS-1$
                 "Dot",//$NON-NLS-1$
                 "Slash",//$NON-NLS-1$
                 "Percent",//$NON-NLS-1$
                 "LT",//$NON-NLS-1$
                 "GT",//$NON-NLS-1$
                 "LE",//$NON-NLS-1$
                 "GE",//$NON-NLS-1$
                 "EOF_TOKEN",//$NON-NLS-1$
                 "EQ",//$NON-NLS-1$
                 "NE",//$NON-NLS-1$
                 "Caret",//$NON-NLS-1$
                 "Or",//$NON-NLS-1$
                 "AndAnd",//$NON-NLS-1$
                 "else",//$NON-NLS-1$
                 "Arrow",//$NON-NLS-1$
                 "OrOr",//$NON-NLS-1$
                 "Question",//$NON-NLS-1$
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
                 "Hash",//$NON-NLS-1$
                 "HashHash",//$NON-NLS-1$
                 "NewLine",//$NON-NLS-1$
                 "Invalid",//$NON-NLS-1$
                 "SingleLineComment",//$NON-NLS-1$
                 "MultiLineComment"//$NON-NLS-1$
             };

    public final static boolean isValidForParser = true;
}
