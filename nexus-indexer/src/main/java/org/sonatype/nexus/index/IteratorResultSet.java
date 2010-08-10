package org.sonatype.nexus.index;

import java.util.Iterator;

/**
 * IteratorResultSet, that returns the result hit's in "iterator-like fashion", instead lifting all the hits into memory
 * (and bashing IO and RAM consumption). This makes search making client memory usa thin, and makes possible to
 * implement things like "streaming" results etc. The result set is java.util.Iterator, but is made also
 * java.lang.Iterable to make it possible to take part in operations like "for-each", but it will usually return itself
 * as Iterator.
 * 
 * @author cstamas
 */
public interface IteratorResultSet
    extends Iterator<ArtifactInfo>, Iterable<ArtifactInfo>
{
    /**
     * Returns the up-to-date number of the actual number of loaded Lucene Documents that were converted into
     * ArtifactInfo object until last next() invocation. Warning: this method will return ALL touched/loaded document
     * count, even those that are filtered out and NOT returned by iterator's next() method!
     * 
     * @return total number of processed ArtifactInfos so far
     */
    int getTotalProcessedArtifactInfoCount();
}
