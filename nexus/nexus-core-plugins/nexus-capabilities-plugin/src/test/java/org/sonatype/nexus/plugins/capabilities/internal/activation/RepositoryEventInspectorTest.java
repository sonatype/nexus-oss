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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryEvent;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * {@link RepositoryEventInspector} UTs.
 *
 * @since 1.10.0
 */
public class RepositoryEventInspectorTest
{

    private RepositoryEventInspector underTest;

    private Repository repository;

    private RepositoryEventsNotifier repositoryEventsNotifier;

    @Before
    public void setUp()
    {
        repository = mock( Repository.class );
        repositoryEventsNotifier = mock( RepositoryEventsNotifier.class );
        underTest = new RepositoryEventInspector( repositoryEventsNotifier );
    }

    /**
     * Notifier called on a supported event.
     */
    @Test
    public void notifiedOnUpdate()
    {
        underTest.inspect( new RepositoryConfigurationUpdatedEvent( repository ) );
        verify( repositoryEventsNotifier ).notify(
            eq( repository ), Matchers.<RepositoryEventsNotifier.Notifier>any()
        );
    }

    /**
     * No notifications for unsupported events
     */
    @Test
    public void notNotifiedOnUnneededEvents()
    {
        underTest.inspect( new RepositoryEvent( repository )
        {
        } );
        verifyNoMoreInteractions( repositoryEventsNotifier );
    }

}
