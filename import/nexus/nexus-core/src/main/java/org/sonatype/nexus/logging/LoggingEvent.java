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
package org.sonatype.nexus.logging;

import org.sonatype.plexus.appevents.Event;

/**
 * This {@link Event} is triggered when an message is logged. Usually only error and warnings will trigger it but this
 * behavior (threshold) can be changed in logging system configuration files.
 * 
 * @author adreghiciu@gmail.com
 */
public interface LoggingEvent
    extends Event<String>
{
    /**
     * @return logging level.
     */
    public Level getLevel();

    /**
     * @return logged message
     */
    public String getMessage();

    /**
     * @return throwable if logged, null otherwise
     */
    public Throwable getThrowable();

    /**
     * Logging level.
     * 
     * @author adreghiciu@gmail.com
     */
    public static enum Level
    {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

}
