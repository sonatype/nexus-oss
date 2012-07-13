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
package org.sonatype.nexus.client.core;

import org.sonatype.nexus.client.internal.util.Check;

/**
 * Generic runtime exception to be thrown by Subsystems, when some unexpected error is reported by Nexus. This exception
 * here is solely for purpose of not proliferating possible runtime exceptions of underlying implementation. Best to use
 * some subclass of this exception, but this is the last resort (ie. resource does not send proper response, just HTTP
 * code and reason phrase).
 * 
 * @author cstamas
 */
@SuppressWarnings( "serial" )
public class NexusUnexpectedResponseException
    extends NexusClientException
{
    private final int statusCode;

    private final String statusMessage;

    public NexusUnexpectedResponseException( final int statusCode, final String statusMessage )
    {
        super( statusMessage );
        this.statusCode = Check.argument( statusCode > 0, statusCode, "statusCode not positive" );
        this.statusMessage = Check.notBlank( statusMessage, "statusMessage" );
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }
}
