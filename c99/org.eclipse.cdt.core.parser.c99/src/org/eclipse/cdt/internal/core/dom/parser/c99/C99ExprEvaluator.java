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

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.parser.c99;

import lpg.lpgjavaruntime.*;

import java.util.*;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.*;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;

public class C99ExprEvaluator extends PrsStream implements RuleAction
{
    private static ParseTable prs = new C99ExprEvaluatorprs();
    private DeterministicParser dtParser;

    public DeterministicParser getParser() { return dtParser; }
    private void setResult(Object object) { dtParser.setSym1(object); }
    public Object getRhsSym(int i) { return dtParser.getSym(i); }

    public int getRhsTokenIndex(int i) { return dtParser.getToken(i); }
    public IToken getRhsIToken(int i) { return super.getIToken(getRhsTokenIndex(i)); }
    
    public int getRhsFirstTokenIndex(int i) { return dtParser.getFirstToken(i); }
    public IToken getRhsFirstIToken(int i) { return super.getIToken(getRhsFirstTokenIndex(i)); }

    public int getRhsLastTokenIndex(int i) { return dtParser.getLastToken(i); }
    public IToken getRhsLastIToken(int i) { return super.getIToken(getRhsLastTokenIndex(i)); }

    public int getLeftSpan() { return dtParser.getFirstToken(); }
    public IToken getLeftIToken()  { return super.getIToken(getLeftSpan()); }

    public int getRightSpan() { return dtParser.getLastToken(); }
    public IToken getRightIToken() { return super.getIToken(getRightSpan()); }

    public int getRhsErrorTokenIndex(int i)
    {
        int index = dtParser.getToken(i);
        IToken err = super.getIToken(index);
        return (err instanceof ErrorToken ? index : 0);
    }
    public ErrorToken getRhsErrorIToken(int i)
    {
        int index = dtParser.getToken(i);
        IToken err = super.getIToken(index);
        return (ErrorToken) (err instanceof ErrorToken ? err : null);
    }

    public C99ExprEvaluator(LexStream lexStream)
    {
        super(lexStream);

        try
        {
            super.remapTerminalSymbols(orderedTerminalSymbols(), C99ExprEvaluatorprs.EOFT_SYMBOL);
        }
        catch(NullExportedSymbolsException e) {
        }
        catch(NullTerminalSymbolsException e) {
        }
        catch(UnimplementedTerminalsException e)
        {
            java.util.ArrayList unimplemented_symbols = e.getSymbols();
            System.out.println("The Lexer will not scan the following token(s):");//$NON-NLS-1$
            for (int i = 0; i < unimplemented_symbols.size(); i++)
            {
                Integer id = (Integer) unimplemented_symbols.get(i);
                System.out.println("    " + C99ExprEvaluatorsym.orderedTerminalSymbols[id.intValue()]);//$NON-NLS-1$               
            }
            System.out.println();                        
        }
        catch(UndefinedEofSymbolException e)
        {
            throw new Error(new UndefinedEofSymbolException
                                ("The Lexer does not implement the Eof symbol " +//$NON-NLS-1$
                                 C99ExprEvaluatorsym.orderedTerminalSymbols[C99ExprEvaluatorprs.EOFT_SYMBOL]));
        } 
    }

    public String[] orderedTerminalSymbols() { return C99ExprEvaluatorsym.orderedTerminalSymbols; }
    public String getTokenKindName(int kind) { return C99ExprEvaluatorsym.orderedTerminalSymbols[kind]; }            
    public int getEOFTokenKind() { return C99ExprEvaluatorprs.EOFT_SYMBOL; }
    public PrsStream getParseStream() { return (PrsStream) this; }

    public Object parser()
    {
        return parser(null, 0);
    }
        
    public Object parser(Monitor monitor)
    {
        return parser(monitor, 0);
    }
        
    public Object parser(int error_repair_count)
    {
        return parser(null, error_repair_count);
    }
        
    public Object parser(Monitor monitor, int error_repair_count)
    {
        try
        {
            dtParser = new DeterministicParser(monitor, (TokenStream)this, prs, (RuleAction)this);
        }
        catch (NotDeterministicParseTableException e)
        {
            throw new Error(new NotDeterministicParseTableException
                                ("Regenerate C99ExprEvaluatorprs.java with -NOBACKTRACK option"));//$NON-NLS-1$
        }
        catch (BadParseSymFileException e)
        {
            throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- C99ExprEvaluatorsym.java. Regenerate C99ExprEvaluatorprs.java"));//$NON-NLS-1$
        }

        try
        {
            return (Object) dtParser.parse();
        }
        catch (BadParseException e)
        {
            reset(e.error_token); // point to error token

            DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
            diagnoseParser.diagnose(e.error_token);
        }

        return null;
    }


private C99ExprEvaluatorAction action = new C99ExprEvaluatorAction(this);

public C99ExprEvaluator(TokenList tokens, final IPPTokenComparator comparator) {
	this(new C99Lexer() {
		public String[] orderedExportedSymbols() {
			return comparator.getLPGOrderedTerminalSymbols();
		}
	});
	addToken(Token.DUMMY_TOKEN);
	for(Iterator iter = tokens.iterator(); iter.hasNext();) {
		Token token = new Token((Token)iter.next());
		// Map token kinds defined in the C99Parser to those defined in the C99ExprEvaluator
		token.setKind(mapKind(token.getKind()));
		addToken(token);
	}
	addToken(new Token(0, 0, C99ExprEvaluatorsym.TK_EOF_TOKEN, "<EOF>"));
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

    public void ruleAction(int ruleNumber)
    {
        switch (ruleNumber)
        {
 
            //
            // Rule 1:  constant ::= integer
            //
            case 1: { action.   evalExpressionConstantInteger();            break;
            }
 
            //
            // Rule 2:  constant ::= charconst
            //
            case 2: { action.   evalExpressionConstantChar();               break;
            }
 
            //
            // Rule 4:  primary_expression ::= identifier
            //
            case 4: { action.   evalExpressionID();            break;
            }
 
            //
            // Rule 7:  unary_expression ::= + unary_expression
            //
            case 7: { action.   evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_plus);             break;
            }
 
            //
            // Rule 8:  unary_expression ::= - unary_expression
            //
            case 8: { action.   evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_minus);            break;
            }
 
            //
            // Rule 9:  unary_expression ::= ~ unary_expression
            //
            case 9: { action.   evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_tilde);            break;
            }
 
            //
            // Rule 10:  unary_expression ::= ! unary_expression
            //
            case 10: { action.   evalExpressionUnaryOperator(C99ExprEvaluatorAction.op_not);              break;
            }
 
            //
            // Rule 12:  multiplicative_expression ::= multiplicative_expression * unary_expression
            //
            case 12: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_multiply);           break;
            }
 
            //
            // Rule 13:  multiplicative_expression ::= multiplicative_expression / unary_expression
            //
            case 13: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_divide);             break;
            }
 
            //
            // Rule 14:  multiplicative_expression ::= multiplicative_expression % unary_expression
            //
            case 14: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_modulo);             break;
            }
 
            //
            // Rule 16:  additive_expression ::= additive_expression + multiplicative_expression
            //
            case 16: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_plus);            break;
            }
 
            //
            // Rule 17:  additive_expression ::= additive_expression - multiplicative_expression
            //
            case 17: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_minus);           break;
            }
 
            //
            // Rule 19:  shift_expression ::= shift_expression << additive_expression
            //
            case 19: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_shiftLeft);             break;
            }
 
            //
            // Rule 20:  shift_expression ::= shift_expression >> additive_expression
            //
            case 20: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_shiftRight);            break;
            }
 
            //
            // Rule 22:  relational_expression ::= relational_expression < shift_expression
            //
            case 22: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_lessThan);               break;
            }
 
            //
            // Rule 23:  relational_expression ::= relational_expression > shift_expression
            //
            case 23: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_greaterThan);            break;
            }
 
            //
            // Rule 24:  relational_expression ::= relational_expression <= shift_expression
            //
            case 24: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_lessEqual);              break;
            }
 
            //
            // Rule 25:  relational_expression ::= relational_expression >= shift_expression
            //
            case 25: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_greaterEqual);           break;
            }
 
            //
            // Rule 27:  equality_expression ::= equality_expression == relational_expression
            //
            case 27: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_equals);            break;
            }
 
            //
            // Rule 28:  equality_expression ::= equality_expression != relational_expression
            //
            case 28: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_notequals);            break;
            }
 
            //
            // Rule 30:  AND_expression ::= AND_expression & equality_expression
            //
            case 30: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_binaryAnd);            break;
            }
 
            //
            // Rule 32:  exclusive_OR_expression ::= exclusive_OR_expression ^ AND_expression
            //
            case 32: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_binaryXor);            break;
            }
 
            //
            // Rule 34:  inclusive_OR_expression ::= inclusive_OR_expression | exclusive_OR_expression
            //
            case 34: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_binaryOr);            break;
            }
 
            //
            // Rule 36:  logical_AND_expression ::= logical_AND_expression && inclusive_OR_expression
            //
            case 36: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_logicalAnd);            break;
            }
 
            //
            // Rule 38:  logical_OR_expression ::= logical_OR_expression || logical_AND_expression
            //
            case 38: { action.   evalExpressionBinaryOperator(C99ExprEvaluatorAction.op_logicalOr);            break;
            }
 
            //
            // Rule 40:  conditional_expression ::= logical_OR_expression ? constant_expression : conditional_expression
            //
            case 40: { action.   evalExpressionConditional();            break;
            }

    
            default:
                break;
        }
        return;
    }
}

