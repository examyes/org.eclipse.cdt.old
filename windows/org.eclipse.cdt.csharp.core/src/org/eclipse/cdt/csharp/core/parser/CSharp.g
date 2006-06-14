grammar CSharp;
options {
	output = template;
}

@parser::header {
package org.eclipse.cdt.csharp.core.parser;
}

@lexer::header {
package org.eclipse.cdt.csharp.core.parser;
}

WS	:	(	' '
		|	'\t'
		|	'\f'
		|	'\r'
		|	'\n'
		)+
		{ channel=99; }
	;

identifier
	:	AVAILABLE_IDENTIFIER
	|	'@' identifierOrKeyword
	;

// To do Unicode character classes
AVAILABLE_IDENTIFIER
	:	( 'a'..'z' | 'A'..'Z' | '_' )
		( 'a'..'z' | 'A'..'Z' | '_' | '0'..'9' )*
	;

identifierOrKeyword
	:	AVAILABLE_IDENTIFIER
	|	keyword
	;

keyword
	:	'abstract' | 'as' | 'base' | 'bool' | 'break'
	|	'byte' | 'case' | 'catch' | 'char' | 'checked'
	|	'class' | 'const' | 'continue' | 'decimal' | 'default'
	|	'delegate' | 'do' | 'double' | 'else' | 'enum'
	|	'event' | 'explicit' | 'extern' | 'false' | 'finally'
	|	'fixed' | 'float' | 'for' | 'foreach' | 'goto'
	|	'if' | 'implicit' | 'in' | 'int' | 'interface'
	|	'internal' | 'is' | 'lock' | 'long' | 'namespace'
	|	'new' | 'null' | 'object' | 'operator' | 'out'
	|	'override' | 'params' | 'private' | 'protected' | 'public'
	|	'readonly' | 'ref' | 'return' | 'sbyte' | 'sealed'
	|	'short' | 'sizeof' | 'stackalloc' | 'static' | 'string'
	|	'struct' | 'switch' | 'this' | 'throw' | 'true'
	|	'try' | 'typeof' | 'uint' | 'ulong' | 'unchecked'
	|	'unsafe' | 'ushort' | 'using' | 'virtual' | 'void'
	|	'volatile' | 'while'
	;

literal
	:	booleanLiteral
	|	INTEGER_LITERAL
	|	REAL_LITERAL
	|	CHARACTER_LITERAL
	|	STRING_LITERAL
	|	nullLiteral
	;

booleanLiteral
	:	'true'
	|	'false'
	;

INTEGER_LITERAL
	:	DECIMAL_INTEGER_LITERAL
	|	HEXADECIMAL_INTEGER_LITERAL
	;

fragment
DECIMAL_INTEGER_LITERAL
	:	DECIMAL_DIGITS ( INTEGER_TYPE_SUFFIX )?
	;

fragment
DECIMAL_DIGITS
	:	( '0'..'9' )+
	;

fragment
INTEGER_TYPE_SUFFIX
	:	'U' | 'u' | 'L' | 'l' | 'UL' | 'Ul' | 'uL' | 'ul' | 'LU' | 'Lu' | 'lU' | 'lu'
	;

fragment
HEXADECIMAL_INTEGER_LITERAL
	:	'0x' ( '0'..'9' | 'a'..'f' | 'A'..'F' )+ ( INTEGER_TYPE_SUFFIX )?
	;

REAL_LITERAL
	:	( DECIMAL_DIGITS )? '.' DECIMAL_DIGITS ( EXPONENT_PART )? ( REAL_TYPE_SUFFIX )?
	|	DECIMAL_DIGITS EXPONENT_PART ( REAL_TYPE_SUFFIX )?
	|	DECIMAL_DIGITS REAL_TYPE_SUFFIX
	;

fragment
EXPONENT_PART
	:	( 'e' | 'E' ) ( '+' | '-' )? DECIMAL_DIGITS
	;

fragment
REAL_TYPE_SUFFIX
	:	'F' | 'f' | 'D' | 'd' | 'M' | 'm'
	;

// TODO - escape the quote
CHARACTER_LITERAL
	:	'\'' ( ~( '\'' ) )* '\'' 
	;

STRING_LITERAL
	:	REGULAR_STRING_LITERAL
	|	VERBATIM_STRING_LITERAL
	;

// TODO - escape the double quote
fragment
REGULAR_STRING_LITERAL
	:	'"' ( ~( '"' ) )* '"'
	;

// TODO - a real verbatim literal
fragment
VERBATIM_STRING_LITERAL
	:	'@"' ( ~( '"' ) )* '"'
	;

nullLiteral
	:	'null'
	;

operatorOrPunctuator
	:	'{' | '}' | '[' | ']' | '(' | ')' | '.' | ',' | ':' | ';'
	|	'+' | '-' | '*' | '/' | '%' | '&' | '|' | '^' | '!' | '~'
	|	'=' | '>' | '<' | '?' | '??' | '::' | '++' | '--' | '&&' | '||'
	|	'->' | '==' | '!=' | '<=' | '>=' | '+=' | '-=' | '*=' | '/=' | '%='
	|	'&=' | '|=' | '^=' | '<<' | '<<='
	;

rightShift
	:	'>' '>'
	;

rightShiftAssignment
	:	'>' '>='
	;

