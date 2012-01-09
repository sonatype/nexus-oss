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

import java.util.Map;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;

import com.google.common.collect.ImmutableMap;

/**
 * {@link AuthenticationToken} that contains credentials from one or more API-Keys.
 */
public class NexusApiKeyAuthenticationToken
    implements HostAuthenticationToken
{
    private static final long serialVersionUID = 1L;

    private final Map<String, char[]> keys;

    private final String host;

    public NexusApiKeyAuthenticationToken( final Map<String, char[]> keys, final String host )
    {
        this.keys = ImmutableMap.copyOf( keys );
        this.host = host;
    }

    public Object getPrincipal()
    {
        return keys.keySet();
    }

    public Object getCredentials()
    {
        return keys.values();
    }

    public char[] getApiKey( final String hint )
    {
        return keys.get( hint );
    }

    public String getHost()
    {
        return host;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder( getClass().getName() );
        buf.append( " - " ).append( getPrincipal() );
        if ( host != null )
        {
            buf.append( " (" ).append( host ).append( ")" );
        }
        return buf.toString();
    }
}
