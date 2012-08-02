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
package org.sonatype.nexus.log.internal;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logback.EventTarget;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * {@link EventTarget} that will multicast the incoming logging event to {@link ApplicationEventMulticaster}.
 * 
 * @author cstamas
 * @since 2.2
 */
@Named
@Singleton
@Typed( EventTarget.class )
public class NexusEventSystemEventTarget
    implements EventTarget
{
    @Inject
    private ApplicationEventMulticaster eventMulticaster;

    @Override
    public void onEvent( final ILoggingEvent eventObject )
    {
        if ( eventMulticaster != null )
        {
            final LogbackLoggingEvent logEvent = new LogbackLoggingEvent( eventObject );
            eventMulticaster.notifyEventListeners( logEvent );
        }
    }
}
