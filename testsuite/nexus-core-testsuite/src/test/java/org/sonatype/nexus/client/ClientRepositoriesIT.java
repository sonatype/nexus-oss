/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenGroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenM1VirtualRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;

public class ClientRepositoriesIT
    extends ClientITSupport
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public ClientRepositoriesIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void getInexistentRepository()
    {
        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( "Repository with id 'getInexistentRepository' was not found" );
        repositories().get( repositoryIdForTest() );
    }

    @Test
    public void getHosted()
    {
        final Repository repository = repositories().get( "releases" );
        assertThat( repository, is( instanceOf( MavenHostedRepository.class ) ) );
    }

    @Test
    public void refreshHosted()
    {
        final Repository repository = repositories().get( "releases" );
        final String name = repository.name();
        repository.withName( "foo" );
        repository.refresh();
        assertThat( repository.name(), is( name ) );
    }

    @Test
    public void createHosted()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenHostedRepository.class, id )
            .save();
    }

    @Test
    public void removeHosted()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenHostedRepository.class, id )
            .save()
            .remove();
    }

    @Test
    public void statusHosted()
    {
        final String id = repositoryIdForTest();
        final MavenHostedRepository repository = repositories().create( MavenHostedRepository.class, id );
        assertThat( repository.status().isInService(), is( false ) );
        repository.save();
        assertThat( repository.status().isInService(), is( true ) );
        assertThat( repository.putOutOfService().status().isInService(), is( false ) );
        assertThat( repository.putInService().status().isInService(), is( true ) );
    }

    @Test
    public void getProxy()
    {
        final Repository repository = repositories().get( "central" );
        assertThat( repository, is( instanceOf( MavenProxyRepository.class ) ) );
    }

    @Test
    public void refreshProxy()
    {
        final Repository repository = repositories().get( "central" );
        final String name = repository.name();
        repository.withName( "foo" );
        repository.refresh();
        assertThat( repository.name(), is( name ) );
    }

    @Test
    public void createProxy()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenProxyRepository.class, id )
            .asProxyOf( "http://localhost:8080" )
            .save();
    }

    public void removeProxy()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenProxyRepository.class, id )
            .asProxyOf( "http://localhost:8080" )
            .save()
            .remove();
    }

    @Test
    public void statusProxy()
    {
        final String id = repositoryIdForTest();
        final MavenProxyRepository repository = repositories().create( MavenProxyRepository.class, id )
            .asProxyOf( "http://localhost:8080" );
        assertThat( repository.status().isInService(), is( false ) );
        repository.save();
        assertThat( repository.status().isInService(), is( true ) );
        assertThat( repository.putOutOfService().status().isInService(), is( false ) );
        assertThat( repository.putInService().status().isInService(), is( true ) );
    }

    @Test
    public void proxyMode()
    {
        final String id = repositoryIdForTest();
        final MavenProxyRepository repository = repositories().create( MavenProxyRepository.class, id )
            .asProxyOf( "http://localhost:8080" )
            .doNotAutoBlock();
        assertThat( repository.status().isBlocked(), is( false ) );
        assertThat( repository.status().isAutoBlocked(), is( false ) );
        repository.save();
        assertThat( repository.status().isBlocked(), is( false ) );
        assertThat( repository.status().isAutoBlocked(), is( false ) );
        repository.block();
        assertThat( repository.status().isBlocked(), is( true ) );
        assertThat( repository.status().isAutoBlocked(), is( false ) );
        repository.unblock();
        assertThat( repository.status().isBlocked(), is( false ) );
        assertThat( repository.status().isAutoBlocked(), is( false ) );
    }

    @Test
    public void getGroup()
    {
        final Repository repository = repositories().get( "public" );
        assertThat( repository, is( instanceOf( MavenGroupRepository.class ) ) );
    }

    @Test
    public void refreshGroup()
    {
        final Repository repository = repositories().get( "public" );
        final String name = repository.name();
        repository.withName( "foo" );
        repository.refresh();
        assertThat( repository.name(), is( name ) );
    }

    @Test
    public void createGroup()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenGroupRepository.class, id )
            .ofRepositories( "central", "releases", "snapshots" )
            .save();
    }

    @Test
    public void groupMembersOperations()
    {
        final String id = repositoryIdForTest();
        final MavenGroupRepository repository = repositories().create( MavenGroupRepository.class, id )
            .ofRepositories( "central", "releases", "snapshots" );

        assertThat( repository.memberRepositories(), contains( "central", "releases", "snapshots" ) );

        repository.ofRepositories( "central", "releases" );
        assertThat( repository.memberRepositories(), contains( "central", "releases" ) );

        repository.addMember( "snapshots" );
        assertThat( repository.memberRepositories(), contains( "central", "releases", "snapshots" ) );

        repository.removeMember( "releases" );
        assertThat( repository.memberRepositories(), contains( "central", "snapshots" ) );
    }

    @Test
    public void removeGroup()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenGroupRepository.class, id )
            .ofRepositories( "central", "releases", "snapshots" )
            .save();
    }

    @Test
    public void statusGroup()
    {
        final String id = repositoryIdForTest();
        final MavenGroupRepository repository = repositories().create( MavenGroupRepository.class, id )
            .ofRepositories( "central", "releases", "snapshots" );
        assertThat( repository.status().isInService(), is( false ) );
        repository.save();
        assertThat( repository.status().isInService(), is( true ) );
        assertThat( repository.putOutOfService().status().isInService(), is( false ) );
        assertThat( repository.putInService().status().isInService(), is( true ) );
    }

    @Test
    public void getShadow()
    {
        final Repository repository = repositories().get( "central-m1" );
        assertThat( repository, is( instanceOf( MavenM1VirtualRepository.class ) ) );
    }

    @Test
    public void refreshShadow()
    {
        final Repository repository = repositories().get( "central-m1" );
        final String name = repository.name();
        repository.withName( "foo" );
        repository.refresh();
        assertThat( repository.name(), is( name ) );
    }

    @Test
    public void createShadow()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenM1VirtualRepository.class, id )
            .ofRepository( "apache-snapshots" )
            .save();
    }

    @Test
    public void removeShadow()
    {
        final String id = repositoryIdForTest();
        repositories().create( MavenM1VirtualRepository.class, id )
            .ofRepository( "apache-snapshots" )
            .save()
            .remove();
    }

    @Test
    public void statusShadow()
    {
        final String id = repositoryIdForTest();
        final MavenM1VirtualRepository repository = repositories().create( MavenM1VirtualRepository.class, id )
            .ofRepository( "apache-snapshots" );
        assertThat( repository.status().isInService(), is( false ) );
        repository.save();
        assertThat( repository.status().isInService(), is( true ) );
        assertThat( repository.putOutOfService().status().isInService(), is( false ) );
        assertThat( repository.putInService().status().isInService(), is( true ) );
    }

    @Test
    public void get()
    {
        final Collection<Repository> repositories = repositories().get();
        assertThat( repositories.size(), is( greaterThanOrEqualTo( 7 ) ) );
    }

    private Repositories repositories()
    {
        return client().getSubsystem( Repositories.class );
    }

}
