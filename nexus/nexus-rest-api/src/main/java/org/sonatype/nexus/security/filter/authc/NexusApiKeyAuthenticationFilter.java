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
