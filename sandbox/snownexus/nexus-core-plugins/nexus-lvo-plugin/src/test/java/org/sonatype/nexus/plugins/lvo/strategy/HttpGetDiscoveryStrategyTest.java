package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.net.ServerSocket;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;

public class HttpGetDiscoveryStrategyTest
    extends PlexusTestCase
{

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

    public void testSimple()
        throws Exception
    {

    }

}
