.SUFFIXES: .java .class

.java.class:
       javac %s

all:   perl                                  \
       awk                                   \
       regex                                 \
       DefaultMatchAction.class               \
       GenericPatternCache.class              \
       GlobCompiler.class                     \
       MalformedCachePatternException.class   \
       MatchAction.class                      \
       MatchActionInfo.class                  \
       MatchActionProcessor.class             \
       PatternCache.class                     \
       PatternCacheFIFO.class                 \
       PatternCacheFIFO2.class                \
       PatternCacheLRU.class                  \
       PatternCacheRandom.class

perl:
	cd perl
	$(MAKE)
	cd ..

awk:
	cd awk
	$(MAKE)
	cd ..

regex:
	cd regex
	$(MAKE)
	cd ..


clean:
	rm *.class
