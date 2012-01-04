/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.support.condition.AbstractCondition;
import org.sonatype.nexus.plugins.capabilities.support.condition.RepositoryConditions;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Support class for repository conditions.
 *
 * @since 2.0
 */
public abstract class AbstractRepositoryCondition
    extends AbstractCondition
{

    private final RepositoryRegistry repositoryRegistry;

    private final RepositoryConditions.RepositoryId repositoryId;

    private final ReentrantReadWriteLock bindLock;

    public AbstractRepositoryCondition( final NexusEventBus eventBus,
                                        final RepositoryRegistry repositoryRegistry,
                                        final RepositoryConditions.RepositoryId repositoryId )
    {
        super( eventBus, false );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.repositoryId = checkNotNull( repositoryId );
        bindLock = new ReentrantReadWriteLock();
    }

    @Override
    protected void doBind()
    {
        try
        {
            bindLock.writeLock().lock();
            for ( final Repository repository : repositoryRegistry.getRepositories() )
            {
                handle( new RepositoryRegistryEventAdd( repositoryRegistry, repository ) );
            }
        }
        finally
        {
            bindLock.writeLock().unlock();
        }
        getEventBus().register( this );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    public abstract void handle( final RepositoryRegistryEventAdd event );

    @Override
    protected void setSatisfied( final boolean satisfied )
    {
        try
        {
            bindLock.readLock().lock();
            super.setSatisfied( satisfied );
        }
        finally
        {
            bindLock.readLock().unlock();
        }
    }

    /**
     * Checks that condition is about the passed in repository id.
     *
     * @param repositoryId to check
     * @return true, if condition repository matches the specified repository id
     */
    protected boolean sameRepositoryAs( final String repositoryId )
    {
        return repositoryId != null && repositoryId.equals( getRepositoryId() );
    }

    protected String getRepositoryId()
    {
        return repositoryId.get();
    }

}
