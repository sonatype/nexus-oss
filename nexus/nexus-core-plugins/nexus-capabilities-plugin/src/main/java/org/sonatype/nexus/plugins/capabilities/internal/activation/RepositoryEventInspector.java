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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

/**
 * Listens to repository related events and forwards them to repository events notifier.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class RepositoryEventInspector
    extends AbstractLoggingComponent
    implements EventInspector
{

    private final RepositoryEventsNotifier repositoryEventsNotifier;

    @Inject
    RepositoryEventInspector( final RepositoryEventsNotifier repositoryEventsNotifier )
    {
        this.repositoryEventsNotifier = checkNotNull( repositoryEventsNotifier );
    }

    public boolean accepts( final Event<?> evt )
    {
        return evt != null
            && ( evt instanceof RepositoryConfigurationUpdatedEvent || evt instanceof RepositoryRegistryEvent );
    }

    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }
        if ( evt instanceof RepositoryConfigurationUpdatedEvent )
        {
            handle( (RepositoryConfigurationUpdatedEvent) evt );
        }
        else if ( evt instanceof RepositoryRegistryEventAdd )
        {
            handle( (RepositoryRegistryEventAdd) evt );
        }
        else if ( evt instanceof RepositoryRegistryEventRemove )
        {
            handle( (RepositoryRegistryEventRemove) evt );
        }
    }

    private void handle( final RepositoryConfigurationUpdatedEvent evt )
    {
        repositoryEventsNotifier.notify( evt.getRepository(), new RepositoryEventsNotifier.Notifier( "updated" )
        {
            @Override
            void run( final RepositoryEventsNotifier.Listener listener, final Repository repository )
            {
                listener.onUpdated( repository );
            }

        } );
    }

    private void handle( final RepositoryRegistryEventAdd evt )
    {
        repositoryEventsNotifier.notify( evt.getRepository(), new RepositoryEventsNotifier.Notifier( "added" )
        {
            @Override
            void run( final RepositoryEventsNotifier.Listener listener, final Repository repository )
            {
                listener.onAdded( repository );
            }

        } );
    }

    private void handle( final RepositoryRegistryEventRemove evt )
    {
        repositoryEventsNotifier.notify( evt.getRepository(), new RepositoryEventsNotifier.Notifier( "removed" )
        {
            @Override
            void run( final RepositoryEventsNotifier.Listener listener, final Repository repository )
            {
                listener.onRemoved( repository );
            }

        } );
    }

}
