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
package org.sonatype.nexus.security.filter.authc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.sonatype.nexus.rest.RemoteIPFinder;

/**
 * {@link AuthenticatingFilter} that looks for credentials in {@link NexusApiKey} HTTP headers.
 */
public class NexusApiKeyAuthenticationFilter
    extends NexusSecureHttpAuthenticationFilter
{
    private Collection<String> keys;

    @Override
    protected void onFilterConfigSet()
        throws Exception
    {
        super.onFilterConfigSet();
        try
        {
            keys = getPlexusContainer().lookupMap( NexusApiKey.class ).keySet();
        }
        catch ( final Exception ignore )
        {
            // ignore...
        }
    }

    @Override
    protected boolean isLoginAttempt( ServletRequest request, ServletResponse response )
    {
        if ( null != keys )
        {
            final HttpServletRequest http = WebUtils.toHttp( request );
            for ( final String k : keys )
            {
                if ( null != http.getHeader( k ) )
                {
                    return true;
                }
            }
        }
        return super.isLoginAttempt( request, response );
    }

    @Override
    protected AuthenticationToken createToken( final ServletRequest request, final ServletResponse response )
    {
        if ( null != keys )
        {
            final HttpServletRequest http = WebUtils.toHttp( request );
            final Map<String, char[]> credentials = new HashMap<String, char[]>();
            for ( final String k : keys )
            {
                final String guid = http.getHeader( k );
                if ( null != guid )
                {
                    credentials.put( k, guid.toCharArray() );
                }
            }
            if ( !credentials.isEmpty() )
            {
                return new NexusApiKeyAuthenticationToken( credentials, RemoteIPFinder.findIP( http ) );
            }
        }
        return super.createToken( request, response );
    }
}
