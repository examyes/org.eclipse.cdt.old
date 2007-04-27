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
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.*;

public class C99ExprEvaluator extends PrsStream implements RuleAction
{
    private static ParseTable prs = new C99ExprEvaluatorprs();
    private BacktrackingParser btParser;

    public BacktrackingParser getParser() { return btParser; }
    private void setResult(Object object) { btParser.setSym1(object); }
    public Object getRhsSym(int i) { return btParser.getSym(i); }

    public int getRhsTokenIndex(int i) { return btParser.getToken(i); }
    public IToken getRhsIToken(int i) { return super.getIToken(getRhsTokenIndex(i)); }
    
    public int getRhsFirstTokenIndex(int i) { return btParser.getFirstToken(i); }
    public IToken getRhsFirstIToken(int i) { return super.getIToken(getRhsFirstTokenIndex(i)); }

    public int getRhsLastTokenIndex(int i) { return btParser.getLastToken(i); }
    public IToken getRhsLastIToken(int i) { return super.getIToken(getRhsLastTokenIndex(i)); }

    public int getLeftSpan() { return btParser.getFirstToken(); }
    public IToken getLeftIToken()  { return super.getIToken(getLeftSpan()); }

    public int getRightSpan() { return btParser.getLastToken(); }
    public IToken getRightIToken() { return super.getIToken(getRightSpan()); }

    public int getRhsErrorTokenIndex(int i)
    {
        int index = btParser.getToken(i);
        IToken err = super.getIToken(index);
        return (err instanceof ErrorToken ? index : 0);
    }
    public ErrorToken getRhsErrorIToken(int i)
    {
        int index = btParser.getToken(i);
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
            System.out.println(Messages.getString("C99ExprEvaluator.0")); //$NON-NLS-1$
            for (int i = 0; i < unimplemented_symbols.size(); i++)
            {
                Integer id = (Integer) unimplemented_symbols.get(i);
                System.out.println("    " + C99ExprEvaluatorsym.orderedTerminalSymbols[id.intValue()]);                //$NON-NLS-1$
            }
            System.out.println();                        
        }
        catch(UndefinedEofSymbolException e)
        {
            throw new Error(new UndefinedEofSymbolException
                                (Messages.getString("C99ExprEvaluator.1") + //$NON-NLS-1$
                                 C99ExprEvaluatorsym.orderedTerminalSymbols[C99ExprEvaluatorprs.EOFT_SYMBOL]));
        } 
    }

    public String[] orderedTerminalSymbols() { return C99ExprEvaluatorsym.orderedTerminalSymbols; }
    public String getTokenKindName(int kind) { return C99ExprEvaluatorsym.orderedTerminalSymbols[kind]; }
    public int getEOFTokenKind() { return C99ExprEvaluatorprs.EOFT_SYMBOL; }
    public PrsStream getParseStream() { return (PrsStream) this; }
    
    //
    // Report error message for given error_token.
    //
    public final void reportErrorTokenMessage(int error_token, String msg)
    {
        int firsttok = super.getFirstErrorToken(error_token),
            lasttok = super.getLastErrorToken(error_token);
        String location = super.getFileName() + ':' +
                          (firsttok > lasttok
                                    ? (super.getEndLine(lasttok) + ":" + super.getEndColumn(lasttok)) //$NON-NLS-1$
                                    : (super.getLine(error_token) + ":" + //$NON-NLS-1$
                                       super.getColumn(error_token) + ":" + //$NON-NLS-1$
                                       super.getEndLine(error_token) + ":" + //$NON-NLS-1$
                                       super.getEndColumn(error_token)))
                          + ": "; //$NON-NLS-1$
        super.reportError((firsttok > lasttok ? ParseErrorCodes.INSERTION_CODE : ParseErrorCodes.SUBSTITUTION_CODE), location, msg);
    }

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
            btParser = new BacktrackingParser(monitor, (TokenStream) this, prs, (RuleAction) this);
        }
        catch (NotBacktrackParseTableException e)
        {
            throw new Error(new NotBacktrackParseTableException
                                (Messages.getString("C99ExprEvaluator.2"))); //$NON-NLS-1$
        }
        catch (BadParseSymFileException e)
        {
            throw new Error(new BadParseSymFileException(Messages.getString("C99ExprEvaluator.3"))); //$NON-NLS-1$
        }

        try
        {
            return (Object) btParser.parse(error_repair_count);
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

public C99ExprEvaluator(TokenList tokens) {
	this(new C99Lexer() {
		public String[] orderedExportedSymbols() {
			return C99Parsersym.orderedTerminalSymbols;
		}
	});
	addToken(C99Token.DUMMY_TOKEN);
	for(Iterator iter = tokens.iterator(); iter.hasNext();) {
		C99Token token = new C99Token((IToken)iter.next());
		// Map token kinds defined in the C99Parser to those defined in the C99ExprEvaluator
		token.setKind(mapKind(token.getKind()));
		addToken(token);
	}
	addToken(new C99Token(0, 0, C99ExprEvaluatorsym.TK_EOF_TOKEN, "<EOF>")); //$NON-NLS-1$
	setStreamLength(getSize());
}


public void addToken(IToken token) {
	ArrayList tokens = getTokens();
	ArrayList adjuncts = getAdjuncts();
	tokens.add(token);
	token.setTokenIndex(tokens.size());
    token.setAdjunctIndex(adjuncts.size());
}

public Integer evaluate() {
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
