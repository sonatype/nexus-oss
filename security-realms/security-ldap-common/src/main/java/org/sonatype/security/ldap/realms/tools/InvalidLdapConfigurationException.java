/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.tools;

public class InvalidLdapConfigurationException
    extends RuntimeException
{

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = 4559396496795216079L;

    public InvalidLdapConfigurationException()
    {
    }

    public InvalidLdapConfigurationException( String message )
    {
        super( message );
    }

    public InvalidLdapConfigurationException( Throwable cause )
    {
        super( cause );
    }

    public InvalidLdapConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
