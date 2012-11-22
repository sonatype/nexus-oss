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
package org.sonatype.nexus.yum.testsuite;

import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.repository.GroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenGroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.TasksNexusRestClient;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.nexus.yum.client.Yum;
import org.sonatype.nexus.yum.client.capabilities.GenerateMetadataCapability;
import org.sonatype.nexus.yum.client.capabilities.MergeMetadataCapability;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class YumITSupport
    extends NexusRunningParametrizedITSupport
{

    protected static final Logger LOG = LoggerFactory.getLogger( YumITSupport.class );

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return firstAvailableTestParameters(
            systemTestParameters(),
            testParameters(
                $( "${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle" )
            )
        ).load();
    }

    // TODO replace this with a proper tasks client
    private TasksNexusRestClient tasks;

    // TODO replace this with a proper events client
    private EventInspectorsUtil events;

    public YumITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Before
    public void initializeClients()
    {
        final NexusRestClient nexusRestClient = new NexusRestClient(
            new TestContext()
                .setNexusUrl( nexus().getUrl().toExternalForm() )
                .setSecureTest( true )
        );
        tasks = new TasksNexusRestClient( nexusRestClient );
        events = new EventInspectorsUtil( nexusRestClient );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( NexusBundleConfiguration configuration )
    {
        return configuration
            .setLogPattern( "%d{HH:mm:ss.SSS} %-5level - %msg%n" )
            .setLogLevel( "org.sonatype.nexus.repository.yum", "DEBUG" )
            .setLogLevel( "org.sonatype.nexus.plugins.capabilities", "DEBUG" )
            .addPlugins(
                artifactResolver().resolvePluginFromDependencyManagement(
                    "org.sonatype.nexus.plugins", "nexus-capabilities-plugin"
                ),
                artifactResolver().resolvePluginFromDependencyManagement(
                    "org.sonatype.nexus.plugins", "nexus-yum-plugin"
                )
            );
    }

    protected Repository createYumEnabledRepository( final String repositoryId )
    {
        final Repository repository = repositories()
            .create( MavenHostedRepository.class, repositoryId )
            .excludeFromSearchResults()
            .save();

        enableMetadataGenerationFor( repositoryId );

        return repository;
    }

    protected GroupRepository createYumEnabledGroupRepository( final String repositoryId, final String... memberIds )
    {
        final GroupRepository repository = repositories().create( MavenGroupRepository.class, repositoryId )
            .ofRepositories( memberIds )
            .save();

        enableMetadataMergeFor( repositoryId );

        return repository;
    }

    private void enableMetadataGenerationFor( final String repositoryId )
    {
        capabilities().create( GenerateMetadataCapability.class ).withRepository( repositoryId ).enable();
    }

    private void enableMetadataMergeFor( final String repositoryId )
    {
        capabilities().create( MergeMetadataCapability.class ).withRepository( repositoryId ).enable();
    }

    protected Yum yum()
    {
        return client().getSubsystem( Yum.class );
    }

    protected Repositories repositories()
    {
        return client().getSubsystem( Repositories.class );
    }

    protected Content content()
    {
        return client().getSubsystem( Content.class );
    }

    private Capabilities capabilities()
    {
        return client().getSubsystem( Capabilities.class );
    }

    protected void waitForNexusToSettleDown()
        throws Exception
    {
        LOG.info( "Waiting for Nexus to settle down..." );
        tasks.waitForAllTasksToStop();
        events.waitForCalmPeriod();
        LOG.info( "Nexus settled down, continuing..." );
    }

}
