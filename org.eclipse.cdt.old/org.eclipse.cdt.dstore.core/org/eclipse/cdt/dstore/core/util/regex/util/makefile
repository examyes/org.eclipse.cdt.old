.SUFFIXES: .java .class

.java.class:
       javac %s

all:   Cache.class \
       CacheFIFO.class \
       CacheFIFO2.class \
       CacheLRU.class \
       CacheRandom.class \
       GenericCache.java \
       GenericCacheEntry.class

clean:
	rm *.class
