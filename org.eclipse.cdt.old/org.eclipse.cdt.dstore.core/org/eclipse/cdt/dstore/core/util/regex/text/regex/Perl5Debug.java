package org.eclipse.cdt.dstore.core.util.regex.text.regex;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Jakarta-Oro"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    or "Jakarta-Oro", nor may "Apache" or "Jakarta-Oro" appear in their
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon software originally written
 * by Daniel F. Savarese. We appreciate his contributions.
 */

/**
 * The Perl5Debug class is not intended for general use and should not
 * be instantiated, but is provided because some users may find the output
 * of its single method to be useful.
 * The Perl5Compiler class generates a representation of a
 * regular expression identical to that of Perl5 in the abstract, but
 * not in terms of actual data structures.  The Perl5Debug class allows
 * the bytecode program contained by a Perl5Pattern to be printed out for
 * comparison with the program generated by Perl5 with the -r option.

 @author <a href="mailto:oro-dev@jakarta.apache.org">Daniel F. Savarese</a>
 @version $Id: Perl5Debug.java,v 1.1.1.1 2001/09/10 22:18:41 dmcknigh Exp $

 * @see Perl5Pattern
 */
public final class Perl5Debug {

  /**
   * A dummy constructor to prevent instantiation of Perl5Debug.
   */
  private Perl5Debug() { }


  /**
   * This method prints to a String the bytecode program contained in a
   * Perl5Pattern._  The program byte codes are identical to those
   * generated by Perl5 with the -r option, but the offsets are
   * different due to the different data structures used.  This
   * method is useful for diagnosing suspected bugs.  The Perl5Compiler
   * class is designed to produce regular expression programs identical
   * to those produced by Perl5.  By comparing the output of this method
   * and the output of Perl5 with the -r option on the same regular
   * expression, you can determine if Perl5Compiler correctly compiled
   * an expression.
   * <p>
   * @param regexp  The Perl5Pattern to print.
   * @return A string representation of the bytecode program defining the
   *         regular expression.
   */


  public static String printProgram(Perl5Pattern regexp) {
    StringBuffer buffer;
    char operator = OpCode._OPEN, prog[];
    int offset, next;

    prog = regexp._program;
    offset = 1;
    buffer = new StringBuffer();

    while(operator != OpCode._END) {
      operator = prog[offset];
      buffer.append(offset);
      _printOperator(prog, offset, buffer);

      next = OpCode._getNext(prog, offset);
      offset+=OpCode._operandLength[operator];

      buffer.append("(" + next + ")");

      offset+=2;

      if(operator == OpCode._ANYOF) {
	offset += 16;
      } else if(operator == OpCode._ANYOFUN || operator == OpCode._NANYOFUN) {
	offset+=(prog[offset-1]-2);
      } else if(operator == OpCode._EXACTLY) {
	  ++offset;
	  buffer.append(" <");

	//while(prog[offset] != '0')
	while(prog[offset] != CharStringPointer._END_OF_STRING) {
	  //while(prog[offset] != 0 &&
	  //  prog[offset] != CharStringPointer._END_OF_STRING) {
	  buffer.append(prog[offset]);
	  ++offset;
	}
	buffer.append(">");
	++offset;
      }

      buffer.append('\n');
    }

    // Can print some other stuff here.
    if(regexp._startString != null)
      buffer.append("start `" + new String(regexp._startString) + "' ");
    if(regexp._startClassOffset != OpCode._NULL_OFFSET) {
      buffer.append("stclass `");
      _printOperator(prog, regexp._startClassOffset, buffer);
      buffer.append("' ");
    }
    if((regexp._anchor & Perl5Pattern._OPT_ANCH) != 0)
      buffer.append("anchored ");
    if((regexp._anchor & Perl5Pattern._OPT_SKIP) != 0)
      buffer.append("plus ");
    if((regexp._anchor & Perl5Pattern._OPT_IMPLICIT) != 0)
      buffer.append("implicit ");
    if(regexp._mustString != null)
      buffer.append("must have \""+ new String(regexp._mustString) +
		       "\" back " + regexp._back + " ");
    buffer.append("minlen " + regexp._minLength + '\n');

    return buffer.toString();
  }


  static void _printOperator(char[] program, int offset, StringBuffer buffer) {
    String str = null;

    buffer.append(":");

    switch(program[offset]) {
    case OpCode._BOL      : str = "BOL"; break;
    case OpCode._MBOL     : str = "MBOL"; break;
    case OpCode._SBOL     : str = "SBOL"; break;
    case OpCode._EOL      : str = "EOL"; break;
    case OpCode._MEOL     : str = "MEOL"; break;
    case OpCode._ANY      : str = "ANY"; break;
    case OpCode._SANY     : str = "SANY"; break;
    case OpCode._ANYOF    : str = "ANYOF"; break;
    case OpCode._ANYOFUN  : str = "ANYOFUN"; break;
    case OpCode._NANYOFUN : str = "NANYOFUN"; break;
      /*
    case OpCode._ANYOF : // debug
      buffer.append("ANYOF\n\n");
      int foo = OpCode._OPERAND(offset);
      char ch;
      for(ch=0; ch < 256; ch++) {
	if(ch % 16 == 0)
	  buffer.append(" ");
	buffer.append((program[foo + (ch >> 4)] &
		       (1 << (ch & 0xf))) == 0 ? 0 : 1);
      }
      buffer.append("\n\n");
      break;
      */
    case OpCode._BRANCH  : str = "BRANCH"; break;
    case OpCode._EXACTLY : str = "EXACTLY"; break;
    case OpCode._NOTHING : str = "NOTHING"; break;
    case OpCode._BACK    : str = "BACK"; break;
    case OpCode._END     : str = "END"; break;
    case OpCode._ALNUM   : str = "ALNUM"; break;
    case OpCode._NALNUM  : str = "NALNUM"; break;
    case OpCode._BOUND   : str = "BOUND"; break;
    case OpCode._NBOUND  : str = "NBOUND"; break;
    case OpCode._SPACE   : str = "SPACE"; break;
    case OpCode._NSPACE  : str = "NSPACE"; break;
    case OpCode._DIGIT   : str = "DIGIT"; break;
    case OpCode._NDIGIT  : str = "NDIGIT"; break;
    case OpCode._ALPHA   : str = "ALPHA"; break;
    case OpCode._BLANK   : str = "BLANK"; break;
    case OpCode._CNTRL   : str = "CNTRL"; break;
    case OpCode._GRAPH   : str = "GRAPH"; break;
    case OpCode._LOWER   : str = "LOWER"; break;
    case OpCode._PRINT   : str = "PRINT"; break;
    case OpCode._PUNCT   : str = "PUNCT"; break;
    case OpCode._UPPER   : str = "UPPER"; break;
    case OpCode._XDIGIT  : str = "XDIGIT"; break;
    case OpCode._ALNUMC  : str = "ALNUMC"; break;
    case OpCode._ASCII   : str = "ASCII"; break;
    case OpCode._CURLY :
      buffer.append("CURLY {");
      buffer.append((int)OpCode._getArg1(program, offset));
      buffer.append(','); buffer.append((int)OpCode._getArg2(program, offset));
      buffer.append('}');
      break;
    case OpCode._CURLYX:
      buffer.append("CURLYX {");
      buffer.append((int)OpCode._getArg1(program, offset));
      buffer.append(','); buffer.append((int)OpCode._getArg2(program, offset));
      buffer.append('}');
      break;
    case OpCode._REF:
      buffer.append("REF"); buffer.append((int)OpCode._getArg1(program, offset));
      break;
    case OpCode._OPEN:
      buffer.append("OPEN"); buffer.append((int)OpCode._getArg1(program, offset));
      break;
    case OpCode._CLOSE:
      buffer.append("CLOSE"); buffer.append((int)OpCode._getArg1(program, offset));
      break;
    case OpCode._STAR   : str = "STAR"; break;
    case OpCode._PLUS   : str = "PLUS"; break;
    case OpCode._MINMOD : str = "MINMOD"; break;
    case OpCode._GBOL   : str = "GBOL"; break;
    case OpCode._UNLESSM: str = "UNLESSM"; break;
    case OpCode._IFMATCH: str = "IFMATCH"; break;
    case OpCode._SUCCEED: str = "SUCCEED"; break;
    case OpCode._WHILEM : str = "WHILEM"; break;
    default:
      buffer.append("Operator is unrecognized.  Faulty expression code!");
      break;
    }

    if(str != null)
      buffer.append(str);
  }
}
