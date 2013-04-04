/*
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
package org.sonatype.nexus.plugins.siesta;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.siesta.SiestaModule.MOUNT_POINT;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.inject.EagerSingleton;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.security.web.ProtectedPathManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Configures Siesta protected resources.
 *
 * @since 2.5.0
 */
@Named
@EagerSingleton
public class SiestaProtectedPaths
{

    private final EventBus eventBus;

    private final Provider<ProtectedPathManager> protectedPathManager;

    @Inject
    public SiestaProtectedPaths( final EventBus eventBus,
                                 final Provider<ProtectedPathManager> protectedPathManager )
    {
        this.eventBus = checkNotNull( eventBus );
        this.protectedPathManager = checkNotNull( protectedPathManager );

        eventBus.register( this );
    }

    @Subscribe
    public void onEvent( final NexusStartedEvent event )
    {
        protectedPathManager.get().addProtectedResource(
            MOUNT_POINT + "/**", "noSessionCreation,authcBasic"
        );
    }

    @Subscribe
    public void onEvent( final NexusStoppedEvent evt )
    {
        eventBus.unregister( this );
    }


}
