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

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component( role = LdapConnectionTester.class )
public class DefaultLdapConnectionTester
    implements LdapConnectionTester
{

    @Requirement
    private LdapUserDAO ldapUserDao;

    @Requirement
    private LdapGroupDAO ldapGroupDAO;

    private Logger logger = LoggerFactory.getLogger( getClass() );

    public void testConnection( LdapContextFactory ldapContextFactory )
        throws NamingException
    {
        // get the connection and close it, if this throws an exception, then the config is wrong.
        LdapContext ctx = null;
        try
        {
            ctx = ldapContextFactory.getSystemLdapContext();
            ctx.getAttributes( "" );
        }
        finally
        {
            if ( ctx != null )
            {
                try
                {
                    ctx.close();
                }
                catch ( NamingException e )
                {
                    // ignore, it might not even be open
                }
            }
        }

    }

    public SortedSet<LdapUser> testUserAndGroupMapping( LdapContextFactory ldapContextFactory,
        LdapAuthConfiguration ldapAuthConfiguration, int numberOfResults )
        throws LdapDAOException,
            NamingException
    {
        LdapContext ctx = ldapContextFactory.getSystemLdapContext();
        try
        {
            SortedSet<LdapUser> users = this.ldapUserDao.getUsers(
                ldapContextFactory.getSystemLdapContext(),
                ldapAuthConfiguration,
                numberOfResults );

            if ( ldapAuthConfiguration.isLdapGroupsAsRoles()
                && StringUtils.isEmpty( ldapAuthConfiguration.getUserMemberOfAttribute() ) )
                for ( LdapUser ldapUser : users )
                {
                    try
                    {
                        ldapUser.setMembership( this.ldapGroupDAO.getGroupMembership( ldapUser.getUsername(), ctx, ldapAuthConfiguration ) );
                    }
                    catch ( NoLdapUserRolesFoundException e )
                    {
                        // this is ok, the users has no roles, not a problem
                        if ( logger.isDebugEnabled() )
                        {
                            this.logger.debug( "While testing for user mapping user: " + ldapUser.getUsername()
                                + " had no roles." );
                        }
                    }
                }
            return users;
        }
        finally
        {
            try
            {
                ctx.close();
            }
            catch ( NamingException e )
            {
                // ignore, it might not even be open
            }
        }
    }

}
