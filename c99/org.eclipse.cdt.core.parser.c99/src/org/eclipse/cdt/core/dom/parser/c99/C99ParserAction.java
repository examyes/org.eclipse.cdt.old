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

package org.eclipse.cdt.core.dom.parser.c99;

import java.util.Iterator;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.c99.IASTNodeFactory;
import org.eclipse.cdt.core.dom.c99.IParserActionTokenProvider;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.c99.ParserAction;
import org.eclipse.cdt.internal.core.dom.parser.c99.TokenMap;


/**
 * Semantic actions called by the parser to build an AST.
 */
public class C99ParserAction extends ParserAction implements IParserAction {

	
	private ITokenMap tokenMap = null;
	private boolean useDisambiguationHacks = true;
	
	
	public C99ParserAction(IParserActionTokenProvider parser) {
		super(parser);
	}

	public void setUseDisambiguationHacks(boolean use) {
		this.useDisambiguationHacks = use;
	}
	
	
	public void setTokenMap(String[] orderedTerminalSymbols) {
		this.tokenMap = new TokenMap(C99Parsersym.orderedTerminalSymbols, orderedTerminalSymbols);
	}
	
	
	private int asC99Kind(IToken token) {
		return asC99Kind(token.getKind());
	}
	
	private int asC99Kind(int tokenKind) {
		return tokenMap == null ? tokenKind : tokenMap.mapKind(tokenKind);
	}
	

	/**
	 * Overrideable by subclasses to provide a different implementation of the node factory.
	 */
	protected IASTNodeFactory createNodeFactory() {
		return new C99ASTNodeFactory();
	}

	
	/**
	 * Used to conveniently pattern match a list of tokens based on their kind. 
	 * 
	 * example) match: x * x
	 * 
	 * matchKinds(tokens, new int[] {TK_identifier, TK_Star, TK_identifier});
	 * 
	 */
	protected boolean matchKinds(List tokens, int[] kinds) {
		if(tokens.size() != kinds.length)
			return false;
		
		for(int i = 0; i < kinds.length; i++) {
			int kind = asC99Kind((IToken)tokens.get(i));
			if(kinds[i] == C99Parsersym.TK_identifier && 
			   (kind == C99Parsersym.TK_identifier || kind == C99Parsersym.TK_Completion)) {
				continue;
			}
			if(kind != kinds[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Consumes a name from an identifier.
	 * Used by several grammar rules.
	 */
	public void consumeName() {
		IASTName name = createName( parser.getRightIToken() );
		astStack.push(name);
	}
	
	
	/**
	 * Creates a IASTName node from an identifier token.
	 * 
	 * If the token is a completion token then a completion node 
	 */
	public IASTName createName(IToken token) {
		IASTName name = nodeFactory.newName(token.toString().toCharArray());
		setOffsetAndLength(name, token); 
		
		if(asC99Kind(token) == C99Parsersym.TK_Completion) {
			super.addNameToCompletionNode(name, token.toString());
		}
		
		return name;
	}
	
	/********************************************************************
	 * Start of semantic actions.
	 ********************************************************************/

	
	/**
	 * constant ::= 'integer' | 'floating' | 'charconst' | 'stringlit'
	 * 
	 * @param kind One of the kind flags from IASTLiteralExpression
	 * @see IASTLiteralExpression
	 */
	public void consumeExpressionConstant(int kind) {
		IASTLiteralExpression expr = nodeFactory.newLiteralExpression();
		IToken token = parser.getRightIToken();
		
		String rep = token.toString();
		
		// Strip the quotes from string literals, this is just to be consistent
		// with the dom parser (i.e. to make a test pass).
		if(kind == IASTLiteralExpression.lk_string_literal && 
				rep.startsWith("\"") && rep.endsWith("\"")) {
			rep = rep.substring(1, rep.length()-1);			
		}
		
		expr.setKind(kind);
		expr.setValue(rep);
		setOffsetAndLength(expr, token);
		astStack.push(expr);
	}
	
	
	/**
	 * primary_expression ::= ident
	 */
	public void consumeExpressionID() {
		IASTIdExpression expr = nodeFactory.newIdExpression();
		IASTName name = createName(parser.getRightIToken());
		
		expr.setName(name);
		name.setParent(expr);
		name.setPropertyInParent(IASTIdExpression.ID_NAME);
        setOffsetAndLength(expr);
        astStack.push(expr);
	}
	
	
	/**
	 * multiplicative_expression ::= multiplicative_expression '*' cast_expression
	 * multiplicative_expression ::= multiplicative_expression '/' cast_expression
	 * multiplicative_expression ::= multiplicative_expression '%' cast_expression
	 * 
	 * additive_expression ::= additive_expression '+' multiplicative_expression
	 * additive_expression ::= additive_expression '_' multiplicative_expression
	 * 
	 * shift_expression ::= shift_expression '<<' additive_expression
	 * shift_expression ::= shift_expression '>>' additive_expression
	 * 
	 * relational_expression ::= relational_expression '<' shift_expression
	 * relational_expression ::= relational_expression '>' shift_expression
	 * relational_expression ::= relational_expression '<=' shift_expression
	 * relational_expression ::= relational_expression '>=' shift_expression
	 * 
	 * equality_expression ::= equality_expression '==' relational_expression
	 * equality_expression ::= equality_expression '!=' relational_expression
	 * 
	 * AND_expression ::= AND_expression '&' equality_expression
	 * 
	 * exclusive_OR_expression ::= exclusive_OR_expression '^' AND_expression
	 * 
	 * inclusive_OR_expression ::= inclusive_OR_expression '|' exclusive_OR_expression
	 * 
	 * logical_AND_expression ::= logical_AND_expression '&&' inclusive_OR_expression
	 * 
	 * logical_OR_expression ::= logical_OR_expression '||' logical_AND_expression
	 * 
	 * assignment_expression ::= unary_expression '='   assignment_expression
	 * assignment_expression ::= unary_expression '*='  assignment_expression
	 * assignment_expression ::= unary_expression '/='  assignment_expression
	 * assignment_expression ::= unary_expression '%='  assignment_expression
	 * assignment_expression ::= unary_expression '+='  assignment_expression
	 * assignment_expression ::= unary_expression '_='  assignment_expression
	 * assignment_expression ::= unary_expression '<<=' assignment_expression
	 * assignment_expression ::= unary_expression '>>=' assignment_expression
	 * assignment_expression ::= unary_expression '&='  assignment_expression
	 * assignment_expression ::= unary_expression '^='  assignment_expression
	 * assignment_expression ::= unary_expression '|='  assignment_expression
	 * 
	 * 
	 * @param op Field from IASTBinaryExpression
	 */
	public void consumeExpressionBinaryOperator(int op) {
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		
		IASTBinaryExpression binExpr = nodeFactory.newBinaryExpression();
		binExpr.setOperator(op);
		
		binExpr.setOperand1(expr1);
		expr1.setParent(binExpr);
		expr1.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
		
		binExpr.setOperand2(expr2);
		expr2.setParent(binExpr);
		expr2.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
		
		setOffsetAndLength(binExpr);
		astStack.push(binExpr);
	}
	
	
	/**
	 * conditional_expression ::= logical_OR_expression '?' expression ':' conditional_expression
	 */
	public void consumeExpressionConditional() {
		IASTExpression expr3 = (IASTExpression) astStack.pop();
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		
		IASTConditionalExpression condExpr = nodeFactory.newConditionalExpession();
		
		condExpr.setLogicalConditionExpression(expr1);
		expr1.setParent(condExpr);
		expr1.setPropertyInParent(IASTConditionalExpression.LOGICAL_CONDITION);
		
		condExpr.setPositiveResultExpression(expr2);
		expr2.setParent(condExpr);
		expr2.setPropertyInParent(IASTConditionalExpression.POSITIVE_RESULT);
		
		condExpr.setNegativeResultExpression(expr3);
		expr3.setParent(condExpr);
		expr3.setPropertyInParent(IASTConditionalExpression.NEGATIVE_RESULT);
		
		setOffsetAndLength(condExpr);
		astStack.push(condExpr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '[' expression ']'
	 */
	public void consumeExpressionArraySubscript() {
		IASTArraySubscriptExpression expr = nodeFactory.newArraySubscriptExpression();
		
		IASTExpression subscript = (IASTExpression) astStack.pop();
		IASTExpression arrayExpr = (IASTExpression) astStack.pop();
		
		expr.setArrayExpression(arrayExpr);
		arrayExpr.setParent(expr);
		arrayExpr.setPropertyInParent(IASTArraySubscriptExpression.ARRAY);
		
		expr.setSubscriptExpression(subscript);
		subscript.setParent(expr);
		arrayExpr.setPropertyInParent(IASTArraySubscriptExpression.SUBSCRIPT);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '(' argument_expression_list ')'
	 * postfix_expression ::= postfix_expression '(' ')'
	 */
	public void consumeExpressionFunctionCall(boolean hasArgs) {
		IASTFunctionCallExpression expr = nodeFactory.newFunctionCallExpression();
		
		if(hasArgs) {
			IASTExpressionList argList = (IASTExpressionList) astStack.pop();
			expr.setParameterExpression(argList);
			argList.setParent(expr);
			argList.setPropertyInParent(IASTFunctionCallExpression.PARAMETERS);
		}
		
		IASTExpression idExpr  = (IASTExpression) astStack.pop();
		expr.setFunctionNameExpression(idExpr);
		idExpr.setParent(expr);
		idExpr.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '.' ident
	 * postfix_expression ::= postfix_expression '->' ident
	 */
	public void consumeExpressionFieldReference(boolean isPointerDereference) {
		IASTExpression idExpression = (IASTExpression) astStack.pop();
		
		IASTFieldReference expr = nodeFactory.newFieldReference();
		IASTName name = createName(parser.getRightIToken());
		expr.setIsPointerDereference(isPointerDereference);
		
		expr.setFieldName(name);
		name.setParent(expr);
		name.setPropertyInParent(IASTFieldReference.FIELD_NAME);
		
		expr.setFieldOwner(idExpression);
		idExpression.setParent(expr);
		idExpression.setPropertyInParent(IASTFieldReference.FIELD_OWNER);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '++'
	 * postfix_expression ::= postfix_expression '__'
	 * 
	 * unary_expression ::= '++' unary_expression
	 * unary_expression ::= '__' unary_expression
	 * unary_expression ::= '&' cast_expression
	 * unary_expression ::= '*' cast_expression
	 * unary_expression ::= '+' cast_expression
	 * unary_expression ::= '_' cast_expression
	 * unary_expression ::= '~' cast_expression
	 * unary_expression ::= '!' cast_expression
	 * unary_expression ::= 'sizeof' unary_expression
	 * 
	 * @param operator From IASTUnaryExpression
	 */
	public void consumeExpressionUnaryOperator(int operator) {
		IASTExpression operand = (IASTExpression) astStack.pop();
		
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression();
		expr.setOperator(operator);
		
		expr.setOperand(operand);
		operand.setParent(expr);
		operand.setPropertyInParent(IASTUnaryExpression.OPERAND);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * unary_operation ::= 'sizeof' '(' type_name ')'
	 * 
	 * @see consumeExpressionUnaryOperator For the other use of sizeof
	 */
	public void consumeExpressionUnarySizeofTypeName() {
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTTypeIdExpression expr = nodeFactory.newTypeIdExpression();
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(IASTTypeIdExpression.TYPE_ID);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		// the following code disambiguates a special case
		// sizeof(x);
		// in this case x can be parsed as a type_name or as an id expression
		disambiguateHackSizofTypeName();
	}
	
	
	/**
	 * This hack recognizes an ambiguous case with 'sizeof' expressions
	 * and generates an ambiguity node in the AST. This ambiguity node
	 * is essential for content assist to work properly.
	 * 
	 * Expects the AST subtree for the first way the sizeof expression was 
	 * parsed to be on the top of the astStack.
	 */
	private void disambiguateHackSizofTypeName() {
		if(!useDisambiguationHacks)
			return;
		
		List<IToken> tokens = parser.getRuleTokens();
		if(tokens.size() != 4) 
			return;
			
		IToken typeName = tokens.get(2);
		int kind = asC99Kind(typeName);
		if(kind != C99Parsersym.TK_identifier && kind != C99Parsersym.TK_Completion) // and its an identifier, we have an ambiguity
			return;
		
		IASTName name = createName(typeName);
		IASTIdExpression idExpr = nodeFactory.newIdExpression();
		idExpr.setName(name);
		name.setParent(idExpr);
		name.setPropertyInParent(IASTIdExpression.ID_NAME);
		
		IASTUnaryExpression secondExpr = nodeFactory.newUnaryExpression();
		secondExpr.setOperator(IASTUnaryExpression.op_sizeof);
		
		secondExpr.setOperand(idExpr);
		idExpr.setParent(secondExpr);
		idExpr.setPropertyInParent(IASTUnaryExpression.OPERAND);
		
		IASTAmbiguousExpression ambExpr = nodeFactory.newAmbiguousExpression();
		
		// the AST subtree representing the first way to parse the expression
		// was pushed onto the stack by consumeExpressionUnarySizeofTypeName()
		IASTExpression firstExpr = (IASTExpression) astStack.pop();
		
		ambExpr.addExpression(firstExpr);
		firstExpr.setParent(ambExpr);
		firstExpr.setPropertyInParent(IASTAmbiguousExpression.SUBEXPRESSION);
		
        ambExpr.addExpression(secondExpr);
        secondExpr.setParent(ambExpr);
        secondExpr.setPropertyInParent(IASTAmbiguousExpression.SUBEXPRESSION);
        
        astStack.push(ambExpr);
	}
	
	
	
	/**
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list '}'
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list ',' '}'            
	 */
	public void consumeExpressionTypeIdInitializer() {
		consumeInitializerList(); // closes the scope
		
		IASTInitializerList list = (IASTInitializerList) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		ICASTTypeIdInitializerExpression expr = nodeFactory.newCTypeIdInitializerExpression();
		
		expr.setInitializer(list);
		list.setParent(expr);
		list.setPropertyInParent(ICASTTypeIdInitializerExpression.INITIALIZER);
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(ICASTTypeIdInitializerExpression.TYPE_ID);
	
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * cast_expression ::= '(' type_name ')' cast_expression
	 */
	public void consumeExpressionCast() {
		IASTCastExpression expr = nodeFactory.newCastExpression();
		expr.setOperator(IASTCastExpression.op_cast);
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		
		if (disambiguateHackCastExpression(typeId, operand)) {
			return;
		}
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(IASTCastExpression.TYPE_ID);
		
		expr.setOperand(operand);
		operand.setParent(expr);
		operand.setPropertyInParent(IASTCastExpression.OPERAND);
		
		setOffsetAndLength(expr);
		
		astStack.push(expr);
	}
	
	/**
	 * A hack to disambiguate binary expressions where the first operand is
	 * a bracketed identifier.  Without this hack, such expressions would be
	 * parsed as cast expressions where the operand is a unary expression.
	 * For example:
	 * 
	 *   int z = 0;
	 *   z = (a) + z;
	 *   z = (a) - z;
	 *   z = (a) * z;
	 *   z = (a) & z;
	 *   
	 * This hack fixes the problems reported in bugs 100408, 168924 and 192693.
	 */
	private boolean disambiguateHackCastExpression(IASTTypeId typeId, IASTExpression operand) {
		if(!useDisambiguationHacks)
			return false;
		
		if (operand instanceof IASTUnaryExpression) {
			IASTUnaryExpression unaryExpression = (IASTUnaryExpression) operand;
			IASTExpression unaryOperand = unaryExpression.getOperand();
			
			List<IToken> ruleTokens = parser.getRuleTokens();
			IToken openParen  = ruleTokens.get(0);
			IToken ident      = ruleTokens.get(1);
			IToken closeParen = ruleTokens.get(2);
			
			if(asC99Kind(openParen) != C99Parsersym.TK_LeftParen || 
			   asC99Kind(ident) != C99Parsersym.TK_identifier || 
			   asC99Kind(closeParen) != C99Parsersym.TK_RightParen) {
						return false;
			}
			
			IToken operator = ruleTokens.get(3);
			int binaryOperator;
			switch (asC99Kind(operator)) {
			case C99Parsersym.TK_Plus:
				binaryOperator = IASTBinaryExpression.op_plus;
				break;
			case C99Parsersym.TK_Minus:
				binaryOperator = IASTBinaryExpression.op_minus;
				break;
			case C99Parsersym.TK_And:
				binaryOperator = IASTBinaryExpression.op_binaryAnd;
				break;
			case C99Parsersym.TK_Star:
				binaryOperator = IASTBinaryExpression.op_multiply;
				break;
			default:
				return false;
			}
			
			IASTDeclSpecifier declSpecifier = typeId.getDeclSpecifier();
			if (!(declSpecifier instanceof IASTNamedTypeSpecifier)) {
				return false;
			}
			
			IASTIdExpression idExpression = nodeFactory.newIdExpression();
			IASTName name = ((IASTNamedTypeSpecifier) declSpecifier).getName();
			idExpression.setName(name);
			name.setParent(idExpression);
			name.setPropertyInParent(IASTIdExpression.ID_NAME);
			
			IASTUnaryExpression operand1 = nodeFactory.newUnaryExpression();
			operand1.setOperator(IASTUnaryExpression.op_bracketedPrimary);
			operand1.setOperand(idExpression);
			idExpression.setParent(operand1);
			idExpression.setPropertyInParent(IASTUnaryExpression.OPERAND);
			setOffsetAndLength(idExpression, offset(typeId), length(typeId));
			
			IASTBinaryExpression binaryExpression = nodeFactory.newBinaryExpression();
			binaryExpression.setOperator(binaryOperator);
			binaryExpression.setOperand1(operand1);
			operand1.setParent(binaryExpression);
			operand1.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
			
			// Compute the offset/length of operand1
			int closingParenthesisOffset = closeParen.getEndOffset();
			int openingParenthesisOffset = openParen.getStartOffset();
			setOffsetAndLength(operand1, openingParenthesisOffset, closingParenthesisOffset - openingParenthesisOffset + 1);
			
			binaryExpression.setOperand2(unaryOperand);
			unaryOperand.setParent(binaryExpression);
			unaryOperand.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
			setOffsetAndLength(binaryExpression);
			
			astStack.push(binaryExpression);
			return true;
		}
		return false;
	}
	
	/**
	 * primary_expression ::= '(' expression ')'
	 * 
	 * TODO: should bracketed expressions cause a new node in the AST? whats the point?
	 */
	public void consumeExpressionBracketed() {
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression();
		expr.setOperator(IASTUnaryExpression.op_bracketedPrimary);
		IASTExpression operand = (IASTExpression) astStack.pop();
		
		expr.setOperand(operand);
		operand.setParent(expr);
        operand.setPropertyInParent(IASTUnaryExpression.OPERAND);

        setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * expression ::= expression_list
	 * 
	 * In the case that an expression list consists of a single expression
	 * then discard the list.
	 */
	public void consumeExpression() {
		IASTExpressionList exprList = (IASTExpressionList) astStack.pop();
		IASTExpression[] expressions = exprList.getExpressions();
		if(expressions.length == 1) {
			astStack.push(expressions[0]);
		}
		else {
			astStack.push(exprList);
		}
	}
	
	
	/**
	 * expression_list
     *     ::= assignment_expression
     *       | expression_list ',' assignment_expression 
	 */
	public void consumeExpressionList(boolean baseCase) {
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		IASTExpressionList exprList;
		if(baseCase) {
			exprList = nodeFactory.newExpressionList();
			astStack.push(exprList);
		}
		else {
			exprList = (IASTExpressionList) astStack.peek();
		}
		
		exprList.addExpression(expr);
		expr.setParent(exprList);
		expr.setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);
		
		setOffsetAndLength(exprList);
	}
	
	
	/**
	 * Sets a token specifier.
	 * Needs to be overridable for new decl spec keywords.
	 * 
	 * @param o Allows subclasses to override this method and use any
	 * object to determine how to set a specifier.
	 */
	protected void setSpecifier(ICASTDeclSpecifier node, Object o) {
		if(!(o instanceof IToken))
			return;
		
		int kind = asC99Kind((IToken)o);
		switch(kind){
			// storage_class_specifier
			case C99Parsersym.TK_typedef: 
				node.setStorageClass(IASTDeclSpecifier.sc_typedef); 
				return;
			case C99Parsersym.TK_extern: 
				node.setStorageClass(IASTDeclSpecifier.sc_extern); 
				return;
			case C99Parsersym.TK_static:
				node.setStorageClass(IASTDeclSpecifier.sc_static);
				return;
			case C99Parsersym.TK_auto:
				node.setStorageClass(IASTDeclSpecifier.sc_auto);
				return;
			case C99Parsersym.TK_register:
				node.setStorageClass(IASTDeclSpecifier.sc_register);
				return;
			// function_specifier
			case C99Parsersym.TK_inline:
				node.setInline(true);
				return;
			// type_qualifier
			case C99Parsersym.TK_const:
				node.setConst(true);
				return;
			case C99Parsersym.TK_restrict:
				node.setRestrict(true);
				return;
			case C99Parsersym.TK_volatile:
				node.setVolatile(true);
				return;
		}
		
		// type_specifier
		if(node instanceof ICASTSimpleDeclSpecifier)
		{
			ICASTSimpleDeclSpecifier n = (ICASTSimpleDeclSpecifier) node;
			switch(kind) {
				case C99Parsersym.TK_void:
					n.setType(IASTSimpleDeclSpecifier.t_void);
					break;
				case C99Parsersym.TK_char:
					n.setType(IASTSimpleDeclSpecifier.t_char);
					break;
				case C99Parsersym.TK_short:
					n.setShort(true);
					break;
				case C99Parsersym.TK_int:
					n.setType(IASTSimpleDeclSpecifier.t_int);
					break;
				case C99Parsersym.TK_long:
					boolean isLong = n.isLong();
					n.setLongLong(isLong);
					n.setLong(!isLong);
					break;
				case C99Parsersym.TK_float:
					n.setType(IASTSimpleDeclSpecifier.t_float);
					break;
				case C99Parsersym.TK_double:
					n.setType(IASTSimpleDeclSpecifier.t_double);
					break;
				case C99Parsersym.TK_signed:
					n.setSigned(true);
					break;
				case C99Parsersym.TK_unsigned:
					n.setUnsigned(true);
					break;
				case C99Parsersym.TK__Bool:
					n.setType(ICASTSimpleDeclSpecifier.t_Bool);
					break;
				case C99Parsersym.TK__Complex:
					n.setComplex(true);
					break;
				default:
					return;
			}
		}
	}
	
	
	/**
	 * type_name ::= specifier_qualifier_list
     *             | specifier_qualifier_list abstract_declarator
	 */
	public void consumeTypeId(boolean hasDeclarator) {
		IASTTypeId typeId = nodeFactory.newTypeId();
		
		IASTDeclarator declarator;
		if(hasDeclarator) {
			declarator = (IASTDeclarator) astStack.pop();
			
		} else {
			declarator = nodeFactory.newDeclarator();
			IASTName name = nodeFactory.newName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
		}
			
		typeId.setAbstractDeclarator(declarator);
		declarator.setParent(typeId);
		declarator.setPropertyInParent(IASTTypeId.ABSTRACT_DECLARATOR);
		
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		typeId.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(typeId);
		declSpecifier.setPropertyInParent(IASTTypeId.DECL_SPECIFIER);
		
		setOffsetAndLength(typeId);
		astStack.push(typeId);
	}
	
	
	/**
	 * declarator ::= <openscope> pointer direct_declarator
     *              
     * abstract_declarator  -- a declarator that does not include an identifier
     *     ::= <openscope> pointer
     *       | <openscope> pointer direct_abstract_declarator 
	 */
	public void consumeDeclaratorWithPointer(boolean hasDeclarator) {
		IASTDeclarator decl;
		if(hasDeclarator) {	
			decl = (IASTDeclarator)astStack.pop();
		}
		else {
			decl = nodeFactory.newDeclarator();
			IASTName name = nodeFactory.newName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		}
		
		// add all the pointers to the declarator
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			ICASTPointer pointer = (ICASTPointer) iter.next();
			decl.addPointerOperator(pointer);
			pointer.setParent(decl);
			pointer.setPropertyInParent(IASTDeclarator.POINTER_OPERATOR);
			
		}
		astStack.closeASTScope();

		setOffsetAndLength(decl);
		astStack.push(decl);
	}
	
	
	/**
	 * Used by rules of the form:  direct_declarator ::= direct_declarator '[' < something > ']'
	 * Consumes the direct_declarator part, the array modifier (the square bracket part) must be provided.
	 * Returns true if there is no problem.
	 * There will be an IASTArrayDeclarator on the stack if this method returns true.
	 *  
	 * __ 5 possibilities
     *    __ identifier
     *       __ create new array declarator
     *    __ nested declarator
     *       __ create new array declarator
     *    __ array declarator
     *       __ add this modifier to existing declarator
     *    __ function declarator
     *       __ problem
     *    __ problem
     *       __ problem
	 */
	private void consumeDeclaratorArray(IASTArrayModifier arrayModifier) {
		IASTDeclarator node = (IASTDeclarator) astStack.pop();
		
		// Its a nested declarator so create an new ArrayDeclarator
		if(node.getNestedDeclarator() != null) {  //node.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			IASTArrayDeclarator declarator = nodeFactory.newArrayDeclarator();
			
			IASTName name = nodeFactory.newName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			IASTDeclarator nested = (IASTDeclarator) node;
			declarator.setNestedDeclarator(nested);
			nested.setParent(declarator);
			//nested.setPropertyInParent(IASTArrayDeclarator.NESTED_DECLARATOR);
			
			int offset = offset(nested);
			int length = endOffset(arrayModifier) - offset;
			setOffsetAndLength(declarator, offset, length);
			
			addArrayModifier(declarator, arrayModifier);
			astStack.push(declarator);
		}
		// There is already an array declarator so just add the modifier to it
		else if(node instanceof IASTArrayDeclarator) {
			IASTArrayDeclarator decl = (IASTArrayDeclarator) node;
			((ASTNode)decl).setLength(endOffset(arrayModifier) - offset(decl));
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		// The declarator is an identifier so create a new array declarator
		else if(node instanceof IASTDeclarator) {
			IASTArrayDeclarator decl = nodeFactory.newArrayDeclarator();
			
			IASTName name = (IASTName)((IASTDeclarator)node).getName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTArrayDeclarator.DECLARATOR_NAME);
		
			int offset = offset(name);
			int length = endOffset(arrayModifier) - offset;
			setOffsetAndLength(decl, offset, length);
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		else {
			IASTProblemDeclaration problem = nodeFactory.newProblemDeclaration();
			setOffsetAndLength(problem);
			astStack.push(problem);
			setEncounteredRecoverableProblem(true);
		}
	}
	
	
	private void addArrayModifier(IASTArrayDeclarator decl, IASTArrayModifier modifier) {
		decl.addArrayModifier(modifier);
		modifier.setParent(decl);
		modifier.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
	}
	
	
	/**
	 * type_qualifier ::= const | restrict | volatile
	 */
	private void collectArrayModifierTypeQualifiers(ICASTArrayModifier arrayModifier) {		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			switch(asC99Kind((IToken)iter.next())) {
				case C99Parsersym.TK_const:
					arrayModifier.setConst(true);
					break;
				case C99Parsersym.TK_restrict:
					arrayModifier.setRestrict(true);
					break;
				case C99Parsersym.TK_volatile:
					arrayModifier.setVolatile(true);
					break;
			}
		}
		astStack.closeASTScope();
	}
	
	
	/**
	 *  array_modifier 
     *      ::= '[' <openscope> type_qualifier_list ']'
     *        | '[' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' 'static' assignment_expression ']'
     *        | '[' 'static' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' <openscope> type_qualifier_list 'static' assignment_expression ']'
     *        | '[' '*' ']'
     *        | '[' <openscope> type_qualifier_list '*' ']'
     *        
     * The main reason to separate array_modifier into its own rule is to
     * make calculating the offset and length much easier.
	 */
	public void consumeDirectDeclaratorModifiedArrayModifier(boolean isStatic, 
			 boolean isVarSized, boolean hasTypeQualifierList, boolean hasAssignmentExpr) {
		assert isStatic || isVarSized || hasTypeQualifierList;
		
		ICASTArrayModifier arrayModifier = nodeFactory.newCArrayModifier();
		
		// consume all the stuff between the square brackets into an array modifier
		arrayModifier.setStatic(isStatic);
		arrayModifier.setVariableSized(isVarSized);
		
		if(hasAssignmentExpr) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			arrayModifier.setConstantExpression(expr);
			expr.setParent(arrayModifier);
			expr.setPropertyInParent(ICASTArrayModifier.CONSTANT_EXPRESSION);
		}
		
		if(hasTypeQualifierList) {
			collectArrayModifierTypeQualifiers(arrayModifier);
		}

		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
	}
	
	
	/**
	 *  array_modifier 
	 *      ::= '[' ']' 
     *        | '[' assignment_expression ']'
     */        
	public void consumeDirectDeclaratorArrayModifier(boolean hasAssignmentExpr) {
		IASTArrayModifier arrayModifier = nodeFactory.newArrayModifier();
		
		if(hasAssignmentExpr) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			arrayModifier.setConstantExpression(expr);
			expr.setParent(arrayModifier);
			expr.setPropertyInParent(ICASTArrayModifier.CONSTANT_EXPRESSION);
		}
		
		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator array_modifier
	 * 
	 * consume the direct_declarator part and add the array modifier
	 */
	public void consumeDirectDeclaratorArrayDeclarator() {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		consumeDeclaratorArray(arrayModifier);
	}
	
	
	/**
	 * direct_declarator ::= '(' declarator ')'
	 */
	public void consumeDirectDeclaratorBracketed() {
		IASTDeclarator nested = (IASTDeclarator) astStack.pop();
		IASTDeclarator declarator = nodeFactory.newDeclarator();
		
		IASTName name = nodeFactory.newName();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
		
		declarator.setNestedDeclarator(nested);
		nested.setParent(declarator);
		nested.setPropertyInParent(IASTDeclarator.NESTED_DECLARATOR);
		
		setOffsetAndLength(declarator);
		astStack.push(declarator);
	}
	
	
	/**
	 * init_declarator ::= declarator '=' initializer
	 */
	public void consumeDeclaratorWithInitializer() {
		IASTInitializer expr = (IASTInitializer) astStack.pop();
		IASTDeclarator declarator = (IASTDeclarator) astStack.peek();
		
		declarator.setInitializer(expr);
		expr.setParent(declarator);
		expr.setPropertyInParent(IASTDeclarator.INITIALIZER);
		setOffsetAndLength(declarator);
	}
	
	
	/**
	 * direct_declarator ::= 'identifier'
	 */
	public void consumeDirectDeclaratorIdentifier() {
		IASTName name = createName(parser.getRightIToken());
		
		IASTDeclarator declarator = nodeFactory.newDeclarator();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		
		setOffsetAndLength(declarator);
		astStack.push(declarator);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> parameter_type_list ')'
	 * direct_declarator ::= direct_declarator '(' ')'
	 */
	public void consumeDirectDeclaratorFunctionDeclarator(boolean hasParameters) {
		IASTStandardFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator();
		
		if(hasParameters) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				IASTParameterDeclaration parameter = (IASTParameterDeclaration) iter.next();
				declarator.addParameterDeclaration(parameter);
				parameter.setParent(declarator);
				parameter.setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
			}
			astStack.closeASTScope();
		}
		
		IASTName name = nodeFactory.newName();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
		
		int endOffset = endOffset(parser.getRightIToken());
		consumeDirectDeclaratorFunctionDeclarator(declarator, endOffset);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> identifier_list ')'
	 */
	public void consumeDirectDeclaratorFunctionDeclaratorKnR() {
		ICASTKnRFunctionDeclarator declarator = nodeFactory.newCKnRFunctionDeclarator();
		
		IASTName[] names = (IASTName[])astStack.topScopeArray(new IASTName[]{});
		declarator.setParameterNames(names);
		for(int i = 0; i < names.length; i++) {
			names[i].setParent(declarator);
			names[i].setPropertyInParent(ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER);
		}
		astStack.closeASTScope();
		
		int endOffset = endOffset(parser.getRightIToken());
		consumeDirectDeclaratorFunctionDeclarator(declarator, endOffset);
	}
	
	
	/**
	 * Pops a simple declarator from the stack, converts it into 
	 * a FunctionDeclator, then pushes it.
	 * 
	 * TODO: is this the best way of doing this?
	 * 
	 */
	private void consumeDirectDeclaratorFunctionDeclarator(IASTFunctionDeclarator declarator, int endOffset) {
		IASTDeclarator decl = (IASTDeclarator) astStack.pop();
		 
		if(decl.getNestedDeclarator() != null) { 
			decl = decl.getNestedDeclarator(); // need to remove one level of nesting for function pointers
			declarator.setNestedDeclarator(decl);
			decl.setParent(declarator);
			
			IASTName name = nodeFactory.newName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			int offset = offset(decl);
			setOffsetAndLength(declarator, offset, endOffset - offset);
			astStack.push(declarator);
		}
		else if(decl instanceof IASTDeclarator) {
			IASTName name = (IASTName)((IASTDeclarator)decl).getName();
			if(name == null) {
				name = nodeFactory.newName();
			}
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			IASTPointerOperator[] pointers = decl.getPointerOperators();
			for(int i = 0; i < pointers.length; i++) {
				IASTPointerOperator pointer = pointers[i];
				declarator.addPointerOperator(pointer);
				pointer.setParent(declarator);
				pointer.setPropertyInParent(IASTFunctionDeclarator.POINTER_OPERATOR);
			}
			
			int offset = offset(name);
			setOffsetAndLength(declarator, offset, endOffset - offset);
			astStack.push(declarator);
		}
		else {
			IASTProblemDeclaration problem = nodeFactory.newProblemDeclaration();
			setOffsetAndLength(problem);
			astStack.push(problem);
			setEncounteredRecoverableProblem(true);
		}
	}
	
	
	/**
	 * pointer ::= '*'
     *           | pointer '*' 
     */ 
	public void consumePointer() {
		IASTPointer pointer = nodeFactory.newCPointer();
		IToken star = parser.getRightIToken();
		setOffsetAndLength(pointer, star);
		astStack.push(pointer);
	}
	
	
	/**
	 * pointer ::= '*' <openscope> type_qualifier_list
     *           | pointer '*' <openscope> type_qualifier_list
	 */
	public void consumePointerTypeQualifierList() {
		ICASTPointer pointer = nodeFactory.newCPointer();

		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IToken token = (IToken) iter.next();			
			switch(asC99Kind(token)) {
				case C99Parsersym.TK_const:
					pointer.setConst(true);
					break;
				case C99Parsersym.TK_restrict:
					pointer.setRestrict(true);
					break;
				case C99Parsersym.TK_volatile:
					pointer.setVolatile(true);
					break;
			}
		}
		astStack.closeASTScope();

		setOffsetAndLength(pointer);
		astStack.push(pointer);
	}
	
	
	/**
	 * parameter_declaration ::= declaration_specifiers declarator
     *                         | declaration_specifiers   
     *                         | declaration_specifiers abstract_declarator
	 */
	public void consumeParameterDeclaration(boolean hasDeclarator) {
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration();
		
		IASTDeclarator declarator;
		if(hasDeclarator) {
			declarator = (IASTDeclarator) astStack.pop();
		}
		else { // it appears that a declarator is always required in the AST here
			declarator = nodeFactory.newDeclarator();
			int ruleOffset = getRuleOffset();
			int ruleLength = getRuleLength();
			setOffsetAndLength(declarator, ruleOffset + ruleLength, 0);
			IASTName name = nodeFactory.newName();
			setOffsetAndLength(name, ruleOffset + ruleLength, 0);
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		}
		
		declaration.setDeclarator(declarator);
		declarator.setParent(declaration);
		declarator.setPropertyInParent(IASTParameterDeclaration.DECLARATOR);
		
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTParameterDeclaration.DECL_SPECIFIER);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	/**
	 * direct_abstract_declarator   
     *     ::= array_modifier
     *       | direct_abstract_declarator array_modifier
	 */
	public void consumeAbstractDeclaratorArrayModifier(boolean hasDeclarator) {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		
		if(hasDeclarator) {
			consumeDeclaratorArray(arrayModifier);
		}
		else {
			IASTArrayDeclarator decl = nodeFactory.newArrayDeclarator();
			
			IASTName name = nodeFactory.newName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTArrayDeclarator.DECLARATOR_NAME);
			
			decl.addArrayModifier(arrayModifier);
			arrayModifier.setParent(decl);
			arrayModifier.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
			setOffsetAndLength(decl);
			astStack.push(decl);
		}
	}
	
	
	/**
	 * direct_abstract_declarator  
	 *     ::= '(' ')'
     *       | direct_abstract_declarator '(' ')'
     *       | '(' <openscope> parameter_type_list ')'
     *       | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
	 */
	public void consumeAbstractDeclaratorFunctionDeclarator(boolean hasDeclarator, boolean hasParameters) {
		IASTStandardFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator();
		
		if(hasParameters) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				IASTParameterDeclaration parameter = (IASTParameterDeclaration) iter.next();
				declarator.addParameterDeclaration(parameter);
				parameter.setParent(declarator);
				parameter.setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
			}
			astStack.closeASTScope();
		}
		
		IASTName name = nodeFactory.newName();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
		
		if(hasDeclarator) {
			consumeDirectDeclaratorFunctionDeclarator(declarator, endOffset(parser.getRightIToken()));
		}
		else {
			setOffsetAndLength(declarator);
			astStack.push(declarator);
		}
	}
	
	
	/**
	 * initializer ::= assignment_expression
	 */
	public void consumeInitializer() {
		IASTExpression assignmentExpression = (IASTExpression) astStack.pop();
		IASTInitializerExpression expr = nodeFactory.newInitializerExpression();
		
		expr.setExpression(assignmentExpression);
		assignmentExpression.setParent(expr);
        assignmentExpression.setPropertyInParent(IASTInitializerExpression.INITIALIZER_EXPRESSION);
        
        setOffsetAndLength(expr);
        astStack.push(expr);
	}
	
	
	/**
	 * initializer ::= '{' <openscope> initializer_list '}'
     *               | '{' <openscope> initializer_list ',' '}'
	 */
	public void consumeInitializerList() {
		IASTInitializerList list = nodeFactory.newInitializerList();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTInitializer initializer = (IASTInitializer) iter.next();
			list.addInitializer(initializer);
			initializer.setParent(list);
			initializer.setPropertyInParent(IASTInitializerList.NESTED_INITIALIZER);
		}
		astStack.closeASTScope();
		
		setOffsetAndLength(list);
		astStack.push(list);
	}
	
	
	/**
	 * designated_initializer ::= <openscope> designation initializer
	 */
	public void consumeInitializerDesignated() {
		ICASTDesignatedInitializer result = nodeFactory.newCDesignatedInitializer();
		IASTInitializer initializer = (IASTInitializer)astStack.pop();
		
		result.setOperandInitializer(initializer);
		initializer.setParent(result);
		initializer.setPropertyInParent(ICASTDesignatedInitializer.OPERAND);
		
		// consume the designation which is a list of designators
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			ICASTDesignator designator  = (ICASTDesignator)iter.next();
			result.addDesignator(designator);
			designator.setParent(result);
			designator.setPropertyInParent(ICASTDesignatedInitializer.DESIGNATOR);
		}
		astStack.closeASTScope();
		
		setOffsetAndLength(result);
		astStack.push(result);
	}
	
	
	/**
	 * designator ::= '[' constant_expression ']'
	 */
	public void consumeDesignatorArrayDesignator() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		ICASTArrayDesignator designator = nodeFactory.newCArrayDesignator();
		
		designator.setSubscriptExpression(expr);
		expr.setParent(designator);
		expr.setPropertyInParent(ICASTArrayDesignator.SUBSCRIPT_EXPRESSION);
		
		setOffsetAndLength(designator);
		astStack.push(designator);
	}
	
	
	/**
	 *  designator ::= '.' 'identifier'
	 */
	public void consumeDesignatorFieldDesignator() {		
		ICASTFieldDesignator designator = nodeFactory.newCFieldDesignator();
		IASTName name = createName( parser.getRightIToken() );
		
		designator.setName(name);
		name.setParent(designator);
		name.setPropertyInParent(ICASTFieldDesignator.FIELD_NAME);
		
		setOffsetAndLength(designator);
		astStack.push(designator);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> simple_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersSimple() {
		ICASTSimpleDeclSpecifier declSpec = nodeFactory.newCSimpleDeclSpecifier();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			setSpecifier(declSpec, iter.next());
		}
		astStack.closeASTScope();
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> struct_or_union_declaration_specifiers
	 * declaration_specifiers ::= <openscope> enum_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersStructUnionEnum() {
		// There's already a composite or elaborated or enum type specifier somewhere on the stack, find it.
		ICASTDeclSpecifier declSpec = null;
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Object o = iter.next();
			if(o instanceof ICASTDeclSpecifier) {
				declSpec = (ICASTDeclSpecifier) o;
				iter.remove();
				break;
			}
		}
		
		// now apply the rest of the specifiers
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			setSpecifier(declSpec, iter.next());
		}
		astStack.closeASTScope();
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::=  <openscope> typdef_name_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersTypedefName() {
		ICASTTypedefNameSpecifier declSpec = nodeFactory.newCTypedefNameSpecifier();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Object o = iter.next();
			if(o instanceof IToken) {
				IToken token = (IToken) o;
				// There is one identifier token on the stack
				int kind = asC99Kind(token);
				if(kind == C99Parsersym.TK_identifier || kind == C99Parsersym.TK_Completion) {
					IASTName name = createName(token);
					declSpec.setName(name);
					name.setParent(declSpec);
					name.setPropertyInParent(IASTNamedTypeSpecifier.NAME);
				}
				else {
					setSpecifier(declSpec, token);
				}
			}
			else {
				setSpecifier(declSpec, o);
			}
		}
		astStack.closeASTScope();
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 */
	public void consumeDeclaration(boolean hasDeclaratorList) {
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration();
		
		if(hasDeclaratorList) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				IASTDeclarator declarator = (IASTDeclarator)iter.next();
				declaration.addDeclarator(declarator);
				declarator.setParent(declaration);
				declarator.setPropertyInParent(IASTSimpleDeclaration.DECLARATOR);
			}
			astStack.closeASTScope();
		}
		
		ICASTDeclSpecifier declSpecifier = (ICASTDeclSpecifier) astStack.pop();
		
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	/**
	 * external_declaration ::= ';'
	 */
	public void consumeDeclarationEmpty() {
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration();
		IASTDeclSpecifier declSpecifier   = nodeFactory.newCSimpleDeclSpecifier();
		
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);
		setOffsetAndLength(declSpecifier);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	/**
	 * a declaration inside of a struct
	 * 
	 * struct_declaration ::= specifier_qualifier_list <openscope> struct_declarator_list ';'
	 * 
	 * specifier_qualifier_list is a subset of declaration_specifiers,
	 * struct_declarators are declarators that are allowed inside a struct,
	 * a struct declarator is a regular declarator plus bit fields
	 */
	public void consumeStructDeclaration(boolean hasDeclaration) {
		consumeDeclaration(hasDeclaration); // TODO this is ok as long as bit fields implement IASTDeclarator (see consumeDeclaration())
	} 
	
	
	/**
	 * struct_declarator
     *     ::= ':' constant_expression  
     *       | declarator ':' constant_expression		
	 */
	public void consumeStructBitField(boolean hasDeclarator) {
		IASTExpression expr = (IASTExpression)astStack.pop();
		IASTFieldDeclarator fieldDecl = nodeFactory.newFieldDeclarator();
		
		fieldDecl.setBitFieldSize(expr);
		expr.setParent(fieldDecl);
		expr.setPropertyInParent(IASTFieldDeclarator.FIELD_SIZE);
		
		IASTName name;
		if(hasDeclarator) { // it should have been parsed into a regular declarator
			IASTDeclarator decl = (IASTDeclarator) astStack.pop();
			name = decl.getName();
		}
		else {
			name = nodeFactory.newName();
		}
		
		fieldDecl.setName(name);
		name.setParent(fieldDecl);
		name.setPropertyInParent(IASTFieldDeclarator.DECLARATOR_NAME);
		
		setOffsetAndLength(fieldDecl);
		astStack.push(fieldDecl);
	}
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  '{' <openscope> struct_declaration_list_opt '}'
     *       | 'struct' struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
	 * 
	 * @param key either k_struct or k_union from IASTCompositeTypeSpecifier
	 */
	public void consumeTypeSpecifierComposite(boolean hasName, int key) {
		ICASTCompositeTypeSpecifier typeSpec = nodeFactory.newCCompositeTypeSpecifier();
		typeSpec.setKey(key); // key specifies struct or union
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTDeclaration declaration = (IASTDeclaration)iter.next();
			typeSpec.addMemberDeclaration(declaration);
			declaration.setParent(typeSpec);
			declaration.setPropertyInParent(IASTCompositeTypeSpecifier.MEMBER_DECLARATION);
		}
		astStack.closeASTScope();
		
		IASTName name = (hasName) ? (IASTName)astStack.pop() : nodeFactory.newName();
		typeSpec.setName(name);
		name.setParent(typeSpec);
		name.setPropertyInParent(IASTCompositeTypeSpecifier.TYPE_NAME);
		
		setOffsetAndLength(typeSpec);
		astStack.push(typeSpec);
	}
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' struct_or_union_identifier
     *       | 'union'  struct_or_union_identifier
     *       
     * enum_specifier ::= 'enum' enum_identifier     
	 */
	public void consumeTypeSpecifierElaborated(int kind) {
		ICASTElaboratedTypeSpecifier typeSpec = nodeFactory.newCElaboratedTypeSpecifier();
		typeSpec.setKind(kind);
		
		IASTName name = (IASTName)astStack.pop();
		typeSpec.setName(name);
		name.setParent(typeSpec);
		name.setPropertyInParent(IASTElaboratedTypeSpecifier.TYPE_NAME);
		
		setOffsetAndLength(typeSpec);
		astStack.push(typeSpec);
	}
	
	
	/**
	 * enum_specifier ::= 'enum' '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' '{' <openscope> enumerator_list_opt ',' '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt ',' '}'
	 */
	public void consumeTypeSpecifierEnumeration(boolean hasIdentifier) {
		ICASTEnumerationSpecifier enumSpec = nodeFactory.newCEnumerationSpecifier();

		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTEnumerator enumerator = (IASTEnumerator)iter.next();
			enumSpec.addEnumerator(enumerator);
			enumerator.setParent(enumSpec);
			enumerator.setPropertyInParent(ICASTEnumerationSpecifier.ENUMERATOR);
		}
		astStack.closeASTScope();
		
		IASTName name = hasIdentifier ? (IASTName)astStack.pop() : nodeFactory.newName();
		
		enumSpec.setName(name);
		name.setParent(enumSpec);
		name.setPropertyInParent(ICASTEnumerationSpecifier.ENUMERATION_NAME);
		
		setOffsetAndLength(enumSpec);
		astStack.push(enumSpec);
	}
	
	
	
	/**
	 * enumerator ::= enum_identifier
     *              | enum_identifier '=' constant_expression
	 */
	public void consumeEnumerator(boolean hasInitializer) {
		IASTEnumerator enumerator = nodeFactory.newEnumerator();
		
		if(hasInitializer) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			enumerator.setValue(expr);
			expr.setParent(enumerator);
			expr.setPropertyInParent(IASTEnumerator.ENUMERATOR_VALUE);
		}
		
		IASTName name = (IASTName)astStack.pop();
		enumerator.setName(name);
		name.setParent(enumerator);
		name.setPropertyInParent(IASTEnumerator.ENUMERATOR_NAME);
		
		setOffsetAndLength(enumerator);
		astStack.push(enumerator);
	}
		
	
	/**
	 * compound_statement ::= <openscope> '{' block_item_list '}'
	 * 
	 * block_item_list ::= block_item | block_item_list block_item
	 */
	public void consumeStatementCompoundStatement() {
		IASTCompoundStatement block = nodeFactory.newCompoundStatement();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTStatement statement = (IASTStatement)iter.next();
			block.addStatement(statement);
			statement.setParent(block);
			statement.setPropertyInParent(IASTCompoundStatement.NESTED_STATEMENT);
		}
		astStack.closeASTScope();
		
		setOffsetAndLength(block);
		astStack.push(block);
	}
	
	
	/**
	 * compound_statement ::= '{' '}' 
	 */
	public void consumeStatementEmptyCompoundStatement() {
		IASTCompoundStatement block = nodeFactory.newCompoundStatement();
		setOffsetAndLength(block);
		astStack.push(block);
	}
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'for' '(' expression ';' expression ';' expression ')' statement
     *       | 'for' '(' expression ';' expression ';'            ')' statement
     *       | 'for' '(' expression ';'            ';' expression ')' statement
     *       | 'for' '(' expression ';'            ';'            ')' statement
     *       | 'for' '('            ';' expression ';' expression ')' statement
     *       | 'for' '('            ';' expression ';'            ')' statement
     *       | 'for' '('            ';'            ';' expression ')' statement
     *       | 'for' '('            ';'            ';'            ')' statement
     *       | 'for' '(' declaration expression ';' expression ')' statement
     *       | 'for' '(' declaration expression ';'            ')' statement
     *       | 'for' '(' declaration            ';' expression ')' statement
     *       | 'for' '(' declaration            ';'            ')' statement
     *       
	 */
	public void consumeStatementForLoop(boolean hasExpr1, boolean hasExpr2, boolean hasExpr3) {
		IASTForStatement forStat = nodeFactory.newForStatement();
		IASTStatement body = (IASTStatement) astStack.pop();
		
		forStat.setBody(body);
		body.setParent(forStat);
		body.setPropertyInParent(IASTForStatement.BODY);
		
		if(hasExpr3) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setIterationExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IASTForStatement.ITERATION);
		} 
		
		if(hasExpr2) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setConditionExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IASTForStatement.CONDITION);
		}
		
		if(hasExpr1) { // may be an expression or a declaration
			IASTNode node = (IASTNode) astStack.pop();
			
			if(node instanceof IASTExpression) {
				IASTExpressionStatement stat = nodeFactory.newExpressionStatement();
				IASTExpression expr = (IASTExpression)node;
				stat.setExpression(expr);
				expr.setParent(stat);
				expr.setPropertyInParent(IASTExpressionStatement.EXPRESSION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IASTForStatement.INITIALIZER);
			}
			else if(node instanceof IASTDeclaration) {
				IASTDeclarationStatement stat = nodeFactory.newDeclarationStatement();
				IASTDeclaration declaration = (IASTDeclaration)node;
				stat.setDeclaration(declaration);
				declaration.setParent(stat);
				declaration.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IASTForStatement.INITIALIZER);
			}
		}
		else {
			forStat.setInitializerStatement(nodeFactory.newNullStatement());
		}
		
		setOffsetAndLength(forStat);
		astStack.push(forStat);
	}
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'while' '(' expression ')' matched_statement
	 */
	public void consumeStatementWhileLoop() {
		IASTWhileStatement stat = nodeFactory.newWhileStatement();
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTWhileStatement.BODY);
		
		stat.setCondition(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTWhileStatement.CONDITIONEXPRESSION);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'do' statement 'while' '(' expression ')' ';'
	 */
	public void consumeStatementDoLoop() {
		IASTDoStatement stat = nodeFactory.newDoStatement();
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTStatement body = (IASTStatement) astStack.pop();
		
		stat.setCondition(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTDoStatement.CONDITION);
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTDoStatement.BODY);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	

	
	/**
	 * block_item ::= declaration | statement 
	 * 
	 * Wrap a declaration in a DeclarationStatement.
	 * 
	 * Disambiguation:
	 * 
	 * x; // should be an expression statement
	 * 
	 */
	public void consumeStatementDeclaration() {
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		
		if(disambiguateHackIdentifierExpression(decl))
			return;
		if(disambiguateHackFunctionCall(decl))
			return;
		
		IASTDeclarationStatement stat = nodeFactory.newDeclarationStatement();
		
		stat.setDeclaration(decl);
		decl.setParent(stat);
		decl.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		disambiguateHackMultiplicationExpression();
	}
	
	
	/**
	 * Disambiguates the case where a standalone multiplication expression is parsed
	 * as a declaration:
	 * 
	 * ex)  x * y;
	 * 
	 */
	private void disambiguateHackMultiplicationExpression() {
		if(!useDisambiguationHacks)
			return;
		
		List<IToken> tokens = parser.getRuleTokens();
		
		// if what was parsed looks like: ident * ident ;
		// oh how I miss static imports 
		if(!matchKinds(tokens, 
			new int[]{C99Parsersym.TK_identifier, C99Parsersym.TK_Star, C99Parsersym.TK_identifier, C99Parsersym.TK_SemiColon})) {
			return;
		}
		
		IASTDeclarationStatement declStat = (IASTDeclarationStatement)astStack.pop();
		
		IToken ident1 = tokens.get(0);
		IASTName name1 = createName(ident1);
		IASTIdExpression id1 = nodeFactory.newIdExpression();
		id1.setName(name1);
		name1.setParent(id1);
		name1.setPropertyInParent(IASTIdExpression.ID_NAME);
		setOffsetAndLength(id1, ident1);
		
		IToken ident2 = tokens.get(2);
		IASTName name2 = createName(ident2);
		IASTIdExpression id2 = nodeFactory.newIdExpression();
		id2.setName(name2);
		name2.setParent(id2);
		name2.setPropertyInParent(IASTIdExpression.ID_NAME);
		setOffsetAndLength(id2);
		
		astStack.push(id1);
		astStack.push(id2);
		
		consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiply);
		consumeStatementExpression();
		
		IASTExpressionStatement exprStat = (IASTExpressionStatement)astStack.pop();
		
		IASTAmbiguousStatement ambiguousStatement = nodeFactory.newAmbiguousStatement();
		ambiguousStatement.addStatement(declStat);
		declStat.setParent(ambiguousStatement);
		declStat.setPropertyInParent(IASTAmbiguousExpression.SUBEXPRESSION);
		
		ambiguousStatement.addStatement(exprStat);
		exprStat.setParent(ambiguousStatement);
		exprStat.setPropertyInParent(IASTAmbiguousExpression.SUBEXPRESSION);
		
		astStack.push(ambiguousStatement);
	}
	
	
	
	
	/**
	 * Kludgy way to disambiguate a certain case.
	 * An identifier alone on a line will be parsed as a declaration
	 * but it probably should be an expression.
	 * eg) i;
	 */
	private boolean disambiguateHackIdentifierExpression(IASTDeclaration decl) {
		if(!useDisambiguationHacks)
			return false;
		
		if(decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) decl;
			if(declaration.getDeclarators() == IASTDeclarator.EMPTY_DECLARATOR_ARRAY) {
				IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
				if(declSpec instanceof ICASTTypedefNameSpecifier) {
					ICASTTypedefNameSpecifier typedefNameSpec = (ICASTTypedefNameSpecifier) declSpec;
					IASTName name = (IASTName)typedefNameSpec.getName();
					
					if(offset(name) == offset(typedefNameSpec) && length(name) == length(typedefNameSpec)) {						
						IASTExpressionStatement stat = nodeFactory.newExpressionStatement();
						IASTIdExpression idExpr = nodeFactory.newIdExpression();
						idExpr.setName(name);
						name.setParent(idExpr);
						name.setPropertyInParent(IASTIdExpression.ID_NAME);
						
						stat.setExpression(idExpr);
						idExpr.setParent(stat);
						idExpr.setPropertyInParent(IASTExpressionStatement.EXPRESSION);
						
						setOffsetAndLength(stat);
						astStack.push(stat);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * A hack to disambiguate one special case:
	 * example:
	 *             x ( y ) ;
	 *
	 * Is that a function call or a declaration of a variable y of type x that just happens to be bracketed?
	 * The function call would be the more common situation, so even though it will always
	 * be parsed as a declaration it would be better to return an expression instead.
	 * 
	 * Really, this is just to get this parser to behave similar to the dom parser.
	 */
	private boolean disambiguateHackFunctionCall(IASTDeclaration decl) {
		if(!useDisambiguationHacks)
			return false;
		
		if(!(decl instanceof IASTSimpleDeclaration))
			return false;
		
		// Match the tokens against   x ( y ) ;
		List<IToken> tokens = parser.getRuleTokens();
		if(!matchKinds(tokens, 
			new int[]{C99Parsersym.TK_identifier, C99Parsersym.TK_LeftParen, C99Parsersym.TK_identifier, C99Parsersym.TK_RightParen, C99Parsersym.TK_SemiColon})) {
			return false;
		}
			
		// We have detected the situation that needs to be disambiguated.
		// Build a function call expression and discard the declaration.
		IASTName name = createName(tokens.get(0));
		
		IASTIdExpression functionName = nodeFactory.newIdExpression();
		functionName.setName(name);
		name.setParent(functionName);
		name.setPropertyInParent(IASTIdExpression.ID_NAME);
		setOffsetAndLength(functionName, offset(name), length(name));
		
		IASTName name2 = createName(tokens.get(2));
		IASTIdExpression parameter = nodeFactory.newIdExpression();
		parameter.setName(name2);
		name2.setParent(parameter);
		name2.setPropertyInParent(IASTIdExpression.ID_NAME);
		setOffsetAndLength(parameter, offset(name2), length(name2));
		
		IASTFunctionCallExpression expr = nodeFactory.newFunctionCallExpression();
		expr.setFunctionNameExpression(functionName);
		functionName.setParent(expr);
		functionName.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);
		expr.setParameterExpression(parameter);
		parameter.setParent(expr);
		parameter.setPropertyInParent(IASTFunctionCallExpression.PARAMETERS);
		
		IToken rightParen = tokens.get(3);
		setOffsetAndLength(expr, offset(name), endOffset(rightParen) - offset(name));
		
		IASTExpressionStatement stat = nodeFactory.newExpressionStatement();
		stat.setExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTExpressionStatement.EXPRESSION);
		setOffsetAndLength(stat);
		
		astStack.push(stat);
		return true;
	}
	
	
	/**
	 * jump_statement ::= goto goto_identifier ';'
	 */
	public void consumeStatementGoto() {
		IASTName name = (IASTName) astStack.pop();
		IASTGotoStatement gotoStat = nodeFactory.newGotoStatement();
		
		gotoStat.setName(name);
		name.setParent(gotoStat);
		name.setPropertyInParent(IASTGotoStatement.NAME);
		
		setOffsetAndLength(gotoStat);
		astStack.push(gotoStat);
	}
	
	
	/**
	 * jump_statement ::= continue ';'
	 */
	public void consumeStatementContinue() {  
		IASTContinueStatement stat = nodeFactory.newContinueStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= break ';'
	 */
	public void consumeStatementBreak() {   
		IASTBreakStatement stat = nodeFactory.newBreakStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= return ';'
	 * jump_statement ::= return expression ';'
	 */
	public void consumeStatementReturn(boolean hasExpression) {
		IASTReturnStatement returnStat = nodeFactory.newReturnStatement();
		
		if(hasExpression) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			returnStat.setReturnValue(expr);
			expr.setParent(returnStat);
			expr.setPropertyInParent(IASTReturnStatement.RETURNVALUE);
		}
		
		setOffsetAndLength(returnStat);
		astStack.push(returnStat);
	}
	
	
	/**
	 * expression_statement ::= ';'
	 */
	public void consumeStatementNull() {
		IASTNullStatement stat = nodeFactory.newNullStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * expression_statement ::= expression ';'
	 */
	public void consumeStatementExpression() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTExpressionStatement stat = nodeFactory.newExpressionStatement();
		
		stat.setExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTExpressionStatement.EXPRESSION);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= label_identifier ':' statement
	 * label_identifier ::= identifier 
	 */
	public void consumeStatementLabeled() {
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTName label = (IASTName) astStack.pop();
		IASTLabelStatement stat = nodeFactory.newLabelStatement();
		
		stat.setNestedStatement(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTLabelStatement.NESTED_STATEMENT);
		
		stat.setName(label);
		label.setParent(stat);
		label.setPropertyInParent(IASTLabelStatement.NAME);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= case constant_expression ':'
	 */
	public void consumeStatementCase() { 
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTCaseStatement caseStatement = nodeFactory.newCaseStatement();
		
		caseStatement.setExpression(expr);
		expr.setParent(caseStatement);
		expr.setPropertyInParent(IASTCaseStatement.EXPRESSION);
		
		setOffsetAndLength(caseStatement);
		astStack.push(caseStatement);
	}
	
	
	/**
	 * labeled_statement ::= default ':'
	 */
	public void consumeStatementDefault() {
		IASTDefaultStatement stat = nodeFactory.newDefaultStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * selection_statement ::=  switch '(' expression ')' statement
	 */
	public void consumeStatementSwitch() {
		IASTStatement body  = (IASTStatement)  astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTSwitchStatement stat = nodeFactory.newSwitchStatment();
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTSwitchStatement.BODY);
		
		stat.setControllerExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTSwitchStatement.CONTROLLER_EXP);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * if_then_statement ::= if '(' expression ')' statement
	 */
	public void consumeStatementIfThen() {
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTIfStatement stat = nodeFactory.newIfStatement();
		
		stat.setConditionExpression(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTIfStatement.CONDITION);
		
		stat.setThenClause(thenClause);
		thenClause.setParent(stat);
		thenClause.setPropertyInParent(IASTIfStatement.THEN);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * if_then_else_matched_statement
     *     ::= if '(' expression ')' statement_no_short_if else statement_no_short_if
     *     
     * if_then_else_unmatched_statement
     *     ::= if '(' expression ')' statement_no_short_if else statement
	 */
	public void consumeStatementIfThenElse() { 
		IASTStatement elseClause = (IASTStatement) astStack.pop();
		
		consumeStatementIfThen();
		IASTIfStatement stat = (IASTIfStatement) astStack.pop();
		
		stat.setElseClause(elseClause);
		elseClause.setParent(stat);
		elseClause.setPropertyInParent(IASTIfStatement.ELSE);
		
		// the offset and length is set in consumeStatementIfThen()
		astStack.push(stat);
	}
	

	/**
	 * translation_unit ::= external_declaration_list
     *
     * external_declaration_list
     *    ::= external_declaration
     *      | external_declaration_list external_declaration
	 */
	public void consumeTranslationUnit() {
		IASTTranslationUnit tu = nodeFactory.newTranslationUnit();
		tu.setParent(null);
		tu.setPropertyInParent(null);
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTDeclaration declaration = (IASTDeclaration) iter.next();
			tu.addDeclaration(declaration);
			declaration.setParent(tu);
			declaration.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
		}
		
		IToken eof = parser.getEOFToken();
		setOffsetAndLength(tu, 0, eof.getEndOffset());
		astStack.push(tu); 
	}
	
	
	/**
	 * function_definition
     *    ::= declaration_specifiers <openscope> declarator compound_statement
     *      | function_declarator compound_statement
     */
	public void consumeFunctionDefinition(boolean hasDeclSpecifiers) {
		IASTFunctionDefinition def = nodeFactory.newFunctionDefinition();
		
		IASTCompoundStatement  body = (IASTCompoundStatement)  astStack.pop();
		IASTFunctionDeclarator decl = (IASTFunctionDeclarator) astStack.pop();
		// The seemingly pointless <openscope> is just there to 
		// prevent a shift/reduce conflict in the grammar.
		
		
		IASTDeclSpecifier declSpecifier;
		if(hasDeclSpecifiers) {
			astStack.closeASTScope();
			declSpecifier = (IASTDeclSpecifier) astStack.pop();
		}
		else {
			declSpecifier = nodeFactory.newCSimpleDeclSpecifier();
		}
		
		def.setBody(body);
		body.setParent(def);
		body.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
		
		def.setDeclarator(decl);
		decl.setParent(def);
		decl.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);
		
		def.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(def);
		declSpecifier.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);
		
		setOffsetAndLength(def);
		astStack.push(def);
	}
	
    
    /**
     * function_definition
     *     ::= declaration_specifiers <openscope> declarator 
     *         <openscope> declaration_list compound_statement
     */
	public void consumeFunctionDefinitionKnR() {
    	IASTFunctionDefinition def = nodeFactory.newFunctionDefinition();
    	
    	// compound_statement
    	IASTCompoundStatement  body = (IASTCompoundStatement) astStack.pop();
    	
    	// declaration_list, parameters
    	IASTDeclaration[] declarations = (IASTDeclaration[]) astStack.topScopeArray(new IASTDeclaration[]{});
    	astStack.closeASTScope();
    	
    	// declarator
    	ICASTKnRFunctionDeclarator decl = (ICASTKnRFunctionDeclarator) astStack.pop();
    	astStack.closeASTScope();

    	ICASTSimpleDeclSpecifier declSpecifier = (ICASTSimpleDeclSpecifier) astStack.pop();
    	
    	decl.setParameterDeclarations(declarations);
		for(int i = 0; i < declarations.length; i++) {
			declarations[i].setParent(decl);
			declarations[i].setPropertyInParent(ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER);
		}
		
		// re-compute the length of the declaration to take the parameter declarations into account
		ASTNode lastDeclaration = (ASTNode) declarations[declarations.length-1];
		int endOffset = lastDeclaration.getOffset() + lastDeclaration.getLength();
		((ASTNode)decl).setLength(endOffset - offset(decl));
		
		def.setBody(body);
		body.setParent(def);
		body.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
		
		def.setDeclarator(decl);
		decl.setParent(def);
		decl.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);
		
		def.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(def);
		declSpecifier.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);
	
		setOffsetAndLength(def);
    	astStack.push(def);
    }
	
	
	/**
	 * statement ::= ERROR_TOKEN
	 */
	public void consumeStatementProblem() {
		consumeProblem(nodeFactory.newProblemStatement());
	}
	
	
	/**
	 * assignment_expression ::= ERROR_TOKEN
	 * constant_expression ::= ERROR_TOKEN
	 */
	public void consumeExpressionProblem() {
		consumeProblem(nodeFactory.newProblemExpression());
	}
	
	
	/**
	 * external_declaration ::= ERROR_TOKEN
	 */
	public void consumeDeclarationProblem() {
		consumeProblem(nodeFactory.newProblemDeclaration());
	}
	
	
	private void consumeProblem(IASTProblemHolder problemHolder) {
		setEncounteredRecoverableProblem(true);
		
		IASTProblem problem = nodeFactory.newProblem(IASTProblem.SYNTAX_ERROR, EMPTY_CHAR_ARRAY, false, true);
		
		problemHolder.setProblem(problem);
		problem.setParent((IASTNode)problemHolder);
		problem.setPropertyInParent(IASTProblemStatement.PROBLEM);
		
		setOffsetAndLength(problem);
		setOffsetAndLength((ASTNode)problemHolder);
		astStack.push(problemHolder);
	}


	
}
