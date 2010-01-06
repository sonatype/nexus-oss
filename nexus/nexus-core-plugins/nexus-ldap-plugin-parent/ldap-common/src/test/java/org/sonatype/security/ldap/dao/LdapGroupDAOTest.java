/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;

import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapGroupDAO;


public class LdapGroupDAOTest
    extends AbstractLdapTestEnvironment
{

    public void testSimple()
        throws Exception
    {
        Map<String, Object> env = new HashMap<String, Object>();
        // Create a new context pointing to the overseas partition
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://localhost:12345/o=sonatype" );
        env.put( Context.SECURITY_PRINCIPAL, "uid=admin,ou=system" );
        env.put( Context.SECURITY_CREDENTIALS, "secret" );
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );

        // if want to use explicitly ApacheDS and not the Sun supplied ones
        // env.put( Context.PROVIDER_URL, "o=sonatype" );
        // env.put( Context.INITIAL_CONTEXT_FACTORY, "org.apache.directory.server.jndi.ServerContextFactory" );

        InitialLdapContext initialContext = new InitialLdapContext( new Hashtable<String, Object>( env ), null );

        LdapAuthConfiguration configuration = new LdapAuthConfiguration();
        configuration.setUserBaseDn( "ou=people" );
        configuration.setGroupBaseDn( "ou=groups" );
        configuration.setGroupObjectClass( "groupOfUniqueNames" );
        configuration.setGroupMemberAttribute( "uniqueMember" );
        configuration.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        configuration.setUserMemberOfAttribute( "" );

        LdapGroupDAO lgm = (LdapGroupDAO) lookup( LdapGroupDAO.class.getName() );

        Set<String> groups = lgm.getGroupMembership( "cstamas", initialContext, configuration );
        assertTrue( groups.contains( "public" ) );
        assertTrue( groups.contains( "snapshots" ) );

        groups = lgm.getGroupMembership( "brianf", initialContext, configuration );
        assertTrue( groups.contains( "public" ) );
        assertTrue( groups.contains( "releases" ) );

        groups = lgm.getGroupMembership( "jvanzyl", initialContext, configuration );
        assertTrue( groups.contains( "public" ) );
        assertTrue( groups.contains( "releases" ) );
        assertTrue( groups.contains( "snapshots" ) );
    }
}
