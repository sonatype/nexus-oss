package org.sonatype.nexus.repositories.metadata;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public class RemoteMirrorDownloadTest
    extends PlexusTestCase
{

    private ServletServer server;

    protected static final String PROXY_SERVER_PORT = "proxy.server.port";

    @Override
    protected void customizeContext( Context ctx )
    {
        ctx.put( PROXY_SERVER_PORT, String.valueOf( allocatePort() ) );
    }

    private int allocatePort()
    {
        ServerSocket ss;
        try
        {
            ss = new ServerSocket( 0 );
        }
        catch ( IOException e )
        {
            return 0;
        }
        int port = ss.getLocalPort();
        try
        {
            ss.close();
        }
        catch ( IOException e )
        {
            // does it matter?
            fail( "Error allocating port " + e.getMessage() );
        }
        return port;
    }

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
