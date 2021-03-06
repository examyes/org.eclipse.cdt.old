-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2007 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.parser.c99
%options template=btParserTemplateD.g
%options import_terminals=C99Lexer.g


$Notice
/./*******************************************************************************
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
./
$End

$Terminals
	
	-- Most terminals are defined in the lexer and imported from there.
	-- This section lists terminals that are not defined in the lexer such as keywords.
	
	
	-- The scanner does not recognize keywords, it will return them as identifier tokens.
	-- An IKeywordMap is used to convert these identifiers to keyword tokens.
	
	auto      break     case      char  
	const     continue  default   do       
	double    else      enum      extern 
	float     for       goto      if        
	inline    int       long      register 
	restrict  return    short     signed     
	sizeof    static    struct    switch
	typedef   union     unsigned  void 
	volatile  while
	_Bool     _Complex  _Imaginary
	
	
    -- These are aliases for lexer tokens.


	LeftBracket      ::= '['
	LeftParen        ::= '('
	LeftBrace        ::= '{'
	Dot              ::= '.'
	Arrow            ::= '->'
	PlusPlus         ::= '++'
	MinusMinus       ::= '--'
	And              ::= '&'
	Star             ::= '*'
	Plus             ::= '+'
	Minus            ::= '-'
	Tilde            ::= '~'
	Bang             ::= '!'
	Slash            ::= '/'
	Percent          ::= '%'
	RightShift       ::= '>>'
	LeftShift        ::= '<<'
	LT               ::= '<'
	GT               ::= '>'
	LE               ::= '<='
	GE               ::= '>='
	EQ               ::= '=='
	NE               ::= '!='
	Caret            ::= '^'
	Or               ::= '|'
	AndAnd           ::= '&&'
	OrOr             ::= '||'
	Question         ::= '?'
	Colon            ::= ':'
	DotDotDot        ::= '...'
	Assign           ::= '='
	StarAssign       ::= '*='
	SlashAssign      ::= '/='
	PercentAssign    ::= '%='
	PlusAssign       ::= '+='
	MinusAssign      ::= '-='
	RightShiftAssign ::= '>>='
	LeftShiftAssign  ::= '<<='
	AndAssign        ::= '&='
	CaretAssign      ::= '^='
	OrAssign         ::= '|='
	Comma            ::= ','
	Hash             ::= '#'
	HashHash         ::= '##'
	NewLine          ::= 'nl'


$End


$Globals
/.	
	import java.util.*;
	
	import org.eclipse.cdt.core.dom.ast.*;
	import org.eclipse.cdt.core.dom.c99.IParserActionTokenProvider;
	import org.eclipse.cdt.core.dom.c99.IParser;
	import org.eclipse.cdt.core.dom.c99.IParseResult;
	import org.eclipse.cdt.core.dom.parser.c99.C99ParseResult;
	import org.eclipse.cdt.core.dom.parser.c99.C99ParserAction;
	import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
	import org.eclipse.cdt.core.dom.c99.IKeywordMap;
	import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
	import org.eclipse.cdt.core.dom.c99.IPreprocessorTokenCollector;
./
$End

$Define

    -- These macros allow the header code to be customized by an extending parser.
    
	$ast_class /.Object./
	$ba /.$BeginAction action.beforeConsume(); action. ./
	$ea /.$EndAction./
	$additional_interfaces /. , IParserActionTokenProvider, IParser, IPreprocessorTokenCollector<IToken> ./
	
	$action_class /. C99ParserAction ./
	$keyword_map_class /. C99KeywordMap ./
	$lexer_class /. C99Lexer ./
$End


$Headers
/.
	private $action_class action = new $action_class(this);
	private List commentTokens = new ArrayList();
	private IKeywordMap keywordMap = new $keyword_map_class();
	
	public $action_type() {  // constructor
		this(new $lexer_class());
	}
	
	public void addToken(IToken token) {
		int newKind = mapKind(token.getKind());
		if(newKind == $prs_type.TK_identifier) {
			Integer keywordKind = keywordMap.getKeywordKind(token.toString());
			if(keywordKind != null) {
				newKind = keywordKind.intValue();
			}
		}
		token.setKind(newKind);
		super.addToken(token);
	}
	
	public void addCommentToken(IToken token) {
		commentTokens.add(token);
	}
	
	public List getCommentTokens() {
		return commentTokens;
	}
	
	public void resetTokenStream() {
		super.resetTokenStream();
		action = new $action_class(this);
		commentTokens = new ArrayList();
	}
	
	
	public IParseResult parse() {
		// this has to be done, or... kaboom!
		setStreamLength(getSize());
		
		final int errorRepairCount = -1;  // -1 means full error handling
		
		if(btParser == null) {
			parser(null, errorRepairCount);
		}
		else {
			try
	        {
	        	// reuse the same btParser object for speed 
	        	// (creating an new instance for every translation unit is dirt slow)
	            btParser.parse(errorRepairCount);
	        }
	        catch (BadParseException e)
	        {
	            reset(e.error_token); // point to error token
	            DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
	            diagnoseParser.diagnose(e.error_token);
	        }
		}
	
		IASTTranslationUnit tu      = action.getAST();
		boolean encounteredError    = action.encounteredError();
		IASTCompletionNode compNode = action.getASTCompletionNode();
	
		resetTokenStream(); // important, allows memory to be reclaimed
		return new C99ParseResult(tu, compNode, encounteredError);
	}
	
	
	// implements IParserActionTokenProvider.getEOFToken()
	public IToken getEOFToken() {
		List tokens = getTokens();
		IToken eof = (IToken) tokens.get(tokens.size() - 1);
		return eof;
	}
	
	
	// implements IParserActionTokenProvider.getRuleTokenCount()
	public int getRuleTokenCount() {
		return (getRightSpan() - getLeftSpan()) + 1; 
	}
	
	public List getRuleTokens() {
		return Collections.unmodifiableList(getTokens().subList(getLeftSpan(), getRightSpan() + 1));
	}
	
./
$End


$Start  -- the start symbol
	translation_unit
$End



$Rules

-------------------------------------------------------------------------------------------
-- AST Scoping
--
-- Special empty rule used to trigger the opening of a new AST scope
-------------------------------------------------------------------------------------------


<openscope> ::= $empty  /.$ba  openASTScope();  $ea./
           

-------------------------------------------------------------------------------------------
-- Content assist
-------------------------------------------------------------------------------------------


identifier_or_completion 
    ::= 'identifier'
      | 'Completion'

']' ::=? 'RightBracket'
       | 'EndOfCompletion'
      
')' ::=? 'RightParen'
       | 'EndOfCompletion'
      
'}' ::=? 'RightBrace'
       | 'EndOfCompletion'
      
';' ::=? 'SemiColon'
       | 'EndOfCompletion'


-------------------------------------------------------------------------------------------
-- Expressions
-------------------------------------------------------------------------------------------


constant
    ::= 'integer'                    
          /.$ba  consumeExpressionConstant(IASTLiteralExpression.lk_integer_constant);  $ea./
      | 'floating'                   
          /.$ba  consumeExpressionConstant(IASTLiteralExpression.lk_float_constant);    $ea./
      | 'charconst'                  
          /.$ba  consumeExpressionConstant(IASTLiteralExpression.lk_char_constant);     $ea./
      | 'stringlit'                  
          /.$ba  consumeExpressionConstant(IASTLiteralExpression.lk_string_literal);    $ea./

primary_expression 
    ::= constant 
      | identifier_or_completion                 
          /.$ba  consumeExpressionID();  $ea./
      | '(' expression ')'         
          /.$ba  consumeExpressionBracketed();  $ea./

postfix_expression
    ::= primary_expression
      | postfix_expression '[' expression ']'
          /.$ba  consumeExpressionArraySubscript();  $ea./
      | postfix_expression '(' ')'
          /.$ba  consumeExpressionFunctionCall(false);  $ea./
      | postfix_expression '(' argument_expression_list ')'
          /.$ba  consumeExpressionFunctionCall(true);  $ea./
      | postfix_expression '.' member_name
          /.$ba  consumeExpressionFieldReference(false);  $ea./
      | postfix_expression '->' member_name
          /.$ba  consumeExpressionFieldReference(true);  $ea./
      | postfix_expression '++'
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixIncr);  $ea./
      | postfix_expression '--'
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixDecr);  $ea./
      | '(' type_name ')' '{' <openscope> initializer_list '}'        
          /.$ba  consumeExpressionTypeIdInitializer();  $ea./
      | '(' type_name ')' '{' <openscope> initializer_list ',' '}'    
          /.$ba  consumeExpressionTypeIdInitializer();  $ea./ 


member_name
    ::= identifier_or_completion
      

argument_expression_list
    ::= assignment_expression
         /.$ba  consumeExpressionList(true);  $ea./
      | argument_expression_list ',' assignment_expression
         /.$ba  consumeExpressionList(false); $ea./
      
unary_expression
    ::= postfix_expression
      | '++' unary_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixIncr);  $ea./
      | '--' unary_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixDecr);  $ea./
      | '&' cast_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_amper);  $ea./
      | '*' cast_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_star);   $ea./
      | '+' cast_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_plus);   $ea./
      | '-' cast_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_minus);  $ea./
      | '~' cast_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_tilde);  $ea./
      | '!' cast_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_not);    $ea./
      | 'sizeof' unary_expression
          /.$ba  consumeExpressionUnaryOperator(IASTUnaryExpression.op_sizeof); $ea./
      | 'sizeof' '(' type_name ')'
          /.$ba  consumeExpressionUnarySizeofTypeName();  $ea./  
          
      -- ambiguity here because type_name can be an identifier and unary_expression can be an identifier in brackets
      -- TODO: will need a way of disambiguation, (parse both ways)

cast_expression
    ::= unary_expression
      | '(' type_name ')' cast_expression
          /.$ba  consumeExpressionCast();  $ea./ 


multiplicative_expression
    ::= cast_expression
      | multiplicative_expression '*' cast_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiply); $ea./
      | multiplicative_expression '/' cast_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_divide);   $ea./
      | multiplicative_expression '%' cast_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_modulo);   $ea./

additive_expression
    ::= multiplicative_expression
      | additive_expression '+' multiplicative_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_plus);  $ea./
      | additive_expression '-' multiplicative_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_minus); $ea./

shift_expression
    ::= additive_expression
      | shift_expression '<<' additive_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeft);   $ea./
      | shift_expression '>>' additive_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRight);  $ea./
      
relational_expression
    ::= shift_expression
      | relational_expression '<' shift_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessThan);     $ea./
      | relational_expression '>' shift_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterThan);  $ea./
      | relational_expression '<=' shift_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessEqual);    $ea./
      | relational_expression '>=' shift_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterEqual); $ea./

equality_expression
    ::= relational_expression
      | equality_expression '==' relational_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_equals);  $ea./
      | equality_expression '!=' relational_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_notequals);  $ea./

AND_expression
    ::= equality_expression
      | AND_expression '&' equality_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAnd);  $ea./

exclusive_OR_expression
    ::= AND_expression
      | exclusive_OR_expression '^' AND_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXor);  $ea./

inclusive_OR_expression
    ::= exclusive_OR_expression
      | inclusive_OR_expression '|' exclusive_OR_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOr);  $ea./

logical_AND_expression
    ::= inclusive_OR_expression
      | logical_AND_expression '&&' inclusive_OR_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalAnd);  $ea./

logical_OR_expression
    ::= logical_AND_expression
      | logical_OR_expression '||' logical_AND_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalOr);  $ea./

conditional_expression
    ::= logical_OR_expression
      | logical_OR_expression '?' expression ':' conditional_expression
          /.$ba  consumeExpressionConditional();  $ea./

assignment_expression
    ::= conditional_expression
      | unary_expression '='   assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_assign);  $ea./
      | unary_expression '*='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiplyAssign);  $ea./
      | unary_expression '/='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_divideAssign);  $ea./
      | unary_expression '%='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_moduloAssign);  $ea./
      | unary_expression '+='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_plusAssign);  $ea./
      | unary_expression '-='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_minusAssign);  $ea./
      | unary_expression '<<=' assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeftAssign);  $ea./
      | unary_expression '>>=' assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRightAssign);  $ea./
      | unary_expression '&='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAndAssign);  $ea./
      | unary_expression '^='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXorAssign);  $ea./
      | unary_expression '|='  assignment_expression
          /.$ba  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOrAssign);  $ea./
      
	
-- special rule to avoid confict between problem statments and problem expressions
expression_in_statement
    ::= expression_list
        /.$ba  consumeExpression();  $ea./

expression
    ::= expression_list
        /.$ba  consumeExpression();  $ea./
      --| ERROR_TOKEN
      --   /.$ba  consumeExpressionProblem();  $ea./

expression_list
    ::= assignment_expression
         /.$ba  consumeExpressionList(true);  $ea./
      | expression_list ',' assignment_expression 
         /.$ba  consumeExpressionList(false);  $ea./

constant_expression
    ::= conditional_expression
      --| ERROR_TOKEN
      --   /.$ba  consumeExpressionProblem();  $ea./
      -- I think expressions will have to go at the bottom of the grammar for this to work
      

-------------------------------------------------------------------------------------------
-- Statements
-------------------------------------------------------------------------------------------
      
statement
    ::= labeled_statement
      | compound_statement
      | expression_statement
      | selection_statement
      | iteration_statement
      | jump_statement
      | ERROR_TOKEN
          /.$ba  consumeStatementProblem();  $ea./


labeled_statement
    ::= label_identifier ':' statement
         /.$ba  consumeStatementLabeled();  $ea./
      | 'case' constant_expression ':'
         /.$ba  consumeStatementCase();  $ea./
      | 'default' ':'
         /.$ba  consumeStatementDefault();  $ea./


label_identifier
    ::= 'identifier'
         /.$ba  consumeName();  $ea./
         
         
compound_statement
    ::= '{' '}' 
         /.$ba  consumeStatementEmptyCompoundStatement();  $ea./
      | '{' <openscope> block_item_list '}'
         /.$ba  consumeStatementCompoundStatement();  $ea./
         
         
block_item_list
    ::= block_item
      | block_item_list block_item
      
      
block_item
    ::= statement
      | declaration
         /.$ba  consumeStatementDeclaration();  $ea./
         
         
expression_statement
    ::= ';'
         /.$ba  consumeStatementNull();  $ea./  
      | expression_in_statement ';'
         /.$ba  consumeStatementExpression();  $ea./
         
         
selection_statement
    ::= 'if' '(' expression ')' statement
          /.$ba  consumeStatementIfThen();  $ea./
      | 'if' '(' expression ')' statement 'else' statement
          /.$ba  consumeStatementIfThenElse();  $ea./ 
      | 'switch' '(' expression ')' statement
          /.$ba  consumeStatementSwitch();  $ea./
  

iteration_statement
    ::= 'do' statement 'while' '(' expression ')' ';'
          /.$ba  consumeStatementDoLoop();  $ea./
      | 'while' '(' expression ')' statement
          /.$ba  consumeStatementWhileLoop();  $ea./
      | 'for' '(' expression ';' expression ';' expression ')' statement
          /.$ba  consumeStatementForLoop(true, true, true);  $ea./
      | 'for' '(' expression ';' expression ';'            ')' statement
          /.$ba  consumeStatementForLoop(true, true, false);  $ea./
      | 'for' '(' expression ';'            ';' expression ')' statement
          /.$ba  consumeStatementForLoop(true, false, true);  $ea./
      | 'for' '(' expression ';'            ';'            ')' statement
          /.$ba  consumeStatementForLoop(true, false, false);  $ea./
      | 'for' '('            ';' expression ';' expression ')' statement
          /.$ba  consumeStatementForLoop(false, true, true);  $ea./
      | 'for' '('            ';' expression ';'            ')' statement
          /.$ba  consumeStatementForLoop(false, true, false);  $ea./
      | 'for' '('            ';'            ';' expression ')' statement
          /.$ba  consumeStatementForLoop(false, false, true);  $ea./
      | 'for' '('            ';'            ';'            ')' statement
          /.$ba  consumeStatementForLoop(false, false, false);  $ea./
      | 'for' '(' declaration expression ';' expression ')' statement
          /.$ba  consumeStatementForLoop(true, true, true);  $ea./
      | 'for' '(' declaration expression ';'            ')' statement
          /.$ba  consumeStatementForLoop(true, true, false);  $ea./
      | 'for' '(' declaration            ';' expression ')' statement
          /.$ba  consumeStatementForLoop(true, false, true);  $ea./
      | 'for' '(' declaration            ';'            ')' statement
          /.$ba  consumeStatementForLoop(true, false, false);  $ea./
          

jump_statement
    ::= 'goto' goto_identifier ';'
          /.$ba  consumeStatementGoto();  $ea./
      | 'continue' ';'
          /.$ba  consumeStatementContinue();  $ea./
      | 'break' ';'
          /.$ba  consumeStatementBreak();  $ea./
      | 'return' ';'
          /.$ba  consumeStatementReturn(false);  $ea./
      | 'return' expression ';'
          /.$ba  consumeStatementReturn(true);  $ea./
          
          
goto_identifier
    ::= 'identifier'
          /.$ba  consumeName();  $ea./
    
    
-------------------------------------------------------------------------------------------
-- Declarations
-------------------------------------------------------------------------------------------

declaration 
    ::= declaration_specifiers  ';'
          /.$ba  consumeDeclaration(false);  $ea./
	  | declaration_specifiers <openscope> init_declarator_list ';'
	      /.$ba  consumeDeclaration(true);  $ea./
         

declaration_specifiers
    ::= <openscope> simple_declaration_specifiers
            /.$ba  consumeDeclarationSpecifiersSimple(); $ea./
      | <openscope> struct_or_union_declaration_specifiers
            /.$ba  consumeDeclarationSpecifiersStructUnionEnum(); $ea./
      | <openscope> enum_declaration_specifiers
            /.$ba  consumeDeclarationSpecifiersStructUnionEnum(); $ea./
      | <openscope> typdef_name_declaration_specifiers
            /.$ba  consumeDeclarationSpecifiersTypedefName(); $ea./


no_type_declaration_specifier
    ::= storage_class_specifier
      | type_qualifier
      | function_specifier
    
    
no_type_declaration_specifiers
    ::= no_type_declaration_specifier
      | no_type_declaration_specifiers no_type_declaration_specifier
  
      
simple_declaration_specifiers
    ::= type_specifier
      | no_type_declaration_specifiers type_specifier
      | simple_declaration_specifiers type_specifier
      | simple_declaration_specifiers no_type_declaration_specifier
      
      
struct_or_union_declaration_specifiers
    ::= struct_or_union_specifier
      | no_type_declaration_specifiers struct_or_union_specifier
      | struct_or_union_declaration_specifiers no_type_declaration_specifier
      

enum_declaration_specifiers
    ::= enum_specifier
      | no_type_declaration_specifiers  enum_specifier
      | enum_declaration_specifiers no_type_declaration_specifier


typdef_name_declaration_specifiers
    ::= typedef_name
      | no_type_declaration_specifiers  typedef_name
      | typdef_name_declaration_specifiers no_type_declaration_specifier
      

init_declarator_list
    ::= init_declarator
      | init_declarator_list ',' init_declarator
					  
init_declarator 
    ::= declarator
      | declarator '=' initializer
            /.$ba  consumeDeclaratorWithInitializer();  $ea./


-- at most one storage_class_specifier is allowed in the declaration specifiers
storage_class_specifier 
    ::= 'typedef'   /.$ba  consumeToken();  $ea./
      | 'extern'    /.$ba  consumeToken();  $ea./
      | 'static'    /.$ba  consumeToken();  $ea./
      | 'auto'      /.$ba  consumeToken();  $ea./
      | 'register'  /.$ba  consumeToken();  $ea./		

				
type_specifier
    ::= 'void'        /.$ba  consumeToken();  $ea./
      | 'char'        /.$ba  consumeToken();  $ea./
      | 'short'       /.$ba  consumeToken();  $ea./
      | 'int'         /.$ba  consumeToken();  $ea./
      | 'long'        /.$ba  consumeToken();  $ea./
      | 'float'       /.$ba  consumeToken();  $ea./
      | 'double'      /.$ba  consumeToken();  $ea./
      | 'signed'      /.$ba  consumeToken();  $ea./
      | 'unsigned'    /.$ba  consumeToken();  $ea./
      | '_Bool'       /.$ba  consumeToken();  $ea./
      | '_Complex'    /.$ba  consumeToken();  $ea./
      | '_Imaginary'  /.$ba  consumeToken();  $ea./
      
		
		
typedef_name
    ::= identifier_or_completion   /.$ba  consumeToken();  $ea./
    
    
struct_or_union_specifier
    ::= 'struct' '{' <openscope> struct_declaration_list_opt '}'
           /.$ba  consumeTypeSpecifierComposite(false, IASTCompositeTypeSpecifier.k_struct);  $ea./
           
      | 'union' '{' <openscope> struct_declaration_list_opt '}'
           /.$ba  consumeTypeSpecifierComposite(false, IASTCompositeTypeSpecifier.k_union);  $ea./
           
      | 'struct' struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
           /.$ba  consumeTypeSpecifierComposite(true, IASTCompositeTypeSpecifier.k_struct);  $ea./
           
      | 'union'  struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
           /.$ba  consumeTypeSpecifierComposite(true, IASTCompositeTypeSpecifier.k_union);  $ea./
           
      | 'struct' struct_or_union_identifier
           /.$ba  consumeTypeSpecifierElaborated(IASTElaboratedTypeSpecifier.k_struct);  $ea./
           
      | 'union'  struct_or_union_identifier
           /.$ba  consumeTypeSpecifierElaborated(IASTElaboratedTypeSpecifier.k_union);  $ea./


struct_or_union_identifier
    ::= 'identifier'
            /.$ba  consumeName();  $ea./

struct_declaration_list_opt
    ::= struct_declaration_list
      | $empty

struct_declaration_list
    ::= struct_declaration
      | struct_declaration_list struct_declaration
      

struct_declaration
    ::= specifier_qualifier_list <openscope> struct_declarator_list ';' -- regular declarators plus bit fields
            /.$ba  consumeStructDeclaration(true);  $ea./  -- TODO is it okay to reuse consumeDeclaration() ?
      | specifier_qualifier_list ';'
            /.$ba  consumeStructDeclaration(false);  $ea./


-- just reuse declaration_specifiers, makes grammar a bit more lenient but thats ok
specifier_qualifier_list
    ::= declaration_specifiers
           

struct_declarator_list
    ::= struct_declarator
      | struct_declarator_list ',' struct_declarator

struct_declarator
    ::= declarator
      | ':' constant_expression  
		/.$ba  consumeStructBitField(false);  $ea./
      | declarator ':' constant_expression		
        /.$ba  consumeStructBitField(true);   $ea./
		
		
enum_identifier
    ::= 'identifier'  /.$ba  consumeName();  $ea./
            
            
enum_specifier
    ::= 'enum' '{' <openscope> enumerator_list_opt '}'
           /.$ba  consumeTypeSpecifierEnumeration(false);  $ea./
           
      | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
           /.$ba  consumeTypeSpecifierEnumeration(true);  $ea./
           
      | 'enum' '{' <openscope> enumerator_list_opt ',' '}'
           /.$ba  consumeTypeSpecifierEnumeration(false);  $ea./
           
      | 'enum' enum_identifier '{' <openscope> enumerator_list_opt ',' '}'
           /.$ba  consumeTypeSpecifierEnumeration(true);  $ea./
           
      | 'enum' enum_identifier
           /.$ba  consumeTypeSpecifierElaborated(IASTElaboratedTypeSpecifier.k_enum);  $ea./
      
      
enumerator_list_opt
    ::= enumerator_list
      | $empty
  
enumerator_list
    ::= enumerator
      | enumerator_list ',' enumerator
      
      
enumerator
    ::= enum_identifier
           /.$ba  consumeEnumerator(false);  $ea./
      | enum_identifier '=' constant_expression
           /.$ba  consumeEnumerator(true);  $ea./
      
      
type_qualifier
    ::= 'const'      /.$ba  consumeToken();  $ea./
      | 'restrict'   /.$ba  consumeToken();  $ea./
      | 'volatile'   /.$ba  consumeToken();  $ea./

function_specifier
    ::= 'inline'     /.$ba  consumeToken();  $ea./
    

declarator
    ::= direct_declarator
      | <openscope> pointer direct_declarator
         /.$ba  consumeDeclaratorWithPointer(true);  $ea./


direct_declarator
    ::= 'identifier' 
         /.$ba  consumeDirectDeclaratorIdentifier();  $ea./
         
      | '(' declarator ')'
         /.$ba  consumeDirectDeclaratorBracketed();  $ea./

      | array_direct_declarator       
        
      | function_direct_declarator



array_direct_declarator
    ::= direct_declarator array_modifier
          /.$ba  consumeDirectDeclaratorArrayDeclarator();  $ea./



function_direct_declarator
    ::= direct_declarator '(' <openscope> parameter_type_list ')'
         /.$ba  consumeDirectDeclaratorFunctionDeclarator(true);  $ea./
      
      | direct_declarator '(' ')'
         /.$ba  consumeDirectDeclaratorFunctionDeclarator(false);  $ea./


function_declarator
    ::= function_direct_declarator
      | <openscope> pointer function_direct_declarator
          /.$ba  consumeDeclaratorWithPointer(true);  $ea./


-- This is a hack because the parser cannot tell the difference between 
-- plain identifiers and types. Because of this an identifier_list would
-- always be parsed as a parameter_type_list instead. In a KnR funciton
-- definition we can use the extra list of declarators to disambiguate.
-- This rule should be merged back into direct_declarator if type info is
-- added to the parser. 
knr_direct_declarator 
    ::= direct_declarator '(' <openscope> identifier_list ')'
         /.$ba  consumeDirectDeclaratorFunctionDeclaratorKnR();  $ea./


knr_function_declarator
    ::= knr_direct_declarator
      | <openscope> pointer knr_direct_declarator
         /.$ba  consumeDeclaratorWithPointer(true);  $ea./
                  

array_modifier 
    ::= '[' ']'
         /.$ba  consumeDirectDeclaratorArrayModifier(false);  $ea./
         
      | '[' <openscope> type_qualifier_list ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(false, false, true,  false);  $ea./
       
      | '[' assignment_expression ']'
         /.$ba  consumeDirectDeclaratorArrayModifier(true );  $ea./
         
      | '[' <openscope> type_qualifier_list assignment_expression ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(false, false, true,  true );  $ea./
         
      | '[' 'static' assignment_expression ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(true,  false, false, true );  $ea./
         
      | '[' 'static' <openscope> type_qualifier_list assignment_expression ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(true,  false, true,  true );  $ea./
      
      | '[' <openscope> type_qualifier_list 'static' assignment_expression ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(true,  false, true,  true );  $ea./
      
      | '[' '*' ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(false, true,  false, false);  $ea./

      | '[' <openscope> type_qualifier_list '*' ']'
         /.$ba  consumeDirectDeclaratorModifiedArrayModifier(false, true,  true,  false);  $ea./
         
         
pointer
    ::= '*'
        /.$ba  consumePointer();  $ea./
      | pointer '*' 
        /.$ba  consumePointer();  $ea./
      | '*' <openscope> type_qualifier_list
        /.$ba  consumePointerTypeQualifierList();  $ea./
      | pointer '*' <openscope> type_qualifier_list
        /.$ba  consumePointerTypeQualifierList();  $ea./

type_qualifier_list
    ::= type_qualifier
      | type_qualifier_list type_qualifier

parameter_type_list
    ::= parameter_list
      | parameter_list ',' '...'
      | '...'  -- not spec

parameter_list
    ::= parameter_declaration
      | parameter_list ',' parameter_declaration

parameter_declaration
    ::= declaration_specifiers init_declarator
          /.$ba  consumeParameterDeclaration(true);  $ea./
      | declaration_specifiers 
          /.$ba  consumeParameterDeclaration(false); $ea./
      | declaration_specifiers abstract_declarator
          /.$ba  consumeParameterDeclaration(true);  $ea./
          
          
identifier_list
    ::= 'identifier'
           /.$ba  consumeName();  $ea./
      | identifier_list ',' 'identifier'
           /.$ba  consumeName();  $ea./

type_name
    ::= specifier_qualifier_list
          /.$ba  consumeTypeId(false);  $ea./
      | specifier_qualifier_list abstract_declarator
          /.$ba  consumeTypeId(true);  $ea./


abstract_declarator  -- a declarator that does not include an identifier
    ::= <openscope> pointer
          /.$ba  consumeDeclaratorWithPointer(false);  $ea./
      | direct_abstract_declarator
      | <openscope> pointer direct_abstract_declarator
          /.$ba  consumeDeclaratorWithPointer(true);   $ea./


-- rewritten to use the more general array_modifier rule
direct_abstract_declarator
    ::= '(' abstract_declarator ')'
          /.$ba  consumeDirectDeclaratorBracketed();  $ea./
      
      | array_modifier
          /.$ba  consumeAbstractDeclaratorArrayModifier(false);  $ea./
          
      | direct_abstract_declarator array_modifier
          /.$ba  consumeAbstractDeclaratorArrayModifier(true);   $ea./
      
      | '(' ')'
          /.$ba  consumeAbstractDeclaratorFunctionDeclarator(false, false); $ea./
          
      | direct_abstract_declarator '(' ')'
          /.$ba  consumeAbstractDeclaratorFunctionDeclarator(true, false);  $ea./
          
      | '(' <openscope> parameter_type_list ')'
          /.$ba  consumeAbstractDeclaratorFunctionDeclarator(false, true);  $ea./
      
      | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
          /.$ba  consumeAbstractDeclaratorFunctionDeclarator(true, true);   $ea./


initializer
    ::= assignment_expression
            /.$ba  consumeInitializer();  $ea./
      | '{' <openscope> initializer_list '}'
            /.$ba  consumeInitializerList();  $ea./
      | '{' <openscope> initializer_list ',' '}'
            /.$ba  consumeInitializerList();  $ea./


initializer_list
    ::= initializer
      | designated_initializer
      | initializer_list ',' initializer
      | initializer_list ',' designated_initializer
            


designated_initializer
    ::= <openscope> designation initializer
            /.$ba  consumeInitializerDesignated();  $ea./

designation
    ::= designator_list '='

designator_list
    ::= designator
      | designator_list designator

designator
    ::= '[' constant_expression ']'
           /.$ba  consumeDesignatorArrayDesignator();  $ea./
      | '.' 'identifier'		
           /.$ba  consumeDesignatorFieldDesignator();  $ea./
		
		
		

      
      


-------------------------------------------------------------------------------------------
-- External Definitions
-------------------------------------------------------------------------------------------

translation_unit
    ::= external_declaration_list
         /.$ba  consumeTranslationUnit();  $ea./
      | $empty
         /.$ba  consumeTranslationUnit();  $ea./

external_declaration_list
    ::= external_declaration
      | external_declaration_list external_declaration

external_declaration
    ::= function_definition
      | declaration
      | ';'
          /.$ba  consumeDeclarationEmpty(); $ea./
      | ERROR_TOKEN
	      /.$ba  consumeDeclarationProblem();  $ea./

-- The extra <openscope> nonterminal before declarator in this rule is only there
-- to avoid a shift/reduce error with the rule for declaration. 
function_definition
    ::= declaration_specifiers <openscope> function_declarator compound_statement
         /.$ba  consumeFunctionDefinition(true);  $ea./
      | declaration_specifiers <openscope> knr_function_declarator <openscope> declaration_list compound_statement
         /.$ba  consumeFunctionDefinitionKnR();  $ea./
         
   -- this rule is here as a special case just to support implicit int in function definitions
      | function_declarator compound_statement
         /.$ba  consumeFunctionDefinition(false);  $ea./

    
declaration_list
    ::= declaration
      | declaration_list declaration

$End
















