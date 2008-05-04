/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.access.ldap;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.nexus.proxy.access.RepositoryPermission;

import com.sonatype.security.ldap.mgmt.LdapAuthConfiguration;

public class DefaultLdapManagerTest
    extends AbstractLdapTestEnvironment
{

    public void testSimple()
        throws Exception
    {
        Map<String, Object> env = new HashMap<String, Object>();
        // Create a new context pointing to the partition
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://localhost:12345/o=sonatype" );
        env.put( Context.SECURITY_PRINCIPAL, "uid=admin,ou=system" );
        env.put( Context.SECURITY_CREDENTIALS, "secret" );
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );

        LdapAuthConfiguration configuration = new LdapAuthConfiguration();
        configuration.setUserBaseDn( "ou=people" );
        configuration.setGroupBaseDn( "ou=groups" );
        configuration.setGroupObjectClass( "groupOfUniqueNames" );
        configuration.setGroupMemberAttribute( "uniqueMember" );
        configuration.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );

        Map<String, String> perm2role = new HashMap<String, String>( 4 );
        perm2role.put( RepositoryPermission.RETRIEVE.toString(), "repoconsumer" );
        perm2role.put( RepositoryPermission.LIST.toString(), "repoconsumer" );
        perm2role.put( RepositoryPermission.STORE.toString(), "developer" );
        perm2role.put( RepositoryPermission.DELETE.toString(), "repomaintainer" );
        configuration.setGroupMappings( perm2role );

        LdapManager ldapManager = (LdapManager) lookup( LdapManager.ROLE );

        LdapConsumer ldapConsumer = new LdapConsumer();
        // connection related stuff
        ldapConsumer.getConfiguration().putAll( env );
        // auth related stuff
        ldapConsumer.setLdapAuthConfiguration( configuration );
        // the manager
        ldapConsumer.setLdapManager( ldapManager );

        // cstamas is repoconsumer, developer
        assertTrue( ldapConsumer.ldapAuthenticate( "cstamas", "cstamas123" ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "cstamas", "/", RepositoryPermission.RETRIEVE ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "cstamas", "/", RepositoryPermission.LIST ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "cstamas", "/", RepositoryPermission.STORE ) );
        assertEquals( false, ldapConsumer.ldapAuthorize( "cstamas", "/", RepositoryPermission.DELETE ) );

        // brian is repoconsumer, repomaintainer
        assertTrue( ldapConsumer.ldapAuthenticate( "brianf", "brianf123" ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "brianf", "/", RepositoryPermission.RETRIEVE ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "brianf", "/", RepositoryPermission.LIST ) );
        assertEquals( false, ldapConsumer.ldapAuthorize( "brianf", "/", RepositoryPermission.STORE ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "brianf", "/", RepositoryPermission.DELETE ) );

        // jvanzyl is repoconsumer, repomaintainer, developer
        assertTrue( ldapConsumer.ldapAuthenticate( "jvanzyl", "jvanzyl123" ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "jvanzyl", "/", RepositoryPermission.RETRIEVE ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "jvanzyl", "/", RepositoryPermission.LIST ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "jvanzyl", "/", RepositoryPermission.STORE ) );
        assertEquals( true, ldapConsumer.ldapAuthorize( "jvanzyl", "/", RepositoryPermission.DELETE ) );

        // jdcasey is not in these groups
        assertTrue( ldapConsumer.ldapAuthenticate( "jdcasey", "jvanzyl123" ) );
        assertEquals( false, ldapConsumer.ldapAuthorize( "jdcasey", "/", RepositoryPermission.RETRIEVE ) );
        assertEquals( false, ldapConsumer.ldapAuthorize( "jdcasey", "/", RepositoryPermission.LIST ) );
        assertEquals( false, ldapConsumer.ldapAuthorize( "jdcasey", "/", RepositoryPermission.STORE ) );
        assertEquals( false, ldapConsumer.ldapAuthorize( "jdcasey", "/", RepositoryPermission.DELETE ) );

    }
}
