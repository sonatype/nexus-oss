/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.plexus.appevents;

import java.util.Date;
import java.util.Map;

public interface Event<T>
{
    /**
     * Returns the timestamp of the creation of this event object. It's usage is left for consumer of this event (or
     * creator).
     * 
     * @return
     */
    Date getEventDate();

    /**
     * Returns the modifiable event context. It may be used for some sort of data or object passing between event
     * consumer. This interface is not guaranteeing any processing order, so it is left to user of this api to sort this
     * out.
     * 
     * @return
     */
    Map<Object, Object> getEventContext();

    /**
     * Returns the event sender/initiator.
     * 
     * @return
     */
    T getEventSender();
}
