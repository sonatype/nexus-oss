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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.RepositoryEventsNotifier;
import org.sonatype.nexus.plugins.capabilities.internal.activation.RepositoryExistsCondition;
import org.sonatype.nexus.plugins.capabilities.internal.activation.RepositoryLocalStatusCondition;

/**
 * {@link RepositoryConditions} UTs.
 *
 * @since 1.10.0
 */
public class RepositoryConditionsTest
{

    /**
     * repositoryIsInService() factory method returns expected condition.
     */
    @Test
    public void repositoryIsInService()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final RepositoryEventsNotifier repositoryEventsNotifier = mock( RepositoryEventsNotifier.class );
        final RepositoryConditions underTest = new RepositoryConditions(
            activationContext, repositoryEventsNotifier
        );

        assertThat(
            underTest.repositoryIsInService( mock( RepositoryConditions.RepositoryId.class ) ),
            is( Matchers.<Condition>instanceOf( RepositoryLocalStatusCondition.class ) )
        );
    }

    /**
     * repositoryExists() factory method returns expected condition.
     */
    @Test
    public void repositoryExists()
    {
        final ActivationContext activationContext = mock( ActivationContext.class );
        final RepositoryEventsNotifier repositoryEventsNotifier = mock( RepositoryEventsNotifier.class );
        final RepositoryConditions underTest = new RepositoryConditions(
            activationContext, repositoryEventsNotifier
        );

        assertThat(
            underTest.repositoryExists( mock( RepositoryConditions.RepositoryId.class ) ),
            is( Matchers.<Condition>instanceOf( RepositoryExistsCondition.class ) )
        );
    }

}
