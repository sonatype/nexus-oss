/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.util.Collection;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.realms.LdapManager;


public class DefaultLdapManagerTest
    extends AbstractLdapTestEnvironment
{

    private LdapManager getLdapManager()
        throws Exception
    {
        return (LdapManager) this.lookup( LdapManager.class );
    }

    public void testGetAll()
        throws Exception
    {
        LdapManager ldapManager = this.getLdapManager();

        Collection<LdapUser> users = ldapManager.getAllUsers();
        Assert.assertEquals( 3, users.size() );

        // NOTE: implementation detail, -1 == all
        Assert.assertEquals( 3, ldapManager.getUsers( -1 ).size() );
    }

    public void testGetLimit()
        throws Exception
    {
        LdapManager ldapManager = this.getLdapManager();

        Assert.assertEquals( 2, ldapManager.getUsers( 2 ).size() );
    }

    public void testSort() throws Exception
    {
        LdapManager ldapManager = this.getLdapManager();

        Set<LdapUser> users = ldapManager.getAllUsers();
        Assert.assertEquals( 3, users.size() );
        
        String[] orderedUsers = {"brianf", "cstamas", "jvanzyl"};
        
        int index = 0;
        for ( LdapUser user : users )
        {
            Assert.assertEquals( orderedUsers[index++], user.getUsername() );
        }
        
        
        
    }

    @Override
    protected void customizeContext( Context context )
    {
        context.put( "application-conf", getBasedir() + "/target/test-classes/test-conf/conf/" );
    }
}
