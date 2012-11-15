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
package org.sonatype.nexus.client.core.exception;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

import org.sonatype.nexus.client.internal.msg.ErrorMessage;
import org.sonatype.nexus.client.internal.msg.ErrorResponse;

/**
 * @since 2.3
 */
public class NexusClientErrorResponseException
    extends NexusClientResponseException
{

    private final List<ErrorMessage> errors;

    public NexusClientErrorResponseException( final String reasonPhrase,
                                              final String responseBody,
                                              final ErrorResponse response )
    {
        super( message( response ), Response.Status.BAD_REQUEST.getStatusCode(), reasonPhrase, responseBody );
        errors = Collections.unmodifiableList(
            response.getErrors() == null ? Collections.<ErrorMessage>emptyList() : response.getErrors()
        );
    }

    private static String message( final ErrorResponse response )
    {
        final List<ErrorMessage> errors = response.getErrors();
        if ( errors != null && !errors.isEmpty() )
        {
            final StringBuilder sb = new StringBuilder();
            for ( final ErrorMessage error : errors )
            {
                if ( sb.length() > 0 )
                {
                    sb.append( "\n" );
                }
                if ( !"*".equals( error.getId() ) )
                {
                    sb.append( "[" ).append( error.getId() ).append( "] " );
                }
                sb.append( error.getMsg() );
            }
            if ( errors.size() > 1 )
            {
                sb.insert( 0, "\n" );
            }
            return sb.toString();
        }
        return "Unknown";
    }

    public List<ErrorMessage> errors()
    {
        return errors;
    }

}
