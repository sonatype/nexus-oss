package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

public class ExtendedIndexWriter
    extends IndexWriter
{
    public ExtendedIndexWriter( Directory d, boolean autoCommit, Analyzer a, boolean create )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        super( d, autoCommit, a, create );
    }
    
    public boolean hasUncommitedChanges()
    {
        return pendingCommit != null;
    }
}
