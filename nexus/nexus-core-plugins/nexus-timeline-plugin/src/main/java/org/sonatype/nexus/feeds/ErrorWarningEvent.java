/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
