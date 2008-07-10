package org.sonatype.nexus.test.proxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.appbooter.ctl.ControlConnectionException;
import org.sonatype.appbooter.ctl.ControllerClient;
import org.sonatype.nexus.artifact.Gav;

public class ProxyRepo
{
    private String baseURL = null;

    private String localStorageDir = null;

    private int controlPort = -1;

    private ControllerClient manager;

    private static ProxyRepo INSTANCE = null;

    private static final int MANAGER_WAIT_TIME = 500;

    private static final int TEST_CONNECTION_TIMEOUT = 3000;

    private static final int TEST_CONNECTION_ATTEMPTS = 5;

    private ProxyRepo()
    {
        ResourceBundle rb = ResourceBundle.getBundle( "baseTest" );

        this.baseURL = rb.getString( "proxy.repo.base.url" );
        this.localStorageDir = rb.getString( "proxy.repo.base.dir" );

        String controlPortString = rb.getString( "proxy.repo.control.port" );
        this.controlPort = Integer.parseInt( controlPortString );
    }

    public synchronized static ProxyRepo getInstance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new ProxyRepo();
        }
        return INSTANCE;
    }

    public synchronized static void stop()
    {
        ProxyRepo proxyRepo = getInstance();

        try
        {
            proxyRepo.getManager().stop();

            Thread.sleep( MANAGER_WAIT_TIME );

            // Note calling testConnection w/ only 1 attempt, because just 1 timeout will do
            boolean notShutdown = proxyRepo.testConnection( 1, TEST_CONNECTION_TIMEOUT );

            if ( notShutdown )
            {
                throw new RuntimeException( "The jetty http server did not shutdown, call to manager.stop() failed." );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }

    }

    public synchronized static void start()
    {
        ProxyRepo proxyRepo = getInstance();

        try
        {
            proxyRepo.getManager().start();

            Thread.sleep( MANAGER_WAIT_TIME );

            // Note calling testConnection w/ only 1 attempt, because just 1 timeout will do
            boolean started = proxyRepo.testConnection( TEST_CONNECTION_ATTEMPTS, TEST_CONNECTION_TIMEOUT );

            if ( !started )
            {
                throw new RuntimeException( "The jetty http server did not startup, call to manager.start() failed." );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }

    }

    private boolean testConnection( int attempts, int timeout )
    {
        if ( attempts < 1 )
        {
            throw new IllegalArgumentException( "Must have at least 1 attempt" );
        }

        if ( timeout < 1 )
        {
            throw new IllegalArgumentException( "Must have at least 1 millisecond timeout" );
        }

        boolean result = false;

        for ( int i = 0; i < attempts; i++ )
        {
            try
            {
                URL url = new URL( this.baseURL );
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout( timeout );
                InputStream stream = connection.getInputStream();
                stream.close();
                result = true;
                break;
            }
            catch ( IOException e )
            {
                // Just break out to skip the unnecessary sleep
                if ( ( i + 1 ) == attempts )
                {
                    break;
                }
                try
                {
                    Thread.sleep( timeout );
                }
                catch ( InterruptedException e1 )
                {
                }
            }
        }

        return result;
    }

    public String getBaseURL()
    {
        return baseURL;
    }

    public void setBaseURL( String baseURL )
    {
        this.baseURL = baseURL;
    }

    public String getLocalStorageDir()
    {
        return localStorageDir;
    }

    public void setLocalStorageDir( String localStorageDir )
    {
        this.localStorageDir = localStorageDir;
    }

    /**
     * Exposed so the AbstractNexusIntegrationTest can control this.
     * 
     * @return
     */
    public ControllerClient getManager()
    {
        if ( this.manager == null )
        {
            // if this throws the test will fail...
            try
            {
                manager = new ControllerClient( this.controlPort );
                manager.shutdownOnClose();
                Thread.sleep( MANAGER_WAIT_TIME );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
        return manager;
    }

    public void disconnect( boolean detach )
    {
        // if detach is true, then the tests passed
        try
        {
            if ( detach && this.manager != null )
            {
                this.manager.detachOnClose();
                this.manager.close();
                this.manager = null;
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public File getLocalFile( String repositoryId, Gav gav)
    {
        return this.getLocalFile( repositoryId, gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                  gav.getExtension() );
    }

    public File getLocalFile( String repositoryId, String groupId, String artifact, String version, String type )
    {
        return new File( this.localStorageDir, repositoryId + "/" + groupId.replace( '.', '/' ) + "/" + artifact + "/"
            + version + "/" + artifact + "-" + version + "." + type );
    }
    
    public void setBlockProxy( String nexusBaseUrl, String repoId, boolean block )
    {

        String serviceURI = nexusBaseUrl + "service/local/repositories/" + repoId + "/status?undefined";

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( Method.PUT );
        
        // unblock string
        String blockOrNotCommand = "\"unavailable\",\"proxyMode\":\"allow\"";
        // change to block if true
        if( block == true )
        {
            blockOrNotCommand = "\"available\",\"proxyMode\":\"blockedManual\"";
        }
        
        request.setEntity(
                           "{\"data\":{\"id\":\""
                               + repoId
                               + "\",\"repoType\":\"proxy\",\"localStatus\":\"inService\",\"remoteStatus\":"+ blockOrNotCommand +"}}",
                           MediaType.APPLICATION_JSON );

        Client client = new Client( Protocol.HTTP );

        Response response = client.handle( request );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not unblock proxy: " + repoId );
        }
    }

}
