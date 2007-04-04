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
      TK_LeftBracket = 28,
      TK_LeftParen = 2,
      TK_LeftBrace = 11,
      TK_Dot = 66,
      TK_Arrow = 79,
      TK_PlusPlus = 14,
      TK_MinusMinus = 15,
      TK_And = 12,
      TK_Star = 4,
      TK_Plus = 9,
      TK_Minus = 10,
      TK_Tilde = 16,
      TK_Bang = 17,
      TK_Slash = 67,
      TK_Percent = 68,
      TK_RightShift = 61,
      TK_LeftShift = 62,
      TK_LT = 69,
      TK_GT = 70,
      TK_LE = 71,
      TK_GE = 72,
      TK_EQ = 74,
      TK_NE = 75,
      TK_Caret = 76,
      TK_Or = 77,
      TK_AndAnd = 78,
      TK_OrOr = 80,
      TK_Question = 81,
      TK_Colon = 48,
      TK_DotDotDot = 63,
      TK_Assign = 64,
      TK_StarAssign = 82,
      TK_SlashAssign = 83,
      TK_PercentAssign = 84,
      TK_PlusAssign = 85,
      TK_MinusAssign = 86,
      TK_RightShiftAssign = 87,
      TK_LeftShiftAssign = 88,
      TK_AndAssign = 89,
      TK_CaretAssign = 90,
      TK_OrAssign = 91,
      TK_Comma = 30,
      TK_Hash = 93,
      TK_HashHash = 94,
      TK_NewLine = 95,
      TK_EOF_TOKEN = 73,
      TK_auto = 18,
      TK_break = 49,
      TK_case = 50,
      TK_char = 31,
      TK_const = 5,
      TK_continue = 51,
      TK_default = 52,
      TK_do = 53,
      TK_double = 32,
      TK_else = 92,
      TK_enum = 44,
      TK_extern = 19,
      TK_float = 33,
      TK_for = 54,
      TK_goto = 55,
      TK_if = 56,
      TK_inline = 20,
      TK_int = 34,
      TK_long = 35,
      TK_register = 21,
      TK_restrict = 6,
      TK_return = 57,
      TK_short = 36,
      TK_signed = 37,
      TK_sizeof = 22,
      TK_static = 13,
      TK_struct = 45,
      TK_switch = 58,
      TK_typedef = 23,
      TK_union = 46,
      TK_unsigned = 38,
      TK_void = 39,
      TK_volatile = 7,
      TK_while = 47,
      TK__Bool = 40,
      TK__Complex = 41,
      TK__Imaginary = 42,
      TK_identifier = 1,
      TK_integer = 24,
      TK_floating = 25,
      TK_charconst = 26,
      TK_stringlit = 27,
      TK_RightBracket = 65,
      TK_RightParen = 59,
      TK_RightBrace = 43,
      TK_SemiColon = 29,
      TK_Invalid = 96,
      TK_PlaceMarker = 97,
      TK_Parameter = 98,
      TK_DisabledMacroName = 99,
      TK_Completion = 8,
      TK_EndOfCompletion = 3,
      TK_ERROR_TOKEN = 60;

      public final static String orderedTerminalSymbols[] = {
                 "",
                 "identifier",
                 "LeftParen",
                 "EndOfCompletion",
                 "Star",
                 "const",
                 "restrict",
                 "volatile",
                 "Completion",
                 "Plus",
                 "Minus",
                 "LeftBrace",
                 "And",
                 "static",
                 "PlusPlus",
                 "MinusMinus",
                 "Tilde",
                 "Bang",
                 "auto",
                 "extern",
                 "inline",
                 "register",
                 "sizeof",
                 "typedef",
                 "integer",
                 "floating",
                 "charconst",
                 "stringlit",
                 "LeftBracket",
                 "SemiColon",
                 "Comma",
                 "char",
                 "double",
                 "float",
                 "int",
                 "long",
                 "short",
                 "signed",
                 "unsigned",
                 "void",
                 "_Bool",
                 "_Complex",
                 "_Imaginary",
                 "RightBrace",
                 "enum",
                 "struct",
                 "union",
                 "while",
                 "Colon",
                 "break",
                 "case",
                 "continue",
                 "default",
                 "do",
                 "for",
                 "goto",
                 "if",
                 "return",
                 "switch",
                 "RightParen",
                 "ERROR_TOKEN",
                 "RightShift",
                 "LeftShift",
                 "DotDotDot",
                 "Assign",
                 "RightBracket",
                 "Dot",
                 "Slash",
                 "Percent",
                 "LT",
                 "GT",
                 "LE",
                 "GE",
                 "EOF_TOKEN",
                 "EQ",
                 "NE",
                 "Caret",
                 "Or",
                 "AndAnd",
                 "Arrow",
                 "OrOr",
                 "Question",
                 "StarAssign",
                 "SlashAssign",
                 "PercentAssign",
                 "PlusAssign",
                 "MinusAssign",
                 "RightShiftAssign",
                 "LeftShiftAssign",
                 "AndAssign",
                 "CaretAssign",
                 "OrAssign",
                 "else",
                 "Hash",
                 "HashHash",
                 "NewLine",
                 "Invalid",
                 "PlaceMarker",
                 "Parameter",
                 "DisabledMacroName"
             };

    public final static boolean isValidForParser = true;
}
