.SUFFIXES: .java .class

.java.class:
       javac %s

all:   AwkFilenameFilter.class \
       GlobFilenameFilter.class \
       Perl5FilenameFilter.class RegexFilenameFilter.class

clean:
	rm *.class
