/*
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
package org.sonatype.nexus.apachehttpclient;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.protocol.HttpContext;

/**
 * @since 2.5
 */
class UrlSchemeAwareHttpRoutePlanner
    implements HttpRoutePlanner
{

    private final Map<String, HttpRoutePlanner> routePlanners;

    private final HttpRoutePlanner defaultRoutPlanner;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    UrlSchemeAwareHttpRoutePlanner( final SchemeRegistry schemeRegistry,
                                    final Map<String, HttpRoutePlanner> routePlanners )
    {
        this.routePlanners = checkNotNull( routePlanners );
        this.defaultRoutPlanner = new DefaultHttpRoutePlanner( schemeRegistry );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public HttpRoute determineRoute( final HttpHost target, final HttpRequest request, final HttpContext context )
        throws HttpException
    {
        HttpRoutePlanner routePlanner = routePlanners.get( target.getSchemeName() );
        if ( routePlanner == null )
        {
            routePlanner = defaultRoutPlanner;
        }

        return routePlanner.determineRoute( target, request, context );
    }

}
