/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api;

import org.codehaus.plexus.component.annotations.Component;

import org.sonatype.nexus.security.ldap.realms.DefaultLdapManager;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;

@Component(role=LdapManager.class, hint="TestLdapManager")
public class TestLdapManager extends DefaultLdapManager
{
    
    private LdapConfiguration ldapConfiguration;

    public LdapConfiguration getLdapConfiguration()
    {
        return ldapConfiguration;
    }

    public void setLdapConfiguration( LdapConfiguration ldapConfiguration )
    {
        this.ldapConfiguration = ldapConfiguration;
    }
    
    
    

}
