/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.util.Set;
import java.util.SortedSet;

import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;


public interface LdapManager
{
    public LdapUser authenticateUser( String userId, String password ) throws AuthenticationException;

    public abstract Set<String> getUserRoles( String userId )
        throws LdapDAOException, NoLdapUserRolesFoundException;

    public abstract SortedSet<LdapUser> getAllUsers()
        throws LdapDAOException;
    
    public abstract SortedSet<LdapUser> getUsers( int userCount )
    throws LdapDAOException;

    public abstract LdapUser getUser( String username )
        throws NoSuchLdapUserException,
            LdapDAOException;
    
    public abstract SortedSet<LdapUser> searchUsers( String username )
        throws LdapDAOException;
    
    public abstract SortedSet<String> getAllGroups()
    throws LdapDAOException;
    
    public abstract String getGroupName( String groupId )
    throws LdapDAOException, NoSuchLdapGroupException;
    
}
