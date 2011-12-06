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

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.support.activation.RepositoryConditions;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is satisfied when a repository has a specified proxy mode.
 *
 * @since 1.10.0
 */
public class RepositoryProxyModeCondition
    extends AbstractRepositoryCondition
{

    private final ProxyMode proxyMode;

    public RepositoryProxyModeCondition( final NexusEventBus eventBus,
                                         final RepositoryRegistry repositoryRegistry,
                                         final ProxyMode proxyMode,
                                         final RepositoryConditions.RepositoryId repositoryId )
    {
        super( eventBus, repositoryRegistry, repositoryId );
        this.proxyMode = checkNotNull( proxyMode );
    }

    @Override
    @Subscribe
    public void handle( final RepositoryRegistryEventAdd event )
    {
        if ( sameRepositoryAs( event.getRepository().getId() )
            && event.getRepository().getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            setSatisfied( proxyMode.equals(
                event.getRepository().adaptToFacet( ProxyRepository.class ).getProxyMode() )
            );
        }
    }

    @Subscribe
    public void handle( final RepositoryConfigurationUpdatedEvent event )
    {
        if ( sameRepositoryAs( event.getRepository().getId() )
            && event.getRepository().getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            setSatisfied( proxyMode.equals(
                event.getRepository().adaptToFacet( ProxyRepository.class ).getProxyMode() )
            );
        }
    }

    @Subscribe
    public void handle( final RepositoryRegistryEventRemove event )
    {
        if ( sameRepositoryAs( event.getRepository().getId() ) )
        {
            setSatisfied( false );
        }
    }

    @Override
    public String toString()
    {
        try
        {
            final String id = getRepositoryId();
            return String.format( "Repository '%s' is %s", id, proxyMode.toString() );
        }
        catch ( Exception ignore )
        {
            return String.format( "Repository '(could not be evaluated)' is %s", proxyMode.toString() );
        }
    }

    @Override
    public String explainSatisfied()
    {
        String mode = "not blocked";
        if ( proxyMode.equals( ProxyMode.BLOCKED_MANUAL ) )
        {
            mode = "manually blocked";
        }
        else if ( proxyMode.equals( ProxyMode.BLOCKED_AUTO ) )
        {
            mode = "auto blocked";
        }
        try
        {
            final String id = getRepositoryId();
            return String.format( "Repository '%s' is %s", id, mode );
        }
        catch ( Exception ignore )
        {
            return String.format( "Repository '(could not be evaluated)' is %s", mode );
        }
    }

    @Override
    public String explainUnsatisfied()
    {
        String mode = "blocked";
        if ( proxyMode.equals( ProxyMode.BLOCKED_MANUAL ) )
        {
            mode = "not manually blocked";
        }
        else if ( proxyMode.equals( ProxyMode.BLOCKED_AUTO ) )
        {
            mode = "not auto blocked";
        }
        try
        {
            final String id = getRepositoryId();
            return String.format( "Repository '%s' is %s", id, mode );
        }
        catch ( Exception ignore )
        {
            return String.format( "Repository '(could not be evaluated)' is %s", mode );
        }
    }

}
