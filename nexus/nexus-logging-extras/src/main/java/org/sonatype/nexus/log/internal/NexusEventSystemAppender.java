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

import javax.inject.Inject;

import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appender that will multicast the logging event.
 * 
 * @author adreghiciu@gmail.com
 */
public class NexusEventSystemAppender
    extends UnsynchronizedAppenderBase<ILoggingEvent>
{

    @Inject
    private ApplicationEventMulticaster eventMulticaster;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void append( ILoggingEvent eventObject )
    {
        if ( eventMulticaster != null )
        {
            LogbackLoggingEvent logEvent = new LogbackLoggingEvent( eventObject );
            eventMulticaster.notifyEventListeners( logEvent );
        }
    }

}
