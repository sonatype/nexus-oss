/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.
 *
 * This file is part of Nexus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.DefaultArchiverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.sonatype.appbooter.ctl.ControlConnectionException;
import org.sonatype.appbooter.ctl.ControllerClient;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.proxy.ProxyRepo;

public abstract class AbstractNexusIntegrationTest
{
    private String nexusUrl;

    private PlexusContainer container;

    private Map context;

    private String baseNexusUrl;

    private String nexusBundleDir;

    private static ControllerClient manager;

    private static boolean detach = false;

    private static Object waitObj = new Object();

    private static final int TEST_CONNECTION_ATTEMPTS = 5;

    private static final int TEST_CONNECTION_TIMEOUT = 3000;

    private static final int MANAGER_WAIT_TIME = 500;

    public static final String REPOSITORY_RELATIVE_URL = "content/repositories/";

    public static final String GROUP_REPOSITORY_RELATIVE_URL = "content/groups/";

    private static String basedir;

    // TODO: need to configure this via the pom
    private static boolean STANDALONE_MODE = false;

    /**
     * Sets up the infrastructure for the tests.
     * 
     * @param nexusUrl The relative url i.e. 'content/groups/nexus-test/'
     */
    public AbstractNexusIntegrationTest( String nexusUrl )
    {
        // Using ResourceBundle here because its easy
        ResourceBundle rb = ResourceBundle.getBundle( "baseTest" );

        this.baseNexusUrl = rb.getString( "nexus.base.url" );
        this.nexusBundleDir = rb.getString( "nexus.bundle.dir" );

        this.nexusUrl = baseNexusUrl + nexusUrl;
        setupContainer();
    }

    protected void complete()
    {
        detach = true;
    }

    @BeforeClass
    public static void oncePerClassSetUp()
    {
        if ( !STANDALONE_MODE )
        {
            synchronized ( waitObj )
            {
                try
                {
                    detach = false;
                    if ( manager != null )
                    {
                        manager.close();

                    }

                    String controlPortString = ResourceBundle.getBundle( "baseTest" ).getString( "nexus.control.port" );
                    // if this throws the test will fail...
                    manager = new ControllerClient( Integer.parseInt( controlPortString ) );
                    manager.shutdownOnClose();
                    Thread.sleep( MANAGER_WAIT_TIME );
                }
                catch ( UnknownHostException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to initialize test.", e );
                }
                catch ( ControlConnectionException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to initialize test.", e );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to initialize test.", e );
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to initialize test.", e );
                }
            }
        }
    }

    @AfterClass
    public static void oncePerClassTearDown()
    {
        if ( !STANDALONE_MODE )
        {
            synchronized ( waitObj )
            {
                try
                {
                    // if detach is true, then the tests passed
                    if ( detach )
                    {
                        manager.detachOnClose();
                    }

                    manager.close();
                    manager = null;

                    ProxyRepo.getInstance().disconnect( detach );

                    Thread.sleep( MANAGER_WAIT_TIME );
                }
                catch ( UnknownHostException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to teardown test.", e );
                }
                catch ( ControlConnectionException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to teardown test.", e );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to teardown test.", e );
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                    throw new RuntimeException( "Unable to teardown test.", e );
                }
            }
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
                URL url = new URL( nexusUrl );
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

    protected void stopNexus()
    {
        try
        {
            manager.stop();

            Thread.sleep( MANAGER_WAIT_TIME );

            // Note calling testConnection w/ only 1 attempt, becuase just 1 timeout will do
            assertFalse( testConnection( 1, TEST_CONNECTION_TIMEOUT ) );
        }
        catch ( UnknownHostException e )
        {
            e.printStackTrace();
            fail( "Exception stopping nexus" );
        }
        catch ( ControlConnectionException e )
        {
            e.printStackTrace();
            fail( "Exception stopping nexus" );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "Exception stopping nexus" );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
            fail( "Exception stopping nexus" );
        }
    }

    protected void startNexus()
    {
        try
        {
            manager.start();

            assertTrue( testConnection( TEST_CONNECTION_ATTEMPTS, TEST_CONNECTION_TIMEOUT ) );
        }
        catch ( UnknownHostException e )
        {
            e.printStackTrace();
            fail( "Exception starting nexus" );
        }
        catch ( ControlConnectionException e )
        {
            e.printStackTrace();
            fail( "Exception starting nexus" );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "Exception starting nexus" );
        }
    }

    protected void restartNexus()
        throws IOException
    {
        stopNexus();
        startNexus();
    }

    protected File downloadArtifact( Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                      targetDirectory );
    }

    protected String getRelitiveArtifactPath( Gav gav )
        throws FileNotFoundException
    {
        return gav.getGroupId().replace( '.', '/' ) + "/" + gav.getArtifactId() + "/" + gav.getVersion() + "/"
            + gav.getArtifactId() + "-" + gav.getVersion() + "." + gav.getExtension();
    }

    protected File downloadArtifact( String groupId, String artifact, String version, String type,
                                     String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( this.nexusUrl, groupId, artifact, version, type, targetDirectory );
    }

    protected File downloadArtifactFromRepository( String repoId, Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( this.baseNexusUrl + REPOSITORY_RELATIVE_URL + repoId + "/", gav.getGroupId(),
                                      gav.getArtifactId(), gav.getVersion(), gav.getExtension(), targetDirectory );
    }

    protected File downloadArtifactFromGroup( String groupId, Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( this.baseNexusUrl + GROUP_REPOSITORY_RELATIVE_URL + groupId + "/",
                                      gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                      targetDirectory );
    }

    protected File downloadArtifact( String baseUrl, String groupId, String artifact, String version, String type,
                                     String targetDirectory )
        throws IOException
    {
        URL url =
            new URL( baseUrl + groupId.replace( '.', '/' ) + "/" + artifact + "/" + version + "/" + artifact + "-"
                + version + "." + type );
                
        return this.downloadFile( url, targetDirectory + "/" + artifact + "-" + version + "." + type );
    }

    protected File downloadFile( URL url, String targetFile ) throws IOException
    {

        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;


        File downloadedFile = new File( targetFile );
        // if this is null then someone was getting really creative with the tests, but hey, we will let them...
        if( downloadedFile.getParentFile() != null )
        {
          downloadedFile.getParentFile().mkdirs();
        }

        try
        {

            System.out.println( "Downloading file: " + url );
            out = new BufferedOutputStream( new FileOutputStream( downloadedFile ) );

            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ( ( numRead = in.read( buffer ) ) != -1 )
            {
                out.write( buffer, 0, numRead );
                numWritten += numRead;
            }
        }
        finally
        {
            try
            {
                if ( out != null )
                {
                    out.close();
                }
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( IOException e )
            {
            }

        }

        return downloadedFile;

    }

    protected File unpackArtifact( File artifact, String targetDirectory )
    {
        File target = null;
        try
        {
            target = new File( targetDirectory );
            target.mkdirs();
            ArchiverManager manager = (ArchiverManager) lookup( DefaultArchiverManager.ROLE );
            UnArchiver unarchiver = manager.getUnArchiver( artifact );
            unarchiver.setSourceFile( artifact );
            unarchiver.setDestDirectory( target );
            unarchiver.extract();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Exception unpacking artifact" );
        }

        return target;
    }

    private void setupContainer()
    {
        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------

        context = new HashMap();

        context.put( "basedir", basedir );

        boolean hasPlexusHome = context.containsKey( "plexus.home" );

        if ( !hasPlexusHome )
        {
            File f = new File( basedir, "target/plexus-home" );

            if ( !f.isDirectory() )
            {
                f.mkdir();
            }

            context.put( "plexus.home", f.getAbsolutePath() );
        }

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------

        ContainerConfiguration containerConfiguration =
            new DefaultContainerConfiguration().setName( "test" ).setContext( context ).setContainerConfiguration(
                                                                                                                   getClass().getName().replace(
                                                                                                                                                 '.',
                                                                                                                                                 '/' )
                                                                                                                       + ".xml" );

        try
        {
            container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
            fail( "Failed to create plexus container." );
        }
    }

    protected Object lookup( String componentKey )
        throws Exception
    {
        return container.lookup( componentKey );
    }

    protected Object lookup( String role, String id )
        throws Exception
    {
        return container.lookup( role, id );
    }

    protected PlexusContainer getContainer()
    {
        return this.container;
    }

    public String getBaseNexusUrl()
    {
        return baseNexusUrl;
    }

    public String getNexusRepositoryURL( String repositoryName )
    {
        return this.getBaseNexusUrl() + REPOSITORY_RELATIVE_URL + repositoryName;
    }

    public String getNexusGroupRepositoryURL( String repositoryName )
    {
        return this.getBaseNexusUrl() + GROUP_REPOSITORY_RELATIVE_URL + repositoryName;
    }

    public String getNexusURL()
    {
        return this.nexusUrl;
    }

    public String getNexusBundleDir()
    {
        return nexusBundleDir;
    }

    public void setNexusBundleDir( String nexusBundleDir )
    {
        this.nexusBundleDir = nexusBundleDir;
    }

}
