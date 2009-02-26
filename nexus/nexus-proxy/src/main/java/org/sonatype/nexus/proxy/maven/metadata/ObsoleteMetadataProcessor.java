package org.sonatype.nexus.proxy.maven.metadata;

/**
 * Used to remove metadata files
 * 
 * @author juven
 */
public class ObsoleteMetadataProcessor
    extends AbstractMetadataProcessor
{

    public ObsoleteMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        super( metadataHelper );
    }

    /**
     * always return false, so the metadata will be removed
     */
    @Override
    protected boolean isMetadataCorrect( String path )
        throws Exception
    {
        return false;
    }

    @Override
    protected void postProcessMetadata()
    {
        // do nothing
    }

    @Override
    protected void processMetadata( String path )
        throws Exception
    {
        // do nothing
    }

    @Override
    protected boolean shouldProcessMetadata( String path )
    {
        return true;
    }

}
