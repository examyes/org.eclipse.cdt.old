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

-----------------------------------------------------------------------------------
-- Grammar file for preprocessor #if and #elif conditional expressions.
--
-- When the LPG generator is run on this file it must then be run on C99Parser.g
-- or the parser will not work properly.
-----------------------------------------------------------------------------------


%options la=1
%options package=org.eclipse.cdt.internal.core.dom.parser.c99
%options template=dtParserTemplateD.g


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
 *******************************************************************************/
 
  // This file was generated by LPG
./
$End


$Terminals

RightParen   ::= ')'
LeftParen    ::= '('
And          ::= '&'
Star         ::= '*'
Plus         ::= '+'
Minus        ::= '-'
Tilde        ::= '~'
Bang         ::= '!'
Slash        ::= '/'
Percent      ::= '%'
RightShift   ::= '>>'
LeftShift    ::= '<<'
LT           ::= '<'
GT           ::= '>'
LE           ::= '<='
GE           ::= '>='
EQ           ::= '=='
NE           ::= '!='
Caret        ::= '^'
Or           ::= '|'
AndAnd       ::= '&&'
OrOr         ::= '||'
Question     ::= '?'
Colon        ::= ':'
integer  
charconst  
identifier

$End

$Globals
/.	
	import java.util.*;
	import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
	import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.*;
	import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;
	import org.eclipse.cdt.core.dom.parser.c99.PPToken;
./
$End	

$Define
	$ast_class /.Object./
	$ba /.$BeginAction action. ./
	$ea /.$EndAction./
$End


$Headers
/.
	private C99ExprEvaluatorAction action = new C99ExprEvaluatorAction(this);
	
	public C99ExprEvaluator(TokenList tokens, final IPPTokenComparator comparator) {
		// hack, prevents NPEs from superclass, only needed by DiagnoseParser
		super(new C99Lexer());
		
		addToken((IToken)Token.DUMMY_TOKEN);
		
		for(Object t : tokens) {
		
			PPToken kind = comparator.getKind(t);
			if(kind == null) {
				throw new RuntimeException("The expression evaluator doesn't support this token: " + t); //$NON-NLS-1$
			}
			
			IToken token = new SynthesizedToken(comparator.getStartOffset(t), comparator.getEndOffset(t), 0, t.toString());
			
			switch(kind) {  
				case INTEGER: token.setKind(C99ExprEvaluatorsym.TK_integer); break;  
				case CHARCONST: token.setKind(C99ExprEvaluatorsym.TK_charconst); break;            
				case LPAREN: token.setKind(C99ExprEvaluatorsym.TK_LeftParen); break;                
				case IDENT: token.setKind(C99ExprEvaluatorsym.TK_identifier); break;                              
				case RPAREN: token.setKind(C99ExprEvaluatorsym.TK_RightParen); break;                
				case AND: token.setKind(C99ExprEvaluatorsym.TK_And); break;   
				case STAR: token.setKind(C99ExprEvaluatorsym.TK_Star); break;   
				case PLUS: token.setKind(C99ExprEvaluatorsym.TK_Plus); break;   
				case MINUS: token.setKind(C99ExprEvaluatorsym.TK_Minus); break;   
				case TILDE: token.setKind(C99ExprEvaluatorsym.TK_Tilde); break;   
				case BANG: token.setKind(C99ExprEvaluatorsym.TK_Bang); break;   
				case SLASH: token.setKind(C99ExprEvaluatorsym.TK_Slash); break;   
				case PERCENT: token.setKind(C99ExprEvaluatorsym.TK_Percent); break;   
				case RIGHTSHIFT: token.setKind(C99ExprEvaluatorsym.TK_RightShift); break;   
				case LEFTSHIFT: token.setKind(C99ExprEvaluatorsym.TK_LeftShift); break;   
				case LT: token.setKind(C99ExprEvaluatorsym.TK_LT); break;   
				case GT: token.setKind(C99ExprEvaluatorsym.TK_GT); break;   
				case LE: token.setKind(C99ExprEvaluatorsym.TK_LE); break;   
				case GE: token.setKind(C99ExprEvaluatorsym.TK_GE); break;   
				case EQ: token.setKind(C99ExprEvaluatorsym.TK_EQ); break;   
				case NE: token.setKind(C99ExprEvaluatorsym.TK_NE); break;   
				case CARET: token.setKind(C99ExprEvaluatorsym.TK_Caret); break;   
				case OR: token.setKind(C99ExprEvaluatorsym.TK_Or); break;   
				case ANDAND: token.setKind(C99ExprEvaluatorsym.TK_AndAnd); break;   
				case OROR: token.setKind(C99ExprEvaluatorsym.TK_OrOr); break;   
				case QUESTION: token.setKind(C99ExprEvaluatorsym.TK_Question); break;   
				case COLON: token.setKind(C99ExprEvaluatorsym.TK_Colon); break;   
			}
			
			addToken(token);
		}
		
		IToken eof = new SynthesizedToken(0, 0, C99ExprEvaluatorsym.TK_EOF_TOKEN, "");
		eof.setKind(mapKind(eof.getKind()));
		addToken(eof);
		setStreamLength(getSize());
	}

	
	public void addToken(IToken token) {
		ArrayList tokens = getTokens();
		ArrayList adjuncts = getAdjuncts();
		tokens.add(token);
		token.setTokenIndex(tokens.size());
        token.setAdjunctIndex(adjuncts.size());
	}
	
	public Long evaluate() {
		parser(null, -1);
		return action.result();	
	}
./
$End


$Start
	constant_expression
$End

$Rules
     

-------------------------------------------------------------------------------------------
-- Expressions
-------------------------------------------------------------------------------------------


constant
    ::= 'integer'                    
          /.$ba  evalExpressionConstantInteger();  $ea./
      | 'charconst'                  
          /.$ba  evalExpressionConstantChar();     $ea./

primary_expression 
    ::= constant 
      | 'identifier'                 
          /.$ba  evalExpressionID();  $ea./
      | '(' constant_expression ')'         
      
      
unary_expression
    ::= primary_expression
      | '+' unary_expression
          /.$ba  evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_plus);   $ea./
      | '-' unary_expression
          /.$ba  evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_minus);  $ea./
      | '~' unary_expression
          /.$ba  evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_tilde);  $ea./
      | '!' unary_expression
          /.$ba  evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_not);    $ea./
 
multiplicative_expression
    ::= unary_expression
      | multiplicative_expression '*' unary_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_multiply); $ea./
      | multiplicative_expression '/' unary_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_divide);   $ea./
      | multiplicative_expression '%' unary_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_modulo);   $ea./

additive_expression
    ::= multiplicative_expression
      | additive_expression '+' multiplicative_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_plus);  $ea./
      | additive_expression '-' multiplicative_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_minus); $ea./

shift_expression
    ::= additive_expression
      | shift_expression '<<' additive_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_shiftLeft);   $ea./
      | shift_expression '>>' additive_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_shiftRight);  $ea./
      
relational_expression
    ::= shift_expression
      | relational_expression '<' shift_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_lessThan);     $ea./
      | relational_expression '>' shift_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_greaterThan);  $ea./
      | relational_expression '<=' shift_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_lessEqual);    $ea./
      | relational_expression '>=' shift_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_greaterEqual); $ea./

equality_expression
    ::= relational_expression
      | equality_expression '==' relational_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_equals);  $ea./
      | equality_expression '!=' relational_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_notequals);  $ea./

AND_expression
    ::= equality_expression
      | AND_expression '&' equality_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_binaryAnd);  $ea./

exclusive_OR_expression
    ::= AND_expression
      | exclusive_OR_expression '^' AND_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_binaryXor);  $ea./

inclusive_OR_expression
    ::= exclusive_OR_expression
      | inclusive_OR_expression '|' exclusive_OR_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_binaryOr);  $ea./

logical_AND_expression
    ::= inclusive_OR_expression
      | logical_AND_expression '&&' inclusive_OR_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_logicalAnd);  $ea./

logical_OR_expression
    ::= logical_AND_expression
      | logical_OR_expression '||' logical_AND_expression
          /.$ba  evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_logicalOr);  $ea./

conditional_expression
    ::= logical_OR_expression
      | logical_OR_expression '?' constant_expression ':' conditional_expression
          /.$ba  evalExpressionConditional();  $ea./

constant_expression
    ::= conditional_expression

$End

