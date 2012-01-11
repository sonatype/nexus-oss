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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * {@link AuthenticatingFilter} that looks for credentials in known {@link NexusApiKey} HTTP headers.
 */
public class NexusApiKeyAuthenticationFilter
    extends NexusSecureHttpAuthenticationFilter
{
    private Collection<String> apiKeys;

    @Override
    protected void onFilterConfigSet()
        throws Exception
    {
        super.onFilterConfigSet();
        if ( null == apiKeys )
        {
            apiKeys = getPlexusContainer().lookupMap( NexusApiKey.class ).keySet();
        }
    }

    @Override
    protected boolean isLoginAttempt( ServletRequest request, ServletResponse response )
    {
        if ( null != apiKeys )
        {
            final HttpServletRequest http = WebUtils.toHttp( request );
            for ( final String key : apiKeys )
            {
                if ( null != http.getHeader( key ) )
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
        if ( null != apiKeys )
        {
            final HttpServletRequest http = WebUtils.toHttp( request );
            for ( final String key : apiKeys )
            {
                final String token = http.getHeader( key );
                if ( null != token )
                {
                    return new NexusApiKeyAuthenticationToken( key, token.toCharArray(), request.getRemoteHost() );
                }
            }
        }
        return super.createToken( request, response );
    }
}
