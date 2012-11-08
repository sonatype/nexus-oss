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
package org.sonatype.nexus.rest;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Form;
import org.restlet.data.Request;

public class RemoteIPFinder
{
    protected static final String FORWARD_HEADER = "X-Forwarded-For";

    public static String findIP( HttpServletRequest request )
    {
        String forwardedIP = getFirstForwardedIp( request.getHeader( FORWARD_HEADER ) );

        if ( forwardedIP != null )
        {
            return forwardedIP;
        }

        String remoteIP = request.getRemoteAddr();

        if ( remoteIP != null )
        {
            return remoteIP;
        }

        return null;
    }

    public static String findIP( Request request )
    {
        Form form = (Form) request.getAttributes().get( "org.restlet.http.headers" );

        String forwardedIP = getFirstForwardedIp( form.getFirstValue( FORWARD_HEADER ) );

        if ( forwardedIP != null )
        {
            return forwardedIP;
        }

        List<String> ipAddresses = request.getClientInfo().getAddresses();

        return resolveIp( ipAddresses );
    }

    protected static String getFirstForwardedIp( String forwardedFor )
    {
        if ( !StringUtils.isEmpty( forwardedFor ) )
        {
            String[] forwardedIps = forwardedFor.split( "," );

            return resolveIp( Arrays.asList( forwardedIps ) );
        }

        return null;
    }

    private static String resolveIp( List<String> ipAddresses )
    {
        String ip0 = null;
        String ip4 = null;
        String ip6 = null;

        if ( ipAddresses.size() > 0 )
        {
            ip0 = ipAddresses.get( 0 );

            for ( String ip : ipAddresses )
            {

                InetAddress ipAdd;
                try
                {
                    ipAdd = InetAddress.getByAddress( ip.getBytes() );
                }
                catch ( UnknownHostException e )
                {
                    continue;
                }
                if ( ipAdd instanceof Inet4Address )
                {
                    ip4 = ip;
                    continue;
                }
                if ( ipAdd instanceof Inet6Address )
                {
                    ip6 = ip;
                    continue;
                }
            }
        }

        if ( ip4 != null )
        {
            return ip4;
        }
        if ( ip6 != null )
        {
            return ip6;
        }

        return ip0;
    }
}
