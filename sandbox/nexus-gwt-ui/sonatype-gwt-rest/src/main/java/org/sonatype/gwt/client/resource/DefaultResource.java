/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.gwt.client.resource;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.gwt.client.request.DefaultRESTRequestBuilder;
import org.sonatype.gwt.client.request.RESTRequestBuilder;
import org.sonatype.gwt.client.resource.PathUtils.UrlElements;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

/**
 * A default implementation of a Resource.
 * 
 * @author cstamas
 */
public class DefaultResource
    implements Resource
{

    private String resourceUri;

    private RESTRequestBuilder restRequestBuilder;

    private HashMap<String, String> headers = new HashMap<String, String>();

    /**
     * Create resource from an URL in form <code>http://localhost[:port][/some/path]</code>. This constructor will
     * set up the resource to access the REST Resource from infos extracted from an URL.
     * 
     * @param url
     */
    public DefaultResource( String url )
        throws IllegalArgumentException
    {
        super();

        UrlElements elems = PathUtils.parseUrl( url );

        this.resourceUri = elems.getPath();
        
        DefaultRESTRequestBuilder defaultBuilder = new DefaultRESTRequestBuilder();

        defaultBuilder.setScheme( elems.getScheme() );

        defaultBuilder.setHostname( elems.getHostname() );

        defaultBuilder.setPort( elems.getPort() );

        this.restRequestBuilder = defaultBuilder;
    }

    /**
     * Sets up a Resource that points to a resourceUri (a path actually) and with preexisting RESTRequestBuilder.
     * 
     * @param uri
     * @param restRequestBuilder
     */
    public DefaultResource( String uri, RESTRequestBuilder restRequestBuilder )
    {
        super();

        this.resourceUri = PathUtils.normalize( uri );

        this.restRequestBuilder = restRequestBuilder;
    }

    public RESTRequestBuilder getRestRequestBuilder()
    {
        return restRequestBuilder;
    }

    public String getPath()
    {
        return resourceUri;
    }

    protected Request pull( RequestCallback callback, RequestBuilder builder )
    {
        try
        {
            return builder.sendRequest( null, callback );
        }
        catch ( RequestException e )
        {
            callback.onError( null, e );

            return null;
        }
    }

    protected Request push( RequestCallback callback, Representation representation, RequestBuilder builder )
    {
        try
        {
            return builder.sendRequest( representation.getText(), callback );
        }
        catch ( RequestException e )
        {
            callback.onError( null, e );

            return null;
        }
    }

    public Request get( RequestCallback callback, Variant variant )
    {
        return pull( callback, restRequestBuilder.buildGet( this, variant ) );
    }

    public Request head( RequestCallback callback, Variant variant )
    {
        return pull( callback, restRequestBuilder.buildHead( this, variant ) );
    }

    public Request put( RequestCallback callback, Representation representation )
    {
        return push( callback, representation, restRequestBuilder.buildPut( this, representation ) );
    }

    public Request post( RequestCallback callback, Representation representation )
    {
        return push( callback, representation, restRequestBuilder.buildPut( this, representation ) );
    }

    public Request delete( RequestCallback callback )
    {
        try
        {
            RequestBuilder builder = restRequestBuilder.buildDelete( this );

            return builder.sendRequest( null, callback );
        }
        catch ( RequestException e )
        {
            callback.onError( null, e );

            return null;
        }

    }

    public Resource getChild( String id )
    {
        return getResource( id );
    }

    public Resource getParent()
    {
        return getResource( ".." );
    }

    public Resource getResource( String resourceUri )
    {
        return new DefaultResource( PathUtils.append( getPath(), resourceUri ), getRestRequestBuilder() );
    }

    public void addHeader( String name, String value )
    {
        headers.put( name, value );
    }

    public void addHeaders( Map<String, String> otherHeaders )
    {
        headers.putAll( otherHeaders );
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

}
