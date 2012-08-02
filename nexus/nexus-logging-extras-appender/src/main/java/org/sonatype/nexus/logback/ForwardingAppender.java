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
package org.sonatype.nexus.logback;

import javax.inject.Inject;
import javax.inject.Named;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * A forwarding appender for Logback, that forwards {@link ILoggingEvent} events it gets as Appender, to it's target.
 * 
 * @author cstamas
 * @since 2.2
 */
@Named
public class ForwardingAppender
    extends UnsynchronizedAppenderBase<ILoggingEvent>
{
    @Inject
    private EventTarget target;

    @Override
    protected void append( final ILoggingEvent event )
    {
        if ( target != null )
        {
            target.onEvent( event );
        }
    }
}
