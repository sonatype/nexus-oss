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
package org.sonatype.nexus.proxy.storage.remote.httpclient;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.protocol.HttpContext;
import com.google.common.base.Preconditions;

/**
 * An {@link HttpRoutePlanner} that bypasses proxy for specific hosts.
 *
 * @since 1.10.0
 */
class NonProxyHostsAwareHttpRoutePlanner
    extends DefaultHttpRoutePlanner
{

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /**
     * Set of patterns for matching hosts names against. Never null.
     */
    private final Set<Pattern> nonProxyHostPatterns;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NonProxyHostsAwareHttpRoutePlanner( final SchemeRegistry schemeRegistry,
                                        final Set<Pattern> nonProxyHostPatterns )
    {
        super( schemeRegistry );
        this.nonProxyHostPatterns = Preconditions.checkNotNull( nonProxyHostPatterns );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public HttpRoute determineRoute( final HttpHost target,
                                     final HttpRequest request,
                                     final HttpContext context )
        throws HttpException
    {
        Object proxy = null;
        if ( noProxyFor( target.getHostName() ) )
        {
            proxy = request.getParams().getParameter( ConnRouteParams.DEFAULT_PROXY );
            if ( proxy != null )
            {
                request.getParams().removeParameter( ConnRouteParams.DEFAULT_PROXY );
            }
        }
        try
        {
            return super.determineRoute( target, request, context );
        }
        finally
        {
            if ( proxy != null )
            {
                request.getParams().setParameter( ConnRouteParams.DEFAULT_PROXY, proxy );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private boolean noProxyFor( final String hostName )
    {
        for ( final Pattern nonProxyHostPattern : nonProxyHostPatterns )
        {
            if ( nonProxyHostPattern.matcher( hostName ).matches() )
            {
                return true;
            }
        }
        return false;
    }

}
