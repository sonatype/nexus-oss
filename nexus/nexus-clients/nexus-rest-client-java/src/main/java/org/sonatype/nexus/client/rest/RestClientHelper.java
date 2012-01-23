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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.rest.XStreamInitializer;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;

public class RestClientHelper
{
    private final Context restContext;

    private final Client restClient;

    private ChallengeResponse challenge;

    private String baseUrl;

    private static final String SERVICE_URL_PART = "service/local/";

    private Logger logger = LoggerFactory.getLogger( getClass() );

    private XStream xstream;

    public RestClientHelper( String baseUrl, String username, String password )
    {
        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
        ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );
        this.challenge = authentication;
        this.baseUrl = baseUrl;
        this.restContext = new Context();
        this.restClient = new Client( restContext, Protocol.HTTP );

        xstream = org.sonatype.nexus.rest.model.XStreamConfigurator.configureXStream( new XStream( new LookAheadXppDriver() ) );

        XStreamInitializer.init( xstream );
    }

    private String buildUrl( String service, String id )
    {
        // build the url
        StringBuffer urlBuffer = new StringBuffer( this.baseUrl );

        // make sure we have a trailing / in the baseurl
        if ( !urlBuffer.toString().endsWith( "/" ) )
        {
            urlBuffer.append( "/" );
        }

        urlBuffer.append( SERVICE_URL_PART ).append( service );

        // if this is a POST we don't know the Id, otherwise we do.
        if ( id != null )
        {
            urlBuffer.append( "/" ).append( id );
        }
        return urlBuffer.toString();
    }

    private String buildParamString( Map<String, String> args )
        throws UnsupportedEncodingException
    {
        StringBuffer params = new StringBuffer( "?" );

        for ( Iterator<Entry<String, String>> iter = args.entrySet().iterator(); iter.hasNext(); )
        {
            Entry<String, String> entry = iter.next();
            params.append( entry.getKey() ).append( "=" ).append( URLEncoder.encode( entry.getValue(), "UTF-8" ) ).append(
                                                                                                                           "&" );
        }
        return params.toString();
    }

    public void delete( String service, String id )
        throws NexusClientException
    {
        String url = this.buildUrl( service, id );
        Response response = this.sendRequest( Method.DELETE, url, (Representation) null );

        if ( !response.getStatus().isSuccess() )
        {
            throw new NexusClientException( "Error in response from the server: " + response.getStatus() );
        }
        // thats it
    }

    public Object get( String service, String id )
        throws NexusClientException, NexusConnectionException
    {
        String url = this.buildUrl( service, id );
        return this.sendMessage( Method.GET, url, (NexusResponse) null );
    }

    public Object get( String service, Map<String, String> args )
        throws NexusClientException, NexusConnectionException
    {
        String url;
        try
        {
            url = this.buildUrl( service, null ) + this.buildParamString( args );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new NexusClientException( e.getMessage(), e );
        }
        return this.sendMessage( Method.GET, url, (NexusResponse) null );
    }

    public Object getList( String service )
        throws NexusClientException, NexusConnectionException
    {
        String url = this.buildUrl( service, "" );
        return this.sendMessage( Method.GET, url, (NexusResponse) null );
    }

    public Object create( String service, String id, NexusResponse nexusResponse )
        throws NexusConnectionException
    {
        return this.sendMessage( Method.POST, this.buildUrl( service, null ), nexusResponse );

        // FIXME: The repositories service doesn't return anything for create, see NEXUS-540
    }

    public Object update( String service, String id, NexusResponse nexusResponse )
        throws NexusConnectionException
    {
        return this.sendMessage( Method.PUT, this.buildUrl( service, id ), nexusResponse );
    }

    public Object sendCommand( String service, String command )
        throws NexusConnectionException
    {
        return this.sendMessage( Method.PUT, this.buildUrl( service, "command" ),
                                 new StringRepresentation( command, MediaType.TEXT_ALL ) );
    }

    public Response sendRequest( Method method, String url, Representation representation )
    {
        this.logger.debug( "Method: " + method.getName() + " url: " + url );

        Request request = new Request();
        request.setResourceRef( url );
        request.setMethod( method );

        if ( !Method.GET.equals( method ) && !Method.DELETE.equals( method ) )
        {
            request.setEntity( representation );
        }

        request.setChallengeResponse( this.challenge );

        return this.restClient.handle( request );
    }

    public Object sendMessage( Method method, String url, NexusResponse nexusResponse )
        throws NexusConnectionException
    {
        XStreamRepresentation representation = new XStreamRepresentation( this.xstream, "", MediaType.APPLICATION_XML );
        // now set the payload
        representation.setPayload( nexusResponse );

        return this.sendMessage( method, url, representation );

    }

    @SuppressWarnings( "unchecked" )
    public Object sendMessage( Method method, String url, Representation representation )
        throws NexusConnectionException
    {

        // get the response
        Response response = this.sendRequest( method, url, representation );

        // always expect a success
        if ( !response.getStatus().isSuccess() )
        {
            String errorMessage = "Error in response from server: " + response.getStatus() + ".";
            List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
            try
            {
                if ( response.getEntity() != null )
                {

                    String responseText = response.getEntity().getText();

                    // this is kinda hackish, but this class is already tied to xstream
                    if ( responseText.contains( "<error" ) ) // quick check before we parse the string
                    {
                        // try to parse the response
                        ErrorResponse errorResponse =
                            (ErrorResponse) this.xstream.fromXML( responseText, new ErrorResponse() );
                        // if we made it this far we can stick the ErrorMessages in the Exception
                        errors = errorResponse.getErrors();
                    }
                    else
                    {
                        // the response text might be helpful in debugging, so we will add it
                        errorMessage +=
                            "\nResponse: " + ( !StringUtils.isEmpty( responseText ) ? "\n" + responseText : "<empty>" );
                    }
                }
                else
                {
                    errorMessage = response.getStatus().getName();
                }
            }
            catch ( Exception e ) // we really don't want our fancy exception to cause another problem.
            {
                logger.warn( "Error getting the response text: " + e.getMessage(), e );
            }

            // now finally throw it...
            throw new NexusConnectionException( errorMessage, errors );
        }

        Object result = null;
        try
        {
            String responseText = response.getEntity().getText();
            if ( StringUtils.isNotEmpty( responseText ) )
            {
                result = this.xstream.fromXML( responseText );
            }
        }
        catch ( IOException e )
        {
            throw new NexusConnectionException( "Error getting response text: " + e.getMessage(), e );
        }
        return result;
    }

}
