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
package org.sonatype.nexus.repository.yum.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.repository.yum.YumRegistry;
import org.sonatype.nexus.repository.yum.internal.task.YumGroupRepositoryGenerationTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
@EventBus.Managed
public class EventsRouter
{

    private final Provider<RepositoryRegistry> repositoryRegistry;

    private final Provider<YumRegistry> yumRegistryProvider;

    private final NexusScheduler nexusScheduler;

    @Inject
    public EventsRouter( final Provider<RepositoryRegistry> repositoryRegistry,
                         final Provider<YumRegistry> yumRegistryProvider,
                         final NexusScheduler nexusScheduler )
    {
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.yumRegistryProvider = checkNotNull( yumRegistryProvider );
        this.nexusScheduler = checkNotNull( nexusScheduler );
    }

    @AllowConcurrentEvents
    @Subscribe
    public void on( final RepositoryGroupMembersChangedEvent event )
    {
        if ( yumRegistryProvider.get().isRegistered( event.getGroupRepository().getId() )
            && ( anyOfRepositoriesIsYumEnabled( event.getAddedRepositoryIds() )
            || anyOfRepositoriesIsYumEnabled( event.getRemovedRepositoryIds() )
            || anyOfRepositoriesIsYumEnabled( event.getReorderedRepositoryIds() ) ) )
        {
            YumGroupRepositoryGenerationTask.createTaskFor( nexusScheduler, event.getGroupRepository() );
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void on( final RepositoryItemEventStore eventStore )
    {
        if ( isRpmItemEvent( eventStore ) )
        {
            final Yum yum = yumRegistryProvider.get().get( eventStore.getRepository().getId() );
            if ( yum != null )
            {
                yum.markDirty( getItemVersion( eventStore.getItem() ) );
                yum.addToYumRepository( eventStore.getItem().getPath() );
            }
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void on( RepositoryItemEventDelete itemEvent )
    {
        final Yum yum = yumRegistryProvider.get().get( itemEvent.getRepository().getId() );
        if ( yum != null )
        {
            if ( isRpmItemEvent( itemEvent ) )
            {
                yum.deleteRpm( itemEvent.getItem().getPath() );
            }
            else if ( isCollectionItem( itemEvent ) )
            {
                yum.deleteDirectory( itemEvent.getItem().getPath() );
            }
        }
    }

    private boolean isCollectionItem( RepositoryItemEvent itemEvent )
    {
        return StorageCollectionItem.class.isAssignableFrom( itemEvent.getItem().getClass() );
    }

    private boolean isRpmItemEvent( RepositoryItemEvent itemEvent )
    {
        return yumRegistryProvider.get().isRegistered( itemEvent.getRepository().getId() )
            && itemEvent.getItem().getPath().endsWith( ".rpm" );
    }

    private String getItemVersion( StorageItem item )
    {
        String[] parts = item.getParentPath().split( "/" );
        return parts[parts.length - 1];
    }

    private boolean anyOfRepositoriesIsYumEnabled( final List<String> repositoryIds )
    {
        if ( repositoryIds != null )
        {
            for ( final String repositoryId : repositoryIds )
            {
                if ( yumRegistryProvider.get().isRegistered( repositoryId ) )
                {
                    final Yum yum = yumRegistryProvider.get().get( repositoryId );
                    try
                    {
                        // TODO this is suspicious. Should not directly use FS
                        final File repomd = new File(
                            RepositoryUtils.getBaseDir( yum.getRepository() ), "repodata/repomd.xml"
                        );
                        if ( repomd.exists() )
                        {
                            return true;
                        }
                    }
                    catch ( final Exception e )
                    {
                        // TODO check if we should silently ignore this
                    }

                }
            }
        }
        return false;
    }

}
