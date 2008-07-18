package org.sonatype.nexus.integrationtests.proxy;

import java.io.File;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public abstract class AbstractNexusProxyIntegrationTest
    extends AbstractNexusIntegrationTest
{

    private String baseURL = null;

    private String localStorageDir = null;

    protected AbstractNexusProxyIntegrationTest()
    {
        this( "release-proxy-repo-1" );
    }

    protected AbstractNexusProxyIntegrationTest( String testRepositoryId )
    {
        super( testRepositoryId );

        ResourceBundle rb = ResourceBundle.getBundle( "baseTest" );

        this.baseURL = rb.getString( "proxy.repo.base.url" );
        this.localStorageDir = rb.getString( "proxy.repo.base.dir" );

    }

    @Before
    public void startProxy()
        throws Exception
    {
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE );
        server.start();
    }

    @After
    public void stopProxy()
        throws Exception
    {
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE );
        server.stop();
    }

    public File getLocalFile( String repositoryId, Gav gav )
    {
        return this.getLocalFile( repositoryId, gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                  gav.getExtension() );
    }

    public File getLocalFile( String repositoryId, String groupId, String artifact, String version, String type )
    {
        File result =
            new File( this.localStorageDir, repositoryId + "/" + groupId.replace( '.', '/' ) + "/" + artifact + "/"
                + version + "/" + artifact + "-" + version + "." + type );
        System.out.println( "Returning file: " + result );
        return result;
    }

    // TODO: Refactor this into the AbstractNexusIntegrationTest or some util class, to make more generic

    public void setBlockProxy( String nexusBaseUrl, String repoId, boolean block )
    {

        String serviceURI = nexusBaseUrl + "service/local/repositories/" + repoId + "/status?undefined";

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( Method.PUT );

        // unblock string
        String blockOrNotCommand = "\"unavailable\",\"proxyMode\":\"allow\"";
        // change to block if true
        if ( block == true )
        {
            blockOrNotCommand = "\"available\",\"proxyMode\":\"blockedManual\"";
        }

        request.setEntity( "{\"data\":{\"id\":\"" + repoId
            + "\",\"repoType\":\"proxy\",\"localStatus\":\"inService\",\"remoteStatus\":" + blockOrNotCommand + "}}",
                           MediaType.APPLICATION_JSON );

        Client client = new Client( Protocol.HTTP );

        Response response = client.handle( request );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not unblock proxy: " + repoId + ", status: " + response.getStatus().getName() + " ("
                + response.getStatus().getCode() + ") - " + response.getStatus().getDescription() );
        }
    }

    public void setOutOfServiceProxy( String nexusBaseUrl, String repoId, boolean outOfService )
    {

        String serviceURI = nexusBaseUrl + "service/local/repositories/" + repoId + "/status?undefined";

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( Method.PUT );

        // unblock string
        String servicePart = outOfService ? "outOfService" : "inService";

        request.setEntity( "{\"data\":{\"id\":\"" + repoId + "\",\"repoType\":\"proxy\",\"localStatus\":\""
            + servicePart + "\"}}", MediaType.APPLICATION_JSON );

        Client client = new Client( Protocol.HTTP );

        Response response = client.handle( request );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not set proxy out of service status (Error: "+response.getStatus().getCode() + "-"+ response.getStatus().getDescription()+ "): " + repoId );
        }
    }
}
