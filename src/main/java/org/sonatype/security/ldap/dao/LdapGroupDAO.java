/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

import java.util.Set;

import javax.naming.ldap.LdapContext;

/**
 * @author cstamas
 */
public interface LdapGroupDAO
{

    public Set<String> getGroupMembership( String username, LdapContext context, LdapAuthConfiguration configuration )
        throws LdapDAOException, NoLdapUserRolesFoundException;

    public Set<String> getAllGroups( LdapContext context, LdapAuthConfiguration configuration )
        throws LdapDAOException;
    
    public String getGroupName( String groupId, LdapContext context, LdapAuthConfiguration configuration )
    throws LdapDAOException, NoSuchLdapGroupException;

}
