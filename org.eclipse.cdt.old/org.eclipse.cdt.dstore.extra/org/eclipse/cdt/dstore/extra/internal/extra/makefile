.SUFFIXES: .java .class

.java.class:
       javac %s

all: DomainEvent.class \
	DomainNotifier.class \
	IDomainListener.class \
	IDomainNotifier.class \
	IElement.class \
	IDataElement.class \
	IPropertySource.class \
	PropertySource.class \
	IDesktopElement.class \
	DesktopElement.class

clean:
	rm *.class
