/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * An extension of <a
 * href="http://lucene.apache.org/java/2_4_0/api/core/org/apache/lucene/index/IndexWriter.html">Lucene IndexWriter</a>
 * to allow to track if writer is closed
 */
public class NexusIndexWriter
    extends IndexWriter
{
    private boolean closed;

    public NexusIndexWriter( final Directory directory, final Analyzer analyzer, boolean create )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        this( directory, analyzer, create, true /* autoCommit */);
    }

    public NexusIndexWriter( final Directory directory, final Analyzer analyzer, boolean create, boolean autoCommit )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        super( directory, autoCommit, analyzer, create );

        this.closed = false;
    }

    @Override
    public void close()
        throws CorruptIndexException, IOException
    {
        super.close();

        this.closed = true;
    }

    public boolean isClosed()
    {
        return closed;
    }
}
