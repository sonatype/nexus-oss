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
