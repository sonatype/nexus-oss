package org.sonatype.plexus.rest.jaxrs;

import javax.ws.rs.core.Application;

import org.codehaus.plexus.PlexusTestCase;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;

public class JaxRsTest
    extends PlexusTestCase
{
    public Application getApplication()
        throws Exception
    {
        return lookup( Application.class );
    }

    public PlexusObjectFactory getPlexusObjectFactory()
        throws Exception
    {
        return lookup( PlexusObjectFactory.class );
    }

    @Test
    public void testSimple()
        throws Exception
    {
        Component comp = new Component();
        Server server = comp.getServers().add( Protocol.HTTP, 8182 );

        // create JAX-RS runtime environment
        JaxRsApplication application = new JaxRsApplication( comp.getContext() );

        // plexus goes here
        application.setObjectFactory( getPlexusObjectFactory() );

        // attach ApplicationConfig
        application.add( getApplication() );

        // Attach the application to the component and start it
        comp.getDefaultHost().attach( application );
        comp.start();

        System.out.println( "Server started on port " + server.getPort() );
        System.out.println( "Press key to stop server" );
        System.in.read();
        System.out.println( "Stopping server" );
        comp.stop();
        System.out.println( "Server stopped" );

    }

}
