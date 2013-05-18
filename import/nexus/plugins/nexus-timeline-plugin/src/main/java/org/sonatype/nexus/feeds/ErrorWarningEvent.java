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
package org.sonatype.nexus.feeds;

import java.util.Date;

/**
 * A class that encapsulates all nexus errors and warnings
 * 
 * @author juven
 */
public class ErrorWarningEvent
    extends AbstractEvent
{
    public static final String ACTION_ERROR = "error";

    public static final String ACTION_WARNING = "warning";

    private final String stackTrace;

    public ErrorWarningEvent( final Date eventDate, final String action, final String message, final String stackTrace )
    {
        super( eventDate, action, message );

        this.stackTrace = stackTrace;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }
}
