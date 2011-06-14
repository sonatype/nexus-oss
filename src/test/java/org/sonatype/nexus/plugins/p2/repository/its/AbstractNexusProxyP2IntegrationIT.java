/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;

public abstract class AbstractNexusProxyP2IntegrationIT
    extends AbstractNexusIntegrationTest
{
    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    protected static ServletServer server;

    protected static final String localStorageDir;
    static
    {
        localStorageDir = TestProperties.getString( "proxy.repo.base.dir" );
    }

    protected AbstractNexusProxyP2IntegrationIT( final String testRepositoryId )
    {
        super( testRepositoryId );
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();
    }

    @Before
    public void startProxy()
        throws Exception
    {
        if ( server == null )
        {
            server = (ServletServer) lookup( ServletServer.ROLE );
            server.start();
        }
    }

    @After
    public void stopProxy()
        throws Exception
    {
        if ( server != null )
        {
            server = (ServletServer) lookup( ServletServer.ROLE );
            server.stop();
            server = null;
        }
    }

    protected void installUsingP2( final String repositoryURL, final String installIU, final String destination )
        throws Exception
    {
        installUsingP2( repositoryURL, installIU, destination, null );
    }

    protected void installUsingP2( final String repositoryURL, final String installIU, final String destination,
                                   final Map<String, String> extraEnv )
        throws Exception
    {
        FileUtils.deleteDirectory( destination );

        final File basedir = ResourceExtractor.simpleExtractResources( getClass(), "/run-p2" );
        final Verifier verifier = new Verifier( basedir.getAbsolutePath() );

        final Map<String, String> env = new HashMap<String, String>();
        env.put( "org.eclipse.ecf.provider.filetransfer.retrieve.readTimeout", "30000" );
        env.put( "p2.installIU", installIU );
        env.put( "p2.destination", destination );
        env.put( "p2.metadataRepository", repositoryURL );
        env.put( "p2.artifactRepository", repositoryURL );
        env.put( "p2.profile", getTestId() );

        if ( extraEnv != null )
        {
            env.putAll( extraEnv );
        }

        verifier.executeGoals( Arrays.asList( "verify" ), env );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
    }

    @Override
    public void oncePerClassSetUp()
        throws Exception
    {
        startProxy();

        super.oncePerClassSetUp();
    }

    protected void replaceInFile( final String filename, final String target, final String replacement )
        throws IOException
    {
        String content = FileUtils.fileRead( filename );
        content = content.replace( target, replacement );
        FileUtils.fileWrite( filename, content );
    }

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        final File source = new File( TestProperties.getString( "test.resources.source.folder" ), "proxyRepo" );
        if ( !source.exists() )
        {
            return;
        }

        FileTestingUtils.interpolationDirectoryCopy( source, new File( localStorageDir ), TestProperties.getAll() );

    }

}
