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
	:	IDENTIFIER ( '::' IDENTIFIER )?
//		( typeArgumentList | genericDimensionSpecifier )?
		(	'.' IDENTIFIER 
//			( typeArgumentList | genericDimensionSpecifier )? 
		)*
	;

type
	:	typeName
//		( '?' )?
		( rankSpecifiers )?
	;

typeName
	:	name
	|	'sbyte'
	|	'byte'
	|	'short'
	|	'ushort'
	|	'int'
	|	'uint'
	|	'long'
	|	'ulong'
	|	'char'
	|	'float'
	|	'double'
	|	'decimal'
	|	'bool'
	|	'object'
	|	'string'
	;

rankSpecifiers
	:	( rankSpecifier )+
	;

rankSpecifier
	:	'[' ( ',' )* ']'
	;

arrayInitializer
	:	'{' ( variableInitializer )? '}'
	;

variableInitializerList
	:	variableInitializer ( ',' variableInitializer )* ( ',' )?
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
	:	primaryNoArrayCreationExpression ( primarySuffix )*
	;

primaryNoArrayCreationExpression
	:	literal
	|	name // covers member access
	|	parenthesizedExpression
	|	thisAccess
	|	baseAccess
	|	objectCreationExpression
	|	typeofExpression
	|	checkedExpression
	|	uncheckedExpression
	|	defaultValueExpression
	|	anonymousMethodExpression
	|	arrayCreationExpression
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

expressionList
	:	expression ( ',' expression )*
	;

thisAccess
	:	'this'
	;

baseAccess
	:	'base' '.' name
	|	'base' '[' expressionList ']'
	;

objectCreationExpression
	:	'new' type '(' ( argumentList )? ')'
	;

arrayCreationExpression
	:	'new' type '[' expressionList ']' ( rankSpecifiers )? ( arrayInitializer )?
	;

typeofExpression
	:	'typeof' '(' name ')'
	|	'typeof' '(' 'void' ')'
	;

genericDimensionSpecifier
	:	'<' ( ',' )* '>'
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
	:	( 'ref' | 'out' )? type IDENTIFIER
	;

unaryExpression
	:	primaryExpression
	|	'+' unaryExpression
	|	'-' unaryExpression
	|	'!' unaryExpression
	|	'~' unaryExpression
	|	preIncrementExpression
	|	preDecrementExpression
//	|	castExpression
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

expression
	:	nullCoalescingExpression
		(	( '?' expression ':' expression )
		|	( assignmentOperator expression )
		)?
	;

assignmentOperator
	:	'=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '|=' | '^=' | '<<='
	|	rightShiftAssignment
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
	:	expression ';'
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
	|	expressionList
	;

forCondition
	:	booleanExpression
	;

forIterator
	:	expressionList
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
	:	'try' block ( specificCatchClause )* ( generalCatchClause )? ( finallyClause )?
	;

specificCatchClause
	:	'catch' '(' type ( IDENTIFIER )? ')' block
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
	:	( attributes )? ( modifiers )? typeDeclarationProper
	;

typeDeclarationProper
	:	( 'partial' )?
		(	classDeclaration
		|	structDeclaration
		|	interfaceDeclaration
		|	enumDeclaration
		|	delegateDeclaration
		)
	;

modifiers
	:	( modifier )+
	;

modifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'abstract'
	|	'sealed'
	|	'static'
	;

classDeclaration
	:	'class' IDENTIFIER ( typeParameterList )?
		( classBase )? ( typeParameterConstraintsClauses )?
		classBody ( ';' )?
	;

classBase
	:	type ( ',' type )*
	;

classBody
	:	'{' ( classMemberDeclarations )? '}'
	;

classMemberDeclarations
	:	( classMemberDeclaration )+
	;

classMemberDeclaration
	:	( attributes )? ( classMemberModifiers )?
		(	constantDeclaration
		|	fieldDeclaration
		|	methodDeclaration
		|	propertyDeclaration
		|	eventDeclaration
		|	indexerDeclaration
		|	operatorDeclaration
		|	constructorDeclaration
		|	finalizerDeclaration
		|	typeDeclarationProper
		)
	;

classMemberModifiers
	:	( classMemberModifier )+
	;

classMemberModifier
	:	'new'
	|	'public'
	|	'protected'
	|	'internal'
	|	'private'
	|	'static'
	|	'readonly'
	|	'volatile'
	|	'virtual'
	|	'sealed'
	|	'override'
	|	'abstract'
	|	'extern'
	;
	
constantDeclaration
	:	'const' type constantDeclarators ';'
	;

constructorDeclarators
	:	constantDeclarator ( ',' constantDeclarator )*
	;

fieldDeclaration
	:	type variableDeclarators ';'
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
	:	returnType name ( typeParameterList )?
		'(' ( formalParameterList )? ')' ( typeParameterConstraintsClauses )?
	;

returnType
	:	type
	|	'void'
	;

methodBody
	:	block
	|	';'
	;

formalParameterList
	:	formalParameter ( ',' formalParameter )*
	;

formalParameter
	:	( attributes )? ( 'ref' | 'out' | 'params' ) type IDENTIFIER
	;

propertyDeclaration
	:	type name '{' accessorDeclarations '}'
	;

accessorDeclarations
	:	accessorDeclaration ( accessorDeclaration )?
	;

accessorDeclaration
	:	( attributes )? ( accessorModifier )? ( 'get' | 'set' ) accessorBody
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
	:	'event' type variableDeclarators ';'
	|	'event' type name
		'{' eventAccessorDeclarations '}'
	;

eventAccessorDeclarations
	:	eventAccessorDeclaration eventAccessorDeclaration
	;

eventAccessorDeclaration
	:	( attributes )? ( 'add' | 'remove' ) block
	;

indexerDeclaration
	:	indexerDeclarator '{' accessorDeclarations '}'
	;

indexerDeclarator
	:	type 'this' '[' formalParameterList ']'
	|	type type '.' 'this' '[' formalParameterList ']'
	;

operatorDeclaration
	:	operatorDeclarator operatorBody
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
	:	constructorDeclarator constructorBody
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

finalizerDeclaration
	:	'~' IDENTIFIER '(' ')' finalizerBody
	;

finalizerBody
	:	block
	| 	';'
	;

structDeclaration
	:	'struct' IDENTIFIER	( typeParameterList )?
		( structInterfaces )? ( typeParameterConstraints )?
		structBody ( ';' )?
	;
	
structInterfaces
	:	':' type ( ',' type )*
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
	|	typeDeclaration
	;

interfaceDeclaration
	:	'interface' IDENTIFIER ( typeParameterList )?
		( interfaceBase )? ( typeParameterConstraintsClauses )?
		interfaceBody ( ';' )?
	;

interfaceBase
	:	':' type ( ',' type )*
	;

interfaceBody
	:	'{' ( interfaceMemberDeclarations )? '}'
	;

interfaceMemberDeclarations
	:	( interfaceMemberDeclaration )+
	;

interfaceMemberDeclaration
	:	( attributes )?
		(	interfaceMethodDeclaration
		|	interfacePropertyDeclaration
		|	interfaceEventDeclaration
		|	interfaceIndexerDeclaration
		)
	;

interfaceMethodDeclaration
	:	( 'new' )? returnType IDENTIFIER ( typeParameterList )?
		'(' ( formalParameterList )? ')' ( typeParameterConstraintsClauses )? ';'
	;

interfacePropertyDeclaration
	:	( 'new' )? type IDENTIFIER '{' interfaceAccessors '}'
	;

interfaceAccessors
	:	interfaceAccessor ( interfaceAccessor )?
	;

interfaceAccessor
	:	( attributes )? ( 'get' | 'set' ) ';'
	;

interfaceEventDeclaration
	:	( 'new' )? 'event' type IDENTIFIER ';'
	;

interfaceIndexerDeclaration
	:	( 'new' )? type 'this' '[' formalParameterList ']'
		'{' interfaceAccessors '}'
	;

enumDeclaration
	:	'enum' IDENTIFIER ( enumBase )?
		enumBody ( ';' )?
	;

enumBase
	:	':' type
	;

enumBody
	:	'{' ( enumMemberDeclarations )? '}'
	;

enumMemberDeclarations
	:	enumMemberDeclaration ( ',' enumMemberDeclaration )* ( ',' )?
	;

enumMemberDeclaration
	:	( attributes )? IDENTIFIER ( '=' constantExpression )?
	;

delegateDeclaration
	:	'delegate' returnType IDENTIFIER
		( typeParameterList )? '(' ( formalParameterList )?
		( typeParameterConstraintsClauses )? ';'
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
	:	'(' ( argumentList )? ')'
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
	:	typeParameterConstraint ( ',' typeParameterConstraint )*
	;

typeParameterConstraint
	:	type
	|	'new' '(' ')'
	;
