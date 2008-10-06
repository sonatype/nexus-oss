package org.sonatype.plexus.rest;

import org.codehaus.plexus.PlexusTestCase;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestTest
    extends PlexusTestCase
{
    public void testRest()
        throws Exception
    {
        Component component = new Component();

        component.getServers().add( Protocol.HTTP, 8182 );

        PlexusRestletApplicationBridge app = (PlexusRestletApplicationBridge) getContainer().lookup( Application.class );

        component.getDefaultHost().attach( app );

        component.start();

        component.stop();
    }
}
