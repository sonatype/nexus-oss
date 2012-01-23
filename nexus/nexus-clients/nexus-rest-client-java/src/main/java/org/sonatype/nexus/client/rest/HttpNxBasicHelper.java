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
package org.sonatype.nexus.client.rest;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.util.Series;

import com.noelios.restlet.authentication.AuthenticationHelper;
import com.noelios.restlet.util.Base64;

/**
 * A copy+pasted "helper" class since the restlet engine is unfriendly for any extensions in this matter.
 * 
 * @author cstamas
 */
public class HttpNxBasicHelper
    extends AuthenticationHelper
{
    public HttpNxBasicHelper()
    {
        super( new ChallengeScheme( "HTTP_NxBasic", "NxBasic", "Nexus modified HTTP Basic" ), true, true );
    }

    @Override
    public void formatCredentials( StringBuilder sb, ChallengeResponse challenge, Request request,
        Series<Parameter> httpHeaders )
    {
        try
        {
            final String credentials = challenge.getIdentifier() + ':' + new String( challenge.getSecret() );
            sb.append( Base64.encode( credentials.getBytes( "US-ASCII" ), false ) );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new RuntimeException( "Unsupported encoding, unable to encode credentials" );
        }
    }

    @Override
    public void parseResponse( ChallengeResponse cr, Request request )
    {
        try
        {
            final byte[] credentialsEncoded = Base64.decode( cr.getCredentials() );
            if ( credentialsEncoded == null )
            {
                getLogger().warning( "Cannot decode credentials: " + cr.getCredentials() );
            }

            final String credentials = new String( credentialsEncoded, "US-ASCII" );
            final int separator = credentials.indexOf( ':' );

            if ( separator == -1 )
            {
                // Log the blocking
                getLogger().warning(
                    "Invalid credentials given by client with IP: "
                        + ( ( request != null ) ? request.getClientInfo().getAddress() : "?" ) );
            }
            else
            {
                cr.setIdentifier( credentials.substring( 0, separator ) );
                cr.setSecret( credentials.substring( separator + 1 ) );
            }
        }
        catch ( final UnsupportedEncodingException e )
        {
            getLogger().log( Level.WARNING, "Unsupported encoding error", e );
        }
    }
}
