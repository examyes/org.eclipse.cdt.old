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

compilationUnit
	:	( externAliasDirective )? ( usingDirectives )? ( globalAttributes )?
		( namespaceMemberDeclarations )?
	;

namespaceName
	:	namespaceOrTypeName
	;

typeName
	:	namespaceOrTypeName
	;

namespaceOrTypeName
	:	(	identifier ( typeArgumentList )?
		|	qualifiedAliasMember
		)
		( '.' identifier ( typeArgumentList )? )*
	;

type
	:	valueType
	|	referenceType
	|	typeParameter
	;

valueType
	:	structType
	|	enumType
	;

structType
	:	typeName
	|	simpleType
	|	nullableType
	;

simpleType
	:	numericType
	|	'bool'
	;

numericType
	:	integralType
	|	floatingPointType
	|	'decimal'
	;

integralType
	:	'sbyte'
	|	'byte'
	|	'short'
	|	'ushort'
	|	'int'
	|	'uint'
	|	'long'
	|	'ulong'
	|	'char'
	;

floatingPointType
	:	'float'
	|	'double'
	;

enumType
	:	typeName
	;

nullableType
	:	nonNullableValueType '?'
	;

nonNullableValueType
	:	enumType
	|	typeName
	|	simpleType
	;

referenceType
	:	classType
	|	interfaceType
	|	arrayType
	|	delegateType
	;

classType
	:	typeName
	|	'object'
	|	'string'
	;

interfaceType
	:	typeName
	;

arrayType
	:	nonArrayType rankSpecifiers
	;

nonArrayType
	:	valueType
	|	classType
	|	interfaceType
	|	delegateType
	|	typeParameter
	;

rankSpecifiers
	:	( rankSpecifier )*
	;

rankSpecifier
	:	'[' ( dimSeparators )? ']'
	;

dimSeparators
	:	( ',' )*
	;

delegateType
	:	typeName
	;

variableReference
	:	expression
	;

argumentList
	:	argument ( ',' argument )*
	;

argument
	:	expression
	|	'ref' variableReference
	|	'out' variableReference
	;

primaryExpression
	:	arrayCreationExpression
	|	primaryNoArrayCreationExpression
	;

primaryNoArrayCreationExpression
	:	literal
	|	simpleName
	|	parenthesizedExpression
	|	memberAccess
	|	invocationExpression
	|	elementAccess
	|	thisAccess
	|	baseAccess
	|	postIncrementExpression
	|	postDecrementExpression
	|	objectCreationExpression
	|	delegateCreationExpression
	|	typeofExpression
	|	checkedExpression
	|	uncheckedExpression
	|	defaultValueExpression
	|	anonymousMethodExpression
	;

simpleName
	:	identifier ( typeArgumentList )?
	;

parenthesizedExpression
	:	'(' expression ')'
	;

memberAccess
	:	primaryExpression '.' identifier ( typeArgumentList )?
	| 	predefinedType '.' identifier ( typeArgumentList )?
	|	qualifiedAliasMember '.' identifier ( typeArgumentList )?
	;

predefinedType
	:	'bool' | 'byte' | 'char' | 'decimal' | 'double' | 'float' | 'int' | 'long'
	|	'object' | 'sbyte' | 'short' | 'string' | 'uint' | 'ulong' | 'ushort'
	;

invocationExpression
	:	primaryExpression '(' ( argumentList )? ')'
	;

elementAccess
	:	primaryNoArrayCreationExpression '[' expressionList ']'
	;

expressionList
	:	expression ( ',' expression )*
	;

thisAccess
	:	'this'
	;

baseAccess
	:	'base' '.' identifier ( typeArgumentList )?
	|	'base' '[' expressionList ']'
	;

postIncrementExpression
	:	primaryExpression '++'
	;

postDecrementExpression
	:	primaryExpression '--'
	;

objectCreationExpression
	:	'new' type '(' ( argumentList )? ')'
	;

arrayCreationExpression
	:	'new' nonArrayType '[' expressionList ']' ( rankSpecifiers )? ( arrayInitializer )?
	;

delegateCreationExpression
	:	'new' delegateType '(' expression ')'
	;

typeofExpression
	:	'typeof' '(' type ')'
	|	'typeof' '(' unboundTypeName ')'
	|	'typeof' '(' 'void' ')'
	;

unboundTypeName
	:	(	identifier ( genericDimensionSpecifier )?
		|	identifier '::' identifier ( genericDimensionSpecifier )?
		)
		( '.' identifier ( genericDimensionSpecifier )? )*
	;

genericDimensionSpecifier
	:	'<' ( commas )? '>'
	;

commas
	:	( ',' )+
	;

checkedExpression
	:	'checked' '(' expression ')'
	;

uncheckedExpression
	:	'unchecked' '(' expression ')'
	;

defaultValueExpression
	:	'default' '(' type ')'
	;

anonymousMethodExpression
	:	'delegate' ( anonymousMethodSignature )? block
	;

anonymousMethodSignature
	:	'(' ( anonymousMethodParameterList )? ')'
	;

anonymousMethodParameterList
	:	anonymousMethodParameter ( ',' anonymousMethodParameter )*
	;

anonymousMethodParameter
	:	( parameterModifier )? type identifier
	;

unaryExpression
	:	primaryExpression
	|	'+' unaryExpression
	|	'-' unaryExpression
	|	'!' unaryExpression
	|	'~' unaryExpression
	|	preIncrementExpression
	|	preDecrementExpression
	|	castExpression
	;

preIncrementExpression
	:	'++' unaryExpression
	;

preDecrementExpression
	:	'--' unaryExpression
	;

castExpression
	:	'(' type ')' unaryExpression
	;

multiplicativeExpression
	:	unaryExpression	( ( '*' | '/' | '%' ) unaryExpression )*
	;

additiveExpression
	:	multiplicativeExpression ( ( '+' | '-') multiplicativeExpression )*
	;

shiftExpression
	:	additiveExpression ( ( '<<' | rightShift ) additiveExpression )*
	;

relationalExpression
	:	shiftExpression
		( ( '<' | '>' | '<=' | '>=' ) shiftExpression )*
		( ( 'is' | 'as' ) type )?
	;

equalityExpression
	:	relationalExpression ( ( '==' | '!=' ) relationalExpression )*
	;

andExpression
	:	equalityExpression ( '&' equalityExpression )*		
	;

exclusiveOrExpression
	:	andExpression ( '^' andExpression )*
	;

inclusiveOrExpression
	:	exclusiveOrExpression ( '|' exclusiveOrExpression )*
	;

conditionalAndExpression
	:	inclusiveOrExpression ( '&&' inclusiveOrExpression )*
	;

conditionalOrExpression
	:	conditionalAndExpression ( '||' conditionalAndExpression )*
	;

nullCoalescingExpression
	:	conditionalOrExpression ( '??' conditionalOrExpression )*
	;

conditionalExpression
	:	nullCoalescingExpression '?' expression ':' expression
	;

assignment
	:	unaryExpression assignmentOperator expression
	;

assignmentOperator
	:	'=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '|=' | '^=' | '<<='
	|	rightShiftAssignment
	;

expression
	:	conditionalExpression
	|	assignment
	;

constantExpression
	:	expression
	;

booleanExpression
	:	expression
	;

statement
	:	labeledStatement
	|	declarationStatement
	|	embeddedStatement
	;

embeddedStatement
	:	block
	|	emptyStatement
	|	expressionStatement
	|	selectionStatement
	|	iterationStatement
	|	jumpStatement
	|	tryStatement
	|	checkedStatement
	|	uncheckedStatement
	|	lockStatement
	|	usingStatement
	|	yieldStatement
	;

block
	:	'{' ( statementList )? '}'
	;

statementList
	:	( statement )+
	;

emptyStatement
	:	';'
	;

labeledStatement
	:	identifier ':' statement
	;

declarationStatement
	:	localVariableDeclaration ';'
	|	localConstantDeclaration ';'
	;

localVariableDeclaration
	:	type localVariableDeclarators
	;

localVariableDeclarators
	:	localVariableDeclarator ( ',' localVariableDeclarator )*
	;

localVariableDeclarator
	:	identifier ( '=' localVariableInitializer )?
	;

localVariableInitializer
	:	expression
	|	arrayInitializer
	;

localConstantDeclaration
	:	'const' type constantDeclarators
	;

constantDeclarators
	:	constantDeclarator ( ',' constantDeclarator )*
	;

constantDeclarator
	:	identifier '=' constantExpression
	;

expressionStatement
	:	statementExpression ';'
	;

statementExpression
	:	invocationExpression
	|	objectCreationExpression
	|	assignment
	|	postIncrementExpression
	|	postDecrementExpression
	|	preIncrementExpression
	|	preDecrementExpression
	;

selectionStatement
	:	ifStatement
	|	switchStatement
	;

ifStatement
	:	'if' '(' booleanExpression ')' embeddedStatement ( 'else' embeddedStatement )?
	;

switchStatement
	:	'switch' '(' expression ')' switchBlock
	;

switchBlock
	:	'{' ( switchSections )? '}'
	;

switchSections
	:	( switchSection )+
	;

switchSection
	:	switchLabels statementList
	;

switchLabels
	:	( switchLabel )+
	;

switchLabel
	:	'case' constantExpression ':'
	|	'default' ':'
	;

iterationStatement
	:	whileStatement
	|	doStatement
	|	forStatement
	|	foreachStatement
	;

whileStatement
	:	'while' '(' booleanExpression ')' embeddedStatement
	;

doStatement
	:	'do' embeddedStatement 'while' '(' booleanExpression ')' ';'
	;

forStatement
	:	'for' '(' ( forInitializer )? ';' ( forCondition )? ';' ( forIterator )? ')'
		embeddedStatement
	;

forInitializer
	:	localVariableDeclaration
	|	statementExpressionList
	;

forCondition
	:	booleanExpression
	;

forIterator
	:	statementExpressionList
	;

statementExpressionList
	:	statementExpression ( ',' statementExpression )*
	;

foreachStatement
	:	'foreach' '(' type identifier 'in' expression ')' embeddedStatement
	;

jumpStatement
	:	breakStatement
	|	continueStatement
	|	gotoStatement
	|	returnStatement
	|	throwStatement
	;

breakStatement
	:	'break' ';'
	;

continueStatement
	:	'continue' ';'
	;

gotoStatement
	:	'goto' identifier ';'
	|	'goto' 'case' constantExpression ';'
	|	'goto' 'default' ';'
	;

returnStatement
	:	'return' ( expression )? ';'
	;

throwStatement
	:	'throw' ( expression )? ';'
	;

tryStatement
	:	'try' block catchClauses
	|	'try' block ( catchClauses )? finallyClause
	;

catchClauses
	:	specificCatchClauses
	|	( specificCatchClauses )? generalCatchClause
	;

specificCatchClauses
	:	( specificCatchClause )+
	;

specificCatchClause
	:	'catch' '(' classType ( identifier )? ')' block
	;

generalCatchClause
	:	'catch' block
	;

finallyClause
	:	'finally' block
	;

checkedStatement
	:	'checked' block
	;

uncheckedStatement
	:	'unchecked' block
	;

lockStatement
	:	'lock' '(' expression ')' embeddedStatement
	;

usingStatement
	:	'using' '(' resourceAcquisition ')' embeddedStatement
	;

resourceAcquisition
	:	localVariableDeclaration
	|	expression
	;

yieldStatement
	:	'yield' 'return' expression ';'
	|	'yield' 'break' ';'
	;

namespaceDeclaration
	:	'namespace' qualifiedIdentifier namespaceBody ( ';' )?
	;

qualifiedIdentifier
	:	identifier ( '.' identifier )*
	;

namespaceBody
	:	'{' ( externAliasDirectives )? ( usingDirectives )? ( namespaceMemberDeclarations )? '}'
	;

externAliasDirectives
	:	( externAliasDirective )+
	;

externAliasDirective
	:	'extern' 'alias' identifier ';'
	;

usingDirectives
	:	( usingDirective )+
	;

usingDirective
	:	usingAliasDirective
	|	usingNamespaceDirective
	;

usingAliasDirective
	:	'using' identifier '=' namespaceOrTypeName ';'
	;

usingNamespaceDirective
	:	'using' namespaceName ';'
	;

namespaceMemberDeclarations
	:	( namespaceMemberDeclaration )+
	;

namespaceMemberDeclaration
	:	namespaceDeclaration
	|	typeDeclaration
	;

typeDeclaration
	:	classDeclaration
	|	structDeclaration
	|	interfaceDeclaration
	|	enumDeclaration
	|	delegateDeclaration
	;

qualifiedAliasMember
	:	identifier '::' identifier ( typeArgumentList )?
	;
