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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * {@link RepositoryEventsNotifier} UTs.
 *
 * @since 1.10.0
 */
public class RepositoryEventsNotifierTest
{

    private RepositoryEventsNotifier underTest;

    private RepositoryEventsNotifier.Listener listener;

    private Repository repository;

    private RepositoryEventsNotifier.Notifier notifier;

    @Before
    public void setUp()
    {
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        when( repositoryRegistry.getRepositories() ).thenReturn( Collections.<Repository>emptyList() );
        repository = mock( Repository.class );
        notifier = mock( RepositoryEventsNotifier.Notifier.class );
        listener = mock( RepositoryEventsNotifier.Listener.class );
        underTest = new RepositoryEventsNotifier( repositoryRegistry );
        underTest.addListener( listener );
    }

    /**
     * run() called on notifier when requested.
     */
    @Test
    public void runCalledOnNotifier()
    {
        underTest.notify( repository, notifier );
        verify( notifier ).run( listener, repository );
    }

    /**
     * When there are no listeners run() is not called.
     */
    @Test
    public void runNotCalledWhenNoListeners()
    {
        underTest.removeListener( listener );
        verifyNoMoreInteractions( notifier );
    }

}
