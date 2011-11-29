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

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;
import org.sonatype.nexus.plugins.capabilities.support.activation.RepositoryConditions;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A condition that is satisfied when a repository exists.
 *
 * @since 1.10.0
 */
public class RepositoryExistsCondition
    extends AbstractCondition
    implements RepositoryEventsNotifier.Listener
{

    private final RepositoryEventsNotifier repositoryEventsNotifier;

    private final RepositoryConditions.RepositoryId repositoryId;

    public RepositoryExistsCondition( final ActivationContext activationContext,
                                      final RepositoryEventsNotifier repositoryEventsNotifier,
                                      final RepositoryConditions.RepositoryId repositoryId )
    {
        super( activationContext, false );
        this.repositoryEventsNotifier = checkNotNull( repositoryEventsNotifier );
        this.repositoryId = checkNotNull( repositoryId );
    }

    @Override
    protected void doBind()
    {
        repositoryEventsNotifier.addListener( this );
    }

    @Override
    public void doRelease()
    {
        repositoryEventsNotifier.removeListener( this );
    }

    @Override
    public void onAdded( final Repository repository )
    {
        if ( repository != null && repository.getId().equals( repositoryId.get() ) )
        {
            setSatisfied( true );
        }
    }

    @Override
    public void onUpdated( final Repository repository )
    {
        // do nothing
    }

    @Override
    public void onRemoved( final Repository repository )
    {
        if ( repository != null && repository.getId().equals( repositoryId.get() ) )
        {
            setSatisfied( false );
        }
    }

    @Override
    public String toString()
    {
        try
        {
            final String id = repositoryId.get();
            return String.format( "Repository '%s' exists", id );
        }
        catch ( Exception ignore )
        {
            return "Repository '(could not be evaluated)' exists";
        }
    }

}
