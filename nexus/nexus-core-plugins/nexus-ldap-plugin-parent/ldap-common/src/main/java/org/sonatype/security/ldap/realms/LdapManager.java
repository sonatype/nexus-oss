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
