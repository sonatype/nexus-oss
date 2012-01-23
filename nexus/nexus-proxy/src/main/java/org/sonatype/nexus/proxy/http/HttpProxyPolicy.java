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
