/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logback.EventTarget;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * {@link EventTarget} that will post the incoming logging event to {@link EventBus}.
 * 
 * @author cstamas
 * @since 2.2
 */
@Component( role = EventTarget.class )
public class NexusEventSystemEventTarget
    implements EventTarget
{
    @Requirement
    private EventBus eventBus;

    @Override
    public void onEvent( final ILoggingEvent eventObject )
    {
        if ( eventBus != null )
        {
            eventBus.post( new LogbackLoggingEvent( eventObject ) );
        }
    }
}
