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

package org.sonatype.nexus.events;

import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.ManagedHandler;
import org.sonatype.sisu.goodies.eventbus.EventBus;

/**
 * Automatically registers event handlers marked with {@link ManagedHandler} with {@link EventBus}.
 *
 * @since 2.3
 */
@Named
@Singleton
public class ManagedHandlers
{

    private final EventBus eventBus;

    private final Collection<ManagedHandler> handlers;

    @Inject
    public ManagedHandlers( final EventBus eventBus, final List<ManagedHandler> handlers )
    {
        this.eventBus = checkNotNull( eventBus );
        this.handlers = checkNotNull( handlers );
    }

    public ManagedHandlers register()
    {
        for ( final ManagedHandler handler : handlers )
        {
            eventBus.register( handler );
        }
        return this;
    }

    public ManagedHandlers unregister()
    {
        for ( final ManagedHandler handler : handlers )
        {
            eventBus.unregister( handler );
        }
        return this;
    }

}
