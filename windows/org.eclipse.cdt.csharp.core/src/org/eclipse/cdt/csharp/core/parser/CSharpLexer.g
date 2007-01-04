--
-- C# Lexer
--
%Options fp=CSharpLexer
%options single-productions
%options package=org.eclipse.cdt.csharp.core.parser
%options template=LexerTemplateD.g
%options filter=CSharpKWLexer.g

$Define
    $kw_lexer_class /.$CSharpKWLexer./
$End

$Include
    LexerBasicMap.g
$End

$Export
	IDENTIFIER
$End

$Terminals
	CtlCharNotWS

	a b c d e f g h i j k l m n o p q r s t u v w x y z
	A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
	0 1 2 3 4 5 6 7 8 9
    _
	
    AfterASCII   ::= '\u0080..\ufffe'
    Space        ::= ' '
    LF           ::= NewLine
    CR           ::= Return
    HT           ::= HorizontalTab
    FF           ::= FormFeed
    DoubleQuote  ::= '"'
    SingleQuote  ::= "'"
    Percent      ::= '%'
    VerticalBar  ::= '|'
    Exclamation  ::= '!'
    AtSign       ::= '@'
    BackQuote    ::= '`'
    Tilde        ::= '~'
    Sharp        ::= '#'
    DollarSign   ::= '$'
    Ampersand    ::= '&'
    Caret        ::= '^'
    Colon        ::= ':'
    SemiColon    ::= ';'
    BackSlash    ::= '\'
    LeftBrace    ::= '{'
    RightBrace   ::= '}'
    LeftBracket  ::= '['
    RightBracket ::= ']'
    QuestionMark ::= '?'
    Comma        ::= ','
    Dot          ::= '.'
    LessThan     ::= '<'
    GreaterThan  ::= '>'
    Plus         ::= '+'
    Minus        ::= '-'
    Slash        ::= '/'
    Star         ::= '*'
    LeftParen    ::= '('
    RightParen   ::= ')'
    Equal        ::= '='
$End

$Start
    Token
$End

$Rules
	Token ::= Identifier
		/.$BeginAction
			checkForKeyWord();
		$EndAction./
		
	Identifier
		-> AvailableIdentifier
		 | '@' IdentifierOrKeyword
	
	AvailableIdentifier -> IdentifierOrKeyword
	
	IdentifierOrKeyword -> IdentifierStartCharacter IdentifierPartCharacters

	IdentifierStartCharacter -> LetterCharacter | _

	IdentifierPartCharacters
		-> LetterCharacter
		 | DecimalDigitCharacter
		 | _

	LetterCharacter
		-> a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z
		 | A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z

	DecimalDigitCharacter -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
	
$End
