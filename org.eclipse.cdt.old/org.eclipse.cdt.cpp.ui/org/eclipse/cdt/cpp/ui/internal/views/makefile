.SUFFIXES: .java .class

.java.class:
       javac %s

all: CppViewer.class CppPerspective.class ObjectsViewPart.class CppProjectsViewPart.class ProjectObjectsViewPart.class SystemObjectsViewPart.class  DetailsViewPart.class SuperDetailsViewPart.class DataStoreViewPart.class MinersViewPart.class SchemaViewPart.class LogViewPart.class ParsedSourceViewPart.class MinersPerspective.class RemoteProjectViewPart.class RemoteProjectNavigator.class ProjectViewPart.class CppCommandViewPart.class CppCommandViewer.class search_view targets_view 

clean:
	rm *.class

search_view:
	cd search
	nmake
	cd ..

targets_view:
	cd targets
	nmake
	cd ..
