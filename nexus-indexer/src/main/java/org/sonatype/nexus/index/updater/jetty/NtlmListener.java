/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.sonatype.nexus.index.updater.jetty;

import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpEventListenerWrapper;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Buffer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * WagonListener
 * 
 * Detect the NTLM authentication scheme and switch to HttpURLConnection
 */
public class NtlmListener
    extends HttpEventListenerWrapper
{
    private static NtlmConnectionHelper _helper;

    private final HttpExchange _exchange;

    private final Set<String> _authTypes;

    private boolean _requestComplete;

    private boolean _responseComplete;

    private boolean _unAuthorized;

    public NtlmListener( final HttpDestination destination, final HttpExchange ex )
    {
        // Start of sending events through to the wrapped listener
        // Next decision point is the onResponseStatus
        super( ex.getEventListener(), true );

        _authTypes = new HashSet<String>();
        _exchange = ex;
    }

    /**
     * scrapes an authentication type from the authString
     * 
     * @param authString
     * @return
     */
    protected String scrapeAuthenticationType( final String authString )
    {
        int idx = authString.indexOf( " " );
        return ( idx < 0 ? authString : authString.substring( 0, idx ) ).trim().toLowerCase();
    }

    @Override
    public void onResponseStatus( final Buffer version, final int status, final Buffer reason )
        throws IOException
    {
        _unAuthorized = ( status == HttpStatus.UNAUTHORIZED_401 );

        if ( _unAuthorized )
        {
            setDelegatingRequests( false );
            setDelegatingResponses( false );
        }

        super.onResponseStatus( version, status, reason );
    }

    @Override
    public void onResponseHeader( final Buffer name, final Buffer value )
        throws IOException
    {
        if ( _unAuthorized )
        {
            int header = HttpHeaders.CACHE.getOrdinal( name );
            switch ( header )
            {
                case HttpHeaders.WWW_AUTHENTICATE_ORDINAL:
                    String authString = value.toString();
                    _authTypes.add( scrapeAuthenticationType( authString ) );
                    break;
            }
        }
        super.onResponseHeader( name, value );
    }

    @Override
    public void onRequestComplete()
        throws IOException
    {
        _requestComplete = true;
        checkExchangeComplete();

        super.onRequestComplete();
    }

    @Override
    public void onResponseComplete()
        throws IOException
    {
        _responseComplete = true;
        checkExchangeComplete();

        super.onResponseComplete();
    }

    public void checkExchangeComplete()
        throws IOException
    {
        if ( _unAuthorized && _requestComplete && _responseComplete )
        {
            setDelegatingRequests( true );
            setDelegatingResponses( true );

            if ( _helper != null && _authTypes.contains( "ntlm" ) && _exchange instanceof ResourceExchange )
            {
                _helper.send( (ResourceExchange) _exchange );
            }
            else
            {
                setDelegationResult( false );
            }
        }
    }

    public static void setHelper( final NtlmConnectionHelper helper )
    {
        _helper = helper;
    }
}
