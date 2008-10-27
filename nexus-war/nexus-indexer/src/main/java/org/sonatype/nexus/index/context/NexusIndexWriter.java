/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamas Cservenak (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.context;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

public class NexusIndexWriter
    extends IndexWriter
{
    private boolean closed;

    public NexusIndexWriter( Directory d, Analyzer a, boolean create )
        throws CorruptIndexException,
            LockObtainFailedException,
            IOException
    {
        // cstamas: was autoCommit = false!
        super( d, true, a, create );

        this.closed = false;
    }

    @Override
    public void close()
        throws CorruptIndexException,
            IOException
    {
        super.close();

        this.closed = true;
    }

    public boolean isClosed()
    {
        return closed;
    }

}
