all: CppEditor.class EditorActionDelegate.class BreakpointRulerAction.class CppDocumentProvider.class CppMarkerAnnotation.class CppMarkerAnnotationModel.class IConstants.class Trace.class contentoutliner contributor codeassist

CppEditor.class: CppEditor.java
	javac $(FLAGS) CppEditor.java

BreakpointRulerAction.class: BreakpointRulerAction.java
	javac $(FLAGS) BreakpointRulerAction.java

EditorActionDelegate.class: EditorActionDelegate.java
	javac $(FLAGS) EditorActionDelegate.java

CppDocumentProvider.class: CppDocumentProvider.java
	javac $(FLAGS) CppDocumentProvider.java

CppMarkerAnnotation.class: CppMarkerAnnotation.java
	javac $(FLAGS) CppMarkerAnnotation.java

CppMarkerAnnotationModel.class: CppMarkerAnnotationModel.java
	javac $(FLAGS) CppMarkerAnnotationModel.java

IConstants.class: IConstants.java
	javac $(FLAGS) IConstants.java

Trace.class: Trace.java
	javac $(FLAGS) Trace.java

contentoutliner:	
	cd contentoutliner
	$(MAKE) all
	cd ..

contributor:	
	cd contributor
	$(MAKE) all
	cd ..

codeassist:	
	cd codeassist
	$(MAKE) all
	cd ..