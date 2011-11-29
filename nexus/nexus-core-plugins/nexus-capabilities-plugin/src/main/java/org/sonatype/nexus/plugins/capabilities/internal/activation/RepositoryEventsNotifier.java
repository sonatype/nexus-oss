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
package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

/**
 * Listens to repository related events and forwards them to configured listeners.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class RepositoryEventsNotifier
    extends AbstractLoggingComponent
{

    private final RepositoryRegistry repositoryRegistry;

    /**
     * Registered listeners. Never null.
     */
    private final Set<Listener> listeners;

    @Inject
    RepositoryEventsNotifier( final RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = checkNotNull( repositoryRegistry );

        listeners = new CopyOnWriteArraySet<Listener>();
    }

    public boolean accepts( final Event<?> evt )
    {
        return evt != null
            && evt instanceof RepositoryConfigurationUpdatedEvent;
    }

    public RepositoryEventsNotifier addListener( final Listener listener )
    {
        listeners.add( listener );
        getLogger().debug( "Added listener '{}'.Notifying it about existing repositories...", listener );
        for ( final Repository repository : new ArrayList<Repository>( repositoryRegistry.getRepositories() ) )
        {
            try
            {
                listener.onAdded( repository );
            }
            catch ( Exception e )
            {
                getLogger().warn(
                    "Catched exception while notifying '{}' about existing repository '{}'",
                    new Object[]{ listener, repository, e }
                );
            }
        }
        return this;
    }

    public RepositoryEventsNotifier removeListener( final Listener listener )
    {
        listeners.remove( listener );
        getLogger().debug( "Removed listener '{}'", listener );
        return this;
    }

    void notify( final Repository repository, final Notifier notifier )
    {
        getLogger().debug( "Notifying {} repository listeners...", listeners.size() );

        for ( final Listener listener : listeners )
        {
            getLogger().debug(
                "Notifying '{}' about {} repository '{}'",
                new Object[]{ listener, notifier.description, repository.getId() }
            );
            try
            {
                notifier.run( listener, repository );
            }
            catch ( Exception e )
            {
                getLogger().warn(
                    "Catched exception while notifying '{}' about {} repository '{}'",
                    new Object[]{ listener, notifier.description, repository, e }
                );
            }
        }
    }

    /**
     * Listener of repository related events.
     *
     * @since 1.10.0
     */
    public static interface Listener
    {

        /**
         * Callback after a repository was added (happens also on start of nexus when repositories are loaded from
         * configuration).
         *
         * @param repository added repository
         */
        void onAdded( Repository repository );

        /**
         * Callback when the repository configuration changed.
         *
         * @param repository updated repository
         */
        void onUpdated( Repository repository );

        /**
         * Callback when a repository has been removed.
         *
         * @param repository updated repository
         */
        void onRemoved( Repository repository );

    }

    abstract static class Notifier
    {

        private String description;

        Notifier( final String description )
        {
            this.description = description;
        }

        abstract void run( Listener listener, Repository repository );
    }

}
