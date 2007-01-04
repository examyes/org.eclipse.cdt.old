%options var=nt,automatic_ast,visitor=default
%options la=3
%options fp=CSharpParser
%options package=org.eclipse.cdt.csharp.core.parser
%options template=btParserTemplateD.g
%options import_terminals=CSharpLexer.g

$Start
	CompilationUnit
$End

$Terminals
	IDENTIFIER ::= identifier
	
	COLON_COLON ::= '::'
	QUESTION_QUESTION ::= '??'
    PLUS_PLUS                  ::= '++'
    MINUS_MINUS                ::= '--'
    EQUAL_EQUAL                ::= '=='
    LESS_EQUAL                 ::= '<='
    GREATER_EQUAL              ::= '>='
    NOT_EQUAL                  ::= '!='
    LEFT_SHIFT                 ::= '<<'
    RIGHT_SHIFT                ::= '>>'
    UNSIGNED_RIGHT_SHIFT       ::= '>>>'
    PLUS_EQUAL                 ::= '+='
    MINUS_EQUAL                ::= '-='
    MULTIPLY_EQUAL             ::= '*='
    DIVIDE_EQUAL               ::= '/='
    AND_EQUAL                  ::= '&='
    OR_EQUAL                   ::= '|='
    XOR_EQUAL                  ::= '^='
    REMAINDER_EQUAL            ::= '%='
    LEFT_SHIFT_EQUAL           ::= '<<='
    RIGHT_SHIFT_EQUAL          ::= '>>='
    UNSIGNED_RIGHT_SHIFT_EQUAL ::= '>>>='
    OR_OR                      ::= '||'
    AND_AND                    ::= '&&'

    PLUS      ::= '+'
    MINUS     ::= '-'
    NOT       ::= '!'
    REMAINDER ::= '%'
    XOR       ::= '^'
    AND       ::= '&'
    MULTIPLY  ::= '*'
    OR        ::= '|'
    TWIDDLE   ::= '~'
    DIVIDE    ::= '/'
    GREATER   ::= '>'
    LESS      ::= '<'
    LPAREN    ::= '('
    RPAREN    ::= ')'
    LBRACE    ::= '{'
    RBRACE    ::= '}'
    LBRACKET  ::= '['
    RBRACKET  ::= ']'
    SEMICOLON ::= ';'
    QUESTION  ::= '?'
    COLON     ::= ':'
    COMMA     ::= ','
    DOT       ::= '.'
    EQUAL     ::= '='
$End

$Rules

	-- A.1 Lexical grammar
	SemiOpt ::= $empty | ;
	
	ExternOpt ::= $empty | extern
	
	NewOpt ::= $empty | new

	PartialOpt ::= $empty | partial
	
	IdentifierOpt ::= $empty | identifier
	
	Literal
		::= BooleanLiteral
	
	BooleanLiteral ::= true | false
	
	-- A.2.1 Basic concepts
	
	-- TODO removed the global attributes since you can't tell them from the attributes of the first
	-- namespace member. Some smarts will have to be used to figure them out.
	CompilationUnit ::= ExternAliasDirectivesOpt UsingDirectivesOpt NamespaceMemberDeclarationsOpt
	
	Name
		::= identifier TypeArgumentListOpt
		 |	Name . identifier TypeArgumentListOpt
		 |	Name :: identifier TypeArgumentListOpt

	-- A.2.2 Types

	Type ::= SimpleType | NullableType | ClassType | ArrayType | void
	
	NonNamedType ::= SimpleType | NullableType | object | string | ArrayType
	
	SimpleType ::= NumericType | bool
	
	NumericType ::= IntegralType | FloatingPointType | decimal
	
	IntegralType ::= sbyte | byte | short | ushort | int | uint | long | ulong | char
	
	FloatingPointType ::= float | double
	
	NullableType ::= Type ?
	
	ClassType ::= Name | object | string
	
	-- A.2.3 Variables
	
	VariableReference ::= Expression
	
	-- A.2.4 Expressions
	
	ArgumentListOpt ::= $empty | ArgumentList
	
	ArgumentList
		::= Argument
		 |	ArgumentList , Argument
	
	Argument
		::= Expression
		 |	ref VariableReference
		 |	out VariableReference

	PrimaryExpression
		::=	ArrayCreationExpression
		 |	PrimaryNoArrayCreationExpression
		 |	Name

	PrimaryNoArrayCreationExpression
		::=	Literal
		 |	ParenthesizedExpression
		 |	MemberAccess
		 |	InvocationExpression
		 |	ElementAccess
		 |	ThisAccess
		 |	BaseAccess
		 |	PostIncrementExpression
		 |	PostDecrementExpression
		 |	ObjectCreationExpression
		 |	TypeofExpression
		 |	CheckedExpression
		 |	UncheckedExpression
		 |	DefaultValueExpression
		 |	AnonymousMethodExpression
	
	ParenthesizedExpression ::= ( Expression )
	
	MemberAccess
		::=	ArrayCreationExpression . identifier TypeArgumentListOpt
		 |	PrimaryNoArrayCreationExpression . identifier TypeArgumentListOpt
		 |	PredefinedType . identifier TypeArgumentListOpt
	
	PredefinedType
		::= bool | byte | char | decimal | double | float | int | long
		 |	object | sbyte | short | string | uint | ulong | ushort
	
	InvocationExpression ::= PrimaryExpression ( ArgumentListOpt )

	ElementAccess ::= PrimaryNoArrayCreationExpression [ ExpressionList ]
	
	ExpressionList ::= Expression | ExpressionList , Expression
	
	ThisAccess ::= this
	
	BaseAccess
		::= base . identifier TypeArgumentListOpt
		 |	base [ ExpressionList ]
	
	PostIncrementExpression ::= PrimaryExpression ++
	
	PostDecrementExpression ::= PrimaryExpression '--'
	
	ObjectCreationExpression ::= new Type ( ArgumentListOpt )
	
	ArrayCreationExpression
		::= new NonArrayType [ ExpressionList ] RankSpecifiersOpt ArrayInitializerOpt
		 |	new ArrayType ArrayInitializer
	
	TypeofExpression
		::= typeof ( Type )
		 |	typeof ( UnboundTypeName )
	
	UnboundTypeName
		::= identifier GenericDimensionSpecifierOpt
		 |	identifier :: identifier GenericDimensionSpecifierOpt
		 |	UnboundTypeName . identifier GenericDimensionSpecifierOpt

	GenericDimensionSpecifierOpt ::= $empty | GenericDimensionSpecifier
	
	GenericDimensionSpecifier ::= < CommasOpt >
	
	CommasOpt ::= $empty | Commas
	
	Commas ::= , | Commas ,
	
	CheckedExpression ::= checked ( Expression )
	
	UncheckedExpression ::= unchecked ( Expression )
	
	DefaultValueExpression ::= default ( Type )
	
	AnonymousMethodExpression ::= delegate AnonymousMethodSignatureOpt Block
	
	AnonymousMethodSignatureOpt ::= $empty | AnonymousMethodSignature
	
	AnonymousMethodSignature ::= ( AnonymousMethodParameterListOpt )
	
	AnonymousMethodParameterListOpt ::= $empty | AnonymousMethodParameterList
	
	AnonymousMethodParameterList ::= AnonymousMethodParameter | AnonymousMethodParameterList , AnonymousMethodParameter
	
	AnonymousMethodParameter ::= ParameterModifierOpt Type identifier
	
	UnaryExpression
		::=	PrimaryExpression
		 |	+ UnaryExpression
		 |	- UnaryExpression
		 |	! UnaryExpression
		 |	~ UnaryExpression
		 |	PreIncrementExpression
		 |	PreDecrementExpression
		 |	CastExpression
	
	PreIncrementExpression ::= ++ UnaryExpression
	
	PreDecrementExpression ::= '--' UnaryExpression
	
	CastExpression
		::= ( NonNamedType ) UnaryExpression
		-- TODO how to get a named Type into the cast Expression
		-- may need to try it and see which rule gets hit
	
	MultiplicativeExpression
		::= UnaryExpression
		 |	MultiplicativeExpression * UnaryExpression
		 |	MultiplicativeExpression / UnaryExpression
		 |	MultiplicativeExpression % UnaryExpression
	
	AdditiveExpression
		::=	MultiplicativeExpression
		 |	AdditiveExpression + MultiplicativeExpression
		 |	AdditiveExpression - MultiplicativeExpression
	
	ShiftExpression
		::= AdditiveExpression
		 |	ShiftExpression << AdditiveExpression
		 |	ShiftExpression >> AdditiveExpression
	
	RelationalExpression
		::=	ShiftExpression
		 |	RelationalExpression < ShiftExpression
		 |	RelationalExpression > ShiftExpression
		 |	RelationalExpression <= ShiftExpression
		 |	RelationalExpression >= ShiftExpression
		 |	RelationalExpression is Type
		 |	RelationalExpression as Type
	
	EqualityExpression
		::=	RelationalExpression
		 |	EqualityExpression == RelationalExpression
		 |	EqualityExpression != RelationalExpression
	
	AndExpression
		::= EqualityExpression
		 |	AndExpression & EqualityExpression
	
	ExclusiveOrExpression
		::= AndExpression
		 |	ExclusiveOrExpression ^ AndExpression

	InclusiveOrExpression
		::=	ExclusiveOrExpression
		 |	InclusiveOrExpression '|' ExclusiveOrExpression
	
	ConditionalAndExpression
		::=	InclusiveOrExpression
		 |	ConditionalAndExpression && InclusiveOrExpression
	
	ConditionalOrExpression
		::= ConditionalAndExpression
		 |	ConditionalOrExpression || ConditionalAndExpression
	
	NullCoalescingExpression
		::= ConditionalOrExpression
		 |	ConditionalOrExpression ?? NullCoalescingExpression
	
	ConditionalExpression
		::= NullCoalescingExpression
		 |	NullCoalescingExpression ? Expression : Expression
	
	Assignment ::= UnaryExpression AssignmentOperator Expression
	
	AssignmentOperator ::= = | += | -= | *= | /= | %= | &= | |= | ^= | <<= | >>=
	
	ExpressionOpt ::= $empty | Expression
	
	Expression ::= ConditionalExpression | Assignment
	
	ConstantExpression ::= Expression
	
	BooleanExpression ::= Expression
	
	-- A.2.6 Statements
	
	Statement
		::=	LabeledStatement
		 |	DeclarationStatement
		 |	EmbeddedStatement
	
	EmbeddedStatement
		::= Block
		 |	EmptyStatement
		 |	ExpressionStatement
		 |	SelectionStatement
		 |	IterationStatement
		 |	JumpStatement
		 |	TryStatement
		 |	CheckedStatement
		 |	UncheckedStatement
		 |	LockStatement
		 |	UsingStatement
		 |	YieldStatement
	
	Block ::= { StatementListOpt }
	
	StatementListOpt ::= $empty | StatementList
	
	StatementList ::= Statement | StatementList Statement
	
	EmptyStatement ::= ;
	
	LabeledStatement ::= identifier : Statement
	
	DeclarationStatement
		::=	LocalVariableDeclaration ;
		 |	LocalConstantDeclaration ;
	
	LocalVariableDeclaration ::= Type LocalVariableDeclarators
	
	LocalVariableDeclarators ::= LocalVariableDeclarator | LocalVariableDeclarators , LocalVariableDeclarator

	LocalVariableDeclarator
		::= identifier
		 |	identifier = LocalVariableInitializer

	LocalVariableInitializer ::= Expression | ArrayInitializer
	
	LocalConstantDeclaration ::= const Type ConstantDeclarators
	
	ExpressionStatement ::= StatementExpression ;
	
	StatementExpression
		::= InvocationExpression
		 |	ObjectCreationExpression
		 |	Assignment
		 |	PostIncrementExpression
		 |	PostDecrementExpression
		 |	PreIncrementExpression
		 |	PreDecrementExpression

	SelectionStatement ::= IfStatement | ElseStatement | SwitchStatement
	
	IfStatement
		::=	if ( BooleanExpression ) EmbeddedStatement

	-- obviously the else Statement must follow an if Statement	
	ElseStatement ::= else EmbeddedStatement
	
	SwitchStatement ::= switch ( Expression ) SwitchBlock
	
	SwitchBlock ::= { SwitchSectionsOpt }
	
	SwitchSectionsOpt ::= $empty | SwitchSections
	
	SwitchSections ::= SwitchSection | SwitchSections SwitchSection
	
	SwitchSection ::= SwitchLabels StatementList
	
	SwitchLabels ::= SwitchLabel | SwitchLabels SwitchLabel
	
	SwitchLabel
		::= case ConstantExpression :
		 |	default :

	IterationStatement ::= WhileStatement | DoStatement | ForStatement | ForeachStatement
	
	WhileStatement ::= while ( BooleanExpression ) EmbeddedStatement
	
	DoStatement ::= do EmbeddedStatement while ( BooleanExpression ) ;
	
	ForStatement ::= for ( ForInitializerOpt ; ForConditionOpt ; ForIteratorOpt ) EmbeddedStatement
	
	ForInitializerOpt ::= $empty | ForInitializer
	
	ForInitializer ::= LocalVariableDeclaration | StatementExpressionList
	
	ForConditionOpt ::= $empty | ForCondition
	
	ForCondition ::= BooleanExpression
	
	ForIteratorOpt ::= $empty | ForIterator
	
	ForIterator ::= StatementExpressionList
	
	StatementExpressionList ::= StatementExpression | StatementExpressionList , StatementExpression
	
	ForeachStatement ::= foreach ( Type identifier in Expression ) EmbeddedStatement
	
	JumpStatement ::= BreakStatement | ContinueStatement | GotoStatement | ReturnStatement | ThrowStatement
	
	BreakStatement ::= break ;
	
	ContinueStatement ::= continue ;
	
	GotoStatement
		::= goto identifier ;
		 |	goto case ConstantExpression ;
		 |	goto default ;
	
	ReturnStatement ::= return ExpressionOpt ;
	
	ThrowStatement ::= throw ExpressionOpt ;
	
	TryStatement
		::= try Block CatchClauses
		 |	try Block CatchClausesOpt FinallyClause
	
	CatchClausesOpt ::= $empty | CatchClauses
	
	CatchClauses
		::= SpecificCatchClauses
		 |	SpecificCatchClausesOpt GeneralCatchClause

	SpecificCatchClausesOpt ::= $empty | SpecificCatchClauses
	
	SpecificCatchClauses ::= SpecificCatchClause | SpecificCatchClauses SpecificCatchClause
	
	SpecificCatchClause ::= catch ( ClassType IdentifierOpt ) Block
	
	GeneralCatchClause ::= catch Block
	
	FinallyClause ::= finally Block
	
	CheckedStatement ::= checked Block
	
	UncheckedStatement ::= unchecked Block
	
	LockStatement ::= lock ( Expression ) EmbeddedStatement
	
	UsingStatement ::= using ( ResourceAcquisition ) EmbeddedStatement
	
	ResourceAcquisition ::= LocalVariableDeclaration | Expression
	
	YieldStatement
		::= yield return Expression ;
		 | 	yield break ;
	
	NamespaceDeclaration ::= namespace Name NamespaceBody SemiOpt
	
	NamespaceBody ::= { ExternAliasDirectivesOpt UsingDirectivesOpt NamespaceMemberDeclarationsOpt }
	
	ExternAliasDirectivesOpt ::= $empty | ExternAliasDirectives
	
	ExternAliasDirectives ::= ExternAliasDirective | ExternAliasDirectives ExternAliasDirective
	
	ExternAliasDirective ::= extern alias identifier ;
	
	UsingDirectivesOpt ::= $empty | UsingDirectives
	
	UsingDirectives ::= UsingDirective | UsingDirectives UsingDirective
	
	UsingDirective ::= UsingAliasDirective | UsingNamespaceDirective
	
	UsingAliasDirective ::= using identifier = Name ;

	UsingNamespaceDirective ::= using Name ;
	
	NamespaceMemberDeclarationsOpt ::= $empty | NamespaceMemberDeclarations
	
	NamespaceMemberDeclarations ::= NamespaceMemberDeclaration | NamespaceMemberDeclarations NamespaceMemberDeclaration
	
	NamespaceMemberDeclaration ::= NamespaceDeclaration | TypeDeclaration
	
	TypeDeclaration
		::= ClassDeclaration
		 |	StructDeclaration
		 |	InterfaceDeclaration
		 |	EnumDeclaration
		 |	DelegateDeclaration
	
	ModifiersOpt ::= $empty | Modifiers
	
	Modifiers ::= Modifier | Modifiers Modifier
	
	Modifier ::= new | public | protected | internal | private | abstract | sealed | static
		| readonly | volatile | virtual | override | extern

	-- A.2.6 Classes
	
	ClassDeclaration ::= AttributesOpt ModifiersOpt PartialOpt class identifier TypeParameterListOpt
		ClassBaseOpt TypeParameterConstraintsClausesOpt ClassBody SemiOpt

	ClassBaseOpt ::= $empty | : ClassBase
	
	ClassBase ::= ClassType | ClassBase , ClassType
	
	ClassBody ::= { ClassMemberDeclarationsOpt }
	
	ClassMemberDeclarationsOpt ::= $empty | ClassMemberDeclarations
	
	ClassMemberDeclarations ::= ClassMemberDeclaration | ClassMemberDeclarations ClassMemberDeclaration
	
	ClassMemberDeclaration ::= ConstantDeclaration | FieldDeclaration | MethodDeclaration | PropertyDeclaration
		| EventDeclaration | IndexerDeclaration | OperatorDeclaration | ConstructorDeclaration | FinalizerDeclaration
		| TypeDeclaration
	
	ConstantDeclaration ::= AttributesOpt ModifiersOpt const Type ConstantDeclarators ;
	
	ConstantDeclarators ::= ConstantDeclarator | ConstantDeclarators , ConstantDeclarator
	
	ConstantDeclarator ::= identifier = ConstantExpression
	
	FieldDeclaration ::= AttributesOpt ModifiersOpt Type VariableDeclarators ;
	
	VariableDeclarators ::= VariableDeclarator | VariableDeclarators , VariableDeclarator
	
	VariableDeclarator
		::= identifier
		 |	identifier = VariableInitializer

	VariableInitializer ::= Expression | ArrayInitializer
	
	MethodDeclaration ::= MethodHeader MethodBody
	
	MethodHeader ::= AttributesOpt ModifiersOpt Type Name TypeParameterListOpt
		( FormalParameterListOpt ) TypeParameterConstraintsClausesOpt

	MethodBody ::= Block | ;
	
	FormalParameterListOpt ::= $empty | FormalParameterList
	
	FormalParameterList
		::= FixedParameters
		 |	FixedParameters , ParameterArray
		 |	ParameterArray
	
	FixedParameters ::= FixedParameter | FixedParameters FixedParameter
	
	FixedParameter ::= AttributesOpt ParameterModifierOpt Type identifier
	
	ParameterModifierOpt ::= $empty | ParameterModifier
	
	ParameterModifier ::= ref | out
	
	ParameterArray ::= AttributesOpt params ArrayType identifier
	
	PropertyDeclaration ::= AttributesOpt ModifiersOpt Type Name { AccessorDeclarations }
	
	AccessorDeclarations
		::= GetAccessorDeclaration SetAccessorDeclarationOpt
		 |	SetAccessorDeclaration GetAccessorDeclarationOpt
	
	GetAccessorDeclarationOpt ::= $empty | GetAccessorDeclaration
	
	GetAccessorDeclaration ::= AttributesOpt AccessorModifierOpt get AccessorBody
	
	SetAccessorDeclarationOpt ::= $empty | SetAccessorDeclaration
	
	SetAccessorDeclaration ::= AttributesOpt AccessorModifierOpt set AccessorBody
	
	AccessorModifierOpt ::= $empty | AccessorModifier
	
	AccessorModifier ::= protected | internal | private | protected internal | internal protected
	
	AccessorBody ::= Block | ;
	
	EventDeclaration
		::= AttributesOpt ModifiersOpt event Type VariableDeclarators ;
		 |	AttributesOpt ModifiersOpt event Type Name { EventAccessorDeclarations }
	
	EventAccessorDeclarations
		::=	AddAccessorDeclaration RemoveAccessorDeclaration
		 |	RemoveAccessorDeclaration AddAccessorDeclaration
	
	AddAccessorDeclaration ::= AttributesOpt add Block
	
	RemoveAccessorDeclaration ::= AttributesOpt remove Block

	IndexerDeclaration ::= AttributesOpt ModifiersOpt IndexerDeclarator { AccessorDeclarations }
	
	IndexerDeclarator
		::=	Type this [ FormalParameterList ]
		 |	Type Name . this [ FormalParameterList ]

	OperatorDeclaration ::= AttributesOpt ModifiersOpt OperatorDeclarator OperatorBody
	
	OperatorDeclarator
		::= SymbolOperatorDeclarator
		 |	ConversionOperatorDeclarator
	
	SymbolOperatorDeclarator ::= Type operator OverloadableOperator ( FormalParameterList )
	
	OverloadableOperator ::= + | - | ! | ~ | ++ | '--' | true | false
		| * | / | % | & | '|' | ^ | << | >> | == | != | > | < | >= | <=
	
	ConversionOperatorDeclarator
		::= implicit operator Type ( Type identifier )
		 |	explicit operator Type ( Type identifier )

	OperatorBody ::= Block | ;
	
	ConstructorDeclaration ::= AttributesOpt ModifiersOpt ConstructorDeclarator ConstructorBody
	
	ConstructorDeclarator ::= identifier ( FormalParameterListOpt ) ConstructorInitializerOpt
	
	ConstructorInitializerOpt ::= $empty | ConstructorInitializer
	
	ConstructorInitializer
		::= base ( ArgumentListOpt )
		 |	this ( ArgumentListOpt )
	
	ConstructorBody ::= Block | ;
	
	FinalizerDeclaration ::= AttributesOpt ExternOpt ~ identifier ( ) FinalizerBody
	
	FinalizerBody ::= Block | ;
	
	-- A.2.7 Structs
	
	StructDeclaration ::= AttributesOpt ModifiersOpt PartialOpt struct identifier TypeParameterListOpt
		StructInterfacesOpt TypeParameterConstraintsClausesOpt StructBody SemiOpt
	
	StructInterfacesOpt ::= $empty | : StructInterfaces
	
	StructInterfaces ::= ClassType | StructInterfaces , ClassType
	
	StructBody ::= { StructMemberDeclarationsOpt }
	
	StructMemberDeclarationsOpt ::= $empty | StructMemberDeclarations
	
	StructMemberDeclarations ::= StructMemberDeclaration | StructMemberDeclarations StructMemberDeclaration
	
	StructMemberDeclaration ::= ConstantDeclaration | FieldDeclaration | MethodDeclaration | PropertyDeclaration
		| EventDeclaration | IndexerDeclaration | OperatorDeclaration | ConstructorDeclaration | TypeDeclaration
	
	-- A.2.8 Arrays
	
	ArrayType ::= NonArrayType RankSpecifiers
	
	NonArrayType ::= SimpleType | NullableType | ClassType
	
	RankSpecifiersOpt ::= $empty | RankSpecifiers
	
	RankSpecifiers
		::= RankSpecifier
		 |  RankSpecifiers RankSpecifier
	
	RankSpecifier ::= [ DimSeparatorsOpt ]
	
	DimSeparatorsOpt ::= $empty | DimSeparators
	
	DimSeparators ::= , | DimSeparators ,
	
	ArrayInitializerOpt ::= $empty | ArrayInitializer
	
	ArrayInitializer
		::=	{ VariableInitializerListOpt }
		 |	{ VariableInitializerList , }
	
	VariableInitializerListOpt ::= $empty | VariableInitializerList
	
	VariableInitializerList ::= VariableInitializer | VariableInitializerList , VariableInitializer
	
	-- A.2.9 Interfaces
	
	InterfaceDeclaration ::= AttributesOpt ModifiersOpt PartialOpt interface identifier TypeParameterListOpt
		InterfaceBaseOpt TypeParameterConstraintsClausesOpt InterfaceBody SemiOpt
	
	InterfaceBaseOpt ::= $empty | InterfaceBase
	
	InterfaceBase ::= Name | InterfaceBase , Name
	
	InterfaceBody ::= { InterfaceMemberDeclarationsOpt }
	
	InterfaceMemberDeclarationsOpt ::= $empty | InterfaceMemberDeclarations
	
	InterfaceMemberDeclarations ::= InterfaceMemberDeclaration | InterfaceMemberDeclarations InterfaceMemberDeclaration
	
	InterfaceMemberDeclaration ::= InterfaceMethodDeclaration | InterfacePropertyDeclaration
		| InterfaceEventDeclaration | InterfaceIndexerDeclaration
	
	InterfaceMethodDeclaration ::= AttributesOpt NewOpt Type identifier TypeParameterListOpt
		( FormalParameterListOpt ) TypeParameterConstraintsClausesOpt ;

	InterfacePropertyDeclaration ::= AttributesOpt NewOpt Type identifier { InterfaceAccessors }
	
	InterfaceAccessors
		::=	AttributesOpt get ;
		 |	AttributesOpt set ;
		 |	AttributesOpt get ; AttributesOpt set ;
		 |	AttributesOpt set ; AttributesOpt get ;
	
	InterfaceEventDeclaration ::= AttributesOpt NewOpt event Type identifier ;
	
	InterfaceIndexerDeclaration ::= AttributesOpt NewOpt Type this [ FormalParameterList ] { InterfaceAccessors }

	-- A.2.10 Enums
	
	EnumDeclaration ::= AttributesOpt ModifiersOpt enum identifier EnumBaseOpt EnumBody SemiOpt
	
	EnumBaseOpt ::= $empty | EnumBase
	
	EnumBase ::= : IntegralType
	
	EnumBody
		::= { EnumMemberDeclarationsOpt }
		 |	{ EnumMemberDeclarations , }
	
	EnumMemberDeclarationsOpt ::= $empty | EnumMemberDeclarations
	
	EnumMemberDeclarations ::= EnumMemberDeclaration | EnumMemberDeclarations , EnumMemberDeclaration
	
	EnumMemberDeclaration
		::= AttributesOpt identifier
		 |	AttributesOpt identifier = ConstantExpression
	
	-- A.2.11 Delegates
	
	DelegateDeclaration ::= AttributesOpt ModifiersOpt delegate Type identifier TypeParameterListOpt
		( FormalParameterListOpt ) TypeParameterConstraintsClausesOpt ;
	
	-- A.2.12 Attributes
	
	AttributesOpt ::= $empty | Attributes
	
	Attributes ::= AttributeSections
	
	AttributeSections ::= AttributeSection | AttributeSections AttributeSection
	
	AttributeSection
		::= [ AttributeTargetSpecifierOpt AttributeList ]
		 |	[ AttributeTargetSpecifierOpt AttributeList , ]
	
	AttributeTargetSpecifierOpt ::= $empty | AttributeTargetSpecifier
	
	AttributeTargetSpecifier ::= AttributeTarget :

	AttributeTarget ::= identifier | Keyword
	
	Keyword ::=	abstract | add | alias | as | base | bool | break | byte | case | catch
		| char | checked | class | const | continue | decimal | default | delegate
		| do | double | else | enum | event | explicit | extern | false | finally
		| fixed | float | for | foreach | get | goto | if | implicit | in | int
		| interface | internal | is | lock | long | namespace | new | null | object
		| operator | out | override | params | partial | private | protected | public
		| readonly | ref | remove | return | sbyte | sealed | set | short | sizeof
		| stackalloc | static | string | struct | switch | this | throw | true
		| try | typeof | uint | ulong | unchecked | unsafe | ushort | using
		| virtual | void | volatile | where | while | yield
	
	AttributeList ::= Attribute | AttributeList , Attribute
	
	Attribute ::= AttributeName AttributeArgumentsOpt
	
	AttributeName ::= Name
	
	AttributeArgumentsOpt ::= $empty | AttributeArguments
	
	AttributeArguments
		::= ( PositionalArgumentListOpt )
		 |	( PositionalArgumentList , NamedArgumentList )
		 |	( NamedArgumentList )
	
	PositionalArgumentListOpt ::= $empty | PositionalArgumentList
	
	PositionalArgumentList ::= PositionalArgument | PositionalArgumentList , PositionalArgument
	
	PositionalArgument ::= AttributeArgumentExpression
	
	NamedArgumentList ::= NamedArgument | NamedArgumentList , NamedArgument
	
	NamedArgument ::= identifier = AttributeArgumentExpression
	
	AttributeArgumentExpression ::= Expression
	
	TypeParameterListOpt ::= $empty | TypeParameterList
	
	TypeParameterList ::= < TypeParameters >
	
	TypeParameters
		::= AttributesOpt TypeParameter
		 |	TypeParameters , AttributesOpt TypeParameter
	
	TypeParameter ::= identifier
	
	TypeArgumentListOpt ::= $empty | TypeArgumentList
	
	TypeArgumentList ::= < TypeArguments >
	
	TypeArguments ::= TypeArgument | TypeArguments , TypeArgument
	
	TypeArgument ::= Type
	
	TypeParameterConstraintsClausesOpt ::= $empty | TypeParameterConstraintsClauses
	
	TypeParameterConstraintsClauses
		::= TypeParameterConstraintsClause
		 |	TypeParameterConstraintsClauses TypeParameterConstraintsClause
	
	TypeParameterConstraintsClause ::= where TypeParameter : TypeParameterConstraints

	TypeParameterConstraints
		::= Constraints
		 |	ConstructorConstraint
		 |	Constraint , ConstructorConstraint

	Constraints ::= Constraint | Constraints , Constraint
	
	Constraint ::= ClassType | class | struct
	
	ConstructorConstraint ::= new ( )	
$End
