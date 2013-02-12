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
package org.sonatype.nexus.plugins.bcprov.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.security.Security;

import javax.inject.Inject;
import javax.inject.Named;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

/**
 * Guava {@link EventBus} handler that listens for Nexus events and performs BC provider registration/removal. This
 * component is marked as {@code EagerSingleton} to be created (and hence to have registration happen) as early as
 * possible, even before any wiring happens in plugins.
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@EagerSingleton
public class BCPluginEventHandler
{
    private final Logger logger;

    private final boolean uninstallBouncyCastleProvider;

    /**
     * Default constructor.
     * 
     * @param eventBus the {@link EventBus} to register with.
     */
    @Inject
    public BCPluginEventHandler( final EventBus eventBus )
    {
        checkNotNull( eventBus );
        this.logger = LoggerFactory.getLogger( getClass() );
        // register BC and nag if already installed
        uninstallBouncyCastleProvider = Security.addProvider( new BouncyCastleProvider() ) != -1;
        if ( !uninstallBouncyCastleProvider )
        {
            logger.info( "BC provider is already registered wih JCE by another party. This might lead to problems if registered version is not the one expected by Nexus!" );
        }
        eventBus.register( this );
    }

    /**
     * {@link NexusStoppedEvent} handler: unregisters BC provider if needed (if it was registered by us, not by some 3rd
     * party).
     * 
     * @param e the event (not used)
     */
    @Subscribe
    public void onNexusStoppedEvent( final NexusStoppedEvent e )
    {
        if ( uninstallBouncyCastleProvider )
        {
            logger.info( "Removing BC Provider from JCE..." );
            Security.removeProvider( BouncyCastleProvider.PROVIDER_NAME );
        }
        else
        {
            logger.info( "Not removing BC Provider from JCE as it was registered by some other party..." );
        }
    }
}
