package org.sonatype.security.ldap.dao;

import java.util.SortedSet;

import javax.naming.NamingException;

import org.jsecurity.realm.ldap.LdapContextFactory;

public interface LdapConnectionTester
{
    public void testConnection( LdapContextFactory ldapContextFactory ) throws NamingException;
    
    public SortedSet<LdapUser> testUserAndGroupMapping( LdapContextFactory ldapContextFactory, LdapAuthConfiguration ldapAuthConfiguration, int numberOfResults  ) throws LdapDAOException, NamingException;

}
