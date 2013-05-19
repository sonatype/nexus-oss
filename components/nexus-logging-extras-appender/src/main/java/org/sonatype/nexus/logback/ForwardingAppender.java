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
package org.sonatype.nexus.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * A forwarding appender for Logback, that forwards {@link ILoggingEvent} events it gets as Appender, to it's target.
 * 
 * @author cstamas
 * @since 2.2
 */
public class ForwardingAppender
    extends UnsynchronizedAppenderBase<ILoggingEvent>
{
    private EventTarget target;

    /**
     * Default constructor that creates and resets this instance.
     */
    public ForwardingAppender()
    {
        reset();
    }

    @Override
    protected void append( final ILoggingEvent event )
    {
        target.onEvent( event );
    }

    // ==

    /**
     * Installs or replaces an {@link EventTarget} to have events forwarded to it.
     * 
     * @param target
     */
    public void installEventTarget( final EventTarget target )
    {
        if ( target != null )
        {
            this.target = target;
        }
        else
        {
            reset();
        }
    }

    /**
     * Performs a "reset" of this forwarding appender, and previously installed {@link EventTarget} (if any) will stop
     * receiving events.
     */
    public void reset()
    {
        this.target = NoopEventTarget.INSTANCE;
    }
}
