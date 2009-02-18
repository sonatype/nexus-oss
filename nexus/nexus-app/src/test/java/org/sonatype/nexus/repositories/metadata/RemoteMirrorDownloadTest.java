package org.sonatype.nexus.repositories.metadata;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public class RemoteMirrorDownloadTest
    extends PlexusTestCase
{
    
    private ServletServer server;

    public void testRemoteMetadataDownload() throws Exception
    {
        NexusRepositoryMetadataHandler repoMetadata = this.lookup( NexusRepositoryMetadataHandler.class );
        
        String url = this.server.getUrl( "repo-with-mirror" + "/" );
        
        RepositoryMetadata metadata = repoMetadata.readRemoteRepositoryMetadata( url );
        
        Assert.assertNotNull( metadata );
        
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        server = this.lookup( ServletServer.class );
        server.start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        if( server != null )
        {
            server.stop();
        }
        
        super.tearDown();
    }    
    
    
    
}
