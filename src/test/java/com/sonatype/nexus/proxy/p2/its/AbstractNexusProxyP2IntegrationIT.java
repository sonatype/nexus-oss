/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
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

        // to force creating the proxies/lineups with no onboarding plugin
        System.setProperty( "p2.lineups.create", "true" );
    }

    protected static ServletServer server;

    protected static final String localStorageDir;
    static
    {
        localStorageDir = TestProperties.getString( "proxy.repo.base.dir" );
    }

    protected AbstractNexusProxyP2IntegrationIT()
    {
        super();
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

    private final int forkedProcessTimeoutInSeconds = 900;

    private File getEquinoxLauncher( final String p2location )
        throws Exception
    {
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( p2location );
        ds.setIncludes( new String[] { "plugins/org.eclipse.equinox.launcher_*.jar" } );
        ds.scan();
        final String[] includedFiles = ds.getIncludedFiles();
        if ( includedFiles == null || includedFiles.length != 1 )
        {
            throw new Exception( "Can't locate org.eclipse.equinox.launcher bundle in " + p2location );
        }
        return new File( p2location, includedFiles[0] );
    }

    protected void installUsingP2( final String repositoryURL, final String installIU, final String destination )
        throws Exception
    {
        installUsingP2( repositoryURL, installIU, destination, null, null, null );
    }

    protected void installUsingP2( final String repositoryURL, final String installIU, final String destination,
                                   final String p2Os, final String p2Ws, final String p2Arch, final String... extraArgs )
        throws Exception
    {
        FileUtils.deleteDirectory( destination );

        final String p2location = getP2RuntimeLocation().getCanonicalPath();
        cleanP2Runtime( p2location );

        final Commandline cli = new Commandline();

        cli.setWorkingDirectory( p2location );

        String executable = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + "java";
        if ( File.separatorChar == '\\' )
        {
            executable = executable + ".exe";
        }
        cli.setExecutable( executable );

        if ( extraArgs != null )
        {
            cli.addArguments( extraArgs );
        }

        cli.addArguments( new String[] { "-Declipse.p2.data.area=" + destination + "/p2" } );
        cli.addArguments( new String[] { "-Dorg.eclipse.ecf.provider.filetransfer.retrieve.readTimeout=30000" } );

        cli.addArguments( new String[] { "-jar", getEquinoxLauncher( p2location ).getAbsolutePath(), } );

        cli.addArguments( new String[] { "-nosplash", "-application", "org.eclipse.equinox.p2.director",
            "-metadataRepository", repositoryURL, "-artifactRepository", repositoryURL, "-installIU", installIU,
            "-destination", destination, "-profile", getTestId(), "-profileProperties",
            "org.eclipse.update.install.features=true", "-bundlepool", destination, "-roaming", "-debug",
            "-consolelog", } );

        if ( p2Os != null )
        {
            cli.addArguments( new String[] { "-p2.os", p2Os } );
        }

        if ( p2Ws != null )
        {
            cli.addArguments( new String[] { "-p2.ws", p2Ws } );
        }

        if ( p2Arch != null )
        {
            cli.addArguments( new String[] { "-p2.arch", p2Arch } );
        }

        log.info( "Command line:\n\t" + cli.toString() );

        final StringBuffer buf = new StringBuffer();

        final StreamConsumer out = new StreamConsumer()
        {
            @Override
            public void consumeLine( final String line )
            {
                System.out.println( line );
                buf.append( "[OUT] " ).append( line ).append( "\n" );
            }
        };

        final StreamConsumer err = new StreamConsumer()
        {
            @Override
            public void consumeLine( final String line )
            {
                System.err.println( line );
                buf.append( "[ERR] " ).append( line ).append( "\n" );
            }
        };

        final int result = CommandLineUtils.executeCommandLine( cli, out, err, forkedProcessTimeoutInSeconds );
        if ( result != 0 )
        {
            throw new P2ITException( result, buf );
        }
    }

    private void cleanP2Runtime( final String p2location )
        throws IOException
    {
        FileUtils.deleteDirectory( p2location + "/p2" ); // clean p2 runtime cache
        final DirectoryScanner scanner = new DirectoryScanner();
        final File configuration = new File( p2location, "configuration" );
        scanner.setBasedir( configuration );
        scanner.scan();
        for ( final String path : scanner.getIncludedFiles() )
        {
            if ( path != null && path.trim().length() > 0 && !"config.ini".equals( path ) )
            {
                new File( configuration, path ).delete();
            }
        }
        for ( final String path : scanner.getIncludedDirectories() )
        {
            if ( path != null && path.trim().length() > 0 )
            {
                FileUtils.deleteDirectory( new File( configuration, path ) );
            }
        }
    }

    protected File getP2RuntimeLocation()
        throws IOException
    {
        final File dst = getOverridableFile( "p2" );
        return dst;
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
