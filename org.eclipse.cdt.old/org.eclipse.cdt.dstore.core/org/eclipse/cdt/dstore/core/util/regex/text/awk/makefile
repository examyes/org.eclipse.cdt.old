.SUFFIXES: .java .class

.java.class:
       javac %s

all:   AwkCompiler.class                  \
       AwkMatcher.class                   \
       AwkMatchResult.class               \
       AwkPattern.class                   \
       AwkStreamInput.class               \
       CatNode.class                      \
       CharacterClassNode.class           \
       EpsilonNode.class                  \
       LeafNode.class                     \
       NegativeCharacterClassNode.class   \
       OrNode.class                       \
       PlusNode.class                     \
       QuestionNode.class                 \
       StarNode.class                     \
       SyntaxNode.class                   \
       SyntaxTree.class                   \
       TokenNode.class

clean:
	rm *.class
