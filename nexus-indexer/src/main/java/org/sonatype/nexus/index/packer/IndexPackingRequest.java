package org.sonatype.nexus.index.packer;

import java.io.File;

import org.sonatype.nexus.index.context.IndexingContext;

public class IndexPackingRequest
{
    private static final int MAX_CHUNKS = 30;

    private IndexingContext context;

    private File targetDir;

    private boolean createIncrementalChunks;

    private IndexChunker indexChunker;

    private int maxIndexChunks;

    public IndexPackingRequest( IndexingContext context, File targetDir )
    {
        this.context = context;

        this.targetDir = targetDir;

        this.createIncrementalChunks = true;

        this.indexChunker = new DailyIndexChunker();

        this.maxIndexChunks = MAX_CHUNKS;
    }

    public IndexingContext getContext()
    {
        return context;
    }

    public void setContext( IndexingContext context )
    {
        this.context = context;
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

    public IndexChunker getIndexChunker()
    {
        return indexChunker;
    }

    public void setIndexChunker( IndexChunker indexChunker )
    {
        this.indexChunker = indexChunker;
    }

    public int getMaxIndexChunks()
    {
        return maxIndexChunks;
    }

    public void setMaxIndexChunks( int maxIndexChunks )
    {
        this.maxIndexChunks = maxIndexChunks;
    }

}
