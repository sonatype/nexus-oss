package org.sonatype.nexus.proxy.maven.metadata;

import org.apache.maven.mercury.repository.metadata.Metadata;

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
    protected boolean isMetadataCorrect( Metadata oldMd, String path )
        throws Exception
    {
        return false;
    }

    @Override
    public void postProcessMetadata( String path )
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
