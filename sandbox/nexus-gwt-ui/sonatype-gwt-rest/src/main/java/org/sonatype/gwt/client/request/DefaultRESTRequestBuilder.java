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
package org.sonatype.gwt.client.request;

import java.util.Map;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;

import com.google.gwt.http.client.RequestBuilder;

public class DefaultRESTRequestBuilder
    implements RESTRequestBuilder
{

    private String scheme = "http";

    private String hostname = "localhost";

    private String port = "8081";

    protected class FullRequestBuilder
        extends RequestBuilder
    {
        public FullRequestBuilder( String httpMethod, String url )
        {
            super( httpMethod, url );
        }
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname( String hostname )
    {
        this.hostname = hostname;
    }

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme( String scheme )
    {
        this.scheme = scheme;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort( String port )
    {
        this.port = port;
    }

    protected String getUrl( String path )
    {
        String url = getScheme() + "://" + getHostname();

        if ( getPort() != null )
        {
            url = url + ":" + getPort();
        }

        if ( path.startsWith( "/" ) )
        {
            url = url + path;
        }
        else
        {
            url = url + "/" + path;
        }

        return url;
    }

    public RequestBuilder buildDelete( Resource resource )
    {
        return build( "DELETE", resource, null );
    }

    public RequestBuilder buildGet( Resource resource, Variant variant )
    {
        return build( "GET", resource, variant );
    }

    public RequestBuilder buildHead( Resource resource, Variant variant )
    {
        return build( "HEAD", resource, variant );
    }

    public RequestBuilder buildPost( Resource resource, Variant variant )
    {
        return build( "POST", resource, variant );
    }

    public RequestBuilder buildPut( Resource resource, Variant variant )
    {
        return build( "PUT", resource, variant );
    }

    protected RequestBuilder build( String method, Resource resource, Variant variant )
    {
        RequestBuilder result = new FullRequestBuilder( method, getUrl( resource.getPath() ) );

        if ( variant != null )
        {
            result.setHeader( "Accept", variant.getMediaType() );

            result.setHeader( "Content-Type", variant.getMediaType() );
        }

        for ( Map.Entry<String, String> header : resource.getHeaders().entrySet() )
        {
            result.setHeader( header.getKey(), header.getValue() );
        }

        return result;
    }

}
