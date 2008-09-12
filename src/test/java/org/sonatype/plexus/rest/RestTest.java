package org.sonatype.plexus.rest;

import org.codehaus.plexus.PlexusTestCase;
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

        PlexusRestletApplicationBridge app = new PlexusRestletApplicationBridge( component
            .getContext().createChildContext() );

        app.setPlexusContainer( getContainer() );

        component.getDefaultHost().attach( app );

        component.start();
    }
}
