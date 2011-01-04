/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.http;

import org.sonatype.nexus.configuration.model.CHttpProxySettings;

/**
 * HTTP Proxy policy, that defines how proxy work/behave in case of non-resolvable URLs.
 * 
 * @author cstamas
 */
public enum HttpProxyPolicy
{
    /**
     * Only allows requests to known Nexus repositories.
     */
    STRICT,

    /**
     * Routes to Nexus for known reposes, but does real HTTP Proxying to unknown repositories. It may gather unknown
     * repo URLs for later reference.
     */
    PASS_THRU;

    public static HttpProxyPolicy fromModel( String string )
    {
        if ( CHttpProxySettings.PROXY_POLICY_STRICT.equals( string ) )
        {
            return STRICT;
        }
        else if ( CHttpProxySettings.PROXY_POLICY_PASS_THRU.equals( string ) )
        {
            return PASS_THRU;
        }
        else
        {
            return null;
        }
    }

    public static String toModel( HttpProxyPolicy proxyMode )
    {
        return proxyMode.toString();
    }

    public String toString()
    {
        if ( STRICT.equals( this ) )
        {
            return CHttpProxySettings.PROXY_POLICY_STRICT;
        }
        else if ( PASS_THRU.equals( this ) )
        {
            return CHttpProxySettings.PROXY_POLICY_PASS_THRU;
        }
        else
        {
            return null;
        }
    }

}
