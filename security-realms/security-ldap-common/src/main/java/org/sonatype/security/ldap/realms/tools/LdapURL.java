/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
