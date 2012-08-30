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
package org.sonatype.nexus.maven.site.plugin;

import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.restlet.data.MediaType;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.RepositoriesNexusRestClient;
import org.sonatype.nexus.test.utils.TasksNexusRestClient;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import com.google.common.base.Throwables;
import com.sun.jersey.api.client.ClientResponse;

public abstract class NexusSiteITSupport
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

    // TODO replace this with a proper client
    private RepositoriesNexusRestClient repositories;

    public NexusSiteITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return configuration.addPlugins(
            artifactResolver().resolvePluginFromDependencyManagement(
                "org.sonatype.nexus.plugins", "nexus-maven-site-plugin"
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
    }

    protected RepositoriesNexusRestClient repositories()
    {
        return repositories;
    }

    protected RepositoryBaseResource createMavenSiteRepository( final String repositoryId )
    {
        final RepositoryResource repository = new RepositoryResource();

        repository.setId( repositoryId );
        repository.setName( repository.getId() );
        repository.setRepoType( "hosted" );
        repository.setProvider( "maven-site" );
        repository.setProviderRole( WebSiteRepository.class.getName() );
        repository.setRepoPolicy( RepositoryPolicy.MIXED.name() );
        repository.setBrowseable( true );
        repository.setExposed( true );

        try
        {
            return repositories().createRepository( repository );
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }

    }

    protected void copySiteContentToRepository( final String sitePath, final String repositoryId )
    {
        tasks().copy().directory( file( testData().resolveFile( sitePath ) ) )
            .to().directory( file( new File( nexus().getWorkDirectory(), "storage/" + repositoryId ) ) )
            .run();
    }

    protected ClientResponse getStatusOf( final String uri )
    {
        return ( (JerseyNexusClient) client() ).uri( uri ).get( ClientResponse.class );
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

    protected File executeMaven( final String projectName, final String repositoryId, final String... goals )
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
        final File localRepo = util.resolveFile( "target/apache-maven-local-repository" );

        tasks().chmod( file( new File( mavenHome, "bin" ) ) ).include( "mvn" ).permissions( "755" ).run();

        System.setProperty( "maven.home", mavenHome.getAbsolutePath() );
        final Verifier verifier = new Verifier( projectToBuildTarget.getAbsolutePath(), false );
        verifier.setAutoclean( true );

        verifier.setLocalRepo( localRepo.getAbsolutePath() );
        verifier.setMavenDebug( true );
        verifier.setCliOptions( Arrays.asList( "-s " + mavenSettingsTarget.getAbsolutePath() ) );

        verifier.resetStreams();

        verifier.setLogFileName( "maven.log" );
        verifier.executeGoals( Arrays.asList( goals ) );
        verifier.verifyErrorFreeLog();
        testIndex().recordLink(
            verifier.getLogFileName(), new File( projectToBuildTarget, verifier.getLogFileName() )
        );

        return projectToBuildTarget;
    }

    protected File downloadFromSite( final String repositoryId, final String path )
        throws IOException
    {
        final File downloaded = new File( testIndex().getDirectory( "downloads" ), path );
        client().getSubsystem( Content.class ).download( new Location( repositoryId, path ), downloaded );
        return downloaded;
    }

}
