/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.obr.test;

import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import javax.annotation.Nullable;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.restlet.data.MediaType;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.RepositoriesNexusRestClient;
import org.sonatype.nexus.test.utils.RepositoryGroupsNexusRestClient;
import org.sonatype.nexus.test.utils.TasksNexusRestClient;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public abstract class ObrITSupport
    extends NexusRunningParametrizedITSupport
{

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return firstAvailableTestParameters(
            systemTestParameters(),
            testParameters(
                $( "org.sonatype.nexus:nexus-oss-webapp:zip:bundle" )
            )
        ).load();
    }

    public static final String FELIX_WEBCONSOLE =
        "org/apache/felix/org.apache.felix.webconsole/3.0.0/org.apache.felix.webconsole-3.0.0.jar";

    public static final String OSGI_COMPENDIUM =
        "org/apache/felix/org.osgi.compendium/1.4.0/org.osgi.compendium-1.4.0.jar";

    public static final String GERONIMO_SERVLET =
        "org/apache/geronimo/specs/geronimo-servlet_3.0_spec/1.0/geronimo-servlet_3.0_spec-1.0.jar";

    public static final String PORTLET_API =
        "org/apache/portals/portlet-api_2.0_spec/1.0/portlet-api_2.0_spec-1.0.jar";

    // TODO replace this with a proper client
    private RepositoriesNexusRestClient repositories;

    // TODO replace this with a proper client
    private RepositoryGroupsNexusRestClient groups;

    public ObrITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return configuration.addPlugins(
            artifactResolver().resolvePluginFromDependencyManagement(
                "org.sonatype.nexus.plugins", "nexus-obr-plugin"
            )
        );
    }

    @Before
    public void initRestClients()
    {
        final NexusRestClient nexusRestClient = new NexusRestClient(
            new TestContext()
                .setNexusUrl( nexus().getUrl().toExternalForm() )
                .setSecureTest( true )
        );
        final TasksNexusRestClient tasks = new TasksNexusRestClient( nexusRestClient );
        final EventInspectorsUtil events = new EventInspectorsUtil( nexusRestClient );

        repositories = new RepositoriesNexusRestClient(
            nexusRestClient, tasks, events, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML
        );
        groups = new RepositoryGroupsNexusRestClient(
            nexusRestClient, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML
        );
    }

    protected RepositoriesNexusRestClient repositories()
    {
        return repositories;
    }

    protected RepositoryGroupsNexusRestClient groups()
    {
        return groups;
    }

    protected Content content()
    {
        return client().getSubsystem( Content.class );
    }

    protected void deployUsingObrIntoFelix( final String repoId )
        throws Exception
    {
        final File felixHome = util.resolveFile( "target/org.apache.felix.main.distribution-3.2.2" );
        final File felixRepo = util.resolveFile( "target/felix-repo" );
        final File felixConfig = testData().resolveFile( "felix.properties" );

        // ensure we have an obr.xml
        final Content content = content();
        final Location obrLocation = new Location( repoId, ".meta/obr.xml" );
        content.download(
            obrLocation,
            new File( testIndex().getDirectory( "downloads" ), repoId + "-obr.xml" )
        );

        FileUtils.deleteDirectory( new File( felixHome, "felix-cache" ) );
        FileUtils.deleteDirectory( new File( felixRepo, ".meta" ) );

        final ProcessBuilder pb = new ProcessBuilder(
            "java", "-Dfelix.felix.properties=" + felixConfig.toURI(), "-jar", "bin/felix.jar"
        );
        pb.directory( felixHome );
        pb.redirectErrorStream( true );
        final Process p = pb.start();

        final Object lock = new Object();

        final Thread t = new Thread( new Runnable()
        {
            public void run()
            {
                // just a safeguard, if felix get stuck kill everything
                try
                {
                    synchronized ( lock )
                    {
                        lock.wait( 5 * 1000 * 60 );
                    }
                }
                catch ( final InterruptedException e )
                {
                    // ignore
                }
                p.destroy();
            }
        } );
        t.setDaemon( true );
        t.start();

        synchronized ( lock )
        {
            final InputStream input = p.getInputStream();
            final OutputStream output = p.getOutputStream();
            waitFor( input, "g!" );

            output.write(
                ( "obr:repos add " + nexus().getUrl() + "content/" + obrLocation.toContentPath() + "\r\n" ).getBytes()
            );
            output.flush();
            waitFor( input, "g!" );

            output.write(
                ( "obr:repos remove http://felix.apache.org/obr/releases.xml\r\n" ).getBytes()
            );
            output.flush();
            waitFor( input, "g!" );

            output.write(
                ( "obr:repos list\r\n" ).getBytes()
            );
            output.flush();
            waitFor( input, "g!" );

            output.write( "obr:deploy -s org.apache.felix.webconsole\r\n".getBytes() );
            output.flush();
            waitFor( input, "done." );

            p.destroy();

            lock.notifyAll();
        }
    }

    private void waitFor( final InputStream input, final String expectedLine )
        throws Exception
    {
        final long startMillis = System.currentTimeMillis();
        final StringBuilder content = new StringBuilder();
        do
        {
            final int available = input.available();
            if ( available > 0 )
            {
                final byte[] bytes = new byte[available];
                input.read( bytes );
                final String current = new String( bytes );
                System.out.print( current );
                content.append( current );
                Thread.yield();
            }
            else if ( System.currentTimeMillis() - startMillis > 5 * 60 * 1000 )
            {
                throw new InterruptedException(); // waited for more than 5 minutes
            }
            else
            {
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( final InterruptedException e )
                {
                    // continue...
                }
            }
        }
        while ( content.indexOf( expectedLine ) == -1 );
        System.out.println();
    }

    protected void createObrHostedRepository( final String repositoryId )
    {
        final RepositoryResource repo = new RepositoryResource();

        repo.setId( repositoryId );
        repo.setRepoType( "hosted" );
        repo.setName( repositoryId );
        repo.setProvider( "obr-proxy" );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo.setChecksumPolicy( ChecksumPolicy.WARN.name() );

        repo.setBrowseable( true );
        repo.setIndexable( false );
        repo.setExposed( true );

        try
        {
            repositories().createRepository( repo );
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    protected void createObrProxyRepository( final String repositoryId, final String obrXmlUrl )
    {
        final RepositoryProxyResource repository = new RepositoryProxyResource();

        repository.setId( repositoryId );
        repository.setRepoType( "proxy" );
        repository.setName( repositoryId );
        repository.setProvider( "obr-proxy" );
        repository.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repository.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        repository.setChecksumPolicy( ChecksumPolicy.IGNORE.name() );
        repository.setBrowseable( true );
        repository.setIndexable( false );
        repository.setExposed( true );
        repository.setArtifactMaxAge( 1440 );
        repository.setMetadataMaxAge( 1440 );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( obrXmlUrl );
        repository.setRemoteStorage( remoteStorage );

        try
        {
            repositories().createRepository( repository );
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    protected void createObrShadowRepository( final String repositoryId, final String shadowOfRepositoryId )
    {
        final RepositoryShadowResource repo = new RepositoryShadowResource();

        repo.setId( repositoryId );
        repo.setRepoType( "virtual" );
        repo.setName( repositoryId );
        repo.setProvider( "obr-shadow" );
        repo.setShadowOf( shadowOfRepositoryId );
        repo.setSyncAtStartup( true );

        repo.setExposed( true );

        try
        {
            repositories().createRepository( repo );
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    protected void createObrGroup( final String groupId, final String... memberRepositoriesIds )
    {
        final RepositoryGroupResource group = new RepositoryGroupResource();
        group.setId( groupId );
        group.setRepoType( "group" );
        group.setProvider( "obr-group" );
        group.setName( groupId );
        group.setExposed( true );

        group.setRepositories( Lists.transform(
            Lists.newArrayList( memberRepositoriesIds ),
            new Function<String, RepositoryGroupMemberRepository>()
            {
                public RepositoryGroupMemberRepository apply( @Nullable final String id )
                {
                    final RepositoryGroupMemberRepository memberRepository = new RepositoryGroupMemberRepository();
                    memberRepository.setId( id );
                    return memberRepository;
                }
            }
        ) );

        try
        {
            groups().createGroup( group );
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    protected void upload( final String repositoryId, final String path )
        throws IOException
    {
        content().upload( new Location( repositoryId, path ), util.resolveFile( "target/felix-repo/" + path ) );
    }

    protected File download( final String repositoryId, final String path )
        throws IOException
    {
        final File downloaded = new File( testIndex().getDirectory( "downloads" ), path );
        content().download( new Location( repositoryId, path ), downloaded );
        return downloaded;
    }

    protected void deployUsingMaven( final String projectName, final String repositoryId )
        throws VerificationException
    {
        final File projectToBuildSource = testData().resolveFile( projectName );
        final File mavenSettingsSource = testData().resolveFile( "settings.xml" );

        final File projectToBuildTarget = testIndex().getDirectory( "maven/" + projectName );
        final File mavenSettingsTarget = new File( testIndex().getDirectory( "maven" ), "settings.xml" );

        final Properties properties = new Properties();
        properties.setProperty( "nexus-base-url", nexus().getUrl().toExternalForm() );
        properties.setProperty( "nexus-repository-id", repositoryId );

        tasks().copy().directory( file( projectToBuildSource ) )
            .filterUsing( properties )
            .to().directory( file( projectToBuildTarget ) ).run();
        tasks().copy().file( file( mavenSettingsSource ) )
            .filterUsing( properties )
            .to().file( file( mavenSettingsTarget ) ).run();

        final File mavenHome = util.resolveFile( "target/apache-maven-3.0.4" );
        final File localRepo = util.resolveFile( "target/maven/fake-repo" );

        tasks().chmod( file( new File( mavenHome, "bin" ) ) ).include( "mvn" ).permissions( "755" ).run();

        System.setProperty( "maven.home", mavenHome.getAbsolutePath() );
        final Verifier verifier = new Verifier( projectToBuildTarget.getAbsolutePath(), false );
        verifier.setAutoclean( true );
        verifier.setLogFileName( "maven.log" );

        verifier.setLocalRepo( localRepo.getAbsolutePath() );
        verifier.setMavenDebug( true );
        verifier.setCliOptions( Arrays.asList( "-s " + mavenSettingsTarget.getAbsolutePath() ) );

        verifier.resetStreams();

        verifier.executeGoal( "deploy" );
        verifier.verifyErrorFreeLog();

        testIndex().recordLink( "maven.log", new File( projectToBuildTarget, "maven.log" ) );
    }

    protected String repositoryIdForTest()
    {
        String methodName = testName.getMethodName();
        if ( methodName.contains( "[" ) )
        {
            return methodName.substring( 0, methodName.indexOf( "[" ) );
        }
        return methodName;
    }

}
