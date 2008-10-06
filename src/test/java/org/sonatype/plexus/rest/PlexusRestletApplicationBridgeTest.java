package org.sonatype.plexus.rest;

import org.codehaus.plexus.PlexusTestCase;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class PlexusRestletApplicationBridgeTest
    extends PlexusTestCase
{
    public void testRest()
        throws Exception
    {
        Component component = new Component();

        component.getServers().add( Protocol.HTTP, 8182 );

        TestApplication app = (TestApplication) getContainer().lookup( Application.class, "test" );

        component.getDefaultHost().attach( app );

        component.start();

        TestClient client = new TestClient();

        assertEquals( "tokenA", client.request( "http://localhost:8182/tokenA" ) );

        assertEquals( "tokenB", client.request( "http://localhost:8182/tokenB" ) );

        assertEquals( "manual", client.request( "http://localhost:8182/manual" ) );

        component.stop();
    }
}
