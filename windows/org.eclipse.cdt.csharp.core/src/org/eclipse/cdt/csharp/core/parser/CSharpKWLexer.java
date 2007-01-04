package org.eclipse.cdt.csharp.core.parser;

import lpg.lpgjavaruntime.*;

public class CSharpKWLexer extends CSharpKWLexerprs implements CSharpParsersym
{
    private char[] inputChars;
    private final int keywordKind[] = new int[85 + 1];

    public int[] getKeywordKinds() { return keywordKind; }

    public int lexer(int curtok, int lasttok)
    {
        int current_kind = getKind(inputChars[curtok]),
            act;

        for (act = tAction(START_STATE, current_kind);
             act > NUM_RULES && act < ACCEPT_ACTION;
             act = tAction(act, current_kind))
        {
            curtok++;
            current_kind = (curtok > lasttok
                                   ? Char_EOF
                                   : getKind(inputChars[curtok]));
        }

        if (act > ERROR_ACTION)
        {
            curtok++;
            act -= ERROR_ACTION;
        }

        return keywordKind[act == ERROR_ACTION  || curtok <= lasttok ? 0 : act];
    }

    public void setInputChars(char[] inputChars) { this.inputChars = inputChars; }


    //
    // Each upper case letter is mapped into is corresponding
    // lower case counterpart. For example, if an 'A' appears
    // in the input, it is mapped into Char_a just like 'a'.
    //
    final static int tokenKind[] = new int[128];
    static
    {
        tokenKind['$'] = Char_DollarSign;
        tokenKind['_'] = Char__;

        tokenKind['a'] = Char_a;
        tokenKind['b'] = Char_b;
        tokenKind['c'] = Char_c;
        tokenKind['d'] = Char_d;
        tokenKind['e'] = Char_e;
        tokenKind['f'] = Char_f;
        tokenKind['g'] = Char_g;
        tokenKind['h'] = Char_h;
        tokenKind['i'] = Char_i;
        tokenKind['j'] = Char_j;
        tokenKind['k'] = Char_k;
        tokenKind['l'] = Char_l;
        tokenKind['m'] = Char_m;
        tokenKind['n'] = Char_n;
        tokenKind['o'] = Char_o;
        tokenKind['p'] = Char_p;
        tokenKind['q'] = Char_q;
        tokenKind['r'] = Char_r;
        tokenKind['s'] = Char_s;
        tokenKind['t'] = Char_t;
        tokenKind['u'] = Char_u;
        tokenKind['v'] = Char_v;
        tokenKind['w'] = Char_w;
        tokenKind['x'] = Char_x;
        tokenKind['y'] = Char_y;
        tokenKind['z'] = Char_z;
    };

    final int getKind(char c)
    {
        return (c < 128 ? tokenKind[c] : 0);
    }


    public CSharpKWLexer(char[] inputChars, int identifierKind)
    {
        this.inputChars = inputChars;
        keywordKind[0] = identifierKind;

        //
        // Rule 1:  Keyword ::= a b s t r a c t
        //
	keywordKind[1] = (TK_abstract);

        //
        // Rule 2:  Keyword ::= a d d
        //
	keywordKind[2] = (TK_add);

        //
        // Rule 3:  Keyword ::= a l i a s
        //
	keywordKind[3] = (TK_alias);

        //
        // Rule 4:  Keyword ::= a s
        //
	keywordKind[4] = (TK_as);

        //
        // Rule 5:  Keyword ::= b a s e
        //
	keywordKind[5] = (TK_base);

        //
        // Rule 6:  Keyword ::= b o o l
        //
	keywordKind[6] = (TK_bool);

        //
        // Rule 7:  Keyword ::= b r e a k
        //
	keywordKind[7] = (TK_break);

        //
        // Rule 8:  Keyword ::= b y t e
        //
	keywordKind[8] = (TK_byte);

        //
        // Rule 9:  Keyword ::= c a s e
        //
	keywordKind[9] = (TK_case);

        //
        // Rule 10:  Keyword ::= c a t c h
        //
	keywordKind[10] = (TK_catch);

        //
        // Rule 11:  Keyword ::= c h a r
        //
	keywordKind[11] = (TK_char);

        //
        // Rule 12:  Keyword ::= c h e c k e d
        //
	keywordKind[12] = (TK_checked);

        //
        // Rule 13:  Keyword ::= c l a s s
        //
	keywordKind[13] = (TK_class);

        //
        // Rule 14:  Keyword ::= c o n s t
        //
	keywordKind[14] = (TK_const);

        //
        // Rule 15:  Keyword ::= c o n t i n u e
        //
	keywordKind[15] = (TK_continue);

        //
        // Rule 16:  Keyword ::= d e c i m a l
        //
	keywordKind[16] = (TK_decimal);

        //
        // Rule 17:  Keyword ::= d e f a u l t
        //
	keywordKind[17] = (TK_default);

        //
        // Rule 18:  Keyword ::= d e l e g a t e
        //
	keywordKind[18] = (TK_delegate);

        //
        // Rule 19:  Keyword ::= d o
        //
	keywordKind[19] = (TK_do);

        //
        // Rule 20:  Keyword ::= d o u b l e
        //
	keywordKind[20] = (TK_double);

        //
        // Rule 21:  Keyword ::= e l s e
        //
	keywordKind[21] = (TK_else);

        //
        // Rule 22:  Keyword ::= e n u m
        //
	keywordKind[22] = (TK_enum);

        //
        // Rule 23:  Keyword ::= e v e n t
        //
	keywordKind[23] = (TK_event);

        //
        // Rule 24:  Keyword ::= e x p l i c i t
        //
	keywordKind[24] = (TK_explicit);

        //
        // Rule 25:  Keyword ::= e x t e r n
        //
	keywordKind[25] = (TK_extern);

        //
        // Rule 26:  Keyword ::= f a l s e
        //
	keywordKind[26] = (TK_false);

        //
        // Rule 27:  Keyword ::= f i n a l l y
        //
	keywordKind[27] = (TK_finally);

        //
        // Rule 28:  Keyword ::= f i x e d
        //
	keywordKind[28] = (TK_fixed);

        //
        // Rule 29:  Keyword ::= f l o a t
        //
	keywordKind[29] = (TK_float);

        //
        // Rule 30:  Keyword ::= f o r
        //
	keywordKind[30] = (TK_for);

        //
        // Rule 31:  Keyword ::= f o r e a c h
        //
	keywordKind[31] = (TK_foreach);

        //
        // Rule 32:  Keyword ::= g e t
        //
	keywordKind[32] = (TK_get);

        //
        // Rule 33:  Keyword ::= g o t o
        //
	keywordKind[33] = (TK_goto);

        //
        // Rule 34:  Keyword ::= i f
        //
	keywordKind[34] = (TK_if);

        //
        // Rule 35:  Keyword ::= i m p l i c i t
        //
	keywordKind[35] = (TK_implicit);

        //
        // Rule 36:  Keyword ::= i n
        //
	keywordKind[36] = (TK_in);

        //
        // Rule 37:  Keyword ::= i n t
        //
	keywordKind[37] = (TK_int);

        //
        // Rule 38:  Keyword ::= i n t e r f a c e
        //
	keywordKind[38] = (TK_interface);

        //
        // Rule 39:  Keyword ::= i n t e r n a l
        //
	keywordKind[39] = (TK_internal);

        //
        // Rule 40:  Keyword ::= i s
        //
	keywordKind[40] = (TK_is);

        //
        // Rule 41:  Keyword ::= l o c k
        //
	keywordKind[41] = (TK_lock);

        //
        // Rule 42:  Keyword ::= l o n g
        //
	keywordKind[42] = (TK_long);

        //
        // Rule 43:  Keyword ::= n a m e s p a c e
        //
	keywordKind[43] = (TK_namespace);

        //
        // Rule 44:  Keyword ::= n e w
        //
	keywordKind[44] = (TK_new);

        //
        // Rule 45:  Keyword ::= n u l l
        //
	keywordKind[45] = (TK_null);

        //
        // Rule 46:  Keyword ::= o b j e c t
        //
	keywordKind[46] = (TK_object);

        //
        // Rule 47:  Keyword ::= o p e r a t o r
        //
	keywordKind[47] = (TK_operator);

        //
        // Rule 48:  Keyword ::= o u t
        //
	keywordKind[48] = (TK_out);

        //
        // Rule 49:  Keyword ::= o v e r r i d e
        //
	keywordKind[49] = (TK_override);

        //
        // Rule 50:  Keyword ::= p a r a m s
        //
	keywordKind[50] = (TK_params);

        //
        // Rule 51:  Keyword ::= p a r t i a l
        //
	keywordKind[51] = (TK_partial);

        //
        // Rule 52:  Keyword ::= p r i v a t e
        //
	keywordKind[52] = (TK_private);

        //
        // Rule 53:  Keyword ::= p r o t e c t e d
        //
	keywordKind[53] = (TK_protected);

        //
        // Rule 54:  Keyword ::= p u b l i c
        //
	keywordKind[54] = (TK_public);

        //
        // Rule 55:  Keyword ::= r e a d o n l y
        //
	keywordKind[55] = (TK_readonly);

        //
        // Rule 56:  Keyword ::= r e f
        //
	keywordKind[56] = (TK_ref);

        //
        // Rule 57:  Keyword ::= r e m o v e
        //
	keywordKind[57] = (TK_remove);

        //
        // Rule 58:  Keyword ::= r e t u r n
        //
	keywordKind[58] = (TK_return);

        //
        // Rule 59:  Keyword ::= s b y t e
        //
	keywordKind[59] = (TK_sbyte);

        //
        // Rule 60:  Keyword ::= s e a l e d
        //
	keywordKind[60] = (TK_sealed);

        //
        // Rule 61:  Keyword ::= s e t
        //
	keywordKind[61] = (TK_set);

        //
        // Rule 62:  Keyword ::= s h o r t
        //
	keywordKind[62] = (TK_short);

        //
        // Rule 63:  Keyword ::= s i z e o f
        //
	keywordKind[63] = (TK_sizeof);

        //
        // Rule 64:  Keyword ::= s t a c k a l l o c
        //
	keywordKind[64] = (TK_stackalloc);

        //
        // Rule 65:  Keyword ::= s t a t i c
        //
	keywordKind[65] = (TK_static);

        //
        // Rule 66:  Keyword ::= s t r i n g
        //
	keywordKind[66] = (TK_string);

        //
        // Rule 67:  Keyword ::= s t r u c t
        //
	keywordKind[67] = (TK_struct);

        //
        // Rule 68:  Keyword ::= s w i t c h
        //
	keywordKind[68] = (TK_switch);

        //
        // Rule 69:  Keyword ::= t h i s
        //
	keywordKind[69] = (TK_this);

        //
        // Rule 70:  Keyword ::= t h r o w
        //
	keywordKind[70] = (TK_throw);

        //
        // Rule 71:  Keyword ::= t r u e
        //
	keywordKind[71] = (TK_true);

        //
        // Rule 72:  Keyword ::= t r y
        //
	keywordKind[72] = (TK_try);

        //
        // Rule 73:  Keyword ::= t y p e o f
        //
	keywordKind[73] = (TK_typeof);

        //
        // Rule 74:  Keyword ::= u i n t
        //
	keywordKind[74] = (TK_uint);

        //
        // Rule 75:  Keyword ::= u l o n g
        //
	keywordKind[75] = (TK_ulong);

        //
        // Rule 76:  Keyword ::= u n c h e c k e d
        //
	keywordKind[76] = (TK_unchecked);

        //
        // Rule 77:  Keyword ::= u n s a f e
        //
	keywordKind[77] = (TK_unsafe);

        //
        // Rule 78:  Keyword ::= u s h o r t
        //
	keywordKind[78] = (TK_ushort);

        //
        // Rule 79:  Keyword ::= u s i n g
        //
	keywordKind[79] = (TK_using);

        //
        // Rule 80:  Keyword ::= v i r t u a l
        //
	keywordKind[80] = (TK_virtual);

        //
        // Rule 81:  Keyword ::= v o i d
        //
	keywordKind[81] = (TK_void);

        //
        // Rule 82:  Keyword ::= v o l a t i l e
        //
	keywordKind[82] = (TK_volatile);

        //
        // Rule 83:  Keyword ::= w h e r e
        //
	keywordKind[83] = (TK_where);
  

        //
        // Rule 84:  Keyword ::= w h i l e
        //
	keywordKind[84] = (TK_while);
  

        //
        // Rule 85:  Keyword ::= y i e l d
        //
	keywordKind[85] = (TK_yield);
  


        for (int i = 0; i < keywordKind.length; i++)
        {
            if (keywordKind[i] == 0)
                keywordKind[i] = identifierKind;
        }
    }
}

