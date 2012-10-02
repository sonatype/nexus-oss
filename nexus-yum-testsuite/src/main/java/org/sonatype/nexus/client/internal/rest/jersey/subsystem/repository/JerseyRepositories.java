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
package org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.util.Set;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.spi.subsystem.repository.RepositoryFactory;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.RepositoryResourceResponse;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;

/**
 * @since 2.2
 */
public class JerseyRepositories
    extends SubsystemSupport<JerseyNexusClient>
    implements Repositories
{

    private final Set<RepositoryFactory> repositoryFactories;

    public JerseyRepositories( final JerseyNexusClient nexusClient, final Set<RepositoryFactory> repositoryFactories )
    {
        super( nexusClient );
        this.repositoryFactories = repositoryFactories;
    }

    @Override
    public <R extends Repository> R get( final String id )
    {
        checkNotNull( id );

        final RepositoryResourceResponse response =
            getNexusClient().serviceResource( "repositories/" + id ).get( RepositoryResourceResponse.class );

        return convert( id, response.getData() );
    }

    @Override
    public <R extends Repository> R getGroup( final String id )
    {
        checkNotNull( id );

        final RepositoryGroupResourceResponse response =
            getNexusClient().serviceResource( "repo_groups/" + id ).get( RepositoryGroupResourceResponse.class );

        return convert( id, response.getData() );
    }

    @Override
    public Set<Repository> get()
    {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<Repository> getGroups()
    {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R extends Repository> R create( final Class<R> type, final String id )
    {
        for ( RepositoryFactory repositoryFactory : repositoryFactories )
        {
            if ( repositoryFactory.canCreate( type ) )
            {
                final Repository<Repository, RepositoryBaseResource> r = repositoryFactory.create( getNexusClient() );
                r.settings().setId( id );
                return (R) r;
            }
        }
        throw new IllegalStateException( format( "No repository factory found for repository of type %s",
            type.getName() ) );
    }

    private <R extends Repository> R convert( final String id, final RepositoryBaseResource rbs )
    {
        int currentScore = 0;
        RepositoryFactory factory = null;
        for ( RepositoryFactory repositoryFactory : repositoryFactories )
        {
            final int score = repositoryFactory.canAdapt( rbs );
            if ( score > currentScore )
            {
                factory = repositoryFactory;
            }
        }

        if ( factory == null )
        {
            throw new IllegalStateException( format( "No repository factory found for repository with id %s", id ) );
        }

        final JerseyRepositorySupport repository = (JerseyRepositorySupport) factory.create( getNexusClient() );
        repository.overwriteSettingsWith( rbs );

        return (R) repository;
    }

}
