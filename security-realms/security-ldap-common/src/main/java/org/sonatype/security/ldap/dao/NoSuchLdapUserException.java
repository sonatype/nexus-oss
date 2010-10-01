/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

public class NoSuchLdapUserException
    extends Exception
{

    private final String username;

    public NoSuchLdapUserException( String username )
    {
        super( "No such user: " + username );
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

}
