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
package org.sonatype.nexus.plugins.yum.plugin.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.yum.plugin.DeletionService;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.plugins.yum.plugin.event.YumRepositoryGenerateEvent;
import org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumGroupRepository;
import org.sonatype.nexus.plugins.yum.repository.service.YumService;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
@EventBus.Managed
public class RpmRepositoryEventsHandler
{

    private static final Logger LOG = LoggerFactory.getLogger( RpmRepositoryEventsHandler.class );

    @Inject
    private RepositoryRegistry repositoryRegistry;

    @Inject
    private org.sonatype.nexus.proxy.registry.RepositoryRegistry nexusRepositoryRegistry;

    @Inject
    private YumService yumService;

    @Inject
    private DeletionService deletionService;

    @Subscribe
    public void on( final YumRepositoryGenerateEvent event )
    {
        final Repository repository = event.getRepository();
        for ( GroupRepository groupRepository : nexusRepositoryRegistry.getGroupsOfRepository( repository ) )
        {
            if ( groupRepository.getRepositoryKind().isFacetAvailable( M2YumGroupRepository.class ) )
            {
                yumService.createGroupRepository( groupRepository );
            }
        }
    }

    @Subscribe
    public void on( final RepositoryRegistryEventAdd event )
    {
        if ( event.getRepository().getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) )
        {
            repositoryRegistry.registerRepository( event.getRepository().adaptToFacet( MavenHostedRepository.class ) );
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void on( final RepositoryItemEventStore itemEvent )
    {
        if ( isRpmItemEvent( itemEvent ) )
        {
            LOG.info( "ItemStoreEvent : {}", itemEvent.getItem().getPath() );
            yumService.markDirty( itemEvent.getRepository(), getItemVersion( itemEvent.getItem() ) );
            yumService.addToYumRepository( itemEvent.getRepository(), itemEvent.getItem().getPath() );
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void on( RepositoryItemEventDelete itemEvent )
    {
        if ( isRpmItemEvent( itemEvent ) )
        {
            deletionService.deleteRpm( itemEvent.getRepository(), itemEvent.getItem().getPath() );
        }
        else if ( isCollectionItem( itemEvent ) )
        {
            deletionService.deleteDirectory( itemEvent.getRepository(), itemEvent.getItem().getPath() );
        }
    }

    private boolean isCollectionItem( RepositoryItemEvent itemEvent )
    {
        return StorageCollectionItem.class.isAssignableFrom( itemEvent.getItem().getClass() );
    }

    private boolean isRpmItemEvent( RepositoryItemEvent itemEvent )
    {
        return repositoryRegistry.isRegistered( itemEvent.getRepository() )
            && itemEvent.getItem().getPath().endsWith( ".rpm" );
    }

    private String getItemVersion( StorageItem item )
    {
        String[] parts = item.getParentPath().split( "/" );
        return parts[parts.length - 1];
    }
}
