/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;

import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

@Component( role = LdapConfiguration.class, hint = "UsersGroupAuthTestLdapConfiguration" )
public class UsersGroupAuthTestLdapConfiguration
    extends  DefaultLdapConfiguration
{

    private LdapAuthConfiguration ldapAuthConfiguration;
    
    private CConnectionInfo connectionInfo;

    /**
     * @param ldapAuthConfiguration the ldapAuthConfiguration to set
     */
    public void setLdapAuthConfiguration( LdapAuthConfiguration ldapAuthConfiguration )
    {
        this.ldapAuthConfiguration = ldapAuthConfiguration;
    }

    public LdapAuthConfiguration getLdapAuthConfiguration()
    {
        return this.ldapAuthConfiguration;
    }

    public void setConnectionInfo( CConnectionInfo connectionInfo)
    {
        this.connectionInfo = connectionInfo;
    }
    
    public CConnectionInfo readConnectionInfo()
    {
        return connectionInfo;
    }
    
    public void clearCache()
    {
        
    }

    public void save()
    {
        
    }

}
