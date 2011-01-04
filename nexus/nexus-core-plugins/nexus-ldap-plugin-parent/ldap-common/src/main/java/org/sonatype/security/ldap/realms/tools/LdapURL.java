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
package org.sonatype.security.ldap.realms.tools;

import java.net.MalformedURLException;

import org.codehaus.plexus.util.StringUtils;

/**
 * It would be nice to create a Protocol Handler, but thats a bit over kill.
 */
public class LdapURL
{

    private String protocol;

    private String host;

    private int port;

    private String searchBase;

    public LdapURL( String protocol, String host, int port, String searchBase )
        throws MalformedURLException
    {

        if ( StringUtils.isEmpty( protocol ) )
        {
            throw new MalformedURLException( "LDAP protocol can not be empty." );
        }
        if ( StringUtils.isEmpty( host ) )
        {
            throw new MalformedURLException( "LDAP host can not be empty." );
        }
        if ( port < 1 )
        {
            throw new MalformedURLException( "LDAP port is not a valid port." );
        }
        if ( StringUtils.isEmpty( searchBase ) )
        {
            throw new MalformedURLException( "LDAP searchBase can not be empty." );
        }

        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.searchBase = searchBase;

    }

    public String toString()
    {
        return protocol + "://" + host + ":" + port + "/" + searchBase;
    }

    /**
     * @return the protocol
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol( String protocol )
    {
        this.protocol = protocol;
    }

    /**
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost( String host )
    {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort( int port )
    {
        this.port = port;
    }

    /**
     * @return the searchBase
     */
    public String getSearchBase()
    {
        return searchBase;
    }

    /**
     * @param searchBase the searchBase to set
     */
    public void setSearchBase( String searchBase )
    {
        this.searchBase = searchBase;
    }

}
