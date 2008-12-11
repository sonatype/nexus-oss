/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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
