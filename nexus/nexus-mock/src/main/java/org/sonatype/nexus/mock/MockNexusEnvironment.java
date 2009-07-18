package org.sonatype.nexus.mock;

import java.io.File;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.sonatype.nexus.mock.util.ContainerUtil;

public class MockNexusEnvironment
{
    private Server server;

    private PlexusContainer container;

    public static void main( String[] args )
        throws Exception
    {
        File webappRoot = new File( "../nexus-webapp/src/main/webapp" );
        if ( !webappRoot.exists() )
        {
            webappRoot = new File( "target/nexus-ui" );
        }

        System.setProperty( "plexus-index.template.file", "templates/index-debug.vm" );

        // create one
        ContainerConfiguration cc = new DefaultContainerConfiguration();
        cc.setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) );
        cc.setContext( ContainerUtil.createContainerContext() );
        cc.addComponentDiscoveryListener( new InhibitingComponentDiscovererListener() );

        DefaultPlexusContainer plexusContainer = new DefaultPlexusContainer( cc );

        MockNexusEnvironment e = new MockNexusEnvironment( 12345, "/nexus", webappRoot, plexusContainer );
        e.start();
    }

    public static Server createSimpleJettyServer( int port )
    {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();

        connector.setHost( null );

        connector.setPort( port );

        server.addConnector( connector );

        return server;
    }

    public MockNexusEnvironment( int port, String contextPath, File webappRoot, PlexusContainer container )
        throws Exception
    {
        this( createSimpleJettyServer( port ), contextPath, webappRoot, container );
    }

    public MockNexusEnvironment( Server server, String contextPath, File webappRoot, PlexusContainer container )
        throws Exception
    {
        this.server = server;
        this.container = container;

        addNexus( server, contextPath, webappRoot, container );
    }

    public Server getServer()
    {
        return server;
    }

    public void start()
        throws Exception
    {
        getServer().start();
    }

    public void stop()
        throws Exception
    {
        getServer().stop();
    }

    public void addNexus( Server server, String contextPath, File webappRoot, PlexusContainer container )
        throws Exception
    {
        // prepare config
        FileUtils.copyFile( new File( "src/test/resources/nexus-1.xml" ), new File( "target/nexus-work/conf/nexus.xml" ) );
        FileUtils.copyFile( new File( "src/test/resources/security-1.xml" ),
                            new File( "target/nexus-work/conf/security.xml" ) );

        // add mock nexus
        ContextHandlerCollection ctxHandler = new ContextHandlerCollection();

        WebAppContext webapp = new WebAppContext( ctxHandler, webappRoot.getAbsolutePath(), contextPath );

        // spoof in our simplified web.xml
        webapp.setDescriptor( new File( "target/test-classes/nexus-ui/WEB-INF/web.xml" ).getAbsolutePath() );

        // Put the container for the application into the servlet context

        webapp.setAttribute( PlexusConstants.PLEXUS_KEY, container );

        webapp.setClassLoader( container.getContainerRealm() );

        ctxHandler.mapContexts();

        getServer().addHandler( ctxHandler );
    }

    public PlexusContainer getPlexusContainer()
    {
        return container;
    }
}
