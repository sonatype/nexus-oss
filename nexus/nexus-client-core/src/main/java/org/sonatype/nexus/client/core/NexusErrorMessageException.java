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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when Nexus responds with an Client Error status code and (optionally) with an error message.
 *
 * @since 2.1
 */
@SuppressWarnings( "serial" )
public class NexusErrorMessageException
    extends NexusUnexpectedResponseException
{

    private final Map<String, String> errors;

    public NexusErrorMessageException( final int statusCode, final String statusMessage,
                                       final Map<String, String> errors )
    {
        super( statusCode, statusMessage, "Nexus Error Response!" );
        this.errors = initErrors( errors );
    }

    protected Map<String, String> initErrors( final Map<String, String> errors )
    {
        final Map<String, String> result = new LinkedHashMap<String, String>();
        if ( errors != null && !errors.isEmpty() )
        {
            result.putAll( errors );
        }
        return result;
    }

    public Map<String, String> getErrors()
    {
        return Collections.unmodifiableMap( errors );
    }
}