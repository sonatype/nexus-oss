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
package org.sonatype.nexus.plugins.capabilities.support.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.RepositoryEventsNotifier;
import org.sonatype.nexus.plugins.capabilities.internal.activation.RepositoryExistsCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.RepositoryLocalStatusCondition;
import org.sonatype.nexus.proxy.repository.LocalStatus;

/**
 * Factory of {@link Condition}s related to repositories.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class RepositoryConditions
{

    private final RepositoryEventsNotifier repositoryEventsNotifier;

    private final ActivationContext activationContext;

    @Inject
    public RepositoryConditions( final ActivationContext activationContext,
                                 final RepositoryEventsNotifier repositoryEventsNotifier )
    {
        this.activationContext = checkNotNull( activationContext );
        this.repositoryEventsNotifier = checkNotNull( repositoryEventsNotifier );
    }

    /**
     * Creates a new condition that is satisfied when a repository is in service.
     *
     * @param repositoryId getter for repository id (usually condition specific property)
     * @return created condition
     */
    public Condition repositoryIsInService( final RepositoryId repositoryId )
    {
        return new RepositoryLocalStatusCondition(
            activationContext, repositoryEventsNotifier, LocalStatus.IN_SERVICE, repositoryId
        );
    }

    /**
     * Creates a new condition that is satisfied when a repository exists.
     *
     * @param repositoryId getter for repository id (usually condition specific property)
     * @return created condition
     */
    public Condition repositoryExists( final RepositoryId repositoryId )
    {
        return new RepositoryExistsCondition(
            activationContext, repositoryEventsNotifier, repositoryId
        );
    }

    public static interface RepositoryId
    {

        String get();

    }

}
