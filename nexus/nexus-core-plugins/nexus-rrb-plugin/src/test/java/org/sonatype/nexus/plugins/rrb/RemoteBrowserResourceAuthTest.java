package org.sonatype.nexus.plugins.rrb;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.restlet.data.Request;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.AbstractPluginTestCase;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;
import org.sonatype.plexus.rest.resource.PlexusResource;

public class RemoteBrowserResourceAuthTest
    extends AbstractPluginTestCase
{
    private ServletServer server = null;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.server = this.lookup( ServletServer.class );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        int port = 0;
        ServerSocket socket = null;
        try
        {
            socket = new ServerSocket( 0 );
            port = socket.getLocalPort();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            Assert.fail( "Could not find free port: " + e.getMessage() );
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                Assert.fail( "Could not close socket: " + e.getMessage() );
            }
        }

        context.put( "jetty-port", Integer.toString( port ) );
        context.put( "resource-base", "target" );

    }

    public void testSiteWithAuth()
        throws Exception
    {
        String remoteUrl = server.getUrl( "auth-test/" );

        String repoId = "testSiteWithAuth";
        RepositoryRegistry repoRegistry = this.lookup( RepositoryRegistry.class );

        TemplateProvider templateProvider =
            this.lookup( TemplateProvider.class, DefaultRepositoryTemplateProvider.PROVIDER_ID );
        Maven2ProxyRepositoryTemplate template =
            (Maven2ProxyRepositoryTemplate) templateProvider.getTemplateById( "default_proxy_release" );
        template.getCoreConfiguration().getConfiguration( true ).setId( repoId );
        template.getCoreConfiguration().getConfiguration( true ).setName( repoId + "-name" );
        template.getCoreConfiguration().getConfiguration( true ).setIndexable( false ); // disable index
        template.getCoreConfiguration().getConfiguration( true ).setSearchable( false ); // disable index

        M2Repository m2Repo = (M2Repository) template.create();
        repoRegistry.addRepository( m2Repo );

        m2Repo.setRemoteUrl( remoteUrl );
        m2Repo.setRemoteAuthenticationSettings( new UsernamePasswordRemoteAuthenticationSettings( "admin", "admin" ) );
        m2Repo.commitChanges();

        // now call the REST resource
        Request request = new Request();
        request.setResourceRef( "http://localhost/resttest" );
        request.getResourceRef().addQueryParameter( "id", repoId ).addQueryParameter( "remoteurl", remoteUrl );

        PlexusResource plexusResource = this.lookup( PlexusResource.class, RemoteBrowserResource.class.getName() );
        String jsonString = plexusResource.get( null, request, null, null ).toString();

        // TODO: do some better validation then this
        Assert.assertTrue( jsonString.contains( "/auth-test/classes/" ) );
        Assert.assertTrue( jsonString.contains( "/auth-test/test-classes/" ) );

    }

    @Override
    protected void tearDown()
        throws Exception
    {

        if ( this.server != null )
        {
            this.server.stop();
        }

        super.tearDown();
    }

}
