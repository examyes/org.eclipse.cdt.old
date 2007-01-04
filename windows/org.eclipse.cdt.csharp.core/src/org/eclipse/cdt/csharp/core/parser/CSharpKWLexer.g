--
-- C# Keyword Lexer
--
%Options fp=CSharpKWLexer
%Options package=org.eclipse.cdt.csharp.core.parser
%Options template=KeyWordTemplateD.g

$Include
    KWLexerLowerCaseMap.g
$End

$Export
	abstract
	add
	alias
	as
	base
	bool
	break
	byte
	case
	catch
	char
	checked
	class
	const
	continue
	decimal
	default
	delegate
	do
	double
	else
	enum
	event
	explicit
	extern
	false
	finally
	fixed
	float
	for
	foreach
	get
	goto
	if
	implicit
	in
	int
	interface
	internal
	is
	lock
	long
	namespace
	new
	null
	object
	operator
	out
	override
	params
	partial
	private
	protected
	public
	readonly
	ref
	remove
	return
	sbyte
	sealed
	set
	short
	sizeof
	stackalloc
	static
	string
	struct
	switch
	this
	throw
	true
	try
	typeof
	uint
	ulong
	unchecked
	unsafe
	ushort
	using
	virtual
	void
	volatile
	where
	while
	yield
$End

$Rules
	Keyword ::= a b s t r a c t
	/.$BeginAction
		$setResult($_abstract);
	$EndAction./
	
	Keyword ::= a d d
	/.$BeginAction
		$setResult($_add);
	$EndAction./
	
	Keyword ::= a l i a s
	/.$BeginAction
		$setResult($_alias);
	$EndAction./
	
	Keyword ::= a s
	/.$BeginAction
		$setResult($_as);
	$EndAction./
	
	Keyword ::= b a s e
	/.$BeginAction
		$setResult($_base);
	$EndAction./
	
	Keyword ::= b o o l
	/.$BeginAction
		$setResult($_bool);
	$EndAction./
	
	Keyword ::= b r e a k
	/.$BeginAction
		$setResult($_break);
	$EndAction./
	
	Keyword ::= b y t e
	/.$BeginAction
		$setResult($_byte);
	$EndAction./
	
	Keyword ::= c a s e
	/.$BeginAction
		$setResult($_case);
	$EndAction./
	
	Keyword ::= c a t c h
	/.$BeginAction
		$setResult($_catch);
	$EndAction./
	
	Keyword ::= c h a r
	/.$BeginAction
		$setResult($_char);
	$EndAction./
	
	Keyword ::= c h e c k e d
	/.$BeginAction
		$setResult($_checked);
	$EndAction./
	
	Keyword ::= c l a s s
	/.$BeginAction
		$setResult($_class);
	$EndAction./
	
	Keyword ::= c o n s t
	/.$BeginAction
		$setResult($_const);
	$EndAction./
	
	Keyword ::= c o n t i n u e
	/.$BeginAction
		$setResult($_continue);
	$EndAction./
	
	Keyword ::= d e c i m a l
	/.$BeginAction
		$setResult($_decimal);
	$EndAction./
	
	Keyword ::= d e f a u l t
	/.$BeginAction
		$setResult($_default);
	$EndAction./
	
	Keyword ::= d e l e g a t e
	/.$BeginAction
		$setResult($_delegate);
	$EndAction./
	
	Keyword ::= d o
	/.$BeginAction
		$setResult($_do);
	$EndAction./
	
	Keyword ::= d o u b l e
	/.$BeginAction
		$setResult($_double);
	$EndAction./
	
	Keyword ::= e l s e
	/.$BeginAction
		$setResult($_else);
	$EndAction./
	
	Keyword ::= e n u m
	/.$BeginAction
		$setResult($_enum);
	$EndAction./
	
	Keyword ::= e v e n t
	/.$BeginAction
		$setResult($_event);
	$EndAction./
	
	Keyword ::= e x p l i c i t
	/.$BeginAction
		$setResult($_explicit);
	$EndAction./
	
	Keyword ::= e x t e r n
	/.$BeginAction
		$setResult($_extern);
	$EndAction./
	
	Keyword ::= f a l s e
	/.$BeginAction
		$setResult($_false);
	$EndAction./
	
	Keyword ::= f i n a l l y
	/.$BeginAction
		$setResult($_finally);
	$EndAction./
	
	Keyword ::= f i x e d
	/.$BeginAction
		$setResult($_fixed);
	$EndAction./
	
	Keyword ::= f l o a t
	/.$BeginAction
		$setResult($_float);
	$EndAction./
	
	Keyword ::= f o r
	/.$BeginAction
		$setResult($_for);
	$EndAction./
	
	Keyword ::= f o r e a c h
	/.$BeginAction
		$setResult($_foreach);
	$EndAction./
	
	Keyword ::= g e t
	/.$BeginAction
		$setResult($_get);
	$EndAction./
	
	Keyword ::= g o t o
	/.$BeginAction
		$setResult($_goto);
	$EndAction./
	
	Keyword ::= i f
	/.$BeginAction
		$setResult($_if);
	$EndAction./
	
	Keyword ::= i m p l i c i t
	/.$BeginAction
		$setResult($_implicit);
	$EndAction./
	
	Keyword ::= i n
	/.$BeginAction
		$setResult($_in);
	$EndAction./
	
	Keyword ::= i n t
	/.$BeginAction
		$setResult($_int);
	$EndAction./
	
	Keyword ::= i n t e r f a c e
	/.$BeginAction
		$setResult($_interface);
	$EndAction./
	
	Keyword ::= i n t e r n a l
	/.$BeginAction
		$setResult($_internal);
	$EndAction./
	
	Keyword ::= i s
	/.$BeginAction
		$setResult($_is);
	$EndAction./
	
	Keyword ::= l o c k
	/.$BeginAction
		$setResult($_lock);
	$EndAction./
	
	Keyword ::= l o n g
	/.$BeginAction
		$setResult($_long);
	$EndAction./
	
	Keyword ::= n a m e s p a c e
	/.$BeginAction
		$setResult($_namespace);
	$EndAction./
	
	Keyword ::= n e w
	/.$BeginAction
		$setResult($_new);
	$EndAction./
	
	Keyword ::= n u l l
	/.$BeginAction
		$setResult($_null);
	$EndAction./
	
	Keyword ::= o b j e c t
	/.$BeginAction
		$setResult($_object);
	$EndAction./
	
	Keyword ::= o p e r a t o r
	/.$BeginAction
		$setResult($_operator);
	$EndAction./
	
	Keyword ::= o u t
	/.$BeginAction
		$setResult($_out);
	$EndAction./
	
	Keyword ::= o v e r r i d e
	/.$BeginAction
		$setResult($_override);
	$EndAction./
	
	Keyword ::= p a r a m s
	/.$BeginAction
		$setResult($_params);
	$EndAction./
	
	Keyword ::= p a r t i a l
	/.$BeginAction
		$setResult($_partial);
	$EndAction./
	
	Keyword ::= p r i v a t e
	/.$BeginAction
		$setResult($_private);
	$EndAction./
	
	Keyword ::= p r o t e c t e d
	/.$BeginAction
		$setResult($_protected);
	$EndAction./
	
	Keyword ::= p u b l i c
	/.$BeginAction
		$setResult($_public);
	$EndAction./
	
	Keyword ::= r e a d o n l y
	/.$BeginAction
		$setResult($_readonly);
	$EndAction./
	
	Keyword ::= r e f
	/.$BeginAction
		$setResult($_ref);
	$EndAction./
	
	Keyword ::= r e m o v e
	/.$BeginAction
		$setResult($_remove);
	$EndAction./
	
	Keyword ::= r e t u r n
	/.$BeginAction
		$setResult($_return);
	$EndAction./
	
	Keyword ::= s b y t e
	/.$BeginAction
		$setResult($_sbyte);
	$EndAction./
	
	Keyword ::= s e a l e d
	/.$BeginAction
		$setResult($_sealed);
	$EndAction./
	
	Keyword ::= s e t
	/.$BeginAction
		$setResult($_set);
	$EndAction./
	
	Keyword ::= s h o r t
	/.$BeginAction
		$setResult($_short);
	$EndAction./
	
	Keyword ::= s i z e o f
	/.$BeginAction
		$setResult($_sizeof);
	$EndAction./
	
	Keyword ::= s t a c k a l l o c
	/.$BeginAction
		$setResult($_stackalloc);
	$EndAction./
	
	Keyword ::= s t a t i c
	/.$BeginAction
		$setResult($_static);
	$EndAction./
	
	Keyword ::= s t r i n g
	/.$BeginAction
		$setResult($_string);
	$EndAction./
	
	Keyword ::= s t r u c t
	/.$BeginAction
		$setResult($_struct);
	$EndAction./
	
	Keyword ::= s w i t c h
	/.$BeginAction
		$setResult($_switch);
	$EndAction./
	
	Keyword ::= t h i s
	/.$BeginAction
		$setResult($_this);
	$EndAction./
	
	Keyword ::= t h r o w
	/.$BeginAction
		$setResult($_throw);
	$EndAction./
	
	Keyword ::= t r u e
	/.$BeginAction
		$setResult($_true);
	$EndAction./
	
	Keyword ::= t r y
	/.$BeginAction
		$setResult($_try);
	$EndAction./
	
	Keyword ::= t y p e o f
	/.$BeginAction
		$setResult($_typeof);
	$EndAction./
	
	Keyword ::= u i n t
	/.$BeginAction
		$setResult($_uint);
	$EndAction./
	
	Keyword ::= u l o n g
	/.$BeginAction
		$setResult($_ulong);
	$EndAction./
	
	Keyword ::= u n c h e c k e d
	/.$BeginAction
		$setResult($_unchecked);
	$EndAction./
	
	Keyword ::= u n s a f e
	/.$BeginAction
		$setResult($_unsafe);
	$EndAction./
	
	Keyword ::= u s h o r t
	/.$BeginAction
		$setResult($_ushort);
	$EndAction./
	
	Keyword ::= u s i n g
	/.$BeginAction
		$setResult($_using);
	$EndAction./
	
	Keyword ::= v i r t u a l
	/.$BeginAction
		$setResult($_virtual);
	$EndAction./
	
	Keyword ::= v o i d
	/.$BeginAction
		$setResult($_void);
	$EndAction./
	
	Keyword ::= v o l a t i l e
	/.$BeginAction
		$setResult($_volatile);
	$EndAction./
	
	Keyword ::= w h e r e
	/.$BeginAction
		$setResult($_where);
	  $EndAction
	./

	Keyword ::= w h i l e
	/.$BeginAction
		$setResult($_while);
	  $EndAction
	./

	Keyword ::= y i e l d
	/.$BeginAction
		$setResult($_yield);
	  $EndAction
	./
$End
