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
package org.sonatype.security.ldap.dao;

import java.util.SortedSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;


/**
 * @author cstamas
 */
public interface LdapUserDAO
{
    public static final String REALM_KEY = "LDAP";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String EMAIL = "email";

    public static final String NAME = "name";

    public static final String WEBSITE = "website";

    public PasswordEncoderManager getPasswordEncoderManager();

    public void setPasswordEncoderManager( PasswordEncoderManager passwordEncoder );

    public void removeUser( String username, LdapContext context, LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException;

    public void updateUser( LdapUser user, LdapContext context, LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException;

    public void changePassword( String username, String password, LdapContext context,
                                LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException;

    public NamingEnumeration<SearchResult> searchUsers( String username, LdapContext context, LdapAuthConfiguration configuration,
                                          long limitCount )
        throws NamingException;

    public NamingEnumeration<SearchResult> searchUsers( LdapContext context, LdapAuthConfiguration configuration, long limitCount )
        throws NamingException;

    public NamingEnumeration<SearchResult> searchUsers( LdapContext context, String[] returnAttributes,
                                          LdapAuthConfiguration configuration, long limitCount )
        throws NamingException;

    public NamingEnumeration<SearchResult> searchUsers( String username, LdapContext context, String[] returnAttributes,
                                          LdapAuthConfiguration configuration, long limitCount )
        throws NamingException;

    public SortedSet<LdapUser> getUsers( LdapContext context, LdapAuthConfiguration configuration, long limitCount )
        throws LdapDAOException;

    public SortedSet<LdapUser> getUsers( String username, LdapContext context, LdapAuthConfiguration configuration,
                                         long limitCount )
        throws LdapDAOException;

    public void createUser( LdapUser user, LdapContext context, LdapAuthConfiguration configuration )
        throws LdapDAOException;

    public LdapUser getUser( String username, LdapContext context, LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException;

}
