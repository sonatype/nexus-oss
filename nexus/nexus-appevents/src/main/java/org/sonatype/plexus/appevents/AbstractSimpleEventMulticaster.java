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
package org.sonatype.plexus.appevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Support for {@link SimpleEventMulticaster} implementations.
 *
 * @author cstamas
 * @since 2.3
 */
public abstract class AbstractSimpleEventMulticaster
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<EventListener>();

    public void addEventListener(final EventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(final EventListener listener) {
        listeners.remove(listener);
    }

    public void notifyEventListeners(final Event<?> event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Notifying {} EventListener about event {} fired ({})", listeners.size(), event.getClass().getName(), event);
        }

        for (EventListener listener : listeners) {
            if (listener == null) continue;
            try {
                listener.onEvent(event);
            }
            catch (Exception e) {
                logger.info("Unexpected exception in listener {}, continuing listener notification.", listener.getClass().getName(), e);
            }
        }
    }
}
