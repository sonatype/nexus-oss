/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
