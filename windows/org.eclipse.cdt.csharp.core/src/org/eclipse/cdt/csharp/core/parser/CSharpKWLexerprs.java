package org.eclipse.cdt.csharp.core.parser;

public class CSharpKWLexerprs implements lpg.lpgjavaruntime.ParseTable, CSharpKWLexersym {

    public interface IsKeyword {
        public final static byte isKeyword[] = {0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0
        };
    };
    public final static byte isKeyword[] = IsKeyword.isKeyword;
    public final boolean isKeyword(int index) { return isKeyword[index] != 0; }

    public interface BaseCheck {
        public final static byte baseCheck[] = {0,
            8,3,5,2,4,4,5,4,4,5,
            4,7,5,5,8,7,7,8,2,6,
            4,4,5,8,6,5,7,5,5,3,
            7,3,4,2,8,2,3,9,8,2,
            4,4,9,3,4,6,8,3,8,6,
            7,7,9,6,8,3,6,6,5,6,
            3,5,6,10,6,6,6,6,4,5,
            4,3,6,4,5,9,6,6,5,7,
            4,8,5,5,5
        };
    };
    public final static byte baseCheck[] = BaseCheck.baseCheck;
    public final int baseCheck(int index) { return baseCheck[index]; }
    public final static byte rhs[] = baseCheck;
    public final int rhs(int index) { return rhs[index]; };

    public interface BaseAction {
        public final static char baseAction[] = {
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,88,146,160,
            162,113,34,83,24,166,99,33,109,163,
            41,30,57,64,47,66,74,84,170,115,
            71,168,14,117,173,174,177,114,123,180,
            78,97,178,103,182,90,186,130,181,187,
            190,193,191,195,198,200,58,201,204,205,
            206,209,210,125,221,137,211,214,219,222,
            28,224,227,140,142,235,231,236,228,239,
            240,243,241,246,247,249,248,254,257,258,
            260,261,262,265,268,272,270,275,277,133,
            147,280,278,283,282,288,289,290,292,291,
            294,155,297,298,302,304,309,299,307,311,
            313,314,318,321,322,325,326,329,331,332,
            333,337,334,339,342,343,151,344,346,347,
            349,357,359,361,362,367,369,363,370,373,
            375,377,371,378,380,382,384,386,387,389,
            390,391,392,394,400,402,404,409,412,407,
            405,416,414,417,421,423,425,427,429,430,
            431,434,433,436,444,440,445,447,450,452,
            453,456,454,461,459,462,463,464,466,468,
            469,470,475,479,482,476,487,490,491,493,
            492,495,496,498,499,500,507,508,510,514,
            515,502,517,518,524,523,530,526,150,527,
            531,533,535,536,539,546,547,550,548,541,
            552,554,556,559,561,562,567,563,568,570,
            571,573,575,580,582,584,574,585,586,591,
            592,594,598,595,599,603,601,607,610,612,
            608,614,615,617,618,623,626,628,630,631,
            633,634,635,637,640,367,367
        };
    };
    public final static char baseAction[] = BaseAction.baseAction;
    public final int baseAction(int index) { return baseAction[index]; }
    public final static char lhs[] = baseAction;
    public final int lhs(int index) { return lhs[index]; };

    public interface TermCheck {
        public final static byte termCheck[] = {0,
            0,1,2,3,4,5,6,7,8,9,
            10,11,12,0,14,15,16,17,5,19,
            0,21,22,0,1,2,13,0,5,0,
            1,4,0,0,7,6,13,4,5,16,
            0,14,9,10,12,22,0,1,16,9,
            10,19,6,21,14,0,0,0,18,3,
            4,5,6,0,7,0,9,4,3,4,
            0,6,9,0,4,5,3,0,13,6,
            3,8,0,0,21,8,23,4,15,0,
            8,2,3,10,11,13,0,15,0,16,
            0,3,0,14,2,3,8,18,0,1,
            12,3,0,0,0,1,0,5,6,5,
            12,25,0,7,0,12,10,5,15,0,
            8,0,0,9,5,6,0,5,2,0,
            1,0,3,2,12,0,0,23,2,0,
            0,10,2,7,0,19,2,3,9,0,
            10,0,0,14,5,0,1,0,6,0,
            1,26,0,0,13,8,0,0,6,0,
            0,0,9,6,5,0,0,1,8,0,
            0,2,0,1,0,19,15,0,4,0,
            0,16,2,0,0,0,2,2,0,0,
            0,1,0,0,24,6,8,18,0,22,
            0,0,19,0,4,12,0,0,10,3,
            0,1,9,12,0,0,2,10,0,0,
            0,6,0,5,4,0,0,0,0,4,
            11,3,10,0,8,2,0,0,11,0,
            0,0,3,6,0,9,2,0,1,0,
            9,0,1,13,0,6,0,0,2,0,
            1,0,0,2,10,8,4,0,0,0,
            0,0,2,0,6,4,0,0,0,12,
            11,0,1,0,8,8,0,4,0,1,
            0,1,0,0,21,17,4,0,1,6,
            0,0,1,3,0,0,20,3,0,1,
            0,0,0,0,4,10,0,1,0,1,
            9,0,0,0,3,0,0,5,0,16,
            18,0,7,10,8,7,0,1,0,1,
            0,0,0,3,2,4,0,1,0,0,
            0,3,0,1,0,1,0,0,2,0,
            11,0,12,0,1,0,0,8,0,0,
            0,0,6,0,17,14,7,7,5,0,
            9,0,17,0,0,2,0,6,0,1,
            22,0,1,0,8,0,0,1,5,20,
            0,6,0,3,0,21,0,5,0,0,
            0,3,0,0,8,0,7,5,3,0,
            10,8,18,0,0,2,0,1,4,0,
            11,0,0,0,2,0,5,8,0,4,
            0,0,0,0,2,0,5,0,0,0,
            17,0,12,10,0,0,18,2,0,10,
            13,0,8,5,3,20,0,1,20,0,
            0,0,0,2,0,0,7,0,0,0,
            3,0,7,2,14,13,0,0,1,0,
            11,17,14,0,0,9,0,0,9,3,
            7,7,0,0,2,0,0,10,5,0,
            0,2,0,7,0,0,4,7,0,1,
            0,1,7,9,19,0,0,0,3,0,
            4,0,3,0,3,0,9,4,0,4,
            0,0,0,1,4,4,0,0,2,0,
            0,4,0,0,0,3,6,3,20,0,
            11,0,3,0,0,0,5,2,15,5,
            0,0,2,0,0,4,13,0,0,1,
            0,7,0,1,4,12,0,0,11,0,
            1,0,1,0,0,8,0,0,4,2,
            7,15,0,7,2,0,1,0,1,0,
            0,2,0,0,0,1,0,1,6,0,
            0,11,0,0,11,0,7,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0
        };
    };
    public final static byte termCheck[] = TermCheck.termCheck;
    public final int termCheck(int index) { return termCheck[index]; }

    public interface TermAction {
        public final static char termAction[] = {0,
            367,103,93,107,99,100,97,105,95,98,
            94,104,92,367,102,89,106,101,168,96,
            367,91,90,367,123,120,169,367,121,367,
            140,213,367,367,215,139,122,114,115,124,
            367,214,113,112,130,119,367,150,132,137,
            407,131,149,129,401,367,367,367,138,144,
            142,143,141,367,198,367,197,148,154,152,
            367,151,147,367,165,166,158,367,153,157,
            180,156,367,367,146,179,145,159,155,367,
            117,185,187,371,160,118,367,116,367,161,
            367,128,367,423,428,183,127,186,367,134,
            126,135,367,367,367,164,367,111,110,163,
            133,181,367,171,367,175,170,177,439,367,
            176,367,367,205,190,189,367,242,207,367,
            218,367,219,220,241,367,367,204,243,367,
            367,221,275,244,367,208,255,256,332,367,
            276,367,367,333,108,367,125,367,136,367,
            162,366,367,367,109,167,367,367,172,367,
            367,367,173,182,178,367,367,192,191,367,
            367,415,367,193,367,174,184,367,195,367,
            36,188,199,367,367,367,201,399,367,367,
            367,209,367,367,194,203,202,196,367,411,
            367,19,200,367,206,210,367,367,211,217,
            367,223,216,212,367,367,222,225,367,367,
            367,224,367,226,228,367,367,367,367,229,
            369,231,227,367,230,232,367,367,448,367,
            367,367,235,234,367,233,441,367,238,367,
            237,367,438,236,367,239,367,367,240,367,
            245,367,367,248,436,246,247,367,367,367,
            367,367,253,367,250,252,367,367,367,249,
            251,367,259,367,257,258,367,412,367,260,
            37,261,367,367,254,409,262,30,263,400,
            367,367,265,264,367,367,408,266,367,268,
            367,367,367,367,269,267,367,388,367,272,
            270,367,367,367,273,367,367,274,367,271,
            389,367,278,277,378,279,367,376,367,375,
            367,367,367,280,282,373,367,372,367,367,
            367,281,367,451,367,450,367,367,283,367,
            452,367,284,367,287,367,367,285,367,367,
            367,367,288,367,446,286,289,290,292,367,
            291,367,442,367,367,429,367,294,367,295,
            437,367,426,367,296,367,367,300,299,293,
            367,298,367,301,367,297,367,302,367,367,
            367,305,367,367,304,367,306,309,310,367,
            307,308,303,367,367,396,367,393,311,367,
            395,367,367,367,390,367,313,312,367,314,
            367,367,367,367,381,367,318,367,367,367,
            315,367,316,380,367,367,317,445,367,370,
            377,367,320,321,322,319,367,444,374,367,
            367,367,367,434,367,367,323,367,367,367,
            324,367,432,327,440,435,367,367,424,367,
            427,433,430,367,367,425,367,367,325,328,
            421,326,367,367,330,367,367,417,329,367,
            367,413,367,334,367,367,336,335,367,387,
            367,342,337,392,331,367,367,367,338,367,
            339,367,340,367,343,367,341,344,367,447,
            367,367,367,419,346,347,367,367,348,367,
            367,418,367,367,367,351,350,352,345,367,
            349,367,353,367,367,367,354,356,394,355,
            367,367,384,367,367,383,398,367,367,449,
            367,358,367,359,360,357,367,367,379,367,
            361,367,416,367,367,414,367,367,406,402,
            362,422,367,363,391,367,385,367,382,367,
            367,368,367,367,367,410,367,405,364,367,
            367,443,367,367,420,367,431
        };
    };
    public final static char termAction[] = TermAction.termAction;
    public final int termAction(int index) { return termAction[index]; }
    public final int asb(int index) { return 0; }
    public final int asr(int index) { return 0; }
    public final int nasb(int index) { return 0; }
    public final int nasr(int index) { return 0; }
    public final int terminalIndex(int index) { return 0; }
    public final int nonterminalIndex(int index) { return 0; }
    public final int scopePrefix(int index) { return 0;}
    public final int scopeSuffix(int index) { return 0;}
    public final int scopeLhs(int index) { return 0;}
    public final int scopeLa(int index) { return 0;}
    public final int scopeStateSet(int index) { return 0;}
    public final int scopeRhs(int index) { return 0;}
    public final int scopeState(int index) { return 0;}
    public final int inSymb(int index) { return 0;}
    public final String name(int index) { return null; }
    public final int getErrorSymbol() { return 0; }
    public final int getScopeUbound() { return 0; }
    public final int getScopeSize() { return 0; }
    public final int getMaxNameLength() { return 0; }

    public final static int
           NUM_STATES        = 278,
           NT_OFFSET         = 29,
           LA_STATE_OFFSET   = 452,
           MAX_LA            = 1,
           NUM_RULES         = 85,
           NUM_NONTERMINALS  = 2,
           NUM_SYMBOLS       = 31,
           SEGMENT_SIZE      = 8192,
           START_STATE       = 86,
           IDENTIFIER_SYMBOL = 0,
           EOFT_SYMBOL       = 26,
           EOLT_SYMBOL       = 30,
           ACCEPT_ACTION     = 366,
           ERROR_ACTION      = 367;

    public final static boolean BACKTRACK = false;

    public final int getNumStates() { return NUM_STATES; }
    public final int getNtOffset() { return NT_OFFSET; }
    public final int getLaStateOffset() { return LA_STATE_OFFSET; }
    public final int getMaxLa() { return MAX_LA; }
    public final int getNumRules() { return NUM_RULES; }
    public final int getNumNonterminals() { return NUM_NONTERMINALS; }
    public final int getNumSymbols() { return NUM_SYMBOLS; }
    public final int getSegmentSize() { return SEGMENT_SIZE; }
    public final int getStartState() { return START_STATE; }
    public final int getStartSymbol() { return lhs[0]; }
    public final int getIdentifierSymbol() { return IDENTIFIER_SYMBOL; }
    public final int getEoftSymbol() { return EOFT_SYMBOL; }
    public final int getEoltSymbol() { return EOLT_SYMBOL; }
    public final int getAcceptAction() { return ACCEPT_ACTION; }
    public final int getErrorAction() { return ERROR_ACTION; }
    public final boolean isValidForParser() { return isValidForParser; }
    public final boolean getBacktrack() { return BACKTRACK; }

    public final int originalState(int state) { return 0; }
    public final int asi(int state) { return 0; }
    public final int nasi(int state) { return 0; }
    public final int inSymbol(int state) { return 0; }

    public final int ntAction(int state, int sym) {
        return baseAction[state + sym];
    }

    public final int tAction(int state, int sym) {
        int i = baseAction[state],
            k = i + sym;
        return termAction[termCheck[k] == sym ? k : i];
    }
    public final int lookAhead(int la_state, int sym) {
        int k = la_state + sym;
        return termAction[termCheck[k] == sym ? k : la_state];
    }
}
