/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.packer;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * An index packing request.
 */
public class IndexPackingRequest
{
    private static final int MAX_CHUNKS = 30;

    private IndexingContext context;

    private File targetDir;

    private boolean createIncrementalChunks;

    private boolean createChecksumFiles;

    private int maxIndexChunks;

    private Collection<IndexFormat> formats;

    public IndexPackingRequest( IndexingContext context, File targetDir )
    {
        this.context = context;

        this.targetDir = targetDir;

        this.createIncrementalChunks = true;

        this.createChecksumFiles = false;

        this.maxIndexChunks = MAX_CHUNKS;

        this.formats = Arrays.asList( IndexFormat.FORMAT_LEGACY, IndexFormat.FORMAT_V1 );
    }

    public IndexingContext getContext()
    {
        return context;
    }

    public void setContext( IndexingContext context )
    {
        this.context = context;
    }

    /**
     * Sets index formats to be created
     */
    public void setFormats( Collection<IndexFormat> formats )
    {
        this.formats = formats;
    }

    /**
     * Returns index formats to be created.
     */
    public Collection<IndexFormat> getFormats()
    {
        return formats;
    }

    public File getTargetDir()
    {
        return targetDir;
    }

    public void setTargetDir( File targetDir )
    {
        this.targetDir = targetDir;
    }

    public boolean isCreateIncrementalChunks()
    {
        return createIncrementalChunks;
    }

    public void setCreateIncrementalChunks( boolean createIncrementalChunks )
    {
        this.createIncrementalChunks = createIncrementalChunks;
    }

    public boolean isCreateChecksumFiles()
    {
        return createChecksumFiles;
    }

    public void setCreateChecksumFiles( boolean createChecksumFiles )
    {
        this.createChecksumFiles = createChecksumFiles;
    }

    public int getMaxIndexChunks()
    {
        return maxIndexChunks;
    }

    public void setMaxIndexChunks( int maxIndexChunks )
    {
        this.maxIndexChunks = maxIndexChunks;
    }

    /**
     * Index format enumeration.
     */
    public static enum IndexFormat
    {
        FORMAT_LEGACY, FORMAT_V1;

    }
}
