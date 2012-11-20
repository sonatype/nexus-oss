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
package org.sonatype.nexus.repository.yum.testsuite;

import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.runners.Parameterized;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.repository.GroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenGroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.repository.yum.client.Yum;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class YumRepositoryITSupport
    extends NexusRunningParametrizedITSupport
{

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

    public YumRepositoryITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
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

        createYumRepositoryCapabilityFor( repositoryId );

        return repository;
    }

    protected GroupRepository createYumEnabledGroupRepository( final String repositoryId, final String... memberIds )
    {
        final GroupRepository repository = repositories().create( MavenGroupRepository.class, repositoryId )
            .ofRepositories( memberIds )
            .save();

        createYumGroupCapabilityFor( repositoryId );

        return repository;
    }

    private void createYumRepositoryCapabilityFor( final String repositoryId )
    {
        capabilities().add(
            new CapabilityResource()
                .withTypeId( "yum.repository" )
                .withProperty( new CapabilityPropertyResource().withKey( "repository" ).withValue( repositoryId ) )
        );
    }

    private void createYumGroupCapabilityFor( final String repositoryId )
    {
        capabilities().add(
            new CapabilityResource()
                .withTypeId( "yum.merge" )
                .withProperty( new CapabilityPropertyResource().withKey( "repository" ).withValue( repositoryId ) )
        );
    }

    protected Yum yum()
    {
        return client().getSubsystem( Yum.class );
    }

    private Repositories repositories()
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

    public static void sleep( int timeout, TimeUnit unit )
        throws InterruptedException
    {
        Thread.sleep( unit.toMillis( timeout ) );
    }

}
