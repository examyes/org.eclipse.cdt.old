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

// To do Unicode character classes
IDENTIFIER
	:	( '@' )? ( 'a'..'z' | 'A'..'Z' | '_' )
		( 'a'..'z' | 'A'..'Z' | '_' | '0'..'9' )*
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
	:	( externAliasDirective )? ( usingDirectives )? /*( globalAttributes )?*/
		( namespaceMemberDeclarations )?
	;

name
	:	IDENTIFIER ( '::' IDENTIFIER )? ( typeArgumentList | genericDimensionSpecifier )?
		( '.' IDENTIFIER ( typeArgumentList | genericDimensionSpecifier )? )*
	;

type
	:	name
	|	simpleType
	|	nullableType
	|	referenceType
	|	typeParameter
	;

simpleType
	:	integralType
	|	'float'
	|	'double'
	|	'decimal'
	|	'bool'
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

nullableType
	:	nonNullableValueType '?'
	;

nonNullableValueType
	:	name
	|	simpleType
	;

referenceType
	:	classType
	|	interfaceType
	|	arrayType
	|	delegateType
	;

classType
	:	name
	|	'object'
	|	'string'
	;

interfaceType
	:	name
	;

arrayType
	:	nonArrayType rankSpecifiers
	;

nonArrayType
	:	name
	|	simpleType
	|	nullableType
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

arrayInitializer
	:	'{' ( variableInitializerList )? '}'
	|	'{' variableInitializerList ',' '}'
	;

variableInitializerList
	:	variableInitializer ( ',' variableInitializer )*
	;

delegateType
	:	name
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
	|	primaryNoArrayCreationExpression ( primarySuffix )*
	;

primaryNoArrayCreationExpression
	:	literal
	|	name // covers member access
	|	parenthesizedExpression
	|	memberAccess
	|	thisAccess
	|	baseAccess
	|	objectCreationExpression
	|	delegateCreationExpression
	|	typeofExpression
	|	checkedExpression
	|	uncheckedExpression
	|	defaultValueExpression
	|	anonymousMethodExpression
	;

// removes the left recursion
primarySuffix
	:	'++'
	|	'--'
	|	'[' expressionList ']'
	|	'(' ( argumentList )? ')'
	;
	
parenthesizedExpression
	:	'(' expression ')'
	;

memberAccess
	:	predefinedType '.' IDENTIFIER ( typeArgumentList )?
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
	:	'base' '.' IDENTIFIER ( typeArgumentList )?
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
	|	'typeof' '(' name ')'
	|	'typeof' '(' 'void' ')'
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
	:	( parameterModifier )? type IDENTIFIER
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
	:	IDENTIFIER ':' statement
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
	:	IDENTIFIER ( '=' localVariableInitializer )?
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
	:	IDENTIFIER '=' constantExpression
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
	:	'foreach' '(' type IDENTIFIER 'in' expression ')' embeddedStatement
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
	:	'goto' IDENTIFIER ';'
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
	:	'catch' '(' classType ( IDENTIFIER )? ')' block
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
	:	IDENTIFIER ( '.' IDENTIFIER )*
	;

namespaceBody
	:	'{' ( externAliasDirectives )? ( usingDirectives )? ( namespaceMemberDeclarations )? '}'
	;

externAliasDirectives
	:	( externAliasDirective )+
	;

externAliasDirective
	:	'extern' 'alias' IDENTIFIER ';'
	;

usingDirectives
	:	( usingDirective )+
	;

usingDirective
	:	usingAliasDirective
	|	usingNamespaceDirective
	;

usingAliasDirective
	:	'using' IDENTIFIER '=' name ';'
	;

usingNamespaceDirective
	:	'using' name ';'
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

classDeclaration
	:	( attributes )? ( classModifiers )? ( 'partial' )? 'class' IDENTIFIER
		( typeParameterList )? ( classBase )? ( typeParameterConstraintsClauses )?
		classBody ( ';' )?
	;

classModifiers
	:	( classModifier )+
	;

classModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'abstract'
	|	'sealed'
	|	'static'
	;

classBase
	:	classType ( ',' interfaceTypeList )?
	|	interfaceTypeList
	;

interfaceTypeList
	:	interfaceType ( ',' interfaceType )*
	;

classBody
	:	'{' ( classMemberDeclarations )? '}'
	;

classMemberDeclarations
	:	( classMemberDeclaration )+
	;

classMemberDeclaration
	:	constantDeclaration
	|	fieldDeclaration
	|	methodDeclaration
	|	propertyDeclaration
	|	eventDeclaration
	|	indexerDeclaration
	|	operatorDeclaration
	|	constructorDeclaration
	|	finalizerDeclaration
	|	staticConstructorDeclaration
	|	typeDeclaration
	;

constantDeclaration
	:	( attributes )? ( constantModifiers )? 'const' type constantDeclarators ';'
	;

constantModifiers
	:	( constantModifier )+
	;

constantModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	;

constructorDeclarators
	:	constantDeclarator ( ',' constantDeclarator )*
	;

fieldDeclaration
	:	( attributes )? ( fieldModifiers )? type variableDeclarators ';'
	;

fieldModifiers
	:	( fieldModifier )+
	;

fieldModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'static'
	|	'readonly'
	|	'volatile'
	;

variableDeclarators
	:	variableDeclarator ( ',' variableDeclarator )*
	;

variableDeclarator
	:	IDENTIFIER ( '=' variableInitializer )?
	;

variableInitializer
	:	expression
	|	arrayInitializer
	;

methodDeclaration
	:	methodHeader methodBody
	;

methodHeader
	:	( attributes )? ( methodModifiers )? returnType memberName ( typeParameterList )?
		'(' ( formalParameterList )? ')' ( typeParameterConstraintsClauses )?
	;

methodModifiers
	:	( methodModifier )+
	;

methodModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'static'
	|	'virtual'
	|	'sealed'
	|	'override'
	|	'abstract'
	|	'extern'
	;

returnType
	:	type
	|	'void'
	;

memberName
	:	IDENTIFIER
	|	interfaceType '.' IDENTIFIER
	;

methodBody
	:	block
	|	';'
	;

formalParameterList
	:	fixedParameters ( ',' parameterArray )?
	|	parameterArray
	;

fixedParameters
	:	fixedParameter ( ',' fixedParameter )*
	;

fixedParameter
	:	( attributes )? ( parameterModifier )? type IDENTIFIER
	;

parameterModifier
	:	'ref' | 'out'
	;

parameterArray
	:	( attributes )? 'params' arrayType IDENTIFIER
	;

propertyDeclaration
	:	( attributes )? ( propertyModifiers )? type memberName '{' accessorDeclarations '}'
	;

propertyModifiers
	:	( propertyModifier )+
	;

propertyModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'static'
	|	'virtual'
	|	'sealed'
	|	'override'
	|	'abstract'
	|	'extern'
	;

accessorDeclarations
	:	getAccessorDeclaration ( setAccessorDeclaration )?
	|	setAccessorDeclaration ( getAccessorDeclaration )?
	;

getAccessorDeclaration
	:	( attributes )? ( accessorModifier )? 'get' accessorBody
	;

setAccessorDeclaration
	:	( attributes )? ( accessorModifier )? 'set' accessorBody
	;

accessorModifier
	:	'protected'
	|	'internal'
	|	'private'
	|	'protected' 'internal'
	|	'internal' 'protected'
	;

accessorBody
	:	block
	|	';'
	;

eventDeclaration
	:	( attributes )? ( eventModifiers )? 'event' type variableDeclarators ';'
	|	( attributes )? ( eventModifiers )? 'event' type memberName
		'{' eventAccessorDeclarations '}'
	;

eventModifiers
	:	( eventModifier )+
	;

eventModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'static'
	|	'virtual'
	|	'sealed'
	|	'override'
	|	'abstract'
	|	'extern'
	;


eventAccessorDeclarations
	:	addAccessorDeclaration removeAccessorDeclaration
	|	removeAccessorDeclaration addAccessorDeclaration
	;

addAccessorDeclaration
	:	( attributes )? 'add' block
	;

removeAccessorDeclaration
	:	( attributes )? 'remove' block
	;

indexerDeclaration
	:	( attributes )? ( indexerModifiers )? indexerDeclarator '{' accessorDeclarations '}'
	;

indexerModifiers
	:	( indexerModifier )+
	;

indexerModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'virtual'
	|	'sealed'
	|	'override'
	|	'abstract'
	|	'extern'
	;

indexerDeclarator
	:	type 'this' '[' formalParameterList ']'
	|	type interfaceType '.' 'this' '[' formalParameterList ']'
	;

operatorDeclaration
	:	( attributes )? operatorModifiers operatorDeclarator operatorBody
	;

operatorModifiers
	:	( operatorModifier )+
	;

operatorModifier
	:	'public'
	|	'static'
	|	'extern'
	;

operatorDeclarator
	:	unaryOperatorDeclarator
	|	binaryOperatorDeclarator
	|	conversionOperatorDeclarator
	;

unaryOperatorDeclarator
	:	type 'operator' overloadableUnaryOperator '(' type IDENTIFIER ')'
	;

overloadableUnaryOperator
	:	'+' | '-' | '!' | '~' | '++' | '--' | 'true' | 'false'
	;

binaryOperatorDeclarator
	:	type 'operator' overloadableBinaryOperator
		'(' type IDENTIFIER ',' type IDENTIFIER ')'
	;

overloadableBinaryOperator
	:	'+' | '-' | '*' | '/' | '%'
	|	'&' | '|' | '^' | '<<' | rightShift
	|	'==' | '!=' | '>' | '<' | '>=' | '<='
	;

conversionOperatorDeclarator
	:	( 'implicit' | 'explicit' ) 'operator' type '(' type IDENTIFIER ')'
	;

operatorBody
	:	block
	|	';'
	;

constructorDeclaration
	:	( attributes )? ( constructorModifiers )? constructorDeclarator constructorBody
	;

constructorModifiers
	:	( constructorModifier )+
	;

constructorModifier
	:	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'extern'
	;

constructorDeclarator
	:	IDENTIFIER '(' ( formalParameterList )? ')' ( constructorInitializer )?
	;

constructorInitializer
	:	':' 'base' '(' ( argumentList )? ')'
	|	':' 'this' '(' ( argumentList )? ')'
	;

constructorBody
	:	block
	|	';'
	;

staticConstructorDeclaration
	:	( attributes )? staticConstructorModifiers IDENTIFIER '(' ')' staticConstructorBody
	;

staticConstructorModifiers
	:	'extern' 'static'
	|	'static' ( 'extern' )?
	;

staticConstructorBody
	:	block
	| 	';'
	;

finalizerDeclaration
	:	( attributes )? ( 'extern' )? '~' IDENTIFIER '(' ')' finalizerBody
	;

finalizerBody
	:	block
	| 	';'
	;

structDeclaration
	:	( attributes )? ( structModifiers )? ( 'partial' )? 'struct' IDENTIFIER
		( typeParameterList )? ( structInterfaces )? ( typeParameterConstraints )?
		structBody ( ';' )?
	;
	
structModifiers
	:	( structModifier )+
	;

structModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	;

structInterfaces
	:	':' interfaceTypeList
	;

structBody
	:	'{' ( structMemberDeclarations )? '}'
	;

structMemberDeclarations
	:	( structMemberDeclaration )+
	;

structMemberDeclaration
	:	constantDeclaration
	|	fieldDeclaration
	|	methodDeclaration
	|	propertyDeclaration
	|	eventDeclaration
	|	indexerDeclaration
	|	operatorDeclaration
	|	constructorDeclaration
	|	staticConstructorDeclaration
	|	typeDeclaration
	;

interfaceDeclaration
	:	( attributes )? ( interfaceModifiers )? ( 'partial' )? 'interface' IDENTIFIER
		( typeParameterList )? ( interfaceBase )? ( typeParameterConstraintsClauses )?
		interfaceBody ( ';' )?
	;

interfaceModifiers
	:	( interfaceModifier )+
	;

interfaceModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	;

interfaceBase
	:	':' interfaceTypeList
	;

interfaceBody
	:	'{' ( interfaceMemberDeclarations )? '}'
	;

interfaceMemberDeclarations
	:	( interfaceMemberDeclaration )+
	;

interfaceMemberDeclaration
	:	interfaceMethodDeclaration
	|	interfacePropertyDeclaration
	|	interfaceEventDeclaration
	|	interfaceIndexerDeclaration
	;

interfaceMethodDeclaration
	:	( attributes )? ( 'new' )? returnType IDENTIFIER ( typeParameterList )?
		'(' ( formalParameterList )? ')' ( typeParameterConstraintsClauses )? ';'
	;

interfacePropertyDeclaration
	:	( attributes )? ( 'new' )? type IDENTIFIER '{' interfaceAccessors '}'
	;

interfaceAccessors
	:	( attributes )? 'get' ';'
	|	( attributes )? 'set' ';'
	|	( attributes )? 'set' ';' ( attributes )? 'get' ';'
	|	( attributes )? 'get' ';' ( attributes )? 'set' ';'
	;

interfaceEventDeclaration
	:	( attributes )? ( 'new' )? 'event' type IDENTIFIER ';'
	;

interfaceIndexerDeclaration
	:	( attributes )? ( 'new' )? type 'this' '[' formalParameterList ']'
		'{' interfaceAccessors '}'
	;

enumDeclaration
	:	( attributes )? ( enumModifiers )? 'enum' IDENTIFIER ( enumBase )?
		enumBody ( ';' )?
	;

enumBase
	:	':' integralType
	;

enumBody
	:	'{' ( enumMemberDeclarations )? '}'
	|	'{' enumMemberDeclarations ',' '}'
	;

enumModifiers
	:	( enumModifier )+
	;

enumModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	;

enumMemberDeclarations
	:	enumMemberDeclaration ( ',' enumMemberDeclaration )*
	;

enumMemberDeclaration
	:	( attributes )? IDENTIFIER ( '=' constantExpression )?
	;

delegateDeclaration
	:	( attributes )? ( delegateModifiers )? 'delegate' returnType IDENTIFIER
		( typeParameterList )? '(' ( formalParameterList )?
		( typeParameterConstraintsClauses )? ';'
	;

delegateModifiers
	:	( delegateModifier )+
	;

delegateModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	;

globalAttributes
	:	globalAttributeSections
	;

globalAttributeSections
	:	( globalAttributeSection )+
	;

globalAttributeSection
	:	'[' globalAttributeTargetSpecifier attributeList ( ',' )? ']'
	;

globalAttributeTargetSpecifier
	:	globalAttributeTarget ':'
	;

globalAttributeTarget
	:	IDENTIFIER
	|	keyword
	;

attributes
	:	attributeSections
	;

attributeSections
	:	( attributeSection )+
	;

attributeSection
	:	'[' ( attributeTargetSpecifier )? attributeList ( ',' )? ']'
	;

attributeTargetSpecifier
	:	attributeTarget ':'
	;

attributeTarget
	:	IDENTIFIER
	|	keyword
	;

attributeList
	:	attribute ( ',' attribute )*
	;

attribute
	:	attributeName ( attributeArguments )?
	;

attributeName
	:	name
	;

attributeArguments
	:	'(' ( positionalArgumentList )? ')'
	|	'(' positionalArgumentList ',' namedArgumentList ')'
	|	'(' namedArgumentList ')'
	;

positionalArgumentList
	:	positionalArgument ( ',' positionalArgument )*
	;

positionalArgument
	:	attributeArgumentExpression
	;

namedArgumentList
	:	namedArgument ( ',' namedArgument )*
	;

namedArgument
	:	IDENTIFIER '=' attributeArgumentExpression
	;

attributeArgumentExpression
	:	expression
	;

typeParameterList
	:	'<' typeParameters '>'
	;

typeParameters
	:	( attributes )? typeParameter ( ',' ( attributes )? typeParameter )*
	;

typeParameter
	:	IDENTIFIER
	;

typeArgumentList
	:	'<' typeArguments '>'
	;

typeArguments
	:	 typeArgument ( ',' typeArgument )*
	;

typeArgument
	:	type
	;

typeParameterConstraintsClauses
	:	( typeParameterConstraintsClause )+
	;

typeParameterConstraintsClause
	:	'where' typeParameter ':' typeParameterConstraints
	;

typeParameterConstraints
	:	primaryConstraint
	|	secondaryConstraints
	|	constructorConstraint
	|	primaryConstraint ',' secondaryConstraints
	|	primaryConstraint ',' constructorConstraint
	|	secondaryConstraints ',' constructorConstraint
	|	primaryConstraint ',' secondaryConstraints ',' constructorConstraint
	;

primaryConstraint
	:	classType
	|	'class'
	|	'struct'
	;

secondaryConstraints
	:	secondaryConstraint ( ',' secondaryConstraint )*
	;

secondaryConstraint
	:	interfaceType
	|	typeParameter
	;

constructorConstraint
	:	'new' '(' ')'
	;
